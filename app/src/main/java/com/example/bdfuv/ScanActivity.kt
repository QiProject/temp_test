package com.example.bdfuv

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bdfuv.adapters.ScanDeviceAdapter
import com.example.bdfuv.databinding.ActivityScanBinding
import com.example.bdfuv.manager.BLEManager
import com.example.bdfuv.model.BLEGattWrapper
import com.example.bdfuv.util.Constant
import com.example.bdfuv.util.RxBus
import com.example.bdfuv.util.RxBusKeyValuePair
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_scan.*
import java.util.*
import kotlin.concurrent.timerTask


class ScanActivity : BaseActivity<ActivityScanBinding>() {
    override fun getLayoutId(): Int {
        return R.layout.activity_scan
    }


    val deviceList: ArrayList<BLEManager.DeviceWithRssi> = ArrayList()
    var lastUpdateDate = Date()

    private val RECORD_REQUEST_CODE = 101
    private val TAG = "Permission"

    private val scanDeviceAdapter = ScanDeviceAdapter(deviceList)


    //具体订阅方式要这样来，我们先在当前 Activity 里维护一个 CompositeDisposable 实例，用于订阅消息和取消订阅关系，避免内存泄漏：
    val mCompositeDisposable: CompositeDisposable = CompositeDisposable()

    var t = getTimerTask()


    fun getTimerTask(): TimerTask {
        return timerTask {
            println(">>>> timer triggered")

//            synchronized(this) {
//                val now = Date()
//                //2秒都沒收到藍牙廣播，就假設裝置已消失
//                for (device in deviceList)
//                    if (now.time - device.lastDiscoverDate.time > 2000L) {
//                        deviceList.remove(device)
//                    }
//            }

            runOnUiThread {
                bindingView!!.scanRecyclerView.adapter!!.notifyDataSetChanged()
            }

            Log.d("Art", "notfiyDataSetChanged")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindingView!!.scanRecyclerView.layoutManager = LinearLayoutManager(this)
        bindingView!!.scanRecyclerView.adapter = scanDeviceAdapter

        scanDeviceAdapter.onItemClick = {
            Log.d("art", ">>>clicked = " + it.bluetoothDevice.address)

            scanBLEDevice(false)

            setProgressBar(true)
            block_click_view.visibility = View.VISIBLE
            BLEManager.getInstance(this).connectToDevice(it.bluetoothDevice)
        }


        var pm: PackageManager = packageManager

        if (!pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Device doesn't support ble", Toast.LENGTH_LONG).show()
        }

        setupPermissions()

        mCompositeDisposable.add(subscribeScanResult())
        mCompositeDisposable.add(subscribeScanTimeout())
        mCompositeDisposable.add(subscribeConnectionChange())
    }

    override fun onDestroy() {
        super.onDestroy()
        mCompositeDisposable.clear()
        Log.d("art", "ScanActivity on Destroy")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d("art", "ScanActivity on Restart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("art", "ScanActivity on Resume")
        scanBLEDevice(true)
    }

    override fun onPause() {
        super.onPause()
        Log.d("art", "ScanActivity on Pause")
        scanBLEDevice(false)
    }

    override fun onStop() {
        super.onStop()
        Log.d("art", "ScanActivity on Stop")
        setProgressBar(false)
        block_click_view.visibility = View.GONE
        mCompositeDisposable.clear()
    }


    private fun triggerTimer(enable: Boolean) {
        if (enable) {
            t = getTimerTask()
            Timer().scheduleAtFixedRate(t, 1000, 1000)
        } else {
            t.cancel()
        }
    }


    private fun subscribeConnectionChange() =
        RxBus.toFlowable(RxBusKeyValuePair::class.java).observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it.eventId == Constant.BLUETOOTH_CONNECTION_STATE_CONNECTED) {
                    val gattwrapper = it.data
                    if (gattwrapper is BLEGattWrapper) {

                    }
                    Log.d("art", "BLUETOOTH_CONNECTION_STATE_CONNECTED")

                    runOnUiThread {
                        Toast.makeText(this, "Connected, initializing Device ", Toast.LENGTH_SHORT).show()
                    }

                    val intent = Intent(this, EngineerActivity::class.java)
                    startActivity(intent)

                } else if (it.eventId == Constant.BLUETOOTH_CONNECTION_STATE_DISCONNECTED) {
                    //連線失敗，重新搜尋裝置
                    Log.d("art", "BLUETOOTH_CONNECTION_STATE_DISCONNECTED")

                    scanBLEDevice(true)
                    setProgressBar(false)
                    block_click_view.visibility = View.GONE
                    runOnUiThread {
                        Toast.makeText(this, "connect fail, try again", Toast.LENGTH_SHORT).show()
                    }
                }
            }


    private fun subscribeScanResult() =
        RxBus.toFlowable(RxBusKeyValuePair::class.java).observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it.eventId == Constant.BLUETOOTH_SCAN_RESULT) {
                    val device = it.data
                    if (device is BLEManager.DeviceWithRssi) {
                        println(">>>>ScanActivity device:" + device.bluetoothDevice.name)
                        updateDeviceList(device)
                    }
                }
            }

    private fun subscribeScanTimeout() =
        RxBus.toFlowable(RxBusKeyValuePair::class.java).observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it.eventId == Constant.BLUETOOTH_SCAN_TIMEOUT) {
                    showRescanIcon()
                    Log.d("art", "scan time out")
                }
            }

    private fun showRescanIcon() {
        invalidateOptionsMenu()
        deviceList.clear()
        scanDeviceAdapter.notifyDataSetChanged()
        triggerTimer(false)
    }

    private fun updateDeviceList(device: BLEManager.DeviceWithRssi) {

        try {
            val old = deviceList.filter { it.bluetoothDevice.address == device.bluetoothDevice.address }.first()
            val now = Date()

            //舊裝置一秒後才刷新rssi
            if (now.time - old.lastDiscoverDate.time > 1000L) {
                println(">>>> updateDeviceList: ${now.time}")
                old.rssi = device.rssi
                old.lastDiscoverDate = now
                deviceList.sortWith(compareByDescending({ it.rssi }))
            }
        } catch (e: NoSuchElementException) {
            println(">>>> new device:")

            deviceList.add(device)
            deviceList.sortWith(compareByDescending({ it.rssi }))
        }
    }


    private fun setupPermissions() {
        val cLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val fLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )


        if (cLocationPermission != PackageManager.PERMISSION_GRANTED || fLocationPermission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to record denied")
            makeRequest()
        }
    }

    private fun scanBLEDevice(enable: Boolean) {
        if (enable) {
            BLEManager.getInstance(this).startScanNearbyDevices()
            if (BLEManager.getInstance(this).isBluetoothEnable()) {
                triggerTimer(true)
            }
        } else {
            BLEManager.getInstance(this).stopScanNearbyDevices()
            triggerTimer(false)
        }
        invalidateOptionsMenu()
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            RECORD_REQUEST_CODE
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.scan_menu, menu)

        menu.findItem(R.id.action_refresh).isVisible = !BLEManager.getInstance(this).isBluetoothScanning()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_refresh -> {
                scanBLEDevice(true)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == Constant.REQUEST_ENABLE_BT) {
            BLEManager.getInstance(this).startScanNearbyDevices()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            RECORD_REQUEST_CODE -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Permission has been denied by user")
                } else {
                    Log.i(TAG, "Permission has been granted by user")
                    scanBLEDevice(false)
                    scanBLEDevice(true)
                }
            }
        }
    }


}

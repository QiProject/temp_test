package com.example.bdfuv


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bdfuv.adapters.ScanDeviceAdapter
import com.example.bdfuv.manager.BLEManager
import com.example.bdfuv.manager.RealmManager
import com.example.bdfuv.model.BLEGattWrapper
import com.example.bdfuv.model.HistoryDeviceModel
import com.example.bdfuv.util.Constant
import com.example.bdfuv.util.RxBus
import com.example.bdfuv.util.RxBusKeyValuePair
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_scan.block_click_view
import kotlinx.android.synthetic.main.activity_scan.scanRecyclerView
import java.util.*
import kotlin.concurrent.timerTask


class SelectDeviceFragment : Fragment() {
    private val TAG = "SelectDeviceFragment"

    val deviceList: ArrayList<BLEManager.DeviceWithRssi> = ArrayList()
    var lastUpdateDate = Date()

    private val RECORD_REQUEST_CODE = 101

    private val scanDeviceAdapter = ScanDeviceAdapter(deviceList)


    //具体订阅方式要这样来，我们先在当前 Activity 里维护一个 CompositeDisposable 实例，用于订阅消息和取消订阅关系，避免内存泄漏：
    val mCompositeDisposable: CompositeDisposable = CompositeDisposable()

    var t: TimerTask? = null
    var timer: Timer? = Timer()


    fun getTimerTask(): TimerTask {
        return timerTask {
            if (activity != null) {
                activity!!.runOnUiThread {
                    scanRecyclerView.adapter?.notifyDataSetChanged()
                }
            }else{
                println(">>>>timer null")
            }

            Log.d(TAG, "timer triggered notfiyDataSetChanged")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var binding =
            DataBindingUtil.inflate<com.example.bdfuv.databinding.FragmentSelectDeviceBinding>(
                inflater,
                R.layout.fragment_select_device,
                container,
                false
            )
        //set variables in Binding

        setHasOptionsMenu(true)

        BLEManager.getInstance(context!!).disconnectCurrent()

        return binding.getRoot()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "onActivityCreated: ")

        scanRecyclerView.layoutManager = LinearLayoutManager(context)
        scanRecyclerView.adapter = scanDeviceAdapter

        scanDeviceAdapter.onItemClick = {
            Log.d("art", ">>>clicked = " + it.bluetoothDevice.address)

            scanBLEDevice(false)

            setProgressBar(true)
            block_click_view.visibility = View.VISIBLE
            BLEManager.getInstance(context!!).connectToDevice(it.bluetoothDevice)
        }


        var pm: PackageManager = activity!!.packageManager

        if (!pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(context, "Device doesn't support ble", Toast.LENGTH_LONG).show()
        }

        setupPermissions()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.scan_menu, menu)

        menu.findItem(R.id.action_refresh).isVisible = !BLEManager.getInstance(context!!).isBluetoothScanning()

        menu.findItem(R.id.ic_connected).isVisible = BLEManager.getInstance(context!!).connectedGattWrapper != null
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
            BLEManager.getInstance(context!!).startScanNearbyDevices()
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

    override fun onStart() {
        super.onStart()
        (activity as MainActivity).checkBLE()
    }

    override fun onDestroy() {
        super.onDestroy()
        mCompositeDisposable.clear()
        Log.d(TAG, "$TAG on Destroy")
    }


    override fun onResume() {
        super.onResume()
        Log.d(TAG, "$TAG on Resume")
        mCompositeDisposable.add(subscribeScanResult())
        mCompositeDisposable.add(subscribeScanTimeout())
        mCompositeDisposable.add(subscribeConnectionChange())
        mCompositeDisposable.add(subscribeBLEinit())

        scanBLEDevice(true)
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "$TAG on Pause")
        scanBLEDevice(false)
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "$TAG on Stop")
        setProgressBar(false)
        block_click_view.visibility = View.GONE
        mCompositeDisposable.clear()
    }

    private fun subscribeBLEinit() =
        RxBus.toFlowable(RxBusKeyValuePair::class.java).observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                //TODO ENABLE_INDICATION_VALUE 若失敗 progress bar會跑不停
                if (it.eventId == Constant.RX_DEVICE_INIT_DONE) {
                    setProgressBar(false)

                    findNavController().popBackStack()
                }
            }


    private fun subscribeConnectionChange() =
        RxBus.toFlowable(RxBusKeyValuePair::class.java).observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it.eventId == Constant.BLUETOOTH_CONNECTION_STATE_CONNECTED) {
                    val gattwrapper = it.data

                    //save mac address for future fast pairing
                    if (gattwrapper is BLEGattWrapper) {
                        RealmManager.instance.deleteAllHistoryDevice()
                        RealmManager.instance.saveHistoryDevice(HistoryDeviceModel(gattwrapper.macAddress))
                    }

                    Log.d("art", "BLUETOOTH_CONNECTION_STATE_CONNECTED")

                    activity!!.runOnUiThread {
                        Toast.makeText(context, "Connected, initializing Device ", Toast.LENGTH_SHORT).show()
                    }

                } else if (it.eventId == Constant.BLUETOOTH_CONNECTION_STATE_DISCONNECTED) {
                    //連線失敗，重新搜尋裝置
                    Log.d("art", "BLUETOOTH_CONNECTION_STATE_DISCONNECTED")
                    scanBLEDevice(false)
                    scanBLEDevice(true)
                    setProgressBar(false)
                    block_click_view.visibility = View.GONE
                    activity!!.runOnUiThread {
                        Toast.makeText(context, "connect fail, try again", Toast.LENGTH_SHORT).show()
                    }
                }
            }


    private fun subscribeScanResult() =
        RxBus.toFlowable(RxBusKeyValuePair::class.java).observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it.eventId == Constant.BLUETOOTH_SCAN_RESULT) {
                    val device = it.data
                    if (device is BLEManager.DeviceWithRssi) {
                        println(">>>>$TAG device:" + device.bluetoothDevice.name)
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

    private fun scanBLEDevice(enable: Boolean) {
        if (enable) {
            BLEManager.getInstance(context!!).startScanNearbyDevices()
            if (BLEManager.getInstance(context!!).isBluetoothEnable()) {
                triggerTimer(true)
            }
        } else {
            BLEManager.getInstance(context!!).stopScanNearbyDevices()
            triggerTimer(false)
        }
        activity!!.invalidateOptionsMenu()
    }

    private fun triggerTimer(enable: Boolean) {
        if (enable) {
            timer?.cancel()
            timer?.purge()
            Log.d(TAG, "start timer")
            t = getTimerTask()
            timer = Timer()
            timer?.scheduleAtFixedRate(t, 1000, 1000)
        } else {
            Log.d(TAG, "cancel timer")
            timer?.cancel()
            timer?.purge()
        }
    }


    private fun showRescanIcon() {
        activity!!.invalidateOptionsMenu()
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

    fun setProgressBar(enable: Boolean) {
        Log.d(TAG, "set progress bar $enable")

        if (enable) {
            activity!!.progressBar.visibility = View.VISIBLE
        } else {
            activity!!.progressBar.visibility = View.GONE
        }
    }

    private fun setupPermissions() {
        val cLocationPermission = ContextCompat.checkSelfPermission(
            context!!,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val fLocationPermission = ContextCompat.checkSelfPermission(
            context!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        )


        if (cLocationPermission != PackageManager.PERMISSION_GRANTED || fLocationPermission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to record denied")
            //TODO  跳自訂dialog提醒用戶要開啟
            makeRequest()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(
            activity!!,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            RECORD_REQUEST_CODE
        )
    }

}

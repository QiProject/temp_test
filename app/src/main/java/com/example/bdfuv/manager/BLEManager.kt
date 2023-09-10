package com.example.bdfuv.manager

import android.bluetooth.*
import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.example.bdfuv.R
import com.example.bdfuv.model.BLEGattWrapper
import com.example.bdfuv.util.Constant
import com.example.bdfuv.util.Constant.BLUETOOTH_CONNECTION_STATE_CONNECTED
import com.example.bdfuv.util.Constant.BLUETOOTH_CONNECTION_STATE_DISCONNECTED
import com.example.bdfuv.util.Constant.BLUETOOTH_SCAN_RESULT
import com.example.bdfuv.util.Constant.BLUETOOTH_SCAN_TIMEOUT
import com.example.bdfuv.util.Constant.BLUETOOTH_SERVICE_DISCOVER
import com.example.bdfuv.util.RxBus
import com.example.bdfuv.util.RxBusKeyValuePair
import com.example.bdfuv.util.Tools
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.TimeUnit

class BLEManager {
    companion object {

        private var instance: BLEManager? = null

        fun getInstance(context: Context): BLEManager {
            if (instance == null) {  // NOT thread safe!
                instance = BLEManager(context)
            } else {
                instance!!.applicationContext = context
            }
            return instance!!
        }

    }

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothManager: BluetoothManager? = null

    private var isScanning = false
    private var scanTimeoutSec: Long = 30L

    private var bleScanner: BluetoothLeScanner? = null
    private var bleSettings: ScanSettings? = null

    /* 藍芽 Scan Callback*/
    private var bluetoothCallbackAbove21: ScanCallback? = null

    private val descriptorWriteQueue = LinkedList<BluetoothGattDescriptor>()
    private val characteristicsReadQueue = LinkedList<BluetoothGattCharacteristic>()

    private val TAG = "BLEManager"

    private var scanTimeoutSubscription: Disposable? = null

    var connectedGattWrapper: BLEGattWrapper? = null

    private var applicationContext: Context? = null

    val mCompositeDisposable: CompositeDisposable = CompositeDisposable()

    var scanSubscription: Disposable? = null

    constructor(applicationContext: Context?) {
        this.applicationContext = applicationContext

        bluetoothManager = applicationContext!!.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        if (bluetoothManager == null) {
            Toast.makeText(applicationContext, R.string.TOAST_BLUETOOTH_NOT_SUPPORTED, Toast.LENGTH_LONG).show()
            return
        }

        bluetoothAdapter = bluetoothManager!!.adapter
        if (bluetoothAdapter == null) {
            Toast.makeText(applicationContext, R.string.TOAST_BLUETOOTH_NOT_SUPPORTED, Toast.LENGTH_LONG).show()
            return
        }
        initBleComponent()
    }


    private fun initBleComponent() {
        bleScanner = bluetoothAdapter!!.getBluetoothLeScanner()
        bleSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        bluetoothCallbackAbove21 = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)

                //以名稱過濾ble裝置
                result.device.name?.let {
                    if (it.contains("clartic", true) || it.contains("detector", true)) {
                        Handler(Looper.getMainLooper()).post {
                            println(">>>device " + result.device.name)
                            RxBus.post(
                                RxBusKeyValuePair(
                                    BLUETOOTH_SCAN_RESULT,
                                    DeviceWithRssi(result.device, result.rssi, Date())
                                )
                            )
                        }
                    }
                }
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e(TAG, ">>> MyBluetoothManager errorCode$errorCode")
            }
        }
    }


    private fun writeGattDescriptor(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor) {
        checkIsInMainThread()
        descriptorWriteQueue.add(descriptor)
        if (descriptorWriteQueue.size == 1) {
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            gatt.writeDescriptor(descriptor)
        }
    }

    private fun setCharacteristicNotification(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        enabled: Boolean
    ) {
        checkIsInMainThread()
        gatt.setCharacteristicNotification(characteristic, enabled)
        println(">>>>>> set notification")
        val descriptor = characteristic.getDescriptor(
            Constant.CHARA_UPDATE_NOTIFICATION_DESCRIPTOR_UUID
        )
        if (enabled) {
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        } else {
            descriptor.value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
        }
        writeGattDescriptor(gatt, descriptor)
        descriptor.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
        writeGattDescriptor(gatt, descriptor)
    }


    /* 已連線的藍芽 狀態callback */
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            Log.w(
                TAG,
                if (newState == BluetoothProfile.STATE_CONNECTED) ">onConnectionStateChange connected" else ">onConnectionStateChange disconnected"
            )

            Handler(Looper.getMainLooper()).post {
                println(">>>>>onConnectionStateChange: $newState")
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    connectedGattWrapper = BLEGattWrapper(gatt)

                    RxBus.post(RxBusKeyValuePair(BLUETOOTH_CONNECTION_STATE_CONNECTED, connectedGattWrapper!!))
                    try {
                        Thread.sleep(900)
                    } catch (e: InterruptedException) {
                        println(">>>>>>sleep")
                        e.printStackTrace()
                    }

                    gatt.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    connectedGattWrapper = null
                    gatt.close()
                    val disconnectGattWrapper = BLEGattWrapper(gatt)
                    RxBus.post(
                        RxBusKeyValuePair(
                            BLUETOOTH_CONNECTION_STATE_DISCONNECTED,
                            disconnectGattWrapper
                        )
                    )
                }
            }
        }


        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                println(">>>onServicesDiscovered received: $status")
                return
            }

            // 掃蕩所有 BluetoothGattCharacteristic
            Handler(Looper.getMainLooper()).post {
                for (gattService in gatt.services) {
                    println(">>>onServicesDiscovered Service: $gattService")
                    for (gattCharacteristic in gattService.characteristics) {
                        println(">>>onServicesDiscovered chara: $gattCharacteristic")
                        configCharacteristic(gatt, gattService, gattCharacteristic)
                    }
                }
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (characteristicsReadQueue.size > 0) {
                characteristicsReadQueue.remove()
            }
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, ">onCharacteristicRead error: $status")
                println(">>>onCharacteristicRead error: ")
                return
            }

            println(">>>on read")

            Handler(Looper.getMainLooper()).post {
                extractValueFromCharacteristicAndBroadcast(
                    characteristic
                )
            }

            if (descriptorWriteQueue.size > 0) {
                gatt.writeDescriptor(descriptorWriteQueue.element())
            } else if (characteristicsReadQueue.size > 0) {
                gatt.readCharacteristic(characteristicsReadQueue.element())
            }
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Callback: Error writing GATT Descriptor: $status")
                println(">>>>錯誤：onDescriptorWrite fail!")
                return
            }
            if (connectedGattWrapper == null) {
                println(">>>>錯誤： onDescriptorWrite 找不到BLEGattWrapper!")
                return
            }

            println(">>>>onDescriptorWrite done")

            if (descriptor.value.contentEquals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)) {
                connectedGattWrapper!!.checkCharAndWriteDescriptorIndi(connectedGattWrapper!!.gatt)
                println(">>>>onDescriptorWrite write id")
            } else if (descriptor.value.contentEquals(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE)) {
                RxBus.post(RxBusKeyValuePair(Constant.RX_ENABLE_INDICATION_VALUE, true))
                println(">>>>onDescriptorWrite write ENABLE_INDICATION_VALUE complete")
            }

            // pop the item that we just finishing writing
            descriptorWriteQueue.remove()
            // If there is more to write, do it!
            if (descriptorWriteQueue.size > 0)
                gatt.writeDescriptor(descriptorWriteQueue.element())
            else if (characteristicsReadQueue.size > 0)
                gatt.readCharacteristic(characteristicsReadQueue.element())
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            Handler(Looper.getMainLooper()).post {
                //TODO 將character數值轉為 使用者看到的數值
                extractValueFromCharacteristicAndBroadcast(characteristic)
                println(">>>>onCharacteristicChanged :" + characteristic.uuid.toString())
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            println("bbbbbbbbbbbbbbbbbbbb")
            super.onCharacteristicWrite(gatt, characteristic, status)
            println(">>>>onCharacteristicWrite :" + characteristic!!.uuid.toString() + " status : " + status)

            if (status != GATT_SUCCESS) {
                println(">>>>onCharacteristicWrite gatt not success")

                //TODO
                //Can't create handler inside thread that has not called Looper.prepare()
                Toast.makeText(applicationContext, "charac write fail, status $status", Toast.LENGTH_SHORT).show()
            } else {
                println(
                    ">>>>onCharacteristicWrite :" + characteristic!!.uuid.toString() + " value : " + Tools.bytesToHex(
                        characteristic.value
                    )
                )

                if (gatt!!.device.address == connectedGattWrapper!!.device.address) {
                    when (characteristic.uuid.toString()) {
                        Constant.CONFIG_INDEX_UUID -> RxBus.post(
                            RxBusKeyValuePair(
                                Constant.RX_CONFIG_INDEX,
                                characteristic.value
                            )
                        )
                        Constant.CONFIG_VALUE_UUID -> RxBus.post(
                            RxBusKeyValuePair(
                                Constant.RX_CONFIG_VALUE,
                                characteristic.value
                            )
                        )
                    }
                }
            }
        }
    }


    private fun extractValueFromCharacteristicAndBroadcast(
        characteristic: BluetoothGattCharacteristic
    ) {
        val characteristicUuid = characteristic.uuid.toString()
        val value = characteristic.value

        /****
         * set the values, and ui will auto refresh via databindingView
         */
        when (characteristicUuid) {
            Constant.MEASUREMENT_DATA_UUID -> {
                connectedGattWrapper!!.measurementData(value)
            }


            Constant.NOTIFY_FROM_SERVER_UUID -> {
                connectedGattWrapper!!.notifyFromServer(value)
            }


            Constant.BATTERY_UUID -> {
                val battery = Tools.convertByteToIntSwap(value)
                connectedGattWrapper!!.battery = battery

                println(">>>>battery:$battery")
            }

            Constant.SERIAL_NUMBER_UUID -> {
                val sn = String(value, StandardCharsets.UTF_8)
                connectedGattWrapper!!.serialNumber = sn

                println(">>>>sn:$sn")
            }

            Constant.FIRMWARE_NUMBER_UUID -> {
                val firmware = String(value, StandardCharsets.UTF_8)
                connectedGattWrapper!!.firmwareRevision = firmware

                println(">>>>firmware:$firmware")
            }

            Constant.HARDWARE_NUMBER_UUID -> {
                val hardware = String(value, StandardCharsets.UTF_8)
                connectedGattWrapper!!.hardwareRevision = hardware

                println(">>>>hardware:$hardware")
            }

            Constant.CONFIG_INDEX_UUID -> {

                println(">>>>config index:${Tools.bytesToHex(value)}")
            }

            Constant.CONFIG_VALUE_UUID -> {
                println(">>>>config value:${Tools.bytesToHex(value)}")
                connectedGattWrapper!!.configValueRead(value)
            }
        }
    }


    inner class DeviceWithRssi internal constructor(
        val bluetoothDevice: BluetoothDevice,
        var rssi: Int,
        var lastDiscoverDate: Date
    )

    private fun configCharacteristic(
        gatt: BluetoothGatt,
        service: BluetoothGattService,
        characteristic: BluetoothGattCharacteristic
    ) {
        val serviceUuid = service.uuid.toString()
        val characteristicUuid = characteristic.uuid.toString()
        connectedGattWrapper!!.addCharac(characteristicUuid, characteristic)
        println(">>>發現特徵值 $characteristicUuid")
        when (characteristicUuid) {
            Constant.BATTERY_UUID, Constant.FIRMWARE_NUMBER_UUID
                , Constant.HARDWARE_NUMBER_UUID, Constant.SERIAL_NUMBER_UUID -> {
                readCharacteristic(
                    gatt,
                    characteristic

                )

                if (characteristicUuid == Constant.BATTERY_UUID) {
                    RxBus.post(RxBusKeyValuePair(Constant.RX_DEVICE_INIT_DONE, true))
                }
            }
            Constant.NOTIFY_FROM_SERVER_UUID, Constant.MEASUREMENT_DATA_UUID -> setCharacteristicNotification(
                gatt,
                characteristic,
                true
            )
        }
    }


    /* Config Ble Characteristic*/
    private fun readCharacteristic(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        println(">>>read charac $characteristic")
        checkIsInMainThread()
        characteristicsReadQueue.add(characteristic)
        if (characteristicsReadQueue.size == 1 && descriptorWriteQueue.size == 0)
            gatt.readCharacteristic(characteristic)
    }

    private fun checkIsInMainThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw IllegalStateException("此程式碼只能跑在Main Thread")
        }
    }

    private fun scanDeviceAboveApi21(enable: Boolean) {
        bleScanner = bluetoothAdapter!!.bluetoothLeScanner
        if (bleScanner == null) {
            return
        }
        if (!enable && !scanTimeoutSubscription?.isDisposed()!!) { // 要求關閉Scan 但有在scan
            bleScanner!!.stopScan(bluetoothCallbackAbove21)
            println(">>>>debug test,  stopScan(bluetoothCallbackAbove21)")

            if (scanTimeoutSubscription != null) {
                Log.d(TAG, "dispose scanTimeoutSubscription (autoConnect)")
                println(">>>>debug test,  mCompositeDisposable.remove(scanTimeoutSubscription!!)")
                mCompositeDisposable.remove(scanTimeoutSubscription!!)
            }


            if (scanSubscription != null) {
                Log.d(TAG, "dispose scanSubscription (autoConnect)")
                println(">>>>debug test,  mCompositeDisposable.remove(scanSubscription!!)")

                mCompositeDisposable.remove(scanSubscription!!)
            }
            Log.d(TAG, "dispose scan timeout")
            return
        }


        if (enable) {
            //TODO ble filter
            bleScanner!!.startScan(null, bleSettings, bluetoothCallbackAbove21)

            scanTimeoutSubscription = Observable.timer(scanTimeoutSec, TimeUnit.SECONDS).subscribe {
                bleScanner!!.stopScan(bluetoothCallbackAbove21)
                isScanning = false
                println(">>>>debug test,  RxBus.post(RxBusKeyValuePair(BLUETOOTH_SCAN_TIMEOUT, Unit))")
                RxBus.post(RxBusKeyValuePair(BLUETOOTH_SCAN_TIMEOUT, Unit))

                if (!enable && !scanSubscription?.isDisposed()!!) { // 要求關閉Scan時，移除scanSubscription
                    println(">>>>debug test,  remove(scanSubscription!!)")
                    mCompositeDisposable.remove(scanSubscription!!)
                }
            }

            if (scanTimeoutSubscription != null) {
                mCompositeDisposable.add(scanTimeoutSubscription!!)
                Log.d(TAG, "subscribe scanTimeout")
                println(">>>>debug test,  not null subscribe scanTimeout")
                Log.d(TAG, "subscribe scanTimeout")
            }
        }
    }


/* 以下為供外部使用的 Public Methods */

    fun isBluetoothEnable(): Boolean {
        return bluetoothAdapter!!.isEnabled()
    }

    fun enableBluetooth() {
        bluetoothAdapter!!.enable()
    }

    fun isBluetoothScanning(): Boolean {
        return isScanning
    }

    fun setScanTimeoutSec(sec: Long) {
        this.scanTimeoutSec = sec
    }

    fun startScanNearbyDevices() {
        isScanning = true
        if (bluetoothAdapter == null) {
            //TODO
            Toast.makeText(applicationContext, R.string.TOAST_BLUETOOTH_NOT_SUPPORTED, Toast.LENGTH_LONG).show()
            return
        }
        scanDeviceAboveApi21(true)
    }

    //TODO
    fun stopScanNearbyDevices() {
        if (bleScanner == null) {
            return
        }

        isScanning = false
        if (!isBluetoothEnable()) {
            Log.d(TAG, "ble disabled dispose scan timeout")
            scanTimeoutSubscription!!.dispose()
            return  // 藍芽關閉的狀態下 不必stop
        }

        scanDeviceAboveApi21(false)
    }

    fun connectToDevice(bluetoothDevice: BluetoothDevice): BluetoothGatt {
        checkIsInMainThread()
        return bluetoothDevice.connectGatt(applicationContext, false, gattCallback)
    }

    fun tryAutoConnect() {
        println(">>>>> tryAutoConnect")
        scanDeviceAboveApi21(true)

        //dispose scanSubscription first

        if (scanSubscription != null && !scanSubscription?.isDisposed!!) {
            mCompositeDisposable.remove(scanSubscription!!)
        }

        val macAddress = RealmManager.instance.retrieveFirstHistoryDevice()?.macAddress

        scanSubscription = subscribeScanResult(macAddress)

        mCompositeDisposable.add(scanSubscription!!)
    }


    private fun subscribeScanResult(macAddress: String?) =
        RxBus.toFlowable(RxBusKeyValuePair::class.java).observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it.eventId == Constant.BLUETOOTH_SCAN_RESULT) {
                    val device = it.data
                    if (device is BLEManager.DeviceWithRssi && device.bluetoothDevice.address == macAddress) {
                        stopScanNearbyDevices()
                        println(">>>>$TAG device found connecting:" + device.bluetoothDevice.name)
                        connectToDevice(device.bluetoothDevice)
                    }
                }
            }

    fun stopConnectingToDevice(connectingGatt: BluetoothGatt) {
        checkIsInMainThread()
        connectingGatt.disconnect()
        connectingGatt.close()
    }


    fun disconnectCurrent() {
        checkIsInMainThread()
        connectedGattWrapper?.gatt?.disconnect()
        connectedGattWrapper?.gatt?.close()
        connectedGattWrapper = null
    }

}
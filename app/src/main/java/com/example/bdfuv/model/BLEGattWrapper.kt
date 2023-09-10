package com.example.bdfuv.model

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.util.Log
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.example.bdfuv.util.Constant
import com.example.bdfuv.util.RxBus
import com.example.bdfuv.util.RxBusKeyValuePair
import com.example.bdfuv.util.Tools
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import org.reactivestreams.Subscription
import java.util.*

//revised by Teddy, 20200204
class BLEGattWrapper(
    /* Setter */

    // 不開放此getter
    var gatt: BluetoothGatt?
) : BaseObservable() {
    private val characteristicMap = HashMap<String, BluetoothGattCharacteristic>()
    var sessionStartTime: Date? = null // 用於記錄RealmTemperatureData 屬於哪個裝置的紀錄 在畫ReaderAccountChart 時 用來判斷資料點有沒有連線
    var userLoginName: String? = null
    private var deviceName: String? = null

    private val TAG = "BLEGattWrapper"

    @get:Bindable
    var serialNumber: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(com.example.bdfuv.BR.serialNumber)
        }

    var scenarioMode = "30sec"

    @get:Bindable
    var firmwareRevision: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(com.example.bdfuv.BR.firmwareRevision)
        }

    @get:Bindable
    var hardwareRevision: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(com.example.bdfuv.BR.hardwareRevision)
        }

    var adcmux: LEDModel? = null
        set(value) {
            field = value
            notifyChange()
        }

    var swGain: LEDModel? = null
        set(value) {
            field = value
            notifyChange()
        }

    var hwGain: LEDModel? = null
        set(value) {
            field = value
            notifyChange()
        }

    var efm8ChannelCurrent: LEDModel? = null
        set(value) {
            field = value
            notifyChange()
        }


    var isUserClickDisconnect = false // 使用者決定斷線
    var hydration: Int? = null
    var skinHealth: Int? = null
    var oil: Int? = null

    @get:Bindable
    var melanin: Int? = null
        set(value) {
            field = value
            notifyPropertyChanged(com.example.bdfuv.BR.melanin)
        }


    @get:Bindable
    var redness: Int? = null
        set(value) {
            field = value
            notifyPropertyChanged(com.example.bdfuv.BR.redness)
        }

    @get:Bindable
    var ch0: Int? = null
        set(value) {
            field = value
            notifyPropertyChanged(com.example.bdfuv.BR.ch0)
        }

    @get:Bindable
    var ch1: Int? = null
        set(value) {
            field = value
            notifyPropertyChanged(com.example.bdfuv.BR.ch1)
        }

    @get:Bindable
    var ch2: Int? = null
        set(value) {
            field = value
            notifyPropertyChanged(com.example.bdfuv.BR.ch2)
        }

    @get:Bindable
    var ch3: Int? = null
        set(value) {
            field = value
            notifyPropertyChanged(com.example.bdfuv.BR.ch3)
        }

    @get:Bindable
    var ch4: Int? = null
        set(value) {
            field = value
            notifyPropertyChanged(com.example.bdfuv.BR.ch4)
        }

    @get:Bindable
    var temp: Float? = null
        set(value) {
            field = value
            notifyPropertyChanged(com.example.bdfuv.BR.temp)
        }

    @get:Bindable
    var humid: Int? = null
        set(value) {
            field = value
            notifyPropertyChanged(com.example.bdfuv.BR.humid)
        }


    private var isWriteSuccess = false
    var state = CONNECTION_STATE.INIT
        private set

    @get:Bindable
    var battery: Int? = null
        set(value) {
            field = value
            notifyPropertyChanged(com.example.bdfuv.BR.battery)
        }

    var rxCharaWritten: Disposable? = null

    @get:Bindable
    var mode: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(com.example.bdfuv.BR.mode)
        }

    var configIndex: CONFIG_INDEX? = null

    @get:Bindable
    var adcmux_ch2: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(com.example.bdfuv.BR.adcmux_ch2)
        }

    @get:Bindable
    var adcmux_ch4: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(com.example.bdfuv.BR.adcmux_ch4)
        }
    //hexString
    var configValue: String? = null

    private var repeatNotifySubscription: Subscription? = null

    /* Method Call */
    val device: BluetoothDevice
        get() = gatt!!.device

    /* Getter */

    val macAddress: String
        get() = gatt!!.device.address

    val isSaveAvailable: Boolean
        get() = state == CONNECTION_STATE.SAVE

    var progressBarListener: ((enable:Boolean) -> Unit)? = null

    var toastListener: ((message:String) -> Unit)? = null

    enum class CONNECTION_STATE {
        INIT, START, SAVE
    }

    enum class CONFIG_INDEX {
        MODE, RATE, ADCMUX, HSIG,
        SW_GAIN, HW_GAIN, SWTRIGGER, EFM8_CHANNEL_CURRENT
    }

    enum class MODE {
        NORMAL, PRODUCTION
    }

    init {
        initProcess()
    }

    private fun tryEnableNotification(gatt: BluetoothGatt?) {

        //TODO oppo 手機重開機後的第一次連線 有很大機率enableNotification會失敗 所以要repeat到成功
        checkCharAndWriteDescriptor(gatt)
    }

    fun initProcess() {
        println(">>>>>>initProcess")
        tryEnableNotification(gatt)
    }

    private fun checkCharAndWriteDescriptor(gatt: BluetoothGatt?) {
        println(">>>>>checkCharAndWriteDescriptor")
        val characteristic = characteristicMap[Constant.NOTIFY_FROM_SERVER_UUID]
        if (characteristic == null) {
            println(">>>>Error 沒有此特徵值")
            return
        }
        gatt!!.setCharacteristicNotification(characteristic, true)
        val descriptor = characteristic.getDescriptor(
            Constant.CHARA_UPDATE_NOTIFICATION_DESCRIPTOR_UUID
        )
        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        gatt.writeDescriptor(descriptor)
    }

    public fun checkCharAndWriteDescriptorIndi(gatt: BluetoothGatt?) {
        println(">>>>>checkCharAndWriteDescriptor")
        val characteristic = characteristicMap[Constant.NOTIFY_FROM_SERVER_UUID]
        if (characteristic == null) {
            println(">>>>Error 沒有此特徵值")
            return
        }
        gatt!!.setCharacteristicNotification(characteristic, true)
        val descriptor = characteristic.getDescriptor(
            Constant.CHARA_UPDATE_NOTIFICATION_DESCRIPTOR_UUID
        )
        descriptor.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
        gatt.writeDescriptor(descriptor)
    }


    override fun hashCode(): Int {
        return if (gatt != null) gatt!!.hashCode() else 0
    }

    override fun toString(): String {
        return "裝置Address: $macAddress"
    }

    /***
     * write config index to MODE (0x05), and write config value (NORMAL 0x00, PRODUCTION 0x01)
     */

    fun writeConfigMode(type: MODE) {
        writeConfigIndex(BLEGattWrapper.CONFIG_INDEX.MODE)
        val byte = when (type) {
            MODE.NORMAL -> byteArrayOf(0x00.toByte())
            MODE.PRODUCTION -> byteArrayOf(0x01.toByte())
        }
        subscribeConfigIndexChange(byte)
    }

    fun writeSWTrigger() {
        writeConfigIndex(BLEGattWrapper.CONFIG_INDEX.SWTRIGGER)

        //write 0x01 to configValue when configIndex is changed
        subscribeConfigIndexChange(byteArrayOf(0x01.toByte()))
    }


    fun writeSWGain() {
        println(">>>>>>>>>swGain =" + swGain)
        writeConfigIndex(BLEGattWrapper.CONFIG_INDEX.SW_GAIN)

        println(">>>>>>>>>swGain!!.toBytes() = " + swGain!!.toBytes())
        subscribeConfigIndexChange(swGain!!.toBytes())
    }

    fun writeHWGain() {
        writeConfigIndex(BLEGattWrapper.CONFIG_INDEX.HW_GAIN)
        subscribeConfigIndexChange(hwGain!!.toBytes())
    }

    fun writeEFM8ChanCurrent() {
        writeConfigIndex(BLEGattWrapper.CONFIG_INDEX.EFM8_CHANNEL_CURRENT)
        subscribeConfigIndexChange(efm8ChannelCurrent!!.toBytes())
    }

    fun writeADCMUX() {
/*        var vis = 13
        var uv = 24
        when (type) {
            ADCMUX.visUV365 -> adcmux!!.ch2UV365 = vis
            ADCMUX.uvUV365 -> adcmux!!.ch2UV365 = uv
            ADCMUX.visUV385 -> adcmux!!.ch4UV385 = vis
            ADCMUX.uvUV385 -> adcmux!!.ch4UV385 = uv
        }*/
        writeConfigIndex(BLEGattWrapper.CONFIG_INDEX.ADCMUX)
        subscribeConfigIndexChange(adcmux!!.toBytes())
    }



/*    fun writeADCMUX(type: ADCMUX) {
        writeConfigIndex(BLEGattWrapper.CONFIG_INDEX.ADCMUX)
        var vis = 13
        var uv = 24
        when (type) {
            ADCMUX.uvUV365 -> println(">>>adcmux" + adcmux)
            //ADCMUX.uvUV365 -> adcmux!!.ch2UV365 = uv
        }
        //subscribeConfigIndexChange(adcmux!!.toBytes())
    }*/


    fun readSWGain() {
        println(">>>>>>>>>swGain =" + swGain)
        subscribeConfigIndexChange()
        println(">>>>>>>>>swGain =" + swGain)
        writeConfigIndex(CONFIG_INDEX.SW_GAIN)
        println(">>>>>>>>>swGain =" + swGain)
    }

    fun readHWGain() {
        subscribeConfigIndexChange()
        writeConfigIndex(CONFIG_INDEX.HW_GAIN)
    }

    fun readEFM8ChannelCurrent() {
        subscribeConfigIndexChange()
        writeConfigIndex(CONFIG_INDEX.EFM8_CHANNEL_CURRENT)

    }

    fun readADCMUX() {
        println(">>>>>>>>>>adcmux = " + adcmux)
        subscribeConfigIndexChange()
        println(">>>>>>>>>>adcmux = " + adcmux)
        writeConfigIndex(CONFIG_INDEX.ADCMUX)
        println(">>>>>>>>>>adcmux = " + adcmux)

    }

/*
    fun writeSensorMode(type: ADCMUX) {
        subscribeConfigIndexChange()
        writeConfigIndex(CONFIG_INDEX.ADCMUX)
        val byte = when(type) {
            ADCMUX.visUV365 -> characteristic.uuid!!.replace(0x0D.toByte())
            ADCMUX.uvUV365 -> byteArrayOf(0x01.toByte(), 0x0B.toByte(), 0x18.toByte(), 0x0D.toByte(), 0x18.toByte())
        }
        subscribeConfigIndexChange(byte)
    }
*/

    private fun subscribeConfigValueChange() {

        rxCharaWritten = RxBus.toFlowable(RxBusKeyValuePair::class.java).observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it.eventId == Constant.RX_CONFIG_VALUE) {
                    val bytes = it.data as ByteArray
                    println(">>>> config value changed, value :" + Tools.bytesToHex(bytes))
                    configValueRead(bytes)

                    rxCharaWritten!!.dispose()
                }
            }
    }



    /***
     * set the config index, so we can use it to decide what config value later
     */
    private fun updateConfigIndex(bytes: ByteArray) {
        //TODO 使用when的時候 == is false, why?
        //MODE
        if (Arrays.equals(bytes, byteArrayOf(0x5.toByte()))) {
            this.configIndex = CONFIG_INDEX.MODE
        } else if (Arrays.equals(bytes, byteArrayOf(0x0C.toByte()))) {
            this.configIndex = CONFIG_INDEX.ADCMUX
        } else if (Arrays.equals(bytes, byteArrayOf(0x8.toByte()))) {
            this.configIndex = CONFIG_INDEX.SWTRIGGER
        } else if (Arrays.equals(bytes, byteArrayOf(0x0E.toByte()))) {
            this.configIndex = CONFIG_INDEX.SW_GAIN
        } else if (Arrays.equals(bytes, byteArrayOf(0x0F.toByte()))) {
            this.configIndex = CONFIG_INDEX.HW_GAIN
        } else if (Arrays.equals(bytes, byteArrayOf(0x16.toByte()))) {
            this.configIndex = CONFIG_INDEX.EFM8_CHANNEL_CURRENT
        }
    }


    private fun subscribeConfigIndexChange(bytesToWrite: ByteArray) {
        //TODO 若onCharaWrite失敗，不會呼叫RxBus, rxCharaWritten就不會dispose, 可能會漏內存

        rxCharaWritten = RxBus.toFlowable(RxBusKeyValuePair::class.java).observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it.eventId == Constant.RX_CONFIG_INDEX) {
                    println(">>>> config index changed")
                    rxCharaWritten!!.dispose()

                    val bytes = it.data as ByteArray
                    updateConfigIndex(bytes)


                    when (this.configIndex) {
                        CONFIG_INDEX.MODE -> {
                            println(">>>>  write config value 0x05 (mode)")
                            subscribeConfigValueChange()
                            writeCharacteristic(Constant.CONFIG_VALUE_UUID, bytesToWrite)
                        }
                        CONFIG_INDEX.ADCMUX -> {
                            println(">>>>  write config value 0x0C (sensor mode)")
                            //subscribeConfigValueChange()
                            writeCharacteristic(Constant.CONFIG_VALUE_UUID, bytesToWrite)
                        }
                        CONFIG_INDEX.RATE -> TODO()
                        CONFIG_INDEX.HSIG -> TODO()
                        CONFIG_INDEX.SW_GAIN -> {
                            println(">>>>  write config value 0x0E (sw gain)")
                            writeCharacteristic(Constant.CONFIG_VALUE_UUID, bytesToWrite)
                        }
                        CONFIG_INDEX.HW_GAIN -> {
                            println(">>>>  write config value 0x0F (hw gain)")
                            writeCharacteristic(Constant.CONFIG_VALUE_UUID, bytesToWrite)
                        }
                        CONFIG_INDEX.SWTRIGGER -> {
                            progressBarListener?.invoke(true)
                            println(">>>>  write config value 0x08 (sw trigger)")
                            writeCharacteristic(Constant.CONFIG_VALUE_UUID, bytesToWrite)
                        }
                        null -> TODO()
                        CONFIG_INDEX.EFM8_CHANNEL_CURRENT -> {
                            println(">>>>  write config value 0x16 (efm8 channel current)")
                            writeCharacteristic(Constant.CONFIG_VALUE_UUID, bytesToWrite)
                        }
                    }
                }
            }
    }

    /****
     * read , 不需要傳入參數byte: ByteArray
     */
    private fun subscribeConfigIndexChange() {
        //TODO 若onCharaWrite失敗，不會呼叫RxBus, rxCharaWritten就不會dispose, 可能會漏內存
        println("aaaaaaaaaaaaaaaaaaaaa")
        rxCharaWritten = RxBus.toFlowable(RxBusKeyValuePair::class.java).observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it.eventId == Constant.RX_CONFIG_INDEX) {
                    println(">>>> config index changed")
                    rxCharaWritten!!.dispose()

                    val bytes = it.data as ByteArray
                    updateConfigIndex(bytes)

                    when (this.configIndex) {

                        CONFIG_INDEX.MODE -> TODO()
                        CONFIG_INDEX.RATE -> TODO()
                        CONFIG_INDEX.ADCMUX -> {
                            println(">>>>  read config value 0x0C")
                            readCharacteristic(Constant.CONFIG_VALUE_UUID)
                        }
                        CONFIG_INDEX.HSIG -> TODO()
                        CONFIG_INDEX.SW_GAIN -> {
                            println(">>>>  read config value 0x0E")
                            readCharacteristic(Constant.CONFIG_VALUE_UUID)
                        }
                        CONFIG_INDEX.HW_GAIN -> {
                            println(">>>>  read config value 0x0F")
                            readCharacteristic(Constant.CONFIG_VALUE_UUID)
                        }
                        CONFIG_INDEX.SWTRIGGER -> TODO()
                        null -> TODO()
                        CONFIG_INDEX.EFM8_CHANNEL_CURRENT -> {
                            println(">>>>  read config value 0x16")
                            readCharacteristic(Constant.CONFIG_VALUE_UUID)
                        }
                    }
                }
            }
    }


    fun writeConfigIndex(indexType: CONFIG_INDEX) {
        val data = getConfigIndexBytes(indexType)
        writeCharacteristic(Constant.CONFIG_INDEX_UUID, data)
    }


    private fun getConfigIndexBytes(indexType: CONFIG_INDEX): ByteArray {

        when (indexType) {
            CONFIG_INDEX.MODE -> return byteArrayOf(0x05.toByte())
            CONFIG_INDEX.RATE -> TODO()
            CONFIG_INDEX.ADCMUX -> return byteArrayOf(0x0C.toByte())
            CONFIG_INDEX.HSIG -> TODO()
            CONFIG_INDEX.SW_GAIN -> return byteArrayOf(0x0E.toByte())
            CONFIG_INDEX.HW_GAIN -> return byteArrayOf(0x0F.toByte())
            CONFIG_INDEX.SWTRIGGER -> return byteArrayOf(0x08.toByte())
            CONFIG_INDEX.EFM8_CHANNEL_CURRENT -> return byteArrayOf(0x16.toByte())
        }

        return byteArrayOf(0xff.toByte())
    }

    fun writeCharacteristic(key: String, data: ByteArray) {
        val characteristic = characteristicMap[key]
        println(">>>>>>>>>>key = " + key)
        println(">>>>>>>>>>data = " + data)
        println(">>>>>>>>>>characteristic = " + characteristic)
        if (characteristic == null) {
            Log.e("BluetoothGattWrapper錯誤", ">>>不存在此特徵值")
            return
        }
        characteristic.value = data
        gatt!!.writeCharacteristic(characteristic)
        println("write Chara :" + characteristic.uuid!!.toString())
    }

    fun readCharacteristic(key: String) {
        val characteristic = characteristicMap[key]
        gatt!!.readCharacteristic(characteristic)
    }

    fun disconnect() {
        gatt!!.disconnect()
    }

    fun close() {
        gatt!!.close()
    }

    fun getDeviceName(): String? {
        return deviceName
    }

    fun addCharac(key: String, characteristic: BluetoothGattCharacteristic) {
        this.characteristicMap[key] = characteristic
    }

    fun getCharac(key: String): BluetoothGattCharacteristic? {
        return characteristicMap[key]
    }

    fun setDeviceName(name: String) {
        this.deviceName = name
    }

    fun measurementData(value: ByteArray) {

        this.melanin = Tools.convertByteToIntSwap(value.copyOfRange(18, 20))
        this.redness = Tools.convertByteToIntSwap(value.copyOfRange(20, 22))
        this.ch0 = Tools.convertByteToInt(value.copyOfRange(3, 6))
        this.ch1 = Tools.convertByteToInt(value.copyOfRange(6, 9))
        this.ch2 = Tools.convertByteToInt(value.copyOfRange(9, 12))
        this.ch3 = Tools.convertByteToInt(value.copyOfRange(12, 15))
        this.ch4 = Tools.convertByteToInt(value.copyOfRange(15, 18))
        this.temp =  Tools.convertByteToInt(value.copyOfRange(0, 2)).toFloat() / 100
        this.humid = Tools.convertByteToInt(value.copyOfRange(2, 3))
        println(">>>>measurement bytes : " + Tools.bytesToHex(value))
        println(">>>> melanin: $melanin ,  redness: $redness   , ch2: $ch2  , ch4 $ch4   , temp: $temp   ,  humid: $humid")

        progressBarListener?.invoke(false)
    }

    fun notifyFromServer(value: ByteArray){
        Log.d(TAG, ">>>> notifyFromServer ")
        println(">>>>measurementData bytes : " + Tools.bytesToHex(value))

        when(value[0]){
            //hardware trigger
            0x04.toByte() -> progressBarListener?.invoke(true)
        }
    }



    fun configValueRead(value: ByteArray) {
        when (this.configIndex) {
            CONFIG_INDEX.MODE -> {
                val hexString = Tools.bytesToHex(value)
                when (hexString) {
                    "00" -> this.mode = "Normal"
                    "01" -> this.mode = "Production"
                }
            }
/*           CONFIG_INDEX.ADCMUX -> {
                val adcmux_ch2 = value[2].toInt()
                val adcmux_ch4 = value[4].toInt()
                when (adcmux_ch2) {
                    13 -> this.adcmux_ch2 = "Full"
                    24 -> this.adcmux_ch2 = "Reduced"
                }

                when (adcmux_ch4) {
                    13 -> this.adcmux_ch4 = "Full"
                    24 -> this.adcmux_ch4 = "Reduced"
                }
            }*/

            CONFIG_INDEX.ADCMUX -> {
                val ch0 = value[0].toInt()
                val ch1 = value[1].toInt()
                val ch2 = value[2].toInt()
                val ch3 = value[3].toInt()
                val ch4 = value[4].toInt()
                val adcmux_ch2 = value[2].toInt()
                val adcmux_ch4 = value[4].toInt()
                when (adcmux_ch2) {
                    0 -> this.adcmux_ch2 = "SmallIR"
                    1 -> this.adcmux_ch2 = "MediumIR"
                    2 -> this.adcmux_ch2 = "LargeIR"
                    11 -> this.adcmux_ch2 = "White"
                    13 -> this.adcmux_ch2 = "LargeWhite"
                    24 -> this.adcmux_ch2 = "UV"
                }

                when (adcmux_ch4) {
                    0 -> this.adcmux_ch4 = "SmallIR"
                    1 -> this.adcmux_ch4 = "MediumIR"
                    2 -> this.adcmux_ch4 = "LargeIR"
                    11 -> this.adcmux_ch4 = "White"
                    13 -> this.adcmux_ch4 = "LargeWhite"
                    24 -> this.adcmux_ch4 = "UV"
                }

                adcmux = LEDModel(ch0,ch1,ch2,ch3,ch4)
                println(">>>>adcmux:$adcmux")
            }
            CONFIG_INDEX.RATE -> TODO()
            CONFIG_INDEX.HSIG -> TODO()
            CONFIG_INDEX.SW_GAIN -> {
                val ch0 = value[0].toInt()
                val ch1 = value[1].toInt()
                val ch2 = value[2].toInt()
                val ch3 = value[3].toInt()
                val ch4 = value[4].toInt()

                swGain = LEDModel(ch0,ch1,ch2,ch3,ch4)
                println(">>>>swGain:$swGain")
            }
            CONFIG_INDEX.HW_GAIN -> {
                val ch0 = value[0].toInt()
                val ch1 = value[1].toInt()
                val ch2 = value[2].toInt()
                val ch3 = value[3].toInt()
                val ch4 = value[4].toInt()

                hwGain = LEDModel(ch0,ch1,ch2,ch3,ch4)
                println(">>>>hwGain:$hwGain")
            }
            CONFIG_INDEX.SWTRIGGER -> TODO()
            null -> TODO()
            CONFIG_INDEX.EFM8_CHANNEL_CURRENT -> {
                val ch0 = value[0].toInt()
                val ch1 = value[1].toInt()
                val ch2 = value[2].toInt()
                val ch3 = value[3].toInt()
                val ch4 = value[4].toInt()

                efm8ChannelCurrent = LEDModel(ch0,ch1,ch2,ch3,ch4)
                println(">>>>efm8 chan current:$efm8ChannelCurrent")
            }
        }
    }

    fun toMeasureData(area:String):MeasureDataModel{
        return MeasureDataModel(Date(),area,ch2!!,ch4!!,redness!!,melanin!!,temp!!,humid!!)
    }


    companion object {
        val IMPOSSIBLE_HIGHEST_NUMBER = -1000.0
        val IMPOSSIBLE_LOWEST_NUMBER = 1000.0
        val IMPOSSIBLE_VOLTAGE_NUMBER = 1000.0
    }
}
package com.example.bdfuv

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.bdfuv.databinding.FragmentDeviceConfigBinding
import com.example.bdfuv.manager.BLEManager
import com.example.bdfuv.model.BLEGattWrapper
import com.example.bdfuv.util.Constant
import com.example.bdfuv.util.RxBus
import com.example.bdfuv.util.RxBusKeyValuePair
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_engineer.efm8ChanCurrentChan2EditText
import kotlinx.android.synthetic.main.activity_engineer.efm8ChanCurrentChan4EditText
import kotlinx.android.synthetic.main.activity_engineer.efm8ChanCurrentReadButton
import kotlinx.android.synthetic.main.activity_engineer.efm8ChanCurrentSaveButton
import kotlinx.android.synthetic.main.activity_engineer.hwGainCH2SeekBar
import kotlinx.android.synthetic.main.activity_engineer.hwGainCH4SeekBar
import kotlinx.android.synthetic.main.activity_engineer.hwGainReadButton
import kotlinx.android.synthetic.main.activity_engineer.hwGainSaveButton
import kotlinx.android.synthetic.main.activity_engineer.normalButton
import kotlinx.android.synthetic.main.activity_engineer.productionButton
import kotlinx.android.synthetic.main.activity_engineer.swGainCH2SeekBar
import kotlinx.android.synthetic.main.activity_engineer.swGainCH4SeekBar
import kotlinx.android.synthetic.main.activity_engineer.swGainReadButton
import kotlinx.android.synthetic.main.activity_engineer.swGainSaveButton
import kotlinx.android.synthetic.main.activity_engineer.swTriggerButton
import kotlinx.android.synthetic.main.fragment_device_config.*


class DeviceConfigFragment : Fragment(), SeekBar.OnSeekBarChangeListener {

    var gatt: BLEGattWrapper? = null
    var rxBusBLEinit: Disposable? = null

    val mCompositeDisposable: CompositeDisposable = CompositeDisposable()

    var thumbView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentDeviceConfigBinding>(
            inflater,
            R.layout.fragment_device_config,
            container,
            false
        )
        gatt = BLEManager.getInstance(context!!).connectedGattWrapper
        // Inflate the layout for this fragment
        binding.gattWrapper = gatt
//        gatt!!.gatt!!.discoverServices()

        gatt?.progressBarListener = {
            setProgressBar(it)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        gatt?.progressBarListener = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        thumbView = LayoutInflater.from(context).inflate(R.layout.layout_seekbar_thumb, null, false)

        normalButton.setOnClickListener { gatt?.writeConfigMode(BLEGattWrapper.MODE.NORMAL) }
        productionButton.setOnClickListener { gatt?.writeConfigMode(BLEGattWrapper.MODE.PRODUCTION) }
/*        visUV365Button.setOnClickListener { gatt?.writeADCMUX(BLEGattWrapper.ADCMUX.visUV365) }
        uvUV365Button.setOnClickListener { gatt?.writeADCMUX(BLEGattWrapper.ADCMUX.uvUV365) }
        visUV385Button.setOnClickListener { gatt?.writeADCMUX(BLEGattWrapper.ADCMUX.visUV385) }
        uvUV385Button.setOnClickListener { gatt?.writeADCMUX(BLEGattWrapper.ADCMUX.uvUV385) }*/
        SIRUV365Button.setOnClickListener { gatt?.adcmux?.ch2UV365 = 0 }
        MIRUV365Button.setOnClickListener { gatt?.adcmux?.ch2UV365 = 1 }
        LIRUV365Button.setOnClickListener { gatt?.adcmux?.ch2UV365 = 2 }
        WUV365Button.setOnClickListener { gatt?.adcmux?.ch2UV365 = 11 }
        LWUV365Button.setOnClickListener { gatt?.adcmux?.ch2UV365 = 13 }
        UVUV365Button.setOnClickListener { gatt?.adcmux?.ch2UV365 = 24 }
        SIRUV385Button.setOnClickListener { gatt?.adcmux?.ch4UV385 = 0 }
        MIRUV385Button.setOnClickListener { gatt?.adcmux?.ch4UV385 = 1 }
        LIRUV385Button.setOnClickListener { gatt?.adcmux?.ch4UV385 = 2 }
        WUV385Button.setOnClickListener { gatt?.adcmux?.ch4UV385 = 11 }
        LWUV385Button.setOnClickListener { gatt?.adcmux?.ch4UV385 = 13 }
        UVUV385Button.setOnClickListener { gatt?.adcmux?.ch4UV385 = 24 }
        swGainCH2SeekBar.setOnSeekBarChangeListener(this)
        swGainCH4SeekBar.setOnSeekBarChangeListener(this)
        hwGainCH2SeekBar.setOnSeekBarChangeListener(this)
        hwGainCH4SeekBar.setOnSeekBarChangeListener(this)

        ADCMUXSaveButton.setOnClickListener { gatt?.writeADCMUX() }

        swGainSaveButton.setOnClickListener {
            gatt?.swGain?.ch2UV365 = swGainCH2SeekBar.progress
            gatt?.swGain?.ch4UV385 = swGainCH4SeekBar.progress
            gatt?.writeSWGain()
        }

        hwGainSaveButton.setOnClickListener {
            gatt?.hwGain?.ch2UV365 = hwGainCH2SeekBar.progress
            gatt?.hwGain?.ch4UV385 = hwGainCH4SeekBar.progress
            gatt?.writeHWGain()
        }

        efm8ChanCurrentSaveButton.setOnClickListener {
            gatt?.efm8ChannelCurrent?.ch0IR = efm8ChanCurrentChan0EditText.editableText.toString().toInt()
            gatt?.efm8ChannelCurrent?.ch1Green = efm8ChanCurrentChan1EditText.editableText.toString().toInt()
            gatt?.efm8ChannelCurrent?.ch2UV365 = efm8ChanCurrentChan2EditText.editableText.toString().toInt()
            gatt?.efm8ChannelCurrent?.ch3Red = efm8ChanCurrentChan3EditText.editableText.toString().toInt()
            gatt?.efm8ChannelCurrent?.ch4UV385 = efm8ChanCurrentChan4EditText.editableText.toString().toInt()
            gatt?.writeEFM8ChanCurrent()
        }




        swGainReadButton.setOnClickListener {
            gatt?.readSWGain()
        }

        hwGainReadButton.setOnClickListener {
            gatt?.readHWGain()
        }

        efm8ChanCurrentReadButton.setOnClickListener {
            gatt?.readEFM8ChannelCurrent()
        }

        ADCMUXReadButton.setOnClickListener {
            gatt!!.readADCMUX()
        }

        //TODO
//        rxBusBLEinit = subscribeBLEinit()
//        mCompositeDisposable.add(rxBusBLEinit!!)
    }

    private fun subscribeBLEinit() =
        RxBus.toFlowable(RxBusKeyValuePair::class.java).observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                //TODO ENABLE_INDICATION_VALUE 若失敗 progress bar會跑不停
                if (it.eventId == Constant.RX_DEVICE_INIT_DONE) {
//                    setProgressBar(false)
//                    rxBusBLEinit!!.dispose()

                }
            }

    override fun onDestroy() {
        super.onDestroy()
        mCompositeDisposable.clear()
    }

    fun getThumb(progress: Int): Drawable {
        (thumbView!!.findViewById(R.id.tvProgress) as TextView).text = progress.toString() + ""

        thumbView!!.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val bitmap =
            Bitmap.createBitmap(
                thumbView!!.getMeasuredWidth(),
                thumbView!!.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888
            )
        val canvas = Canvas(bitmap)
        thumbView!!.layout(0, 0, thumbView!!.getMeasuredWidth(), thumbView!!.getMeasuredHeight())
        thumbView!!.draw(canvas)

        return BitmapDrawable(resources, bitmap)
    }


    override fun onStartTrackingTouch(seekBar: SeekBar?) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {

    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        seekBar!!.thumb = getThumb(progress)
    }

    fun setProgressBar(enable: Boolean) {
        if (enable) {
            activity?.progressBar?.visibility = View.VISIBLE
            blockingView.visibility = View.VISIBLE
        } else {
            activity?.progressBar?.visibility = View.GONE
            blockingView.visibility = View.GONE
        }
    }

}

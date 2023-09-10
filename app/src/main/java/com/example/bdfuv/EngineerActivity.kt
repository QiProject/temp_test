package com.example.bdfuv

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import com.example.bdfuv.manager.BLEManager
import com.example.bdfuv.model.BLEGattWrapper
import com.example.bdfuv.util.Constant
import com.example.bdfuv.util.RxBus
import com.example.bdfuv.util.RxBusKeyValuePair
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_engineer.*
import kotlinx.android.synthetic.main.activity_engineer.ADCMUXReadButton
import kotlinx.android.synthetic.main.activity_engineer.efm8ChanCurrentChan2EditText
import kotlinx.android.synthetic.main.activity_engineer.efm8ChanCurrentChan4EditText
import kotlinx.android.synthetic.main.activity_engineer.efm8ChanCurrentReadButton
import kotlinx.android.synthetic.main.activity_engineer.efm8ChanCurrentSaveButton
import kotlinx.android.synthetic.main.activity_engineer.hwGainCH2SeekBar
import kotlinx.android.synthetic.main.activity_engineer.hwGainCH4SeekBar
import kotlinx.android.synthetic.main.activity_engineer.hwGainReadButton
import kotlinx.android.synthetic.main.activity_engineer.hwGainSaveButton
import kotlinx.android.synthetic.main.activity_engineer.swGainCH2SeekBar
import kotlinx.android.synthetic.main.activity_engineer.swGainCH4SeekBar
import kotlinx.android.synthetic.main.activity_engineer.swGainReadButton
import kotlinx.android.synthetic.main.activity_engineer.swGainSaveButton
import kotlinx.android.synthetic.main.activity_engineer.UVUV365Button
import kotlinx.android.synthetic.main.activity_engineer.UVUV385Button
import kotlinx.android.synthetic.main.activity_engineer.WUV365Button
import kotlinx.android.synthetic.main.activity_engineer.WUV385Button
import kotlinx.android.synthetic.main.fragment_device_config.*

//revised by Teddy, 20200204
class EngineerActivity : BaseActivity<com.example.bdfuv.databinding.ActivityEngineerBinding>(),
    SeekBar.OnSeekBarChangeListener {
    override fun getLayoutId(): Int {
        return R.layout.activity_engineer
    }


    val gatt = BLEManager.getInstance(this).connectedGattWrapper
    var rxBusBLEinit: Disposable? = null

    val mCompositeDisposable: CompositeDisposable = CompositeDisposable()

    var thumbView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        thumbView = LayoutInflater.from(this@EngineerActivity).inflate(R.layout.layout_seekbar_thumb, null, false)

        gatt!!.gatt!!.discoverServices()
        bindingView!!.gattWrapper = gatt

        bindingView!!.normalButton.setOnClickListener { gatt!!.writeConfigMode(BLEGattWrapper.MODE.NORMAL) }
        bindingView!!.productionButton.setOnClickListener { gatt!!.writeConfigMode(BLEGattWrapper.MODE.PRODUCTION) }
        bindingView!!.WUV365Button.setOnClickListener { gatt?.adcmux?.ch2UV365 = 13 }
        bindingView!!.UVUV365Button.setOnClickListener { gatt?.adcmux?.ch2UV365 = 24 }
        bindingView!!.WUV385Button.setOnClickListener { gatt?.adcmux?.ch4UV385 = 13 }
        bindingView!!.UVUV385Button.setOnClickListener { gatt?.adcmux?.ch4UV385 = 24 }
        bindingView!!.swGainCH2SeekBar.setOnSeekBarChangeListener(this)
        bindingView!!.swGainCH4SeekBar.setOnSeekBarChangeListener(this)
        bindingView!!.hwGainCH2SeekBar.setOnSeekBarChangeListener(this)
        bindingView!!.hwGainCH4SeekBar.setOnSeekBarChangeListener(this)

        ADCMUXSaveButton.setOnClickListener { gatt?.writeADCMUX() }

        swGainSaveButton.setOnClickListener {
            gatt.swGain!!.ch2UV365 = swGainCH2SeekBar.progress
            gatt.swGain!!.ch4UV385 = swGainCH4SeekBar.progress
            gatt.writeSWGain()
        }

        hwGainSaveButton.setOnClickListener {
            gatt.hwGain!!.ch2UV365 = hwGainCH2SeekBar.progress
            gatt.hwGain!!.ch4UV385 = hwGainCH4SeekBar.progress
            gatt.writeHWGain()
        }

        efm8ChanCurrentSaveButton.setOnClickListener {
            gatt.efm8ChannelCurrent!!.ch2UV365 = efm8ChanCurrentChan2EditText.editableText.toString().toInt()
            gatt.efm8ChannelCurrent!!.ch4UV385 = efm8ChanCurrentChan4EditText.editableText.toString().toInt()
            gatt.writeEFM8ChanCurrent()
        }

        ADCMUXReadButton.setOnClickListener {
            gatt!!.readADCMUX()
            println(">>>>>>>>>>ADCMUXReadButton = " + gatt!!.readADCMUX())
        }


        swGainReadButton.setOnClickListener {
            gatt!!.readSWGain()
            println(">>>>>>>>>>swGainReadButton = " + gatt!!.readSWGain())
        }

        hwGainReadButton.setOnClickListener {
            gatt!!.readHWGain()
        }

        efm8ChanCurrentReadButton.setOnClickListener {
            gatt!!.readEFM8ChannelCurrent()
        }

        rxBusBLEinit = subscribeBLEinit()
        mCompositeDisposable.add(rxBusBLEinit!!)
    }


    private fun subscribeBLEinit() =
        RxBus.toFlowable(RxBusKeyValuePair::class.java).observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                //TODO ENABLE_INDICATION_VALUE 若失敗 progress bar會跑不停
                if (it.eventId == Constant.RX_ENABLE_INDICATION_VALUE) {
                    setProgressBar(false)
                    rxBusBLEinit!!.dispose()

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
}



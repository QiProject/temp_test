package com.example.bdfuv


import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.example.bdfuv.databinding.FragmentDeviceConfigBinding
import com.example.bdfuv.databinding.FragmentTestMeasurementBinding
import com.example.bdfuv.manager.BLEManager
import com.example.bdfuv.model.BLEGattWrapper
import com.example.bdfuv.util.Constant
import com.example.bdfuv.util.RxBus
import com.example.bdfuv.util.RxBusKeyValuePair
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_engineer.*
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


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */


class TestMeasurementFragment : Fragment() {

    var gatt: BLEGattWrapper? = null
    var rxBusBLEinit: Disposable? = null

    val mCompositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentTestMeasurementBinding>(
            inflater,
            R.layout.fragment_test_measurement,
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

        swTriggerButton.setOnClickListener { gatt?.writeSWTrigger() }

        //TODO
//        rxBusBLEinit = subscribeBLEinit()
//        mCompositeDisposable.add(rxBusBLEinit!!)
    }

    private fun subscribeBLEinit() =
        RxBus.toFlowable(RxBusKeyValuePair::class.java).observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                //TODO ENABLE_INDICATION_VALUE 若失敗 progress bar會跑不停
                if (it.eventId == Constant.RX_ENABLE_INDICATION_VALUE) {
//                    setProgressBar(false)
//                    rxBusBLEinit!!.dispose()

                }
            }

    override fun onDestroy() {
        super.onDestroy()
        mCompositeDisposable.clear()
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

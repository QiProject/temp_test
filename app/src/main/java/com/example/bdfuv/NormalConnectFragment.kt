package com.example.bdfuv


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.example.bdfuv.databinding.FragmentNormalConnectBinding
import com.example.bdfuv.manager.BLEManager
import com.example.bdfuv.model.NormalConnectModel
import com.example.bdfuv.util.Constant
import com.example.bdfuv.util.RxBus
import com.example.bdfuv.util.RxBusKeyValuePair
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.fragment_normal_connect.*


class NormalConnectFragment : Fragment() {
    val mCompositeDisposable: CompositeDisposable = CompositeDisposable()
    var uiControl: NormalConnectModel = NormalConnectModel()

    private fun subscribeBLEinit() =
        RxBus.toFlowable(RxBusKeyValuePair::class.java).observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                //TODO ENABLE_INDICATION_VALUE 若失敗 progress bar會跑不停
                if (it.eventId == Constant.RX_DEVICE_INIT_DONE) {
                    setProgressBar(false)
                }
            }


    enum class STATE {
        TURN_ON_DEVICE, SEARCHING, NOT_FOUND, CONNECTED
    }

    var currentState = STATE.TURN_ON_DEVICE

    override fun onResume() {
        super.onResume()

        mCompositeDisposable.add(subscribeBLEinit())
    }

    override fun onStop() {
        super.onStop()

        mCompositeDisposable.clear()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = DataBindingUtil.inflate<FragmentNormalConnectBinding>(
            inflater,
            R.layout.fragment_normal_connect,
            container,
            false
        )
        // Inflate the layout for this fragment

        binding.control = uiControl

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        (activity as MainActivity).checkBLE()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //TURN_ON_DEVICE
        switchState()

        continueButton.setOnClickListener {
            when (currentState) {
                STATE.TURN_ON_DEVICE -> {
                    currentState = STATE.SEARCHING
                    switchState()
                }
                STATE.NOT_FOUND -> {
                    currentState = STATE.TURN_ON_DEVICE
                    switchState()
                }
                STATE.CONNECTED -> {
                    if (findNavController().currentDestination?.id == R.id.normalConnectFragment) {
                        findNavController().navigate(R.id.action_normalConnectFragment_to_measureInfoFragment)
                    }
                }
            }
        }
    }

    fun connected() {
        currentState = STATE.CONNECTED
        switchState()
    }

    fun disconnected() {
        currentState = STATE.NOT_FOUND
        switchState()
    }


    fun timeout() {
        currentState = STATE.NOT_FOUND
        switchState()
    }


    private fun switchState() {
        when (currentState) {
            STATE.TURN_ON_DEVICE -> {
                uiControl.hint = getString(R.string.please_turn_on_uv_sensor)
                uiControl.showButton = true
            }
            STATE.SEARCHING -> {
                BLEManager.getInstance(context!!).tryAutoConnect()
                uiControl.hint = getString(R.string.searching_for_uv_sensor)
                uiControl.showButton = false
                setProgressBar(true)
            }
            STATE.NOT_FOUND -> {
                uiControl.hint = getString(R.string.uv_sensor_not_found)
                uiControl.showButton = true

                setProgressBar(false)
            }
            STATE.CONNECTED -> {
                uiControl.hint = getString(R.string.connected)
                uiControl.showButton = true

                Toast.makeText(context, "Connected, initializing Device ", Toast.LENGTH_SHORT).show()

                setProgressBar(true)
            }
        }
    }

    fun setProgressBar(enable: Boolean) {

        if (enable) {
            activity?.progressBar?.visibility = View.VISIBLE
        } else {
            activity?.progressBar?.visibility = View.GONE
        }
    }

}

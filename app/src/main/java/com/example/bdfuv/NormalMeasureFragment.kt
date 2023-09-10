package com.example.bdfuv


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bdfuv.adapters.MeasureAdapter
import com.example.bdfuv.manager.BLEManager
import com.example.bdfuv.manager.RealmManager
import com.example.bdfuv.model.AreaModel
import com.example.bdfuv.model.BLEGattWrapper
import com.example.bdfuv.model.MeasureDataModel
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.fragment_app_config_areas.*
import kotlinx.android.synthetic.main.fragment_device_config.*
import kotlinx.android.synthetic.main.fragment_measure_info.*
import kotlin.collections.ArrayList


class NormalMeasureFragment : Fragment() {
    var gatt: BLEGattWrapper? = null
    var measureAdapter: MeasureAdapter? = null

    private var areaList: ArrayList<AreaModel> = arrayListOf()
    private var measureDataList: ArrayList<MeasureDataModel> = arrayListOf()

    private val measureViewModel: MeasureViewModel by lazy { ViewModelProviders.of(activity!!).get(MeasureViewModel::class.java) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding =
            DataBindingUtil.inflate<com.example.bdfuv.databinding.FragmentNormalMeasureBinding>(
                inflater,
                R.layout.fragment_normal_measure,
                container,
                false
            )
        //set variables in Binding
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        gatt?.progressBarListener = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        areaList = (RealmManager.instance.retrieveCheckedAreas() as ArrayList<AreaModel>?)!!
        measureAdapter = MeasureAdapter(areaList)
        measureAdapter?.onItemClickListener = {
            println(">>>>click $it")
        }
        recyclerView.adapter = measureAdapter
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)


        gatt = BLEManager.getInstance(context!!).connectedGattWrapper

        val macAddress = gatt?.device?.address

        gatt?.progressBarListener = {
            if (!it) {
                val i = measureAdapter!!.selectedIndex
                // i will be -1 when all is done and none is selected
                if (i >= 0 && i < areaList.size) {
                    addMeasureData(gatt?.toMeasureData(areaList.get(i).name)!!, i)
                }
                if (measureDataList.size == areaList.size) {
                    okButton.visibility = View.VISIBLE
                }
            }
            setProgressBar(it)
        }

        okButton.setOnClickListener {
            //            CSVWriter.writeToCSV(measureDataList, context!!)

            if (findNavController().currentDestination?.id == R.id.normalMeasureFragment) {
                findNavController().navigate(R.id.action_normalMeasureFragment_to_measureCompleteFragment)

                measureViewModel.datas.value = measureDataList
                measureViewModel.macAddress.value = macAddress
            }
        }

    }

    fun setProgressBar(enable: Boolean) {
        if (enable) {
            activity?.progressBar?.visibility = View.VISIBLE
            blockingView?.visibility = View.VISIBLE
        } else {
            activity?.progressBar?.visibility = View.GONE
            blockingView?.visibility = View.GONE

            measureAdapter?.switchIndicator()
        }
    }

    fun addMeasureData(data: MeasureDataModel, index: Int) {
        if (measureDataList.size > index) {
            measureDataList.set(index, data)
        } else {
            measureDataList.add(data)
        }

        println(">>>>> measure Data size :${measureDataList.size}")
    }
}

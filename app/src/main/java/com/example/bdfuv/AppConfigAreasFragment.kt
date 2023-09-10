package com.example.bdfuv


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bdfuv.adapters.AreaAdapter
import com.example.bdfuv.databinding.FragmentAppConfigAreasBinding
import com.example.bdfuv.manager.RealmManager
import com.example.bdfuv.model.AreaModel
import kotlinx.android.synthetic.main.fragment_app_config_areas.*


/**
 * A simple [Fragment] subclass.
 *
 */
class AppConfigAreasFragment : Fragment() {
    private var areaList: ArrayList<AreaModel> = arrayListOf()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding =
            DataBindingUtil.inflate<FragmentAppConfigAreasBinding>(
                inflater,
                R.layout.fragment_app_config_areas,
                container,
                false
            )
        //set variables in Binding
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        areaList = (RealmManager.instance.retrieveAreas() as ArrayList<AreaModel>?)!!

        recyclerView.adapter = AreaAdapter(areaList)
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        saveButton.setOnClickListener {
            RealmManager.instance.saveAreas(areaList)
            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()

            findNavController().navigate(R.id.action_appConfigAreaFragment_to_appConfigQuestionFragment)
        }
    }
}

package com.example.bdfuv


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_measure_info.*


class MeasureInfoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_measure_info, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        okButton.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.measureInfoFragment) {
                findNavController().navigate(R.id.action_measureInfoFragment_to_normalMeasureFragment)
            }
        }
    }


}

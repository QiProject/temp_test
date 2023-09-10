package com.example.bdfuv


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_measurement_complete.*


class MeasurementCompleteFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_measurement_complete, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        continueButton.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.measureCompleteFragment) {
                findNavController().navigate(R.id.action_measureCompleteFragment_to_normalQuestionFragment)
            }
        }


    }
}

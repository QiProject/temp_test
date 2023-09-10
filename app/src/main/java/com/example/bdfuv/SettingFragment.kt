package com.example.bdfuv


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil.inflate
import androidx.navigation.fragment.findNavController
import com.example.bdfuv.databinding.FragmentSettingBinding
import kotlinx.android.synthetic.main.fragment_setting.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class SettingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        var binding = inflate<FragmentSettingBinding>(inflater, R.layout.fragment_setting, container, false)
        //set variables in Binding



        return binding.getRoot()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        okButton.setOnClickListener {

            if (codeEditText.editableText.toString() == "4223") {
                findNavController().navigate(R.id.expertFragment)
                codeEditText.editableText.clear()
            } else {
                Toast.makeText(activity, "invalid code", Toast.LENGTH_SHORT).show()
                codeEditText.editableText.clear()
            }
        }

    }


}

package com.example.bdfuv


import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.bdfuv.manager.BLEManager
import kotlinx.android.synthetic.main.fragment_expert.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class ExpertFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = inflater.inflate(R.layout.fragment_expert, container, false)
        // Inflate the layout for this fragment
        return binding.rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        exitButton.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.expertFragment) {
                findNavController().navigate(R.id.action_expert_to_main)
            }
        }

        appConfigButton.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.expertFragment) {
                findNavController().navigate(R.id.action_expertFragment_to_appConfigAreaFragment)
            }
        }

        deviceSelectionButton.setOnClickListener {
            //prevent double click error
            //https://stackoverflow.com/questions/51060762/java-lang-illegalargumentexception-navigation-destination-xxx-is-unknown-to-thi
            if (findNavController().currentDestination?.id == R.id.expertFragment) {
                findNavController().navigate(R.id.action_expertFragment_to_selectDeviceFragment)
            }
        }

        deviceConfigButton.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.expertFragment) {
                if (BLEManager.getInstance(context!!).connectedGattWrapper == null) {
                    showConnectFirstDialog()
                } else {
                    findNavController().navigate(R.id.action_expertFragment_to_deviceConfigFragment)
                }
            }
        }

        testMeasurementButton.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.expertFragment) {
                if (findNavController().currentDestination?.id == R.id.expertFragment) {
                    if (BLEManager.getInstance(context!!).connectedGattWrapper == null) {
                        showConnectFirstDialog()
                    } else {
                        findNavController().navigate(R.id.action_expertFragment_to_testMeasurementFragment)
                    }
                }
            }
        }

        exportFilePathButton.setOnClickListener {
            onShareText()
        }

    }


    private fun showConnectFirstDialog() {

        // setup dialog builder
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle("Device not Connected")
        builder.setMessage("Please connect to device first")
        builder.setPositiveButton("ok", { dialog, whichButton ->

        })

        // create dialog and show it
        val dialog = builder.create()
        dialog.show()
    }

    private fun onShareText() {
        val path = context?.getExternalFilesDir(null)?.absolutePath

        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, path)
        startActivity(Intent.createChooser(shareIntent, "Share file path"))
    }
}

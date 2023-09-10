package com.example.bdfuv

import android.content.Context
import android.os.Bundle
import android.text.style.DynamicDrawableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bdfuv.manager.BLEManager
import kotlinx.android.synthetic.main.fragment_expert.*
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.fragment_measure_info.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [MainFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [MainFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class MainFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        println(">>>>> main fragment")
        return inflater.inflate(R.layout.fragment_main, container, false)
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onStart() {
        super.onStart()

        val isConnected = BLEManager.getInstance(context!!).connectedGattWrapper != null
        showView(isConnected)
    }

    fun showView(isConnected: Boolean) {
        var title: String?

        if (isConnected) {
            continueButton.visibility = View.VISIBLE
            connectButton.visibility = View.INVISIBLE
            title = getString(R.string.welcome_back)
        } else {
            continueButton.visibility = View.INVISIBLE
            connectButton.visibility = View.VISIBLE
            title = getString(R.string.welcome)
        }
        titleTextView.text = title
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        connectButton.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.mainFragment) {
                findNavController().navigate(R.id.action_mainFragment_to_normalConnectFragment)
            }
        }

        continueButton.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.mainFragment) {
                findNavController().navigate(R.id.action_mainFragment_to_measureInfoFragment)
            }
        }

    }
}

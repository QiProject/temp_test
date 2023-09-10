package com.example.bdfuv


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bdfuv.adapters.NormalQuestionAdapter
import com.example.bdfuv.manager.RealmManager
import com.example.bdfuv.model.QuestionModel
import com.example.bdfuv.util.CSVWriter
import kotlinx.android.synthetic.main.fragment_app_config_areas.*
import kotlinx.android.synthetic.main.fragment_measure_info.*


class NormalQuestionFragment : Fragment() {
    var questionAdapter: NormalQuestionAdapter? = null
    val questionArray: IntArray = IntArray(7)

    private var questionList: ArrayList<QuestionModel> = arrayListOf()
    val vm: MeasureViewModel by lazy { ViewModelProviders.of(activity!!).get(MeasureViewModel::class.java) }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding =
            DataBindingUtil.inflate<com.example.bdfuv.databinding.FragmentNormalQuestionBinding>(
                inflater,
                R.layout.fragment_normal_question,
                container,
                false
            )
        //set variables in Binding
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        questionList = (RealmManager.instance.retrieveCheckedQuestions() as ArrayList<QuestionModel>?)!!
        for (q in questionList) {
            q.isChecked = false
        }

        questionAdapter = NormalQuestionAdapter(questionList)
        questionAdapter?.onItemClickListener = {
            questionArray[it.index] = if (it.isChecked) 1 else 0
        }
        recyclerView.adapter = questionAdapter
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        CSVWriter.writeListener = {
            if (it) {
                Toast.makeText(context!!, "Saved", Toast.LENGTH_SHORT).show()
                if (findNavController().currentDestination?.id == R.id.normalQuestionFragment) {
                    findNavController().navigate(R.id.action_normalQuestionFragment_to_measurementReadyFragment)
                }
            } else {
                Toast.makeText(context!!, "Write File Fail", Toast.LENGTH_SHORT).show()
            }
        }

        okButton.setOnClickListener {
            CSVWriter.writeToCSV(vm.datas.value!!, vm.macAddress.value!!, questionArray, context!!)
        }
    }


}

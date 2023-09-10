package com.example.bdfuv


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bdfuv.adapters.QuestionAdapter
import com.example.bdfuv.manager.RealmManager
import com.example.bdfuv.model.QuestionModel
import kotlinx.android.synthetic.main.fragment_app_config_areas.*


/**
 * A simple [Fragment] subclass.
 *
 */
class AppConfigQuestionsFragment : Fragment() {
    private var questionList: ArrayList<QuestionModel> = arrayListOf()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding =
            DataBindingUtil.inflate<com.example.bdfuv.databinding.FragmentAppConfigQuestionsBinding>(
                inflater,
                R.layout.fragment_app_config_questions,
                container,
                false
            )
        //set variables in Binding
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        questionList = (RealmManager.instance.retrieveQuestions() as ArrayList<QuestionModel>?)!!

        recyclerView.adapter = QuestionAdapter(questionList)
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        saveButton.setOnClickListener {
            RealmManager.instance.saveQuestions(questionList)
            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()

            findNavController().navigate(R.id.action_appConfigQuestionFragment_to_expertFragment)
        }
    }
}

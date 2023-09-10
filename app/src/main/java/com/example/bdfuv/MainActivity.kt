package com.example.bdfuv

import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.bdfuv.manager.BLEManager
import com.example.bdfuv.manager.RealmManager
import com.example.bdfuv.model.AreaModel
import com.example.bdfuv.model.QuestionModel
import com.example.bdfuv.util.Constant
import com.example.bdfuv.util.RxBus
import com.example.bdfuv.util.RxBusKeyValuePair
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.navigation_drawer.*


class MainActivity : BaseActivity<com.example.bdfuv.databinding.ActivityMainBinding>() {
    private val TAG = "MainActivity"

    val mCompositeDisposable: CompositeDisposable = CompositeDisposable()

    private fun subscribeConnectionChange() =
        RxBus.toFlowable(RxBusKeyValuePair::class.java).observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it.eventId == Constant.BLUETOOTH_CONNECTION_STATE_CONNECTED || it.eventId == Constant.BLUETOOTH_CONNECTION_STATE_DISCONNECTED) {
                    invalidateOptionsMenu()
                    val navHostFragment = main_fragment as NavHostFragment


                    //https://issuetracker.google.com/issues/119800853
                    //https://developer.android.com/topic/libraries/architecture/viewmodel#sharing
                    //use viewModel instead?
                    if (navHostFragment.childFragmentManager.primaryNavigationFragment is NormalConnectFragment) {
                        val normalConnectFragment =
                            navHostFragment.childFragmentManager.primaryNavigationFragment as NormalConnectFragment
                        if (it.eventId == Constant.BLUETOOTH_CONNECTION_STATE_CONNECTED) {
                            normalConnectFragment.connected()
                        } else if (it.eventId == Constant.BLUETOOTH_CONNECTION_STATE_DISCONNECTED) {
                            normalConnectFragment.disconnected()
                        }
                    } else if (navHostFragment.childFragmentManager.primaryNavigationFragment is MainFragment && it.eventId == Constant.BLUETOOTH_CONNECTION_STATE_DISCONNECTED) {
                        val mainFragment =
                            navHostFragment.childFragmentManager.primaryNavigationFragment as MainFragment
                        mainFragment.showView(false)
                    } else if (it.eventId == Constant.BLUETOOTH_CONNECTION_STATE_DISCONNECTED) {
                        //TODO  setting要回？
                        val drawerNavController = Navigation.findNavController(this, R.id.main_fragment)

                        drawerNavController.popBackStack(R.id.mainFragment, false)
                    }
                } else if (it.eventId == Constant.BLUETOOTH_SCAN_TIMEOUT) {
                    val navHostFragment = main_fragment as NavHostFragment

                    if (navHostFragment.childFragmentManager.primaryNavigationFragment is NormalConnectFragment) {
                        val normalConnectFragment =
                            navHostFragment.childFragmentManager.primaryNavigationFragment as NormalConnectFragment
                        normalConnectFragment.timeout()
                    }
                }
            }


    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "on Create")

        Realm.init(this)

        val drawerNavController = Navigation.findNavController(this, R.id.main_fragment)
        NavigationUI.setupWithNavController(nav_view, drawerNavController)

        if (!RealmManager.instance.questionExist()) {
            createInitQuestions()
        }

        if (!RealmManager.instance.areaExist()) {
            createInitAreas()
        }

        mCompositeDisposable.add(subscribeConnectionChange())

        setVersionName()
    }

    override fun onDestroy() {
        super.onDestroy()
        mCompositeDisposable.clear()
        Log.d(TAG, "on Destroy")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.scan_menu, menu)

        menu.findItem(R.id.ic_connected).isVisible = BLEManager.getInstance(this).connectedGattWrapper != null

        return true
    }

    private fun createInitQuestions() {
        Log.d(TAG, "create init questions");

        val questions = ArrayList<QuestionModel>()

        val questionNames =
            arrayOf("untreated", "sun exposure", "shadow", "sweating", "swimming", "toweling", "reapplication")


        for ((i, name) in questionNames.withIndex()) {
            val q = QuestionModel(i, name, true)
            questions.add(q)
        }

        RealmManager.instance.saveQuestions(questions)
    }

    private fun createInitAreas() {
        Log.d(TAG, "create init areas");

        val areas = ArrayList<AreaModel>()

        val areaNames = arrayOf("Cheek", "Chest", "Belly", "Inner Forearm", "Outer Forearm", "Thigh")

        for ((i, name) in areaNames.withIndex()) {
            val a = AreaModel(i, name, true)
            areas.add(a)
        }

        RealmManager.instance.saveAreas(areas)
    }

    private fun setVersionName() {
        val pInfo = packageManager.getPackageInfo(packageName, 0);
        val version = "v" + pInfo.versionName;
//        val versionCode = pInfo.versionCode;
//        println(">>>>>name: $version   code:$versionCode")
        version_text.text = version
    }


}

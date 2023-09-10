package com.example.bdfuv

import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.example.bdfuv.custom_view.CustomTypefaceSpan
import com.example.bdfuv.manager.BLEManager
import com.example.bdfuv.util.Constant.REQUEST_ENABLE_BT
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.navigation_drawer.*


open abstract class BaseActivity<T : ViewDataBinding>() : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener {


    protected var bindingView: T? = null
    var baseView: View? = null

    // 内容布局
    protected var mContainer: FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        baseView = layoutInflater.inflate(R.layout.navigation_drawer, null, false) //获取基类的布局
        setContentView(baseView)  //设置基类的布局
        bindingView = DataBindingUtil.inflate(
            layoutInflater,
            getLayoutId(),
            baseView as ViewGroup?,
            false
        )  //使用databindingView获取子类布局

        var params: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        bindingView?.getRoot()?.layoutParams = params
        mContainer = baseView?.findViewById(R.id.layout_container) //获取父类的RelativieLayout布局
        mContainer?.addView(bindingView?.getRoot())
//        initData() //初始化数据

        setSupportActionBar(tool_bar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, tool_bar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        setMenuItemFont()
    }

    private fun setMenuItemFont() {
        val m = nav_view.menu

        for (i in 0 until m.size()) {
            val mi = m.getItem(i)

            //for applying a font to subMenu ...
            val subMenu = mi.subMenu;
            if (subMenu != null && subMenu.size() > 0) {
                for (j in 0 until subMenu.size()) {
                    val subMenuItem = subMenu.getItem(j)
                    applyFontToMenuItem(subMenuItem)
                }
            }

            //the method we have create in activity
            applyFontToMenuItem(mi)
        }
    }


    fun checkBLE() {
        if (!BLEManager.getInstance(this).isBluetoothEnable()) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
    }

    override fun onStart() {
        super.onStart()

        if (!packageManager.hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "ble not supported", Toast.LENGTH_SHORT)
                .show()
        }else{
            checkBLE()
        }
    }



    override fun onResume() {
        super.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {

            } else {
                showEnableBLEDialog()
            }
        }
    }

    private fun showEnableBLEDialog() {

        // setup dialog builder
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Turn on Bluetooth")
        builder.setMessage("Turn on Bluetooth for device scanning")
        builder.setPositiveButton("ok", { dialog, whichButton ->

        })

        // create dialog and show it
        val dialog = builder.create()
        dialog.show()
    }

    /**
     * 基类返回一个layout布局的int地址
     */
    abstract protected fun getLayoutId(): Int

//    abstract protected fun initData()

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {

//            R.id.nav_gallery -> {
//
//            }
//            R.id.nav_slideshow -> {
//
//            }
//            R.id.nav_manage -> {
//
//            }
//            R.id.nav_share -> {
//
//            }
//            R.id.nav_send -> {
//
//            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true

    }

    open fun setProgressBar(enable: Boolean) {
        Log.d("art", "set progress bar $enable")

        if (enable) {
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.GONE
        }
    }

    private fun applyFontToMenuItem(mi: MenuItem) {
        val font = ResourcesCompat.getFont(this, R.font.nivea_light)!!
        val mNewTitle = SpannableString(mi.title)
        mNewTitle.setSpan(CustomTypefaceSpan("", font), 0, mNewTitle.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        mNewTitle.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.niveaBlue1)),
            0,
            mNewTitle.length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        //mNewTitle.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, mNewTitle.length(), 0); Use this if you want to center the items
        mi.title = mNewTitle
    }
}
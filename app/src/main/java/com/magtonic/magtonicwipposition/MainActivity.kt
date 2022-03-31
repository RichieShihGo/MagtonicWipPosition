package com.magtonic.magtonicwipposition

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager

import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi

import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.GravityCompat
import com.google.gson.Gson
import com.magtonic.magtonicwipposition.api.ApiFunc
import com.magtonic.magtonicwipposition.data.Constants
import com.magtonic.magtonicwipposition.databinding.ActivityMainBinding
import com.magtonic.magtonicwipposition.model.item.ItemPosition
import com.magtonic.magtonicwipposition.model.item.ItemUpdatePosition
import com.magtonic.magtonicwipposition.model.receive.RJPosition
import com.magtonic.magtonicwipposition.model.receive.RJUpdatePosition
import com.magtonic.magtonicwipposition.model.receive.ReceiveTransform
import com.magtonic.magtonicwipposition.model.send.HttpGetPositionPara
import com.magtonic.magtonicwipposition.model.send.HttpUpdatePositionPara
import com.magtonic.magtonicwipposition.model.sys.ScanBarcode
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener  {
    private val mTAG = MainActivity::class.java.name

    private val requestIdMultiplePermission = 1

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private var mContext: Context? = null

    private var mReceiver: BroadcastReceiver? = null
    private var isRegister = false

    private var imm: InputMethodManager? = null

    companion object {
        @JvmStatic var screenWidth: Int = 0
        @JvmStatic var screenHeight: Int = 0
        @JvmStatic var isKeyBoardShow: Boolean = false
        @JvmStatic var isWifiConnected: Boolean = false
        @JvmStatic var currentSSID: String = ""
    }

    var barcode: ScanBarcode? = null
    //private var navView: NavigationView? = null
    private var toastHandle: Toast? = null
    private var isBarcodeScanning: Boolean = false

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(mTAG, "onCreate")

        mContext = applicationContext

        //disable Scan2Key Setting
        val disableServiceIntent = Intent()
        disableServiceIntent.action = "unitech.scanservice.scan2key_setting"
        disableServiceIntent.putExtra("scan2key", false)
        sendBroadcast(disableServiceIntent)

        val displayMetrics = DisplayMetrics()

        //
        //mContext!!.display!!.getMetrics(displayMetrics)
        /*if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M)
        {
            windowManager.defaultDisplay.getMetrics(displayMetrics)

            screenHeight = displayMetrics.heightPixels
            screenWidth = displayMetrics.widthPixels
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            mContext!!.display!!.getRealMetrics(displayMetrics)

            screenHeight = displayMetrics.heightPixels
            screenWidth = displayMetrics.widthPixels
        } else { //Android 11
            //mContext!!.display!!.getMetrics(displayMetrics)
            screenHeight = windowManager.currentWindowMetrics.bounds.height()
            screenWidth = windowManager.currentWindowMetrics.bounds.width()

        }*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { //api 31
            screenHeight = windowManager.currentWindowMetrics.bounds.height()
            screenWidth = windowManager.currentWindowMetrics.bounds.width()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { //api 30
            mContext!!.display!!.getRealMetrics(displayMetrics)
            screenHeight = displayMetrics.heightPixels
            screenWidth = displayMetrics.widthPixels
        } else { // < Build.VERSION_CODES.R
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                windowManager.defaultDisplay.getMetrics(displayMetrics)

                screenHeight = displayMetrics.heightPixels
                screenWidth = displayMetrics.widthPixels
            }
        }

        Log.e(mTAG, "width = $screenWidth, height = $screenHeight")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        /*binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }*/
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_position, R.id.nav_gallery, R.id.nav_slideshow, R.id.nav_about
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navView.setNavigationItemSelectedListener(this)

        val filter: IntentFilter
        @SuppressLint("CommitPrefEdits")
        mReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action != null) {
                    if (intent.action!!.equals(Constants.ACTION.ACTION_HIDE_KEYBOARD, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_HIDE_KEYBOARD")

                        imm?.toggleSoftInput(InputMethodManager.RESULT_HIDDEN, 0)
                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_USER_INPUT_SEARCH, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_USER_INPUT_SEARCH")

                        if (isKeyBoardShow) {
                            imm?.toggleSoftInput(InputMethodManager.RESULT_HIDDEN, 0)
                        }

                        val inputNo = intent.getStringExtra("INPUT_NO")

                        Log.e(mTAG, "inputNo = $inputNo")


                        if (inputNo != null) {

                            try {
                                barcode = ScanBarcode.setPoBarcodeByScanTransform(inputNo.trim())

                                Log.e(mTAG, "barcode = ${barcode!!.poBarcodeByScan}")

                                getPosition(barcode)
                            } catch (ex: NumberFormatException) {
                                ex.printStackTrace()
                            }


                        } else {
                            Log.e(mTAG, "inputNo = null")
                        }


                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_POSITION_UPDATE_ACTION, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_POSITION_UPDATE_ACTION")

                        val materialNo = intent.getStringExtra("MATERIAL_NO")
                        if (materialNo != null && materialNo != "") {
                            updatePositionDate(materialNo)
                        }

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_POSITION_SCAN_BARCODE_CAMERA, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_POSITION_SCAN_BARCODE_CAMERA")

                        val cameraData = intent.getStringExtra("CAMERA_DATA")
                        Log.d(mTAG, "camera_data = $cameraData")
                        //showMyToast(text, ReceiptActivity.this);
                        if (cameraData != null && cameraData != "") {
                            val firstChar: String = cameraData.substring(0, 1)
                            if (firstChar == "T") {
                                barcode = ScanBarcode.setPoBarcodeByScanTransform(cameraData.toString().trim())

                                if (barcode!!.poBarcodeByScan.length == 9) { //material no
                                    val scanIntent = Intent()
                                    scanIntent.action =
                                        Constants.ACTION.ACTION_POSITION_SCAN_BARCODE
                                    scanIntent.putExtra(
                                        "BARCODE_BY_SCAN",
                                        barcode!!.poBarcodeByScan
                                    )
                                    //scanIntent.putExtra("BARCODE", barcode!!.poBarcode)
                                    //scanIntent.putExtra("LINE", barcode!!.poLine)
                                    sendBroadcast(scanIntent)
                                    getPosition(barcode)
                                } else if (barcode!!.poBarcodeByScan.length > 9) {
                                    toast(getString(R.string.storage_location, cameraData))

                                    val storageArray = barcode!!.poBarcodeByScan.split("@")
                                    if (storageArray.size == 3) {
                                        toast(getString(R.string.storage_location, storageArray[2]))
                                        val scanIntent = Intent()
                                        scanIntent.action =
                                            Constants.ACTION.ACTION_POSITION_PASS_STORAGE
                                        //scanIntent.putExtra(
                                        //"STORAGE_LOCATION",
                                        //barcode!!.poBarcodeByScan
                                        //)
                                        scanIntent.putExtra(
                                            "STORAGE_LOCATION",
                                            storageArray[2]
                                        )
                                        sendBroadcast(scanIntent)
                                    } else {
                                        toast(getString(R.string.unknown_barcode))
                                    }
                                } else {
                                    toast(getString(R.string.unknown_barcode))
                                }
                            } else { //firstChar is not "T"

                                toast(getString(R.string.unknown_barcode))

                                val scanIntent = Intent()
                                scanIntent.action = Constants.ACTION.ACTION_POSITION_SCAN_BARCODE
                                sendBroadcast(scanIntent)
                            }
                        }


                    }
                }

                //detect wifi
                if ("android.net.wifi.STATE_CHANGE" == intent.action) {
                    Log.e(mTAG, "Wifi STATE_CHANGE")

                    //val info: NetworkInfo? = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO)

                    val wifiMgr = mContext!!.getSystemService(Context.WIFI_SERVICE) as WifiManager

                    //if (info!!.isConnected) {
                    if (wifiMgr.isWifiEnabled) {

                        val wifiInfo: WifiInfo = wifiMgr.connectionInfo
                        if (wifiInfo.networkId == -1) {
                            Log.d(mTAG, "Not connected to an access point")// Not connected to an access point
                            //fabWifi!!.visibility = View.VISIBLE

                            isWifiConnected = false
                            currentSSID = ""
                            Log.e(mTAG, "info ===> not connected ")
                            //toast(getString(R.string.wifi_state_disconnected))
                        } else {
                            isWifiConnected = true

                            currentSSID = wifiInfo.ssid

                            Log.e(mTAG, "currentSSID = $currentSSID")
                            //toast(getString(R.string.wifi_state_connected, currentSSID))
                            //Log.d(mTAG, "Connected to ${wifiInfo.ssid}")// Not connected to an access point
                            //fabWifi!!.visibility = View.GONE
                        }

                        /*isWifiConnected = true
                        Log.e(mTAG, "info ===> connected ")
                        val wifiManager: WifiManager = mContext!!.getSystemService(Context.WIFI_SERVICE) as WifiManager
                        val wifiInfo = wifiManager.connectionInfo

                        val rssi = wifiInfo.rssi
                        val level = WifiManager.calculateSignalLevel(rssi, 10)
                        val percentage = (level / 10.0 * 100).toInt()

                        Log.e(mTAG, "rssi = $rssi, level = %$level, percentage = $percentage")

                        currentSSID = wifiInfo.ssid*/



                    } else {
                        //show wifi

                        isWifiConnected = false
                        currentSSID = ""
                        Log.e(mTAG, "info ===> not connected ")

                    }

                    val changeIntent = Intent()
                    changeIntent.action = Constants.ACTION.ACTION_WIFI_STATE_CHANGED
                    sendBroadcast(changeIntent)

                }

                if ("android.net.wifi.WIFI_STATE_CHANGED" == intent.action) {
                    Log.e(mTAG, "Wifi WIFI_STATE_CHANGED")

                    //val wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)
                }

                if ("unitech.scanservice.data" == intent.action) {
                    val bundle = intent.extras
                    if (bundle != null) {

                        //if (isWifiConnected) {
                            //detect if is scanning or not
                            if (!isBarcodeScanning) {
                                isBarcodeScanning = true

                                val text = bundle.getString("text")
                                Log.d(mTAG, "text len = ${text!!.length}, text = " + text)

                                //showMyToast(text, ReceiptActivity.this);
                                val firstChar: String = text.substring(0, 1)
                                if (firstChar == "T") {
                                    barcode = ScanBarcode.setPoBarcodeByScanTransform(text.toString().trim())

                                    if (isWifiConnected) {

                                        if (barcode!!.poBarcodeByScan.length == 9) { //material no, ex: T16100359
                                            //isBarcodeScanning = false
                                            val scanIntent = Intent()
                                            scanIntent.action =
                                                Constants.ACTION.ACTION_POSITION_SCAN_BARCODE
                                            scanIntent.putExtra(
                                                "BARCODE_BY_SCAN",
                                                barcode!!.poBarcodeByScan
                                            )
                                            //scanIntent.putExtra("BARCODE", barcode!!.poBarcode)
                                            //scanIntent.putExtra("LINE", barcode!!.poLine)
                                            sendBroadcast(scanIntent)

                                            getPosition(barcode)
                                        } else if (barcode!!.poBarcodeByScan.length > 9){ //storage location
                                            isBarcodeScanning = false
                                            //toast(getString(R.string.storage_location, text))

                                            val storageArray = barcode!!.poBarcodeByScan.split("@")
                                            if (storageArray.size == 3) {
                                                toast(getString(R.string.storage_location, storageArray[2]))
                                                val scanIntent = Intent()
                                                scanIntent.action =
                                                    Constants.ACTION.ACTION_POSITION_PASS_STORAGE
                                                //scanIntent.putExtra(
                                                    //"STORAGE_LOCATION",
                                                    //barcode!!.poBarcodeByScan
                                                //)
                                                scanIntent.putExtra(
                                                    "STORAGE_LOCATION",
                                                    storageArray[2]
                                                )
                                                sendBroadcast(scanIntent)
                                            } else {
                                                toast(getString(R.string.unknown_barcode))
                                            }


                                        } else {
                                            toast(getString(R.string.unknown_barcode))
                                        }


                                    } else {
                                        toast(getString(R.string.get_or_send_failed_wifi_is_not_connected))
                                        isBarcodeScanning = false
                                    }
                                } else { //firstChar is not "T"
                                    isBarcodeScanning = false

                                    toast(getString(R.string.unknown_barcode))

                                    val scanIntent = Intent()
                                    scanIntent.action = Constants.ACTION.ACTION_POSITION_SCAN_BARCODE
                                    sendBroadcast(scanIntent)
                                }


                            } else {
                                Log.e(mTAG, "isBarcodeScanning = true")
                                toast(getString(R.string.barcode_scanning_get_info))
                            }
                        //} else {
                        //    Log.e(mTAG, "Wifi is not connected. Barcode scan is useless.")
                        //    toast(getString(R.string.barcode_scan_off_because_wifi_is_not_connected))
                        //}
                    }
                }
                if ("unitech.scanservice.datatype" == intent.action) {
                    val bundle = intent.extras
                    if (bundle != null) {
                        val type = bundle.getInt("text")

                        Log.d(mTAG, "type = $type")

                    }
                }
            }
        }


        if (!isRegister) {
            filter = IntentFilter()
            //keyboard
            filter.addAction(Constants.ACTION.ACTION_HIDE_KEYBOARD)
            filter.addAction(Constants.ACTION.ACTION_USER_INPUT_SEARCH)
            filter.addAction(Constants.ACTION.ACTION_POSITION_UPDATE_ACTION)
            filter.addAction(Constants.ACTION.ACTION_POSITION_SCAN_BARCODE_CAMERA)
            filter.addAction("android.net.wifi.STATE_CHANGE")
            filter.addAction("android.net.wifi.WIFI_STATE_CHANGED")
            filter.addAction("unitech.scanservice.data")
            filter.addAction("unitech.scanservice.datatype")
            mContext!!.registerReceiver(mReceiver, filter)
            isRegister = true
            Log.d(mTAG, "registerReceiver mReceiver")
        }
    }

    override fun onDestroy() {
        Log.i(mTAG, "onDestroy")

        //enable Scan2Key Setting
        val enableServiceIntent = Intent()
        enableServiceIntent.action = "unitech.scanservice.scan2key_setting"
        enableServiceIntent.putExtra("scan2key", true)
        sendBroadcast(enableServiceIntent)

        if (isRegister && mReceiver != null) {
            try {
                mContext!!.unregisterReceiver(mReceiver)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }

            isRegister = false
            mReceiver = null
            Log.d(mTAG, "unregisterReceiver mReceiver")
        }

        super.onDestroy()
    }

    override fun onResume() {
        Log.i(mTAG, "onResume")
        super.onResume()

        //disable Scan2Key Setting
        val disableServiceIntent = Intent()
        disableServiceIntent.action = "unitech.scanservice.scan2key_setting"
        disableServiceIntent.putExtra("scan2key", false)
        sendBroadcast(disableServiceIntent)

    }

    override fun onPause() {
        Log.i(mTAG, "onPause")
        super.onPause()

        //disable Scan2Key Setting
        val enableServiceIntent = Intent()
        enableServiceIntent.action = "unitech.scanservice.scan2key_setting"
        enableServiceIntent.putExtra("scan2key", true)
        sendBroadcast(enableServiceIntent)
    }

    override fun onBackPressed() {

        showExitConfirmDialog()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_camera -> {
                getPermissionsCamera()
            }


        }


        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        Log.e(mTAG, "onNavigationItemSelected")
        selectDrawerItem(item)
        return true
    }

    private fun selectDrawerItem(menuItem: MenuItem) {

        //hide keyboard
        val view = currentFocus

        if (view != null) {
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }

        when (menuItem.itemId) {

            R.id.nav_about -> {
                showCurrentVersionDialog()
            }

        }
    }

    private fun toast(message: String) {

        if (toastHandle != null) {
            toastHandle!!.cancel()
        }

        val toast = Toast.makeText(this, HtmlCompat.fromHtml("<h1>$message</h1>", HtmlCompat.FROM_HTML_MODE_COMPACT), Toast.LENGTH_LONG)
        toast.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL, 0, 0)
        /*val group = toast.view as ViewGroup
        val textView = group.getChildAt(0) as TextView
        textView.textSize = 30.0f*/
        toast.show()

        toastHandle = toast
    }

    private fun toastLong(message: String) {

        if (toastHandle != null)
            toastHandle!!.cancel()

        val toast = Toast.makeText(this, HtmlCompat.fromHtml("<h1>$message</h1>", HtmlCompat.FROM_HTML_MODE_COMPACT), Toast.LENGTH_LONG)
        toast.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL, 0, 0)
        /*val group = toast.view as ViewGroup
        val textView = group.getChildAt(0) as TextView
        textView.textSize = 30.0f*/
        toast.show()
        toastHandle = toast
    }

    fun checkServerErrorString(res: String): Boolean {
        var ret = false
        if (res.contains("System.OutOfMemoryException")) {
            ret = true
        }

        return ret
    }

    fun getPosition(barcode: ScanBarcode?) {
        if (barcode != null) {
            Log.e(mTAG, "getPosition poBarcode = "+barcode.poBarcode+ ", poLine = "+barcode.poLine)
            val para = HttpGetPositionPara()
            para.data1 = barcode.poBarcodeByScan
            ///para.pmn02 = barcode.poLine

            ApiFunc().getPosition(para, getPositionCallback)

        }


    }//getPosition


    private var getPositionCallback: Callback = object : Callback {

        override fun onFailure(call: Call, e: IOException) {
            isBarcodeScanning = false
            runOnUiThread(netErrRunnable)

        }

        @Throws(IOException::class)
        override fun onResponse(call: Call, response: Response) {
            Log.e(mTAG, "onResponse : "+response.body.toString())
            val res = ReceiveTransform.restoreToJsonStr(response.body!!.string())
            //1.get response ,2 error or right , 3 update ui ,4. restore acState 5. update fragment detail
            runOnUiThread {
                try {
                    Log.e(mTAG, "res = $res")
                    if (res != "Error" && !checkServerErrorString(res)) {


                        val rjPosition =
                            Gson().fromJson(res, RJPosition::class.java) as RJPosition
                        /*
                        val rjReceiptUpload: RJReceiptUpload =
                            Gson().fromJson(res, RJReceiptUpload::class.java) as RJReceiptUpload
                         */

                        if (rjPosition != null) {
                            if (rjPosition.result != ItemPosition.RESULT_CORRECT) {
                                Log.e(
                                    mTAG,
                                    "result = " + rjPosition.result + " result2 = " + rjPosition.result2
                                )
                                //can't receive the item
                                //val mess = retItemReceipt.poNumScanTotal + " " + retItemReceipt.rjReceipt?.result2
                                val mess = rjPosition.result2
                                toastLong(mess)


                                val positionNoIntent = Intent()
                                positionNoIntent.action =
                                    Constants.ACTION.ACTION_POSITION_UPDATE_FAILED
                                sendBroadcast(positionNoIntent)
                            }// result  = 0
                            else {
                                Log.e(mTAG, "2")
                                toastLong(rjPosition.data1)
                                val refreshIntent = Intent()
                                refreshIntent.action =
                                    Constants.ACTION.ACTION_POSITION_FRAGMENT_REFRESH
                                refreshIntent.putExtra("data1", rjPosition.data1)
                                mContext!!.sendBroadcast(refreshIntent)

                            }//result = 1
                        } else {
                            Log.e(mTAG, "rjPosition = null")

                            toast(getString(R.string.this_position_not_exist))

                            val receiptNoIntent = Intent()
                            receiptNoIntent.action = Constants.ACTION.ACTION_POSITION_NOT_EXIST
                            sendBroadcast(receiptNoIntent)
                        }
                    } else {
                        toast(getString(R.string.toast_server_error))

                        val failIntent = Intent()
                        failIntent.action = Constants.ACTION.ACTION_SERVER_ERROR
                        sendBroadcast(failIntent)
                    }


                } catch (ex: Exception) {

                    Log.e(mTAG, "Server error")

                    val serverErrorIntent = Intent()
                    serverErrorIntent.action = Constants.ACTION.ACTION_SERVER_ERROR
                    sendBroadcast(serverErrorIntent)
                    //system error
                    runOnUiThread {

                        toast(getString(R.string.toast_server_error))
                    }
                }
                isBarcodeScanning = false
            }


        }//onResponse
    }

    fun updatePositionDate(materialNo: String) {
        Log.e(mTAG, "updatePositionDate = $materialNo")
        val para = HttpUpdatePositionPara()
        para.data1 = materialNo
        ///para.pmn02 = barcode.poLine

        ApiFunc().updatePositionDate(para, updatePositionDateCallback)


    }//getPosition


    private var updatePositionDateCallback: Callback = object : Callback {

        override fun onFailure(call: Call, e: IOException) {
            isBarcodeScanning = false
            runOnUiThread(netErrRunnable)

        }

        @Throws(IOException::class)
        override fun onResponse(call: Call, response: Response) {
            Log.e(mTAG, "onResponse : "+response.body.toString())
            val res = ReceiveTransform.restoreToJsonStr(response.body!!.string())
            //1.get response ,2 error or right , 3 update ui ,4. restore acState 5. update fragment detail
            runOnUiThread {
                try {
                    Log.e(mTAG, "res = $res")
                    if (res != "Error" && !checkServerErrorString(res)) {


                        val rjUpdatePosition =
                            Gson().fromJson(res, RJUpdatePosition::class.java) as RJUpdatePosition
                        /*
                        val rjReceiptUpload: RJReceiptUpload =
                            Gson().fromJson(res, RJReceiptUpload::class.java) as RJReceiptUpload
                         */

                        if (rjUpdatePosition != null) {
                            if (rjUpdatePosition.result != ItemUpdatePosition.RESULT_CORRECT) {
                                Log.e(
                                    mTAG,
                                    "result = " + rjUpdatePosition.result + " result2 = " + rjUpdatePosition.result2
                                )
                                //can't receive the item
                                //val mess = retItemReceipt.poNumScanTotal + " " + retItemReceipt.rjReceipt?.result2
                                val mess = rjUpdatePosition.result2
                                toastLong(mess)


                                val positionNoIntent = Intent()
                                positionNoIntent.action =
                                    Constants.ACTION.ACTION_POSITION_NOT_EXIST
                                sendBroadcast(positionNoIntent)
                            }// result  = 0
                            else {
                                Log.e(mTAG, "2")
                                //toastLong(rjUpdatePosition.data1)
                                val refreshIntent = Intent()
                                refreshIntent.action =
                                    Constants.ACTION.ACTION_POSITION_UPDATE_SUCCESS
                                //refreshIntent.putExtra("data1", rjUpdatePosition.data1)
                                mContext!!.sendBroadcast(refreshIntent)

                            }//result = 1
                        } else {
                            Log.e(mTAG, "rjUpdatePosition = null")

                            //toast(getString(R.string.this_position_not_exist))

                            //val receiptNoIntent = Intent()
                            //receiptNoIntent.action = Constants.ACTION.ACTION_POSITION_NOT_EXIST
                            //sendBroadcast(receiptNoIntent)
                        }
                    } else {
                        toast(getString(R.string.toast_server_error))

                        val failIntent = Intent()
                        failIntent.action = Constants.ACTION.ACTION_SERVER_ERROR
                        sendBroadcast(failIntent)
                    }


                } catch (ex: Exception) {

                    Log.e(mTAG, "Server error")

                    val serverErrorIntent = Intent()
                    serverErrorIntent.action = Constants.ACTION.ACTION_SERVER_ERROR
                    sendBroadcast(serverErrorIntent)
                    //system error
                    runOnUiThread {

                        toast(getString(R.string.toast_server_error))
                    }
                }
                isBarcodeScanning = false
            }


        }//onResponse
    }

    internal var netErrRunnable: Runnable = Runnable {

        isBarcodeScanning = false

        //mLoadingView.setStatus(LoadingView.GONE)
        // Toast.makeText(mContext,getString(R.string.toast_network_error),Toast.LENGTH_LONG).show();
        //showMyToast(getString(R.string.toast_network_error), mContext)
        toast(getString(R.string.toast_network_error))
        val failIntent = Intent()
        failIntent.action = Constants.ACTION.ACTION_NETWORK_FAILED
        sendBroadcast(failIntent)


    }

    private fun showExitConfirmDialog() {

        // get prompts.xml view
        /*LayoutInflater layoutInflater = LayoutInflater.from(Nfc_read_app.this);
        View promptView = layoutInflater.inflate(R.layout.input_dialog, null);*/
        val promptView = View.inflate(this@MainActivity, R.layout.confirm_dialog, null)

        val alertDialogBuilder = AlertDialog.Builder(this@MainActivity).create()
        alertDialogBuilder.setView(promptView)

        //final EditText editFileName = (EditText) promptView.findViewById(R.id.editFileName);
        val textViewMsg = promptView.findViewById<TextView>(R.id.textViewDialog)
        val btnCancel = promptView.findViewById<Button>(R.id.btnDialogCancel)
        val btnConfirm = promptView.findViewById<Button>(R.id.btnDialogConfirm)

        textViewMsg.text = getString(R.string.exit_app_msg)
        btnCancel.text = getString(R.string.cancel)
        btnConfirm.text = getString(R.string.confirm)

        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
        btnCancel!!.setOnClickListener {
            alertDialogBuilder.dismiss()
        }
        btnConfirm!!.setOnClickListener {
            val drawer : DrawerLayout = findViewById(R.id.drawer_layout)
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START)
            }
            alertDialogBuilder.dismiss()
            //isLogin = false

            finish()


        }
        alertDialogBuilder.show()
    }

    private fun showCurrentVersionDialog() {

        // get prompts.xml view
        /*LayoutInflater layoutInflater = LayoutInflater.from(Nfc_read_app.this);
        View promptView = layoutInflater.inflate(R.layout.input_dialog, null);*/
        val promptView = View.inflate(this@MainActivity, R.layout.about_dialog, null)

        val alertDialogBuilder = AlertDialog.Builder(this@MainActivity).create()
        alertDialogBuilder.setView(promptView)

        //final EditText editFileName = (EditText) promptView.findViewById(R.id.editFileName);
        val textViewMsg = promptView.findViewById<TextView>(R.id.textViewDialog)
        val textViewFixMsg = promptView.findViewById<TextView>(R.id.textViewFixHistory)
        val btnCancel = promptView.findViewById<Button>(R.id.btnDialogCancel)
        val btnConfirm = promptView.findViewById<Button>(R.id.btnDialogConfirm)

        textViewMsg.text = getString(R.string.version_string, BuildConfig.VERSION_CODE, BuildConfig.VERSION_NAME)
        var msg = "[20211109] 第一版\n"
        //msg += "[20210819] 修正伺服器當掉時，使用web service導致App crash。\n"
        //msg += "3. 新增\"設定\"讓使用者決定手動或自動確認"
        textViewFixMsg.text = msg

        btnCancel.text = getString(R.string.cancel)
        btnCancel.visibility = View.GONE
        btnConfirm.text = getString(R.string.confirm)

        // setup a dialog window
        alertDialogBuilder.setCancelable(false)

        btnConfirm!!.setOnClickListener {

            alertDialogBuilder.dismiss()
        }
        alertDialogBuilder.show()

    }

    private fun getPermissionsCamera() {

        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)

        val listPermissionsNeeded = ArrayList<String>()

        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA)
        }

        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionsNeeded.toTypedArray(),
                requestIdMultiplePermission
            )
            //return false;
        } else {
            Log.e(mTAG, "camera permission is granted")

            val intent = Intent(mContext, CameraActivity::class.java)
            /*
            intent.putExtra("SEND_ORDER", currentSendOrder)
            intent.putExtra("TITLE", getString(R.string.nav_outsourced))
            intent.putExtra("WAREHOUSE", currentWarehouse)
            intent.putExtra("SEND_FRAGMENT", "OUTSOURCED_PROCESS")
             */
            startActivity(intent)
        }

        /*if( ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, String[]{Manifest.permission.CAMERA},1)
        }*/


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        Log.d(mTAG, "Permission callback called------- permissions.size = ${permissions.size}")
        when (requestCode) {
            requestIdMultiplePermission -> {

                val perms: HashMap<String, Int> = HashMap()

                // Initialize the map with both permissions
                perms[Manifest.permission.CAMERA] = PackageManager.PERMISSION_GRANTED

                if (grantResults.isNotEmpty()) {
                    for (i in permissions.indices) {
                        perms[permissions[i]] = grantResults[i]
                        Log.e(mTAG, "perms[permissions[$i]] = ${permissions[i]}")

                    }
                    // Check for both permissions
                    if (perms[Manifest.permission.CAMERA] == PackageManager.PERMISSION_GRANTED) {
                        Log.d(mTAG, "camera permission granted")

                    } else {
                        Log.d(mTAG, "Some permissions are not granted ask again ")

                    }//&& perms.get(Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED &&
                    //perms.get(Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED
                }
            }
        }

    }
}
package com.magtonic.magtonicwipposition.ui.position

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.magtonic.magtonicwipposition.MainActivity.Companion.screenHeight
import com.magtonic.magtonicwipposition.MainActivity.Companion.screenWidth
import com.magtonic.magtonicwipposition.R
import com.magtonic.magtonicwipposition.data.Constants
import com.magtonic.magtonicwipposition.databinding.FragmentPositionBinding
import java.util.*

class PositionFragment : Fragment() {

    private val mTAG = PositionFragment::class.java.name

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentPositionBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var progressBar: ProgressBar? = null
    private var relativeLayout: RelativeLayout? = null
    private var barcodeInput: EditText? = null
    private var linearLayout: LinearLayout? = null

    private var mReceiver: BroadcastReceiver? = null
    private var isRegister = false

    //private var toastHandle: Toast? = null

    private var positionContext: Context? = null

    private var storageLocationContent: TextView? = null
    private var storageLocationMatch: TextView? = null
    private var storageLocationUpdate: TextView? = null
    private var resultImage: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        positionContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel =
            ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentPositionBinding.inflate(inflater, container, false)
        val root: View = binding.root

        relativeLayout = binding.positionContainer
        linearLayout = binding.linearLayoutPosition
        progressBar = ProgressBar(positionContext, null, android.R.attr.progressBarStyleLarge)
        val params = RelativeLayout.LayoutParams(screenHeight / 4, screenWidth / 4)
        params.addRule(RelativeLayout.CENTER_IN_PARENT)

        val localRelativeLayout: RelativeLayout? = relativeLayout
        if (localRelativeLayout != null) {
            localRelativeLayout.addView(progressBar, params)
        } else {
            Log.e(mTAG, "localRelativeLayout = null")
        }
        progressBar!!.visibility = View.GONE

        barcodeInput = binding.editTextPoint
        storageLocationContent = binding.storageLocationContent
        storageLocationMatch = binding.storageLocationMatch
        storageLocationUpdate = binding.storageLocationUpdate
        resultImage = binding.resultImage
        //val textView: TextView = binding.textHome
        //homeViewModel.text.observe(viewLifecycleOwner, Observer {
        //    textView.text = it
        //})

        barcodeInput!!.setOnEditorActionListener { _, actionId, _ ->

            when(actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    Log.e(mTAG, "IME_ACTION_DONE")

                    //val hideIntent = Intent()
                    //hideIntent.action = Constants.ACTION.ACTION_HIDE_KEYBOARD
                    //positionContext?.sendBroadcast(hideIntent)
                    storageLocationMatch!!.text = ""
                    storageLocationUpdate!!.text = ""

                    progressBar!!.visibility = View.VISIBLE
                    resultImage!!.visibility = View.INVISIBLE

                    val searchIntent = Intent()
                    searchIntent.action = Constants.ACTION.ACTION_USER_INPUT_SEARCH
                    searchIntent.putExtra("INPUT_NO",
                        barcodeInput!!.text.toString().uppercase(Locale.getDefault())
                    )
                    positionContext?.sendBroadcast(searchIntent)
                    true
                }

                EditorInfo.IME_ACTION_GO -> {
                    Log.e(mTAG, "IME_ACTION_GO")
                    true
                }

                EditorInfo.IME_ACTION_NEXT -> {
                    Log.e(mTAG, "IME_ACTION_NEXT")

                    //val hideIntent = Intent()
                    //hideIntent.action = Constants.ACTION.ACTION_HIDE_KEYBOARD
                    //positionContext?.sendBroadcast(hideIntent)
                    storageLocationMatch!!.text = ""
                    storageLocationUpdate!!.text = ""

                    progressBar!!.visibility = View.VISIBLE
                    resultImage!!.visibility = View.INVISIBLE

                    val searchIntent = Intent()
                    searchIntent.action = Constants.ACTION.ACTION_USER_INPUT_SEARCH
                    searchIntent.putExtra("INPUT_NO",
                        barcodeInput!!.text.toString().uppercase(Locale.getDefault())
                    )
                    positionContext?.sendBroadcast(searchIntent)
                    true
                }

                EditorInfo.IME_ACTION_SEND -> {
                    Log.e(mTAG, "IME_ACTION_SEND")
                    true
                }

                else -> {
                    false
                }
            }




        }

        val filter: IntentFilter

        mReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action != null) {
                    if (intent.action!!.equals(Constants.ACTION.ACTION_BARCODE_NULL, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_BARCODE_NULL")



                        progressBar!!.visibility = View.GONE


                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_NETWORK_FAILED, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_NETWORK_FAILED")

                        progressBar!!.visibility = View.GONE



                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_CONNECTION_TIMEOUT, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_CONNECTION_TIMEOUT")

                        progressBar!!.visibility = View.GONE



                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_CONNECTION_NO_ROUTE_TO_HOST, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_CONNECTION_NO_ROUTE_TO_HOST")

                        progressBar!!.visibility = View.GONE


                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_SERVER_ERROR, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_SERVER_ERROR")

                        progressBar!!.visibility = View.GONE



                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_POSITION_SCAN_BARCODE, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_POSITION_SCAN_BARCODE")
                        try {
                            val barcodeByScan = intent.getStringExtra("BARCODE_BY_SCAN")
                            Log.d(mTAG, "barcodeByScan = $barcodeByScan")
                            //poBarcode = intent.getStringExtra("BARCODE") as String
                            //poLine = intent.getStringExtra("LINE") as String
                            if (barcodeByScan != null) {
                                barcodeInput!!.setText(barcodeByScan)
                                progressBar!!.visibility = View.VISIBLE
                            }

                        } catch (ex: Exception) {
                            barcodeInput!!.setText("")
                            Log.e(mTAG, "Exception : $ex")
                        }
                        storageLocationMatch!!.setTextColor(Color.BLUE)
                        storageLocationMatch!!.text = ""
                        storageLocationUpdate!!.text = ""
                        resultImage!!.visibility = View.INVISIBLE
                        //removeTimer()
                        //stopTimer()

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_POSITION_FRAGMENT_REFRESH, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_POSITION_FRAGMENT_REFRESH")

                        val data1 = intent.getStringExtra("data1")

                        Log.e(mTAG, "data1 = $data1")

                        storageLocationContent!!.text = data1

                        progressBar!!.visibility = View.GONE

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_POSITION_PASS_STORAGE, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_POSITION_PASS_STORAGE")

                        val storageLocation = intent.getStringExtra("STORAGE_LOCATION")

                        Log.e(mTAG, "storageLocation = $storageLocation")


                        if (storageLocation.equals(storageLocationContent!!.text.toString())) {
                            storageLocationMatch!!.setTextColor(Color.BLUE)
                            storageLocationMatch!!.text = getString(R.string.storage_match)

                            val updateIntent = Intent()
                            updateIntent.action = Constants.ACTION.ACTION_POSITION_UPDATE_ACTION
                            updateIntent.putExtra("MATERIAL_NO", barcodeInput!!.text.toString())
                            positionContext!!.sendBroadcast(updateIntent)

                            progressBar!!.visibility = View.VISIBLE
                        } else {
                            storageLocationMatch!!.setTextColor(Color.RED)
                            storageLocationMatch!!.text = getString(R.string.storage_mismatch)
                            storageLocationUpdate!!.text = ""
                            resultImage!!.setImageResource(R.drawable.cross_red)
                            resultImage!!.visibility = View.VISIBLE
                            progressBar!!.visibility = View.GONE
                        }

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_POSITION_FRAGMENT_REFRESH, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_POSITION_FRAGMENT_REFRESH")

                        val data1 = intent.getStringExtra("data1")

                        Log.e(mTAG, "data1 = $data1")
                        progressBar!!.visibility = View.GONE

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_POSITION_UPDATE_FAILED, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_POSITION_UPDATE_FAILED")

                        storageLocationUpdate!!.text = getString(R.string.locate_update_failed)
                        resultImage!!.setImageResource(R.drawable.cross_red)
                        resultImage!!.visibility = View.VISIBLE
                        progressBar!!.visibility = View.GONE

                    } else if (intent.action!!.equals(Constants.ACTION.ACTION_POSITION_UPDATE_SUCCESS, ignoreCase = true)) {
                        Log.d(mTAG, "ACTION_POSITION_UPDATE_SUCCESS")

                        storageLocationUpdate!!.text = getString(R.string.locate_update_success)
                        resultImage!!.setImageResource(R.drawable.circle_green)
                        resultImage!!.visibility = View.VISIBLE
                        progressBar!!.visibility = View.GONE

                    }

                }
            }
        }

        if (!isRegister) {
            filter = IntentFilter()
            filter.addAction(Constants.ACTION.ACTION_BARCODE_NULL)
            filter.addAction(Constants.ACTION.ACTION_NETWORK_FAILED)
            filter.addAction(Constants.ACTION.ACTION_CONNECTION_TIMEOUT)
            filter.addAction(Constants.ACTION.ACTION_SERVER_ERROR)
            filter.addAction(Constants.ACTION.ACTION_POSITION_SCAN_BARCODE)
            filter.addAction(Constants.ACTION.ACTION_POSITION_PASS_STORAGE)
            filter.addAction(Constants.ACTION.ACTION_POSITION_FRAGMENT_REFRESH)
            filter.addAction(Constants.ACTION.ACTION_POSITION_UPDATE_FAILED)
            filter.addAction(Constants.ACTION.ACTION_POSITION_UPDATE_SUCCESS)
            //filter.addAction(Constants.ACTION.ACTION_RECEIPT_ALREADY_UPLOADED_SEND_TO_FRAGMENT)
            positionContext?.registerReceiver(mReceiver, filter)
            isRegister = true
            Log.d(mTAG, "registerReceiver mReceiver")
        }

        return root
    }

    override fun onDestroyView() {
        Log.i(mTAG, "onDestroyView")

        super.onDestroyView()
        _binding = null
    }
}
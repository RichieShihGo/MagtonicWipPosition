package com.magtonic.magtonicwipposition

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.vision.barcode.BarcodeDetector

import com.google.android.gms.vision.CameraSource

import android.widget.TextView

import android.view.SurfaceView
import com.magtonic.magtonicwipposition.databinding.ActivityCameraBinding
import com.magtonic.magtonicwipposition.databinding.ActivityMainBinding
import com.google.android.gms.vision.barcode.Barcode
import android.view.SurfaceHolder
import androidx.core.app.ActivityCompat
import java.io.IOException
import com.google.android.gms.vision.Detector
import android.util.SparseArray
import com.google.mlkit.vision.barcode.Barcode.*

import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning

import com.google.mlkit.vision.barcode.BarcodeScanner
import androidx.annotation.NonNull
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat

import com.google.android.gms.tasks.OnFailureListener

import com.google.android.gms.tasks.OnSuccessListener
import com.google.mlkit.vision.common.InputImage
import com.magtonic.magtonicwipposition.data.Constants
import kotlinx.android.synthetic.main.activity_camera.*
import java.util.concurrent.Executors


class CameraActivity : AppCompatActivity() {
    private val mTAG = CameraActivity::class.java.name

    private lateinit var binding: ActivityCameraBinding

    private var mContext: Context? = null

    //var surfaceView: SurfaceView? = null
    //var textView: TextView? = null
    //var cameraSource: CameraSource? = null
    //var barcodeDetector: BarcodeDetector? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(mTAG, "onCreate")

        mContext = applicationContext

        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //surfaceView = binding.surfaceViewCamera
        //textView = binding.textViewScan

        //barcodeDetector = BarcodeDetector.Builder(this)
        //    .setBarcodeFormats(Barcode.QR_CODE).build()
        //cameraSource =
        //    CameraSource.Builder(mContext, barcodeDetector).setAutoFocusEnabled(true).build()

        /*surfaceView!!.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    if(ActivityCompat.checkSelfPermission(mContext as Context, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                        try {
                            cameraSource!!.start(holder)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {

            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource!!.stop()
            }
        })*/

        /*barcodeDetector!!.setProcessor(object : Detector.Processor<Barcode?> {
            override fun release() {

            }

            override fun receiveDetections(p0: Detector.Detections<Barcode?>) {
                val qrCodes: SparseArray<Barcode?> = p0.detectedItems
                if (qrCodes.size() != 0) {
                    textView!!.post { textView!!.text = qrCodes.valueAt(0)!!.displayValue }
                }
            }
        })*/



        bindCameraUseCases()
    }

    override fun onDestroy() {
        Log.i(mTAG, "onDestroy")

        //enable Scan2Key Setting
        /*val enableServiceIntent = Intent()
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
        }*/

        super.onDestroy()
    }

    override fun onResume() {
        Log.i(mTAG, "onResume")
        super.onResume()

        //disable Scan2Key Setting
        /*val disableServiceIntent = Intent()
        disableServiceIntent.action = "unitech.scanservice.scan2key_setting"
        disableServiceIntent.putExtra("scan2key", false)
        sendBroadcast(disableServiceIntent)
        */
    }

    override fun onPause() {
        Log.i(mTAG, "onPause")
        super.onPause()

        //disable Scan2Key Setting
        /*val enableServiceIntent = Intent()
        enableServiceIntent.action = "unitech.scanservice.scan2key_setting"
        enableServiceIntent.putExtra("scan2key", true)
        sendBroadcast(enableServiceIntent)
        */
    }

    override fun onBackPressed() {

        finish()
    }

    private fun bindCameraUseCases() {
        Log.e(mTAG, "bindCameraUseCases start")
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.CODE_128,
                Barcode.CODE_39,
                Barcode.CODE_93,
                Barcode.CODABAR,
                Barcode.EAN_13,
                Barcode.EAN_8,
                Barcode.ITF,
                Barcode.UPC_A,
                Barcode.UPC_E,
                Barcode.QR_CODE,
                Barcode.PDF417,
                Barcode.AZTEC,
                Barcode.DATA_MATRIX)
            .build()
        val barcodeScanner = BarcodeScanning.getClient(options)

        // setting up the analysis use case
        val analysisUseCase = ImageAnalysis.Builder()
            .build()

        // define the actual functionality of our analysis use case
        analysisUseCase.setAnalyzer(
            // newSingleThreadExecutor() will let us perform analysis on a single worker thread
            Executors.newSingleThreadExecutor(),
            { imageProxy ->
                processImageProxy(barcodeScanner, imageProxy)
            }
        )

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

// setting up the preview use case
            val previewUseCase = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.cameraView.surfaceProvider)
                }

// configure to use the back camera
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    previewUseCase,
                    analysisUseCase
//TODO: Add Image analysis use case
                )
            } catch (illegalStateException: IllegalStateException) {
                // If the use case has already been bound to another lifecycle or method is not called on main thread.
                Log.e(mTAG, illegalStateException.message.orEmpty())
            } catch (illegalArgumentException: IllegalArgumentException) {
                // If the provided camera selector is unable to resolve a camera to be used for the given use cases.
                Log.e(mTAG, illegalArgumentException.message.orEmpty())
            }
        }, ContextCompat.getMainExecutor(this))

        Log.e(mTAG, "bindCameraUseCases end")
    }


    @SuppressLint("UnsafeOptInUsageError")
    private fun processImageProxy(
        barcodeScanner: BarcodeScanner,
        imageProxy: ImageProxy
    ) {

        imageProxy.image?.let { image ->
            val inputImage =
                InputImage.fromMediaImage(
                    image,
                    imageProxy.imageInfo.rotationDegrees
                )

            barcodeScanner.process(inputImage)
                .addOnSuccessListener { barcodeList ->
                    val barcode = barcodeList.getOrNull(0)        // `rawValue` is the decoded value of the barcode
                    barcode?.rawValue?.let { value ->
                        // update our textView to show the decoded value

                        //binding.textViewScan.text = value
                        val refreshIntent = Intent()
                        refreshIntent.action =
                            Constants.ACTION.ACTION_POSITION_SCAN_BARCODE_CAMERA
                        refreshIntent.putExtra("CAMERA_DATA", value)
                        mContext!!.sendBroadcast(refreshIntent)
                        finish()
                            //getString(R.string.barcode_value, value)
                    }
                }
                .addOnFailureListener {
// This failure will happen if the barcode scanning model
                    // fails to download from Google Play Services
                    Log.e(mTAG, it.message.orEmpty())
                }.addOnCompleteListener {
// When the image is from CameraX analysis use case, must
                    // call image.close() on received images when finished
                    // using them. Otherwise, new images may not be received
                    // or the camera may stall.
                    imageProxy.image?.close()
                    imageProxy.close()
                }
        }
    }
}
package com.example.tonguemask

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.OrientationEventListener
import android.view.Surface
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.*
import androidx.camera.core.ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
import androidx.camera.core.R
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.tonguemask.databinding.ActivityCameraXactivityBinding
import kotlinx.android.synthetic.main.dialog_item.view.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraXActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraXactivityBinding
    private var imageCapture: ImageCapture?= null
    private var imageAnalysis: ImageAnalysis?= null
    private var savedUri: Uri ?=null
    private lateinit var outputDirectory: File
    private  lateinit var cameraExecutor: ExecutorService
    private var lensFacing = CameraSelector.DEFAULT_FRONT_CAMERA
    var savefinish:Int = 0

    private companion object {
        const val TAG = "camerax"
        const val FILE_NAME_FORMAT = "yy-MM-dd-HH-mm-ss-SSS"
        const val REQUEST_CODE_PERMISSION = 123
        val REQUIRE_PERMISSION = arrayOf(android.Manifest.permission.CAMERA)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivityCameraXactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()

        if (allPermissionGranted()) {
            startCamera()
        }

        else {
            ActivityCompat.requestPermissions(
                this, REQUIRE_PERMISSION,
                REQUEST_CODE_PERMISSION
            )
        }

        binding.btnTakePhoto.setOnClickListener{
            val view = LayoutInflater.from(this@CameraXActivity).inflate(com.example.tonguemask.R.layout.dialog_item, null)
            takePhoto()
        }
        binding.btnSwitchCamera.setOnClickListener{
            flipCamera()
        }
    }

    private fun getOutputDirectory(): File{
        val mediaDir = externalMediaDirs.firstOrNull()?.let { mFile ->
            File(mFile,"TongueMask").apply {
                mkdirs()
            }
        }
        return if(mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    private fun takePhoto(){
        val imageCapture = imageCapture ?: return
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(FILE_NAME_FORMAT,
                Locale.getDefault())
                .format(System.currentTimeMillis())+".jpg")

        val outputOption = ImageCapture
            .OutputFileOptions
            .Builder(photoFile)
            .build()

        imageCapture.takePicture(
            outputOption, ContextCompat.getMainExecutor(this),
            object :ImageCapture.OnImageSavedCallback{
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    savedUri = Uri.fromFile(photoFile)
                    Log.d(TAG, "savedUri :${savedUri}")
                    val showImg = Intent(this@CameraXActivity, CameraActivity::class.java)
                    showImg.putExtra("Img_taked",savedUri.toString())
                    startActivity(showImg)
                    //val msg = "Photo Saved"
                    //Toast.makeText(this@CameraXActivity,
                        //"$msg $savedUri",
                        //Toast.LENGTH_LONG)
                        //.show()
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG,"onError: ${exception.message}",
                        exception)
                }
            }
        )
    }

    //主鏡頭轉前鏡頭
    private fun flipCamera() {
        if (lensFacing == CameraSelector.DEFAULT_FRONT_CAMERA) lensFacing =
            CameraSelector.DEFAULT_BACK_CAMERA else if (lensFacing == CameraSelector.DEFAULT_BACK_CAMERA) lensFacing =
            CameraSelector.DEFAULT_FRONT_CAMERA
        startCamera()
    }

    private fun startCamera(){
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also { mPreview ->
                    mPreview.setSurfaceProvider(
                        binding.viewFinder.surfaceProvider
                    )
                }
            imageCapture = ImageCapture.Builder().setCaptureMode(CAPTURE_MODE_MINIMIZE_LATENCY).build()
            imageAnalysis = ImageAnalysis.Builder().build()
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,lensFacing,preview,imageCapture
                )

            }catch (e: Exception){
                Log.d(TAG,"startCamera Fail!",e)
            }
        }, ContextCompat.getMainExecutor(this))

        val p = Paint()
        p.strokeWidth = 2f
        p.color = Color.RED
        p.style = Paint.Style.STROKE

        val bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val rect = RectF(100f, 100f, 400f, 400f)
        canvas.drawRect(rect, p)
    }

    //處理手機旋轉時，照片轉動問題
    private val orientationEventListener by lazy {
        object : OrientationEventListener(this) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) {
                    return
                }
                val rotation = when (orientation) {
                    in 45 until 135 -> Surface.ROTATION_270
                    in 135 until 225 -> Surface.ROTATION_180
                    in 225 until 315 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }
                imageAnalysis?.targetRotation = rotation
                imageCapture?.targetRotation = rotation
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION){
            if(allPermissionGranted()){
                //our code
                startCamera()
            }else{
                Toast.makeText(
                    this,
                    "Permission Not Granted",
                    Toast.LENGTH_SHORT
                ).show()

                finish()
            }
        }
    }

    private fun allPermissionGranted()=
        REQUIRE_PERMISSION.all {
            ContextCompat.checkSelfPermission(
                baseContext,it
            ) == PackageManager.PERMISSION_GRANTED
        }

    override fun onStart() {
        super.onStart()
        orientationEventListener.enable()
    }

    override fun onStop() {
        super.onStop()
        orientationEventListener.disable()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
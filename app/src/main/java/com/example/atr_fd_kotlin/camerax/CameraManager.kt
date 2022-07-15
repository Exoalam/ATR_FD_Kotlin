package com.example.atr_fd_kotlin.camerax

import android.R.attr.bitmap
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.Base64
import android.util.Log
import android.util.Size
import android.view.Surface.*
import androidx.camera.core.*
import androidx.camera.core.AspectRatio.RATIO_4_3
import androidx.camera.core.Camera
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.atr_fd_kotlin.face_detection.FaceContourDetectionProcessor
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameraManager(
    private val context: Context,
    private val finderView: PreviewView,
    private val lifecycleOwner: LifecycleOwner,
    private val graphicOverlay: GraphicOverlay
) {

    private var preview: Preview? = null

    private var camera: Camera? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private var cameraSelectorOption = CameraSelector.LENS_FACING_FRONT
    private var cameraProvider: ProcessCameraProvider? = null

    private var imageAnalyzer: ImageAnalysis? = null

    private var imageCapture: ImageCapture? = null


    init {
        createNewExecutor()
    }

    private fun createNewExecutor() {
        cameraExecutor = Executors.newSingleThreadExecutor()
    }


    @SuppressLint("RestrictedApi")
    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(
            Runnable {
                cameraProvider = cameraProviderFuture.get()
                preview = Preview.Builder()
                    .build()

                imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, selectAnalyzer())
                    }

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(cameraSelectorOption)
                    .build()
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .setBufferFormat(ImageFormat.JPEG)
                    .setDefaultResolution(Size(512, 512))
                    .setTargetAspectRatio(RATIO_4_3)
                    .build()

                setCameraConfig(cameraProvider, cameraSelector)

            }, ContextCompat.getMainExecutor(context)
        )
    }

    private fun selectAnalyzer(): ImageAnalysis.Analyzer {
        return FaceContourDetectionProcessor(graphicOverlay)
    }

    private fun setCameraConfig(
        cameraProvider: ProcessCameraProvider?,
        cameraSelector: CameraSelector
    ) {
        try {
            cameraProvider?.unbindAll()
            camera = cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalyzer,
                imageCapture
            )
            preview?.setSurfaceProvider(
                finderView.surfaceProvider
            )
        } catch (e: Exception) {
            Log.e(TAG, "Use case binding failed", e)
        }
    }

    fun changeCameraSelector() {
        cameraProvider?.unbindAll()
        cameraSelectorOption =
            if (cameraSelectorOption == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT
            else CameraSelector.LENS_FACING_BACK
        graphicOverlay.toggleSelector()
        startCamera()
    }

    fun capture_image() {
        val thread = Thread {
            imageCapture!!.takePicture(
                cameraExecutor,
                object : ImageCapture.OnImageCapturedCallback() {

                    @SuppressLint("UnsafeExperimentalUsageError")
                    override fun onCaptureSuccess(imageProxy: ImageProxy) {
                        var data = imageProxyToBitmap(imageProxy)
                        val byteArrayOutputStream = ByteArrayOutputStream()
                        data!!.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
                        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
                        val encoded: String = Base64.encodeToString(byteArray, Base64.DEFAULT)
                        Log.d("data", "{{" + encoded.toString() + "}}")
                    }

                    override fun onError(exception: ImageCaptureException) {
                        super.onError(exception)
                    }
                })
        }
            thread.start()

    }

    companion object {
        private const val TAG = "CameraXBasic"
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
        val buffer: ByteBuffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
    }

}
package com.example.atr_fd_kotlin.face_detection

import android.graphics.Bitmap
import android.graphics.Rect
import android.media.Image
import android.os.CountDownTimer
import android.util.Log
import android.view.TextureView
import androidx.recyclerview.widget.RecyclerView
import com.example.atr_fd_kotlin.Communication
import com.example.atr_fd_kotlin.MainActivity
import com.example.atr_fd_kotlin.camerax.BaseImageAnalyzer
import com.example.atr_fd_kotlin.camerax.CameraManager
import com.example.atr_fd_kotlin.camerax.GraphicOverlay
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

import java.io.IOException
import java.net.Socket


class FaceContourDetectionProcessor(private val view: GraphicOverlay) :
    BaseImageAnalyzer<List<Face>>() {
    private val realTimeOpts = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
        .build()
    private val  imaged: Image? = null
    private val detector = FaceDetection.getClient(realTimeOpts)

    override val graphicOverlay: GraphicOverlay
        get() = view

    override fun detectInImage(image: InputImage): Task<List<Face>> {
        
        return detector.process(image)
    }

    override fun stop() {
        try {
            detector.close()
        } catch (e: IOException) {
            Log.e(TAG, "Exception thrown while trying to close Face Detector: $e")
        }
    }

    override fun onSuccess(
        results: List<Face>,
        graphicOverlay: GraphicOverlay,
        rect: Rect
    ) {
        graphicOverlay.clear()
        results.forEach {
            if(gg){
                capture()
                gg = false
                var communication: Communication = Communication()
                communication.receiveData()
            }
            val faceGraphic = FaceContourGraphic(graphicOverlay, it, rect)
            graphicOverlay.add(faceGraphic)
        }
        graphicOverlay.postInvalidate()
    }

    override fun onFailure(e: Exception) {
        Log.w(TAG, "Face Detector failed.$e")
    }

    companion object {
        var gg = true
        private const val TAG = "FaceDetectorProcessor"
    }

    fun capture() {
        MainActivity.cameraManager.capture_image()
    }


}
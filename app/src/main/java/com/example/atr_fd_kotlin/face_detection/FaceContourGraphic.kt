package com.example.atr_fd_kotlin.face_detection

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.Log
import com.example.atr_fd_kotlin.camerax.GraphicOverlay
import com.google.mlkit.vision.face.Face
import kotlin.math.log

class FaceContourGraphic(
    overlay: GraphicOverlay,
    private val face: Face,
    private val imageRect: Rect
) : GraphicOverlay.Graphic(overlay) {

    private val facePositionPaint: Paint
    private val idPaint: Paint
    private val boxPaint: Paint
    private val paint: Paint

    init {
        val selectedColor = Color.BLACK

        facePositionPaint = Paint()
        facePositionPaint.color = selectedColor

        idPaint = Paint()
        idPaint.color = selectedColor

        boxPaint = Paint()
        boxPaint.color = selectedColor
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = BOX_STROKE_WIDTH

        paint = Paint()
        paint.color = Color.RED
        paint.style = Paint.Style.FILL
        paint.textSize = 72f
    }

    override fun draw(canvas: Canvas?) {
        val rect = calculateRect(
            imageRect.height().toFloat(),
            imageRect.width().toFloat(),
            face.boundingBox
        )
        canvas?.drawRect(rect, boxPaint)
        canvas?.drawText(image_name, rect.left.toFloat(), rect.top.toFloat(), paint)
    }

    companion object {
        private const val BOX_STROKE_WIDTH = 5.0f
        var image_name = ""
    }

}
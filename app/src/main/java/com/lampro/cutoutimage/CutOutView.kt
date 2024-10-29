package com.lampro.cutoutimage

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import kotlin.math.pow
import kotlin.math.sqrt

class CutOutView : View {

    private val path: Path = Path()
    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var currentX = 0f
    private var currentY = 0f
    private var startX = 0f
    private var startY = 0f

    private var isReady = false

    constructor(context: Context) : super(context, null)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    private lateinit var listener : (isReady: Boolean) -> Unit

    private var viewCanvasWidth: Int = 0
    private var viewCanvasHeight : Int = 0

    private var targetOriginalBitmap: Bitmap? = null
    private var actualVisibleBitmap: Bitmap? = null

    private val TAG = "CutOutView"

    init {
        path.reset()
        paint.apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 10f
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    fun setImageBitmap(imageBitmap: Bitmap) {
        targetOriginalBitmap = imageBitmap
        actualVisibleBitmap = null
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        viewCanvasWidth = width
        viewCanvasHeight = height

        if (targetOriginalBitmap != null) {
            // If target's original bitmap is bigger than view size, adjust size for fit
            if (actualVisibleBitmap == null) actualVisibleBitmap = scaleBitmapAndKeepRation(
                targetOriginalBitmap!!,
                    height,
                    width
                )
            canvas.drawBitmap(
                actualVisibleBitmap!!,
                (viewCanvasWidth / 2 - actualVisibleBitmap!!.width / 2).toFloat(),
                (viewCanvasHeight / 2 - actualVisibleBitmap!!.height / 2).toFloat(),
                null
            )
            canvas.drawPath(path,paint)

        } else {
            canvas.drawColor(Color.WHITE)

            val textPaint = Paint()
            textPaint.color = Color.BLACK
            textPaint.textSize = 20f
            textPaint.isAntiAlias = true

            canvas.drawText(
                "Please set image bitmap for process",
                (width/3).toFloat(),
                (height/2).toFloat(),
                textPaint
            )
            isReady = false

        }

    }

    fun scaleBitmapAndKeepRation(
        TargetBmp: Bitmap,
        reqHeightInPixels: Int,
        reqWidthInPixels: Int
    ): Bitmap {
        val m = Matrix()
        m.setRectToRect(
            RectF(0f, 0f, TargetBmp.width.toFloat(), TargetBmp.height.toFloat()),
            RectF(0f, 0f, reqWidthInPixels.toFloat(), reqHeightInPixels.toFloat()),
            Matrix.ScaleToFit.CENTER
        )
        return Bitmap.createBitmap(TargetBmp, 0, 0, TargetBmp.width, TargetBmp.height, m, true)
    }
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (targetOriginalBitmap == null) {
            return false
        }


        event?.let {

            val x = event.x
            val y = event.y

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    path.reset()
                    path.moveTo(x, y)
                    currentX = x
                    currentY = y

                    startX = x
                    startY = y

                    isReady = false
                    listener(isReady)
                }
                MotionEvent.ACTION_MOVE -> {

                    path.quadTo(currentX, currentY, (x + currentX) / 2, (y + currentY) / 2)
                    currentX = x
                    currentY = y

                }
                MotionEvent.ACTION_UP -> {
                    path.quadTo(currentX, currentY, (x + currentX) / 2, (y + currentY) / 2)
                    
                    val distance = calculateDistance(startX,startY,currentX,currentY)

                    if (distance < 100) {
                        path.quadTo(x,y, startX , startY)

                        isReady = true
                        listener(isReady)
                    } else
                    {
                        Toast.makeText(
                            this.context,
                            context.getString(R.string.toast_text_cut_out),
                            Toast.LENGTH_SHORT
                        ).show()
                        path.reset()
                        isReady = false
                        listener(isReady)
                        invalidate()
                    }
                }
            }

            invalidate()
            return true
        }

        return super.onTouchEvent(event)
    }

    private fun calculateDistance(startX: Float, startY: Float, endX: Float, endY: Float) : Double {
        val offsetX = endX - startX
        val offsetY = endY - startY
        return sqrt(offsetX.toDouble().pow(2.0) + offsetY.toDouble().pow(2.0))
    }

    override fun onHoverEvent(event: MotionEvent?): Boolean {
        return super.onHoverEvent(event)
    }

    override fun onDragEvent(event: DragEvent?): Boolean {
        event?.let {
            path.lineTo(event.x, event.y)
            invalidate()
            Log.e(TAG, "onDragEvent:" )
            return true
        }
        return super.onDragEvent(event)
    }

    fun cropImageWithPath(): Bitmap {

        val resultImage =
            Bitmap.createBitmap(viewCanvasWidth, viewCanvasHeight, actualVisibleBitmap!!.config)

        val resultCanvas = Canvas(resultImage)
        val resultPaint = Paint()


        // struct paint for naturally
        resultPaint.isAntiAlias = true
        resultPaint.isDither = true // set the dither to true
        resultPaint.strokeJoin = Paint.Join.ROUND // set the join to round you want
        resultPaint.strokeCap = Paint.Cap.ROUND // set the paint cap to round too
        resultPaint.setPathEffect(CornerPathEffect(10f))
        resultPaint.isAntiAlias = true // set anti alias so it smooths


        // struct paint for path-crop
        resultCanvas.drawPath(path, resultPaint)
        resultPaint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))

        val dst = Rect(
            viewCanvasWidth / 2 - actualVisibleBitmap!!.width / 2,
            viewCanvasHeight / 2 - actualVisibleBitmap!!.height / 2,
            viewCanvasWidth / 2 + actualVisibleBitmap!!.width / 2,
            viewCanvasHeight / 2 + actualVisibleBitmap!!.height / 2
        )

        resultCanvas.drawBitmap(actualVisibleBitmap!!, null, dst, resultPaint)

        return BitmapUtils.cropBitmapToBoundingBox(resultImage, Color.TRANSPARENT)
    }

    fun cropImageWithShape(): Bitmap{
        val resultImage =
            Bitmap.createBitmap(viewCanvasWidth, viewCanvasHeight, actualVisibleBitmap!!.config)

        val resultCanvas = Canvas(resultImage)
        val resultPaint = Paint()


        // struct paint for naturally
        resultPaint.isAntiAlias = true
        resultPaint.isDither = true // set the dither to true
        resultPaint.strokeJoin = Paint.Join.ROUND // set the join to round you want
        resultPaint.strokeCap = Paint.Cap.ROUND // set the paint cap to round too
        resultPaint.setPathEffect(CornerPathEffect(10f))
        resultPaint.isAntiAlias = true // set anti alias so it smooths


        // struct paint for path-crop
        resultCanvas.drawPath(path, resultPaint)
        resultPaint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))

        val dst = Rect(
            viewCanvasWidth / 2 - actualVisibleBitmap!!.width / 2,
            viewCanvasHeight / 2 - actualVisibleBitmap!!.height / 2,
            viewCanvasWidth / 2 + actualVisibleBitmap!!.width / 2,
            viewCanvasHeight / 2 + actualVisibleBitmap!!.height / 2
        )
        resultCanvas.drawBitmap(actualVisibleBitmap!!, null, dst, resultPaint)

        return resultImage
    }

    fun getIsReady() : Boolean {
        return isReady
    }

    fun setOnCutOutListener(callback: (isReady: Boolean) -> Unit) {
            listener = callback
    }

}
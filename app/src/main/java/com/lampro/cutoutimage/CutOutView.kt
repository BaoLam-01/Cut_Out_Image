package com.lampro.cutoutimage

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View

class CutOutView : View {

    private val path: Path = Path()
    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var currentX = 0f
    private var currentY = 0f

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

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
        // Account for padding.
        var xpad = (paddingLeft + paddingRight).toFloat()
        val ypad = (paddingTop + paddingBottom).toFloat()

        // Account for the label.
//        if (showText) xpad += textWidth.toFloat()
//        val ww = w.toFloat() - xpad
//        val hh = h.toFloat() - ypad
//
        // Figure out how big you can make the pie.
//        val diameter = Math.min(ww, hh)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(path,paint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {


        event?.let {

            val x = event.x
            val y = event.y

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Khi bắt đầu chạm, di chuyển path đến vị trí chạm
                    path.moveTo(x, y)
                    currentX = x
                    currentY = y
                }
                MotionEvent.ACTION_MOVE -> {
                    // Khi di chuyển, thêm các điểm vào path
                    path.quadTo(currentX, currentY, (x + currentX) / 2, (y + currentY) / 2)
                    currentX = x
                    currentY = y
                }
                MotionEvent.ACTION_UP -> {
                    // Khi kết thúc chạm, vẽ đường đến điểm cuối
                    path.lineTo(currentX, currentY)
                }
            }

            invalidate() // Gọi vẽ lại màn hình
            return true
        }

        return super.onTouchEvent(event)
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

}
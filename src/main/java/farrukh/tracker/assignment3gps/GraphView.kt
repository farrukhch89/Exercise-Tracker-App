package farrukh.tracker.assignment3gps

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class GraphView : View {
        private var paint_B: Paint? = null
        private var paint_W: Paint? = null
        var myGraphArray: DoubleArray? = null
        var m_maxY = 0.0

        constructor(context: Context?) : super(context) {
            init()
        }

        constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
            init()
        }

        constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
        ) {
            init()
        }

        fun init() {
            paint_B = Paint(Paint.ANTI_ALIAS_FLAG)
            paint_W = Paint(Paint.ANTI_ALIAS_FLAG)
            paint_B!!.color = ContextCompat.getColor(context, R.color.black)
            paint_W!!.color = ContextCompat.getColor(context, R.color.white)
            paint_W!!.strokeWidth = 7f
        }

        fun setGraphArray_double_para(Xi_graphArray: DoubleArray?, Xi_maxY: Double) {
            myGraphArray = Xi_graphArray
            m_maxY = Xi_maxY
        }

        fun setGraphArray_double_para(Xi_graphArray: DoubleArray) {
            var maxY = 0.0
            for (i in Xi_graphArray.indices) {
                if (Xi_graphArray[i] > maxY) {
                    maxY = Xi_graphArray[i]
                }
            }
            setGraphArray_double_para(Xi_graphArray, maxY)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint_B!!)
            if (myGraphArray == null) {
                return
            }
            val maxX = myGraphArray!!.size
            val factorX = width / maxX.toDouble() - 5
            val factorY = height / m_maxY - 7
            for (i in 1 until myGraphArray!!.size) {
                val j = i - 1
                val x0 = i - 1
                val y0 = myGraphArray!![i - 1]
                val y1 = myGraphArray!![i]
                val sx = (x0 * factorX).toInt()
                val sy = height - (y0 * factorY).toInt()
                val ex = (i * factorX).toInt()
                val ey = height - (y1 * factorY).toInt()
                canvas.drawLine(sx.toFloat(), sy.toFloat(), ex.toFloat(), ey.toFloat(), paint_W!!)
                paint_W!!.textSize = 35f
                canvas.drawText(
                    "" + String.format("%.03f", myGraphArray!![j] * 10),
                    sx.toFloat(),
                    sy.toFloat(),
                    paint_W!!
                )
            }
            canvas.drawText("Speed", 30f, 30f, paint_W!!)
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            var width = measuredWidth
            var height = measuredHeight
            val widthWithoutPadding = width - paddingLeft - paddingRight
            val heigthWithoutPadding = height - paddingTop - paddingBottom
            val maxWidth = (heigthWithoutPadding * RATIO).toInt()
            val maxHeight = (widthWithoutPadding / RATIO).toInt()
            if (widthWithoutPadding > maxWidth) {
                width = maxWidth + paddingLeft + paddingRight
            } else {
                height = maxHeight + paddingTop + paddingBottom
            }
            if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
                width = measuredWidth
            }
            if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
                height = measuredHeight
            }
            setMeasuredDimension(width, height)
        }

        companion object {
            private const val RATIO = 4f / 3f
        }
    }
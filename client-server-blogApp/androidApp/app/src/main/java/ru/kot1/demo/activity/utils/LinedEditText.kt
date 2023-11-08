package ru.kot1.demo.activity.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet


class LinedEditText(paramContext: Context, paramAttributeSet: AttributeSet?) :
    androidx.appcompat.widget.AppCompatEditText(paramContext, paramAttributeSet) {
    private val linePaint: Paint = Paint().apply { color= Color.parseColor("#7792E3") }
    private val margin = 0f
    private val paperColor = 0

    override fun onDraw(paramCanvas: Canvas) {
        paramCanvas.drawColor(paperColor)
        var i = lineCount
        val j = height
        val k = lineHeight
        val m = 1 + j / k
        if (i < m) i = m
        var n = compoundPaddingTop
        paramCanvas.drawLine(0.0f, n.toFloat(), right.toFloat(), n.toFloat(), linePaint)
        var i2 = 0
        while (true) {
            if (i2 >= i) {
                setPadding(10 + margin.toInt(), 0, 0, 0)
                super.onDraw(paramCanvas)
                paramCanvas.restore()
                return
            }
            n += k
            paramCanvas.drawLine(0.0f, n.toFloat(), right.toFloat(), n.toFloat(), linePaint)
            paramCanvas.save()
            i2++
        }
    }

}
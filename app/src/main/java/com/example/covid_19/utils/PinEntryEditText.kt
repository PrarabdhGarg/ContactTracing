package com.example.covid_19.utils

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.widget.EditText

class PinEntryEditText : EditText {
    var mSpace = 24f
    var mCharSize = 0f
    var mNumChars = 4f

    constructor(context: Context?) : super(context) {}
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet,
                defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet,
                defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet) {
        setBackgroundResource(0)

        val multi = context.resources.displayMetrics.density
        mSpace = multi * mSpace //convert to pixels for our density

    }
    override fun onDraw(canvas: Canvas?) {
        // super.onDraw(canvas)
        val availableWidth = width - paddingRight - paddingLeft
        mCharSize = if (mSpace < 0) {
            availableWidth / (mNumChars * 2 - 1)
        } else {
            (availableWidth - mSpace * (mNumChars - 1)) / mNumChars
        }

        var startX = paddingLeft
        val bottom = height - paddingBottom

        paint.apply {
            color = Color.parseColor("#FFFFFF")
        }

        for (i in 0 until mNumChars.toInt()) {
            canvas!!.drawLine(startX.toFloat(), bottom.toFloat(), startX + mCharSize, bottom.toFloat(), paint)
            if (mSpace < 0) {
                startX += (mCharSize * 2).toInt()
            } else {
                startX += (mCharSize + mSpace.toInt()).toInt()
            }
        }
    }
}
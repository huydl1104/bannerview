package com.ydl.bannerlib.hintview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.ydl.bannerlib.interfces.BaseHintView
import java.lang.StringBuilder

class TextHintView : AppCompatTextView, BaseHintView {
    private var length = 0

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
    }

    override fun initView(length: Int, gravity: Int, padding : Rect, margin: Rect) {
        this.length = length
        setTextColor(Color.WHITE)
        when (gravity) {
            0 -> setGravity(Gravity.START or Gravity.CENTER_VERTICAL)
            1 -> setGravity(Gravity.CENTER)
            2 -> setGravity(Gravity.END or Gravity.CENTER_VERTICAL)
        }

        val params =layoutParams as ViewGroup.MarginLayoutParams
        params.bottomMargin = margin.bottom
        params.topMargin = margin.top
        params.leftMargin = margin.left
        params.rightMargin = margin.right

        setPadding(padding.left,padding.top,padding.right,padding.bottom)
//        Log.e("yuyu","leftMargin ->${margin.left} , topMargin -->${margin.top} ,rightMargin ->${margin.right} ,bottomMargin -->${margin.bottom}")
//        Log.e("yuyu","paddingLeft ->${padding.left} , paddingTop -->${padding.top} ,paddingRight ->${padding.right} ,paddingBottom -->${padding.bottom}")
        setCurrent(0)
    }

    @SuppressLint("SetTextI18n")
    override fun setCurrent(current: Int) {
        val builder = StringBuilder().append((current + 1)).append("/").append(length)
        text = builder.toString()
    }
}
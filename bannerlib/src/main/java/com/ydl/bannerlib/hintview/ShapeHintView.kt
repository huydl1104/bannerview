package com.ydl.bannerlib.hintview

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import com.ydl.bannerlib.interfces.BaseHintView
import java.util.ArrayList

/**
 * shape图形
 */
abstract class ShapeHintView : LinearLayout, BaseHintView {
    private var mDots: Array<ImageView?>?=null
    private var length = 0
    private var lastPosition = 0
    private var dotNormal: Drawable? = null
    private var dotFocus: Drawable? = null

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
    }

    abstract fun makeFocusDrawable(): Drawable?
    abstract fun makeNormalDrawable(): Drawable?
    override fun initView(length: Int, gravity: Int, padding : Rect, margin: Rect) {
        removeAllViews()
        lastPosition = 0
        orientation = HORIZONTAL
//        Log.e("yuyu", "initView  gravity ->$gravity")
        when (gravity) {
            0 -> setGravity(Gravity.START or Gravity.CENTER_VERTICAL)
            1 -> setGravity(Gravity.CENTER )
            2 -> setGravity(Gravity.END or Gravity.CENTER_VERTICAL)
        }
        this.length = length
        mDots = arrayOfNulls(length)
        dotFocus = makeFocusDrawable()
        dotNormal = makeNormalDrawable()
        for (i in 0 until length) {
            mDots!![i] = ImageView(context)
            val dotLp = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            )
            dotLp.setMargins(10, 0, 10, 0)
            mDots!![i]!!.layoutParams = dotLp
            mDots!![i]!!.setBackgroundDrawable(dotNormal)
            addView(mDots!![i])
        }

        val params =layoutParams as MarginLayoutParams
        params.bottomMargin = margin.bottom
        params.topMargin = margin.top
        params.leftMargin = margin.left
        params.rightMargin = margin.right

        setPadding(padding.left,padding.top,padding.right,padding.bottom)

        setCurrent(0)
    }


    override fun setCurrent(current: Int) {
        if (current < 0 || current > length - 1) {
            return
        }
        mDots!![lastPosition]?.setBackgroundDrawable(dotNormal)
        mDots!![current]?.setBackgroundDrawable(dotFocus)
        lastPosition = current
    }
}
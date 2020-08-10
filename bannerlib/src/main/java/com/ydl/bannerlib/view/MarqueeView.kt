package com.ydl.bannerlib.view

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewTreeObserver
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.ViewFlipper
import com.ydl.bannerlib.R
import com.ydl.bannerlib.R.styleable.MarqueeViewStyle
import com.ydl.bannerlib.util.Utils
import java.util.*

class MarqueeView : ViewFlipper {

    companion object{
        const val TEXT_GRAVITY_LEFT = 0
        const val TEXT_GRAVITY_CENTER = 1
        const val TEXT_GRAVITY_RIGHT = 2
    }
    private var mContext: Context? = null
    private var noticesList: ArrayList<String>? = null
    private var isSetAnimDuration = false
    private var mOnItemClickListener: OnItemClickListener? = null

    private var interval = 2000
    private var animDuration = 500
    private var textSize = 14
    private var textColor = -0x1

    private var singleLine = false
    private var mGravity = Gravity.START or Gravity.CENTER_VERTICAL

    constructor(context: Context,attrs:AttributeSet):super(context,attrs){
        mContext = context
        if (noticesList == null) {
            noticesList = ArrayList()
        }
        val typedArray = getContext().obtainStyledAttributes(attrs, MarqueeViewStyle, 0, 0)
        interval = typedArray.getInteger(R.styleable.MarqueeViewStyle_mvInterval, interval)
        isSetAnimDuration = typedArray.hasValue(R.styleable.MarqueeViewStyle_mvAnimDuration)
        singleLine = typedArray.getBoolean(R.styleable.MarqueeViewStyle_mvSingleLine, false)
        animDuration = typedArray.getInteger(R.styleable.MarqueeViewStyle_mvAnimDuration, animDuration)
        if (typedArray.hasValue(R.styleable.MarqueeViewStyle_mvTextSize)) {
            textSize = typedArray.getDimension(R.styleable.MarqueeViewStyle_mvTextSize, textSize.toFloat()).toInt()
            textSize = Utils.px2sp(context, textSize.toFloat())
        }
        textColor = typedArray.getColor(R.styleable.MarqueeViewStyle_mvTextColor, textColor)
        val gravityType = typedArray.getInt(R.styleable.MarqueeViewStyle_mvGravity, TEXT_GRAVITY_LEFT)
        when (gravityType) {
            TEXT_GRAVITY_CENTER -> mGravity = Gravity.CENTER
            TEXT_GRAVITY_RIGHT -> mGravity = Gravity.START or Gravity.CENTER_VERTICAL
        }
        typedArray.recycle()
        flipInterval = interval
    }

    fun startWithText(notice:String){
        if (TextUtils.isEmpty(notice)){
            return
        }
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener{
            override fun onGlobalLayout() {
                viewTreeObserver.removeGlobalOnLayoutListener(this);
                startWithFixedWidth(notice, getWidth());
            }

        });
    }

    fun startWithList(notices: ArrayList<String>) {
        setNotices(notices)
        start()
    }
    fun start(): Boolean {
        if (noticesList == null || noticesList!!.size == 0) {
            return false
        }
        //先移除所有view
        removeAllViews()
        //然后重置动画
        resetAnimation()
        //根据设置的数据集合数量创建TextView
        for (i in noticesList!!.indices) {
            val textView = createTextView(noticesList!![i], i)
            val finalI: Int = i
            textView.setOnClickListener {
                mOnItemClickListener?.onItemClick(finalI, textView)
            }
            addView(textView)
        }
        //如果集合数目大于1，则开始；否则停止
        if (noticesList!!.size > 1) {
            startFlipping()
        } else {
            stopFlipping()
        }
        return true
    }
    private fun resetAnimation() {
        clearAnimation()
        val animIn = AnimationUtils.loadAnimation(mContext, R.anim.anim_marquee_in)
        if (isSetAnimDuration) {
            animIn.duration = animDuration.toLong()
        }
        inAnimation = animIn
        //设置结束的动画
        val animOut = AnimationUtils.loadAnimation(mContext, R.anim.anim_marquee_out)
        if (isSetAnimDuration) {
            animOut.duration = animDuration.toLong()
        }
        outAnimation = animOut
    }
    private fun startWithFixedWidth(notice: String, width: Int) {
        val noticeLength = notice.length
        val dpW: Int = Utils.px2dip(mContext!!, width.toFloat())
        val limit = dpW / textSize
        if (dpW == 0) {
            throw RuntimeException("Please set MarqueeView width !")
        }
        val list = ArrayList<String>()
        if (noticeLength <= limit) {
            list.add(notice)
        } else {
            val size = noticeLength / limit + if (noticeLength % limit != 0) 1 else 0
            for (i in 0 until size) {
                val startIndex = i * limit
                val endIndex =
                    if ((i + 1) * limit >= noticeLength) noticeLength else (i + 1) * limit
                list.add(notice.substring(startIndex, endIndex))
            }
        }
        noticesList?.addAll(list)
        start()
    }

    private fun createTextView(text: CharSequence, position: Int): TextView {
        val tv = TextView(mContext)
        tv.gravity = mGravity
        tv.text = text
        tv.setTextColor(textColor)
        tv.textSize = textSize.toFloat()
        tv.isSingleLine = singleLine
        tv.tag = position
        return tv
    }

    fun getPosition(): Int {
        return currentView.tag as Int
    }

    fun getNotices(): ArrayList<String> {
        return noticesList!!
    }

    fun setNotices(notices: ArrayList<String>) {
        this.noticesList = notices
    }



    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.mOnItemClickListener = onItemClickListener
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, textView: TextView?)
    }
}
package com.ydl.bannerlib.view

import android.annotation.TargetApi
import android.content.Context
import android.database.DataSetObserver
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Interpolator
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Scroller
import androidx.annotation.ColorInt
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.ydl.bannerlib.R
import com.ydl.bannerlib.adapter.LoopPagerAdapter
import com.ydl.bannerlib.hintview.ColorPointHintView
import com.ydl.bannerlib.hintview.TextHintView
import com.ydl.bannerlib.interfces.BaseHintView
import com.ydl.bannerlib.interfces.HintViewDelegate
import com.ydl.bannerlib.util.Utils.dip2px
import java.lang.ref.WeakReference
import java.util.*

class CustomBannerView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RelativeLayout(context, attrs, defStyle) {

    companion object{
        private val TAG = "CustomBannerView"
    }
    init {
        initView(attrs)
    }
    private var mViewPager: BannerViewPager? = null
    private var mAdapter: PagerAdapter? = null
    private var mGestureDetector: GestureDetector? = null
    private var mRecentTouchTime: Long = 0
    /**
     * 指示器类型
     */
    private var hintMode = 0
    /**
     * 播放延迟
     */
    private var delay = 0
    /**
     * hint位置
     */
    private var mGravity = 0
    /**
     * hint颜色
     */
    private var color = 0
    /**
     * hint透明度
     */
    private var alpha = 0
    private var mPaddingLeft = 0
    private var mPaddingTop = 0
    private var mPaddingRight = 0
    private var mPaddingBottom = 0
    private var mHintView: View? = null
    private var timer: Timer? = null
    private var mHintViewDelegate: HintViewDelegate? = null
    private var mMarginLeft:Int=0
    private var mMarginTop:Int=0
    private var mMarginRight:Int=0
    private var mMarginBottom:Int=0
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class HintMode {
        companion object {
            var COLOR_POINT_HINT = 0
            var TEXT_HINT = 1
        }
    }

    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class HintGravity {
        companion object {
            var LEFT = 1
            var CENTER = 2
            var RIGHT = 3
        }
    }

    /**
     * 读取提示形式  和   提示位置   和    播放延迟
     */
    private fun initView(attrs: AttributeSet?) { //这里要做移除，有时在使用中没有手动销毁，所以初始化时要remove
        if (mViewPager != null) {
            removeView(mViewPager)
        }
        mHintViewDelegate =  object : HintViewDelegate {
            override fun setCurrentPosition(position: Int, hintView: BaseHintView?) {
                hintView?.setCurrent(position)
            }

            override fun initView(length: Int, gravity: Int, hintView: BaseHintView?, padding: Rect, margin:Rect) {
                hintView?.initView(length, gravity,padding,margin)
            }
        }

        //初始化自定义属性
        val type = context.obtainStyledAttributes(attrs, R.styleable.CustomBannerView)
        hintMode = type.getInteger(R.styleable.CustomBannerView_hint_mode, 0)
        mGravity = type.getInteger(R.styleable.CustomBannerView_hint_gravity, 1)
        delay = type.getInt(R.styleable.CustomBannerView_play_delay, 0)
        color = type.getColor(R.styleable.CustomBannerView_hint_color, Color.BLACK)
        alpha = type.getInt(R.styleable.CustomBannerView_hint_alpha, 0)
        mPaddingLeft = type.getDimension(R.styleable.CustomBannerView_hint_paddingLeft, 0f).toInt()
        mPaddingRight = type.getDimension(R.styleable.CustomBannerView_hint_paddingRight, 0f).toInt()
        mPaddingTop = type.getDimension(R.styleable.CustomBannerView_hint_paddingTop, 0f).toInt()
        mPaddingBottom = type.getDimension(R.styleable.CustomBannerView_hint_paddingBottom, dip2px(context, 4f).toFloat()).toInt()
        mMarginLeft = type.getDimension(R.styleable.CustomBannerView_hint_marginLeft, 0f).toInt()
        mMarginTop = type.getDimension(R.styleable.CustomBannerView_hint_marginTop, 0f).toInt()
        mMarginRight = type.getDimension(R.styleable.CustomBannerView_hint_marginRight, 0f).toInt()
        mMarginBottom = type.getDimension(R.styleable.CustomBannerView_hint_marginBottom, 0f).toInt()
        type.recycle()

        mViewPager = BannerViewPager(context)
        mViewPager!!.id = R.id.banner_inner
        mViewPager!!.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        addView(mViewPager)
        when (hintMode) {
            HintMode.COLOR_POINT_HINT -> {
                initHint(
                    ColorPointHintView(
                        context, Color.parseColor("#E3AC42"),
                        Color.parseColor("#88ffffff")
                    )
                )
            }
            HintMode.TEXT_HINT -> {
                initHint(TextHintView(context))
            }
            else -> {
                initHint(
                    ColorPointHintView(
                        context, Color.parseColor("#E3AC42")
                        ,
                        Color.parseColor("#88ffffff")
                    )
                )
            }
        }
        initGestureDetector()
    }

    private fun initGestureDetector() { //手势处理
        mGestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    if (mOnItemClickListener != null) {
                        if (mAdapter is LoopPagerAdapter) {
                            val count = (mAdapter as LoopPagerAdapter).realCount
                            val i = mViewPager!!.currentItem % count
                            Log.d(TAG, count.toString() + "---" + i + "-----" + mViewPager!!.currentItem)
                            mOnItemClickListener!!.onItemClick(i)
                        } else {
                            mOnItemClickListener!!.onItemClick(mViewPager!!.currentItem)
                        }
                    }
                    return super.onSingleTapUp(e)
                }
            })
    }

    /**
     * 计算所有ChildView的宽度和高度 然后根据ChildView的计算结果，设置自己的宽和高
     * 11月18日，当不设置轮播图布局具体宽高的时候，则轮播图全屏展示
     * 需求：1.当即使不设置具体宽高值，就展现默认值宽高，默认宽是MATCH_PARENT，高是200dp
     * 2.能够根据图片自动展示轮播图宽高。这个认为没必要
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        //获得此ViewGroup上级容器为其推荐的宽和高，以及计算模式
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        // 计算出所有的childView的宽和高
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        //记录如果是wrap_content是设置的宽和高
        val width: Int
        val height: Int
        val childView = getChildAt(0)
        val cWidth = childView.measuredWidth
        val cHeight = childView.measuredHeight
        val cParams = childView.layoutParams as MarginLayoutParams
        //设置位置
        cParams.setMargins(0, 0, 0, 0)
        /*
         * 如果是wrap_content设置为我们计算的值
         * 否则：直接设置为父容器计算的值
         */width = if (widthMode == MeasureSpec.EXACTLY) {
            cWidth
        } else {
            LinearLayout.LayoutParams.MATCH_PARENT
        }
        height = if (heightMode == MeasureSpec.EXACTLY) {
            cHeight
        } else {
            dip2px(context, 200f)
        }
        setMeasuredDimension(width, height)
    }

    /**
     * 用静态内部类来防止持有外部类的隐性引用，避免之前总是内存泄漏
     * https://github.com/yangchong211
     */
    private val mHandler = TimeTaskHandler(this)

    private class TimeTaskHandler internal constructor(rollPagerView: CustomBannerView) :
        Handler() {
        private val mRollPagerView: WeakReference<CustomBannerView> = WeakReference(rollPagerView)
        override fun handleMessage(msg: Message) {
            val rollPagerView = mRollPagerView.get()
            if (rollPagerView != null) {
                val currentItem = rollPagerView.getViewPager()!!.currentItem
                Log.d(TAG, "$currentItem---")
                var cur = currentItem + 1
                if (cur >= rollPagerView.mAdapter!!.count) {
                    cur = 0
                }
                rollPagerView.getViewPager()!!.currentItem = cur
                rollPagerView.mHintViewDelegate?.setCurrentPosition(cur, rollPagerView.mHintView as BaseHintView?)
                //假如说轮播图只有一张，那么就停止轮播
                val count = rollPagerView.mAdapter!!.count
                Log.d(TAG, "$count---")
                if (rollPagerView.mAdapter!!.count <= 1) {
                    rollPagerView.stopPlay()
                }
            }
        }

    }

    private class WeakTimerTask internal constructor(mRollPagerView: CustomBannerView) :
        TimerTask() {
        private val mRollPagerView: WeakReference<CustomBannerView> = WeakReference(mRollPagerView)
        override fun run() {
            val rollPagerView = mRollPagerView.get()
            if (rollPagerView != null) {
                val count = rollPagerView.mAdapter!!.count
                //假如说轮播图只有一张，那么就停止轮播
                Log.d(TAG, "$count---")
                if (rollPagerView.isShown && System.currentTimeMillis() - rollPagerView.mRecentTouchTime > rollPagerView.delay) {
                    rollPagerView.mHandler.sendEmptyMessage(0)
                }
            } else {
                cancel()
            }
        }

    }

    /**
     * 开始播放
     * 仅当view正在显示 且 触摸等待时间过后 播放
     */
    private fun startPlay() {
        if (delay <= 0 || mAdapter == null || mAdapter!!.count <= 1) {
            return
        }
        if (timer != null) {
            timer!!.cancel()
        }
        timer = Timer()
        //用一个timer定时设置当前项为下一项
        timer!!.schedule(WeakTimerTask(this), delay.toLong(), delay.toLong())
    }

    /**
     * 停止轮播
     */
    private fun stopPlay() {
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
    }

    fun setHintViewDelegate(delegate: HintViewDelegate) {
        mHintViewDelegate = delegate
    }

    /**
     * 初始化轮播图指示器
     * @param hintView          hintView
     */
    private fun initHint(hintView: BaseHintView?) {
        if (mHintView != null) {
            removeView(mHintView)
        }
        if (hintView == null) {
            return
        }
        mHintView = hintView as View
        loadHintView()
    }

    /**
     * 加载hintView的容器
     */
    private fun loadHintView() {
        addView(mHintView)
        val lp = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )

        lp.addRule(ALIGN_PARENT_BOTTOM )
        mHintView!!.layoutParams = lp
        val gd = GradientDrawable()
        gd.setColor(color)
        gd.alpha = alpha
        mHintView!!.setBackgroundDrawable(gd)
        mHintViewDelegate?.initView(
            if (mAdapter == null) 0 else mAdapter!!.count,
            mGravity, mHintView as BaseHintView?,
            getPaddingRect(),
            getMarginRect()
        )
    }

    private fun getPaddingRect():Rect{
        return Rect(mPaddingLeft,mPaddingTop, mPaddingRight, mPaddingBottom)
    }
    private fun getMarginRect():Rect{
        return Rect(mMarginLeft,mMarginTop, mMarginRight, mMarginBottom)
    }
    /**
     * 设置viewPager滑动动画持续时间
     * API>19
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun setAnimationDuration(during: Int) {
        try { // viewPager平移动画事件
            val mField = ViewPager::class.java.getDeclaredField("mScroller")
            mField.isAccessible = true
            // 动画效果与ViewPager的一致
            val interpolator =
                Interpolator { t ->
                    var t = t
                    t -= 1.0f
                    t * t * t * t * t + 1.0f
                }
            val mScroller: Scroller = object : Scroller(context, interpolator) {
                override fun startScroll(
                    startX: Int,
                    startY: Int,
                    dx: Int,
                    dy: Int,
                    duration: Int
                ) { // 如果手工滚动,则加速滚动
                    var duration = duration
                    if (System.currentTimeMillis() - mRecentTouchTime > delay) {
                        duration = during
                    } else {
                        duration /= 2
                    }
                    super.startScroll(startX, startY, dx, dy, duration)
                }

                override fun startScroll(
                    startX: Int,
                    startY: Int,
                    dx: Int,
                    dy: Int
                ) {
                    super.startScroll(startX, startY, dx, dy, during)
                }
            }
            mField[mViewPager] = mScroller
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    /**
     * 停止轮播
     * 在onPause中调用
     */
    fun pause() {
        stopPlay()
    }

    /**
     * 开始轮播
     * 在onResume中调用
     */
    fun resume() {
        startPlay()
    }

    /**
     * 判断轮播是否进行
     */
    val isPlaying: Boolean get() = timer != null

    /**
     * 取真正的Viewpager
     */
    fun getViewPager(): ViewPager? {
        return mViewPager
    }

    /**
     * 设置Adapter
     */
    fun setAdapter(adapter: PagerAdapter) {
        adapter.registerDataSetObserver(PagerObserver())
        mViewPager!!.adapter = adapter
        mViewPager!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                if (position >= 0) {
                    mHintViewDelegate?.setCurrentPosition(position, mHintView as BaseHintView?)
                    if (mOnPageListener != null) {
                        mOnPageListener!!.onPageChange(position)
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        mAdapter = adapter
        dataSetChanged()
    }

    /**
     * 用来实现adapter的notifyDataSetChanged通知HintView变化
     */
    private inner class PagerObserver : DataSetObserver() {
        override fun onChanged() {
            dataSetChanged()
        }

        override fun onInvalidated() {
            dataSetChanged()
        }
    }

    private fun dataSetChanged() {
        if (mHintView != null) {
            mHintViewDelegate?.initView(mAdapter!!.count, mGravity, mHintView as BaseHintView?,
                getPaddingRect(),getMarginRect())
            mHintViewDelegate?.setCurrentPosition(
                mViewPager!!.currentItem,
                mHintView as BaseHintView?
            )
        }
        startPlay()
    }

    /**
     * 为了实现触摸时和过后一定时间内不滑动,这里拦截
     */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        mRecentTouchTime = System.currentTimeMillis()
        mGestureDetector!!.onTouchEvent(ev)
        /*int action = ev.getAction();
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL
                || action == MotionEvent.ACTION_OUTSIDE) {
            startPlay();
        } else if (action == MotionEvent.ACTION_DOWN) {
            stopPlay();
        }*/return super.dispatchTouchEvent(ev)
    }

    /**
     * 设置轮播时间
     */
    fun setPlayDelay(delay: Int) {
        this.delay = delay
        startPlay()
    }

    /**
     * 设置颜色
     * @param c             色值
     */
    fun setHintColor(@ColorInt c: Int) {
        color = c
        mHintView!!.setBackgroundColor(c)
    }

    /**
     * 设置位置
     * @param g             位置
     */
    fun setHintGravity(@HintGravity g: Int) {
        mGravity = g
        //loadHintView();
        mHintViewDelegate?.initView(
            if (mAdapter == null) 0 else mAdapter!!.count,
            mGravity, mHintView as BaseHintView?,
            getPaddingRect(),
            getMarginRect()
        )
    }

    /**
     * 设置指示器样式
     * @param mode          样式：文字/红点
     */
    fun setHintMode(@HintMode mode: Int) {
        hintMode = mode
    }

    /**
     * 设置提示view的透明度
     * @param alpha 0为全透明  255为实心
     */
    fun setHintAlpha(alpha: Int) {
        this.alpha = alpha
        initHint(mHintView as BaseHintView?)
    }

    /**
     * 支持自定义hintView
     * 只需new一个实现HintView的View传进来
     * 会自动将你的view添加到本View里面。重新设置LayoutParams。
     */
    fun setHintView(hintView: BaseHintView?) {
        if (mHintView != null) {
            removeView(mHintView)
        }
        mHintView = hintView as View?
        hintView?.let { initHint(it) }
    }

    private var mOnPageListener: OnPageListener? = null

    interface OnPageListener {
        /**
         * 滑动监听
         * @param position          索引
         */
        fun onPageChange(position: Int)
    }

    /**
     * 轮播图滑动事件
     */
    fun setOnPageListener(listener: OnPageListener?) {
        mOnPageListener = listener
    }

    private var mOnItemClickListener: OnBannerClickListener? = null

    interface OnBannerClickListener {
        /**
         * 点击
         * @param position          索引
         */
        fun onItemClick(position: Int)
    }

    /**
     * 轮播图点击事件
     */
    fun setOnBannerClickListener(listener: OnBannerClickListener?) {
        mOnItemClickListener = listener
    }


}
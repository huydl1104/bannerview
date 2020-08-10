package com.ydl.bannerlib.gallery

import android.content.Context
import android.os.Handler
import android.os.Message
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.Nullable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.lang.ref.WeakReference
import java.util.*

class GalleryRecyclerView @JvmOverloads constructor(
    context: Context?, @Nullable attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RecyclerView(context!!, attrs, defStyle) {
    /**
     * 播放延迟
     */
    private var delay = 0
    /**
     * 触摸轮播图时间戳
     */
    private var mRecentTouchTime: Long = 0
    /**
     * timer
     */
    private var timer: Timer? = null
    /**
     * 轮播图数量
     */
    private var size = 0
    /**
     * 滑动速度
     */
    private var mFlingSpeed = FLING_MAX_VELOCITY
    private var mAdapter: Adapter<*>? = null
    private var mCallbackInFling = false
    private var mSnapHelper: GalleryLinearSnapHelper? = null

    override fun onSaveInstanceState(): Parcelable? { //异常情况保存重要信息。暂不操作
        return super.onSaveInstanceState()
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
        // 轮播图用在fragment中，如果是横竖屏切换（Fragment销毁），不应该走smoothScrollToPosition(0)
        // 因为这个方法会导致ScrollManager的onHorizontalScroll不断执行，而ScrollManager.mConsumeX已经重置，会导致这个值紊乱
        // 而如果走scrollToPosition(0)方法，则不会导致ScrollManager的onHorizontalScroll执行，
        // 所以ScrollManager.mConsumeX这个值不会错误
        // 从索引0处开始轮播
        smoothScrollToPosition(0)
        // 但是因为不走ScrollManager的 onHorizontalScroll ，所以不会执行切换动画，
        // 所以就调用smoothScrollBy(int dx, int dy)，让item轻微滑动，触发动画
        smoothScrollBy(10, 0)
        smoothScrollBy(0, 0)
        startPlay()
    }

    override fun fling(velocityX: Int, velocityY: Int): Boolean {
        var velocityX = velocityX
        var velocityY = velocityY
        velocityX = balanceVelocity(velocityX)
        velocityY = balanceVelocity(velocityY)
        return super.fling(velocityX, velocityY)
    }

    /**
     * 返回滑动速度值
     */
    private fun balanceVelocity(velocity: Int): Int {
        return if (velocity > 0) {
            Math.min(velocity, mFlingSpeed)
        } else {
            Math.max(velocity, -mFlingSpeed)
        }
    }

    /**
     * 开始播放
     * 仅当view正在显示 且 触摸等待时间过后 播放
     */
    private fun startPlay() {
        if (delay <= 0 || size <= 1) {
            return
        }
        if (timer != null) {
            timer!!.cancel()
        }
        timer = Timer()
        //用一个timer定时设置当前项为下一项
        timer!!.schedule(
            WeakTimerTask(this),
            delay.toLong(),
            delay.toLong()
        )
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

    /**
     * 停止轮播
     * 在onPause中调用
     */
    fun onStop() {
        stopPlay()
    }

    /**
     * 开始轮播
     * 在onResume中调用
     */
    fun onStart() {
        startPlay()
    }

    /**
     * 判断轮播是否进行
     */
    val isPlaying: Boolean get() = timer != null

    fun release() {
        stopPlay()
        if (mHandler != null) {
            mHandler!!.removeCallbacksAndMessages(null)
            mHandler = null
        }
        clearOnScrollListeners()
    }

    /**
     * 用静态内部类来防止持有外部类的隐性引用，避免之前总是内存泄漏
     * https://github.com/yangchong211
     */
    private var mHandler: TimeTaskHandler? = TimeTaskHandler(this)

    private class TimeTaskHandler internal constructor(rollPagerView: GalleryRecyclerView) :
        Handler() {
        private val mGalleryRecyclerView: WeakReference<GalleryRecyclerView> = WeakReference(rollPagerView)
        override fun handleMessage(msg: Message) {
            val recyclerView = mGalleryRecyclerView.get()
            //注意这个地方需要添加非空判断
            if (recyclerView != null) {
                //如果cur大于或等于轮播图数量，那么播放到最后一张后时，接着轮播便是索引为0的图片
                //int cur = GalleryLayoutManager.getPosition()+1;
                val cur = mSelectedPosition++
                val currentItem = recyclerView.getCurrentItem()
                //Log.e("handleMessage----------",cur+"---" + recyclerView.size);
                Log.e("handleMessage----", "$cur----$currentItem")
                recyclerView.smoothScrollToPosition(cur)
                //假如说轮播图只有一张，那么就停止轮播
                if (recyclerView.size <= 1) {
                    recyclerView.stopPlay()
                }
            }
        }

    }

    private class WeakTimerTask internal constructor(recyclerView: GalleryRecyclerView) :
        TimerTask() {
        private val mGalleryRecyclerView: WeakReference<GalleryRecyclerView> = WeakReference(recyclerView)
        override fun run() {
            val recyclerView = mGalleryRecyclerView.get()
            if (recyclerView != null) {
                if (recyclerView.isShown && System.currentTimeMillis() - recyclerView.mRecentTouchTime > recyclerView.delay) {
                    recyclerView.mHandler!!.sendEmptyMessage(0)
                }
            } else {
                cancel()
            }
        }

    }

    /**
     * 为了实现触摸时和过后一定时间内不滑动,这里拦截
     */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        mRecentTouchTime = System.currentTimeMillis()
        val action = ev.action
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_OUTSIDE) {
            startPlay()
        } else if (action == MotionEvent.ACTION_DOWN) {
            mSelectedPosition = getCurrentItem() + 2
            Log.e("handleMessage----", "----$mSelectedPosition")
            stopPlay()
        }
        return super.dispatchTouchEvent(ev)
    }

    /**
     * 设置滑动速度（像素/s）
     * 建议设置8000到15000之间，不要设置太小。
     */
    fun setFlingSpeed(speed: Int): GalleryRecyclerView {
        mFlingSpeed = speed
        return this
    }

    fun setDataAdapter(adapter: Adapter<*>?): GalleryRecyclerView {
        this.adapter = adapter
        return this
    }

    /**
     * 播放间隔时间 ms
     */
    fun setDelayTime(interval: Int): GalleryRecyclerView {
        delay = interval
        return this
    }

    /**
     * 轮播图数量
     */
    fun setSize(size: Int): GalleryRecyclerView {
        this.size = size
        return this
    }

    /**
     * 设置是否
     */
    fun setCallbackInFling(callbackInFling: Boolean): GalleryRecyclerView {
        mCallbackInFling = callbackInFling
        return this
    }

    fun setSelectedPosition(position: Int): GalleryRecyclerView {
        mSelectedPosition = position
        return this
    }

    fun setOnItemSelectedListener(onItemSelectedListener: OnItemSelectedListener): GalleryRecyclerView {
        mOnItemSelectedListener = onItemSelectedListener
        return this
    }

    /**
     * 装载
     * 注意要点：recyclerView轮播要是无限轮播，必须设置两点
     * 第一处是getItemCount() 返回的是Integer.MAX_VALUE。这是因为广告轮播图是无限轮播，getItemCount()
     * 返回的是Adapter中的总项目数，这样才能使RecyclerView能一直滚动。
     *
     * 第二处是onBindViewHolder()中的 position%list.size() ，表示position对图片列表list取余，
     * 这样list.get(position%list.size())才能按顺序循环展示图片。
     */
    fun setUp() {
        setAdapter(adapter)
        if (getAdapter()!!.itemCount <= 0) {
            return
        }
        val manager = GalleryLayoutManager(context, LinearLayoutManager.HORIZONTAL)
        //attach，绑定recyclerView，并且设置默认选中索引的位置
        manager.attach(mSelectedPosition)
        //设置缩放比例因子，在0到1.0之间即可
        manager.setItemTransformer(GalleryScaleTransformer(0.2f, 20))
        layoutManager = manager
        mSnapHelper = GalleryLinearSnapHelper(this)
        mSnapHelper?.attachToRecyclerView(this)
        addOnScrollListener(InnerScrollListener())
        startPlay()
    }

    fun getCurrentItem(): Int{
        val layoutManager = this.layoutManager
        return if (mSnapHelper != null) {
            val snapView: View = mSnapHelper?.findSnapView(layoutManager)!!
            if (snapView != null) {
                layoutManager!!.getPosition(snapView)
            } else {
                mSelectedPosition
            }
        } else {
            mSelectedPosition
        }
    }


    private inner class InnerScrollListener : OnScrollListener() {
        var mState = 0
        var mCallbackOnIdle = false
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val snap: View = mSnapHelper?.findSnapView(recyclerView.layoutManager)!!
            if (snap != null) {
                val selectedPosition = recyclerView.layoutManager!!.getPosition(snap)
                if (selectedPosition != GalleryLayoutManager.Companion.mPosition) {
                    GalleryLayoutManager.mPosition = selectedPosition
                    if (!mCallbackInFling && mState != SCROLL_STATE_IDLE) {
                        mCallbackOnIdle = true
                        return
                    }
                    if (mOnItemSelectedListener != null) {
                        mOnItemSelectedListener!!.onItemSelected(recyclerView, snap, GalleryLayoutManager.Companion.mPosition)
                    }
                }
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            mState = newState
            if (mState == SCROLL_STATE_IDLE) {
                val snap: View = mSnapHelper?.findSnapView(recyclerView.layoutManager)!!
                if (snap != null) {
                    val selectedPosition = recyclerView.layoutManager!!.getPosition(snap)
                    if (selectedPosition != GalleryLayoutManager.Companion.mPosition) {
                        GalleryLayoutManager.Companion.mPosition = (selectedPosition)
                        if (mOnItemSelectedListener != null) {
                            mOnItemSelectedListener!!.onItemSelected(recyclerView, snap, GalleryLayoutManager.Companion.mPosition)
                        }
                    } else if (!mCallbackInFling && mOnItemSelectedListener != null && mCallbackOnIdle) {
                        mCallbackOnIdle = false
                        mOnItemSelectedListener!!.onItemSelected(recyclerView, snap, GalleryLayoutManager.Companion.mPosition)
                    }
                }
            }
        }
    }

    private var mOnItemSelectedListener: OnItemSelectedListener? = null

    interface OnItemSelectedListener {
        fun onItemSelected(recyclerView: RecyclerView?, item: View?, position: Int)
    }

    companion object {
        private var mSelectedPosition = 0
        private const val FLING_MAX_VELOCITY = 8000 // 最大顺时滑动速度
    }
}
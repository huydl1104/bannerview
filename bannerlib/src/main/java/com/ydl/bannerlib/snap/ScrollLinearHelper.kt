package com.ydl.bannerlib.snap

import android.util.DisplayMetrics
import android.view.View
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.RecyclerView.SmoothScroller.ScrollVectorProvider

class ScrollLinearHelper : LinearSnapHelper() {

    companion object {
        private const val MILLISECONDS_PER_INCH = 40f
    }

    private var mHorizontalHelper: OrientationHelper? = null
    private var mVerticalHelper: OrientationHelper? = null
    private var mRecyclerView: RecyclerView? = null

    @Throws(IllegalStateException::class)
    override fun attachToRecyclerView(recyclerView: RecyclerView?) {
        mRecyclerView = recyclerView
        super.attachToRecyclerView(recyclerView)
    }

    override fun calculateDistanceToFinalSnap(layoutManager: RecyclerView.LayoutManager, targetView: View): IntArray? {
        val out = IntArray(2)
        if (layoutManager.canScrollHorizontally()) {
            out[0] = distanceToStart(targetView, getHorizontalHelper(layoutManager))
        } else {
            out[0] = 0
        }
        if (layoutManager.canScrollVertically()) {
            out[1] = distanceToStart(targetView, getVerticalHelper(layoutManager))
        } else {
            out[1] = 0
        }
        return out
    }

    private fun distanceToStart(targetView: View, helper: OrientationHelper): Int {
        return helper.getDecoratedStart(targetView) - helper.startAfterPadding
    }

    override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
        return if (layoutManager is LinearLayoutManager) {
            if (layoutManager.canScrollHorizontally()) {
                findStartView(layoutManager, getHorizontalHelper(layoutManager))
            } else {
                findStartView(layoutManager, getVerticalHelper(layoutManager))
            }
        } else super.findSnapView(layoutManager)
    }

    private fun findStartView(layoutManager: RecyclerView.LayoutManager, helper: OrientationHelper): View? {
        if (layoutManager is LinearLayoutManager) {
            val firstChild = layoutManager.findFirstVisibleItemPosition()
            //需要判断是否是最后一个Item，如果是最后一个则不让对齐，以免出现最后一个显示不完全。
            val isLastItem = (layoutManager.findLastCompletelyVisibleItemPosition() == layoutManager.getItemCount() - 1)
            if (firstChild == RecyclerView.NO_POSITION || isLastItem) {
                return null
            }
            val child = layoutManager.findViewByPosition(firstChild)
            return if (helper.getDecoratedEnd(child) >= helper.getDecoratedMeasurement(child) / 2 && helper.getDecoratedEnd(child) > 0) {
                        child
                   } else {
                        if (layoutManager.findLastCompletelyVisibleItemPosition() == layoutManager.getItemCount() - 1){
                            null
                        }else {
                            layoutManager.findViewByPosition(firstChild + 1)
                        }
                   }
        }
        return super.findSnapView(layoutManager)
    }

    override fun createSnapScroller(layoutManager: RecyclerView.LayoutManager): LinearSmoothScroller? {
        return if (layoutManager !is ScrollVectorProvider) {
            null
        } else object : LinearSmoothScroller(mRecyclerView!!.context) {
            override fun onTargetFound(
                targetView: View,
                state: RecyclerView.State,
                action: Action
            ) {
                val snapDistances = calculateDistanceToFinalSnap(mRecyclerView!!.layoutManager!!, targetView)
                val dx: Int
                val dy: Int
                if (snapDistances != null) {
                    dx = snapDistances[0]
                    dy = snapDistances[1]
                    val time = calculateTimeForDeceleration(
                        Math.max(
                            Math.abs(dx),
                            Math.abs(dy)
                        )
                    )
                    if (time > 0) {
                        action.update(dx, dy, time, mDecelerateInterpolator)
                    }
                }
            }

            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi//这个地方可以自己设置
            }
        }
    }

    private fun getVerticalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper {
        if (mVerticalHelper == null) {
            mVerticalHelper = OrientationHelper.createVerticalHelper(layoutManager)
        }
        return mVerticalHelper!!
    }

    private fun getHorizontalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper {
        if (mHorizontalHelper == null) {
            mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager)
        }
        return mHorizontalHelper!!
    }


}
package com.ydl.bannerlib.gallery

import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import androidx.annotation.Nullable
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SmoothScroller.ScrollVectorProvider


class GalleryLinearSnapHelper(@param:Nullable private val mRecyclerView: RecyclerView) : LinearSnapHelper() {
    private var mHorizontalHelper: OrientationHelper? = null
    @Nullable
    override fun createSnapScroller(layoutManager: RecyclerView.LayoutManager): LinearSmoothScroller? {
        return if (layoutManager !is ScrollVectorProvider) {
            null
        } else object : LinearSmoothScroller(mRecyclerView.context) {
            override fun onTargetFound(
                targetView: View,
                state: RecyclerView.State,
                action: RecyclerView.SmoothScroller.Action
            ) {
                val snapDistances: IntArray = calculateDistanceToFinalSnap(mRecyclerView.layoutManager!!, targetView)!!
                val dx: Int
                val dy: Int
                if (snapDistances != null) {
                    dx = snapDistances[0]
                    dy = snapDistances[1]
                    val time: Int = calculateTimeForDeceleration(
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

            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float { //这个地方可以自己设置
                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi
            }

            override fun calculateTimeForScrolling(dx: Int): Int {
                return Math.min(
                    MAX_SCROLL_ON_FLING_DURATION,
                    super.calculateTimeForScrolling(dx)
                )
            }
        }
    }

    /**
     * 提供一个用于对齐的Adapter 目标position,抽象方法，需要子类自己实现
     * 发现滚动时候，会滑动多个item，如果相对item个数做限制，可以在findTargetSnapPosition()方法中处理。
     * @param layoutManager                 layoutManager
     * @param velocityX                     velocityX
     * @param velocityY                     velocityY
     * @return
     */
    override fun findTargetSnapPosition(
        layoutManager: RecyclerView.LayoutManager,
        velocityX: Int,
        velocityY: Int
    ): Int {
        if (layoutManager !is ScrollVectorProvider) {
            return RecyclerView.NO_POSITION
        }
        val itemCount = layoutManager.itemCount
        if (itemCount == 0) {
            return RecyclerView.NO_POSITION
        }
        val currentView: View = findSnapView(layoutManager) ?: return RecyclerView.NO_POSITION
        val currentPosition = layoutManager.getPosition(currentView)
        if (currentPosition == RecyclerView.NO_POSITION) {
            return RecyclerView.NO_POSITION
        }
        val vectorProvider = layoutManager as ScrollVectorProvider
        val vectorForEnd = vectorProvider.computeScrollVectorForPosition(itemCount - 1)
            ?: return RecyclerView.NO_POSITION
        //在松手之后,列表最多只能滚多一屏的item数
        val deltaThreshold = layoutManager.width / getHorizontalHelper(layoutManager)!!.getDecoratedMeasurement(currentView)
        Log.d("GalleryLinearSnapHelper", "---deltaThreshold---$deltaThreshold")
        var hDeltaJump: Int
        if (layoutManager.canScrollHorizontally()) {
            hDeltaJump = estimateNextPositionDiffForFling(
                layoutManager,
                getHorizontalHelper(layoutManager),
                velocityX,
                0
            )
            if (hDeltaJump > deltaThreshold) {
                hDeltaJump = deltaThreshold
            }
            if (hDeltaJump < -deltaThreshold) {
                hDeltaJump = -deltaThreshold
            }
            if (vectorForEnd.x < 0) {
                hDeltaJump = -hDeltaJump
            }
            Log.d("GalleryLinearSnapHelper", "+++-hDeltaJump-+++$hDeltaJump")
        } else {
            hDeltaJump = 0
        }
        if (hDeltaJump == 0) {
            return RecyclerView.NO_POSITION
        }
        Log.d("GalleryLinearSnapHelper", "---hDeltaJump---$hDeltaJump")
        var targetPos = currentPosition + hDeltaJump
        if (targetPos < 0) {
            targetPos = 0
        }
        Log.d("GalleryLinearSnapHelper", "+++targetPos+++$targetPos")
        if (targetPos >= itemCount) {
            targetPos = itemCount - 1
        }
        Log.d("GalleryLinearSnapHelper", "---targetPos---$targetPos")
        return targetPos
    }

    private fun estimateNextPositionDiffForFling(
        layoutManager: RecyclerView.LayoutManager,
        helper: OrientationHelper?, velocityX: Int, velocityY: Int
    ): Int {
        val distances: IntArray = calculateScrollDistance(velocityX, velocityY)
        val distancePerChild = computeDistancePerChild(layoutManager, helper)
        if (distancePerChild <= 0) {
            return 0
        }
        val distance = distances[0]
        return if (distance > 0) {
            Math.floor(distance / distancePerChild.toDouble()).toInt()
        } else {
            Math.ceil(distance / distancePerChild.toDouble()).toInt()
        }
    }

    private fun computeDistancePerChild(
        layoutManager: RecyclerView.LayoutManager,
        helper: OrientationHelper?
    ): Float {
        var minPosView: View? = null
        var maxPosView: View? = null
        var minPos = Int.MAX_VALUE
        var maxPos = Int.MIN_VALUE
        val childCount = layoutManager.childCount
        if (childCount == 0) {
            return INVALID_DISTANCE
        }
        for (i in 0 until childCount) {
            val child = layoutManager.getChildAt(i)
            val pos = layoutManager.getPosition(child!!)
            if (pos == RecyclerView.NO_POSITION) {
                continue
            }
            if (pos < minPos) {
                minPos = pos
                minPosView = child
            }
            if (pos > maxPos) {
                maxPos = pos
                maxPosView = child
            }
        }
        if (minPosView == null || maxPosView == null) {
            return INVALID_DISTANCE
        }
        val start = Math.min(
            helper!!.getDecoratedStart(minPosView),
            helper.getDecoratedStart(maxPosView)
        )
        val end = Math.max(
            helper.getDecoratedEnd(minPosView),
            helper.getDecoratedEnd(maxPosView)
        )
        val distance = end - start
        return if (distance == 0) {
            INVALID_DISTANCE
        } else 1f * distance / (maxPos - minPos + 1)
    }

    private fun getHorizontalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper? {
        if (mHorizontalHelper == null) {
            mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager)
        }
        return mHorizontalHelper
    }

    companion object {
        private const val INVALID_DISTANCE = 1f
        private const val MILLISECONDS_PER_INCH = 100.0f
        private const val MAX_SCROLL_ON_FLING_DURATION = 100 // ms
    }

}
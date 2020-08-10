package com.ydl.bannerlib.gallery

import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView


class GallerySmoothScroller internal constructor(context: Context?) :
    LinearSmoothScroller(context) {

    companion object {
        private const val MILLISECONDS_PER_INCH = 100f
    }

     override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float { // 返回：滑过1px时经历的时间(ms)。
        //return MILLISECONDS_PER_INCH / displayMetrics.density;
        return MILLISECONDS_PER_INCH / displayMetrics.densityDpi
        //返回滑动一个pixel需要多少毫秒
    }

    override fun onTargetFound(targetView: View, state: RecyclerView.State, action: Action) {
        val dx = calculateDxToMakeCentral(targetView)
        val dy = calculateDyToMakeCentral(targetView)
        val distance = Math.sqrt(dx * dx + dy * dy.toDouble()).toInt()
        val time: Int = calculateTimeForDeceleration(distance)
        if (time > 0) {
            action.update(-dx, -dy, time, mDecelerateInterpolator)
        }
    }

    private fun calculateDxToMakeCentral(view: View): Int {
        val layoutManager: RecyclerView.LayoutManager = layoutManager!!
        if (layoutManager == null || !layoutManager.canScrollHorizontally()) {
            return 0
        }
        val params = view.layoutParams as RecyclerView.LayoutParams
        val left = layoutManager.getDecoratedLeft(view) - params.leftMargin
        val right = layoutManager.getDecoratedRight(view) + params.rightMargin
        val start = layoutManager.paddingLeft
        val end = layoutManager.width - layoutManager.paddingRight
        val childCenter = left + ((right - left) / 2.0f).toInt()
        val containerCenter = ((end - start) / 2f).toInt()
        return containerCenter - childCenter
    }

    private fun calculateDyToMakeCentral(view: View): Int {
        val layoutManager: RecyclerView.LayoutManager = layoutManager!!
        if (layoutManager == null || !layoutManager.canScrollVertically()) {
            return 0
        }
        val params = view.layoutParams as RecyclerView.LayoutParams
        val top = layoutManager.getDecoratedTop(view) - params.topMargin
        val bottom = layoutManager.getDecoratedBottom(view) + params.bottomMargin
        val start = layoutManager.paddingTop
        val end = layoutManager.height - layoutManager.paddingBottom
        val childCenter = top + ((bottom - top) / 2.0f).toInt()
        val containerCenter = ((end - start) / 2f).toInt()
        return containerCenter - childCenter
    }


}
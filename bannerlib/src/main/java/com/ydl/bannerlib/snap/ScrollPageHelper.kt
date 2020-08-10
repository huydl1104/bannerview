package com.ydl.bannerlib.snap

import android.os.Build
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import androidx.recyclerview.widget.*
import java.util.*

class ScrollPageHelper(private val gravity: Int, private val snapLastItem: Boolean) : PagerSnapHelper() {

    private var mHorizontalHelper: OrientationHelper? = null
    private var mVerticalHelper: OrientationHelper? = null
    private var isRtlHorizontal = false

    @Throws(IllegalStateException::class)
    override fun attachToRecyclerView(recyclerView: RecyclerView?) {
        check(!(recyclerView != null && (recyclerView.layoutManager !is LinearLayoutManager
                    || recyclerView.layoutManager is GridLayoutManager))) {
            "ScrollPageHelper needs a RecyclerView with a LinearLayoutManager"
        }
        if (recyclerView != null) {
            recyclerView.onFlingListener = null
            if (gravity == Gravity.START || gravity == Gravity.END) {
                isRtlHorizontal = isRtl
            }
        }
        super.attachToRecyclerView(recyclerView)
    }

    override fun calculateDistanceToFinalSnap(layoutManager: RecyclerView.LayoutManager, targetView: View): IntArray? {
        val out = IntArray(2)
        if (layoutManager.canScrollHorizontally()) {
            if (gravity == Gravity.START) {
                out[0] = distanceToStart(targetView, getHorizontalHelper(layoutManager), false)
            } else { // END
                out[0] = distanceToEnd(targetView, getHorizontalHelper(layoutManager), false)
            }
        } else {
            out[0] = 0
        }
        if (layoutManager.canScrollVertically()) {
            if (gravity == Gravity.TOP) {
                out[1] = distanceToStart(targetView, getVerticalHelper(layoutManager), false)
            } else { // BOTTOM
                out[1] = distanceToEnd(targetView, getVerticalHelper(layoutManager), false)
            }
        } else {
            out[1] = 0
        }
        return out
    }

    override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
        var snapView: View? = null
        if (layoutManager is LinearLayoutManager) {
            when (gravity) {
                Gravity.START -> snapView = findStartView(layoutManager, getHorizontalHelper(layoutManager))
                Gravity.END -> snapView = findEndView(layoutManager, getHorizontalHelper(layoutManager))
                Gravity.TOP -> snapView = findStartView(layoutManager, getVerticalHelper(layoutManager))
                Gravity.BOTTOM -> snapView = findEndView(layoutManager, getVerticalHelper(layoutManager))
            }
        }
        return snapView
    }

    private val isRtl: Boolean
         get() = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) { false }
         else TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL

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

    private fun distanceToStart(targetView: View, helper: OrientationHelper, fromEnd: Boolean): Int {
        return if (isRtlHorizontal && !fromEnd) {
            distanceToEnd(targetView, helper, true)
        } else helper.getDecoratedStart(targetView) - helper.startAfterPadding
    }

    private fun distanceToEnd(targetView: View, helper: OrientationHelper, fromStart: Boolean): Int {
        return if (isRtlHorizontal && !fromStart) {
            distanceToStart(targetView, helper, true)
        } else helper.getDecoratedEnd(targetView) - helper.endAfterPadding
    }

    private fun findStartView(layoutManager: RecyclerView.LayoutManager, helper: OrientationHelper): View? {
        if (layoutManager is LinearLayoutManager) {
            val reverseLayout = layoutManager.reverseLayout
            val firstChild =
                if (reverseLayout) layoutManager.findLastVisibleItemPosition() else layoutManager.findFirstVisibleItemPosition()
            var offset = 1
            if (layoutManager is GridLayoutManager) {
                offset += layoutManager.spanCount - 1
            }
            if (firstChild == RecyclerView.NO_POSITION) {
                return null
            }
            val child = layoutManager.findViewByPosition(firstChild)
            val visibleWidth: Float
            visibleWidth = if (isRtlHorizontal) {
                ((helper.totalSpace - helper.getDecoratedStart(child)).toFloat()
                        / helper.getDecoratedMeasurement(child))
            } else {
                (helper.getDecoratedEnd(child).toFloat()
                        / helper.getDecoratedMeasurement(child))
            }
            val endOfList: Boolean
            endOfList = if (!reverseLayout) {
                (layoutManager
                    .findLastCompletelyVisibleItemPosition()
                        == layoutManager.getItemCount() - 1)
            } else {
                (layoutManager
                    .findFirstCompletelyVisibleItemPosition()
                        == 0)
            }
            return if (visibleWidth > 0.5f && !endOfList) {
                child
            } else if (snapLastItem && endOfList) {
                child
            } else if (endOfList) {
                null
            } else {
                if (reverseLayout) layoutManager.findViewByPosition(firstChild - offset) else layoutManager.findViewByPosition(
                    firstChild + offset
                )
            }
        }
        return null
    }

    private fun findEndView(layoutManager: RecyclerView.LayoutManager, helper: OrientationHelper): View? {
        if (layoutManager is LinearLayoutManager) {
            val reverseLayout = layoutManager.reverseLayout
            val lastChild = if (reverseLayout) layoutManager.findFirstVisibleItemPosition() else layoutManager.findLastVisibleItemPosition()
            var offset = 1
            if (layoutManager is GridLayoutManager) {
                offset += layoutManager.spanCount - 1
            }
            if (lastChild == RecyclerView.NO_POSITION) {
                return null
            }
            val child = layoutManager.findViewByPosition(lastChild)
            val visibleWidth: Float
            visibleWidth = if (isRtlHorizontal) {
                (helper.getDecoratedEnd(child).toFloat() / helper.getDecoratedMeasurement(child))
            } else {
                ((helper.totalSpace - helper.getDecoratedStart(child)).toFloat() / helper.getDecoratedMeasurement(child))
            }
            val startOfList = if (!reverseLayout) {
                layoutManager.findFirstCompletelyVisibleItemPosition() == 0
            } else {
                layoutManager.findLastCompletelyVisibleItemPosition() == layoutManager.getItemCount() - 1
            }
            return if (visibleWidth > 0.5f && !startOfList) {
                child
            } else if (snapLastItem && startOfList) {
                child
            } else if (startOfList) {
                null
            } else {
                if (reverseLayout) layoutManager.findViewByPosition(lastChild + offset)
                else layoutManager.findViewByPosition(lastChild - offset)
            }
        }
        return null
    }

}
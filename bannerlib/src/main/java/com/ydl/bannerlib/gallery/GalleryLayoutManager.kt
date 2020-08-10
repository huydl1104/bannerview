package com.ydl.bannerlib.gallery

import android.content.Context
import android.graphics.PointF
import android.graphics.Rect
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import androidx.recyclerview.widget.RecyclerView.SmoothScroller.ScrollVectorProvider

class GalleryLayoutManager(context: Context?, orientation: Int) :
    LinearLayoutManager(context), ScrollVectorProvider {

    private var mFirstVisiblePosition = 0
    private var mLastVisiblePos = 0
    private var mState: State? = null
    private var mOrientation = HORIZONTAL
    private var mHorizontalHelper: OrientationHelper? = null
    private var mVerticalHelper: OrientationHelper? = null
    private var mItemTransformer: ItemTransformer? = null

    init {
        mOrientation = orientation
    }

    fun attach(selectedPosition: Int) {
        mCurSelectedPosition = Math.max(0, selectedPosition)
    }

    fun attach(recyclerView: RecyclerView?, selectedPosition: Int) {
        requireNotNull(recyclerView) { "The attach RecycleView must not null!!" }
        val mSnapHelper = GalleryLinearSnapHelper(recyclerView)
        mCurSelectedPosition = Math.max(0, selectedPosition)
        recyclerView.layoutManager = this
        mSnapHelper.attachToRecyclerView(recyclerView)
    }

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return if (mOrientation == VERTICAL) {
            RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        } else {
            RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    override fun onLayoutChildren(recycler: Recycler, state: RecyclerView.State) {
        if (itemCount == 0) {
            reset()
            detachAndScrapAttachedViews(recycler)
            return
        }
        if (state.isPreLayout || state.itemCount != 0 && !state.didStructureChange()) {
            return
        }
        if (childCount == 0 || state.didStructureChange()) {
            reset()
        }
        //去初始化选中索引。注意如果是 mInitialSelectedPosition>getItemCount()-1，则取值为getItemCount()-1
        val max = Math.max(0, mCurSelectedPosition)
        mCurSelectedPosition = Math.min(max, itemCount - 1)
        logger("position --> $mCurSelectedPosition")
        detachAndScrapAttachedViews(recycler)
        firstFillCover(recycler, 0)
    }

    override fun canScrollHorizontally(): Boolean {
        return mOrientation == HORIZONTAL
    }

    override fun canScrollVertically(): Boolean {
        return mOrientation == VERTICAL
    }

    /**
     * 该方法作用是启动到适配器位置的平滑滚动，必须重写，否则无法实现滚动
     * @param recyclerView                  recyclerView
     * @param state                         state状态
     * @param position                      position索引
     */
    override fun smoothScrollToPosition(recyclerView: RecyclerView, state: RecyclerView.State,position: Int) {
        val linearSmoothScroller = GallerySmoothScroller(recyclerView.context)
        linearSmoothScroller.targetPosition = position
        startSmoothScroll(linearSmoothScroller)
        mCurSelectedPosition = position
    }

    override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
        val direction = calculateScrollDirectionForPosition(targetPosition)
        val outVector = PointF()
        if (direction == 0) {
            return null
        }
        if (mOrientation == HORIZONTAL) {
            outVector.x = direction.toFloat()
            outVector.y = 0f
        } else {
            outVector.x = 0f
            outVector.y = direction.toFloat()
        }
        return outVector
    }

    private fun reset() {
        if (mState != null) {
            mState!!.mItemsFrames.clear()
        }
        mCurSelectedPosition = Math.min(Math.max(0, mCurSelectedPosition), itemCount - 1)
        mFirstVisiblePosition = mCurSelectedPosition
        mLastVisiblePos = mCurSelectedPosition
    }

    private fun firstFillCover(recycler: Recycler, scrollDelta: Int) { //首先判断是横向还是竖向的

        if (mOrientation == HORIZONTAL) {
            firstFillWithHorizontal(recycler)
        } else {
            firstFillWithVertical(recycler)
        }
        logger("firstFillCover finish:first: $mFirstVisiblePosition,last:$mLastVisiblePos")
        if (mItemTransformer != null) {
            var child: View?
            for (i in 0 until childCount) {
                child = getChildAt(i)
                mItemTransformer!!.transformItem(this, child, calculateToCenterFraction(child, scrollDelta.toFloat()))
            }
        }
    }

    private fun firstFillWithHorizontal(recycler: Recycler) {

        detachAndScrapAttachedViews(recycler)
        val leftEdge = orientationHelper!!.startAfterPadding
        val rightEdge = orientationHelper!!.endAfterPadding
        val startPosition = mCurSelectedPosition
        val scrapWidth: Int
        val scrapHeight: Int
        val scrapRect = Rect()
        val height = verticalSpace
        val topOffset: Int
        //layout the init position view
        val scrap = recycler.getViewForPosition(mCurSelectedPosition)
        addView(scrap, 0)
        measureChildWithMargins(scrap, 0, 0)
        scrapWidth = getDecoratedMeasuredWidth(scrap)
        scrapHeight = getDecoratedMeasuredHeight(scrap)
        topOffset = (paddingTop + (height - scrapHeight) / 2.0f).toInt()
        val left = (paddingLeft + (horizontalSpace - scrapWidth) / 2f).toInt()
        scrapRect[left, topOffset, left + scrapWidth] = topOffset + scrapHeight
        layoutDecorated(scrap, scrapRect.left, scrapRect.top, scrapRect.right, scrapRect.bottom)
        if (getState().mItemsFrames.get(startPosition) == null) {
            getState().mItemsFrames.put(startPosition, scrapRect)
        } else {
            getState().mItemsFrames.get(startPosition)?.set(scrapRect)
        }
        mLastVisiblePos = startPosition
        mFirstVisiblePosition = mLastVisiblePos
        //fill left of center
        fillLeft(recycler, mCurSelectedPosition - 1, getDecoratedLeft(scrap), leftEdge)
        //fill right of center
        fillRight(recycler, mCurSelectedPosition + 1, getDecoratedRight(scrap), rightEdge)
    }

    private fun firstFillWithVertical(recycler: Recycler) {
        detachAndScrapAttachedViews(recycler)
        val topEdge = orientationHelper!!.startAfterPadding
        val bottomEdge = orientationHelper!!.endAfterPadding
        val startPosition = mCurSelectedPosition
        val scrapWidth: Int
        val scrapHeight: Int
        val scrapRect = Rect()
        val width = horizontalSpace
        val leftOffset: Int
        //layout the init position view
        val scrap = recycler.getViewForPosition(mCurSelectedPosition)
        addView(scrap, 0)
        measureChildWithMargins(scrap, 0, 0)
        scrapWidth = getDecoratedMeasuredWidth(scrap)
        scrapHeight = getDecoratedMeasuredHeight(scrap)
        leftOffset = (paddingLeft + (width - scrapWidth) / 2.0f).toInt()
        val top = (paddingTop + (verticalSpace - scrapHeight) / 2f).toInt()
        scrapRect[leftOffset, top, leftOffset + scrapWidth] = top + scrapHeight
        layoutDecorated(scrap, scrapRect.left, scrapRect.top, scrapRect.right, scrapRect.bottom)
        if (getState().mItemsFrames.get(startPosition) == null) {
            getState().mItemsFrames.put(startPosition, scrapRect)
        } else {
            getState().mItemsFrames.get(startPosition)?.set(scrapRect)
        }
        mLastVisiblePos = startPosition
        mFirstVisiblePosition = mLastVisiblePos
        //fill left of center
        fillTop(recycler, mCurSelectedPosition - 1, getDecoratedTop(scrap), topEdge)
        //fill right of center
        fillBottom(recycler, mCurSelectedPosition + 1, getDecoratedBottom(scrap), bottomEdge)
    }

    private fun fillLeft(recycler: Recycler,startPosition: Int,startOffset: Int, leftEdge: Int) {

        var startOffset = startOffset
        var scrap: View
        var topOffset: Int
        var scrapWidth: Int
        var scrapHeight: Int
        val scrapRect = Rect()
        val height = verticalSpace
        var i = startPosition
        while (i >= 0 && startOffset > leftEdge) {
            scrap = recycler.getViewForPosition(i)
            addView(scrap, 0)
            measureChildWithMargins(scrap, 0, 0)
            scrapWidth = getDecoratedMeasuredWidth(scrap)
            scrapHeight = getDecoratedMeasuredHeight(scrap)
            topOffset = (paddingTop + (height - scrapHeight) / 2.0f).toInt()
            scrapRect[startOffset - scrapWidth, topOffset, startOffset] = topOffset + scrapHeight
            layoutDecorated(scrap, scrapRect.left, scrapRect.top, scrapRect.right, scrapRect.bottom)
            startOffset = scrapRect.left
            mFirstVisiblePosition = i
            if (getState().mItemsFrames[i] == null) {
                getState().mItemsFrames.put(i, scrapRect)
            } else {
                getState().mItemsFrames[i]!!.set(scrapRect)
            }
            i--
        }
    }

    private fun fillRight(recycler: Recycler, startPosition: Int, startOffset: Int,rightEdge: Int) {
        var startOffset = startOffset
        var scrap: View
        var topOffset: Int
        var scrapWidth: Int
        var scrapHeight: Int
        val scrapRect = Rect()
        val height = verticalSpace
        var i = startPosition
        while (i < itemCount && startOffset < rightEdge) {
            scrap = recycler.getViewForPosition(i)
            addView(scrap)
            measureChildWithMargins(scrap, 0, 0)
            scrapWidth = getDecoratedMeasuredWidth(scrap)
            scrapHeight = getDecoratedMeasuredHeight(scrap)
            topOffset = (paddingTop + (height - scrapHeight) / 2.0f).toInt()
            scrapRect[startOffset, topOffset, startOffset + scrapWidth] = topOffset + scrapHeight
            layoutDecorated(scrap, scrapRect.left, scrapRect.top, scrapRect.right, scrapRect.bottom)
            startOffset = scrapRect.right
            mLastVisiblePos = i
            if (getState().mItemsFrames[i] == null) {
                getState().mItemsFrames.put(i, scrapRect)
            } else {
                getState().mItemsFrames[i]!!.set(scrapRect)
            }
            i++
        }
    }

    private fun fillTop(recycler: Recycler, startPosition: Int, startOffset: Int,topEdge: Int) {

        var startOffset = startOffset
        var scrap: View
        var leftOffset: Int
        var scrapWidth: Int
        var scrapHeight: Int
        val scrapRect = Rect()
        val width = horizontalSpace
        var i = startPosition
        while (i >= 0 && startOffset > topEdge) {
            scrap = recycler.getViewForPosition(i)
            addView(scrap, 0)
            measureChildWithMargins(scrap, 0, 0)
            scrapWidth = getDecoratedMeasuredWidth(scrap)
            scrapHeight = getDecoratedMeasuredHeight(scrap)
            leftOffset = (paddingLeft + (width - scrapWidth) / 2.0f).toInt()
            scrapRect[leftOffset, startOffset - scrapHeight, leftOffset + scrapWidth] = startOffset
            layoutDecorated(scrap, scrapRect.left, scrapRect.top, scrapRect.right, scrapRect.bottom)
            startOffset = scrapRect.top
            mFirstVisiblePosition = i
            if (getState().mItemsFrames[i] == null) {
                getState().mItemsFrames.put(i, scrapRect)
            } else {
                getState().mItemsFrames[i]!!.set(scrapRect)
            }
            i--
        }
    }

    private fun fillBottom(recycler: Recycler, startPosition: Int, startOffset: Int, bottomEdge: Int) {
        var startOffset = startOffset
        var scrap: View
        var leftOffset: Int
        var scrapWidth: Int
        var scrapHeight: Int
        val scrapRect = Rect()
        val width = horizontalSpace
        var i = startPosition
        while (i < itemCount && startOffset < bottomEdge) {
            scrap = recycler.getViewForPosition(i)
            addView(scrap)
            measureChildWithMargins(scrap, 0, 0)
            scrapWidth = getDecoratedMeasuredWidth(scrap)
            scrapHeight = getDecoratedMeasuredHeight(scrap)
            leftOffset = (paddingLeft + (width - scrapWidth) / 2.0f).toInt()
            scrapRect[leftOffset, startOffset, leftOffset + scrapWidth] = startOffset + scrapHeight
            layoutDecorated(scrap, scrapRect.left, scrapRect.top, scrapRect.right, scrapRect.bottom)
            startOffset = scrapRect.bottom
            mLastVisiblePos = i
            if (getState().mItemsFrames[i] == null) {
                getState().mItemsFrames.put(i, scrapRect)
            } else {
                getState().mItemsFrames[i]!!.set(scrapRect)
            }
            i++
        }
    }

    private fun fillCover(recycler: Recycler,scrollDelta: Int) {
        if (itemCount == 0) {
            return
        }
        if (mOrientation == HORIZONTAL) {
            fillWithHorizontal(recycler, scrollDelta)
        } else {
            fillWithVertical(recycler, scrollDelta)
        }
        if (mItemTransformer != null) {
            var child: View?
            for (i in 0 until childCount) {
                child = getChildAt(i)
                mItemTransformer!!.transformItem(this, child, calculateToCenterFraction(child, scrollDelta.toFloat()))
            }
        }
    }

    private fun calculateToCenterFraction(child: View?, pendingOffset: Float): Float {
        val distance = calculateDistanceCenter(child, pendingOffset)
        val childLength = if (mOrientation == HORIZONTAL) child!!.width else child!!.height
        logger("calculateToCenterFraction: distance-> $distance,childLength:$childLength")
        return Math.max(-1f, Math.min(1f, distance * 1f / childLength))
    }

    private fun calculateDistanceCenter(child: View?, pendingOffset: Float): Int {
        val orientationHelper = orientationHelper
        val parentCenter =
            (orientationHelper!!.endAfterPadding - orientationHelper.startAfterPadding) / 2 + orientationHelper.startAfterPadding
        return if (mOrientation == HORIZONTAL) {
            (child!!.width / 2 - pendingOffset + child.left - parentCenter).toInt()
        } else {
            (child!!.height / 2 - pendingOffset + child.top - parentCenter).toInt()
        }
    }

    private fun fillWithVertical(recycler: Recycler,dy: Int) {
        logger("fillWithVertical: dy:$dy")
        val topEdge = orientationHelper!!.startAfterPadding
        val bottomEdge = orientationHelper!!.endAfterPadding
        //1.remove and recycle the view that disappear in screen
        var child: View?
        if (childCount > 0) {
            if (dy >= 0) { //remove and recycle the top off screen view
                var fixIndex = 0
                for (i in 0 until childCount) {
                    child = getChildAt(i + fixIndex)
                    if (getDecoratedBottom(child!!) - dy < topEdge) {
                        logger("fillWithVertical: removeAndRecycleView:" + getPosition(child) + ",bottom:" + getDecoratedBottom(child))
                        removeAndRecycleView(child, recycler)
                        mFirstVisiblePosition++
                        fixIndex--
                    } else {
                        logger("fillWithVertical: break:" + getPosition(child) + ",bottom:" + getDecoratedBottom(child))
                        break
                    }
                }
            } else { //dy<0
                //remove and recycle the bottom off screen view
                for (i in childCount - 1 downTo 0) {
                    child = getChildAt(i)
                    if (getDecoratedTop(child!!) - dy > bottomEdge) {
                        logger("fillWithVertical: removeAndRecycleView:" + getPosition(child))
                        removeAndRecycleView(child, recycler)
                        mLastVisiblePos--
                    } else {
                        break
                    }
                }
            }
        }
        var startPosition = mFirstVisiblePosition
        var startOffset = -1
        var scrapWidth: Int
        var scrapHeight: Int
        var scrapRect: Rect?= null
        val width = horizontalSpace
        var leftOffset: Int
        var scrap: View
        //2.Add or reattach item view to fill screen
        if (dy >= 0) {
            if (childCount != 0) {
                val lastView = getChildAt(childCount - 1)
                startPosition = getPosition(lastView!!) + 1
                startOffset = getDecoratedBottom(lastView)
            }
            var i = startPosition
            while (i < itemCount && startOffset < bottomEdge + dy) {
                scrapRect = getState().mItemsFrames.get(i)
                scrap = recycler.getViewForPosition(i)
                addView(scrap)
                if (scrapRect == null) {
                    scrapRect = Rect()
                    getState().mItemsFrames.put(i, scrapRect)
                }
                measureChildWithMargins(scrap, 0, 0)
                scrapWidth = getDecoratedMeasuredWidth(scrap)
                scrapHeight = getDecoratedMeasuredHeight(scrap)
                leftOffset = (paddingLeft + (width - scrapWidth) / 2.0f).toInt()
                if (startOffset == -1 && startPosition == 0) { //layout the first position item in center
                    val top =
                        (paddingTop + (verticalSpace - scrapHeight) / 2f).toInt()
                    scrapRect[leftOffset, top, leftOffset + scrapWidth] = top + scrapHeight
                } else {
                    scrapRect[leftOffset, startOffset, leftOffset + scrapWidth] =
                        startOffset + scrapHeight
                }
                layoutDecorated(
                    scrap,
                    scrapRect.left,
                    scrapRect.top,
                    scrapRect.right,
                    scrapRect.bottom
                )
                startOffset = scrapRect.bottom
                mLastVisiblePos = i
                logger("fillWithVertical: add view:$i,startOffset:$startOffset,mLastVisiblePos:$mLastVisiblePos,bottomEdge$bottomEdge")
                i++
            }
        } else { //dy<0
            if (childCount > 0) {
                val firstView = getChildAt(0)
                startPosition = getPosition(firstView!!) - 1 //前一个View的position
                startOffset = getDecoratedTop(firstView)
            }
            var i = startPosition
            while (i >= 0 && startOffset > topEdge + dy) {
                scrapRect = getState().mItemsFrames.get(i)
                scrap = recycler.getViewForPosition(i)
                addView(scrap, 0)
                if (scrapRect == null) {
                    scrapRect = Rect()
                    getState().mItemsFrames.put(i, scrapRect)
                }
                measureChildWithMargins(scrap, 0, 0)
                scrapWidth = getDecoratedMeasuredWidth(scrap)
                scrapHeight = getDecoratedMeasuredHeight(scrap)
                leftOffset = (paddingLeft + (width - scrapWidth) / 2.0f).toInt()
                scrapRect[leftOffset, startOffset - scrapHeight, leftOffset + scrapWidth] =
                    startOffset
                layoutDecorated(
                    scrap,
                    scrapRect.left,
                    scrapRect.top,
                    scrapRect.right,
                    scrapRect.bottom
                )
                startOffset = scrapRect.top
                mFirstVisiblePosition = i
                i--
            }
        }
    }


    private fun fillWithHorizontal(recycler: Recycler, dx: Int) {
        val leftEdge = orientationHelper!!.startAfterPadding
        val rightEdge = orientationHelper!!.endAfterPadding
        logger("fillWithHorizontal() called with: dx = [$dx],leftEdge:$leftEdge,rightEdge:$rightEdge")
        //1.remove and recycle the view that disappear in screen
        var child: View?
        if (childCount > 0) {
            if (dx >= 0) { //remove and recycle the left off screen view
                var fixIndex = 0
                for (i in 0 until childCount) {
                    child = getChildAt(i + fixIndex)
                    if (getDecoratedRight(child!!) - dx < leftEdge) {
                        removeAndRecycleView(child, recycler)
                        mFirstVisiblePosition++
                        fixIndex--
                        logger("fillWithHorizontal:removeAndRecycleView:" + getPosition(child) + " mFirstVisiblePosition change to:" + mFirstVisiblePosition)
                    } else {
                        break
                    }
                }
            } else { //dx<0
                //remove and recycle the right off screen view
                for (i in childCount - 1 downTo 0) {
                    child = getChildAt(i)
                    if (getDecoratedLeft(child!!) - dx > rightEdge) {
                        removeAndRecycleView(child, recycler)
                        mLastVisiblePos--
                        logger("fillWithHorizontal:removeAndRecycleView:" + getPosition(child) + "mLastVisiblePos change to:" + mLastVisiblePos)
                    }
                }
            }
        }
        //2.Add or reattach item view to fill screen
        var startPosition = mFirstVisiblePosition
        var startOffset = -1
        var scrapWidth: Int
        var scrapHeight: Int
        var scrapRect: Rect?= null
        val height = verticalSpace
        var topOffset: Int
        var scrap: View
        if (dx >= 0) {
            if (childCount != 0) {
                val lastView = getChildAt(childCount - 1)
                startPosition = getPosition(lastView!!) + 1 //start layout from next position item
                startOffset = getDecoratedRight(lastView)
                logger("fillWithHorizontal:to right startPosition:$startPosition,startOffset:$startOffset,rightEdge:$rightEdge")
            }
            var i = startPosition
            while (i < itemCount && startOffset < rightEdge + dx) {
                val framesArray=getState().mItemsFrames
                scrapRect = framesArray[i]
                scrap = recycler.getViewForPosition(i)
                addView(scrap)
                if (scrapRect == null) {
                    scrapRect = Rect()
                    framesArray.put(i, scrapRect)
                }
                measureChildWithMargins(scrap, 0, 0)
                scrapWidth = getDecoratedMeasuredWidth(scrap)
                scrapHeight = getDecoratedMeasuredHeight(scrap)
                topOffset = (paddingTop + (height - scrapHeight) / 2.0f).toInt()
                if (startOffset == -1 && startPosition == 0) { // layout the first position item in center
                    val left = (paddingLeft + (horizontalSpace - scrapWidth) / 2f).toInt()
                    scrapRect[left, topOffset, left + scrapWidth] = topOffset + scrapHeight
                } else {
                    scrapRect[startOffset, topOffset, startOffset + scrapWidth] = topOffset + scrapHeight
                }
                layoutDecorated(scrap, scrapRect.left, scrapRect.top, scrapRect.right, scrapRect.bottom)
                startOffset = scrapRect.right
                mLastVisiblePos = i
                logger("fillWithHorizontal,layout:mLastVisiblePos: $mLastVisiblePos")
                i++
            }
        } else { //dx<0
            if (childCount > 0) {
                val firstView = getChildAt(0)
                startPosition = getPosition(firstView!!) - 1 //start layout from previous position item
                startOffset = getDecoratedLeft(firstView)
                logger("fillWithHorizontal:to left startPosition:$startPosition,startOffset:$startOffset,leftEdge:$leftEdge,child count:$childCount")
            }
            var i = startPosition
            while (i >= 0 && startOffset > leftEdge + dx) {
                scrapRect = getState().mItemsFrames.get(i)
                scrap = recycler.getViewForPosition(i)
                addView(scrap, 0)
                if (scrapRect == null) {
                    scrapRect = Rect()
                    getState().mItemsFrames.put(i, scrapRect)
                }
                measureChildWithMargins(scrap, 0, 0)
                scrapWidth = getDecoratedMeasuredWidth(scrap)
                scrapHeight = getDecoratedMeasuredHeight(scrap)
                topOffset = (paddingTop + (height - scrapHeight) / 2.0f).toInt()
                scrapRect[startOffset - scrapWidth, topOffset, startOffset] = topOffset + scrapHeight
                layoutDecorated(scrap, scrapRect.left, scrapRect.top, scrapRect.right, scrapRect.bottom)
                startOffset = scrapRect.left
                mFirstVisiblePosition = i
                i--
            }
        }
    }

    private val horizontalSpace: Int get() = width - paddingRight - paddingLeft

    private val verticalSpace: Int get() = height - paddingBottom - paddingTop

    private fun calculateScrollDirectionForPosition(position: Int): Int {
        if (childCount == 0) {
            return LAYOUT_START
        }
        val firstChildPos = mFirstVisiblePosition
        return if (position < firstChildPos) LAYOUT_START else LAYOUT_END
    }

    override fun scrollHorizontallyBy(dx: Int, recycler: Recycler, state: RecyclerView.State): Int {
        // When dx is positive，finger fling from right to left(←)，scrollX+
        if (childCount == 0 || dx == 0) {
            return 0
        }
        var delta = -dx
        val parentCenter =
            (orientationHelper!!.endAfterPadding - orientationHelper!!.startAfterPadding) / 2 + orientationHelper!!.startAfterPadding
        val child: View?
        if (dx > 0) { //If we've reached the last item, enforce limits
            if (getPosition(getChildAt(childCount - 1)!!) == itemCount - 1) {
                child = getChildAt(childCount - 1)
                delta = -Math.max(0,
                    Math.min(dx, (child!!.right - child.left) / 2 + child.left - parentCenter)
                )
            }
        } else { //If we've reached the first item, enforce limits
            if (mFirstVisiblePosition == 0) {
                child = getChildAt(0)
                delta = -Math.min(0,
                    Math.max(dx, (child!!.right - child.left) / 2 + child.left - parentCenter)
                )
            }
        }
        logger("scrollHorizontallyBy: dx:$dx,fixed:$delta")
        getState().mScrollDelta = -delta
        fillCover(recycler, -delta)
        offsetChildrenHorizontal(delta)
        return -delta
    }

    override fun scrollVerticallyBy(dy: Int, recycler: Recycler, state: RecyclerView.State): Int {
        if (childCount == 0 || dy == 0) {
            return 0
        }
        var delta = -dy
        val parentCenter =
            (orientationHelper!!.endAfterPadding - orientationHelper!!.startAfterPadding) / 2 + orientationHelper!!.startAfterPadding
        val child: View?
        if (dy > 0) { //If we've reached the last item, enforce limits
            if (getPosition(getChildAt(childCount - 1)!!) == itemCount - 1) {
                child = getChildAt(childCount - 1)
                delta = -Math.max(
                    0,
                    Math.min(
                        dy,
                        (getDecoratedBottom(child!!) - getDecoratedTop(child)) / 2 + getDecoratedTop(
                            child
                        ) - parentCenter
                    )
                )
            }
        } else { //If we've reached the first item, enforce limits
            if (mFirstVisiblePosition == 0) {
                child = getChildAt(0)
                delta = -Math.min(
                    0,
                    Math.max(dy, (getDecoratedBottom(child!!) - getDecoratedTop(child)) / 2 + getDecoratedTop(child) - parentCenter)
                )
            }
        }
        logger("scrollVerticallyBy: dy-> $dy,fixed:$delta")
        getState().mScrollDelta = -delta
        fillCover(recycler, -delta)
        offsetChildrenVertical(delta)
        return -delta
    }

    private val orientationHelper: OrientationHelper?
         get() = if (mOrientation == HORIZONTAL) {
            if (mHorizontalHelper == null) {
                mHorizontalHelper = OrientationHelper.createHorizontalHelper(this)
            }
            mHorizontalHelper
        } else {
            if (mVerticalHelper == null) {
                mVerticalHelper = OrientationHelper.createVerticalHelper(this)
            }
            mVerticalHelper
        }

    fun setItemTransformer(itemTransformer: ItemTransformer?) {
        mItemTransformer = itemTransformer
    }

    interface ItemTransformer {
        fun transformItem(layoutManager: GalleryLayoutManager?, item: View?, fraction: Float)
    }

    inner class State {
        var mItemsFrames = SparseArray<Rect?>()
        var mScrollDelta = 0
    }

    private fun getState(): State{
        if (mState == null) {
            mState = State()
        }
        return mState!!
    }

    companion object {
        private const val TAG = "GalleryLayoutManager"
        private const val LAYOUT_START = -1
        private const val LAYOUT_END = 1
        private const val HORIZONTAL = OrientationHelper.HORIZONTAL
        private const val VERTICAL = OrientationHelper.VERTICAL
        private var mCurSelectedPosition = 0
        var mPosition: Int
            get() {
                logger("position-----$mCurSelectedPosition")
                return mCurSelectedPosition
            }
            set(position) {
                mCurSelectedPosition = position
            }

        private fun logger(log: String) {
            Log.e(TAG, log)
        }
    }


}
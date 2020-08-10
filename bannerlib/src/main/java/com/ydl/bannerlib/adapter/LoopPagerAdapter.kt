package com.ydl.bannerlib.adapter

import android.annotation.TargetApi
import android.database.DataSetObserver
import android.graphics.Rect
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.ydl.bannerlib.interfces.BaseHintView
import com.ydl.bannerlib.interfces.HintViewDelegate
import com.ydl.bannerlib.view.CustomBannerView
import java.lang.reflect.Field
import java.util.*

/**
 * 自动轮播图
 */
abstract class LoopPagerAdapter(viewPager: CustomBannerView) : PagerAdapter() {

    private val mViewPager: CustomBannerView = viewPager

    private val mViewList = ArrayList<View>()

    init {
        viewPager.setHintViewDelegate(LoopHintViewDelegate())
    }

    private inner class LoopHintViewDelegate : HintViewDelegate {

        override fun setCurrentPosition(position: Int, hintView: BaseHintView?) {
            if (realCount > 0) {
                hintView?.setCurrent(position % realCount)
            }
        }

        override fun initView(length: Int, gravity: Int, hintView: BaseHintView?, padding: Rect, margin: Rect) {
            hintView?.initView(realCount, gravity, padding, margin)
        }

    }

    /**
     * 刷新全部
     */
    override fun notifyDataSetChanged() {
        mViewList.clear()
        initPosition()
        super.notifyDataSetChanged()
    }

    /**
     * POSITION_UNCHANGED表示位置没有变化，即在添加或移除一页或多页之后该位置的页面保持不变，
     * 可以用于一个ViewPager中最后几页的添加或移除时，保持前几页仍然不变.
     * POSITION_NONE，表示当前页不再作为ViewPager的一页数据，将被销毁，可以用于无视View缓存的刷新；
     * 根据传过来的参数Object来判断这个key所指定的新的位置
     */
    internal open fun getItemPosition(@NonNull obj: Any?): Int {
        return POSITION_NONE
    }

    /**
     * 注册数据观察者监听
     * @param observer                      observer
     */
    internal fun registerDataSetObserver(@NonNull observer: DataSetObserver?) {
        super.registerDataSetObserver(observer!!)
        initPosition()
    }

    private fun initPosition() {
        if (realCount > 1) {
            if (mViewPager.getViewPager()!!.currentItem === 0 && realCount > 0) {
                val half = Int.MAX_VALUE / 2
                val start = half - half % realCount
                setCurrent(start)
            }
        }
    }

    /**
     * 设置位置，利用反射实现
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun setCurrent(index: Int) {
        try {
            val field: Field = ViewPager::class.java.getDeclaredField("mCurItem")
            field.isAccessible = true
            field[mViewPager.getViewPager()] = index
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }

    override fun isViewFromObject(@NonNull arg0: View, @NonNull arg1: Any): Boolean {
        return arg0 === arg1
    }

    /**
     * 如果页面不是当前显示的页面也不是要缓存的页面，会调用这个方法，将页面销毁。
     * @param container                     container
     * @param position                      索引
     * @param object                        object
     */
    override fun destroyItem(@NonNull container: ViewGroup, position: Int, @NonNull obj: Any) {
        container.removeView(obj as View?)
        Log.d("PagerAdapter", "销毁的方法")
    }

    /**
     * 要显示的页面或需要缓存的页面，会调用这个方法进行布局的初始化。
     * @param container                     container
     * @param position                      索引
     * @return
     */
    @NonNull
    override fun instantiateItem(@NonNull container: ViewGroup, position: Int): Any {
        val realPosition = position % realCount
        val itemView = findViewByPosition(container, realPosition)
        container.addView(itemView)
        Log.d("PagerAdapter", "创建的方法")
        return itemView
    }

    /**
     * 这个是避免重复创建，如果集合中有，则取集合中的
     */
    private fun findViewByPosition(container: ViewGroup, position: Int): View {
        for (view in mViewList) {
            if (view.tag as Int == position && view.parent == null) {
                return view
            }
        }
        val view = getView(container, position)
        view.tag = position
        mViewList.add(view)
        return view
    }

    override fun getCount(): Int { //设置最大轮播图数量 ，如果是1那么就是1，不轮播；如果大于1则设置一个最大值，可以轮播
        return if (realCount <= 1) realCount else Int.MAX_VALUE
    }

    /**
     * 获取轮播图数量
     */
    abstract val realCount: Int

    /**
     * 创建view
     */
    abstract fun getView(container: ViewGroup?, position: Int): View


}
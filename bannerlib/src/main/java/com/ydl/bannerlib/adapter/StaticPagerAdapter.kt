package com.ydl.bannerlib.adapter

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.viewpager.widget.PagerAdapter
import java.util.*


abstract class StaticPagerAdapter : PagerAdapter() {
    private val mViewList = ArrayList<View>()

    override fun isViewFromObject(@NonNull arg0: View, @NonNull arg1: Any): Boolean {
        return arg0 === arg1
    }

    override fun destroyItem(@NonNull container: ViewGroup, position: Int, @NonNull obj: Any) {
        container.removeView(obj as View?)
        Log.d("PagerAdapter", "销毁的方法")
    }

    override fun notifyDataSetChanged() {
        mViewList.clear()
        super.notifyDataSetChanged()
    }

    override fun getItemPosition(@NonNull obj: Any): Int {
        return POSITION_NONE
    }

    @NonNull
    override fun instantiateItem(@NonNull container: ViewGroup, position: Int): Any {
        val itemView = findViewByPosition(container, position)
        container.addView(itemView)
        onBind(itemView, position)
        Log.d("PagerAdapter", "创建的方法")
        return itemView
    }

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

    fun onBind(view: View?, position: Int) {}
    abstract fun getView(container: ViewGroup?, position: Int): View
}
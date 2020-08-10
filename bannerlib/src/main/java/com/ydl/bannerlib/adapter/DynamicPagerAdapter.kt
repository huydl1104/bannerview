package com.ydl.bannerlib.adapter

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter

abstract class DynamicPagerAdapter : PagerAdapter() {

    override fun isViewFromObject(arg0: View, arg1: Any): Boolean {
        return arg0 === arg1
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
    }

    override fun getItemPosition(obj: Any): Int {
        return super.getItemPosition(obj)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val itemView = getView(container, position)
        container.addView(itemView)
        return itemView
    }

    abstract fun getView(container: ViewGroup?, position: Int): View
}
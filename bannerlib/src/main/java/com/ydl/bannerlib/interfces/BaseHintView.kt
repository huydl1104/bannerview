package com.ydl.bannerlib.interfces

import android.graphics.Rect

/**
 * 所有指示器的接口
 */
interface BaseHintView {
    fun initView(length: Int, gravity: Int,padding :Rect,margin:Rect)
    fun setCurrent(current: Int)
}
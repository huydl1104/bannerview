package com.ydl.bannerlib.interfces

import android.graphics.Rect

interface HintViewDelegate {
    fun setCurrentPosition(position: Int, hintView: BaseHintView?)
    fun initView(length: Int, gravity: Int, hintView: BaseHintView?,padding :Rect,margin:Rect)
}
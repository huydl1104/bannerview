package com.ydl.bannerlib.util

import android.content.Context

object Utils {


    fun dip2px(ctx: Context, dpValue: Float): Int {
        val scale = ctx.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }
    fun px2sp(context: Context, pxValue: Float): Int {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return (pxValue / fontScale + 0.5f).toInt()
    }

    fun px2dip(ctx: Context, pxValue: Float): Int {
        val scale = ctx.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

}
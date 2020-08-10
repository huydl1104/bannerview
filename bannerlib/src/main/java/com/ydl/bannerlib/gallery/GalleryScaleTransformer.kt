package com.ydl.bannerlib.gallery

import android.view.View
import androidx.annotation.FloatRange
import com.ydl.bannerlib.gallery.GalleryLayoutManager.ItemTransformer


class GalleryScaleTransformer(@FloatRange(from = 0.0, to = 1.0) scaleSize: Float, padding: Int) : ItemTransformer {

    private var scaleDivisor = 0.2f    //设置缩放比例因子
    private var padding = 30
    init {
        scaleDivisor = scaleSize
        this.padding = padding
    }


    override fun transformItem(layoutManager: GalleryLayoutManager?, item: View?, fraction: Float) {
        item!!.pivotX = item.width / 2.0f
        item.pivotY = item.height / 2.0f
        if (scaleDivisor == 0.0f) {
            val measuredWidth = item.measuredWidth
            if (padding < 0 || padding > measuredWidth) {
                padding = 30
            }
            item.setPadding(padding, 0, padding, 0)
        } else {
            val scale = 1 - scaleDivisor * Math.abs(fraction)
            //可以在这里对view设置动画效果
            item.scaleX = scale
            item.scaleY = scale
        }
    }


}
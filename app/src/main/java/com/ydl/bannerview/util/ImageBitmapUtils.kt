package com.ydl.bannerview.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.annotation.IntRange
import java.io.ByteArrayOutputStream

object ImageBitmapUtils {
    private const val TAG = "ImageBitmap"
    /*
    质量压缩方法：在保持像素的前提下改变图片的位深及透明度等，来达到压缩图片的目的:
    1、bitmap图片的大小不会改变
    2、bytes.length是随着quality变小而变小的。
    这样适合去传递二进制的图片数据，比如分享图片，要传入二进制数据过去，限制500kb之内。
    */
    /**
     * 第一种：质量压缩法
     * @param image     目标原图
     * @param maxSize   最大的图片大小
     * @return          bitmap，注意可以测试以下压缩前后bitmap的大小值
     */
    fun compressImage(image: Bitmap, maxSize: Long): Bitmap? {
        val byteCount = image.byteCount
        Log.i(TAG, "压缩前大小$byteCount")
        val baos = ByteArrayOutputStream()
        // 把ByteArrayInputStream数据生成图片
        var bitmap: Bitmap? = null
        // 质量压缩方法，options的值是0-100，这里100表示原来图片的质量，不压缩，把压缩后的数据存放到baos中
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        var options = 90
        // 循环判断如果压缩后图片是否大于maxSize,大于继续压缩
        while (baos.toByteArray().size > maxSize) { // 重置baos即清空baos
            baos.reset()
            // 这里压缩options%，把压缩后的数据存放到baos中
            image.compress(Bitmap.CompressFormat.JPEG, options, baos)
            // 每次都减少10，当为1的时候停止，options<10的时候，递减1
            options -= if (options == 1) {
                break
            } else if (options <= 10) {
                1
            } else {
                10
            }
        }
        val bytes = baos.toByteArray()
        if (bytes.size != 0) { // 把压缩后的数据baos存放到bytes中
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            val byteCount1 = bitmap.byteCount
            Log.i(TAG, "压缩后大小$byteCount1")
        }
        return bitmap
    }

    /**
     * 第一种：质量压缩法
     *
     * @param src           源图片
     * @param maxByteSize   允许最大值字节数
     * @param recycle       是否回收
     * @return              质量压缩压缩过的图片
     */
    fun compressByQuality1(
        src: Bitmap?,
        maxByteSize: Long,
        recycle: Boolean
    ): Bitmap? {
        if (src == null || src.width == 0 || src.height == 0 || maxByteSize <= 0) {
            return null
        }
        Log.i(TAG, "压缩前大小" + src.byteCount)
        val baos = ByteArrayOutputStream()
        src.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val bytes: ByteArray
        if (baos.size() <= maxByteSize) { // 最好质量的不大于最大字节，则返回最佳质量
            bytes = baos.toByteArray()
        } else {
            baos.reset()
            src.compress(Bitmap.CompressFormat.JPEG, 0, baos)
            if (baos.size() >= maxByteSize) { // 最差质量不小于最大字节，则返回最差质量
                bytes = baos.toByteArray()
            } else { // 二分法寻找最佳质量
                var st = 0
                var end = 100
                var mid = 0
                while (st < end) {
                    mid = (st + end) / 2
                    baos.reset()
                    src.compress(Bitmap.CompressFormat.JPEG, mid, baos)
                    val len = baos.size()
                    if (len.toLong() == maxByteSize) {
                        break
                    } else if (len > maxByteSize) {
                        end = mid - 1
                    } else {
                        st = mid + 1
                    }
                }
                if (end == mid - 1) {
                    baos.reset()
                    src.compress(Bitmap.CompressFormat.JPEG, st, baos)
                }
                bytes = baos.toByteArray()
            }
        }
        if (recycle && !src.isRecycled) {
            src.recycle()
        }
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        Log.i(TAG, "压缩后大小" + bitmap.byteCount)
        return bitmap
    }

    /**
     * 第一种：质量压缩法
     *
     * @param src           源图片
     * @param quality       质量
     * @param recycle       是否回收
     * @return              质量压缩后的图片
     */
    fun compressByQuality(src: Bitmap?, @IntRange(from = 0, to = 100) quality: Int, recycle: Boolean): Bitmap? {
        if (src == null || src.width == 0 || src.height == 0) {
            return null
        }
        Log.i(TAG, "压缩前大小" + src.byteCount)
        val baos = ByteArrayOutputStream()
        src.compress(Bitmap.CompressFormat.JPEG, quality, baos)
        val bytes = baos.toByteArray()
        if (recycle && !src.isRecycled) {
            src.recycle()
        }
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        Log.i(TAG, "压缩后大小" + bitmap.byteCount)
        return bitmap
    }

    /**
     * 第二种：按采样大小压缩
     *
     * @param src               源图片
     * @param sampleSize        采样率大小
     * @param recycle           是否回收
     * @return                  按采样率压缩后的图片
     */
    fun compressBySampleSize(src: Bitmap?, sampleSize: Int, recycle: Boolean): Bitmap? {
        if (src == null || src.width == 0 || src.height == 0) {
            return null
        }
        Log.i(TAG, "压缩前大小" + src.byteCount)
        val options = BitmapFactory.Options()
        options.inSampleSize = sampleSize
        val baos = ByteArrayOutputStream()
        src.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val bytes = baos.toByteArray()
        if (recycle && !src.isRecycled) {
            src.recycle()
        }
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        Log.i(TAG, "压缩后大小" + bitmap.byteCount)
        return bitmap
    }

    /**
     * 第二种：按采样大小压缩
     *
     * @param src               源图片
     * @param maxWidth          最大宽度
     * @param maxHeight         最大高度
     * @param recycle           是否回收
     * @return                  按采样率压缩后的图片
     */
    fun compressBySampleSize(
        src: Bitmap?,
        maxWidth: Int,
        maxHeight: Int,
        recycle: Boolean
    ): Bitmap? {
        if (src == null || src.width == 0 || src.height == 0) {
            return null
        }
        Log.i(TAG, "压缩前大小" + src.byteCount)
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        val baos = ByteArrayOutputStream()
        src.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val bytes = baos.toByteArray()
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
        options.inJustDecodeBounds = false
        if (recycle && !src.isRecycled) {
            src.recycle()
        }
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        Log.i(TAG, "压缩后大小" + bitmap.byteCount)
        return bitmap
    }

    /**
     * 计算获取缩放比例inSampleSize
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val heightRatio =
                Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio =
                Math.round(width.toFloat() / reqWidth.toFloat())
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        val totalPixels = width * height.toFloat()
        val totalReqPixelsCap = reqWidth * reqHeight * 2.toFloat()
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++
        }
        return inSampleSize
    }

    /**
     * 第三种：按缩放压缩
     *
     * @param src                   源图片
     * @param newWidth              新宽度
     * @param newHeight             新高度
     * @param recycle               是否回收
     * @return                      缩放压缩后的图片
     */
    fun compressByScale(
        src: Bitmap?,
        newWidth: Int,
        newHeight: Int,
        recycle: Boolean
    ): Bitmap? {
        return scale(src, newWidth.toFloat(), newHeight.toFloat(), recycle)
    }

    /**
     * 第三种：按缩放压缩
     *
     * @param src                   源图片
     * @param scaleWidth            缩放宽度倍数
     * @param scaleHeight           缩放高度倍数
     * @param recycle               是否回收
     * @return                      缩放压缩后的图片
     */
    fun compressByScale(
        src: Bitmap?,
        scaleWidth: Float,
        scaleHeight: Float,
        recycle: Boolean
    ): Bitmap? {
        return scale(src, scaleWidth, scaleHeight, recycle)
    }

    /**
     * 缩放图片
     *
     * @param src                   源图片
     * @param scaleWidth            缩放宽度倍数
     * @param scaleHeight           缩放高度倍数
     * @param recycle               是否回收
     * @return                      缩放后的图片
     */
    private fun scale(
        src: Bitmap?,
        scaleWidth: Float,
        scaleHeight: Float,
        recycle: Boolean
    ): Bitmap? {
        if (src == null || src.width == 0 || src.height == 0) {
            return null
        }
        val matrix = Matrix()
        matrix.setScale(scaleWidth, scaleHeight)
        val ret =
            Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
        if (recycle && !src.isRecycled) {
            src.recycle()
        }
        return ret
    }
}
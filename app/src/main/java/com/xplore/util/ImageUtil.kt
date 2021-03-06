package com.xplore.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import com.xplore.CircleTransformation
import com.xplore.R
import com.xplore.util.FirebaseUtil.FS_PROFILE_PIC_EXTENSION
import com.xplore.util.FirebaseUtil.FS_PROFILE_PIC_KB_LIMIT
import com.xplore.util.FirebaseUtil.FS_PROFILE_PIC_NAME_PREFIX
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by Nika on 8/1/2017.
 * TODO write description of this class - what it does and why.
 */

object ImageUtil {

    const val PROFILE_PIC_WIDTH = 300
    const val PROFILE_PIC_HEIGHT = 300

    const val FILE_PROVIDER_AUTHORITY = "com.xplore.fileprovider"

    @JvmStatic
    fun circle(context: Context, dimenId: Int): CircleTransformation {
        val size = context.resources.getDimension(dimenId).toInt()
        return CircleTransformation(size, size)
    }

    @JvmStatic
    fun tinyCircle(context: Context) = circle(context, R.dimen.user_profile_image_tiny_size)

    @JvmStatic
    fun smallCircle(context: Context) = circle(context, R.dimen.user_profile_image_small_size)

    @JvmStatic
    fun mediumCircle(context: Context) = circle(context, R.dimen.user_profile_image_medium_size)

    @JvmStatic
    fun largeCircle(context: Context) = circle(context, R.dimen.user_profile_image_large_size)

    // Adds picture to gallery
    @JvmStatic
    fun addPictureToGallery(context: Context, picturePath: String) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        mediaScanIntent.data = Uri.fromFile(File(picturePath))
        context.sendBroadcast(mediaScanIntent)
    }

    // Creates path for a given picture name
    @JvmStatic
    fun getPicturePath(context: Context, picName: String): String =
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).absolutePath + "/" + picName

    // Returns new path of resized and compressed image
    @JvmStatic
    fun resizeAndCompressImage(context: Context, filePath: String): String {
        val maxImageSize = FS_PROFILE_PIC_KB_LIMIT * 1024 // Kilobytes

        // Decode with inJustDecodeBounds so we can resize it first
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filePath, options)
        //
        options.inSampleSize = calculateInSampleSize(options, PROFILE_PIC_WIDTH, PROFILE_PIC_HEIGHT)
        //
        options.inJustDecodeBounds = false
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        val bmp = BitmapFactory.decodeFile(filePath, options)

        // Compressing
        var compressQuality = 100 // Decreases by 5 every loop
        var streamLength = 0

        do {
            val bmpStream = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
            val bmpByteAarray = bmpStream.toByteArray()
            streamLength = bmpByteAarray.size
            compressQuality -= 5
        } while (streamLength >= maxImageSize)

        val tempFile = createImageFile(context)
        try {
            val tempBmpFile = FileOutputStream(tempFile)
            bmp.compress(Bitmap.CompressFormat.JPEG, compressQuality, tempBmpFile)
            tempBmpFile.flush()
            tempBmpFile.close()
        } catch (e: Exception) {
            throw e
        }
        return tempFile.absolutePath
    }

    // Calculates largest sample size that is the power of 2
    @JvmStatic
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val width = options.outWidth
        val height = options.outHeight
        var sampleSize = 1

        if (width > reqWidth || height > reqHeight) {
            val halfWidth = width /2
            val halfHeight = height / 2

            while ((halfWidth / sampleSize) > reqWidth && (halfHeight / sampleSize) > reqHeight) {
                sampleSize *= 2
            }
        }
        return sampleSize
    }

    // Returns temporary file for storing cropped picture
    @Throws(IOException::class)
    @JvmStatic
    fun createImageFile(context: Context): File {
        val imageFileName = FS_PROFILE_PIC_NAME_PREFIX
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File.createTempFile(imageFileName, FS_PROFILE_PIC_EXTENSION, storageDir)
        return imageFile
    }
}
package com.xplore

import android.content.Context

/**
 * Created by Nika on 8/1/2017.
 * TODO write description of this class - what it does and why.
 */
class ImageUtil {
    companion object {
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
    }
}
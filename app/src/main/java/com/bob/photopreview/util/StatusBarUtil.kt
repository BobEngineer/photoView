package com.bob.tongchuang.util

import android.content.Context

/**
 * Created by iMac on 2019/4/20.
 */
object StatusBarUtil {
    fun getStatusBarHeight(context: Context): Int {
        val resources = context.resources
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return resources.getDimensionPixelSize(resourceId)
    }
}
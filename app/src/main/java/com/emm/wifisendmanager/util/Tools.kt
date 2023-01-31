package com.emm.wifisendmanager.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import com.emm.wifisendmanager.TransportApplication

/**
 * @author dengjie
 * date 2023.01.30
 * description
 */
object Tools {
    /**
     * 调用系统api复制字符串到剪切板
     * @receiver String
     */
    fun String.copy(label: String = "text", tips: String? = "已复制") {
        val clipboardManager = TransportApplication.appContext
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.setPrimaryClip(
            ClipData.newPlainText(label, this)
        )
        showToast(tips ?: "已复制")
    }


    /**
     * toast提示
     */
    fun showToast(string: String) {
        if (string.isNotEmpty())
            Toast.makeText(TransportApplication.appContext, string, Toast.LENGTH_SHORT).show()
    }
}
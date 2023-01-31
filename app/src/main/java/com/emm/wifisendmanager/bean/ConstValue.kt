package com.emm.wifisendmanager.bean

import android.os.Environment
import com.emm.wifisendmanager.TransportApplication
import java.io.File

/**
 * @author dengjie
 * date 2023.01.28
 * description 常量类
 */
object ConstValue {
    const val TEXT_CONTENT_TYPE = "text/html;charset=utf-8"
    const val CSS_CONTENT_TYPE = "text/css;charset=utf-8"
    const val BINARY_CONTENT_TYPE = "application/octet-stream"
    const val JS_CONTENT_TYPE = "application/javascript"
    const val PNG_CONTENT_TYPE = "application/x-png"
    const val JPG_CONTENT_TYPE = "application/jpeg"
    const val SWF_CONTENT_TYPE = "application/x-shockwave-flash"
    const val WOFF_CONTENT_TYPE = "application/x-font-woff"
    const val TTF_CONTENT_TYPE = "application/x-font-truetype"
    const val SVG_CONTENT_TYPE = "image/svg+xml"
    const val EOT_CONTENT_TYPE = "image/vnd.ms-fontobject"
    const val MP3_CONTENT_TYPE = "audio/mp3"
    const val MP4_CONTENT_TYPE = "video/mpeg4"

    const val FILE_DIR_NAME = "dataFile"

    val rootFile = File(TransportApplication.appContext.filesDir, FILE_DIR_NAME)

    const val TXT = "text"
    const val APK = "apk"
    const val JPG = "JPG"
    const val JPEG = "JPEG"
    const val PNG = "PNG"
}
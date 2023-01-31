package com.emm.wifisendmanager.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.emm.wifisendmanager.TransportApplication
import com.emm.wifisendmanager.bean.ConstValue.rootFile
import java.io.File

/**
 * @author dengjie
 * date 2023.01.30
 * description
 */
object FileUtil {

    private val authorities by lazy { "${TransportApplication.appContext.packageName}.fileprovider" }

    /**
     * 根据key获取uri
     */
    fun getUriByKey(key: String): Uri {
        val file = getFileByKey(key)
        return FileProvider.getUriForFile(TransportApplication.appContext, authorities, file)
    }

    /**
     * 根据key获取文件
     */
    fun getFileByKey(key: String): File {
        val parent = rootFile
        if (!parent.exists()) parent.mkdir()
        return File(parent, key)
    }

    /**
     * 根据文件名获取名称和类型
     */
    fun getFileTypeByName(fileName : String) : Pair<String,String>?{
        if(!fileName.contains(".")){
            return null
        }
        val index = fileName.lastIndexOf(".")
        if(index == -1) return null
        val name = fileName.substring(0,index)
        val type = fileName.substring(index + 1,fileName.length)
        return Pair(name,type)
    }

    /**
     * 安装apk
     */
    fun installApk(context: Context,uri : Uri){
        val intent = Intent(Intent.ACTION_VIEW)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.setDataAndType(uri,"application/vnd.android.package-archive")
        }else{
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.setDataAndType(uri,"application/vnd.android.package-archive")
        }
        context.startActivity(intent)
    }
}
package com.emm.wifisendmanager.server

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.emm.wifisendmanager.bean.ConstValue
import com.emm.wifisendmanager.bean.TextBean
import com.emm.wifisendmanager.database.DataBaseStore
import com.emm.wifisendmanager.util.EventUtil
import com.emm.wifisendmanager.util.FileUtil
import com.koushikdutta.async.AsyncServer
import com.koushikdutta.async.ByteBufferList
import com.koushikdutta.async.DataEmitter
import com.koushikdutta.async.callback.CompletedCallback
import com.koushikdutta.async.callback.DataCallback
import com.koushikdutta.async.http.body.MultipartFormDataBody
import com.koushikdutta.async.http.body.MultipartFormDataBody.MultipartCallback
import com.koushikdutta.async.http.body.Part
import com.koushikdutta.async.http.body.UrlEncodedFormBody
import com.koushikdutta.async.http.server.AsyncHttpServer
import com.koushikdutta.async.http.server.AsyncHttpServerRequest
import com.koushikdutta.async.http.server.AsyncHttpServerResponse
import com.koushikdutta.async.http.server.HttpServerRequestCallback
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.Charset
import java.text.DecimalFormat


class WebListenServer : Service() {

    companion object{

        const val ACTION_START_WEB_SERVICE = "com.emm.wifisendmanager.action.START_WEB_SERVICE"
        const val ACTION_STOP_WEB_SERVICE = "com.emm.wifisendmanager.action.STOP_WEB_SERVICE"
        const val ACTION_SEND_TEXT= "com.emm.wifisendmanager.action.SEND_TEXT"

        const val TYPEWRITE_TEXT ="typewrite_text"

        const val PPORT = 65412

        fun start(context: Context) {
            val intent = Intent(context, WebListenServer::class.java)
            intent.action = ACTION_START_WEB_SERVICE
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, WebListenServer::class.java)
            intent.action = ACTION_STOP_WEB_SERVICE
            context.startService(intent)
        }
        fun sendText(context: Context,text : String){
            val intent = Intent(context, WebListenServer::class.java)
            intent.action = ACTION_SEND_TEXT
            intent.putExtra(TYPEWRITE_TEXT,text)
            context.startService(intent)
        }
    }

    private val server by lazy {
        AsyncHttpServer()
    }

    private val mAsyncServer by lazy {
        AsyncServer()
    }

    private val fileUploadHolder by lazy {
        FileUploadHolder()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action){
                ACTION_START_WEB_SERVICE  ->{
                    onStartService()
                }
                ACTION_STOP_WEB_SERVICE ->{
                    stopSelf()
                }
                ACTION_SEND_TEXT ->{//发送文字
                    val text = it.getStringExtra(TYPEWRITE_TEXT)
                    if(!text.isNullOrEmpty()){
//                        DataBaseStore.add(TextBean("text",text))
                        setTextList()
                    }
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * 服务启动
     */
    private fun onStartService(){

        server.get("/", HttpServerRequestCallback { request, response ->
            try {
                response.send(getIndexContent())
            }catch (e : Exception){
                e.printStackTrace()
                response.code(500).end()
            }
        })

        server.get("/images/.*",this::setResources)
        server.get("/scripts/.*", this::setResources)
        server.get("/css/.*", this::setResources)

        //query upload list
        server.get("/files",HttpServerRequestCallback { request: AsyncHttpServerRequest?, response: AsyncHttpServerResponse ->
            val array = JSONArray()
            val dir: File = ConstValue.rootFile
            if (dir.exists() && dir.isDirectory()) {
                val fileNames: Array<String> = dir.list()
                if (fileNames != null) {
                    for (fileName in fileNames) {
                        val file = File(dir, fileName)
                        if (file.exists() && file.isFile()) {
                            try {
                                val jsonObject = JSONObject()
                                jsonObject.put("name", fileName)
                                val fileLen: Long = file.length()
                                val df = DecimalFormat("0.00")
                                if (fileLen > 1024 * 1024) {
                                    jsonObject.put(
                                        "size",
                                        df.format(fileLen * 1f / 1024 / 1024) + "MB"
                                    )
                                } else if (fileLen > 1024) {
                                    jsonObject.put("size", df.format(fileLen * 1f / 1024) + "KB")
                                } else {
                                    jsonObject.put("size", fileLen.toString() + "B")
                                }
                                array.put(jsonObject)
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
            response.send(array.toString())
        })
        //delete
        server.post("/files/.*") { request: AsyncHttpServerRequest, response: AsyncHttpServerResponse ->
            val body = request.body as UrlEncodedFormBody
            if ("delete".equals(body.get().getString("_method"), ignoreCase = true)) {
                var path = request.path.replace("/files/", "")
                try {
                    path = URLDecoder.decode(path, "utf-8")
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                }
                val file = File(ConstValue.rootFile, path)
                if (file.exists() && file.isFile()) {
                    file.delete()
//                    RxBus.get().post(Constants.RxBusEventType.LOAD_BOOK_LIST, 0)
                }
            }
            response.end()
        }
        //download
        server.get("/files/.*", HttpServerRequestCallback { request: AsyncHttpServerRequest, response: AsyncHttpServerResponse ->
            var path = request.path.replace("/files/", "")
            try {
                path = URLDecoder.decode(path, "utf-8")
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
            val file =  File(ConstValue.rootFile, path)
            if (file.exists() && file.isFile()) {
                try {
                    response.headers.add(
                        "Content-Disposition",
                        "attachment;filename=" + URLEncoder.encode(file.getName(), "utf-8")
                    )
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                }
                response.sendFile(file)
                return@HttpServerRequestCallback
            }
            response.code(404).send("Not found!")
        })
        //web端上传文件
        server.post(
            "/files"
        ) { request: AsyncHttpServerRequest, response: AsyncHttpServerResponse ->
            val body = request.body as MultipartFormDataBody
            body.multipartCallback = MultipartCallback { part: Part ->
                if (part.isFile()) {//为文件时，写入文件
                    body.dataCallback =
                        DataCallback { emitter: DataEmitter?, bb: ByteBufferList ->
                            //获取文件数据，并写入本地
                            fileUploadHolder.write(bb.allByteArray)
                            bb.recycle()
                        }
                } else {//为其他数据时，写入文件名
                    if (body.dataCallback == null) {
                        body.dataCallback =
                            DataCallback { emitter: DataEmitter?, bb: ByteBufferList ->
                                try {
                                    val fileName: String =
                                        URLDecoder.decode(String(bb.allByteArray), "UTF-8")
                                    fileUploadHolder.fileName = fileName
                                } catch (e: UnsupportedEncodingException) {
                                    e.printStackTrace()
                                }
                                bb.recycle()
                            }
                    }
                }
            }
            request.endCallback = CompletedCallback { e: java.lang.Exception? ->
                fileUploadHolder.reset()
                response.end()
                //发送消息到列表并保存到本地数据库
                val pair = FileUtil.getFileTypeByName(fileUploadHolder.fileName?:"")
                pair?.let {
                    MainScope().launch {
                        EventUtil.onReceiveDataFlow.emit(TextBean(it.second,it.first))
                    }
                }
//                RxBus.get().post(Constants.RxBusEventType.LOAD_BOOK_LIST, 0)
            }
        }
        server["/progress/.*", { request: AsyncHttpServerRequest, response: AsyncHttpServerResponse ->
            val res = JSONObject()
            val path = request.path.replace("/progress/", "")
            if (path == fileUploadHolder.fileName) {
                try {
                    res.put("fileName", fileUploadHolder.fileName)
                    res.put("size", fileUploadHolder.totalSize)
                    res.put("progress", if (fileUploadHolder.fileOutPutStream == null) 1 else 0.1)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            response.send(res)
        }]

        setTextList()

        server.listen(mAsyncServer,PPORT)
    }

    private fun setTextList(){
        server.get("/text", HttpServerRequestCallback { request, response ->
            val jsonArray = JSONArray()
            val localList = DataBaseStore.query()
            localList.forEach {
                val jsonObject = JSONObject()
                jsonObject.put("type",it.type)
                jsonObject.put("text",it.text)
                jsonArray.put(jsonObject)
            }
            response.send(jsonArray.toString())
        })
    }

    /**
     * 设置加载资源
     */
    private fun setResources(request: AsyncHttpServerRequest,response: AsyncHttpServerResponse){
        try {
            var fullPath = request.path
            fullPath = fullPath.replace("%20", " ")
            var resourceName = fullPath
            if (resourceName.startsWith("/")) {
                resourceName = resourceName.substring(1)
            }
            if (resourceName.indexOf("?") > 0) {
                resourceName = resourceName.substring(0, resourceName.indexOf("?"))
            }
            response.setContentType(getContentTypeByResourceName(resourceName))
            val bInputStream = BufferedInputStream(assets.open(resourceName))
            response.sendStream(bInputStream, bInputStream.available().toLong())
        } catch (e: IOException) {
            e.printStackTrace()
            response.code(404).end()
            return
        }
    }

    private fun getContentTypeByResourceName(resourceName: String): String {
        if (resourceName.endsWith(".css")) {
            return ConstValue.CSS_CONTENT_TYPE
        } else if (resourceName.endsWith(".js")) {
            return ConstValue.JS_CONTENT_TYPE
        } else if (resourceName.endsWith(".swf")) {
            return ConstValue.SWF_CONTENT_TYPE
        } else if (resourceName.endsWith(".png")) {
            return ConstValue.PNG_CONTENT_TYPE
        } else if (resourceName.endsWith(".jpg") || resourceName.endsWith(".jpeg")) {
            return ConstValue.JPG_CONTENT_TYPE
        } else if (resourceName.endsWith(".woff")) {
            return ConstValue.WOFF_CONTENT_TYPE
        } else if (resourceName.endsWith(".ttf")) {
            return ConstValue.TTF_CONTENT_TYPE
        } else if (resourceName.endsWith(".svg")) {
            return ConstValue.SVG_CONTENT_TYPE
        } else if (resourceName.endsWith(".eot")) {
            return ConstValue.EOT_CONTENT_TYPE
        } else if (resourceName.endsWith(".mp3")) {
            return ConstValue.MP3_CONTENT_TYPE
        } else if (resourceName.endsWith(".mp4")) {
            return ConstValue.MP4_CONTENT_TYPE
        }
        return ""
    }

    override fun onDestroy() {
        super.onDestroy()
        //停止
        server.stop()
        mAsyncServer.stop()
    }

    /**
     * 返回index的页面
     */
    @Throws(IOException::class)
    private fun getIndexContent(): String? {
        var bInputStream: BufferedInputStream? = null
        return try {
            //获取index页面
            bInputStream = BufferedInputStream(assets.open("index.html"))
            val baos = ByteArrayOutputStream()
            var len = 0
            val tmp = ByteArray(10240)
            while (bInputStream.read(tmp).also { len = it } > 0) {
                baos.write(tmp, 0, len)
            }
            String(baos.toByteArray(), Charset.forName("utf-8"))
        } catch (e: IOException) {
            e.printStackTrace()
            throw e
        } finally {
            if (bInputStream != null) {
                try {
                    bInputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    class FileUploadHolder {
        var fileName: String? = null
            set(value) {
                field = value
                totalSize = 0
                if (!ConstValue.rootFile.exists()) {
                    ConstValue.rootFile.mkdirs()
                }
                recievedFile = File(ConstValue.rootFile, this.fileName)
                Log.e("ATG",recievedFile!!.absolutePath)
                try {
                    fileOutPutStream = BufferedOutputStream(FileOutputStream(recievedFile))
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        var recievedFile: File? = null
        var fileOutPutStream: BufferedOutputStream? = null
            private set
        var totalSize: Long = 0

        fun reset() {
            if (fileOutPutStream != null) {
                try {
                    fileOutPutStream!!.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            fileOutPutStream = null
        }

        fun write(data: ByteArray) {
            if (fileOutPutStream != null) {
                try {
                    fileOutPutStream!!.write(data)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            totalSize += data.size.toLong()
        }
    }
}
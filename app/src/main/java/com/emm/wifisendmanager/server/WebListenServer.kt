package com.emm.wifisendmanager.server

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.emm.wifisendmanager.bean.TextBean
import com.emm.wifisendmanager.database.DataBaseStore
import com.koushikdutta.async.AsyncServer
import com.koushikdutta.async.http.server.AsyncHttpServer
import com.koushikdutta.async.http.server.HttpServerRequestCallback
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.Exception
import java.nio.charset.Charset

class WebListenServer : Service() {

    companion object{

        const val ACTION_START_WEB_SERVICE = "com.emm.wifisendmanager.action.START_WEB_SERVICE"
        const val ACTION_STOP_WEB_SERVICE = "com.emm.wifisendmanager.action.STOP_WEB_SERVICE"
        const val ACTION_SEND_TEXT= "com.emm.wifisendmanager.action.SEND_TEXT"

        const val TYPEWRITE_TEXT ="typewrite_text"

        const val PPORT = 45321

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

        server.get("/scripts/jquery-1.7.2.min.js", HttpServerRequestCallback { request, response ->
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
                response.setContentType("application/javascript")
                val bInputStream = BufferedInputStream(assets.open(resourceName))
                response.sendStream(bInputStream, bInputStream.available().toLong())
            } catch (e: IOException) {
                e.printStackTrace()
                response.code(404).end()
                return@HttpServerRequestCallback
            }
        })

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
}
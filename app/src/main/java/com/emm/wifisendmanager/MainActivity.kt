package com.emm.wifisendmanager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emm.wifisendmanager.adapter.TextListAdapter
import com.emm.wifisendmanager.bean.ConstValue
import com.emm.wifisendmanager.bean.TextBean
import com.emm.wifisendmanager.bean.TextStoreBean
import com.emm.wifisendmanager.database.DataBaseStore
import com.emm.wifisendmanager.server.WebListenServer
import com.emm.wifisendmanager.util.EventUtil
import com.emm.wifisendmanager.util.FileUtil
import com.emm.wifisendmanager.util.Tools.copy
import com.emm.wifisendmanager.util.WifiUtil
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
import com.koushikdutta.async.http.server.AsyncHttpServerResponse

import com.koushikdutta.async.http.server.AsyncHttpServerRequest
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author:dengjie
 * @time:2021/12/31
 * @description:首页
 **/
class MainActivity : AppCompatActivity() {

    private val btnSend by lazy {
        findViewById<Button>(R.id.btnSend)
    }
    private val tvTip by lazy {
        findViewById<TextView>(R.id.tvTip)
    }


    private val rvList by lazy {
        findViewById<RecyclerView>(R.id.rvList)
    }

    private val etInput by lazy {
        findViewById<EditText>(R.id.etInput)
    }

    private val mAdapter by lazy {
        TextListAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //初始化数据库
        DataBaseStore.initDataBase(this)
        //开始监听
        WebListenServer.start(this)

        tvTip.text = "请连接与手机所在的同一wifi局域网，并在浏览器输入:${WifiUtil.getWifiIp(this)}:${WebListenServer.PPORT}"

        //设置列表
        rvList.layoutManager = LinearLayoutManager(this)
        rvList.adapter = mAdapter
        mAdapter.itemClick ={
            //点击效果
            when(it.type){
                ConstValue.APK -> {
                    //点击安装
                    val fileName = "${it.text}.${it.type}"
                    val uri = FileUtil.getUriByKey(fileName)
                    FileUtil.installApk(this,uri)
                }
                ConstValue.TXT -> {
                    //复制
                    it.text.copy()

                }
                ConstValue.JPG -> {

                }
                ConstValue.JPEG -> {

                }
                ConstValue.PNG -> {

                }
                else -> "未知"
            }

        }
//        mAdapter.setOnDelete {
//            DataBaseStore.delete(it)
//        }

        refreshList()

        btnSend.setOnClickListener {
            //点击发送文字
            val text = etInput.text.toString()
            if(text.isNotEmpty()){
                val bean = TextStoreBean(type = ConstValue.TXT,text = text,timestamp = getTimestamp(System.currentTimeMillis()),timeMils = System.currentTimeMillis())
                val id = DataBaseStore.add(bean)
                WebListenServer.sendText(this,text)
                etInput.setText("") //清空文本
                bean.id = id
                mAdapter.addData(bean)
            }
        }

        lifecycleScope.launch {
            EventUtil.onReceiveDataFlow.collect{
                //接收监听相应的文件发送，并显示到列表
                if(!it.isEmpty()){
                    val bean = TextStoreBean(type = it.type, text = it.text,timestamp = getTimestamp(System.currentTimeMillis()),timeMils = System.currentTimeMillis())
                    val id = DataBaseStore.add(bean)
                    bean.id = id
                    mAdapter.addData(bean)
                }
            }
        }

    }

    private fun getTimestamp(time: Long) : String{
        val simg = SimpleDateFormat("yy-MM-dd hh:mm")
        return simg.format(Date(time))
    }

    private fun refreshList(){
        val list = DataBaseStore.query()
        mAdapter.setList(list)
    }

    override fun onDestroy() {
        super.onDestroy()
        WebListenServer.stop(this)
    }


}
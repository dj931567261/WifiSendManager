package com.emm.wifisendmanager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emm.wifisendmanager.adapter.TextListAdapter
import com.emm.wifisendmanager.bean.TextBean
import com.emm.wifisendmanager.bean.TextStoreBean
import com.emm.wifisendmanager.database.DataBaseStore
import com.emm.wifisendmanager.server.WebListenServer
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

        tvTip.text = "请链接与电脑所在的同一wifi局域网，并在浏览器输入:${WifiUtil.getWifiIp(this)}:${WebListenServer.PPORT}"

        //设置列表
        rvList.layoutManager = LinearLayoutManager(this)
        rvList.adapter = mAdapter
//        mAdapter.setOnDelete {
//            DataBaseStore.delete(it)
//        }

        refreshList()

        btnSend.setOnClickListener {
            val text = etInput.text.toString()
            if(text.isNotEmpty()){
                val bean = TextStoreBean(type = "text",text = text,timestamp = getTimestamp(System.currentTimeMillis()),timeMils = System.currentTimeMillis())
                val id = DataBaseStore.add(bean)
                WebListenServer.sendText(this,text)
                etInput.setText("")
                bean.id = id
                mAdapter.addData(bean)
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
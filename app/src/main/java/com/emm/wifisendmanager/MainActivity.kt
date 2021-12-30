package com.emm.wifisendmanager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.emm.wifisendmanager.bean.TextBean
import com.emm.wifisendmanager.server.WebListenServer
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




class MainActivity : AppCompatActivity() {

    private val btnSend by lazy {
        findViewById<Button>(R.id.btnSend)
    }

    private val etInput by lazy {
        findViewById<EditText>(R.id.etInput)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        WebListenServer.start(this)

        btnSend.setOnClickListener {
            val text = etInput.text.toString()
            if(text.isNotEmpty()){
                WebListenServer.sendText(this,text)
                etInput.setText("")
            }
        }

    }


}
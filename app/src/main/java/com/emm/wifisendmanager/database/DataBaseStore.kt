package com.emm.wifisendmanager.database

import android.content.Context
import com.emm.wifisendmanager.bean.MyObjectBox
import com.emm.wifisendmanager.bean.TextBean
import com.emm.wifisendmanager.bean.TextStoreBean
import io.objectbox.Box
import io.objectbox.BoxStore

object DataBaseStore {
    //ObjectBox数据库操作类，用于初始化及数据库管理
    private var mBoxStore : BoxStore ?= null

    //管理TextStoreBean表的增删改
    private var mTextStoreBox : Box<TextStoreBean> ?= null

    private val mIdWorker by lazy {

    }

    //初始化数据库
    fun initDataBase(context: Context){
        mBoxStore = MyObjectBox.builder()
            .androidContext(context)
            .build()
        mTextStoreBox =  mBoxStore?.boxFor(TextStoreBean::class.java)
    }

    fun add(bean : TextBean){
        mTextStoreBox?.put(TextStoreBean(type = bean.type,text = bean.text,timestamp = System.currentTimeMillis().toString()))
    }

    fun query() : List<TextStoreBean>{
       return mTextStoreBox?.query()?.build()?.find() ?: emptyList()
    }

    fun delete(id : Long){
        mTextStoreBox?.remove(id)
    }
}
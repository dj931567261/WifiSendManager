package com.emm.wifisendmanager.database

import android.content.Context
import com.emm.wifisendmanager.bean.MyObjectBox
import com.emm.wifisendmanager.bean.TextBean
import com.emm.wifisendmanager.bean.TextStoreBean
import io.objectbox.Box
import io.objectbox.BoxStore

/**
 * @author:dengjie
 * @time:2021/12/31
 * @description:数据库管理操作
 **/
object DataBaseStore {
    //ObjectBox数据库操作类，用于初始化及数据库管理
    private var mBoxStore : BoxStore ?= null

    //管理TextStoreBean表的增删改
    private var mTextStoreBox : Box<TextStoreBean> ?= null

    //初始化数据库
    fun initDataBase(context: Context){
        mBoxStore = MyObjectBox.builder()
            .androidContext(context)
            .build()
        mTextStoreBox =  mBoxStore?.boxFor(TextStoreBean::class.java)
    }

    fun add(bean : TextBean) : Long{
        return mTextStoreBox?.put(TextStoreBean(type = bean.type,text = bean.text,timestamp = "",timeMils = System.currentTimeMillis()))?:0
    }

    fun add(bean: TextStoreBean) : Long{
       return mTextStoreBox?.put(bean)?:0
    }

    fun query() : List<TextStoreBean>{
       return mTextStoreBox?.query()?.build()?.find() ?: emptyList()
    }

    fun delete(id : Long){
        mTextStoreBox?.remove(id)
    }
}
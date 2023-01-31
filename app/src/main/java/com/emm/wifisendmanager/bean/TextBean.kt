package com.emm.wifisendmanager.bean

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Uid

/**
 * @author:dengjie
 * @time:2021/12/31
 * @description:实体类
 **/
data class TextBean(val type : String,val text : String){
    fun isEmpty() : Boolean{
        return type.isEmpty() || text.isEmpty()
    }
}

//存入数据库的类
@Entity
data class TextStoreBean(@Id var id : Long = 0, var type : String, var text : String,var timestamp:String,var timeMils : Long)
package com.emm.wifisendmanager.bean

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

data class TextBean(val type : String,val text : String)

@Entity
data class TextStoreBean(@Id var id : Long = 0, var type : String, var text : String, var timestamp:String)
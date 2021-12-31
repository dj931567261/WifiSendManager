package com.emm.wifisendmanager.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.emm.wifisendmanager.R
import com.emm.wifisendmanager.bean.TextStoreBean
import com.emm.wifisendmanager.database.DataBaseStore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author:dengjie
 * @time:2021/12/31
 * @description:
 **/
class TextListAdapter : RecyclerView.Adapter<TextListAdapter.TextListViewHolder>() {

    private val dataList = ArrayList<TextStoreBean>()

//    private var onDeleteClick : ((id : Long) -> Unit) ?= null

    fun addData(data : TextStoreBean){
        dataList.add(data)
        notifyItemInserted(dataList.lastIndex)
    }

    fun setList(list : List<TextStoreBean>){
        dataList.clear()
        dataList.addAll(list)
        notifyDataSetChanged()
    }

//    fun setOnDelete(delete : (id : Long) -> Unit){
//        onDeleteClick = delete
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextListViewHolder {
        return TextListViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_text_list,parent,false))
    }

    override fun onBindViewHolder(holder: TextListViewHolder, position: Int) {
        val data = dataList[position]
        holder.tvText.text = data.text
        holder.tvTime.text = getTimestamp(data.timeMils)
        holder.btnDelete.setOnClickListener {
            DataBaseStore.delete(data.id)
            dataList.remove(data)
            notifyItemRemoved(holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    private fun getTimestamp(time: Long) : String{
        val simg = SimpleDateFormat("yy-MM-dd hh:mm")
        return simg.format(Date(time))
    }

    class TextListViewHolder(view: View) : RecyclerView.ViewHolder(view){
        lateinit var tvText : TextView
        lateinit var btnDelete : TextView
        lateinit var tvTime : TextView
        init {
            tvText = view.findViewById(R.id.tvText)
            btnDelete = view.findViewById(R.id.btnDelete)
            tvTime = view.findViewById(R.id.tvTime)
        }
    }
}
package com.vikanshu.vaartalap.HomeActivity.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.icu.text.TimeZoneFormat
import android.net.Uri
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.vikanshu.vaartalap.R
import com.vikanshu.vaartalap.model.LogsModel
import java.text.DateFormat.*
import java.util.*
import kotlin.collections.ArrayList

class LogsAdapter(
    private val ctx: Context,
    private var data: ArrayList<LogsModel>,
    private var mOnClick: LogListItemClickListener
) :
    RecyclerView.Adapter<LogsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogsViewHolder {
        val view = LayoutInflater.from(ctx).inflate(R.layout.logs_layout, parent, false)
        return LogsViewHolder(view, mOnClick)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: LogsViewHolder, position: Int) {
        holder.setViews(data[position], ctx)
    }

    fun updateData(newData: LogsModel) {
        data.add(newData)
        this.notifyDataSetChanged()
    }

    fun setData(newData: ArrayList<LogsModel>) {
        data = newData
        this.notifyDataSetChanged()
    }

    fun deleteAllData() {
        data.clear()
    }

    interface LogListItemClickListener {
        fun onItemClicked(view: View)
    }
}

class LogsViewHolder(
    itemView: View,
    private var mOnClick: LogsAdapter.LogListItemClickListener
) :
    RecyclerView.ViewHolder(itemView), View.OnClickListener {

    private var imageView = itemView.findViewById<ImageView>(R.id.logs_image)
    private var nameView = itemView.findViewById<TextView>(R.id.logs_name)
    private var statusView = itemView.findViewById<TextView>(R.id.logs_status)
    private val v = itemView

    @SuppressLint("SetTextI18n")
    fun setViews(log: LogsModel, ctx: Context) {
        nameView.text = log.NAME
        Picasso.with(ctx)
            .load(Uri.parse(log.IMAGE))
            .placeholder(ctx.getDrawable(R.drawable.icon_loading))
            .error(ctx.getDrawable(R.drawable.default_user))
            .into(imageView)
        var type = ""
        val time = getDateTimeInstance(SHORT, SHORT).format(Date(log.TIME))
        when (log.TYPE) {
            "O" -> {
                type = "Outgoing"
            }
            "I-A" -> {
                type = "Incoming Accepted"
            }
            "I-R" -> {
                type = "Incoming Rejected"
            }
        }
        statusView.text = "$type ${time.replace(" India Standard Time","")}"
        val data = HashMap<String,Any>()
        data["name"] = log.NAME
        data["number"] = log.NUMBER
        data["image"] = log.IMAGE
        data["uid"] = log.UID
        v.tag = data
        v.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        mOnClick.onItemClicked(v)
    }
}
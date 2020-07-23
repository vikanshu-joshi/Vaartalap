package com.vikanshu.vaartalap.HomeActivity.Adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.vikanshu.vaartalap.R
import com.vikanshu.vaartalap.model.ContactsModel

class ContactsAdapter(private val ctx: Context, private var data: List<ContactsModel>) :
    RecyclerView.Adapter<ContactsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsViewHolder {
        val view = LayoutInflater.from(ctx).inflate(R.layout.contact_layout,parent,false)
        return ContactsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
        holder.setViews(data[position].name,data[position].image,ctx)
    }

    fun setData(newData: List<ContactsModel>){
        data = newData
        notifyDataSetChanged()
    }
}

class ContactsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var imageUser = itemView.findViewById<ImageView>(R.id.contact_image_contacts)
    private var nameUser = itemView.findViewById<TextView>(R.id.contact_name_contacts)

    fun setViews(name: String,image: String,ctx: Context){
        nameUser.text = name
        if(image == "default"){
            imageUser.setImageDrawable(ctx.getDrawable(R.drawable.default_user))
        }else{
            Picasso.with(ctx).load(Uri.parse(image)).into(imageUser)
        }
    }
}
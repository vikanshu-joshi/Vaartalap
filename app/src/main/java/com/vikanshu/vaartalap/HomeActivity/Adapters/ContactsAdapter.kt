package com.vikanshu.vaartalap.HomeActivity.Adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.vikanshu.vaartalap.R
import com.vikanshu.vaartalap.model.ContactsModel


class ContactsAdapter(
    private val ctx: Context,
    private var data: ArrayList<ContactsModel>,
    private var mOnClick: ListItemClickListener
) :
    RecyclerView.Adapter<ContactsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsViewHolder {
        val view = LayoutInflater.from(ctx).inflate(R.layout.contact_layout, parent, false)
        return ContactsViewHolder(view, mOnClick)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
        holder.setViews(data[position].name, data[position].image, data[position].number, ctx,data[position].uid)
    }

    fun updateData(newData: ContactsModel) {
        data.add(newData)
        this.notifyDataSetChanged()
    }

    fun setData(newData: List<ContactsModel>) {
        data = ArrayList(newData)
        this.notifyDataSetChanged()
    }

    fun deleteAllData() {
        data.clear()
    }

    interface ListItemClickListener {
        fun onItemClicked(view: View)
    }
}

class ContactsViewHolder(
    itemView: View,
    private var mOnClick: ContactsAdapter.ListItemClickListener
) :
    RecyclerView.ViewHolder(itemView), View.OnClickListener {

    private var imageUser = itemView.findViewById<ImageView>(R.id.contact_image_contacts)
    private var nameUser = itemView.findViewById<TextView>(R.id.contact_name_contacts)
    private var phoneUser = itemView.findViewById<TextView>(R.id.contact_number_contacts)
    private val v = itemView

    fun setViews(name: String, image: String, number: String, ctx: Context,uid: String) {
        nameUser.text = name
        phoneUser.text = number
        if (image == "default") {
            imageUser.setImageDrawable(ctx.getDrawable(R.drawable.default_user))
        } else {
            Picasso.with(ctx)
                .load(Uri.parse(image))
                .placeholder(ctx.getDrawable(R.drawable.icon_loading))
                .error(ctx.getDrawable(R.drawable.default_user))
                .into(imageUser,object : Callback {
                    override fun onSuccess() {}
                    override fun onError() {
                        Toast.makeText(ctx,"Error loading profile image of $name",Toast.LENGTH_LONG).show()
                    }
                })
        }
        val data = HashMap<String,Any>()
        data["name"] = name
        data["number"] = number
        data["image"] = image
        data["uid"] = uid
        v.tag = data
        v.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        mOnClick.onItemClicked(v)
    }
}
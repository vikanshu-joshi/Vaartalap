package com.vikanshu.vaartalap.HomeActivity.HomeFragments

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.vikanshu.vaartalap.Database.ContactsDBHelper
import com.vikanshu.vaartalap.HomeActivity.Adapters.ContactsAdapter
import com.vikanshu.vaartalap.R
import com.vikanshu.vaartalap.UserDataSharedPref
import com.vikanshu.vaartalap.model.ContactsModel


class ContactsFragment : Fragment() {

    private lateinit var contactsDBHelper: ContactsDBHelper
    private lateinit var firestore: FirebaseFirestore
    private lateinit var contactsRecyclerView: RecyclerView
    private lateinit var adapter: ContactsAdapter
    private lateinit var noContacts: TextView
    private lateinit var refresh: FloatingActionButton
    private lateinit var userDataSharedPref: UserDataSharedPref
    private lateinit var ctx: Context
    private lateinit var mOnClick: ContactsAdapter.ListItemClickListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ctx = activity!!.applicationContext
        contactsDBHelper = ContactsDBHelper(ctx)
        firestore = FirebaseFirestore.getInstance()
        userDataSharedPref = UserDataSharedPref(ctx)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_contacts, container, false)

        contactsRecyclerView = v.findViewById(R.id.recyclerViewContacts)
        noContacts = v.findViewById(R.id.no_contacts_so_far)
        refresh = v.findViewById(R.id.refresh_contacts)

        mOnClick = object : ContactsAdapter.ListItemClickListener {
            override fun onItemClicked(view: View) {
                Toast.makeText(ctx,view.tag.toString(),Toast.LENGTH_LONG).show()
            }
        }

        adapter = ContactsAdapter(ctx, contactsDBHelper.getAll(),mOnClick)

        contactsRecyclerView.adapter = adapter
        contactsRecyclerView.layoutManager = LinearLayoutManager(ctx)

        if (adapter.itemCount == 0) {
            refreshContacts()
            contactsRecyclerView.visibility = View.GONE
            noContacts.visibility = View.VISIBLE
        } else {
            contactsRecyclerView.visibility = View.VISIBLE
            noContacts.visibility = View.GONE
        }

        refresh.setOnClickListener {
            Toast.makeText(ctx, "Refreshing", Toast.LENGTH_LONG).show()
            refreshContacts()
        }

        return v
    }

    private fun refreshContacts() {
        val list = getContacts()
        contactsDBHelper.deleteAll()
        adapter.deleteAllData()
        list.forEach {
            firestore.collection("users").document(it.number).get().addOnSuccessListener { res ->
                if (res.exists()) {
                    val image = res.getString("image")
                    val uid = res.getString("uid")
                    val model = ContactsModel(it.name, it.number, uid!!, image!!)
                    contactsDBHelper.store(model)
                    adapter.updateData(model)
                    if (contactsRecyclerView.visibility == View.GONE) {
                        if (adapter.itemCount == 0) {
                            contactsRecyclerView.visibility = View.GONE
                            noContacts.visibility = View.VISIBLE
                        } else {
                            contactsRecyclerView.visibility = View.VISIBLE
                            noContacts.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun getContacts(): List<ContactsModel> {
        val result = ArrayList<ContactsModel>()
        val cr = ctx.contentResolver
        val cur: Cursor? = cr?.query(
            ContactsContract.Contacts.CONTENT_URI,
            null, null, null, null
        )
        if ((cur?.count ?: 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                val id: String = cur.getString(
                    cur.getColumnIndex(ContactsContract.Contacts._ID)
                )
                val name: String = cur.getString(
                    cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME
                    )
                )
                if (cur.getInt(
                        cur.getColumnIndex(
                            ContactsContract.Contacts.HAS_PHONE_NUMBER
                        )
                    ) > 0
                ) {
                    val pCur: Cursor? = cr.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id),
                        null
                    )
                    while (pCur!!.moveToNext()) {
                        var phoneNo: String = pCur.getString(
                            pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER
                            )
                        )
                        phoneNo = phoneNo.replace(" ", "")
                        if (phoneNo.startsWith("+") || phoneNo.startsWith("0"))
                            phoneNo = phoneNo.substring(1)
                        if (phoneNo.startsWith("91"))
                            phoneNo = phoneNo.substring(2)
                        if (phoneNo != userDataSharedPref.getNumber() && phoneNo.length == 10) {
                            result.add(ContactsModel(name, phoneNo, "", "default"))
                        }
                    }
                    pCur.close()
                }
            }
        }
        cur?.close()
        return result.distinctBy { it.number }
    }
}

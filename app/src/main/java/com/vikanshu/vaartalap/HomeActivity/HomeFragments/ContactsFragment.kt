package com.vikanshu.vaartalap.HomeActivity.HomeFragments

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    private lateinit var contactsList: ArrayList<ContactsModel>
    private lateinit var contactsDBHelper: ContactsDBHelper
    private lateinit var firestore: FirebaseFirestore
    private lateinit var contactsRecyclerView: RecyclerView
    private lateinit var adapter: ContactsAdapter
    private lateinit var noContacts: TextView
    private lateinit var refresh: FloatingActionButton
    private lateinit var userDataSharedPref: UserDataSharedPref

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contactsDBHelper = ContactsDBHelper(activity!!.applicationContext)
        firestore = FirebaseFirestore.getInstance()
        userDataSharedPref = UserDataSharedPref(activity!!.applicationContext)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_contacts, container, false)
        contactsRecyclerView = v.findViewById(R.id.recyclerViewContacts)
        noContacts = v.findViewById(R.id.no_contacts_so_far)
        refresh = v.findViewById(R.id.refresh_contacts)
        contactsList = contactsDBHelper.getAll()
        adapter = ContactsAdapter(activity!!.applicationContext, contactsList.toList())
        contactsRecyclerView.adapter = adapter
        contactsRecyclerView.layoutManager = LinearLayoutManager(activity!!.applicationContext)
        if (contactsList.isEmpty()) {
            contactsRecyclerView.visibility = View.GONE
            noContacts.visibility = View.VISIBLE
        } else {
            contactsRecyclerView.visibility = View.VISIBLE
            noContacts.visibility = View.GONE
        }
        refresh.setOnClickListener {
            Toast.makeText(activity!!.applicationContext, "Refreshing", Toast.LENGTH_LONG).show()
            refreshUserContacts(activity!!.applicationContext)
        }
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        refreshUserContacts(activity!!.applicationContext)
        super.onActivityCreated(savedInstanceState)
    }

    private fun refreshUserContacts(ctx: Context) {
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

                        if (phoneNo != userDataSharedPref.getNumber() && !contactsDBHelper.exists(phoneNo)) {
                            firestore.collection("users").document(phoneNo).get()
                                .addOnSuccessListener {
                                    if (it.exists()) {
                                        val data = it.data
                                        contactsDBHelper.store(
                                            ContactsModel(
                                                name,
                                                phoneNo,
                                                data?.get("uid").toString(),
                                                data?.get("image").toString()
                                            )
                                        )
                                        contactsList.add(
                                            ContactsModel(
                                                name,
                                                phoneNo,
                                                data?.get("uid").toString(),
                                                data?.get("image").toString()
                                            )
                                        )
                                        adapter.setData(contactsList.distinctBy { e ->
                                            e.number
                                        })
                                        if (contactsList.isEmpty()) {
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
                    pCur.close()
                }
            }
        }
        cur?.close()
        contactsList = contactsDBHelper.getAll()
        adapter.notifyDataSetChanged()
    }
}
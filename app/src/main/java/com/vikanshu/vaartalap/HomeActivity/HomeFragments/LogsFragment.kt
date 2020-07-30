package com.vikanshu.vaartalap.HomeActivity.HomeFragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.vikanshu.vaartalap.CallingActivity.OutgoingCallActivity
import com.vikanshu.vaartalap.Database.ContactsDBHelper
import com.vikanshu.vaartalap.Database.LogDBHelper
import com.vikanshu.vaartalap.HomeActivity.Adapters.ContactsAdapter
import com.vikanshu.vaartalap.HomeActivity.Adapters.LogsAdapter
import com.vikanshu.vaartalap.HomeActivity.Adapters.LogsViewHolder
import com.vikanshu.vaartalap.R
import java.util.*
import kotlin.collections.HashMap

class LogsFragment : Fragment() {

    private lateinit var logsDBHelper: LogDBHelper
    private lateinit var logsRecyclerView: RecyclerView
    private lateinit var adapter: LogsAdapter
    private lateinit var userDataSharedPref: SharedPreferences
    private lateinit var ctx: Context
    private lateinit var mOnClick: LogsAdapter.LogListItemClickListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ctx = requireActivity().applicationContext
        logsDBHelper = LogDBHelper(ctx)
        userDataSharedPref = PreferenceManager.getDefaultSharedPreferences(ctx)
    }

    override fun onResume() {
        adapter.setData(logsDBHelper.getAll())
        super.onResume()
    }

    override fun onStart() {
        adapter.setData(logsDBHelper.getAll())
        super.onStart()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_logs, container, false)
        logsRecyclerView = v.findViewById(R.id.logs_recycer_view)
        mOnClick = object : LogsAdapter.LogListItemClickListener {
            override fun onItemClicked(view: View) {
                val data = view.tag as HashMap<*, *>
                val channel = UUID.randomUUID().toString()
                if (userDataSharedPref.getBoolean(
                        getString(R.string.preference_key_status),
                        false
                    )
                ) {
                    Toast.makeText(ctx, "Already Busy On Another Call", Toast.LENGTH_LONG)
                        .show()
                } else {
                    val i = Intent(ctx, OutgoingCallActivity::class.java)
                    i.putExtra(
                        getString(R.string.call_data_name),
                        data["name"].toString()
                    )
                    i.putExtra(
                        getString(R.string.call_data_number),
                        data["number"].toString()
                    )
                    i.putExtra(
                        getString(R.string.call_data_uid),
                        data["uid"].toString()
                    )
                    i.putExtra(
                        getString(R.string.call_data_image),
                        data["image"].toString()
                    )
                    i.putExtra(
                        getString(R.string.call_data_channel),
                        channel
                    )
                    startActivity(i)
                }
            }
        }
        adapter = LogsAdapter(ctx, logsDBHelper.getAll(), mOnClick)
        logsRecyclerView.adapter = adapter
        logsRecyclerView.layoutManager = LinearLayoutManager(ctx)
        return v
    }
}
package com.hits.app.application

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import com.hits.app.ApplicationViewerActivity
import com.hits.app.R
import com.hits.app.data.remote.api.ApplicationApi
import com.hits.app.databinding.ItemRequestBinding
import java.time.format.DateTimeFormatter


class ApplicationAdapter (
    private val context: Context,
    private val api: ApplicationApi,
    private val dataSource: ArrayList<ApplicationItem>
) : BaseAdapter() {

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private val binding = ItemRequestBinding.inflate(inflater)

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("ViewHolder", "SetTextI18n", "SimpleDateFormat")
    override fun getView(position: Int, view: View?, parent: ViewGroup?): View {
        val view = inflater.inflate(R.layout.item_request, parent, false)
        val item = dataSource[position]

        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        val formattedDate: String = item.applicationDate.format(formatter)

        view.findViewById<TextView>(R.id.reqNumber).text = "Заявка №" + item.number
        view.findViewById<TextView>(R.id.date).text = "Дата подачи $formattedDate"

        view.setOnClickListener {
            val intent = Intent(context, ApplicationViewerActivity::class.java)
            intent.putExtra("id", item.id)
            context.startActivity(intent)

            notifyDataSetChanged()
        }
        return view
    }
}
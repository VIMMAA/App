package com.hits.app.request

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.hits.app.R
import com.hits.app.databinding.ItemRequestBinding
import java.time.format.DateTimeFormatter


class RequestAdapter (
    private val context: Context,
    private val dataSource: ArrayList<RequestItem>
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
            dataSource.remove(item)

            notifyDataSetChanged()
        }
        return view
    }
}
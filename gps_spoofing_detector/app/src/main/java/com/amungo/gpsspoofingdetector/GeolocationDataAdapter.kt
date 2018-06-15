package com.amungo.gpsspoofingdetector

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 * Created by Michael Lukin on 23.05.2018.
 */

class GeolocationDataAdapter(val context: Context, val storage: GeolocationData) :
        RecyclerView.Adapter<GeolocationDataAdapter.ViewHolder>(),
        View.OnClickListener {
    private var mInflater: LayoutInflater? = null

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var name: TextView? = null
        internal var longitude: TextView? = null
        internal var latitude: TextView? = null

        init {
            name = itemView.findViewById(R.id.tw_provider)
            longitude = itemView.findViewById(R.id.tw_longitude)
            latitude = itemView.findViewById(R.id.tw_latitude)
        }
    }

//    private val listeners = ArrayList<VisibleBeaconsAdapter.ItemClickListener>()

    init {
        mInflater = LayoutInflater.from(context)
//        if (context is VisibleBeaconsAdapter.ItemClickListener) {
//            listeners.add(context)
//        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view = mInflater?.inflate(R.layout.geolocation_item, parent, false)
        return ViewHolder(view!!)
    }

    override fun getItemCount(): Int  = storage.size()


    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        if (position < storage.size()) {
            val geolocationItem = storage[position]
            holder?.name?.text = geolocationItem.name
            holder?.latitude?.text = context.getString(R.string.s_tw_latitude, geolocationItem.latitude.toFloat())
            holder?.longitude?.text = context.getString(R.string.s_tw_longitude, geolocationItem.longitude.toFloat())
        }
    }

    override fun onClick(v: View?) {
    }


}
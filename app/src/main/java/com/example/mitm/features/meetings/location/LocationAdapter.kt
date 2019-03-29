package com.example.mitm.features.meetings.location

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mitm.R
import kotlinx.android.synthetic.main.location_item.view.*


class LocationAdapter(var addresses: List<Location>, val listItemClickListener: OnListItemClickListener ): RecyclerView.Adapter<LocationAdapter.LocationViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationAdapter.LocationViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.location_item, parent, false)
        return LocationViewHolder(itemView)
    }

    override fun getItemCount(): Int  = addresses.size

    override fun onBindViewHolder(holder: LocationAdapter.LocationViewHolder, position: Int) {
        holder.bindLocationItem(addresses[position])
    }

    inner class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindLocationItem(location: Location) {

            with(location) {

                itemView.primaryAddress.text = primaryAddress
                itemView.secondaryAddress.text = secondaryAddress
                itemView.setOnClickListener {
                    listItemClickListener.onListItemClick(this)
                }
            }
        }

    }

    fun setLocations(locations: List<Location>) {
        this.addresses = locations
        notifyDataSetChanged()
    }

    interface OnListItemClickListener {

        fun onListItemClick(location: Location)
    }


}
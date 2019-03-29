package com.example.mitm.features.meetings.meetingdetail

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.mitm.R
import com.example.mitm.features.meetings.meetingdetail.acceptedmeeting.Place
import kotlinx.android.synthetic.main.suggested_place_item.view.*


class PlacesAdapter(val places: List<Place>, val context: Context): RecyclerView.Adapter<PlacesAdapter.PlaceViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): PlacesAdapter.PlaceViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.suggested_place_item, parent, false)
        return PlaceViewHolder(itemView)

    }

    override fun getItemCount(): Int = places.size

    override fun onBindViewHolder(holder: PlacesAdapter.PlaceViewHolder, position: Int) {
        holder.bindSuggestPlaceItem(places[position])
    }

    inner class PlaceViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bindSuggestPlaceItem(place: Place) {
            Glide.with(context)
                    .load(place.iconUrl)
                    .placeholder(R.drawable.ic_android_black_24dp)
                    .into(itemView.placeImg)

            itemView.placeName.text = place.name
            itemView.placeDistance.text = place.distance
            itemView.placeRating.text = place.rating

        }

    }


}
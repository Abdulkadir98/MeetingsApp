package com.example.mitm.features.meetings

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.mitm.R
import com.example.mitm.data.models.Meeting
import com.example.mitm.data.source.remote.requests.InvitationUpdate
import com.example.mitm.features.utils.DelegateExt
import kotlinx.android.synthetic.main.meeting_item.view.*


class MyMeetingsAdapter(var meetings: MutableList<Meeting>, val listItemClick: (Meeting) -> Unit, val context: Context,
                        val accept: (Int) -> Unit,
                        val decline: (token: String, body: InvitationUpdate) -> Unit) :
        RecyclerView.Adapter<MyMeetingsAdapter.MyMeetingsViewHolder>() {


    val accessToken: String by DelegateExt.stringPreference(context, DelegateExt.ACCESS_TOKEN, DelegateExt.DEFAULT_TOKEN)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyMeetingsAdapter.MyMeetingsViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.meeting_item, parent, false)
        return MyMeetingsViewHolder(itemView)
    }

    override fun getItemCount(): Int  = meetings.size


    override fun onBindViewHolder(holder: MyMeetingsAdapter.MyMeetingsViewHolder, position: Int) {

        holder.bindMeetingItem(meetings[position])
    }


    inner class MyMeetingsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        fun bindMeetingItem(meeting: Meeting) {

            with(meeting) {

                itemView.meetingName.text = meeting_name
                itemView.meetingDate.text = meeting_start_date
                itemView.meetingTime.text = meeting_start_time
                itemView.duration.text = "Duration: $duration hours"
                Glide.with(context)
                        .load(meeting_creator_pic)
                        .circleCrop()
                        .error(R.mipmap.user_6)
                        .into(itemView.inivitedByIv)

                Glide.with(context)
                        .load("")
                        .circleCrop()
                        .error(R.mipmap.user_6)
                        .into(itemView.profileImageIv1)
                Glide.with(context)
                        .load("")
                        .circleCrop()
                        .error(R.mipmap.user_6)
                        .into(itemView.profileImageIv2)
                Glide.with(context)
                        .load("")
                        .circleCrop()
                        .error(R.mipmap.user_6)
                        .into(itemView.profileImageIv3)

                if (profile_pics.isNotEmpty()) {

                    Glide.with(context)
                            .load(profile_pics[0])
                            .circleCrop()
                            .error(R.mipmap.user_6)
                            .into(itemView.profileImageIv1)
                    if (profile_pics.size > 1)
                    Glide.with(context)
                            .load(profile_pics[1])
                            .circleCrop()
                            .error(R.mipmap.user_6)
                            .into(itemView.profileImageIv2)
                    if (profile_pics.size > 2)
                    Glide.with(context)
                            .load(profile_pics[2])
                            .circleCrop()
                            .error(R.mipmap.user_6)
                            .into(itemView.profileImageIv3)
                }




                when(meeting_status) {
                     "admin" -> {
                         itemView.joinMeetingContainer.visibility = View.GONE
                         itemView.meetingCreator.text = "Created By You"
                     }
                    "pending" -> {

                        itemView.joinMeetingContainer.visibility = View.VISIBLE
                        itemView.meetingCreator.text = "Invited By $meeting_creator"
                        itemView.declineMeetingBtn.setOnClickListener {

                            val invitationUpdate = InvitationUpdate(status = "rejected", meeting_id = meeting_id)
                            deleteItem(adapterPosition)
                            decline(accessToken, invitationUpdate)
                        }

                        itemView.joinMeetingBtn.setOnClickListener {

                            accept(meeting_id)
                        }
                    }

                    "accepted" -> {
                        itemView.joinMeetingContainer.visibility = View.GONE
                        itemView.meetingCreator.text = "Invited By $meeting_creator"
                    }
                }


                itemView.numberOfPeople.text = "$accepted  /  $invited members going"
                itemView.setOnClickListener {
                    listItemClick(this)
                }
            }
        }
    }

    private fun deleteItem(position: Int) {

        meetings.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, meetings.size)
    }
}
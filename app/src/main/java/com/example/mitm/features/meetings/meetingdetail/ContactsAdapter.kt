package com.example.mitm.features.meetings.meetingdetail

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.mitm.R
import kotlinx.android.synthetic.main.invited_contact_item.view.*

class ContactsAdapter(val contacts: MutableList<Contact>, val context: Context,
                      val onContactRemovedListener: OnContactRemovedListener?) : RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsAdapter.ContactsViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.invited_contact_item, parent, false)
        return ContactsViewHolder(itemView)
    }

    override fun getItemCount() = contacts.size

    override fun onBindViewHolder(holder: ContactsAdapter.ContactsViewHolder, position: Int) {

        holder.bindContactItem(contacts[position])
    }

    fun addContact(contact: Contact){

        contacts.add(contact)
        notifyDataSetChanged()
    }


    inner class ContactsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindContactItem(contact: Contact) {

            with(contact) {

                if (name.isNotEmpty())
                itemView.contact_name.text = name
                else
                    itemView.contact_name.text = number.toString()
                itemView.contact_status.text = status
                itemView.contact_status.setTextColor(ContextCompat.getColor(context, textColor))

                Glide.with(context)
                        .load(iconUrl)
                        .placeholder(R.drawable.ic_android_black_24dp)
                        .circleCrop()
                        .into(itemView.invitedContactIv)
            }

            itemView.contact_status.setOnClickListener {
                if (itemView.contact_status.text == "Remove") {

                    contacts.remove(contact)
                    notifyDataSetChanged()

                    onContactRemovedListener?.onContactRemoved(contact)
                }
            }

        }
    }


}


interface OnContactRemovedListener {

    fun onContactRemoved(contact: Contact)
}
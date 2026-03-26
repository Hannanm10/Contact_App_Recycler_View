package com.example.contact_app_recycler_view

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ContactAdapter(
    private val contactList: MutableList<Contact>,
    private val listener: OnContactActionListener
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    private var filteredList: MutableList<Contact> = contactList.toMutableList()

    interface OnContactActionListener {
        fun onItemClick(position: Int)
        fun onEditClick(position: Int)
        fun onDeleteClick(position: Int)
    }

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvContactName)
        val tvPhone: TextView = itemView.findViewById(R.id.tvContactPhone)
        val imgProfile: ImageView = itemView.findViewById(R.id.imgProfile)
        val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = filteredList[position]

        holder.tvName.text = contact.name
        holder.tvPhone.text = contact.phone

        if (contact.imageUri != null) {
            holder.imgProfile.setImageURI(Uri.parse(contact.imageUri))
        } else {
            holder.imgProfile.setImageResource(R.drawable.ic_launcher_foreground)
        }

        holder.itemView.setOnClickListener {
            listener.onItemClick(holder.adapterPosition)
        }

        holder.btnEdit.setOnClickListener {
            listener.onEditClick(holder.adapterPosition)
        }

        holder.btnDelete.setOnClickListener {
            listener.onDeleteClick(holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int = filteredList.size

    fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            contactList.toMutableList()
        } else {
            contactList.filter {
                it.name.contains(query, true) ||
                        it.phone.contains(query)
            }.toMutableList()
        }
        notifyDataSetChanged()
    }

    fun sortAscending() {
        filteredList = filteredList.sortedBy { it.name.lowercase() }.toMutableList()
        notifyDataSetChanged()
    }

    fun sortDescending() {
        filteredList = filteredList.sortedByDescending { it.name.lowercase() }.toMutableList()
        notifyDataSetChanged()
    }

    fun updateList(newList: MutableList<Contact>) {
        contactList.clear()
        contactList.addAll(newList)
        filteredList = contactList.toMutableList()
        notifyDataSetChanged()
    }
}
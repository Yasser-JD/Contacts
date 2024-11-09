package com.jdcoding.contacts.adapter

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.Manifest
import android.graphics.drawable.GradientDrawable
import android.util.Log
import com.jdcoding.contacts.R
import com.jdcoding.contacts.contact.Contact

class ContactAdapter(private val context: Context, private val contacts: List<Contact>) :
    RecyclerView.Adapter<ContactAdapter.ContactViewHolder>(), Filterable {

    private var contactsFiltered: MutableList<Contact> = ArrayList(contacts)


    private val letterColors = mapOf(
        'A' to R.color.colorA, 'B' to R.color.colorB, 'C' to R.color.colorC, 'D' to R.color.colorD,
        'E' to R.color.colorE, 'F' to R.color.colorF, 'G' to R.color.colorG, 'H' to R.color.colorH,
        'I' to R.color.colorI, 'J' to R.color.colorJ, 'K' to R.color.colorK, 'L' to R.color.colorL,
        'M' to R.color.colorM, 'N' to R.color.colorN, 'O' to R.color.colorO, 'P' to R.color.colorP,
        'Q' to R.color.colorQ, 'R' to R.color.colorR, 'S' to R.color.colorS, 'T' to R.color.colorT,
        'U' to R.color.colorU, 'V' to R.color.colorV, 'W' to R.color.colorW, 'X' to R.color.colorX,
        'Y' to R.color.colorY, 'Z' to R.color.colorZ
    )

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val contactInitial: TextView = itemView.findViewById(R.id.contactInitial)
        val contactName: TextView = itemView.findViewById(R.id.contactName)
        val contactPhone: TextView = itemView.findViewById(R.id.contactPhone)
        val optionsMenu: View = itemView.findViewById(R.id.optionsMenu)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.contactInitial.text = contact.firstName.first().toString()
        holder.contactName.text = "${contact.firstName} ${contact.secondName}"
        holder.contactPhone.text = contact.phoneNumber

        // Set background color based on the initial letter
        val initialChar = contact.firstName.first().uppercaseChar()
        val colorResId = letterColors[initialChar] ?: R.color.circle_background // Fallback to a default color
        val drawable = holder.contactInitial.background as GradientDrawable
        drawable.setColor(ContextCompat.getColor(context, colorResId))


        holder.optionsMenu.setOnClickListener {
            showPopupMenu(it, contact)
        }

        holder.itemView.setOnLongClickListener {
            val phoneNumber = contact.phoneNumber
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                val callIntent = Intent(Intent.ACTION_CALL).apply {
                    data = Uri.parse("tel:$phoneNumber")

                }
                context.startActivity(callIntent)
            } else {
                Toast.makeText(context, "Permission to call is required.", Toast.LENGTH_SHORT).show()
            }
            true
        }
    }

    override fun getItemCount() = contactsFiltered.size

    // The search still not accurate 
    override fun getFilter(): Filter {
        return object: Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val charString = charSequence?.toString()?.trim() ?: ""
                Log.d("ContactAdapter", "Filtering for: $charString")  // Debug log for search term
                val filteredList : MutableList<Contact> = if (charString.isEmpty()) {
                    ArrayList(contacts)
                } else {
                    contacts.filter { contact ->
                        val fullName = "${contact.firstName} ${contact.secondName}".lowercase()
                        val phoneNumber = contact.phoneNumber
                        // Match the full name, phone number, or any part of it
                        fullName.contains(charString.lowercase()) || phoneNumber.contains(charString)
                    }.toMutableList()
                }
                Log.d("ContactAdapter", "Filtered list size: ${filteredList.size}")  // Debug log for result size
                return FilterResults().apply { values = filteredList }
            }

            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults?) {
                if (filterResults?.values is List<*>) {
                    contactsFiltered = filterResults.values as MutableList<Contact>
                    notifyDataSetChanged()
                } else {
                    Log.e("ContactAdapter", "Unexpected filter results type")
                }
            }
        }
    }

    private fun showPopupMenu(view: View, contact: Contact) {
        val popupMenu = PopupMenu(context, view)
        popupMenu.inflate(R.menu.contact_options_menu)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_call -> {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                        val callIntent = Intent(Intent.ACTION_CALL).apply {
                            data = Uri.parse("tel:${contact.phoneNumber}")
                        }
                        context.startActivity(callIntent)
                    } else {
                        Toast.makeText(context, "Permission to call is required.", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                R.id.menu_message -> {
                    val smsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:${contact.phoneNumber}"))
                    context.startActivity(smsIntent)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }
}

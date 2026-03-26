package com.example.contact_app_recycler_view

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity(), ContactAdapter.OnContactActionListener {

    private lateinit var etName: EditText
    private lateinit var etPhone: EditText
    private lateinit var btnSave: Button
    private lateinit var btnLoadContacts: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var btnAsc: Button
    private lateinit var btnDesc: Button
    private lateinit var btnSelectImage: Button

    private lateinit var adapter: ContactAdapter
    private val contactList = mutableListOf<Contact>()

    private var selectedImageUri: Uri? = null

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            selectedImageUri = uri
            Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etName = findViewById(R.id.etName)
        etPhone = findViewById(R.id.etPhone)
        btnSave = findViewById(R.id.btnSave)
        btnLoadContacts = findViewById(R.id.btnLoadContacts)
        recyclerView = findViewById(R.id.recyclerViewContacts)
        searchView = findViewById(R.id.searchView)
        btnAsc = findViewById(R.id.btnAsc)
        btnDesc = findViewById(R.id.btnDesc)
        btnSelectImage = findViewById(R.id.btnSelectImage)

        adapter = ContactAdapter(contactList, this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnSelectImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString()
            val phone = etPhone.text.toString()

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Enter all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            contactList.add(Contact(name, phone, selectedImageUri?.toString()))
            adapter.updateList(contactList)

            selectedImageUri = null
            etName.text.clear()
            etPhone.text.clear()
        }

        btnLoadContacts.setOnClickListener {
            loadContacts()
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText ?: "")
                return true
            }
        })

        btnAsc.setOnClickListener { adapter.sortAscending() }
        btnDesc.setOnClickListener { adapter.sortDescending() }
    }

    override fun onItemClick(position: Int) {}

    override fun onEditClick(position: Int) {
        val contact = contactList[position]

        val view = LayoutInflater.from(this).inflate(R.layout.activity_dialog_edit_item, null)
        val etEditName = view.findViewById<EditText>(R.id.etEditName)
        val etEditPhone = view.findViewById<EditText>(R.id.etEditPhone)
        val btnChangeImage = view.findViewById<Button>(R.id.btnChangeImage)

        etEditName.setText(contact.name)
        etEditPhone.setText(contact.phone)

        btnChangeImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        AlertDialog.Builder(this)
            .setView(view)
            .setPositiveButton("Update") { _, _ ->
                contact.name = etEditName.text.toString()
                contact.phone = etEditPhone.text.toString()
                if (selectedImageUri != null) {
                    contact.imageUri = selectedImageUri.toString()
                }
                adapter.updateList(contactList)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDeleteClick(position: Int) {
        contactList.removeAt(position)
        adapter.updateList(contactList)
    }

    private fun loadContacts() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), 1)
            return
        }

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null, null
        )

        cursor?.use {
            contactList.clear()
            while (it.moveToNext()) {
                val name = it.getString(
                    it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                )
                val phone = it.getString(
                    it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                )
                contactList.add(Contact(name, phone))
            }
        }

        adapter.updateList(contactList)
    }
}
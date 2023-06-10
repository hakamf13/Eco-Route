package com.capstone.eco_route.datasource

import com.google.firebase.database.FirebaseDatabase

object DatabaseReference {

    private val references = FirebaseDatabase.getInstance()
        .reference.child("datasource")

    fun getDatabaseReference() = references
}
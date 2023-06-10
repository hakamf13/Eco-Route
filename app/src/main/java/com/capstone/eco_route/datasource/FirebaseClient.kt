package com.capstone.eco_route.datasource

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.capstone.eco_route.datasource.model.UserModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class FirebaseClient {

    private val reference = DatabaseReference.getDatabaseReference()
    val idTrip = reference.push().key

    private val _isSuccess = MutableLiveData<Boolean>()
    val isSuccess: LiveData<Boolean> = _isSuccess

    private val _username = MutableLiveData<String>()
    val username: LiveData<String> = _username

    private val _user = MutableLiveData<UserModel>()
    val user: LiveData<UserModel> = _user

    private val _password = MutableLiveData<String>()
    val password: LiveData<String> = _password


    fun authRegister(
        context: Context,
        userModel: UserModel,
        userId: String
    ) {

        val userReference = reference.child("userdata")
        userReference.child(userId).setValue(userModel).addOnCompleteListener {
            _isSuccess.value = true
            Toast.makeText(context, "Authentication Success", Toast.LENGTH_LONG).show()
        }

    }

    fun authLogin(userId: String) {

        val userReference = reference.child("userdata")
        val userQuery = userReference.orderByChild("userId").equalTo(userId)
        userQuery.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userM = UserModel(
                        userId = userId,
                        userEmail = snapshot.child(userId).child("userEmail")
                            .getValue(String::class.java).toString(),
                        userName = snapshot.child(userId).child("userName")
                            .getValue(String::class.java).toString(),
                        userPassword = snapshot.child(userId).child("userPassword")
                            .getValue(String::class.java).toString()
                    )
                    _user.postValue(userM)
                } else {
                    Log.w("Login Query", "Snapshot is not exists")
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

}
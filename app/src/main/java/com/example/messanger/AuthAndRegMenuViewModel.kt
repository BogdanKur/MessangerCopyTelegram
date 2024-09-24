package com.example.messanger

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
class AuthAndRegMenuViewModel: ViewModel() {
    fun addNewUser(etNumber: String) {
        AuthAndRegMenuFragment.user.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var maxUserId = 0
                for(child in snapshot.children) {
                    val userId = child.child("userId").getValue<Int?>()
                    if(userId != null && userId > maxUserId) {
                        maxUserId = userId
                    }
                }

                val newUser = UserOfMessanger(etNumber, maxUserId+1)
                AuthAndRegMenuFragment.user.child((maxUserId+1).toString()).setValue(newUser)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("Firebase", "User added successfully")
                        } else {
                            Log.w("Firebase", "Error adding user", task.exception)
                        }
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("FirebaseReg", "Error getting users", error.toException())
            }

        })
    }

}
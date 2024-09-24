package com.example.messanger

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import com.example.messanger.AuthAndRegMenuFragment.Companion.numberOfAuthUser
import com.example.messanger.YourProfileFragment.Companion.aboutYourSelves
import com.example.messanger.YourProfileFragment.Companion.currentUserId
import com.example.messanger.YourProfileFragment.Companion.name
import com.example.messanger.YourProfileFragment.Companion.phoneNumber
import com.example.messanger.YourProfileFragment.Companion.surname
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val  databaseRef = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            phoneNumber = currentUser.phoneNumber
            if (phoneNumber != null) {
                val userRef = databaseRef.child("user")

                userRef.orderByChild("number").equalTo(phoneNumber)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                for (userSnapshot in snapshot.children) {
                                    currentUserId = userSnapshot.key!!.toInt()
                                    name = userSnapshot.child("name").getValue<String?>().toString()
                                    surname = userSnapshot.child("surname").getValue<String?>().toString()
                                    aboutYourSelves = userSnapshot.child("aboutYourSelves").getValue<String?>().toString()
                                }
                            } else {
                                Log.e("userId", "Пользователь не найден")
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("DatabaseError", error.message)
                        }
                    })
            }
        }
        if (currentUser != null) {
            navController.navigate(R.id.action_to_messageMainFragment)
        } else {
            navController.navigate(R.id.action_to_startMenuAuthAndRegFragment)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return item.onNavDestinationSelected(navController)
                || super.onOptionsItemSelected(item)
    }
}
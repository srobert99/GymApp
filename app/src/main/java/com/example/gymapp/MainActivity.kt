package com.example.gymapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import com.bumptech.glide.Glide
import com.example.gymapp.databinding.ActivityMainBinding
import com.example.gymapp.firebase_auth.LoginActivity
import com.example.gymapp.menu.profile.ProfileActivity
import com.example.gymapp.menu.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private val usersCollectionRef = Firebase.firestore.collection("Users")
    private lateinit var mStoreRef: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.myToolbar)
        auth = FirebaseAuth.getInstance()
        mStoreRef = FirebaseStorage.getInstance().getReference()

        binding.logoutCV.setOnClickListener {
            logout()
        }

        binding.profileCV.setOnClickListener {
            startActivity(Intent(this@MainActivity, ProfileActivity::class.java))
        }

    }

    private fun loading() {
        if (binding.progressBar2.isGone) {
            binding.menuViews.visibility = View.GONE
            binding.progressBar2.visibility = View.VISIBLE
        } else {
            binding.menuViews.visibility = View.VISIBLE
            binding.progressBar2.visibility = View.GONE
        }
    }


    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        finish()
    }

    override fun onStart() {
        loading()
        super.onStart()
        val id = auth.currentUser!!.uid
        val db = usersCollectionRef.document(id)

        db.get().addOnSuccessListener { documentSnapshot ->
            val user = documentSnapshot.toObject<User>()
            binding.mainUserNick.setText(user?.nickname)
            binding.balance.setText(getString(R.string.balance, user?.balance))
            mStoreRef.child("images").child("profile")
                .child(id).downloadUrl.addOnSuccessListener { uri ->
                Glide.with(this@MainActivity)
                    .load(uri)
                    .into(binding.profileImage)
            }
            if(user!!.uid.isEmpty()){
                db.update("uid",id)
            }
            loading()
        }.addOnFailureListener {
            Toast.makeText(this@MainActivity, "Error getting user data", Toast.LENGTH_SHORT).show()
            loading()
        }

    }



}
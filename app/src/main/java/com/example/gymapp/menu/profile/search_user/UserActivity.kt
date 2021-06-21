package com.example.gymapp.menu.profile.search_user

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.example.gymapp.Helpers
import com.example.gymapp.databinding.ActivityUserBinding
import com.example.gymapp.firebase_database.FDB
import com.example.gymapp.local_data_base.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

class UserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserBinding
    private val usersCollectionRef = Firebase.firestore.collection("Users")
    private lateinit var mStoreRef: StorageReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)


        mStoreRef = FirebaseStorage.getInstance().getReference()
        binding.backButton.setOnClickListener {
            finish()
        }

    }

    override fun onStart() {
        super.onStart()
        val uid = intent.getStringExtra("userUID")
        if(uid!=null){
        val FDBD = FDB(uid,this@UserActivity)
        }
    }


     fun setProfileView(user: User) {
        binding.userNickTV.text = user.nickname
        binding.userNameTV.text = user.name
        binding.userAgeTV.text = user.birthDate + " " + (Helpers.getAge(user.birthDate).toString())
        if (user.instagram.isNotEmpty()) {
            binding.userInstaTV.text = user.instagram
        } else {
            binding.userInstaTV.visibility = View.GONE
        }
    }


     fun setImages(uid: String) {
        mStoreRef.child("images").child("profile")
            .child(uid).downloadUrl.addOnSuccessListener { uri ->
                Glide.with(this)
                    .load(uri)
                    .into(binding.userProfileImage)
            }.addOnFailureListener { listener ->
            }

        mStoreRef.child("images").child("cover")
            .child(uid).downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this)
                .load(uri)
                .into(binding.userCoverImage)
        }.addOnFailureListener { listener ->
        }

    }
}
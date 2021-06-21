package com.example.gymapp.firebase_database

import android.content.ContentValues.TAG
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.gymapp.local_data_base.User
import com.example.gymapp.menu.profile.ProfileActivity
import com.example.gymapp.menu.profile.search_user.UserActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.delay
import kotlin.concurrent.timerTask

class FDB(var uid: String, var context: Context) {
    var usersCollectionRef = Firebase.firestore.collection("Users")
    var storageReference: StorageReference? = null


    init {
        storageReference = FirebaseStorage.getInstance().getReference()
        getUser()
        getImages()
    }


    private fun getUser() {
        usersCollectionRef.document(uid).get().addOnSuccessListener { doc ->
            val user = doc.toObject<User>()!!

            if (context is ProfileActivity) {
                (context as ProfileActivity).setProfileView(user)
            }else if(context is UserActivity){
                (context as UserActivity).setProfileView(user)
            }
        }.addOnFailureListener { error ->
            Log.d(TAG, error.message.toString())
        }
    }

    private fun getImages() {
        storageReference!!.child("images").child("profile")
            .child(uid).downloadUrl.addOnSuccessListener { uri ->
                val profilePicture = uri
                storageReference!!.child("images").child("cover")
                    .child(uid).downloadUrl.addOnSuccessListener { uri2 ->
                    val coverPicture = uri2
                    if (context is ProfileActivity) {
                        (context as ProfileActivity).setImages(profilePicture, coverPicture)
                    }else if(context is UserActivity) {
                        (context as UserActivity).setImages(uid)
                    }
                }
            }.addOnFailureListener { error ->
                Log.d(TAG, error.message.toString())
            }
    }

    fun updateUser(user:User){
        val DB = usersCollectionRef.document(uid)
        DB.set(user)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Succes", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        context,
                        task.exception?.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }


    fun uploadPictures(requestCode:Int,file:Uri){
        var child1 = ""
        if (requestCode == 1) {
            child1 = "profile"
        } else {
            child1 = "cover"
        }
        var profilePic = storageReference!!.child("images").child(child1).child(uid)
        if (file != null) {
            profilePic.putFile(file!!)
        }
    }


}
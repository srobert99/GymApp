package com.example.gymapp.menu.profile.search_user

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.example.gymapp.R
import com.example.gymapp.databinding.ActivityProfileBinding
import com.example.gymapp.databinding.ActivitySearchUserBinding
import com.example.gymapp.databinding.ActivityUserBinding
import com.example.gymapp.menu.User
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
        if(uid?.isNotEmpty()==true){
            usersCollectionRef.document(uid).get().addOnSuccessListener { doc->
                val user = doc.toObject<User>()!!
                setView(user)
            }
            setImages(uid)
        }
    }


    private fun setView(user:User){
        binding.userNickTV.text=user.nickname
        binding.userNameTV.text=user.name
        binding.userAgeTV.text = getAge(user)
        if(user.instagram.isNotEmpty()){
            binding.userInstaTV.text= user.instagram
        }else{
            binding.userInstaTV.visibility= View.GONE
        }
    }


    private fun getAge(user: User): String {
        val aux = user.birthDate.split("/")
        val c = Calendar.getInstance()
        val y = c.get(Calendar.YEAR)
        val m = c.get(Calendar.MONTH)
        val d = c.get(Calendar.DAY_OF_MONTH)

        var ydiff = y - aux[2].toInt()

        if (aux[1].toInt() > m) {
            ydiff--
        } else if (aux[1].toInt() == m) {
            if (aux[0].toInt() > d) {
                ydiff--
            }
        }
        return ydiff.toString()
    }


    private fun setImages(uid: String){
        mStoreRef.child("images").child("profile")
            .child(uid).downloadUrl.addOnSuccessListener { uri ->
                Glide.with(this)
                    .load(uri)
                    .into(binding.userProfileImage)
            }.addOnFailureListener { listener ->
            }

        mStoreRef.child("images").child("cover").child(uid).downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this)
                .load(uri)
                .into(binding.userCoverImage)
        }.addOnFailureListener { listener ->
        }

    }
}
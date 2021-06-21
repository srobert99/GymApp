package com.example.gymapp.menu.profile

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.gymapp.Helpers
import com.example.gymapp.R
import com.example.gymapp.databinding.ActivityProfileBinding
import com.example.gymapp.firebase_database.FDB
import com.example.gymapp.local_data_base.User
import com.example.gymapp.menu.profile.search_user.SearchUserActivity
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.*
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private var editable = false
    private var balance = 0
    private lateinit var FBDB: FDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        loading()
        FBDB = FDB(auth.currentUser!!.uid, this@ProfileActivity)


        binding.editPB.setOnClickListener {
            updateUser()
        }
        binding.rBirthdateTV.setOnClickListener {
            if (editable) {
                selectDate()
            }
        }
        binding.profileImage.setOnClickListener {
            if (editable) {
                pickImage1()
            }
        }

        binding.coverImage.setOnClickListener {
            if (editable) {
                pickImage2()
            }
        }

        binding.searchUserTV.setOnClickListener {
            startActivity(Intent(this, SearchUserActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }


    }

    private fun selectDate() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(this@ProfileActivity, { view, year, monthOfYear, dayOfMonth ->
            val mAux = monthOfYear + 1
            binding.rBirthdateTV.text = ("" + dayOfMonth + "/" + monthOfYear + "/" + year)
        }, year, month, day)

        dpd.show()
    }


    fun setProfileView(user: User) {
        binding.nickET.setText(user!!.nickname)
        binding.nameET.setText(user!!.name)
        binding.surnameET.setText(user!!.surname)
        binding.phoneNumberET.setText(user!!.phoneNumber)
        binding.rBirthdateTV.setText(
            user!!.birthDate + " (" + Helpers.getAge(user.birthDate).toString() + ")"
        )

        if (user.instagram!!.isNotEmpty()) {
            binding.instaET.setText(user!!.instagram)
            binding.instaET.visibility = View.VISIBLE
        }
    }


    private fun updateUser() {
        if (binding.editPB.text == "EDIT PROFILE") {
            editable = true
            binding.editPB.text = "CONFIRM"
            binding.nameET.isEnabled = true
            binding.surnameET.isEnabled = true
            binding.nickET.isEnabled = true
            binding.instaET.isEnabled = true
            binding.instaET.visibility = View.VISIBLE
            binding.phoneNumberET.visibility = View.VISIBLE
        } else {
            editable = false
            binding.editPB.text = "EDIT PROFILE"
            binding.nameET.isEnabled = false
            binding.surnameET.isEnabled = false
            binding.nickET.isEnabled = false
            binding.instaET.isEnabled = false
            binding.instaET.visibility = View.GONE
            binding.phoneNumberET.visibility = View.GONE
            updateUserDB()
        }
    }


    private fun updateUserDB() {
        val id = auth.currentUser!!.uid
        val nickname = binding.nickET.text.toString()
        val name = binding.nameET.text.toString()
        val surname = binding.surnameET.text.toString()
        val phoneNumber = binding.phoneNumberET.text.toString()
        val birthdate = binding.rBirthdateTV.text.toString().split(" ")
        var insta = ""

        if (binding.instaET.text.toString().isNotEmpty()) {
            insta = binding.instaET.text.toString()
            binding.instaET.visibility = View.VISIBLE
        }

        FBDB.updateUser(User(nickname, name, surname,birthdate[0],phoneNumber,insta,balance,uid=id))


    }

    private fun pickImage1() {
        ImagePicker.with(this@ProfileActivity)
            .galleryOnly()
            .compress(1024)
            .crop()
            .maxResultSize(1080, 1080)
            .start(1)
    }

    private fun pickImage2() {
        ImagePicker.with(this@ProfileActivity)
            .galleryOnly()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .crop(17f, 9.3f)
            .start(2)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 1) {
            val profileImage = data!!.data
            if (profileImage != null) {
                binding.profileImage.setImageURI(profileImage)
                FBDB.uploadPictures(requestCode,profileImage)
            }
        } else if (resultCode == RESULT_OK && requestCode == 2) {
            val coverImage = data!!.data
            if (coverImage != null) {
                binding.coverImage.setImageURI(coverImage)
                FBDB.uploadPictures(requestCode,coverImage)
            }
        }
    }


    fun setImages(profilePicture: Uri, coverImage: Uri) {
        Glide.with(this)
            .load(profilePicture)
            .into(binding.profileImage)

        Glide.with(this)
            .load(coverImage)
            .into(binding.coverImage)
        loading()
    }


    private fun loading() {
        if (binding.progressBar3.isVisible) {
            binding.progressBar3.visibility = View.VISIBLE
            binding.profileLayout.visibility = View.INVISIBLE
        } else {
            binding.progressBar3.visibility = View.GONE
            binding.profileLayout.visibility = View.VISIBLE
        }
    }


}







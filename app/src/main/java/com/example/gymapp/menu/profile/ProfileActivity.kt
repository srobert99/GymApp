package com.example.gymapp.menu.profile

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.DatePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.gymapp.R
import com.example.gymapp.databinding.ActivityProfileBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.okhttp.Dispatcher
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.lang.Exception
import java.lang.Math.abs
import java.text.SimpleDateFormat
import java.time.Year
import java.util.*
import javax.xml.datatype.DatatypeConstants.MONTHS

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private val usersCollectionRef = Firebase.firestore.collection("Users")
    private var fileUri: Uri? = null
    private lateinit var mStoreRef: StorageReference
    private var editImage = false
    private var balance = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            finish()
        }


        auth = FirebaseAuth.getInstance()
        mStoreRef = FirebaseStorage.getInstance().getReference()


        binding.editPB.setOnClickListener {
            updateUser()
        }

        binding.rBirthdateTV.setOnClickListener {
            if (editImage) {
                selectDate()
            }
        }
        binding.profileImage.setOnClickListener {
            if (editImage) {
                pickImage1()
            }
        }

        binding.coverImage.setOnClickListener {
            if (editImage) {
                pickImage2()
            }
        }


    }

    private fun selectDate() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(this@ProfileActivity, { view, year, monthOfYear, dayOfMonth ->
            val mAux = monthOfYear+1
            binding.rBirthdateTV.text=("" + dayOfMonth + "/" + monthOfYear + "/" + year)
        }, year, month, day)

        dpd.show()
    }


    private fun setProfileView(user: User) {
        binding.nickET.setText(user.nickname)
        binding.nameET.setText(user.name)
        binding.surnameET.setText(user.surname)
        binding.phoneNumberET.setText(user.phoneNumber)
        binding.rBirthdateTV.setText(user.birthDate + " (" + getAge(user) + ")")

        if (user.instagram!!.isNotEmpty()) {
            binding.instaET.setText(user.instagram)
            binding.instaET.visibility = View.VISIBLE
        }
    }


    private fun updateUser() {
        if (binding.editPB.text == "EDIT PROFILE") {
            editImage = true
            binding.editPB.text = "CONFIRM"
            binding.nameET.isEnabled = true
            binding.surnameET.isEnabled = true
            binding.nickET.isEnabled = true
            binding.instaET.isEnabled = true
            binding.instaET.visibility = View.VISIBLE
            binding.phoneNumberET.visibility = View.VISIBLE
        } else {
            editImage = false
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


    private fun updateUserDB() {
        val id = auth.currentUser!!.uid
        val DB = usersCollectionRef.document(id)
        val nickname = binding.nickET.text.toString()
        val name = binding.nameET.text.toString()
        val surname = binding.surnameET.text.toString()
        val phoneNumber = binding.phoneNumberET.text.toString()
        val birthdate = binding.rBirthdateTV.text.toString().split(" ")
        var insta = ""

        if (binding.instaET.text.toString().isNotEmpty()) {
            insta = binding.instaET.text.toString()
        }

        DB.set(User(nickname, name, surname, birthdate[0], phoneNumber, insta, balance))
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this@ProfileActivity, "Succes", Toast.LENGTH_SHORT).show()
                    getData()
                    getImages()
                } else {
                    Toast.makeText(
                        this@ProfileActivity,
                        task.exception?.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }


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
            fileUri = data!!.data
            binding.profileImage.setImageURI(fileUri)
            uploadPicture(requestCode)

        } else if (resultCode == RESULT_OK && requestCode == 2) {
            fileUri = data!!.data
            binding.coverImage.setImageURI(fileUri)
            uploadPicture(requestCode)
        }
    }


    private fun uploadPicture(request: Int) {
        var child1 = ""
        val id = auth.currentUser!!.uid
        if (request == 1) {
            child1 = "profile"
        } else {
            child1 = "cover"
        }
        var profilePic = mStoreRef.child("images").child(child1).child(id)

        if (fileUri != null) {
            profilePic.putFile(fileUri!!)
        }
    }

    private fun getData() {
        val id = auth.currentUser!!.uid
        val DB = usersCollectionRef.document(id)

        DB.get().addOnSuccessListener { documentSnapshot ->
            val user = documentSnapshot.toObject<User>()!!
            balance = user.balance
            setProfileView(user)
        }.addOnFailureListener { error ->
            Toast.makeText(this@ProfileActivity, error.message, Toast.LENGTH_SHORT).show()
        }

    }

    private fun getImages() {
        val id = auth.currentUser!!.uid
        mStoreRef.child("images").child("profile")
            .child(id).downloadUrl.addOnSuccessListener { uri ->
                Glide.with(this)
                    .load(uri)
                    .into(binding.profileImage)
            }.addOnFailureListener { listener ->
            }

        mStoreRef.child("images").child("cover").child(id).downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this)
                .load(uri)
                .into(binding.coverImage)
        }.addOnFailureListener { listener ->
        }
    }


    private fun loading() {
        if (!binding.progressBar3.isVisible) {
            binding.progressBar3.visibility = View.VISIBLE
            binding.profileLayout.visibility = View.INVISIBLE
        } else {
            binding.progressBar3.visibility = View.GONE
            binding.profileLayout.visibility = View.VISIBLE
        }
    }


    override fun onStart() {
        super.onStart()
        loading()
        getImages()
        getData()
        loading()

    }


}







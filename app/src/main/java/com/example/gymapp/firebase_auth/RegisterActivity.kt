package com.example.gymapp.firebase_auth

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gymapp.databinding.ActivityRegisterBinding
import com.example.gymapp.local_data_base.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private val usersCollectionRef = Firebase.firestore.collection("Users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.registerButton.setOnClickListener {
            registerUser()
        }

        binding.loginTV.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.birthdateTV.setOnClickListener {
            chooseDate()
        }


    }

    private fun chooseDate() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(
            this@RegisterActivity,
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                val mAux = monthOfYear+1
                binding.birthdateTV.setText("" + dayOfMonth + "/" + mAux + "/" + year)
            },
            year,
            month,
            day
        )

        dpd.show()
    }

    private fun verif(): Boolean {
        for (i in 0..binding.rElements.childCount) {
            if (binding.rElements.getChildAt(i) is EditText) {
                if ((binding.rElements.getChildAt(i) as EditText).text.toString().isEmpty()) {
                    return false
                }
            }
        }
        if (binding.passwordET.text.toString() != binding.confirmPasswordET.text.toString()) {
            Toast.makeText(this@RegisterActivity, "Passwords don t match", Toast.LENGTH_SHORT)
                .show()
            return false
        }
        return true
    }

    private fun registerUser() {
        if (verif()) {
            val email = binding.emailET.text.toString()
            val password = binding.passwordET.text.toString()

            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val id = auth.currentUser!!.uid
                    usersCollectionRef.document(id).set(saveUserData())
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                startActivity(
                                    Intent(
                                        this@RegisterActivity,
                                        LoginActivity::class.java
                                    )
                                )
                                finish()
                            }
                        }

                } else {
                    Toast.makeText(
                        this@RegisterActivity,
                        task.exception?.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun saveUserData(): User {
        val name = binding.nameET.text.toString()
        val surname = binding.surnameET.text.toString()
        val birthDate = binding.birthdateTV.text.toString()
        val nickname = binding.nicknameET.text.toString()
        val phoneNumber = binding.phoneET.text.toString()

        return User(nickname, name, surname, birthDate, phoneNumber)
    }


}
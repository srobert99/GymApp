package com.example.gymapp.firebase_auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.gymapp.MainActivity
import com.example.gymapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.loginB.setOnClickListener {
            login()
        }

        binding.registerTV.setOnClickListener {
            register()
        }
    }


    private fun login() {
        val email = binding.lMailET.text.toString()
        val password = binding.lPasswordET.text.toString()

        loading()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        task.exception?.message.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                    loading()
                }
            }
        } else {
            Toast.makeText(this@LoginActivity, "Password/email is empty", Toast.LENGTH_SHORT).show()
            loading()
        }
    }

    private fun register() {
        startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        finish()
    }

    private fun loading() {
        if (binding.loginB.isVisible) {
            binding.progressBar.visibility = View.VISIBLE
            binding.loginB.visibility = View.INVISIBLE
            binding.registerTV.visibility = View.INVISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
            binding.loginB.visibility = View.VISIBLE
            binding.registerTV.visibility = View.VISIBLE
        }
    }


    override fun onStart() {
        super.onStart()
        if (auth.currentUser?.uid != null) {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }
    }


}
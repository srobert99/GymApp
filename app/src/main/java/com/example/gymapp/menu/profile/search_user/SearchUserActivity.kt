package com.example.gymapp.menu.profile.search_user

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Adapter
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gymapp.R
import com.example.gymapp.databinding.ActivityMainBinding
import com.example.gymapp.databinding.ActivitySearchUserBinding
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class SearchUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchUserBinding
    private val usersCollectionRef = Firebase.firestore.collection("Users")
    private lateinit var mStoreRef: StorageReference
    private var users = mutableListOf<com.example.gymapp.menu.User>()
    private lateinit var adapter: UserSearchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mStoreRef = FirebaseStorage.getInstance().getReference()
        getUsers()
        binding.searchUserView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }
        })

        binding.profileTV.setOnClickListener {
            finish()
        }

    }


    private fun getUsers() {
        usersCollectionRef.get().addOnSuccessListener { doc ->
            for (document in doc) {
                users.add(document.toObject())
            }
            if (users.size > 0) {
                initRecyclerView()
                binding.noUsersTV.visibility = View.GONE
            }
        }
    }


    private fun initRecyclerView() {
        adapter = UserSearchAdapter(this, users)
        binding.usersRV.layoutManager = LinearLayoutManager(this)
        binding.usersRV.adapter = adapter
    }

    fun showUser(user:com.example.gymapp.menu.User){
        val intent = Intent(this@SearchUserActivity,UserActivity::class.java)
        intent.putExtra("userUID",user.uid)
        startActivity(intent)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
    }

}


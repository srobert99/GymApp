package com.example.gymapp.menu.profile.search_user

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gymapp.R
import com.example.gymapp.menu.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.*


class UserSearchAdapter(val context: Context, val users: MutableList<User>) :
    RecyclerView.Adapter<UserSearchAdapter.UserSearchViewHolder>(), Filterable {

    private var auxUsers = mutableListOf<User>()

    init{
        auxUsers = users
    }


    class UserSearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserSearchViewHolder {
        return UserSearchViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.profile_view,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: UserSearchViewHolder, position: Int) {
        val curUser = auxUsers.get(position)
        val document = FirebaseStorage.getInstance().getReference().child("images").child("profile").child(curUser.uid)

        holder.itemView.findViewById<TextView>(R.id.sUserName).setText(curUser.name + " " + curUser.surname)
        holder.itemView.findViewById<TextView>(R.id.sUserNickname).text = curUser.nickname
        document.downloadUrl.addOnSuccessListener {uri->
            Glide.with(context)
                .load(uri)
                .into(holder.itemView.findViewById<ImageView>(R.id.sProfileImage))
        }.addOnFailureListener { e->
            Log.d(TAG,e.message.toString())
        }

    }



    override fun getItemCount(): Int {
        return auxUsers.size
    }

    @ExperimentalStdlibApi
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if(charSearch.isEmpty()) {
                    auxUsers = users
                }else{
                    val resultList = mutableListOf<User>()
                    for(user in users) {
                        if(user.nickname.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))){
                            resultList.add(user)
                        }
                    }
                    auxUsers = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = auxUsers
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                auxUsers = results?.values as ArrayList<User>
                notifyDataSetChanged()
            }
        }
    }


}
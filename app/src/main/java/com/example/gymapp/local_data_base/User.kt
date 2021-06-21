package com.example.gymapp.local_data_base


data class User(
    var nickname: String = "",
    var name: String = "",
    var surname: String = "",
    var birthDate: String = "",
    var phoneNumber: String = "",
    var instagram: String = "",
    var balance: Int = 0,
    var uid:String=""
)

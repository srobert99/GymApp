package com.example.gymapp

import com.example.gymapp.local_data_base.User
import java.util.*

object Helpers {

    fun getAge(date: String): Int {
        val aux = date.split("/")
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
        return ydiff
    }
}
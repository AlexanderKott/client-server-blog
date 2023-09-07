package ru.kot1.demo.api

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build

fun checkInternetConnection(context: Context): Boolean {
    val connectivity =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (connectivity == null) {
        return false
    } else if (Build.VERSION.SDK_INT >= 21) {
        val info = connectivity.allNetworks
        if (info != null) {
            for (i in info.indices) {
                if (info[i] != null && connectivity.getNetworkInfo(info[i])!!.isConnected) {
                    return true
                }
            }
        }
    } else {
        val info = connectivity.allNetworkInfo
        if (info != null) {
            for (i in info.indices) {
                if (info[i].state == NetworkInfo.State.CONNECTED) {
                    return true
                }
            }
        }
        val activeNetwork = connectivity.activeNetworkInfo
        if (activeNetwork != null && activeNetwork.isConnected) {
            return true
        }
    }
    return false
}
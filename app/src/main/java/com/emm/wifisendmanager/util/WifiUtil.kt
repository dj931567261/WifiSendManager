package com.emm.wifisendmanager.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager

/**
 * @author:dengjie
 * @time:2021/12/31
 * @description:wifi工具类
 **/
object WifiUtil {
    fun getWifiIp(context: Context): String? {
        val wifimanager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifimanager.connectionInfo
        return if (wifiInfo != null) {
            intToIp(wifiInfo.ipAddress)
        } else null
    }

    private fun intToIp(i: Int): String? {
        return (i and 0xFF).toString() + "." + (i shr 8 and 0xFF) + "." + (i shr 16 and 0xFF) + "." + (i shr 24 and 0xFF)
    }

    fun getWifiState(context: Context): Int {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiManager?.wifiState ?: WifiManager.WIFI_STATE_DISABLED
    }

    fun getWifiConnectState(context: Context): NetworkInfo.State? {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val mWiFiNetworkInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        return if (mWiFiNetworkInfo != null) {
            mWiFiNetworkInfo.state
        } else NetworkInfo.State.DISCONNECTED
    }
}
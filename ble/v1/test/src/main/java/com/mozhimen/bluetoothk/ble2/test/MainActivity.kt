package com.mozhimen.bluetoothk.ble2.test

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mozhimen.bluetoothk.ble.test.databinding.ActivityMainBinding
import com.mozhimen.kotlin.utilk.android.content.startContext
import com.mozhimen.uik.databinding.bases.viewdatabinding.activity.BaseActivityVDB

class MainActivity : BaseActivityVDB<ActivityMainBinding>() {
    fun startServer(view: View) {
        startContext<ServerActivity>()
    }

    fun startClient(view: View) {
        startContext<ClientActivity>()
    }
}
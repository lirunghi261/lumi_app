package com.example.personaldashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.example.personaldashboard.data.DashboardRepository
import com.example.personaldashboard.ui.DashboardApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val controller = remember { DashboardController(DashboardRepository(applicationContext)) }
            DashboardApp(controller)
        }
    }
}

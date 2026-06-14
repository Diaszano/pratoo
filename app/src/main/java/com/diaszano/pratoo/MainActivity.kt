package com.diaszano.pratoo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.diaszano.pratoo.ui.navigation.PratooNavGraph
import com.diaszano.pratoo.ui.theme.PratooTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PratooTheme {
                PratooNavGraph()
            }
        }
    }
}

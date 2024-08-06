package com.ntt.jin.skywaycomposequickstart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ntt.jin.skywaycomposequickstart.ui.theme.SkyWayComposeQuickStartTheme

class MainActivity : ComponentActivity() {
    private val mainViewModel = MainViewModel(applicationContext)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        installSplashScreen().let { splashScreen ->
            splashScreen.setKeepOnScreenCondition {
                !mainViewModel.skyWayInitialized
            }
        }
        setContent {
            SkyWayComposeQuickStartTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LiveChatScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SkyWayComposeQuickStartTheme {
        Greeting("Android")
    }
}
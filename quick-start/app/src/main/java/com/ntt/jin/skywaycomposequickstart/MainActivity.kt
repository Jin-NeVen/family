package com.ntt.jin.skywaycomposequickstart

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
    private val mainViewModel: MainViewModel by viewModels()
    private val checkPermissionsUseCase: CheckPermissionsUseCase = CheckPermissionsUseCase()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().let { splashScreen ->
            splashScreen.setKeepOnScreenCondition {
                !mainViewModel.skyWayInitialized
            }
        }
        checkPermissionsUseCase(this, listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ))

        mainViewModel.initializeSkyWayContext()
        setContent {
            SkyWayComposeQuickStartTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (mainViewModel.skyWayInitialized) {
                        LiveChatScreen(
                            modifier = Modifier.padding(innerPadding)
                        )
                    } else {
                        Text("SkyWayContext initializing...")
                    }
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
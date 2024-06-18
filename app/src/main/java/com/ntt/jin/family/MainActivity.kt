package com.ntt.jin.family

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ntt.jin.family.data.AuthTokenRepository
import com.ntt.jin.family.ui.HomeViewModel
import com.ntt.jin.family.ui.theme.FamilyTheme
import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch



class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels { HomeViewModel.Factory }


    @Composable
    fun FamilyApplicationProvider(content: @Composable () -> Unit) {
        CompositionLocalProvider(LocalAppContext provides applicationContext) {
            content()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeViewModel.setupSkyWayContext(applicationContext)
            }
        }
        setContent {
            FamilyTheme {
                FamilyApplicationProvider {
                    FamilyApp(homeViewModel = homeViewModel)
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
    FamilyTheme {
        Greeting("Android")
    }
}
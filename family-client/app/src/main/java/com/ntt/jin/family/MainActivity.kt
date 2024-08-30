package com.ntt.jin.family

import android.os.Bundle
import android.Manifest
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ntt.jin.family.domain.CheckPermissionsUseCase
import com.ntt.jin.family.ui.HomeViewModel
import com.ntt.jin.family.ui.theme.FamilyTheme

class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels { HomeViewModel.Factory }
    private val checkPermissionsUseCase: CheckPermissionsUseCase = CheckPermissionsUseCase()

    @Composable
    fun FamilyApplicationProvider(content: @Composable () -> Unit) {
        CompositionLocalProvider(LocalAppContext provides applicationContext) {
            content()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen().let { splashScreen ->
            splashScreen.setKeepOnScreenCondition {
                if (homeViewModel.isSkyWayInitialized) {
                    homeViewModel.startRoomsStateChecker()
                }
                !homeViewModel.isSkyWayInitialized
            }
        }

        checkPermissionsUseCase(this, listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ))
        setContent {
            FamilyTheme {
                FamilyApplicationProvider {
                    FamilyApp(homeViewModel = homeViewModel)
                }
            }
        }
    }
}
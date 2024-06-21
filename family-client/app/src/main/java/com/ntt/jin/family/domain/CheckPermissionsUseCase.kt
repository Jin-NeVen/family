package com.ntt.jin.family.domain

import android.Manifest
import android.app.Activity
import android.content.Context
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker

class CheckPermissionsUseCase {
    operator fun invoke(activity: Activity, permissions: List<String>) {
        var permissionGranted = true
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    activity,
                    permission,
                ) == PermissionChecker.PERMISSION_DENIED
            ) {
                permissionGranted = false
                break
            }
        }
        //TODO only request these permissions which are not granted
        if (!permissionGranted) {
            ActivityCompat.requestPermissions(
                activity,
                permissions.toTypedArray(),
                0
            )
        }
    }
}
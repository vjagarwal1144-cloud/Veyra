package com.vjagarwal.veyra

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.vjagarwal.veyra.ui.MainViewModel
import com.vjagarwal.veyra.ui.VeyraApp

class MainActivity : ComponentActivity() {
    private val model: MainViewModel by viewModels { MainViewModel.factory(application) }
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { model.refreshSafetyState() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.POST_NOTIFICATIONS))
        setContent { VeyraApp(model) }
    }
}

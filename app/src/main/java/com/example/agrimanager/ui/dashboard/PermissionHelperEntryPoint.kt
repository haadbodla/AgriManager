package com.example.agrimanager.ui.dashboard

import com.example.agrimanager.utils.PermissionHelper
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@EntryPoint
@InstallIn(ActivityComponent::class)
interface PermissionHelperEntryPoint {
    fun permissionHelper(): PermissionHelper
}

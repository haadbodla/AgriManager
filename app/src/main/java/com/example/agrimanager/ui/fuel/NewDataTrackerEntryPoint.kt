package com.example.agrimanager.ui.fuel

import com.example.agrimanager.utils.NewDataTracker
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt EntryPoint to access NewDataTracker from Composable functions.
 * Used to check if entries are "new" (created after last seen timestamp).
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface NewDataTrackerEntryPoint {
    fun newDataTracker(): NewDataTracker
}

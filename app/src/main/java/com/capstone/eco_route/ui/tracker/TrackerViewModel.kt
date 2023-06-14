@file:Suppress("DEPRECATION")

package com.capstone.eco_route.ui.tracker

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.capstone.eco_route.datasource.db.repository.TrackerRepository

@Suppress("DEPRECATION")
class TrackerViewModel @ViewModelInject constructor(
    val trackerRepository: TrackerRepository
): ViewModel()
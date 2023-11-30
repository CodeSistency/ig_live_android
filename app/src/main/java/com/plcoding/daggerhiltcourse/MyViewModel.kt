package com.plcoding.daggerhiltcourse

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.plcoding.daggerhiltcourse.domain.repository.MyRepository
import dagger.Lazy
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: Lazy<MyRepository>
): ViewModel() {

    var nombre by mutableStateOf<String?>("")
    var quantity by mutableStateOf<Int?>(100)

    init {
        repository.get()
    }

    fun simulateFluctuation(currentViewers: Int, maxFluctuationPercentage: Int): Int {
        // Calculate the maximum fluctuation based on the current viewers
        val maxFluctuation = (currentViewers * maxFluctuationPercentage) / 100

        // Generate a random fluctuation within the calculated range
        val fluctuation = Random.nextInt(-maxFluctuation, maxFluctuation + 1)

        // Ensure the result is within bounds (e.g., not negative viewers)
        val newViewers = maxOf(0, currentViewers + fluctuation)

        return newViewers
    }

    var seguidores = quantity?.let { simulateFluctuation(it, 5) }

    fun updateViewerCount() {
        // Simulate fluctuation
        quantity = quantity?.let { simulateFluctuation(it, maxFluctuationPercentage = 5) }
    }

}
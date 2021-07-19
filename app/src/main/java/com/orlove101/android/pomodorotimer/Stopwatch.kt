package com.orlove101.android.pomodorotimer

data class Stopwatch(
    val id: Int,
    var currentMs: Long,
    var isStarted: Boolean,
    var currentViewState: Long,
)
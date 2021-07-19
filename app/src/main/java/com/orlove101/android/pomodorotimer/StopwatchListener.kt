package com.orlove101.android.pomodorotimer

interface StopwatchListener {

    fun start(id: Int)

    fun stop(id: Int, currentMs: Long, currentViewState: Long)

    fun delete(id: Int)
}
package com.orlove101.android.pomodorotimer

import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.orlove101.android.pomodorotimer.databinding.StopwatchItemBinding
import kotlinx.coroutines.*

class StopwatchAdapter(
    private val listener: StopwatchListener,
    private val lifecycleScope: LifecycleCoroutineScope
): ListAdapter<Stopwatch, StopwatchAdapter.StopwatchViewHolder>(itemComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopwatchViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = StopwatchItemBinding.inflate(layoutInflater, parent, false)
        return StopwatchViewHolder(binding, listener, binding.root.context.resources)
    }

    override fun onBindViewHolder(holder: StopwatchViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class StopwatchViewHolder(
        private val binding: StopwatchItemBinding,
        private val listener: StopwatchListener,
        private val resources: Resources
    ): RecyclerView.ViewHolder(binding.root) {

//        private var timer: CountDownTimer? = null

        private var startTime = 0L
        private var timerValue = 0L
        private var timerActive = true
        private var job: Job? = null

        fun bind(stopwatch: Stopwatch) {
            binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()

            if ( stopwatch.isStarted ) {
                startTimer(stopwatch)
            } else {
                stopTimer(stopwatch)
            }

            initButtonsListeners(stopwatch)
        }

        private fun initButtonsListeners(stopwatch: Stopwatch) {
            binding.startPauseButton.setOnClickListener {
                if ( stopwatch.isStarted ) {
                    listener.stop(stopwatch.id, stopwatch.currentMs, stopwatch.currentViewState)
                } else {
                    listener.start(stopwatch.id)
                }
            }

            binding.deleteButton.setOnClickListener { listener.delete(stopwatch.id) }
        }

        private fun startTimer(stopwatch: Stopwatch) {
            val drawable = resources.getDrawable(R.drawable.ic_baseline_pause_24)
            binding.startPauseButton.setImageDrawable(drawable)

            timerActive = true
            // when element is out of screen timer stopped

            // when another element picked other coroutines continue their work

            job = lifecycleScope.launch(Dispatchers.Main) {
                startTime = System.currentTimeMillis()
                timerValue = stopwatch.currentMs

                while (timerActive || binding.stopwatchTimer.text != START_TIME) {
                    var nowTime = System.currentTimeMillis()
                    var validTime = timerValue - (nowTime - startTime)

                    stopwatch.currentMs = validTime
                    binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
                    delay(UNIT_TEN_MS)
                }
            }

//            timer?.cancel()
//            timer = getCountDownTimer(stopwatch)
//            timer?.start()

            binding.progressView.setPeriod(stopwatch.currentMs)

            binding.blinkingIndicator.isInvisible = false
            (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
        }

        private fun stopTimer(stopwatch: Stopwatch) {
            val drawable = resources.getDrawable(R.drawable.ic_baseline_play_arrow_24)
            binding.startPauseButton.setImageDrawable(drawable)

            timerActive = false

            job?.cancel()

//            timer?.cancel()

            binding.progressView.setPeriod(stopwatch.currentMs)
            binding.progressView.setCurrent(stopwatch.currentViewState)

            binding.blinkingIndicator.isInvisible = true
            (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
        }

//        private fun getCountDownTimer(stopwatch: Stopwatch): CountDownTimer {
//            return object : CountDownTimer(stopwatch.currentMs, UNIT_TEN_S) {
//                val interval = UNIT_TEN_S
//
//                override fun onTick(millisUntilFinished: Long) {
//                    stopwatch.currentMs -= interval
//                    stopwatch.currentViewState += UNIT_TEN_S
//                    binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
//                    binding.progressView.setCurrent(stopwatch.currentViewState)
//                }
//
//                override fun onFinish() {
//                    binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
//                    binding.blinkingIndicator.isInvisible = true
//                    (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
//                    binding.root.setCardBackgroundColor(Color.RED)
//                }
//            }
//        }
    }

    private companion object {
        private val itemComparator = object : DiffUtil.ItemCallback<Stopwatch>() {

            override fun areItemsTheSame(oldItem: Stopwatch, newItem: Stopwatch): Boolean {
                return oldItem.id == newItem.id && oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: Stopwatch, newItem: Stopwatch): Boolean {
                return oldItem.currentMs == newItem.currentMs &&
                        oldItem.isStarted == newItem.isStarted &&
                        oldItem.currentViewState == newItem.currentViewState
            }

            override fun getChangePayload(oldItem: Stopwatch, newItem: Stopwatch) = Any()
        }
    }
}
package com.orlove101.android.pomodorotimer

import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import android.text.Layout
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.orlove101.android.pomodorotimer.databinding.StopwatchItemBinding

class StopwatchAdapter(
    private val listener: StopwatchListener
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

        private var timer: CountDownTimer? = null

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
                    listener.stop(stopwatch.id, stopwatch.currentMs)
                } else {
                    listener.start(stopwatch.id)
                }
            }

            binding.restartButton.setOnClickListener { listener.reset(stopwatch.id) }

            binding.deleteButton.setOnClickListener { listener.delete(stopwatch.id) }
        }

        private fun startTimer(stopwatch: Stopwatch) {
            val drawable = resources.getDrawable(R.drawable.ic_baseline_pause_24)
            binding.startPauseButton.setImageDrawable(drawable)

            timer?.cancel()
            timer = getCountDownTimer(stopwatch)
            timer?.start()

            binding.blinkingIndicator.isInvisible = false
            (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
        }

        private fun stopTimer(stopwatch: Stopwatch) {
            val drawable = resources.getDrawable(R.drawable.ic_baseline_play_arrow_24)
            binding.startPauseButton.setImageDrawable(drawable)

            timer?.cancel()

            binding.blinkingIndicator.isInvisible = true
            (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
        }

        private fun getCountDownTimer(stopwatch: Stopwatch): CountDownTimer {
            return object : CountDownTimer(PERIOD, UNIT_TEN_MS) {
                val interval = UNIT_TEN_MS

                override fun onTick(millisUntilFinished: Long) {
                    stopwatch.currentMs += interval
                    binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
                }

                override fun onFinish() {
                    binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
                }
            }
        }

        private fun Long.displayTime(): String {
            if ( this <= 0L ) {
                return START_TIME
            }
            val h = this / 1000 / 3600
            val m = this / 1000 % 3600 / 60
            val s = this / 1000 % 60
            val ms = this % 1000 / 10

            return "${displaySlot(h)}:${displaySlot(m)}:${displaySlot(s)}:${displaySlot(ms)}"
        }

        private fun displaySlot(count: Long): String {
            return if ( count / 10L > 0 ) {
                "$count"
            } else {
                "0$count"
            }
        }
    }

    private companion object {
        private val itemComparator = object : DiffUtil.ItemCallback<Stopwatch>() {

            override fun areItemsTheSame(oldItem: Stopwatch, newItem: Stopwatch): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Stopwatch, newItem: Stopwatch): Boolean {
                return oldItem.currentMs == newItem.currentMs &&
                        oldItem.isStarted == newItem.isStarted
            }

            override fun getChangePayload(oldItem: Stopwatch, newItem: Stopwatch) = Any()
        }

        private const val START_TIME = "00:00:00:00"
        private const val UNIT_TEN_MS = 10L
        private const val PERIOD = 1000L * 60L * 60L * 24L // Day
    }
}
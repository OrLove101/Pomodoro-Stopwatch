package com.orlove101.android.pomodorotimer

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.orlove101.android.pomodorotimer.databinding.PomodoroFragmentBinding
import kotlinx.coroutines.cancel

class PomodoroFragment: Fragment(), StopwatchListener, LifecycleObserver {
    private var _binding: PomodoroFragmentBinding? = null
    private val binding get() = _binding!!

    private val stopwatchAdapter = this.activity?.let { StopwatchAdapter(this, it) }
    private val stopwatches = mutableListOf<Stopwatch>()
    private var nextId = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PomodoroFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stopwatchAdapter
        }

        binding.addNewStopwatchButton.setOnClickListener {
            val minutes = binding.editTextMinutes.text.toString().toLong()
            val milliseconds = minutes * 60000L

            if ( minutes > 0 ) {
                stopwatches.add(Stopwatch(nextId++, milliseconds, false, 0L))
                stopwatchAdapter?.submitList(stopwatches.toList())
            }
        }
    }

    override fun start(id: Int) {
        stopwatches.forEach { it.isStarted = false }
        stopwatchAdapter?.notifyDataSetChanged()
        changeStopwatch(id, null, true, null)
    }

    override fun stop(id: Int, currentMs: Long, currentViewState: Long) {
        changeStopwatch(id, currentMs, false, currentViewState)
    }

    override fun delete(id: Int) {
        stopwatches.remove(stopwatches.find { it.id == id })
        stopwatchAdapter?.submitList(stopwatches.toList())
    }

    private fun changeStopwatch(id: Int, currentMs: Long?, isStarted: Boolean, currentViewState: Long?) {
        val newTimers = mutableListOf<Stopwatch>()
        stopwatches.forEach {
            if ( it.id == id ) {
                newTimers.add(Stopwatch(
                    it.id,
                    currentMs ?: it.currentMs,
                    isStarted,
                    currentViewState ?: it.currentViewState)
                ) //just change not create
            } else {
                newTimers.add(it)
            }
        }
        stopwatchAdapter?.submitList(newTimers)
        stopwatches.clear()
        stopwatches.addAll(newTimers) // searched item should to be changed in stopwatches // make new list is redundant
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        val startIntent = Intent(activity, ForegroundService::class.java)
        startIntent.putExtra(COMMAND_ID, COMMAND_START)
        startIntent.putExtra(STARTED_TIMER_TIME_MS, startTime)
        activity?.startService(startIntent)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        val stopIntent = Intent(activity, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        activity?.startService(stopIntent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): PomodoroFragment {
            val fragment = PomodoroFragment()
            val args = bundleOf()

            fragment.arguments = args
            return fragment
        }
        private const val TAG = "PomodoroFragment"
    }
}
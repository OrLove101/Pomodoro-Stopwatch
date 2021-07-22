package com.orlove101.android.pomodorotimer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.orlove101.android.pomodorotimer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        if ( savedInstanceState == null ) {
            openPomodoroFragment()
        }
    }

    private fun openPomodoroFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, PomodoroFragment.newInstance())
            .commit()
    }
}
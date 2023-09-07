package ru.kot1.demo.activity.editors

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import ru.kot1.demo.R


@AndroidEntryPoint
class MapActivity : AppCompatActivity(R.layout.activity_map) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar((findViewById(R.id.toolbar)))

    }

}


package com.anwesh.uiprojects.linkedcircleinclineview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.circleinclineview.CircleIncLineView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CircleIncLineView.create(this)
    }
}

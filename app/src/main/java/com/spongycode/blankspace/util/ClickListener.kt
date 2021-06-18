package com.spongycode.blankspace.util

import android.content.Context
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.GestureDetectorCompat
import com.spongycode.blankspace.model.modelmemes.MemeModel

open class ClickListener(context: Context): View.OnTouchListener {

    private val gestureDetector = GestureDetector(context, GestureListener())

    fun onTouch(event: MotionEvent): Boolean{
        return gestureDetector.onTouchEvent(event)
    }

    private inner class GestureListener: GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener{
        override fun onDown(e: MotionEvent?): Boolean {
            return true
        }

        override fun onShowPress(e: MotionEvent?) {
            return
        }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            onTapUp()
            return true
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent?,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            return true
        }

        override fun onLongPress(e: MotionEvent?) {
            onLong()
            return
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent?,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            onSingle()
            return true
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            onDouble()
            return true
        }

        override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
            return true
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    open fun onSingle(){}
    open fun onDouble(){}
    open fun onLong(){}
    open fun onTapUp(){}

}
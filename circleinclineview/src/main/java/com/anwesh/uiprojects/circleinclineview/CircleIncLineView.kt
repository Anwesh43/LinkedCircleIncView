package com.anwesh.uiprojects.circleinclineview

/**
 * Created by anweshmishra on 31/08/18.
 */
import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Canvas
import android.content.Context
import android.graphics.Color
import android.graphics.RectF

val nodes : Int = 5

fun Canvas.drawCILNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / nodes
    val sc : Float = Math.min(0.5f, Math.max(0f, scale - 0.5f))
    val size : Float = gap / 4
    paint.strokeWidth = Math.min(w, h) / 60
    paint.strokeCap = Paint.Cap.ROUND
    paint.color = Color.parseColor("#4CAF50")
    save()
    translate(w/2, i * gap)
    drawLine(0f, 0f, 0f, gap * scale, paint)
    save()
    translate(0f, gap/2)
    drawArc(RectF(-size, -size, size, size), 90f * (1 - 2 * (i % 2)), 180f * sc, true, paint)
    restore()
    restore()
}

class CircleIncLineView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var prevScale : Float = 0f, var dir : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += 0.1f * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1 - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch (ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class CILNode(var i : Int, val state : State = State()) {

        var prev : CILNode? = null

        var next : CILNode? = null

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : CILNode {
            var curr : CILNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = CILNode(i + 1)
                next?.prev = this
            }
        }

        init {
            addNeighbor()
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawCILNode(i, state.scale, paint)
            prev?.draw(canvas, paint)
        }
    }

    data class LinkedCircleIncLine(var i : Int) {
        private var curr : CILNode = CILNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : CircleIncLineView) {

        private val curr : LinkedCircleIncLine = LinkedCircleIncLine(0)
        private val animator : Animator = Animator(view)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#BDBDBD"))
            curr.draw(canvas, paint)
            animator.animate {
                curr.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            curr.startUpdating {
                animator.start()
            }
        }
    }
}
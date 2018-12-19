package com.example.viet.tableattendancekotlin

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.support.v4.view.GestureDetectorCompat
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.OverScroller
import java.util.*

class TableAttendanceView : View {
    companion object {
        const val DEBUG = true
        const val DEFAULT_STROKE_WIDTH = 1
    }

    //region attrs
    private enum class Direction {
        LEFT, RIGHT, VERTICAL, NONE
    }

    private var mColumnDateWidth = 200
        set(value) {
            field = value
            invalidate()
        }
    private var mColumnEventWidth = 200
        set(value) {
            field = value
            invalidate()
        }
    private var mRowHeaderHeight = 100
        set(value) {
            field = value
            invalidate()
        }
    private var mRowFooterHeight = 100
        set(value) {
            field = value
            invalidate()
        }
    private var mRowDateHeight = 100
        set(value) {
            field = value
            invalidate()
        }
    private var mBackgroundColor = Color.rgb(1, 2, 3)
        set(value) {
            field = value
            invalidate()
        }
    private var mHeaderEventTextColor = Color.BLACK
        set(value) {
            field = value
            invalidate()
        }
    private var mHeaderEventTextSize = 20
        set(value) {
            field = value
            invalidate()
        }
    private var mDateTextSize = 20
        set(value) {
            field = value
            invalidate()
        }
    private var mXScrollingSpeed = 1
        set(value) {
            field = value
            invalidate()
        }
    private var mYScrollingSpeed = 1
        set(value) {
            field = value
            invalidate()
        }
    private var mScrollingFriction = 1
        set(value) {
            field = value
            invalidate()
        }
    private var mHorizontalFlingEnabled = true
        set(value) {
            field = value
            invalidate()
        }
    private var mVerticalFlingEnabled = true
        set(value) {
            field = value
            invalidate()
        }
    //endregion attrs

    //region paint
    private val defaultPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    //endregion paint

    //region others
    private val currentDate = Calendar.getInstance()
    //endregion others

    private var mScaledTouchSlop = 0
    private var mCurrentOrigin: PointF = PointF(0f, 0f)
    private var mCurrentScrollDirection = Direction.NONE
    private var mCurrentFlingDirection = Direction.NONE
    private lateinit var mScroller: OverScroller
    private lateinit var mGestureDetector: GestureDetectorCompat
    private var mGestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent?): Boolean {
            debug("onDown")
            return true
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            debug("onFling")
            if (mCurrentFlingDirection == Direction.LEFT && !mHorizontalFlingEnabled ||
                mCurrentFlingDirection == Direction.RIGHT && !mHorizontalFlingEnabled ||
                mCurrentFlingDirection == Direction.VERTICAL && !mVerticalFlingEnabled
            ) {
                return true
            }

            mScroller.forceFinished(true)

            mCurrentFlingDirection = mCurrentScrollDirection
            when (mCurrentFlingDirection) {
                Direction.LEFT, Direction.RIGHT -> {
                    mScroller.fling(
                        mCurrentOrigin.x.toInt(),
                        mCurrentOrigin.y.toInt(),
                        (velocityX * mXScrollingSpeed).toInt(),
                        0,
                        -(getMaxHorizontalScroll().toFloat()).toInt(),
                        (getMaxHorizontalScroll().toFloat()).toInt(),
                        0,
                        0
                    )
                    invalidate()
                }

//                Direction.VERTICAL -> if (e1.y > mHeaderHeight || mNeedToScrollAllDayEvents) {
                Direction.VERTICAL ->
                    if (mCurrentOrigin.y != 0f) {
                        mScroller.fling(
                            mCurrentOrigin.x.toInt(),
                            mCurrentOrigin.y.toInt(),
                            0,
                            (velocityY * mYScrollingSpeed).toInt(),
                            Integer.MIN_VALUE,
                            Integer.MAX_VALUE,
                            (-getMaxVerticalScroll().toFloat() + mRowHeaderHeight + height).toInt(),
                            0
                        )
                        invalidate()

                    }
                else -> {
                    //do nothing
                }
            }
            return true
        }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            debug("onScroll")
            when (mCurrentScrollDirection) {
                Direction.NONE -> {
                    // Allow scrolling only in one direction.
                    mCurrentScrollDirection = if (Math.abs(distanceX) > Math.abs(distanceY)) {
                        if (distanceX > 0) {
                            Direction.LEFT
                        } else {
                            Direction.RIGHT
                        }
                    } else {
                        Direction.VERTICAL
                    }
                }
                Direction.LEFT -> {
                    // Change direction if there was enough change.
                    if (Math.abs(distanceX) > Math.abs(distanceY) && distanceX < -mScaledTouchSlop) {
                        mCurrentScrollDirection = Direction.RIGHT
                    }
                }
                Direction.RIGHT -> {
                    // Change direction if there was enough change.
                    if (Math.abs(distanceX) > Math.abs(distanceY) && distanceX > mScaledTouchSlop) {
                        mCurrentScrollDirection = Direction.LEFT
                    }
                }
                else -> {
                    //do nothing
                }
            }
            when (mCurrentScrollDirection) {
                Direction.LEFT, Direction.RIGHT -> {
                    mCurrentOrigin.x -= distanceX * mXScrollingSpeed
                    invalidate()
                }
                Direction.VERTICAL -> {
                    if (mCurrentOrigin.y - distanceY < 0) {
                        mCurrentOrigin.y -= distanceY
                        invalidate()
                    }
                }
                else -> {
                    //do nothing
                }
            }
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            println("onSingleTapConfirmed")
            return true
        }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val attrs = context.theme.obtainStyledAttributes(attrs, R.styleable.TableAttendanceView, 0, 0)
        try {

        } finally {
            attrs.recycle()
        }
        init()
    }

    private fun init() {
        mScroller = OverScroller(context)
        mGestureDetector = GestureDetectorCompat(context, mGestureListener)
        mScaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop

        defaultPaint.strokeWidth = DEFAULT_STROKE_WIDTH.toFloat()
        defaultPaint.color = Color.BLACK
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        val rectPaint = Paint()
        rectPaint.strokeWidth = 2f
        rectPaint.color = Color.BLACK
        canvas.drawRect(
            RectF(
                50 + mCurrentOrigin.x,
                50 + mCurrentOrigin.y,
                200 + mCurrentOrigin.x,
                200 + mCurrentOrigin.y
            ), rectPaint
        )
        drawDateColumn(canvas)
    }

    /**
     *
     */
    private fun drawDateColumn(canvas: Canvas) {
        val today = Calendar.getInstance()
        val currentMonth = today.get(Calendar.MONTH)
        val numberOfDayInMonth = today.getActualMaximum(Calendar.DAY_OF_MONTH)
        val maxHeight: Int = if (mRowDateHeight * numberOfDayInMonth > height) {
            height
        } else {
            mRowDateHeight * numberOfDayInMonth
        }
        val dateRect = Rect(0, 0, mColumnDateWidth, maxHeight)
        canvas.clipRect(dateRect, Region.Op.REPLACE)
        val dateRectPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        dateRectPaint.color = mBackgroundColor
        dateRectPaint.strokeWidth = DEFAULT_STROKE_WIDTH.toFloat()
        dateRectPaint.style = Paint.Style.STROKE
        canvas.drawRect(0f, 0f, mColumnDateWidth.toFloat(), maxHeight.toFloat(), dateRectPaint)

        for (i in 0 until numberOfDayInMonth) {
            val startY = mCurrentOrigin.y + i * mRowDateHeight + mRowHeaderHeight
            val stopY = mCurrentOrigin.y + mRowDateHeight * (i + 1) + mRowHeaderHeight
            canvas.drawLine(0F, startY, mColumnDateWidth.toFloat(), startY, defaultPaint)
            canvas.drawLine(0F, stopY, mColumnDateWidth.toFloat(), stopY, defaultPaint)
        }
    }

    private fun getMaxVerticalScroll(): Int {
        return currentDate.getActualMaximum(Calendar.DAY_OF_MONTH) * mRowDateHeight
    }

    private fun getMaxHorizontalScroll(): Int {
        return 10 * mColumnDateWidth
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val result = mGestureDetector.onTouchEvent(event)
        if (event.action == MotionEvent.ACTION_UP) {
            if (mCurrentScrollDirection == Direction.RIGHT || mCurrentScrollDirection == Direction.LEFT) {

            } else {

            }
            mCurrentScrollDirection = Direction.NONE
        }
        return result
    }

    override fun computeScroll() {
        super.computeScroll()
        debug("computeScroll")
        if (mScroller.isFinished) {
            if (mCurrentFlingDirection != Direction.NONE) {
                // Snap to day after fling is finished.
                mCurrentFlingDirection = Direction.NONE
            }
        } else {
            if (mScroller.computeScrollOffset()) {
                mCurrentOrigin.y = mScroller.currY.toFloat()
                mCurrentOrigin.x = mScroller.currX.toFloat()
                invalidate()
            }
        }

    }

    /**
     * DEBUG
     */
    private fun debug(message: String) {
        if (DEBUG) {
            Log.d("TableAttendanceView", message)
        }
    }
}
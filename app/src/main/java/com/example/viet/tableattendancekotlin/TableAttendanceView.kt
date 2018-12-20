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
import kotlin.collections.ArrayList

class TableAttendanceView : View {
    companion object {
        const val DEBUG = true
        const val DEFAULT_STROKE_WIDTH = 1
    }

    //region attrs
    private enum class Direction {
        LEFT, RIGHT, VERTICAL, NONE
    }

    private var mColumnDateWidth = 240
        set(value) {
            field = value
            invalidate()
        }
    private var mColumnEventWidth = 240
        set(value) {
            field = value
            invalidate()
        }
    private var mRowHeaderHeight = 120
        set(value) {
            field = value
            invalidate()
        }
    private var mRowFooterHeight = 120
        set(value) {
            field = value
            invalidate()
        }
    private var mRowDateHeight = 120
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
    private var mXScrollingSpeed = 0.5f
        set(value) {
            field = value
            invalidate()
        }
    private var mYScrollingSpeed = 0.75f
        set(value) {
            field = value
            invalidate()
        }
    private var mScrollingFriction = 0.75
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
    private val mDefaultPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val mEventHeaderTextPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    //endregion paint

    //region content var
    private val mCurrentDate = Calendar.getInstance()
    private val mEventHeader = ArrayList<String>()
    private var mEventHeaderTextHeight: Int = 20
    //endregion content var

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
                        -getMaxHorizontalScroll(),
                        0,
                        Integer.MIN_VALUE,
                        Integer.MAX_VALUE
                    )
                    invalidate()
                }

                Direction.VERTICAL ->
                    if (mCurrentOrigin.y != 0f) {
                        mScroller.fling(
                            mCurrentOrigin.x.toInt(),
                            mCurrentOrigin.y.toInt(),
                            0,
                            (velocityY * mYScrollingSpeed).toInt(),
                            Integer.MIN_VALUE,
                            Integer.MAX_VALUE,
                            -getMaxVerticalScroll(),
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
                    if (mCurrentOrigin.x - distanceX * mXScrollingSpeed < 0 && mCurrentOrigin.x - distanceX * mXScrollingSpeed > -getMaxHorizontalScroll()) {
                        mCurrentOrigin.x -= distanceX * mXScrollingSpeed
                        invalidate()
                    }
                }
                Direction.VERTICAL -> {
                    if (mCurrentOrigin.y - distanceY < 0 && mCurrentOrigin.y - distanceY > -getMaxVerticalScroll()) {
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

        mDefaultPaint.apply {
            strokeWidth = DEFAULT_STROKE_WIDTH.toFloat()
            color = Color.BLACK
            textSize = 40f
        }
        initData()
    }

    private fun initData() {
        mEventHeader.clear()
        mEventHeader.add("出勤") // firstCheckIn
        mEventHeader.add("退勤") // lastCheckOut
        mEventHeader.add("滞在") // hospitalStayTime
        mEventHeader.add("超過") // extendedTime
        mEventHeader.add("残業") // overtime
        mEventHeader.add("その他") // otherTime
        mEventHeader.add("休憩") // breaktime
        mEventHeader.add("深夜") // nightWorkTime
        mEventHeaderTextHeight = getTextBound(mEventHeader[0], mDefaultPaint).height()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        debug("mCurrentOrigin.x: " + mCurrentOrigin.x)
        debug("mCurrentOrigin.y: " + mCurrentOrigin.y)
        debug("height: " + height)
        debug("width: " + width)
        debug("getMaxHorizontalScroll(): " + getMaxHorizontalScroll())
        debug("getMaxVerticalScroll(): " + getMaxVerticalScroll())
        //draw line on left of event header row
        canvas.drawLine(
            mColumnDateWidth.toFloat(),
            0F,
            mColumnDateWidth.toFloat(),
            (height - mRowFooterHeight).toFloat(),
            mDefaultPaint
        )
        //draw line in top of date column
        canvas.drawLine(
            0F,
            mRowHeaderHeight.toFloat(),
            width.toFloat(),
            mRowHeaderHeight.toFloat(),
            mDefaultPaint
        )
        //draw line top of sumary row
        canvas.drawLine(
            0F,
            (height - mRowFooterHeight).toFloat(),
            width.toFloat(),
            (height - mRowFooterHeight).toFloat(),
            mDefaultPaint
        )
        drawDateColumn(canvas)
        drawEventHeader(canvas)
        drawEventAxes(canvas)
        drawSumaryRow(canvas)
    }

    private fun drawSumaryRow(canvas: Canvas) {
        val totalHeaderRect = Rect(0, height - mRowFooterHeight, mColumnDateWidth, height)
        canvas.clipRect(totalHeaderRect, Region.Op.REPLACE)
        //draw total title
        val leftTotalText = (mColumnDateWidth - mDefaultPaint.measureText("Total")) / 2
        val topTotalText = height - (mRowFooterHeight - getTextBound("Total", mDefaultPaint).height()) / 2
        canvas.drawText("Total", leftTotalText, topTotalText.toFloat(), mDefaultPaint)
        // draw total data
        val totalDataRect = Rect(mColumnDateWidth, height - mRowFooterHeight, width, height)
        canvas.clipRect(totalDataRect, Region.Op.REPLACE)
        val topText = height - mRowFooterHeight / 2 + getTextBound("-", mDefaultPaint).height()
        mEventHeader.indices.forEach { index ->
            val leftColumn = index * mColumnEventWidth + mCurrentOrigin.x + mColumnDateWidth
            val rightColum = leftColumn + mColumnEventWidth
            val leftText = (rightColum + leftColumn - mDefaultPaint.measureText("-")) / 2
            canvas.drawText("-", leftText, topText.toFloat(), mDefaultPaint)
        }
    }

    private fun drawEventHeader(canvas: Canvas) {
        val eventHeaderRect = Rect(mColumnDateWidth, 0, width, mRowHeaderHeight)
        canvas.clipRect(eventHeaderRect, Region.Op.REPLACE)
        val topText = (mRowHeaderHeight + mEventHeaderTextHeight) / 2
        mEventHeader.indices.forEach { index ->
            val leftColumn = index * mColumnEventWidth + mCurrentOrigin.x + mColumnDateWidth
            val rightColum = leftColumn + mColumnEventWidth
            val textWidth = mDefaultPaint.measureText(mEventHeader[index])
            val leftText = (rightColum + leftColumn) / 2 - textWidth / 2
            canvas.drawText(mEventHeader[index], leftText, topText.toFloat(), mDefaultPaint)
            canvas.drawLine(rightColum, 0f, rightColum, mRowHeaderHeight.toFloat(), mDefaultPaint)
        }
    }

    /**
     *
     */
    private fun drawDateColumn(canvas: Canvas) {
        val numberOfDayInMonth = getNumberOfDayCurrentMonth()
        val maxHeight: Int = if (mRowDateHeight * numberOfDayInMonth > height) {
            height
        } else {
            mRowDateHeight * numberOfDayInMonth
        }
        val dateRect = Rect(0, mRowDateHeight, mColumnDateWidth, maxHeight - mRowFooterHeight)
        canvas.clipRect(dateRect, Region.Op.REPLACE)
        canvas.drawLine(mColumnDateWidth.toFloat(), 0f, mColumnDateWidth.toFloat(), height.toFloat(), mDefaultPaint)
        val textHeight = getTextBound("I", mDefaultPaint).height()
        for (i in 1..numberOfDayInMonth) {
            val startY = mCurrentOrigin.y + (i - 1) * mRowDateHeight + mRowHeaderHeight
            val stopY = mCurrentOrigin.y + mRowDateHeight * i + mRowHeaderHeight
            val topText = (stopY + startY + textHeight) / 2
            val calendar = Calendar.getInstance()
            calendar[Calendar.DAY_OF_MONTH] = i
            val dayTitle = getDayTitle(calendar)
            val leftText = mColumnDateWidth * 3 / 4 - mDefaultPaint.measureText(dayTitle)
            canvas.drawText(dayTitle, leftText, topText, mDefaultPaint)
            canvas.drawLine(0F, stopY, mColumnDateWidth.toFloat(), stopY, mDefaultPaint)
        }
    }

    private fun drawEventAxes(canvas: Canvas) {
        val eventRect = Rect(mColumnDateWidth, mRowDateHeight, width, height - mRowFooterHeight)
        canvas.clipRect(eventRect, Region.Op.REPLACE)
        mEventHeader.indices.forEach { index ->
            val leftColumn = index * mColumnEventWidth + mCurrentOrigin.x + mColumnDateWidth
            val rightColum = leftColumn + mColumnEventWidth
            canvas.drawLine(rightColum, 0f, rightColum, height.toFloat(), mDefaultPaint)
        }

        for (i in 1..getNumberOfDayCurrentMonth()) {
            val startY = mCurrentOrigin.y + (i - 1) * mRowDateHeight + mRowHeaderHeight
            val stopY = startY + mRowDateHeight
            canvas.drawLine(0F, stopY, width.toFloat(), stopY, mDefaultPaint)
        }
    }

    private fun getDayTitle(calendar: Calendar, template: String = "%d日 %s"): String {
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val week = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> {
                context.getString(R.string.COMMON_SUN)
            }
            Calendar.MONDAY -> {
                context.getString(R.string.COMMON_MON)
            }
            Calendar.TUESDAY -> {
                context.getString(R.string.COMMON_TUE)
            }
            Calendar.WEDNESDAY -> {
                context.getString(R.string.COMMON_WED)
            }
            Calendar.THURSDAY -> {
                context.getString(R.string.COMMON_THU)
            }
            Calendar.FRIDAY -> {
                context.getString(R.string.COMMON_FRI)
            }
            Calendar.SATURDAY -> {
                context.getString(R.string.COMMON_SAT)
            }
            else -> {
                ""
            }

        }
        return String.format(template, dayOfMonth, week)
    }

    private fun getTextBound(text: String, paint: Paint): Rect {
        val boundingRect = Rect()
        paint.getTextBounds(text, 0, text.length, boundingRect)
        return boundingRect
    }

    private fun getNumberOfDayCurrentMonth(): Int {
        return mCurrentDate.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    private fun getMaxVerticalScroll(): Int {
        return getNumberOfDayCurrentMonth() * mRowDateHeight + mRowHeaderHeight - height
    }

    private fun getMaxHorizontalScroll(): Int {
        return mEventHeader.size * mColumnDateWidth - width + mColumnDateWidth
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
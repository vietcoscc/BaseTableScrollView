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
    private var mRowHeaderHeight = 80
        set(value) {
            field = value
            invalidate()
        }
    private var mRowFooterHeight = 80
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
    private var mHeaderEventTextSize = 40
        set(value) {
            field = value
            invalidate()
        }
    private var mDateTextColor = Color.BLACK
        set(value) {
            field = value
            invalidate()
        }
    private var mDateTextSize = 40
        set(value) {
            field = value
            invalidate()
        }
    private var mXScrollingSpeed = 0.8f
        set(value) {
            field = value
            invalidate()
        }
    private var mYScrollingSpeed = 0.8f
        set(value) {
            field = value
            invalidate()
        }
    private var mScrollingFriction = 0.8
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
    private var mSundayDateTextColor = Color.RED
        set(value) {
            field = value
            invalidate()
        }
    private var mSaturdayTextColor = Color.BLUE
        set(value) {
            field = value
            invalidate()
        }
    private var mTodayTextColor = Color.BLACK
        set(value) {
            field = value
            invalidate()
        }
    private var mDefaultDateBackroundColor = Color.LTGRAY
        set(value) {
            field = value
            invalidate()
        }
    private var mTodayDateBackroundColor = Color.CYAN
        set(value) {
            field = value
            invalidate()
        }

    private var mHeaderEventBackgroundColor = Color.LTGRAY
        set(value) {
            field = value
            invalidate()
        }
    //endregion attrs

    //region paint
    private val mDefaultPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val mEventBackgroundPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val mHeaderEventTextPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val mHeaderEventBackgroundPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val mDatePaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    //endregion paint
    //region bitmap
    private var mHeaderEventBackground: Bitmap? = null
    private var mFooterEventBackground: Bitmap? = null
    private var mDateBackground: Bitmap? = null
    private var mTodayDateBackground: Bitmap? = null
    //endregion bitmap
    //region content var
    private val mCurrentDate = Calendar.getInstance()
    private val mHeaderEvent = ArrayList<String>()
    private var mHeaderEventTextHeight: Int = 0
    private val mTimeSheet = TimeSheet()
    private val mEventDateRects = ArrayList<EventDateRect>()

    //endregion content var

    private var mScaledTouchSlop = 0
    private var mCurrentOrigin: PointF = PointF(0f, 0f)
    private var mCurrentScrollDirection = Direction.NONE
    private var mCurrentFlingDirection = Direction.NONE
    private lateinit var mScroller: OverScroller
    private lateinit var mGestureDetector: GestureDetectorCompat


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
        initPaint()
        initBitmap()
        initData()

    }

    private fun initBitmap() {
        mHeaderEventBackground = drawableToBitmap(R.drawable.ap9001_header_event_background, width, mRowHeaderHeight)
        mFooterEventBackground = drawableToBitmap(R.drawable.ap9001_footer_event_background, width, mRowFooterHeight)
        mDateBackground = drawableToBitmap(R.drawable.ap9001_date_background, mColumnDateWidth, mRowDateHeight)
        mTodayDateBackground =
                drawableToBitmap(R.drawable.ap9001_today_date_background, mColumnDateWidth, mRowDateHeight)
    }

    private fun initPaint() {
        mDefaultPaint.apply {
            strokeWidth = DEFAULT_STROKE_WIDTH.toFloat()
            color = context.resources.getColor(R.color.common_text_primary)
            textSize = 40f
        }
        mHeaderEventTextPaint.apply {
            strokeWidth = DEFAULT_STROKE_WIDTH.toFloat()
            color = mHeaderEventTextColor
            textSize = mHeaderEventTextSize.toFloat()
        }
        mDatePaint.apply {
            strokeWidth = DEFAULT_STROKE_WIDTH.toFloat()
            color = mDateTextColor
            textSize = mDateTextSize.toFloat()
        }
        mHeaderEventBackgroundPaint.apply {
            color = Color.LTGRAY
        }

        mEventBackgroundPaint.apply {
            color = context.resources.getColor(R.color.ap9001_shadow_5)
        }
    }

    private fun initData() {
        mHeaderEvent.clear()
        mHeaderEvent.add("出勤") // firstCheckIn
        mHeaderEvent.add("退勤") // lastCheckOut
        mHeaderEvent.add("滞在") // hospitalStayTime
        mHeaderEvent.add("超過") // extendedTime
        mHeaderEvent.add("残業") // overtime
        mHeaderEvent.add("その他") // otherTime
        mHeaderEvent.add("休憩") // breaktime
        mHeaderEvent.add("深夜") // nightWorkTime


        onLoadDataSuccess()

        mHeaderEventTextHeight = getTextBound(mHeaderEvent[0], mDefaultPaint).height()
    }

    private fun onLoadDataSuccess() {
        mEventDateRects.clear()
        val timeSheet = TimeSheet()
        for(i in 1..getNumberOfDayInCurrentMonth()){
            timeSheet.timeSheetItems.add(
                TimeSheetItem(
                    i,
                    "01:00",
                    "00:00",
                    "00:00",
                    "00:00",
                    "00:00",
                    "00:00",
                    "00:00",
                    "00:00"
                )
            )
        }

        for (i in 1..getNumberOfDayInCurrentMonth()) {
            timeSheet.timeSheetItems.firstOrNull {
                if (it.dayOfMonth == null) {
                    false
                } else {
                    it.dayOfMonth == i
                }
            }.let {
                if (it == null) {
                    mEventDateRects.add(EventDateRect(null, TimeSheetItem(dayOfMonth = i)))
                } else {
                    mEventDateRects.add(EventDateRect(null, it))
                }
            }
        }

    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        debug("mCurrentOrigin.x: " + mCurrentOrigin.x)
        debug("mCurrentOrigin.y: " + mCurrentOrigin.y)
        debug("height: " + height)
        debug("width: " + width)
        debug("getMaxHorizontalScroll(): " + getMaxHorizontalScroll())
        debug("getMaxVerticalScroll(): " + getMaxVerticalScroll())

        drawDateColumn(canvas)
        drawHeaderEvent(canvas)
        drawEventAndAxes(canvas)
        drawFooterEvent(canvas)
    }

    private fun drawFooterEvent(canvas: Canvas) {
        //draw total background
        //draw header event background
        canvas.clipRect(
            RectF(0F, height - mRowFooterHeight.toFloat(), width.toFloat(), height.toFloat()),
            Region.Op.REPLACE
        )
        if (mFooterEventBackground == null) {
            mFooterEventBackground =
                    drawableToBitmap(R.drawable.ap9001_footer_event_background, width, mRowFooterHeight)
        }
        canvas.drawBitmap(
            mFooterEventBackground!!,
            0f,
            height - mRowFooterHeight.toFloat(),
            mHeaderEventBackgroundPaint
        )

        val totalHeaderRect = Rect(0, height - mRowFooterHeight, mColumnDateWidth, height)
        canvas.clipRect(totalHeaderRect, Region.Op.REPLACE)
        //draw total title
        val leftTotalText = (mColumnDateWidth - mDefaultPaint.measureText("Total")) / 2
        val topTotalText = height - (mRowFooterHeight - getTextBound("Total", mDefaultPaint).height()) / 2
        canvas.drawText("Total", leftTotalText, topTotalText.toFloat(), mDefaultPaint)
        drawSumOfTime(canvas)

    }

    private fun drawSumOfTime(canvas: Canvas) {
        val totalDataRect = Rect(mColumnDateWidth, height - mRowFooterHeight, width, height)
        canvas.clipRect(totalDataRect, Region.Op.REPLACE)
        mHeaderEvent.indices.forEach { index ->
            val leftColumn = index * mColumnEventWidth + mCurrentOrigin.x + mColumnDateWidth
            val rightColum = leftColumn + mColumnEventWidth
            var data: String? = "-"
            when (index) {
                0, 1 -> {
                    data = "-"
                }
                2 -> {
                    data = mTimeSheet.sumOfTimeInCell?.columnInHospitalTime ?: ""
                }
                3 -> {
                    data = mTimeSheet.sumOfTimeInCell?.columnExtendedTime ?: ""
                }
                4 -> {
                    data = mTimeSheet.sumOfTimeInCell?.columnOvertime ?: ""
                }
                5 -> {
                    data = mTimeSheet.sumOfTimeInCell?.columnOthertime ?: ""
                }
                6 -> {
                    data = mTimeSheet.sumOfTimeInCell?.columnBreakTime ?: ""
                }
                7 -> {
                    data = mTimeSheet.sumOfTimeInCell?.columnNightWorkTime ?: ""
                }
            }
            val leftText = (rightColum + leftColumn - mDefaultPaint.measureText(data)) / 2
            val topText = (height - mRowFooterHeight / 2 + getTextBound(data!!, mDefaultPaint).height() / 2).toFloat()
            canvas.drawText(data, leftText, topText, mDefaultPaint)
        }
    }

    private fun drawHeaderEvent(canvas: Canvas) {
        //draw header event background
        canvas.clipRect(RectF(0F, 0F, width.toFloat(), mRowHeaderHeight.toFloat() + 100), Region.Op.REPLACE)
        if (mHeaderEventBackground == null) {
            mHeaderEventBackground =
                    drawableToBitmap(R.drawable.ap9001_header_event_background, width, mRowHeaderHeight)
        }
        mHeaderEventBackgroundPaint.style = Paint.Style.STROKE;
        mHeaderEventBackgroundPaint.setShadowLayer(5.0f, 10.0f, 10.0f, Color.BLACK);
        canvas.drawBitmap(mHeaderEventBackground!!, 0f, 0f, mHeaderEventBackgroundPaint)

        //draw header event text
        val headerEventRect = Rect(mColumnDateWidth, 0, width, mRowHeaderHeight)
        canvas.clipRect(headerEventRect, Region.Op.REPLACE)
        val topText = (mRowHeaderHeight + mHeaderEventTextHeight) / 2
        mHeaderEvent.indices.forEach { index ->
            val leftColumn = index * mColumnEventWidth + mCurrentOrigin.x + mColumnDateWidth
            val rightColum = leftColumn + mColumnEventWidth
            val textWidth = mHeaderEventTextPaint.measureText(mHeaderEvent[index])
            val leftText = (rightColum + leftColumn) / 2 - textWidth / 2
            canvas.drawText(mHeaderEvent[index], leftText, topText.toFloat(), mHeaderEventTextPaint)
        }
    }

    private fun drawableToBitmap(id: Int, width: Int, height: Int): Bitmap? {
        if (width <= 0) {
            return null
        }
        val drawable = context.resources.getDrawable(id)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun drawDateColumn(canvas: Canvas) {
        val numberOfDayInMonth = getNumberOfDayInCurrentMonth()
        val maxHeight: Int = if (mRowDateHeight * numberOfDayInMonth > height) {
            height
        } else {
            mRowDateHeight * numberOfDayInMonth
        }
        val dateRect = Rect(0, mRowHeaderHeight, mColumnDateWidth, maxHeight - mRowFooterHeight)
        canvas.clipRect(dateRect, Region.Op.REPLACE)
        val textHeight = getTextBound("I", mDatePaint).height()
        for (i in 1..numberOfDayInMonth) {
            val startY = mCurrentOrigin.y + (i - 1) * mRowDateHeight + mRowHeaderHeight
            val stopY = mCurrentOrigin.y + mRowDateHeight * i + mRowHeaderHeight
            val targetDate = Calendar.getInstance()
            targetDate[Calendar.DAY_OF_MONTH] = i
            //draw date background
            if (targetDate.isTheSameDay(Calendar.getInstance())) {
                canvas.drawBitmap(mTodayDateBackground!!, 0f, startY, mDatePaint)
            } else {
                canvas.drawBitmap(mDateBackground!!, 0f, startY, mDatePaint)
            }
            //draw day text
            val topText = (stopY + startY + textHeight) / 2
            val dayTitle = targetDate.getDayTitle(context, "%d日 %s")
            val leftText = mColumnDateWidth * 3 / 4 - mDatePaint.measureText(dayTitle)
            mDatePaint.color = getDayOfWeekColor(targetDate)
            canvas.drawText(dayTitle, leftText, topText, mDatePaint)
            //draw line bellow date
        }
    }

    private fun drawEventAndAxes(canvas: Canvas) {

        mHeaderEvent.indices.forEach { index ->
            if (index % 2 == 1) {
                val leftColumn = index * mColumnEventWidth + mCurrentOrigin.x + mColumnDateWidth
                val rightColum = leftColumn + mColumnEventWidth
                val backgroundRect =  RectF(leftColumn, mRowHeaderHeight.toFloat(), rightColum, (height - mRowFooterHeight).toFloat())
                canvas.clipRect(backgroundRect)
                canvas.drawRoundRect(backgroundRect, 0F, 0F, mEventBackgroundPaint)
            }
        }
        val eventRect = Rect(mColumnDateWidth, mRowHeaderHeight, width, height - mRowFooterHeight)
        canvas.clipRect(eventRect, Region.Op.REPLACE)
        for (i in 1..getNumberOfDayInCurrentMonth()) {
            val startY = mCurrentOrigin.y + (i - 1) * mRowDateHeight + mRowHeaderHeight
            val stopY = startY + mRowDateHeight
            canvas.drawLine(0F, stopY, width.toFloat(), stopY, mDefaultPaint)
        }
        mEventDateRects.indices.forEach { index ->
            val startY = mCurrentOrigin.y + (index) * mRowDateHeight + mRowHeaderHeight
            val stopY = mCurrentOrigin.y + mRowDateHeight * (index + 1) + mRowHeaderHeight
            val dateRect = Rect(mColumnDateWidth, startY.toInt(), width, stopY.toInt())
            mEventDateRects[index].rect = dateRect
            drawEvent(mEventDateRects[index], startY, stopY, canvas) // row
        }
    }

    /**
     * draw event
     * 0->firstCheckIn
     * 1->lastCheckOut
     * 2->hospitalStayTime
     * 3->extendedTime
     * 4->overtime
     * 5->otherTime
     * 6->breaktime
     * 7->nightWorkTime
     */
    private fun drawEvent(eventDateRect: EventDateRect, startY: Float, stopY: Float, canvas: Canvas) {
        mHeaderEvent.indices.forEach { index ->
            val leftColumn = index * mColumnEventWidth + mCurrentOrigin.x + mColumnDateWidth
            val rightColum = leftColumn + mColumnEventWidth
            var data = ""
            var leftText = 0f
            var topText = 0f
            when (index) {
                0 -> {
                    data = eventDateRect.timeSheetItem?.checkInTime ?: ""
                    leftText = (rightColum + leftColumn - mDefaultPaint.measureText(data)) / 2
                    topText = (startY + stopY + getTextBound(data, mDefaultPaint).height()) / 2
                }
                1 -> {
                    data = eventDateRect.timeSheetItem?.checkOutTime ?: ""
                    leftText = (rightColum + leftColumn - mDefaultPaint.measureText(data)) / 2
                    topText = (startY + stopY + getTextBound(data, mDefaultPaint).height()) / 2
                }
                2 -> {
                    data = eventDateRect.timeSheetItem?.inHospitalTime ?: ""
                    leftText = (rightColum + leftColumn - mDefaultPaint.measureText(data)) / 2
                    topText = (startY + stopY + getTextBound(data, mDefaultPaint).height()) / 2
                }
                3 -> {
                    data = eventDateRect.timeSheetItem?.extendedTime ?: ""
                    leftText = (rightColum + leftColumn - mDefaultPaint.measureText(data)) / 2
                    topText = (startY + stopY + getTextBound(data, mDefaultPaint).height()) / 2
                }
                4 -> {
                    data = eventDateRect.timeSheetItem?.overTime ?: ""
                    leftText = (rightColum + leftColumn - mDefaultPaint.measureText(data)) / 2
                    topText = (startY + stopY + getTextBound(data, mDefaultPaint).height()) / 2
                }
                5 -> {
                    data = eventDateRect.timeSheetItem?.otherTime ?: ""
                    leftText = (rightColum + leftColumn - mDefaultPaint.measureText(data)) / 2
                    topText = (startY + stopY + getTextBound(data, mDefaultPaint).height()) / 2
                }
                6 -> {
                    data = eventDateRect.timeSheetItem?.breakTime ?: ""
                    leftText = (rightColum + leftColumn - mDefaultPaint.measureText(data)) / 2
                    topText = (startY + stopY + getTextBound(data, mDefaultPaint).height()) / 2
                }
                7 -> {
                    data = eventDateRect.timeSheetItem?.nightWorkTime ?: ""
                    leftText = (rightColum + leftColumn - mDefaultPaint.measureText(data)) / 2
                    topText = (startY + stopY + getTextBound(data, mDefaultPaint).height()) / 2
                }

            }
            canvas.drawText(data, leftText, topText, mDefaultPaint)
        }
    }

    /**
     * sunday -> kind of red color
     * sunday -> kind of blue color
     * other -> otherwise
     */
    private fun getDayOfWeekColor(calendar: Calendar): Int {
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> {
                mSundayDateTextColor
            }
            Calendar.SATURDAY -> {
                mSaturdayTextColor
            }
            else -> {
                Color.BLACK
            }

        }
    }

    /**
     * text bound for width and height
     */
    private fun getTextBound(text: String, paint: Paint): Rect {
        val boundingRect = Rect()
        paint.getTextBounds(text, 0, text.length, boundingRect)
        return boundingRect
    }

    /**
     * number of day in current month
     */
    private fun getNumberOfDayInCurrentMonth(): Int {
        return mCurrentDate.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    /**
     * max range vertical scroll
     */
    private fun getMaxVerticalScroll(): Int {
        return getNumberOfDayInCurrentMonth() * mRowDateHeight + mRowHeaderHeight + mRowFooterHeight - height
    }

    /**
     * max range horizontal scroll
     */
    private fun getMaxHorizontalScroll(): Int {
        return mHeaderEvent.size * mColumnDateWidth - width + mColumnDateWidth
    }

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

    /**
     * compute scroll on fling
     */
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

    inner class EventDateRect(var rect: Rect?, var timeSheetItem: TimeSheetItem?)
}





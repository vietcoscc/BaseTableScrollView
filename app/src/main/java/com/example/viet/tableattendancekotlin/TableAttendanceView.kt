package com.example.viet.tableattendancekotlin

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.support.v4.content.ContextCompat
import android.support.v4.view.GestureDetectorCompat
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.OverScroller
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class TableAttendanceView : View {
    companion object {
        const val DEBUG = true
        const val DEFAULT_STROKE_WIDTH = 1
    }

    //region attrs
    private enum class Direction {
        LEFT, RIGHT, VERTICAL, NONE
    }

    private var mColumnDateWidth =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100f, resources.displayMetrics).toInt()
        set(value) {
            field = value
            invalidate()
        }
    private var mColumnEventWidth =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100f, resources.displayMetrics).toInt()
        set(value) {
            field = value
            invalidate()
        }
    private var mRowHeaderHeight =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40f, resources.displayMetrics).toInt()
        set(value) {
            field = value
            invalidate()
        }
    private var mRowFooterHeight =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40f, resources.displayMetrics).toInt()
        set(value) {
            field = value
            invalidate()
        }
    private var mRowDateHeight =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, resources.displayMetrics).toInt()
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
    private var mHeaderEventTextSize =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15f, resources.displayMetrics)
        set(value) {
            field = value
            invalidate()
        }
    private var mEventTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15f, resources.displayMetrics)
        set(value) {
            field = value
            invalidate()
        }
    private var mDateTextColor = Color.BLACK
        set(value) {
            field = value
            invalidate()
        }
    private var mDateTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15f, resources.displayMetrics)
        set(value) {
            field = value
            invalidate()
        }
    private var mXScrollingSpeed = 0.8f
        set(value) {
            field = value
            invalidate()
        }
    private var mYScrollingSpeed = 1f
        set(value) {
            field = value
            invalidate()
        }
    private var mScrollingFriction = 1.5f
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
    private var mFooterEventBackgroundColor = Color.LTGRAY
        set(value) {
            field = value
            invalidate()
        }
    private var mShadowThickness =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.5f, resources.displayMetrics)
        set(value) {
            field = value
            invalidate()
        }

    //endregion attrs

    //region paint
    private val mDefaultPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val mEventBackgroundPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val mEventTextPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val mTodayEventBackgroundPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val mTodayEventTextPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val mHeaderEventTextPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val mHeaderEventBackgroundPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val mFooterEventBackgroundPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val mDatePaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val mTodayDatePaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val mTodayDateBackgroundPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val mDateBackgroundPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    //endregion paint
    //region bitmap
    private var mHeaderEventShadow: Bitmap? = null
    private var mFooterEventShadow: Bitmap? = null
    private var mDateShadow: Bitmap? = null
    private var mTodayDateBackground: Bitmap? = null
    //endregion bitmap
    //region content var
    private val mCurrentDate = Calendar.getInstance()
    private val mDayOfWeekInMonth: HashMap<Int, Int> = HashMap() // key = dayOfMonth, value = dayOfWeek
    private lateinit var mHeaderEvent: Array<String>
    private val mTotalData = ArrayList<String>()
    private val mEventDateRects = ArrayList<EventDateRect>()
    private var mHeaderEventTextHeight: Int = 0

    private var mFirstVisibleDay: Int = 1 // start at 1
    private var mLastVisibleDay: Int = 1

    private var mFirstVisibleColumn: Int = 0 // start at 0
    private var mLastVisibleColumn: Int = 0

    private lateinit var mHeaderEventRect: Rect
    private lateinit var mHeaderEventBackgroundRect: RectF
    private lateinit var mDateRect: Rect
    private lateinit var mDateBackgroundRect: Rect
    private lateinit var mEventRect: Rect
    private lateinit var mFooterEventRect: Rect
    private lateinit var mFooterTotalEventRect: Rect
    private lateinit var mFooterTotalDataEventRect: Rect
    private lateinit var mFooterEventBackgroundRect: RectF
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
        mHeaderEventBackgroundColor = ContextCompat.getColor(context, R.color.ap9001_common_background)
        mFooterEventBackgroundColor = ContextCompat.getColor(context, R.color.ap9001_common_background)
        mDefaultDateBackroundColor = ContextCompat.getColor(context, R.color.ap9001_common_background)
        init()
    }

    private fun init() {
        mScroller = OverScroller(context)
        mScroller.setFriction((ViewConfiguration.getScrollFriction() * mScrollingFriction).toFloat())
        mGestureDetector = GestureDetectorCompat(context, mGestureListener)
        mScaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
        initPaint()
        initData()

    }

    override fun onMeasure(width: Int, height: Int) {
        super.onMeasure(width, height)
        initBitmap()
        initAreaRect()
    }

    private fun initAreaRect() {
        mHeaderEventRect = Rect(0, 0, width, (mRowHeaderHeight + mShadowThickness).toInt())
        mHeaderEventBackgroundRect = RectF(0f, 0f, width.toFloat(), mRowHeaderHeight - mShadowThickness)
        mDateRect = Rect(0, mRowHeaderHeight, (mColumnDateWidth + mShadowThickness).toInt(), height - mRowFooterHeight)
        mDateBackgroundRect =
                Rect(0, mRowHeaderHeight, (mColumnDateWidth + mShadowThickness).toInt(), height - mRowFooterHeight)
        mEventRect = Rect()
        mFooterEventRect = Rect(0, (height - mRowFooterHeight - mShadowThickness).toInt(), width, height)
        mFooterEventBackgroundRect = RectF(
            0f, (height - mRowFooterHeight + mShadowThickness), width.toFloat(),
            height.toFloat()
        )
        mFooterTotalEventRect = Rect(0, height - mRowFooterHeight, mColumnDateWidth, height)
        mFooterTotalDataEventRect = Rect(mColumnDateWidth, height - mRowFooterHeight, width, height)
        mEventRect = Rect(mColumnDateWidth, mRowHeaderHeight, width, height - mRowFooterHeight)
    }

    private fun initBitmap() {
        mHeaderEventShadow = drawableToBitmap(R.drawable.ap9001_shadow_bottom, width, mShadowThickness.toInt())
        mFooterEventShadow = drawableToBitmap(R.drawable.ap9001_shadow_top, width, mShadowThickness.toInt())
        mDateShadow = drawableToBitmap(R.drawable.ap9001_shadow_right, mShadowThickness.toInt(), height)
        mTodayDateBackground =
                drawableToBitmap(R.drawable.ap9001_today_date_background, mShadowThickness.toInt(), height)
    }

    private fun initPaint() {
        mDefaultPaint.apply {
            strokeWidth = DEFAULT_STROKE_WIDTH.toFloat()
            color = ContextCompat.getColor(context, R.color.common_text_primary)
            textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15f, resources.displayMetrics)
        }
        mHeaderEventTextPaint.apply {
            strokeWidth = DEFAULT_STROKE_WIDTH.toFloat()
            color = mHeaderEventTextColor
            textSize = mHeaderEventTextSize
        }
        mDatePaint.apply {
            strokeWidth = DEFAULT_STROKE_WIDTH.toFloat()
            color = mDateTextColor
            textSize = mDateTextSize
        }
        mTodayDatePaint.apply {
            strokeWidth = DEFAULT_STROKE_WIDTH.toFloat()
            color = mDateTextColor
            textSize = mDateTextSize
            typeface = Typeface.DEFAULT_BOLD
        }
        mTodayDateBackgroundPaint.apply {
            color = mTodayDateBackroundColor
        }
        mDateBackgroundPaint.apply {
            color = mDefaultDateBackroundColor
        }
        mHeaderEventBackgroundPaint.apply {
            color = mHeaderEventBackgroundColor
        }
        mFooterEventBackgroundPaint.apply {
            color = ContextCompat.getColor(context, R.color.ap9001_common_background)
        }
        mEventBackgroundPaint.apply {
            strokeWidth = DEFAULT_STROKE_WIDTH.toFloat()
            color = ContextCompat.getColor(context, R.color.ap9001_common_background)
        }
        mTodayEventBackgroundPaint.apply {
            strokeWidth = DEFAULT_STROKE_WIDTH.toFloat()
            color = Color.YELLOW
        }
        mTodayEventTextPaint.apply {
            strokeWidth = DEFAULT_STROKE_WIDTH.toFloat()
            color = ContextCompat.getColor(context, R.color.common_text_primary)
            textSize = mEventTextSize
            typeface = Typeface.DEFAULT_BOLD
        }
        mEventTextPaint.apply {
            strokeWidth = DEFAULT_STROKE_WIDTH.toFloat()
            color = ContextCompat.getColor(context, R.color.common_text_primary)
            textSize = mEventTextSize
        }
    }

    private fun initData() {
        mHeaderEvent = resources.getStringArray(R.array.header_event)
        onLoadDataSuccess()
        mHeaderEventTextHeight = getTextBound(mHeaderEvent[0], mDefaultPaint).height()
    }

    private fun onLoadDataSuccess() {
        mEventDateRects.clear()
        mTotalData.clear()
        val timeSheet = TimeSheet()
        timeSheet.currentMonth = mCurrentDate
        for (i in 1..getNumberOfDayInCurrentMonth()) {
            val data = ArrayList<String>()
            data.add("01:00")
            data.add("01:00")
            data.add("01:00")
            data.add("01:00")
            data.add("01:00")
            data.add("01:00")
            data.add("01:00")
            data.add("01:00")
            timeSheet.timeSheetItems.add(
                TimeSheetItem(i, data)
            )
        }

        for (i in 1..getNumberOfDayInCurrentMonth()) {
            val targetDate = timeSheet.currentMonth
            targetDate[Calendar.DAY_OF_MONTH] = i
            mDayOfWeekInMonth[i] = targetDate[Calendar.DAY_OF_WEEK]
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

        for (i in 0..mHeaderEvent.size) {
            mTotalData.add(
                when (i) {
                    0, 1 -> {
                        "_"
                    }
                    2 -> {
                        timeSheet.sumOfTimeInCell?.columnInHospitalTime ?: ""
                    }
                    3 -> {
                        timeSheet.sumOfTimeInCell?.columnExtendedTime ?: ""
                    }
                    4 -> {
                        timeSheet.sumOfTimeInCell?.columnOvertime ?: ""
                    }
                    5 -> {
                        timeSheet.sumOfTimeInCell?.columnOthertime ?: ""
                    }
                    6 -> {
                        timeSheet.sumOfTimeInCell?.columnBreakTime ?: ""
                    }
                    7 -> {
                        timeSheet.sumOfTimeInCell?.columnNightWorkTime ?: ""
                    }
                    else -> {
                        ""
                    }
                }
            )
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
        prepairToDraw()
        drawEventAndAxes(canvas)
        drawDateColumn(canvas)
        drawHeaderEvent(canvas)
        drawFooterEvent(canvas)
    }

    private fun prepairToDraw() {
        mFirstVisibleDay = Math.abs(mCurrentOrigin.y / mRowDateHeight).toInt() + 1
        mLastVisibleDay = mFirstVisibleDay + (height - mRowHeaderHeight - mRowFooterHeight) / mRowDateHeight + 1
        if (mFirstVisibleDay < 1) {
            mFirstVisibleDay = 1
        }
        if (mLastVisibleDay > getNumberOfDayInCurrentMonth()) {
            mLastVisibleDay = getNumberOfDayInCurrentMonth()
        }
        mFirstVisibleColumn = Math.abs(mCurrentOrigin.x / mColumnEventWidth).toInt()
        mLastVisibleColumn = mFirstVisibleColumn + (width - mColumnDateWidth) / mColumnEventWidth + 1
        if (mFirstVisibleColumn < 0) {
            mFirstVisibleColumn = 0
        }
        if (mLastVisibleColumn > mHeaderEvent.size - 1) {
            mLastVisibleColumn = mHeaderEvent.size - 1
        }
        debug("mFirstVisibleDay" + mFirstVisibleDay)
        debug("mLastVisibleDay" + mLastVisibleDay)
        debug("mFirstVisibleColumn" + mFirstVisibleColumn)
        debug("mLastVisibleColumn" + mLastVisibleColumn)
    }

    /**
     * draw header row
     */
    private fun drawHeaderEvent(canvas: Canvas) {
        //draw header event background
        canvas.clipRect(mHeaderEventRect, Region.Op.REPLACE)
        canvas.drawRoundRect(mHeaderEventBackgroundRect, 0f, 0f, mHeaderEventBackgroundPaint)
        canvas.drawBitmap(mHeaderEventShadow!!, 0f, mRowHeaderHeight.toFloat(), mHeaderEventBackgroundPaint)
        mHeaderEventRect.left = mColumnDateWidth
        canvas.clipRect(mHeaderEventRect, Region.Op.REPLACE)
        mHeaderEventRect.left = 0
        //draw header event text
        mHeaderEvent.indices.forEach { index ->
            if (index in mFirstVisibleColumn..mLastVisibleColumn) {
                val leftColumn = index * mColumnEventWidth + mCurrentOrigin.x + mColumnDateWidth
                val rightColum = leftColumn + mColumnEventWidth
                val textWidth = mHeaderEventTextPaint.measureText(mHeaderEvent[index])
                val leftText = ((rightColum + leftColumn - textWidth).toInt() shr 1).toFloat()
                val topText = ((mRowHeaderHeight + mHeaderEventTextHeight) shr 1).toFloat()
                canvas.drawText(mHeaderEvent[index], leftText, topText, mHeaderEventTextPaint)
            }
        }
    }

    /**
     * draw total data row
     * @see drawSumOfTime
     */
    private fun drawFooterEvent(canvas: Canvas) {
        //draw total background
        //draw header event background
        canvas.clipRect(mFooterEventRect, Region.Op.REPLACE)
        canvas.drawRoundRect(mFooterEventBackgroundRect, 0f, 0f, mFooterEventBackgroundPaint)
        canvas.drawBitmap(
            mFooterEventShadow!!,
            0f,
            height - mRowFooterHeight - mShadowThickness,
            mFooterEventBackgroundPaint
        )

        //draw total title
        canvas.clipRect(mFooterTotalEventRect, Region.Op.REPLACE)
        val leftTotalText = (mColumnDateWidth - mDefaultPaint.measureText("合計")).toInt() shr 1
        val topTotalText = height - (mRowFooterHeight - getTextHeight("合計", mDefaultPaint) shr 1)
        canvas.drawText("合計", leftTotalText.toFloat(), topTotalText.toFloat(), mDefaultPaint)
        drawSumOfTime(canvas)

    }

    /**
     * draw total data
     */
    private fun drawSumOfTime(canvas: Canvas) {
        canvas.clipRect(mFooterTotalDataEventRect, Region.Op.REPLACE)
        mTotalData.indices.forEach { index ->
            if (index in mFirstVisibleColumn..mLastVisibleColumn) {
                val leftColumn = index * mColumnEventWidth + mCurrentOrigin.x + mColumnDateWidth
                val rightColum = leftColumn + mColumnEventWidth
                val data: String? = mTotalData[index]
                val leftText = (rightColum + leftColumn - mDefaultPaint.measureText(data)).toInt() shr 1
                val topText = height - (mRowFooterHeight - getTextHeight(data!!, mDefaultPaint) shr 1)
                canvas.drawText(data, leftText.toFloat(), topText.toFloat(), mDefaultPaint)
            }
        }
    }

    /**
     * get drawable resource
     */
    private fun drawableToBitmap(id: Int, width: Int, height: Int): Bitmap? {
        if (width <= 0) {
            return null
        }
        if (height <= 0) {
            return null
        }
        val drawable = ContextCompat.getDrawable(context, id)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable!!.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun drawDateColumn(canvas: Canvas) {
        canvas.clipRect(mDateRect, Region.Op.REPLACE)
        canvas.drawBitmap(mDateShadow!!, mColumnDateWidth.toFloat(), mRowHeaderHeight.toFloat(), mDatePaint)
        val textHeight = getTextBound("I", mDatePaint).height()
        for (i in mFirstVisibleDay..mLastVisibleDay) {
            val startY = mCurrentOrigin.y + (i - 1) * mRowDateHeight + mRowHeaderHeight
            val stopY = mCurrentOrigin.y + mRowDateHeight * i + mRowHeaderHeight
            //draw date background
            val dateBackgroundRect = RectF(
                0f,
                startY,
                mColumnDateWidth.toFloat() - mShadowThickness,
                stopY - mShadowThickness
            )
            val topText = (stopY + startY + textHeight).toInt() shr 1
            val dayTitle = i.getDayTitle(context, mDayOfWeekInMonth, "%d日 %s")
            if (i == Calendar.getInstance()[Calendar.DAY_OF_MONTH]) {
                canvas.drawRoundRect(dateBackgroundRect, 0f, 0f, mTodayDateBackgroundPaint)
                //draw day text
                val leftText = mColumnDateWidth * 3 / 4 - mTodayDatePaint.measureText(dayTitle)
                mTodayDatePaint.color = getDayOfWeekColor(mDayOfWeekInMonth[i]!!)
                canvas.drawText(dayTitle, leftText, topText.toFloat(), mTodayDatePaint)
            } else {
                canvas.drawRoundRect(dateBackgroundRect, 0f, 0f, mDateBackgroundPaint)
                //draw day text
                val leftText = mColumnDateWidth * 3 / 4 - mDatePaint.measureText(dayTitle)
                mDatePaint.color = getDayOfWeekColor(mDayOfWeekInMonth[i]!!)
                canvas.drawText(dayTitle, leftText, topText.toFloat(), mDatePaint)
            }
            //draw line bellow date
        }
    }

    private fun drawEventAndAxes(canvas: Canvas) {
        canvas.clipRect(mEventRect, Region.Op.REPLACE)
        mEventDateRects.indices.forEach { index ->
            if (index + 1 in mFirstVisibleDay..mLastVisibleDay) {
                val startY = mCurrentOrigin.y + (index) * mRowDateHeight + mRowHeaderHeight
                val stopY = mCurrentOrigin.y + mRowDateHeight * (index + 1) + mRowHeaderHeight
                val dateRect = Rect(mColumnDateWidth, startY.toInt(), width, stopY.toInt())
                mEventDateRects[index].rect = dateRect
                val targetDate = Calendar.getInstance()
                targetDate[Calendar.DAY_OF_MONTH] = index + 1
                //draw date background
                drawEvent(
                    mEventDateRects[index], startY, stopY, canvas,
                    (index + 1) == Calendar.getInstance()[Calendar.DAY_OF_MONTH]
                )
            }
        }
        for (i in mFirstVisibleDay..mLastVisibleDay) {
            val startY = mCurrentOrigin.y + (i - 1) * mRowDateHeight + mRowHeaderHeight
            val stopY = startY + mRowDateHeight
            canvas.drawLine(0F, stopY, width.toFloat(), stopY, mDefaultPaint)
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
    private fun drawEvent(eventDateRect: EventDateRect, startY: Float, stopY: Float, canvas: Canvas, isToday: Boolean) {
        mHeaderEvent.indices.forEach { index ->
            if (index in mFirstVisibleColumn..mLastVisibleColumn) {
                val leftColumn = index * mColumnEventWidth + mCurrentOrigin.x + mColumnDateWidth
                val rightColum = leftColumn + mColumnEventWidth
                val backgroundRect = RectF(leftColumn, startY, rightColum, stopY)
                if (index % 2 == 1) {
                    canvas.drawRoundRect(backgroundRect, 0F, 0F, mEventBackgroundPaint)
                }
                if (isToday) {
                    canvas.drawRoundRect(backgroundRect, 0F, 0F, mTodayEventBackgroundPaint)
                }
                val data = eventDateRect.timeSheetItem?.data!![index]
                if (isToday) {
                    val leftText = (rightColum + leftColumn - mTodayEventTextPaint.measureText(data)).toInt() shr 1
                    val topText = (startY + stopY + getTextBound(data, mTodayEventTextPaint).height()).toInt() shr 1
                    canvas.drawText(data, leftText.toFloat(), topText.toFloat(), mTodayEventTextPaint)
                } else {
                    val leftText = (rightColum + leftColumn - mEventTextPaint.measureText(data)).toInt() shr 1
                    val topText = (startY + stopY + getTextBound(data, mEventTextPaint).height()).toInt() shr 1
                    canvas.drawText(data, leftText.toFloat(), topText.toFloat(), mEventTextPaint)
                }
            }
        }
    }

    /**
     * sunday -> kind of red color
     * sunday -> kind of blue color
     * other -> otherwise
     */
    private fun getDayOfWeekColor(dayOfWeek: Int): Int {
        return when (dayOfWeek) {
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
     *
     */
    private fun getTextHeight(text: String, paint: Paint): Int {
        return getTextBound(text, paint).height()
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





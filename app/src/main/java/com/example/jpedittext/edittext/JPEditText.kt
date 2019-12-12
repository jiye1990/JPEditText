package com.example.jpedittext.edittext

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.*
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.example.jpedittext.R
import com.example.jpedittext.edittext.validation.METLengthChecker


class JPEditText : AppCompatEditText, TextWatcher, View.OnFocusChangeListener, View.OnTouchListener {

    var hideBottomText: Boolean = true
    private var bottomSpacing: Int = 0
    private var bottomTextSize: Float = 0.0f
    private var singleLineEllipsis: Boolean = false
    private var labelTextLayout: StaticLayout? = null
    private var bottomTextLayout: StaticLayout? = null
    private var textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    private var innerPaddingTop: Int = 0
    private var innerPaddingBottom: Int = 0
    private var innerPaddingLeft: Int = 0
    private var innerPaddingRight: Int = 0

    private var extraPaddingTop: Int = 0
    private var extraPaddingBottom: Int = 0 // error msg를 위한 bottom padding
    private var extraPaddingLeft: Int = 0
    private var extraPaddingRight: Int = 0

    private var hideLabelText: Boolean = false
    private var labelSpacing: Int = 0
    private var labelTextSize: Float = 0.0f
    private var labelText: String? = null

    private var errorText: String? = null

    private var bottomEllipsisSize: Int = 0
    private var minCharacters: Int = 0
    private var maxCharacters: Int = 0

    private var lengthChecker: METLengthChecker? = null

    private var underlineSize: Float = 0.0f
    private var underlineSpacing: Int = 0

    private var primaryColor: Int = 0
    private var errorColor: Int = 0
    private var baseColor: Int = 0
    private var defaultLabelColor: Int = 0

    private var icon : Bitmap? = null
    private var iconOuterWidth: Int = 0
    private var iconOuterHeight: Int = 0
    private var iconPadding: Int = 0
    private var clearButtonTouched: Boolean = false
    private var clearButtonClicking: Boolean = false

    private var charactersCountValid: Boolean = false


    constructor(context: Context) : super(context) {
        init(context, null)
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    override fun setError(error: CharSequence?) {
        errorText = error?.toString()
        hideBottomText = error == null

        if (adjustBottomLines()) {
            initPadding()
            postInvalidate()
        }
    }

    @SuppressLint("ResourceType")
    private fun init(context: Context, attrs: AttributeSet?) {

        defaultLabelColor = ContextCompat.getColor(context, R.color.gray500)
        bottomSpacing = resources.getDimensionPixelSize(R.dimen.bottom_spacing)
        underlineSpacing = resources.getDimensionPixelSize(R.dimen.underline_spacing)
        bottomEllipsisSize = resources.getDimensionPixelSize(R.dimen.bottom_ellipsis_height)
        underlineSize = 1f //resources.getDimension(R.dimen.underline_size)

        var typedArray = context.obtainStyledAttributes(attrs, R.styleable.JPEditText)

        labelText = typedArray.getString(R.styleable.JPEditText_jp_labelText)
        labelTextSize = typedArray.getDimension(R.styleable.JPEditText_jp_labelTextSize, resources.getDimension(
            R.dimen.label_text_size
        ))
        labelSpacing = typedArray.getDimensionPixelSize(R.styleable.JPEditText_jp_labelSpacing, resources.getDimension(R.dimen.label_spacing).toInt())

        bottomTextSize = typedArray.getDimension(R.styleable.JPEditText_jp_bottomTextSize, resources.getDimension(R.dimen.bottom_text_size))
        hideLabelText = typedArray.getBoolean(R.styleable.JPEditText_jp_hideLabelText, false)

        primaryColor = typedArray.getColor(R.styleable.JPEditText_jp_primaryColor, ContextCompat.getColor(context, R.color.green500))
        errorColor = typedArray.getColor(R.styleable.JPEditText_jp_errorColor, ContextCompat.getColor(context, R.color.red500))
        baseColor = typedArray.getColor(R.styleable.JPEditText_jp_baseColor, ContextCompat.getColor(context, R.color.gray100))

        icon = drawableToBitmap(typedArray.getDrawable(R.styleable.JPEditText_jp_iconRight))
        iconOuterWidth = typedArray.getDimensionPixelSize(R.styleable.JPEditText_jp_iconOuterWidth, 0)
        iconOuterHeight = typedArray.getDimensionPixelSize(R.styleable.JPEditText_jp_iconOuterHeight, 0)
        iconPadding = typedArray.getDimensionPixelSize(R.styleable.JPEditText_jp_iconPadding, 0)

        singleLineEllipsis = typedArray.getBoolean(R.styleable.JPEditText_jp_singleLineEllipsis, false)
        minCharacters = typedArray.getInt(R.styleable.JPEditText_jp_minCharacters, 0)
        maxCharacters = typedArray.getInt(R.styleable.JPEditText_jp_maxCharacters, 0)


        val paddings = intArrayOf(
            android.R.attr.padding, // 0
            android.R.attr.paddingLeft, // 1
            android.R.attr.paddingTop, // 2
            android.R.attr.paddingRight, // 3
            android.R.attr.paddingBottom // 4
        )
        val paddingsTypedArray = context.obtainStyledAttributes(attrs, paddings)
        val padding = paddingsTypedArray.getDimensionPixelSize(0, 0)
        innerPaddingLeft = paddingsTypedArray.getDimensionPixelSize(1, padding)
        innerPaddingTop = paddingsTypedArray.getDimensionPixelSize(2, padding)
        innerPaddingRight = paddingsTypedArray.getDimensionPixelSize(3, padding)
        innerPaddingBottom = paddingsTypedArray.getDimensionPixelSize(4, padding)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            background = null
        else
            setBackgroundDrawable(null)

        initPadding()
        checkCharactersCount()
    }

    private fun drawableToBitmap(drawable: Drawable?): Bitmap? {
        if (drawable == null)
            return null
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun isRTL(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return false
        }
        val config = resources.configuration
        return config.layoutDirection == View.LAYOUT_DIRECTION_RTL
    }

    private fun initPadding() {
        // 글자 써지는 영역 확보

        textPaint.textSize = bottomTextSize
        val textMetrics = textPaint.fontMetrics

        var currentBottomLines = 1 // error text의 줄 수
        extraPaddingTop = labelSpacing + if (!hideLabelText && labelText != null) labelTextSize.toInt() else 0
        extraPaddingBottom = underlineSpacing + underlineSize.dp2px(context) +
                if (!hideBottomText)
                    (bottomSpacing + ((textMetrics.descent - textMetrics.ascent) * currentBottomLines).toInt())
                else 0
        extraPaddingLeft = 0
        extraPaddingRight = if (icon == null) 0 else iconOuterWidth + iconPadding

        correctPaddings()
    }

    /**
     * Set paddings to the correct values
     */
    private fun correctPaddings() {
        var buttonsWidthLeft = 0
        var buttonsWidthRight = 0
        val buttonsWidth = iconOuterWidth * 1 // 버튼 개수
        if (isRTL()) {
            buttonsWidthLeft = buttonsWidth
        } else {
            buttonsWidthRight = buttonsWidth
        }
        super.setPadding(
            innerPaddingLeft + extraPaddingLeft + buttonsWidthLeft,
            innerPaddingTop + extraPaddingTop,
            innerPaddingRight + extraPaddingRight + buttonsWidthRight,
            innerPaddingBottom + extraPaddingBottom
        )
    }

    /**
     * use [setPaddings] instead, or the paddingTop and the paddingBottom may be set incorrectly.
     */
    @Deprecated("")
    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(left, top, right, bottom)
    }

    /**
     * Use this method instead of [setPadding] to automatically set the paddingTop and the paddingBottom correctly.
     */
    fun setPaddings(left: Int, top: Int, right: Int, bottom: Int) {
        innerPaddingTop = top
        innerPaddingBottom = bottom
        innerPaddingLeft = left
        innerPaddingRight = right
        correctPaddings()
    }

    private fun checkLength(text: CharSequence): Int {
        return lengthChecker?.getLength(text) ?: text.length
    }

    private fun getCharactersCounterText(): String {
        val text: String
        val contentText = getText().toString()
        text = if (minCharacters <= 0) {
            if (isRTL())
                maxCharacters.toString() + " / " + checkLength(contentText)
            else checkLength(
                contentText).toString() + " / " + maxCharacters
        } else if (maxCharacters <= 0) {
            if (isRTL()) "+" + minCharacters + " / " + checkLength(contentText)
            else checkLength(
                contentText).toString() + " / " + minCharacters + "+"
        } else {
            if (isRTL())
                maxCharacters.toString() + "-" + minCharacters + " / " + checkLength(contentText)
            else checkLength(contentText).toString() + " / " + minCharacters + "-" + maxCharacters
        }
        return text
    }

    private fun getBottomTextLeftOffset(): Int {
        return if (isRTL()) getCharactersCounterWidth() else getBottomEllipsisWidth()
    }

    private fun getBottomTextRightOffset(): Int {
        return if (isRTL()) getBottomEllipsisWidth() else getCharactersCounterWidth()
    }

    private fun getCharactersCounterWidth(): Int {
        return if (hasCharactersCounter()) textPaint.measureText(getCharactersCounterText()).toInt() else 0
    }

    private fun getBottomEllipsisWidth(): Int {
        return if (singleLineEllipsis) bottomEllipsisSize * 5 + 4f.dp2px(context) else 0
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            adjustTopLines()
            adjustBottomLines()
        }
    }

    private fun adjustBottomLines(): Boolean {
        if (width == 0)
            return false

        if (errorText != null) {
            textPaint.textSize = bottomTextSize

            val alignment = if (gravity and Gravity.RIGHT == Gravity.RIGHT || isRTL())
                Layout.Alignment.ALIGN_OPPOSITE
            else if (gravity and Gravity.LEFT == Gravity.LEFT)
                Layout.Alignment.ALIGN_NORMAL
            else
                Layout.Alignment.ALIGN_CENTER

            var viewWidth = width - getBottomTextLeftOffset() - getBottomTextRightOffset() - paddingLeft - paddingRight


            bottomTextLayout = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                StaticLayout(errorText, textPaint, viewWidth, alignment, 1.0f, 0.0f, true)
            else {
                val builder =
                    StaticLayout.Builder.obtain(errorText!!, 0, errorText!!.length, textPaint, viewWidth)
                builder.build()
            }
        }

        return true
    }

    private fun adjustTopLines(): Boolean {
        if (width == 0)
            return false

        if (labelText != null) {
            textPaint.textSize = labelTextSize

            val alignment = if (gravity and Gravity.RIGHT == Gravity.RIGHT || isRTL())
                Layout.Alignment.ALIGN_OPPOSITE
            else if (gravity and Gravity.LEFT == Gravity.LEFT)
                Layout.Alignment.ALIGN_NORMAL
            else
                Layout.Alignment.ALIGN_CENTER

            var viewWidth = width - getBottomTextLeftOffset() - getBottomTextRightOffset() - paddingLeft - paddingRight

            labelTextLayout = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                StaticLayout(labelText, textPaint, viewWidth, alignment, 1.0f, 0.0f, true)
            } else {
                val builder =
                    StaticLayout.Builder.obtain(labelText.toString(), 0, labelText!!.length, textPaint, viewWidth)
                builder.build()
            }
        }

        return true
    }

    override fun onDraw(canvas: Canvas) {
        val startX = scrollX
        val endX = scrollX + width
        var lineStartY = scrollY + height - paddingBottom

        // underline
        if (!isInternalValid()) // not valid
            paint.color = errorColor
        else if (!isEnabled) // disabled
            paint.color = baseColor
        else if (hasFocus()) // focused
            paint.color = primaryColor
        else  // normal
            paint.color = baseColor

        lineStartY += underlineSpacing // edittext 글자 아래부터의 간격 추가
        canvas.drawRect(
            startX.toFloat(),
            (lineStartY).toFloat(),
            endX.toFloat(),
            (lineStartY + underlineSize.dp2px(context)).toFloat(),
            paint
        )

        // draw icon
        paint.alpha = 255
        if (hasFocus() && !TextUtils.isEmpty(text) && icon != null) {
            val iconRight = endX + iconPadding + (iconOuterWidth - icon!!.width) / 2 - iconOuterWidth - iconPadding
            val iconTop = lineStartY - underlineSpacing - iconOuterHeight + (iconOuterHeight - icon!!.height) / 2
            canvas.drawBitmap(icon!!, iconRight.toFloat(), iconTop.toFloat(), paint)
        }

        textPaint.textSize = labelTextSize
        var textMetrics = textPaint.fontMetrics  // label text의 descent를 사용하기 위함

        // draw label text
        if (!hideLabelText && labelTextLayout != null) {
            textPaint.textSize = labelTextSize
            textPaint.color = defaultLabelColor

            canvas.save()
            if (isRTL())
                canvas.translate((endX - labelTextLayout!!.width).toFloat(), scrollY.toFloat() - textMetrics.descent)
            else
                canvas.translate(startX.toFloat(), scrollY.toFloat() - textMetrics.descent)

            labelTextLayout!!.draw(canvas)
            canvas.restore()
        }

        // draw bottom text
        textPaint.textSize = bottomTextSize
        textMetrics = textPaint.fontMetrics

        if (!hideBottomText && bottomTextLayout != null) {
            textPaint.textSize = bottomTextSize
            textPaint.color = errorColor

            canvas.save()
            if (isRTL())
                canvas.translate((endX - bottomTextLayout!!.width).toFloat(),
                    ((lineStartY + bottomSpacing/* + underlineSize.dp2px(context)*/).toFloat()))
            else
                canvas.translate(startX.toFloat(),
                    ((lineStartY + bottomSpacing/* + underlineSize.dp2px(context)*/).toFloat()))

            bottomTextLayout!!.draw(canvas)
            canvas.restore()
        }

        super.onDraw(canvas)
    }

    private fun isInternalValid(): Boolean {
        return errorText == null
    }

    private fun checkCharactersCount() {
        charactersCountValid = if (!hasCharactersCounter()) {
            true
        } else {
            val text = text
            val count = if (text == null) 0 else checkLength(text)
            count >= minCharacters && (maxCharacters <= 0 || count <= maxCharacters)
        }
    }

    private fun hasCharactersCounter(): Boolean {
        return minCharacters > 0 || maxCharacters > 0
    }

    fun setUnderlineBaseColor(color: Int) {
        this.baseColor = ContextCompat.getColor(context, color)
        postInvalidate()
    }

    fun setUnderlineErrorColor(color: Int) {
        this.errorColor = ContextCompat.getColor(context, color)
        postInvalidate()
    }

    fun setUnderlinePrimaryColor(color: Int) {
        this.primaryColor = ContextCompat.getColor(context, color)
        postInvalidate()
    }

    fun setBottomTextSize(size: Int) {
        bottomTextSize = size.toFloat()
        initPadding()
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when(event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                if (insideClearButton(event)) {
                    clearButtonTouched = true
                    clearButtonClicking = true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (clearButtonClicking && !insideClearButton(event))
                    clearButtonClicking = false
            }
            MotionEvent.ACTION_UP -> {
                if (clearButtonClicking) {
                    if (!TextUtils.isEmpty(text)) {
                        text = null
                        error = null
                    }
                    clearButtonClicking = false
                }
                if (clearButtonTouched) {
                    clearButtonTouched = false
                }
                clearButtonTouched = false
            }
            MotionEvent.ACTION_CANCEL -> {
                clearButtonTouched = false
                clearButtonClicking = false
            }
        }

        return super.onTouchEvent(event)
    }

    private fun insideClearButton(event: MotionEvent): Boolean {
        // 터치 이벤트 발생 위치를 버튼 위치에 국한시킴

        val x = event.x
        val y = event.y
        val startX = if (icon == null) 0 else iconOuterWidth + iconPadding
        val endX = if (icon == null) width else width - iconOuterWidth - iconPadding
        val buttonLeft: Int
        buttonLeft = if (isRTL()) startX else endX
        val buttonTop =
            scrollY + height - paddingBottom - iconOuterHeight + (iconOuterHeight - icon!!.height) / 2
        return x >= buttonLeft && x < buttonLeft + iconOuterWidth && y >= buttonTop && y < buttonTop + iconOuterHeight
    }

    override fun afterTextChanged(s: Editable?) {

    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {
        /*error = if (hasFocus() && (text!!.length < minCharacters || text!!.length > maxCharacters))
            "${minCharacters}자 이상 ${maxCharacters}자 이하로 입력해주세요."
        else
            null

        postInvalidate()*/
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return false
    }
}
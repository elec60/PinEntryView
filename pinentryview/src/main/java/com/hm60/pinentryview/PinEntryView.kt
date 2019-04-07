package com.hm60.pinentryview

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.*
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat

class PinEntryView : AppCompatEditText {

    private var maxLength = 4 // default length
    private val mSpace = toPxF(16)
    private val mCharSize = toPxF(32)
    private var mLineSpacing = toPxF(12)
    private var mLineSpacingAnimated = toPxF(12)

    private var textWidths = FloatArray(maxLength)

    var hasAnimation = false
    private var isAnimating = false
    private var animatedAlpha = 255


    private var linePaint: Paint = Paint().apply {
        isAntiAlias = true
        color = getColor(R.color.silverGray)
        style = Paint.Style.FILL
    }

    private var textPaint: TextPaint = TextPaint().apply {
        isAntiAlias = true
        color = getColor(R.color.coal)
        textSize = toPxF(18)
    }


    var lineColor = getColor(R.color.silverGray)

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.PinEntryView, 0, 0)

        if (typedArray.hasValue(R.styleable.PinEntryView_number_count)) {
            maxLength = typedArray.getInt(R.styleable.PinEntryView_number_count, 4)
            textWidths = FloatArray(maxLength)
        }

        if (typedArray.hasValue(R.styleable.PinEntryView_line_color)) {
            lineColor = typedArray.getColor(
                R.styleable.PinEntryView_line_color,
                ContextCompat.getColor(context, R.color.silverGray)
            )
        }

        if (typedArray.hasValue(R.styleable.PinEntryView_text_color))
            textPaint.color = typedArray.getInt(
                R.styleable.PinEntryView_text_color,
                ContextCompat.getColor(context, R.color.coal)
            )

        if (typedArray.hasValue(R.styleable.PinEntryView_has_animation))
            hasAnimation = typedArray.getBoolean(R.styleable.PinEntryView_has_animation, false)


        typedArray.recycle()

        setBackgroundResource(0)
        setTextIsSelectable(false)
        isCursorVisible = false
        inputType = InputType.TYPE_CLASS_NUMBER
        keyListener = DigitsKeyListener.getInstance()


        val lengthFilter = InputFilter.LengthFilter(maxLength)
        filters = arrayOf<InputFilter>(lengthFilter)

        //Disable copy paste
        super.setCustomSelectionActionModeCallback(object : ActionMode.Callback {
            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                return false
            }

            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
            }

        })

        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (hasAnimation) {
                    if (start == s!!.length && !isAnimating) {
                        animate1()
                    }
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })

        mLineSpacingAnimated = if (hasAnimation) 0f else toPxF(12)

    }

    override fun onDraw(canvas: Canvas) {
        //super.onDraw(canvas)

        setWillNotDraw(false)
        var startX = paddingLeft
        val top = height - paddingBottom

        val charSequence = text as CharSequence
        val textLength = charSequence.length
        textPaint.getTextWidths(charSequence, 0, textLength, textWidths)

        //draw lines
        var i = 0
        while (i < maxLength) {
            when {
                i < textLength -> linePaint.color = getColor(R.color.green)
                else -> linePaint.color = lineColor
            }
            canvas.drawRect(
                startX.toFloat(),
                top.toFloat() + 0,
                startX + mCharSize,
                (top + toPxF(2)),
                linePaint
            )

            startX += (mCharSize + mSpace).toInt()
            i++
        }

        //draw characters
        startX = paddingLeft
        i = 0
        if (!hasAnimation) {
            while (i < textLength) {
                val middle = startX + mCharSize / 2
                drawNumber(canvas, charSequence, i, middle, top, false)

                startX += (mCharSize + mSpace).toInt()
                i++
            }
        } else {//last character must be animate

            for (k in 0 until textLength) {
                val middle = startX + mCharSize / 2
                if ((k < textLength - 1)) {
                    drawNumber(canvas, charSequence, k, middle, top, false)
                    startX += (mCharSize + mSpace).toInt()
                } else {
                    drawNumber(canvas, charSequence, k, middle, top, true)
                }

            }

        }
    }

    private fun drawNumber(canvas: Canvas, text: CharSequence, i: Int, middle: Float, top: Int, animated: Boolean) {
        if (animated) {
            textPaint.alpha = animatedAlpha
        } else {
            textPaint.alpha = 255
        }
        canvas.drawText(
            text,
            i,
            i + 1,
            middle - textWidths[i] / 2,
            top - if (animated) mLineSpacingAnimated else mLineSpacing,
            textPaint
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        setMeasuredDimension(
            (maxLength * mCharSize).toInt() + ((maxLength - 1) * mSpace).toInt() + paddingLeft + paddingRight,
            measuredHeight
        )

    }

    private fun animate1() {
        val valueAnimator = ValueAnimator.ofFloat(0F, toPxF(12))
        valueAnimator.duration = 200
        valueAnimator.interpolator = AccelerateDecelerateInterpolator()
        valueAnimator.addUpdateListener {
            mLineSpacingAnimated = it.animatedValue as Float
            animatedAlpha = ((it.animatedValue as Float) / toPxF(12) * 255).toInt()
            postInvalidate()
        }
        valueAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                // mLineSpacingAnimated = 0F
                isAnimating = false
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
                isAnimating = true
            }

        })
        valueAnimator.start()
    }

}
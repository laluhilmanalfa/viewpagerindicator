package com.laluhilman.viewpagerindicator

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.View

/**
 * Created by laluhilman on 29/11/17.
 */
class ViewPagerIndicator : View {

    constructor(context: Context?) : super(context)
    private var animationDuration: Long = 3000
    internal var valueAnimator: ValueAnimator? = null
    private var maxItem : Int =7
    private var currentPosition : Int =1
    private var distanceIndicator : Int ?=null
    private var activeRadius : Float ?=10.toFloat()
    private var inActiveRadius : Float ?=15.toFloat()
    private var indicatorPosition: Float  = 0.toFloat()
    private var centerItem = maxItem!!/2
    private var isDefaultSizeCalculated: Boolean = false
    lateinit  private var adapter : PagerAdapter ;
    lateinit var viewPager : ViewPager;

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs){
        generateDefaultColor(context!!)
        loadAttributeValue(attrs, context!!)
        initDefaultValue()
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)


    protected var top: Float = 0.toFloat()
    protected var left: Float = 0.toFloat()
    protected var right: Float = 0.toFloat()
    protected var buttom: Float = 0.toFloat()

    private val activePaint = Paint()
    private val inActivePaint = Paint()

    private var activeColor: Int = 0
    private var inActiveColor: Int = 0
    private val mBarStrokeCap = Paint.Cap.BUTT



    private fun loadAttributeValue(attrs: AttributeSet?, context: Context) {

        val ta = context.theme.obtainStyledAttributes(attrs, R.styleable.ViewPagerIndicator, 0, 0)
        activeColor = ta.getColor(R.styleable.ViewPagerIndicator_activeColor, activeColor)
        inActiveColor = ta.getColor(R.styleable.ViewPagerIndicator_inActiveColor, inActiveColor)
        animationDuration = ta.getInteger(R.styleable.ViewPagerIndicator_animationDuration, animationDuration.toInt()).toLong()

    }

    private fun initDefaultValue() {
        activePaint.setAntiAlias(true)
        activePaint.setStyle(Paint.Style.FILL)
        activePaint.setColor(activeColor)
        activePaint.setStrokeWidth(8.toFloat())


        inActivePaint.setAntiAlias(true)
        inActivePaint.setStrokeCap(mBarStrokeCap)
        inActivePaint.setStyle(Paint.Style.STROKE)
        inActivePaint.setColor(inActiveColor)
        inActivePaint.setStrokeWidth(5.toFloat())

        distanceIndicator = (inActiveRadius!! * 2.5).toInt()
    }

    private fun calculateSize() {
        top = paddingTop.toFloat()
        buttom = (height - paddingTop - paddingBottom ).toFloat()
        left = paddingLeft.toFloat()
        right = (width - paddingRight ).toFloat()
        indicatorPosition = getXActivePosition(currentPosition)


    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec))
    }

    private fun measureHeight(measureSpec: Int): Int {
        val size = paddingTop + paddingBottom
        return View.resolveSizeAndState(size, measureSpec, 0)
    }



    private fun generateDefaultColor(context: Context) {
        activeColor = context.resources.getColor(R.color.colorPrimary)
        inActiveColor = context.resources.getColor(R.color.colorAccent)

    }

    private fun measureWidth(measureSpec: Int): Int {
        var size = paddingLeft + paddingRight
        var bounds = Rect()
        size += bounds.width()
        bounds = Rect()
        size += bounds.width()
        return View.resolveSizeAndState(size, measureSpec, 0)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if(!isDefaultSizeCalculated){
            calculateSize()
            isDefaultSizeCalculated = true
        }
        drawInActiveIndicator(canvas!!)
        drawCircle(canvas!!,indicatorPosition, (height/2).toFloat(),activeRadius!!, activePaint)
    }

    fun drawInActiveIndicator(canvas :Canvas){
        var i : Int =1
        while (i<= maxItem!!){
            drawCircle(canvas!!,getXposition(i), (height/2).toFloat(),inActiveRadius!!, inActivePaint)
            i++
        }
    }

    fun setMaxItems(maxItem :Int){
        this.maxItem = maxItem
        centerItem = maxItem!!/2
    }

    fun setCurrentPositions(currentPosition : Int){
        this.currentPosition = currentPosition!!
        animateProgress(getXActivePosition(currentPosition))
    }

    fun drawCircle(canvas : Canvas, xPosisition : Float, yPosition: Float,radius : Float, paint : Paint){
        canvas?.drawCircle(xPosisition, yPosition,radius, paint)
    }

    fun getXActivePosition(position: Int) : Float{
        if(position<= centerItem)
            return getXposition(centerItem-position+1)
        else
            return getXposition(position)
    }

    fun getXposition(position : Int) : Float{
        if(maxItem!!%2==0){
            if(position <=centerItem)
                return (width/2).toFloat()-(position*distanceIndicator!!)+(distanceIndicator!!/2)
            else
                return (width/2).toFloat()+((position-(centerItem))*distanceIndicator!!)-(distanceIndicator!!/2)
        } else {

            if(centerItem!!+1==position ){
                return (width/2).toFloat()
            } else {
                if(position <=centerItem)
                    return (width/2).toFloat()-(position*distanceIndicator!!)
                else
                    return (width/2).toFloat()+((position-(centerItem+1))*distanceIndicator!!)
            }
        }
    }

    private fun animateProgress(newPosition: Float) {
        val presviousPosition = indicatorPosition
        indicatorPosition = newPosition
        valueAnimator = ValueAnimator.ofFloat(presviousPosition, indicatorPosition)

        val changeInValue = Math.abs(newPosition.toInt() - presviousPosition.toInt())
        val durationToUse = (animationDuration * (changeInValue.toFloat() / 360.toFloat())).toLong()
        valueAnimator?.duration = durationToUse

        valueAnimator?.addUpdateListener(ValueAnimator.AnimatorUpdateListener { valueAnimator ->
            indicatorPosition = valueAnimator.animatedValue as Float
            invalidate()
        })

        valueAnimator?.start()
    }

    fun setUpViewPager(viewPager: ViewPager){
        this.viewPager = viewPager
        this.adapter = viewPager.adapter!!
        setMaxItems(adapter.count)

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }
            override fun onPageSelected(position: Int) {
                setCurrentPositions(position+1)
            }

        })

    }






}
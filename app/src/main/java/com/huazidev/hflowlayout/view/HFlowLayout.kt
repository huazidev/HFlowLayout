package com.huazidev.hflowlayout.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import kotlin.math.max
import kotlin.math.min

/**
 * @author hua on 2020/05/21.
 */
class HFlowLayout : ViewGroup {

    private val TAG = "HFlowLayout"

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Log.d(TAG, "onMeasure")

        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)

        val childCount = childCount

        // 测量子 View 尺寸
        measureChildren(widthMeasureSpec, heightMeasureSpec)

        var lineWidth = 0
        var lineHeight = 0

        var maxWidthSize = 0
        var height = 0

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility == View.GONE) {
                // 处理最后一个 View 出现的情况,上面 maxWidthSize 和 height只处理到了倒数第二行
                if (i == childCount - 1) {
                    maxWidthSize = max(lineWidth, maxWidthSize)
                    height += lineHeight
                }
                continue
            }
            val childParams = child.layoutParams as MarginLayoutParams
            // 子 View 的宽高包含他们的 margin
            val childWidth = child.measuredWidth + childParams.leftMargin + childParams.rightMargin
            val childHeight = child.measuredHeight + childParams.topMargin + childParams.bottomMargin

            if (lineWidth + childWidth > widthSpecSize - paddingLeft - paddingRight) { // 换行
                // 记录最大宽度
                maxWidthSize = max(lineWidth, maxWidthSize)
                // 重置 lineWidth
                lineWidth = childWidth
                height += lineHeight
                lineHeight = childHeight
            } else {
                lineWidth += childWidth
                lineHeight = max(childHeight, lineHeight)
            }

            // 处理最后一个 View 出现的情况,上面 maxWidthSize 和 height只处理到了倒数第二行
            if (i == childCount - 1) {
                maxWidthSize = max(lineWidth, maxWidthSize)
                height += lineHeight
            }
        }

        val resultWidth = if (widthSpecMode == MeasureSpec.EXACTLY) {
            widthSpecSize
        } else {
            min(maxWidthSize + paddingLeft + paddingRight, widthSpecSize)
        }

        val resultHeight = if (heightSpecMode == MeasureSpec.EXACTLY) {
            heightSpecSize
        } else {
            min(height + paddingTop + paddingBottom, heightSpecSize)
        }

        setMeasuredDimension(resultWidth, resultHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        Log.d(TAG, "onLayout")

        var childLeft = paddingLeft
        var childTop = paddingTop
        var lineHeight = 0

        var childParams: MarginLayoutParams

        val childCount = childCount

        for (i in 0 until childCount) {
            val child = get(i)
            if (child.visibility == View.GONE) {
                continue
            }

            childParams = child.layoutParams as MarginLayoutParams

            val childWidth = child.measuredWidth + childParams.leftMargin + childParams.rightMargin
            val childHeight = child.measuredHeight + childParams.topMargin + childParams.bottomMargin

            if (childLeft + childWidth > width - paddingRight) {
                // 换行, 更新 childLeft，childTop，lineHeight
                childLeft = paddingLeft
                childTop += lineHeight
                lineHeight = childHeight
            } else {
                lineHeight = max(lineHeight, childHeight)
            }

            setChildFrame(child, childLeft + childParams.leftMargin, childTop + childParams.topMargin, child.measuredWidth, child.measuredHeight)
            childLeft += childWidth
        }
    }

    private fun setChildFrame(child: View, left: Int, top: Int, width: Int, height: Int) {
        // 可有可无，仿照 FlexboxLayout 做了此操作
        val right = if (left + width > getWidth()) {
            getWidth() - (child.layoutParams as MarginLayoutParams).rightMargin
        } else {
            left + width
        }
        Log.d(TAG, "left=$left,right=$right,top=$top,bottom=${top + height}")
        child.layout(left, top, right, top + height)
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    override fun generateLayoutParams(p: LayoutParams?): LayoutParams {
        return MarginLayoutParams(p)
    }
}
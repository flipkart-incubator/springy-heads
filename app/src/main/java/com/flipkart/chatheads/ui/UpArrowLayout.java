package com.flipkart.chatheads.ui;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.flipkart.chatheads.R;

import java.util.ArrayList;

/**
 * Created by kirankumar on 17/02/15.
 */
public class UpArrowLayout extends ViewGroup {
    private final Point pointTo = new Point(0, 0);
    private final ArrayList<View> mMatchParentChildren = new ArrayList<View>(1);
    private ImageView arrowView;
    private int arrowDrawable = R.drawable.chat_top_arrow;

    public UpArrowLayout(Context context) {
        super(context);
        init();
    }

    public UpArrowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public UpArrowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public int getArrowDrawable() {
        return arrowDrawable;
    }

    public void setArrowDrawable(int arrowDrawable) {
        this.arrowDrawable = arrowDrawable;
        init();
    }

    private void init() {
        if (arrowView != null) {
            removeView(arrowView);
        }
        arrowView = createArrowView();
        addView(arrowView);
    }

    protected ImageView createArrowView() {
        Drawable drawable = getResources().getDrawable(arrowDrawable);
        ImageView imageView = new ImageView(getContext());
        imageView.setImageDrawable(drawable);
        return imageView;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int count = getChildCount();
        int measureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        arrowView.measure(measureSpec, measureSpec);
        int arrowViewMeasuredHeight = arrowView.getMeasuredHeight();
        int size = MeasureSpec.getSize(heightMeasureSpec);
        if (size > arrowViewMeasuredHeight) {
            size -= arrowViewMeasuredHeight + pointTo.y;
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.getMode(heightMeasureSpec));
        }


        final boolean measureMatchParentChildren =
                MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY ||
                        MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY;

        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child == arrowView) continue;
            if (child.getVisibility() != GONE) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                maxWidth = Math.max(maxWidth,
                        child.getMeasuredWidth());
                maxHeight = Math.max(maxHeight,
                        child.getMeasuredHeight());
                childState = combineMeasuredStates(childState, child.getMeasuredState());
                if (measureMatchParentChildren) {
                    if (lp.width == LayoutParams.MATCH_PARENT ||
                            lp.height == LayoutParams.MATCH_PARENT) {
                        mMatchParentChildren.add(child);
                    }
                }
            }
        }


        // Check against our minimum height and width
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());


        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                resolveSizeAndState(maxHeight, heightMeasureSpec,
                        childState << MEASURED_HEIGHT_STATE_SHIFT));

        count = mMatchParentChildren.size();
        if (count > 1) {
            for (int i = 0; i < count; i++) {
                final View child = mMatchParentChildren.get(i);

                final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                int childWidthMeasureSpec;
                int childHeightMeasureSpec;

                if (lp.width == LayoutParams.MATCH_PARENT) {
                    childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth() -
                                    lp.leftMargin - lp.rightMargin,
                            MeasureSpec.EXACTLY);
                } else {
                    childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,

                            lp.leftMargin + lp.rightMargin,
                            lp.width);
                }

                if (lp.height == LayoutParams.MATCH_PARENT) {
                    childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight() -

                                    lp.topMargin - lp.bottomMargin,
                            MeasureSpec.EXACTLY);
                } else {
                    childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,

                            lp.topMargin + lp.bottomMargin,
                            lp.height);
                }

                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }
        }


        setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight() + arrowViewMeasuredHeight + pointTo.y);
        updatePointer();

    }

    protected void measureChildWithMargins(View child,
                                           int parentWidthMeasureSpec, int widthUsed,
                                           int parentHeightMeasureSpec, int heightUsed) {
        final LayoutParams lp = child.getLayoutParams();

        final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec, widthUsed, lp.width);
        final int childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec,

                +heightUsed, lp.height);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    public void pointTo(final int viewX, final int viewY) {
        pointTo.x = viewX;
        pointTo.y = viewY;
        if (getMeasuredHeight() != 0 && getMeasuredWidth() != 0) {
            updatePointer();
        }
        invalidate();

    }


    private void updatePointer() {
        int x = (int) (pointTo.x - arrowView.getMeasuredWidth() / 2);
        int y = pointTo.y;
        if (x != arrowView.getTranslationX()) {
            arrowView.setTranslationX(x);
        }
        if (y != arrowView.getTranslationY()) {
            arrowView.setTranslationY(y);
        }

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        arrowView.layout(left, top, left + arrowView.getMeasuredWidth(), top + arrowView.getMeasuredHeight());
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == arrowView) continue;
            child.layout(left, top + arrowView.getMeasuredHeight() + pointTo.y, right, bottom);
        }

    }
    protected LayoutParams generateDefaultLayoutParams() {
        return new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new FrameLayout.LayoutParams(getContext(), attrs);
    }

}


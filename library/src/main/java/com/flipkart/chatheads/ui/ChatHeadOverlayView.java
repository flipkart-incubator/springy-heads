package com.flipkart.chatheads.ui;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.PathEffect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.flipkart.chatheads.ChatHeadUtils;

public class ChatHeadOverlayView extends View {
    private static final long ANIMATION_DURATION = 600;
    private float OVAL_RADIUS;
    private float STAMP_SPACING;
    private Path arrowDashedPath;
    private Paint paint = new Paint();
    private ObjectAnimator animator;
    private PathEffect pathDashEffect;

    public ChatHeadOverlayView(Context context) {
        super(context);
        init(context);
    }

    public ChatHeadOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        STAMP_SPACING = ChatHeadUtils.dpToPx(context, 20);
        OVAL_RADIUS = ChatHeadUtils.dpToPx(context, 3);
        animator = ObjectAnimator.ofFloat(this, "phase", 0f, -STAMP_SPACING);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setDuration(ANIMATION_DURATION);
    }

    public ChatHeadOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (arrowDashedPath != null) {
            paint.setPathEffect(pathDashEffect);
            canvas.drawPath(arrowDashedPath, paint);
        }
    }

    /**
     * taken from https://github.com/romainguy/road-trip/blob/master/application/src/main/java/org/curiouscreature/android/roadtrip/IntroView.java *
     */
    private Path makeDot(float radius) {
        Path p = new Path();
        p.addCircle(0, 0, radius, Path.Direction.CCW);
        return p;
    }

    public void drawPath(float fromX, float fromY, float toX, float toY) {
        arrowDashedPath = new Path();
        arrowDashedPath.moveTo(fromX, fromY);
        arrowDashedPath.lineTo(toX, toY);
        paint.setColor(Color.parseColor("#77FFFFFF"));
        paint.setStrokeWidth(OVAL_RADIUS * 2); // width = diameter
        animatePath();
        invalidate();
    }

    /**
     * Will be called by animator
     * @param phase
     */
    private void setPhase(float phase) {
        pathDashEffect = new PathDashPathEffect(makeDot(OVAL_RADIUS), STAMP_SPACING, phase, PathDashPathEffect.Style.ROTATE);
        invalidate();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void animatePath() {
        animator.start();

    }

    public void clearPath() {
        animator.cancel();
        arrowDashedPath = null;
        invalidate();
    }

}

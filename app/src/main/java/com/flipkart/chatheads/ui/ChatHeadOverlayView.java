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

/**
 * Created by kiran.kumar on 10/04/15.
 */
public class ChatHeadOverlayView extends View {
    private Path arrowDashedPath;
    private Paint paint = new Paint();
    private float phase;
    private ObjectAnimator animator;

    public ChatHeadOverlayView(Context context) {
        super(context);
        init(context);
    }

    public ChatHeadOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        animator = ObjectAnimator.ofFloat(this, "phase", 0f, -25f);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setDuration(500);
    }

    public ChatHeadOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (arrowDashedPath != null) {
            PathEffect effect = new PathDashPathEffect(
                    makeConvexArrow(24.0f, 7.0f),    // "stamp"
                    30.0f,                            // advance, or distance between two stamps
                    phase,                             // phase, or offset before the first stamp
                    PathDashPathEffect.Style.ROTATE); // how to transform each stamp

            // Apply the effect and draw the arrowDashedPath
            paint.setPathEffect(effect);
            canvas.drawPath(arrowDashedPath, paint);
        }
    }

    /**
     * taken from https://github.com/romainguy/road-trip/blob/master/application/src/main/java/org/curiouscreature/android/roadtrip/IntroView.java *
     */
    private Path makeConvexArrow(float length, float height) {
        Path p = new Path();
        p.addCircle(0, 0, height, Path.Direction.CCW);
//        p.moveTo(0.0f, -height / 2.0f);
//        p.lineTo(length - height / 4.0f, -height / 2.0f);
//        p.lineTo(length, 0.0f);
//        p.lineTo(length - height / 4.0f, height / 2.0f);
//        p.lineTo(0.0f, height / 2.0f);
//        p.lineTo(0.0f + height / 4.0f, 0.0f);
//        p.close();
        return p;
    }

    public void drawPath(float fromX, float fromY, float toX, float toY) {
        // Create a straight line
        arrowDashedPath = new Path();
        arrowDashedPath.moveTo(fromX, fromY);
        arrowDashedPath.lineTo(toX, toY);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4.0f);
        // Stamp a concave arrow along the line

        animatePath();
        invalidate();
    }

    private void setPhase(float phase) {
        this.phase = phase;
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

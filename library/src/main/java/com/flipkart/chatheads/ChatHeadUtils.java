package com.flipkart.chatheads;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;

/**
 * Created by kirankumar on 11/02/15.
 */
public class ChatHeadUtils {

    /**
     * Faster than {@link #dpToPx(Context, int)} since metrics is already present.
     * Use this is you are able to cache the reference to display metrics
     * @param metrics
     * @param dp
     * @return
     */
    public static int dpToPx(DisplayMetrics metrics, int dp) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
        if (px < 1.0f) {
            px = 1;
        }
        return (int) px;
    }

    public static int dpToPx(Context context, int dp) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        return dpToPx(metrics, dp);
    }

    public static int pxToDp(Context context, int px) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }
}

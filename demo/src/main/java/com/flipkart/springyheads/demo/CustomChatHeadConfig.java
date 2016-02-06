package com.flipkart.springyheads.demo;

import android.content.Context;
import android.graphics.Point;

import com.flipkart.chatheads.reboundextensions.ChatHeadUtils;
import com.flipkart.chatheads.ui.ChatHeadDefaultConfig;

/**
 * Created by kiran.kumar on 06/05/15.
 */
public class CustomChatHeadConfig extends ChatHeadDefaultConfig {
    public CustomChatHeadConfig(Context context, int xPosition, int yPosition) {
        super(context);
        setHeadHorizontalSpacing(ChatHeadUtils.dpToPx(context, -2));
        setHeadVerticalSpacing(ChatHeadUtils.dpToPx(context, 2));
        setHeadWidth(ChatHeadUtils.dpToPx(context, 50));
        setHeadHeight(ChatHeadUtils.dpToPx(context, 50));
        setInitialPosition(new Point(xPosition, yPosition));
        setCloseButtonHeight(ChatHeadUtils.dpToPx(context, 50));
        setCloseButtonWidth(ChatHeadUtils.dpToPx(context, 50));
        setCloseButtonBottomMargin(ChatHeadUtils.dpToPx(context, 100));
        setCircularRingWidth(ChatHeadUtils.dpToPx(context, 53));
        setCircularRingHeight(ChatHeadUtils.dpToPx(context, 53));
    }

    @Override
    public int getMaxChatHeads(int maxWidth, int maxHeight) {
        return (int) Math.floor(maxWidth / (getHeadWidth() + getHeadHorizontalSpacing(maxWidth, maxHeight))) - 1;
    }
}

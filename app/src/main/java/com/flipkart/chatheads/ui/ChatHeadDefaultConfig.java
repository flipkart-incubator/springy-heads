package com.flipkart.chatheads.ui;

import android.content.Context;
import android.graphics.Point;

import com.flipkart.chatheads.reboundextensions.ChatHeadUtils;

/**
 * Created by kiran.kumar on 06/05/15.
 */
public class ChatHeadDefaultConfig extends ChatHeadConfig {
    public ChatHeadDefaultConfig(Context context) {
        int diameter = 56;
        setHeadHeight(ChatHeadUtils.dpToPx(context,diameter));
        setHeadWidth(ChatHeadUtils.dpToPx(context, diameter));
        setHeadHorizontalSpacing(ChatHeadUtils.dpToPx(context, 10));
        setHeadVerticalSpacing(ChatHeadUtils.dpToPx(context, 5));
        setInitialPosition(new Point(0,ChatHeadUtils.dpToPx(context,200)));
        setCloseButtonWidth(ChatHeadUtils.dpToPx(context, 62));
        setCloseButtonHeight(ChatHeadUtils.dpToPx(context, 62));
        setCloseButtonBottomMargin(ChatHeadUtils.dpToPx(context, 50));
        setMaxChatHeads(5);
    }
}

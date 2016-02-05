package com.flipkart.chatheads.ui;

import android.graphics.Point;

/**
 * Created by kiran.kumar on 06/05/15.
 */
public class ChatHeadConfig {
    private int headHeight;
    private int headWidth;
    private int headHorizontalSpacing;
    private int headVerticalSpacing;
    private Point initialPosition;
    private int maxChatHeads;
    private int closeButtonWidth;
    private int closeButtonHeight;
    private int closeButtonBottomMargin;
    private int circularRingWidth;
    private int circularRingHeight;
    private int circularFanOutRadius;

    public void setCircularFanOutRadius(int circularFanOutRadius) {
        this.circularFanOutRadius = circularFanOutRadius;
    }

    public int getMaxChatHeads() {
        return maxChatHeads;
    }

    public void setMaxChatHeads(int maxChatHeads) {
        this.maxChatHeads = maxChatHeads;
    }

    public int getHeadHeight() {
        return headHeight;
    }

    public void setHeadHeight(int headHeight) {
        this.headHeight = headHeight;
    }

    public int getHeadWidth() {
        return headWidth;
    }

    public void setHeadWidth(int headWidth) {
        this.headWidth = headWidth;
    }

    public int getHeadHorizontalSpacing(int maxWidth, int maxHeight) {
        return headHorizontalSpacing;
    }

    public void setHeadHorizontalSpacing(int headHorizontalSpacing) {
        this.headHorizontalSpacing = headHorizontalSpacing;
    }

    public int getHeadVerticalSpacing(int maxWidth, int maxHeight) {
        return headVerticalSpacing;
    }

    public void setHeadVerticalSpacing(int headVerticalSpacing) {
        this.headVerticalSpacing = headVerticalSpacing;
    }

    public Point getInitialPosition() {
        return initialPosition;
    }

    public void setInitialPosition(Point initialPosition) {
        this.initialPosition = initialPosition;
    }

    public int getMaxChatHeads(int maxWidth, int maxHeight) {
        return maxChatHeads;
    }

    public int getCloseButtonWidth() {
        return closeButtonWidth;
    }

    public void setCloseButtonWidth(int closeButtonWidth) {
        this.closeButtonWidth = closeButtonWidth;
    }

    public int getCloseButtonHeight() {
        return closeButtonHeight;
    }

    public void setCloseButtonHeight(int closeButtonHeight) {
        this.closeButtonHeight = closeButtonHeight;
    }

    public int getCloseButtonBottomMargin() {
        return closeButtonBottomMargin;
    }

    public void setCloseButtonBottomMargin(int closeButtonBottomMargin) {
        this.closeButtonBottomMargin = closeButtonBottomMargin;
    }

    public int getCircularRingWidth() {

        return circularRingWidth;
    }

    public void setCircularRingWidth(int circularRingWidth) {
        this.circularRingWidth = circularRingWidth;
    }

    public int getCircularRingHeight() {
        return circularRingHeight;
    }

    public void setCircularRingHeight(int circularRingHeight) {
        this.circularRingHeight = circularRingHeight;
    }

    public int getCircularFanOutRadius(int maxWidth, int maxHeight) {
        return circularFanOutRadius;
    }
}

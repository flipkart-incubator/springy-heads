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

    public int getHeadHorizontalSpacing() {
        return headHorizontalSpacing;
    }

    public void setHeadHorizontalSpacing(int headHorizontalSpacing) {
        this.headHorizontalSpacing = headHorizontalSpacing;
    }

    public int getHeadVerticalSpacing() {
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
}

package com.xplore.notifications;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;

/**
 * Created by Nikaoto on 7/22/2017.
 * TODO write description of this class - what it does and why.
 */

public class BadgeDrawerArrowDrawable extends DrawerArrowDrawable {

    private static final float SIZE_FACTOR = .35f;
    private static final float HALF_SIZE_FACTOR = SIZE_FACTOR / 2;

    private Paint backgroundPaint;
    private Paint textPaint;
    private boolean enabled;
    private String text;

    public BadgeDrawerArrowDrawable(Context context) {
        super(context);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.RED);
        backgroundPaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setAntiAlias(true);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize((SIZE_FACTOR + 0.1f) * getIntrinsicHeight());
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (enabled && text.length() > 0) {
            final Rect bounds = getBounds();
            final float x = (1 - HALF_SIZE_FACTOR) * bounds.width();
            final float y = HALF_SIZE_FACTOR * bounds.height();
            canvas.drawCircle(x, y, SIZE_FACTOR * bounds.width(), backgroundPaint);

            final Rect textBounds = new Rect();
            textPaint.getTextBounds(text, 0, text.length(), textBounds);
            canvas.drawText(text, x, y + textBounds.height() / 2, textPaint);
        }
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            invalidateSelf();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setBackgroundColor(int color) {
        if (backgroundPaint.getColor() != color) {
            backgroundPaint.setColor(color);
            invalidateSelf();
        }
    }

    public int getBackgroundColor() {
        return backgroundPaint.getColor();
    }

    public void setTextColor(int color) {
        if (textPaint.getColor() != color) {
            textPaint.setColor(color);
            invalidateSelf();
        }
    }

    public int getTextColor() {
        return textPaint.getColor();
    }
}

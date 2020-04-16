package com.rndash.creatureSim.Creator;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Custom button for the game engine canvas
 */
public class Button {
    private String text;
    private final int x;
    private final int y;
    private final int text_size;
    private final int textColour;
    private final int backgroundColour;

    private ButtonAction buttonAction = null;
    final Rect bounds = new Rect();
    Rect clickBounds = new Rect();

    /**
     * Custom UI Button for a canvas
     * @param text Text for button
     * @param x X location from top left (0,0)
     * @param y location from top left (0,0)
     * @param text_size Text size, in DP
     * @param textColour Text colour
     * @param backgroundColour Background of button colour
     */
    public Button(String text, int x, int y, int text_size, Color textColour, Color backgroundColour) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.text_size = text_size;
        this.textColour = textColour.toArgb();
        this.backgroundColour = backgroundColour.toArgb();
    }
    public void setOnClick(ButtonAction a) {
        this.buttonAction = a;
    }
    public final void detectClick(MotionEvent event) {
        if (clickBounds.contains((int) event.getX(), (int) event.getY()) && event.getAction() == MotionEvent.ACTION_DOWN) {
            onClicked();
        }
    }

    public void changeText(String s) {
        this.text = s;
    }

    /**
     * Draws button on canvas
     * @param c Canvas object
     * @param p Paint object
     */
    public final void draw(Canvas c, Paint p) {
        p.setTextSize(text_size);
        p.getTextBounds(text, 0, text.length(), bounds);
        p.setColor(backgroundColour);
        int targetWidth = 100;
        float width = Math.max(bounds.width() * 1.1F, targetWidth);
        int targetHeight = 100;
        float height = Math.max(bounds.height() * 1.1F, targetHeight);
        c.drawRect(x, y, x + width, y + height, p);
        clickBounds = new Rect(x, y, x + (int) width, y + (int) height);
        p.setColor(textColour);
        c.drawText(text, x + (width-bounds.width())/2, y+(height/2), p);
    }

    protected void onClicked() {
        if (buttonAction == null) {
            Log.d("BUTTON", "CLICKED!");
        } else {
            buttonAction.onClick();
        }
    }
}

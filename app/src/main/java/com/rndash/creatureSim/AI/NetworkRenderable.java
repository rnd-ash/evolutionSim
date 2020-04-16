package com.rndash.creatureSim.AI;

import android.graphics.Canvas;
import android.graphics.Paint;

public interface NetworkRenderable {
    void render(Canvas c, Paint p, int x, int y, int h, int w);
}

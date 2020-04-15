package com.rndash.creatureSim;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.rndash.creatureSim.CreatureParts.Node;
import com.rndash.creatureSim.Interfaces.Button;
import com.rndash.creatureSim.Species.Population;
import com.rndash.creatureSim.Species.Species;

import java.util.ArrayList;

public class GameEngine extends View {
    public static float SIM_MULTIPLIER = 1;
    public static int PIXELS_PER_M = 200;
    public static double CAMERA_POS_SIM_X = 0;
    public static int max_screen_width = 0;
    public static int max_screen_height = 0;
    int animation_delay;
    Button b = new Button("Show network", 0, 200, 40, Color.valueOf(Color.WHITE), Color.valueOf(Color.BLACK));
    Population population;
    Paint p;
    Thread physicsSim;
    public GameEngine(Context c, Population population) {
        super(c);
        animation_delay = 1000/100; // Aim for 100 fps - I have a 144Hz display on my phone
        p = new Paint();
        physicsSim = new Thread(new simThread());
        this.population = population;
        loop.run();
    }

    int deadCount = 0;
    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);
        max_screen_width = canvas.getWidth();
        max_screen_height = canvas.getHeight();
        PIXELS_PER_M = canvas.getWidth()/100;
        // Draw sky and ground
        canvas.drawRGB(135,206,235);
        p.setColor(Color.GREEN);
        canvas.drawRect(0.0f, max_screen_height-(2*PIXELS_PER_M), max_screen_width, max_screen_height, p);
        // draw all the creatures
        population.render(canvas, p);
        deadCount = 0;
        p.setColor(Color.BLACK);
        p.setTextSize(48);
        canvas.drawText(String.format("Max distance: %.2f meters",population.globalBestScore), 10F,50F, p);
        canvas.drawText(String.format("Generation: %d",population.batchNo), 10F,100F, p);
        b.draw(canvas, p);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        b.detectClick(event);
        return true;
    }

    Handler handler = new Handler(Looper.getMainLooper());
    Runnable loop = new Runnable() {
        @Override
        public void run() {
            invalidate();
            handler.postDelayed(this, animation_delay);
            if (max_screen_height != 0 && max_screen_width != 0 && !physicsSim.isAlive()) {
                physicsSim.start();
            }
        }
    };



    private class simThread implements Runnable {
        private long lastTime = System.currentTimeMillis();
        @Override
        public void run() {
            while (true) {
                try {
                    population.simulationTick(System.currentTimeMillis() - lastTime);
                    lastTime = System.currentTimeMillis();
                    Thread.sleep((long) (animation_delay/SIM_MULTIPLIER));
                } catch (InterruptedException ignored) {

                }
            }
        }
    }

    public void pause() {
        try {
            physicsSim.wait();
        } catch (InterruptedException e) {

        } catch (IllegalMonitorStateException e) {

        }
    }

    public void resume() {
        try {
            physicsSim.notify();
        } catch (IllegalMonitorStateException ignored) {

        }
    }
}

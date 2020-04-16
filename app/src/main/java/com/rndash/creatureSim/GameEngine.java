package com.rndash.creatureSim;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import com.rndash.creatureSim.Creator.Button;
import com.rndash.creatureSim.Creator.ButtonAction;
import com.rndash.creatureSim.Species.Population;

public class GameEngine extends View {
    public static int PIXELS_PER_M = 200;
    public static double CAMERA_POS_SIM_X = 0;
    public static int max_screen_width = 0;
    public static int max_screen_height = 0;
    public static boolean inEditMode = true;
    boolean hasWon = false;
    private CreatureBuilder cb = new CreatureBuilder();
    int animation_delay;
    Button b = new Button("Play Simulation", 0, 200, 40, Color.valueOf(Color.WHITE), Color.valueOf(Color.BLACK));
    private Population population;
    Paint p;
    Thread physicsSim;

    public GameEngine(Context c) {
        super(c);
        animation_delay = 1000/100; // Aim for 100 fps - I have a 144Hz display on my phone
        p = new Paint();
        physicsSim = new Thread(new simThread());
        loop.run();
        b.setOnClick(new ButtonAction() {
            @Override
            public void onClick() {
                if (inEditMode) {
                    inEditMode = false;
                    b.changeText("Edit creature");
                    setPopulation(new Population(cb, 10));
                } else {
                    cb.assumeEditPosition();
                    inEditMode = true;
                    b.changeText("Play Simulation");
                }
            }
        });
    }

    private void setPopulation(Population p) {
        this.population = p;
    }

    private void drawSimulation(Canvas canvas) {
        max_screen_width = canvas.getWidth();
        max_screen_height = canvas.getHeight();
        PIXELS_PER_M = canvas.getWidth()/100;
        // Draw sky and ground
        canvas.drawRGB(135,206,235);
        p.setColor(Color.GREEN);
        canvas.drawRect(0.0f, max_screen_height-(2*PIXELS_PER_M), max_screen_width, max_screen_height, p);
        // draw all the creatures
        population.render(canvas, p);
        p.setColor(Color.BLACK);
        p.setTextSize(48);
        canvas.drawText(String.format("Max distance: %.2f meters",population.maxTravelled), 10F,50F, p);
        canvas.drawText(String.format("Generation: %d",population.batchNo), 10F,100F, p);
        canvas.drawText(String.format("Mutations: %d",population.history.size()), 10F,150F, p);
        canvas.drawText("CHAMPION NETWORK", 750F,50F, p);
    }
    private void drawWinner(Canvas canvas) {

    }

    private void drawEditor(Canvas canvas) {
        max_screen_width = canvas.getWidth();
        max_screen_height = canvas.getHeight();
        PIXELS_PER_M = canvas.getWidth()/100; // Make UI A lot larger for drawing the population
        canvas.drawRGB(192,192,192);
        p.setColor(Color.BLACK);
        canvas.drawRect(0.0f, max_screen_height-(2*PIXELS_PER_M), max_screen_width, max_screen_height, p);
        cb.render(canvas, p);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (inEditMode) {
            drawEditor(canvas);
            if (cb.isCreatureValid()) {
                b.draw(canvas, p);
            }
        } else if (hasWon) {
            drawWinner(canvas);
        } else {
            drawSimulation(canvas);
            b.draw(canvas, p);
        }
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        b.detectClick(event);
        if (cb != null) {
            cb.onClick(event);
        }
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
                    if (!inEditMode && !hasWon && population != null) {
                        population.simulationTick(System.currentTimeMillis() - lastTime);
                        lastTime = System.currentTimeMillis();
                        Thread.sleep(20); // Sleep for 10MS (50fps physics)
                        // Check if genetics has won!
                        if (population.maxTravelled > 100) {
                            hasWon = true;
                            break;
                        }
                    } else if (inEditMode) {
                        Thread.sleep(500);
                        lastTime = System.currentTimeMillis();
                    }
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

package com.rndash.creatureSim;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.rndash.creatureSim.Species.Population;

public class MainActivity extends AppCompatActivity {
    GameEngine gameengine;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameengine = new GameEngine(this);
        setContentView(gameengine);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameengine.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameengine.resume();
    }
}

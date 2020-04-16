package com.rndash.creatureSim;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    GameEngine gameengine;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameengine = new GameEngine(this);
        setContentView(gameengine);
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

package com.rndash.creatureSim.CreatureParts;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;

public class Vector {
    protected double x;
    protected double y;
    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() { return x; };
    public double getY() { return y; };

    public Vector add(Vector v) {
        return new Vector(this.x + v.x, this.y + v.y);
    }

    public Vector minus(Vector v) {
        return new Vector(this.x - v.x, this.y - v.y);
    }

    public boolean equals(Vector v) {
        return this.x == getX() && this.y == getY();
    }

    public Vector divide(double d) {
        return new Vector(this.x / d, this.y / d);
    }

    public Vector times(double d) {
        return new Vector(this.x * d, this.y * d);
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getLength() {
        return Math.sqrt(Math.pow(this.x, 2.0) + Math.pow(this.y ,2.0));
    }

    public void normalise() {
        double len = this.getLength();
        if (len == 0) return;
        if (x != 0 || y != 0) {
            this.x /= len;
            this.y /= len;
        }
    }

    @SuppressLint("DefaultLocale")
    @NonNull
    @Override
    public String toString() {
        return String.format("(%.2f,%.2f)", x, y);
    }


}

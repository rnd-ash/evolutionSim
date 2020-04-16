package com.rndash.creatureSim.CreatureParts;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;

/**
 * Simple physics vector implementation
 */
public class Vector {
    protected double x;
    protected double y;
    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the X vector
     * @return X Vector component
     */
    public double getX() { return x; }

    /**
     * Returns the Y vector
     * @return Y Vector component
     */
    public double getY() { return y; }

    /**
     * Adds 'V' to this vector
     * @param v vector to add with
     * @return new Vector with total of addition
     */
    public Vector add(Vector v) {
        return new Vector(this.x + v.x, this.y + v.y);
    }

    /**
     * minus's 'V' to this vector
     * @param v vector to minus with
     * @return new Vector with total of the subtraction
     */
    public Vector minus(Vector v) {
        return new Vector(this.x - v.x, this.y - v.y);
    }

    /**
     * Checks if input vector is equal to this vector
     * @param v Comparison vector
     * @return Boolean indicating if vectors are the same or not
     */
    public boolean equals(Vector v) {
        return this.x == getX() && this.y == getY();
    }

    /**
     * Divides the vector by a constant, and returns the result
     * @param d Divisor
     * @return New vector with division applied
     */
    public Vector divide(double d) {
        return new Vector(this.x / d, this.y / d);
    }

    /**
     * Multiplies the vector by a constant, and returns the result
     * @param d Multiplier
     * @return New vector with multiplication applied
     */
    public Vector times(double d) {
        return new Vector(this.x * d, this.y * d);
    }

    /**
     * Sets the X value component
     * @param x new X component
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Sets the Y value component
     * @param y new Y component
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Returns the magnitude (AKA Length) of the vector
     * @return vector magnitude
     */
    public double getLength() {
        return Math.sqrt(Math.pow(this.x, 2.0) + Math.pow(this.y ,2.0));
    }

    /**
     * Normalises the vector
     */
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

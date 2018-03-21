package com.example.android.beacontrilateration.utils;


import java.util.ArrayList;

/**
 * Created by lottejespers.
 */
public class Kalman {

    /* Complete calculation of Kalman Filter */
    public static Double kalman(ArrayList<Double> inputValues, double initialVariance, double noise) {
        return calculate(inputValues, initialVariance, noise);
    }

    /* Calculation of Kalman Filter using default values for wireless Access Points data acquisition */
    public static Double kalman(ArrayList<Double> inputValues) {
        return calculate(inputValues, 50.0, 0.008);
    }

    /* Calculation of arithmetic mean */
    public static Double mean(ArrayList<Double> inputValues) {
        return StatUtils.mean(inputValues);
    }


    /*This method is the responsible for calculating the value refined with Kalman Filter */
    private static Double calculate(ArrayList<Double> inputValues, double initialVariance, double noise) {
        Double kalmanGain;
        Double variance = initialVariance;
        Double processNoise = noise;
        Double measurementNoise = StatUtils.variance(inputValues);
        Double mean = inputValues.get(0);

        for (Double value : inputValues) {
            variance = variance + processNoise;
            kalmanGain = variance / ((variance + measurementNoise));
            mean = mean + kalmanGain * (value - mean);
            variance = variance - (kalmanGain * variance);
        }

        return mean;
    }
}
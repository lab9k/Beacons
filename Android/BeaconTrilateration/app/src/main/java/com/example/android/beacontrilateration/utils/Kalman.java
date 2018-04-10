package com.example.android.beacontrilateration.utils;


import java.util.ArrayList;

import static java.lang.Double.NaN;

/**
 * Created by lottejespers.
 */
public class Kalman {

//    /* Complete calculation of Kalman Filter */
//    public static Double kalman(ArrayList<Double> inputValues, double initialVariance, double noise) {
//        return calculate(inputValues, initialVariance, noise);
//    }
//
//    /* Calculation of Kalman Filter using default values for wireless Access Points data acquisition */
//    public static Double kalman(ArrayList<Double> inputValues) {
//        return calculate(inputValues, 50.0, 0.008);
//    }
//
//    /* Calculation of arithmetic mean */
//    public static Double mean(ArrayList<Double> inputValues) {
//        return StatUtils.mean(inputValues);
//    }
//
//
//    /*This method is the responsible for calculating the value refined with Kalman Filter */
//    private static Double calculate(ArrayList<Double> inputValues, double initialVariance, double noise) {
//        Double kalmanGain;
//        Double variance = initialVariance;
//        Double processNoise = noise;
//        Double measurementNoise = StatUtils.variance(inputValues);
//        Double mean = inputValues.get(0);
//
//        for (Double value : inputValues) {
//            variance = variance + processNoise;
//            kalmanGain = variance / ((variance + measurementNoise));
//            mean = mean + kalmanGain * (value - mean);
//            variance = variance - (kalmanGain * variance);
//        }
//
//        return mean;
//    }



/* translated a Javascript KalmanFilter : https://github.com/wouterbulten/kalmanjs */

    private double r;
    private double q;
    private double a;
    private double b;
    private double c;
    private double cov;
    private double x;

    public Kalman(double r, double q, double a, double b, double c) {
        this.r = (r == 0) ? 1 : r;
        this.q = (q == 0) ? 1 : q;
        this.a = (a == 0) ? 1 : a;
        this.b = b;
        this.c = (c == 0) ? 1 : c;
        this.cov = NaN;
        this.x = NaN;
    }

    public double filter(double z, double u) {
        if (this.x == NaN) {
            x = (1 / c) * z;
            cov = (1 / c) * q * (1 / c);
        } else {
            // prediction
            double predX = this.predict(u);
            double predCov = this.uncertainty();
            // Kalman
            double k = predCov * c * (1 / ((c * predCov * c) + q));
            // Correction
            x = predX + k * (z - (c * predX));
            cov = predCov - (k * c * predCov);

        }
        return x;
    }

    public double predict(double u) {
        return (a * x) + (b * u);
    }

    public double uncertainty() {
        return ((a * cov) * a) + r;
    }

    public double lastMeasurement() {
        return x;
    }

    public void setMeasurementNoise(double noise) {
        q = noise;
    }

    public void setProcessNoise(double noise) {
        r = noise;
    }


    // example :
//    let random = GKRandomSource()
//    let gaussian = GKGaussianDistribution(randomSource:random,mean:0,deviation:3)
    // simple dataset :
//    var dataSet =[Double](repeating:4.0,count:20)
    // adding noise :
//    var noisyDataSet = dataSet.map({
//            (value:Double)->
//    Double in
//        return value+
//
//    Double(gaussian.nextUniform());
//})
// apply kalman filter :


//        var kalmanFilterData=noisyDataSet.map({
//        (value:Double)->Double in
//        return kalmanFilter.filter(z:value);
//        })

}
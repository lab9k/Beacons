package com.example.android.beacontrilateration.utils;

import java.util.ArrayList;

/**
 * Created by lottejespers.
 */
public class StatUtils {

    static Double variance (ArrayList<Double> values){
        Double sum = 0.0;
        Double mean = mean(values);
        for(double num : values){
            sum += Math.pow(num - mean , 2);
        }
        return sum/(values.size()-1);
    }

    static Double mean (ArrayList<Double> values){
        return sum(values)/values.size();
    }

    private static Double sum (ArrayList<Double> values){
        Double sum = 0.0;
        for (Double num : values){
            sum+=num;
        }
        return sum;
    }
}
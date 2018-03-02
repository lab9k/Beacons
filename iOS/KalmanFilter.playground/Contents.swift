
/* translated a Javascript KalmanFilter : https://github.com/wouterbulten/kalmanjs */

import GameplayKit

public class KalmanFilter {
    
    var r: Double;
    var q: Double;
    var a: Double;
    var b: Double;
    var c: Double;
    var cov: Double;
    var x: Double;
    
    init(r: Double = 1, q: Double = 1, a: Double = 1, b: Double = 0, c: Double = 1){
        self.r = r;
        self.q = q;
        self.a = a;
        self.b = b;
        self.c = c;
        self.cov = Double.nan;
        self.x = Double.nan;
    }
    
    public func filter(z: Double, u: Double = 0) -> Double {
        if x.isNaN {
            x = ( 1 / c) * z;
            cov = ( 1 / c ) * q * ( 1 / c );
        } else {
            // prediction
            let predX = self.predict(u: u);
            let predCov = self.uncertainty();
            // Kalman
            let k = predCov * c * ( 1 / ((c * predCov * c) + q));
            // Correction
            x = predX + k * (z - (c * predX));
            cov = predCov - (k * c * predCov);
            
        }
        return x;
    }
    
    func predict(u: Double = 0) -> Double {
        return (a * x) + (b * u)
    }
    
    func uncertainty() -> Double {
        return ((a * cov) * a) + r;
    }
    
    func lastMeasurement() -> Double {
        return x;
    }
    
    func setMeasurementNoise(noise: Double){
        q = noise;
    }
    
    func setProcessNoise(noise: Double){
        r = noise;
    }
}

// example :
let random = GKRandomSource()
let gaussian = GKGaussianDistribution(randomSource: random, mean: 0, deviation: 3)
// simple dataset :
var dataSet = [Double](repeating: 4.0, count: 20)
// adding noise :
var noisyDataSet = dataSet.map({
    (value: Double) -> Double in
    return value + Double(gaussian.nextUniform());
})
// apply kalman filter :
 

var kalmanFilterData = noisyDataSet.map({
    (value: Double) -> Double in
    return kalmanFilter.filter(z: value);
})

//
//  ViewController.swift
//  Tracker
//
//  Created by Jeremie Van de Walle on 2/05/18.
//  Copyright © 2018 Jeremie Van de Walle. All rights reserved.
//

import UIKit
import Firebase
import CoreLocation
import UserNotifications

class ViewController: UIViewController, CLLocationManagerDelegate {

    @IBOutlet weak var deviceName: UITextField!
    @IBOutlet weak var button: UIButton!
    @IBOutlet weak var segment: UISegmentedControl!
    
    var beacons : [Beacon] = [];
    var ref: DatabaseReference!
    let locationManager = CLLocationManager()
    var counter = 0;
    var endDate: Date?
    var buttonState = 0;
    var alreadySendAlert = false;
    var timer: Timer?
    var clickTimes = 0;
    
    override func viewDidLoad() {
        super.viewDidLoad()
        UIApplication.shared.statusBarStyle = .lightContent
        fillBeacons()
        ref = Database.database().reference()
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyThreeKilometers
        locationManager.allowsBackgroundLocationUpdates = true;
        
        if (CLLocationManager.authorizationStatus() != CLAuthorizationStatus.authorizedAlways) {
            locationManager.requestAlwaysAuthorization()
        }
        deviceName.placeholder = UIDevice.current.name
        deviceName.autocapitalizationType = .words
        deviceName.addTarget(self, action: #selector(clickFunction), for: UIControlEvents.touchDown)
        
    }
    
    @IBAction func startTracking() {
        if buttonState == 0 {
            buttonState = 1
            button.setTitle("Stop tracking", for: .normal)
            deviceName.isEnabled = false;
            segment.isEnabled = false;
            if segment.selectedSegmentIndex == 0 {
                endDate = Calendar.current.date(
                    byAdding: .hour,
                    value: +1,
                    to: Date())
            }else if segment.selectedSegmentIndex == 1 {
                endDate = Calendar.current.date(
                    byAdding: .hour,
                    value: +3,
                    to: Date())
            }else if segment.selectedSegmentIndex == 2 {
                endDate = Calendar.current.date(
                    byAdding: .year,
                    value: +3,
                    to: Date())
            }
            locationManager.startUpdatingLocation()
            for beacon in beacons {
                locationManager.startRangingBeacons(in: beacon.region);
            }
        }else{
            stopTracking()
            ref.child(deviceName.text!).removeValue()
        }
    }
    
    @IBAction func deviceNameChanged() {
        if let text = deviceName.text {
            if(text.count > 3){
                button.isEnabled = true;
                button.alpha = 1.0;
            }else{
                button.isEnabled = false;
                button.alpha = 0.5;
            }
        }
    }
    
    func locationManager(_ manager: CLLocationManager, didRangeBeacons beacons: [CLBeacon], in region: CLBeaconRegion) {
            counter += 1;
            if(endDate! >= Date()){
                if(counter < 3000) {
                    if let beacon = beacons.first {
                        stopTimer()
                        if(beacon.rssi != 0){
                            for row in 0..<self.beacons.count {
                                if(self.beacons[row].region.proximityUUID.uuidString == beacon.proximityUUID.uuidString){
                                    self.beacons[row].rssi = beacon.rssi;
                                }
                            }
                        }
                    }
                }else{
                    if buttonState == 1 {
                        let filteredbeacons = self.beacons.filter { $0.rssi != 0}
                        if filteredbeacons.count == 0 {
                            print("no beacons found")
                                if(segment.selectedSegmentIndex == 2){
                                    print("'Zolang aanwezig' was selected")
                                    timer = Timer.scheduledTimer(timeInterval: 30, target: self, selector: #selector(self.update), userInfo: nil, repeats: false)
                                }
                        }else{
                            print("Push to firebase")
                            stopTimer()
                            for beacon in filteredbeacons {
                                self.ref.child(deviceName.text!).child(beacon.id).setValue(["RSSI": beacon.rssi])
                                print("Push to firebase with rssi:", beacon.rssi)
                                beacon.rssi = 0
                            }
                        }
                        counter = 0;
                    }
                }
            }else{
                ref.child(deviceName.text!).removeValue()
                print("date ended, stop tracking")
                stopTracking()
            }
    }
    
    private func fillBeacons() {
        for i in 0...252 {
            var hex = String(i, radix: 16)
            if i < 16 {
                hex = "0\(hex)";
            }
            beacons.append(Beacon(id: "\(hex)", region: CLBeaconRegion(proximityUUID: UUID(uuidString: "e2c56db5-dffb-48d2-b060-d04f435441\(hex)")!, identifier: "\(hex)")));
        }
    }
    private func resetBeacons() {
        for beacon in beacons {
            beacon.rssi = 0;
        }
    }
    
    private func sendNotification(){
        let notification = UNMutableNotificationContent()
        notification.body = "Je bent niet (meer) in de bibliotheek, daarom is de app gestopt je te volgen. "
        let notificationTrigger = UNTimeIntervalNotificationTrigger(timeInterval: 3, repeats: false)
        let request = UNNotificationRequest(identifier: "notification", content: notification, trigger: notificationTrigger)
        UNUserNotificationCenter.current().add(request, withCompletionHandler: nil)
    }
    
    private func stopTracking(){
        print("stopped tracking")
        locationManager.stopUpdatingLocation()
        for beacon in self.beacons {
            locationManager.stopRangingBeacons(in: beacon.region)
        }
        buttonState = 0
        button.setTitle("Start tracking", for: .normal)
        deviceName.isEnabled = true;
        segment.isEnabled = true;
    }
    
    private func stopTimer(){
        timer?.invalidate()
        timer = nil
    }
    
    @objc func update() {
        print("update modus")
        if timer != nil {
        print("stop modus")
        ref.child(deviceName.text!).removeValue()
        stopTracking()
        }
    }
    
    @objc func clickFunction(textField: UITextField) {
        clickTimes += 1;
        if clickTimes == 2 {
            clickTimes = 0;
            deviceName.text = UIDevice.current.name
        }
    }
    
}


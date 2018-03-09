//
//  Verdiep0Controller.swift
//  De Krook
//
//  Created by Jeremie Van de Walle on 8/03/18.
//  Copyright © 2018 Jeremie Van de Walle. All rights reserved.
//

import Foundation
import UIKit
import CoreLocation

class VerdiepMin1Controller: UITableViewController, CLLocationManagerDelegate  {
    
    var beacons : [Beacon] = [];
    let locationManager = CLLocationManager()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        fillBeacons();
        locationManager.delegate = self
        if (CLLocationManager.authorizationStatus() != CLAuthorizationStatus.authorizedWhenInUse) {
            locationManager.requestWhenInUseAuthorization()
        }
        for beacon in beacons {
            locationManager.startRangingBeacons(in: beacon.region);
        }
    }
    
    func locationManager(_ manager: CLLocationManager, didRangeBeacons beacons: [CLBeacon], in region: CLBeaconRegion) {
        if let beacon = beacons.first {
            if(beacon.rssi != 0){
                
                var indexPaths = [IndexPath]()
                for row in 0..<self.beacons.count {
                    if(self.beacons[row].region.proximityUUID.uuidString == beacon.proximityUUID.uuidString){
                        self.beacons[row].rssi = beacon.rssi;
                        indexPaths += [IndexPath(row: row, section: 0)]
                    }
                }
                if let visibleRows = tableView.indexPathsForVisibleRows {
                    let rowsToUpdate = visibleRows.filter { indexPaths.contains($0) }
                    for row in rowsToUpdate {
                        let cell = tableView.cellForRow(at: row) as! BeaconCell
                        cell.refreshRssi();
                    }
                }
            }
        }
    }
    
    private func fillBeacons() {
        for i in 16...60 {
            let hex = String(i, radix: 16)
            beacons.append(Beacon(name: "\(i)", region: CLBeaconRegion(proximityUUID: UUID(uuidString: "e2c56db5-dffb-48d2-b060-d04f435441\(hex)")!, identifier: "\(i)")));
        }
        let hex = String(238, radix: 16)
        beacons.append(Beacon(name: "\(238)", region: CLBeaconRegion(proximityUUID: UUID(uuidString: "e2c56db5-dffb-48d2-b060-d04f435441\(hex)")!, identifier: "\(238)")));
    }
    
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return beacons.count
    }
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "Beacon", for: indexPath) as! BeaconCell
        cell.beacon = beacons[indexPath.row]
        return cell
    }
    
    override func tableView(_ tableView: UITableView, canEditRowAt indexPath: IndexPath) -> Bool {
        return true
    }
    
}



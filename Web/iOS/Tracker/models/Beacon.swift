//
//  Beacon.swift
//  Tracker
//
//  Created by Jeremie Van de Walle on 2/05/18.
//  Copyright © 2018 Jeremie Van de Walle. All rights reserved.
//

import Foundation

import Foundation
import CoreLocation

class Beacon {
    
    let id: String;
    let region: CLBeaconRegion;
    var rssi = 0;
    
    init(id: String, region: CLBeaconRegion){
        self.id = id;
        self.region = region;
    }
    
}

//
//  Beacon.swift
//  De Krook
//
//  Created by Jeremie Van de Walle on 8/03/18.
//  Copyright © 2018 Jeremie Van de Walle. All rights reserved.
//

import Foundation
import CoreLocation

class Beacon {
    
    let name: String;
    let region: CLBeaconRegion;
    var rssi = 0;

    init(name: String, region: CLBeaconRegion){
        self.name = name;
        self.region = region;
    }

}



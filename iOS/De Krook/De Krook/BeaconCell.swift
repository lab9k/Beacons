//
//  BeaconCell.swift
//  De Krook
//
//  Created by Jeremie Van de Walle on 8/03/18.
//  Copyright © 2018 Jeremie Van de Walle. All rights reserved.
//

import UIKit

class BeaconCell: UITableViewCell {
    
    @IBOutlet weak var uuidLabel: UILabel!
    @IBOutlet weak var nameLabel: UILabel!
    @IBOutlet weak var dotImage: UIImageView!
    @IBOutlet weak var rssiLabel: UILabel!
    
    var beacon: Beacon? = nil {
        didSet {
            if let beacon = beacon {
                uuidLabel.text = beacon.region.proximityUUID.uuidString;
                rssiLabel.text = "RSSI : \(beacon.rssi)";
                nameLabel.text = "Octa ID : \(beacon.name)";
                if beacon.rssi != 0 {
                    dotImage.tintColor = UIColor(red:0.06, green:0.64, blue:0.30, alpha:1.0);
                }else{
                    dotImage.tintColor = UIColor(red:0.68, green:0.12, blue:0.16, alpha:1.0);
                }
            }
        }
    }
    
    func refreshRssi() {
        if let beacon = beacon {
            rssiLabel.text = "RSSI : \(beacon.rssi)";
            if beacon.rssi != 0 {
                dotImage.tintColor = UIColor(red:0.06, green:0.64, blue:0.30, alpha:1.0);
            }
        }
    }
}

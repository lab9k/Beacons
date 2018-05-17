//
//  AppDelegate.swift
//  Tracker
//
//  Created by Jeremie Van de Walle on 2/05/18.
//  Copyright © 2018 Jeremie Van de Walle. All rights reserved.
//

import UIKit
import Firebase
import UserNotifications

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?


    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplicationLaunchOptionsKey: Any]?) -> Bool {
        // Override point for customization after application launch.
        UIApplication.shared.statusBarStyle = .lightContent
        UIApplication.shared.isStatusBarHidden = false
        window?.tintColor = UIColor(red:0.00, green:0.40, blue:0.76, alpha:1.0)
        FirebaseApp.configure()
        
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound]) {(accepted, error) in
            if !accepted {
                print("Notificaties uitgeschakeld")
            }
        }
        return true
    }



}


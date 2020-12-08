import Foundation
import sonolib
import CoreLocation


@objc(SonoNetPlugin)
class SonoNetPlugin: CDVPlugin {

    var eventCallbackId: String = ""

    fileprivate lazy var locationManager: CLLocationManager = {
        let manager = CLLocationManager()
        manager.delegate = self
        manager.desiredAccuracy = kCLLocationAccuracyBest
        manager.allowsBackgroundLocationUpdates = false
        manager.pausesLocationUpdatesAutomatically = false
        return manager
    }()

    @objc(initialize:)
    func initialize(command: CDVInvokedUrlCommand) {

        let config = SonoNetConfigBuilder { builder in

            if let apiKey: String = command.argument(at: 0) as? String {
                builder.apiKey = apiKey
            } else {
                let pluginErrorResult = CDVPluginResult (status: CDVCommandStatus_ERROR, messageAs: "ApiKey must be provided!");
                self.commandDelegate.send(pluginErrorResult, callbackId:command.callbackId)
            }
            if let isDebugging: Bool = command.argument(at: 1) as? Bool {
                builder.debugMode = isDebugging
            }
            if let notifyMe: Bool = command.argument(at: 2) as? Bool {
                builder.notifyMe = notifyMe
                locationManager.allowsBackgroundLocationUpdates = notifyMe
            }
            if let bluetoothOnly: Bool = command.argument(at: 3) as? Bool {
                builder.bluetoothOnly = bluetoothOnly
            }
            if let lat: String = command.argument(at: 4) as? String,
               let lng: String = command.argument(at: 5) as? String,
               let latf = Float(lat),
               let lngf = Float(lng) {
                builder.initialLocation = [latf, lngf]
            }
        }

        guard let sonoNetConfig = SonoNetConfig(config) else { return }

        locationManager.delegate = self
        locationManager.requestAlwaysAuthorization()

        SonoNet.shared.bind(withConfig: sonoNetConfig)
        setUpForNotifications()
        let pluginBindResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "bindSuccess")
        self.commandDelegate.send(pluginBindResult, callbackId:command.callbackId)
    }

    @objc(beaconCallback:)
    func beaconCallback(command: CDVInvokedUrlCommand) {

        SonoNet.shared.didReceiveContent = { [weak self] content in
            guard let self = self else { return }
            let jsonObject: [String: Any] = [
                "id": content.id,
                "url": content.url,
                "title": content.title
            ]

            let pluginCallbackResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: jsonObject)
            pluginCallbackResult?.keepCallback = true
            self.commandDelegate.send(pluginCallbackResult, callbackId: command.callbackId)
        }
    }

    @objc(eventCallback:)
    func eventCallback(command: CDVInvokedUrlCommand) {
        self.eventCallbackId = command.callbackId
    }

    func setUpForNotifications() {

        let options: UNAuthorizationOptions = [.badge, .sound, .alert]
        UNUserNotificationCenter.current().requestAuthorization(options: options) { success, error in
            if let error = error {
                print("Error: \(error)")
            }
        }
    }

    func buildLocationEvent(lat: Double, lng: Double, enterOrExit: String) {
        let jsonObject: [String: Any] = [
            "lat": lat,
            "long": lng,
            "enterOrExit": enterOrExit
        ]

        sendEvent(json: jsonObject)
    }

    func sendEvent(json: [String : Any]) {
        let pluginCallbackResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: json)
        pluginCallbackResult?.keepCallback = true
        self.commandDelegate.send(pluginCallbackResult, callbackId: eventCallbackId)
    }

}

extension SonoNetPlugin: CLLocationManagerDelegate {

    func locationManager(_ manager: CLLocationManager, didDetermineState state: CLRegionState, for region: CLRegion) {
    }

    func locationManager(_ manager: CLLocationManager, didEnterRegion region: CLRegion) {
        SonoNet.shared.enteredRegion(region: region, appState: UIApplication.shared.applicationState)
        if let region = region as? CLCircularRegion {
            self.buildLocationEvent(lat: region.center.latitude, lng: region.center.longitude, enterOrExit: "enter")
        }
        guard let region = region as? CLBeaconRegion, let major = region.major?.stringValue, let minor = region.minor?.stringValue else { return }
        let bleString = "\(region.proximityUUID.uuidString)_\(major)_\(minor)"
        let jsonObject: [String: Any] = [
            "bleId": bleString,
            "enterOrExit": "enter"
        ]
        sendEvent(json: jsonObject)
    }

    func locationManager(_ manager: CLLocationManager, didExitRegion region: CLRegion) {
        SonoNet.shared.exitedRegion(region: region, appState: UIApplication.shared.applicationState)
        if let region = region as? CLCircularRegion {
            self.buildLocationEvent(lat: region.center.latitude, lng: region.center.longitude, enterOrExit: "exit")
        }
        guard let region = region as? CLBeaconRegion, let major = region.major?.stringValue, let minor = region.minor?.stringValue else { return }
        let bleString = "\(region.proximityUUID.uuidString)_\(major)_\(minor)"
        let jsonObject: [String: Any] = [
            "bleId": bleString,
            "enterOrExit": "exit"
        ]
        sendEvent(json: jsonObject)
    }
}

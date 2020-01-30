import CoreLocation
import Foundation

class LocationManager : NSObject, CLLocationManagerDelegate {


	let locationManager = CLLocationManager()


	override init() {
		super.init()

		self.locationManager.delegate = self
        self.locationManager.requestAlwaysAuthorization()


    }


	func locationManager(_ manager: CLLocationManager, didDetermineState state: CLRegionState, for region: CLRegion) {
        // if UIApplication.shared.applicationState != .active && state == .inside { SonoNet.shared.sendNotification(withIdentifier: region.identifier) }
        print("didDetermineState")    
   }

    func locationManager(_ manager: CLLocationManager, didEnterRegion region: CLRegion) {
        // if UIApplication.shared.applicationState != .active { SonoNet.shared.sendNotification(withIdentifier: region.identifier) }
        print("didEnterRegion")
    }
    
   func locationManager(_ manager: CLLocationManager, didExitRegion region: CLRegion) {
       // if UIApplication.shared.applicationState != .active { SonoNet.shared.sendNotification(withIdentifier: region.identifier) }
       print("didExitRegion")
   }

}
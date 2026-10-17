1.0 (iOS and Android)
Initial release


Questions
- Pantry Locations?
- Special instructions?
- Acceptable icon and logo?

Key points:
Update Bundle Identifiers (App and Watch)

(Case sensitive)
Replace all occurances of files containing the name icon.png (10x total)
Replace all occurances of files with the name logo.png (5x total)
Replace 1 occurance of file with the name strip.png (1x total) <-- This is actually the same as logo.png
Caution - the logo.png in Barcode-PassTemplate may be too big to fit the title. Shrink down or use a dummy 1x1 blank .png

In BottomBannerView.swift, replace website and facebook page with proper URL's
In ContentView.swift, replace website with proper URL
In SettingsView, leave the Link2Feed registration URL alone
In SettingsView, update the URL's for the Apple App store and Google Play store
In pass.json, update organizationName, and logoText
In pass.json, update the location address(es)
In pass.json, update teamIdentifier (App ID Prefix - Apple developer account))
In the various Localizable strings for Barcode, update "app_title" and "website_label"
In the various Localizable strings for BarcodeWatchApp, update "app_title"
In App, Targets, General update Display Name for both iOS and watchOS app

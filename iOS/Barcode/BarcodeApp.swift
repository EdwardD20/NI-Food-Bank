//
//  Barcode/BarcodeApp.swift
//
//  Created by Ed Danley on 4/24/26.
//


import SwiftUI

enum Globals {
    #if DEBUG
    static let splashDuration: TimeInterval = 2
    #else
    static let splashDuration: TimeInterval = 4
    #endif
} // Globals

@main
struct BarcodeApp: App {
    @AppStorage("appLanguage") private var appLanguage: String = "en"
    @State private var showSplash = true

    var body: some Scene {
        WindowGroup {
            if showSplash {
                SplashScreenView()
                    .onAppear {
                        LanguageBundle.set(appLanguage) // set before first render
                        DispatchQueue.main.asyncAfter(deadline: .now() + Globals.splashDuration) {
                            showSplash = false
                        } // DispatchQueue.main.asyncAfter
                    } // .onAppear
                    .environment(\.locale, Locale(identifier: appLanguage))
                    .id(appLanguage)
            } else {
                ContentView()
                    .environment(\.locale, Locale(identifier: appLanguage))
                    .id(appLanguage)
            } // if showSplash
        } // WindowGroup
        .onChange(of: appLanguage) { _, newValue in
            LanguageBundle.set(newValue)
        } // .onChange
    } // body
} // BarcodeApp

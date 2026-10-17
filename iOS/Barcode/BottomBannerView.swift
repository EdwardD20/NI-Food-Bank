//
//  Barcode/BottomBannerView.swift
//
//  Created by Ed Danley on 2/21/26.
//

import SwiftUI
import Combine

struct BottomBannerView: View {
    @State private var showWebsite = true

    private let bannerTimer = Timer
        .publish(every: 10, on: .main, in: .common)
        .autoconnect()

    var body: some View {
        let titleKey: LocalizedStringKey =
            showWebsite ? "website" : "facebook"

        let iconName: String? =
            showWebsite ? nil : "Facebook_Logo_Primary"

        ZStack {
            ShadedBottomBarGeometry(height: 30)

            ShadedBottomBarTapLayer(
                height: 30,
                titleKey: titleKey,
                iconName: iconName
            ) {
                if showWebsite {
                    openWebsite()
                } else {
                    openFacebook()
                } // if
            } // ShadedBottomBarTapLayer
        } // ZStack
        .onReceive(bannerTimer) { _ in
            showWebsite.toggle()
        } // onReceive
    } // body

    private func openFacebook() {
        if let url = URL(string: facebookURL) {
            UIApplication.shared.open(url)
        }
    } // openFacebook

    private func openWebsite() {
        if let url = URL(string: websiteURL) {
            UIApplication.shared.open(url)
        }
    } // openWebsite
} // BottomBannerView

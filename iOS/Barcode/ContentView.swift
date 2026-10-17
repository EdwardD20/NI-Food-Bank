//
//  Barcode/ContentView.swift
//
//  Created by Ed Danley on 4/24/26.
//


import SwiftUI
import Combine
import PassKit

// Use the live window shape and both size classes. Large iPhones can have a
// regular horizontal size class in landscape, but their vertical class is compact.
// iPad and the Duo inner display have regular size classes in both dimensions.
enum MainDisplayLayout {
    static func usesTwoColumns(size: CGSize, regularWidth: Bool, regularHeight: Bool) -> Bool {
        regularWidth && regularHeight && size.width > size.height
    } // usesTwoColumns
} // enum MainDisplayLayout

struct ContentView: View {
    @Environment(\.horizontalSizeClass) private var horizontalSizeClass
    @Environment(\.verticalSizeClass) private var verticalSizeClass

    var body: some View {
        GeometryReader { geometry in
            MainScreen(usesTwoColumns: MainDisplayLayout.usesTwoColumns(
                size: geometry.size,
                regularWidth: horizontalSizeClass == .regular,
                regularHeight: verticalSizeClass == .regular
            ))
        } // GeometryReader
        // Keyboard appearance must not turn a portrait window into landscape.
        .ignoresSafeArea(.keyboard)
    } // body
} // ContentView

struct MainScreen: View {
    var usesTwoColumns: Bool = false
    @Environment(\.layoutDirection) private var layoutDirection
    @AppStorage("appLanguage") private var appLanguage: String = ""
    @AppStorage("clientDisplayStyle") private var clientDisplayStyle: String = "barcode"
    @StateObject private var model = PassModel.load()
    @State private var showingSettings = false

    private var mainDisplay: some View {
        GeometryReader { geo in
            ScrollView {
                VStack(spacing: 16) {

                    // --- Logo ---
                    if let logo = model.Logo {
                        Button(action: openWebsite) {
                            Image(uiImage: logo)
                                .resizable()
                                .scaledToFit()
                                .frame(
                                    maxWidth: geo.size.width * 0.9,
                                    maxHeight: geo.size.height * 0.18
                                )
                                .cornerRadius(6)
                                .padding(.top, 12)
                        } // Button action: openWebsite
                        .accessibilityLabel("Open NIFB website")
                        .accessibilityAddTraits(.isButton)
                    } else {
                        Color.gray.opacity(0.12)
                            .frame(height: geo.size.height * 0.14)
                            .overlay(Text("Logo").foregroundColor(.secondary))
                            .cornerRadius(6)
                            .padding(.top, 12)
                    } // if/else logo

                    // --- Title ---
                    Text(LocalizedStringKey("app_title"))
                        .font(.title2.weight(.semibold))

                    // --- Barcode section ---
                    VStack(spacing: 8) {
                        let isLandscape = geo.size.width > geo.size.height
                        let barcodeTargetWidth: CGFloat = isLandscape
                            ? geo.size.width * 0.5
                            : geo.size.width * 0.9

                        let qrTargetWidth: CGFloat = min(
                            isLandscape
                                ? geo.size.width * 0.5
                                : geo.size.width * 0.75,
                            barcodeTargetWidth
                        )

                        let targetHeight = barcodeTargetWidth / 2.0

                        let cid = model.clientId ?? ""

                        if !cid.isEmpty {
                            switch clientDisplayStyle {
                            case "barcode":
                                BarcodeView(code: cid)
                                    .frame(width: barcodeTargetWidth, height: targetHeight)
                                    .frame(maxWidth: .infinity)
                                    .padding(.horizontal, 8)

                                Text(cid)
                                    .font(.title2.weight(.semibold))
                                    .padding(.bottom, 8)
                                    .lineLimit(1)

                            case "qrcode":
                                if let qrImage = generateQRCodeImage(
                                    from: cid,
                                    size: CGSize(width: qrTargetWidth, height: qrTargetWidth)
                                ) {
                                    ZStack {
                                        Color.white

                                        Image(uiImage: qrImage)
                                            .interpolation(.none)
                                            .resizable()
                                            .scaledToFit()
                                            .padding(28)
                                    } // ZStack
                                    .frame(width: qrTargetWidth, height: qrTargetWidth)
                                } // if qrImage

                                Text(cid)
                                    .font(.title2.weight(.semibold))
                                    .padding(.bottom, 8)
                                    .lineLimit(1)

                            case "number":
                                Text(cid)
                                    .font(.system(size: 34, weight: .bold, design: .monospaced))
                                    .frame(maxWidth: .infinity)
                                    .multilineTextAlignment(.center)
                                    .padding(.vertical, 20)

                            default:
                                BarcodeView(code: cid)
                                    .frame(width: barcodeTargetWidth, height: targetHeight)
                                    .frame(maxWidth: .infinity)
                                    .padding(.horizontal, 8)

                                Text(cid)
                                    .font(.title2.weight(.semibold))
                                    .padding(.bottom, 8)
                                    .lineLimit(1)
                            } // switch
                        } else {
                            Text(LocalizedStringKey("no_client"))
                                .foregroundColor(.secondary)
                                .padding(.bottom, 8)
                        } // if cid
                    } // VStack barcode
                    .padding()
                    .background(
                        RoundedRectangle(cornerRadius: 12)
                            .fill(Color(.green))
                        
                    ) // background
                    .padding(.horizontal, 12)

                    if #available(iOS 17.0, *) {
                        // --- Wallet Button ---
                        if let clientId = model.clientId, !clientId.isEmpty {
                            Button(action: {
                                Task {
                                    do {
                                        let passData = try await PassService.shared.fetchPass(clientID: clientId)
                                        if let rootVC = UIApplication.shared.connectedScenes
                                            .compactMap({ $0 as? UIWindowScene })
                                            .first?.windows.first?.rootViewController {
                                            try WalletManager.shared.presentPass(from: passData, in: rootVC)
                                        }
                                    } catch {
                                        print("Wallet error:", error)
                                    }
                                } // Task
                            }) {
                                Image("apple-wallet")
                                    .resizable()
                                    .scaledToFit()
                                    .frame(height: 50)
                            } // Button action: present wallet pass
                            .buttonStyle(.bordered)
                            .padding(.horizontal, 12)
                            .accessibilityLabel("Add to Apple Wallet")
                            .accessibilityHint("Opens wallet pass for this client")
                        } // if clientID
                        
                        Spacer(minLength: 10)
                    } // if iOS 17+

                    // --- Facebook button ---
                    HStack {
                        Button(action: openFacebook) {
                            HStack(spacing: 8) {
                                Image("Facebook_Logo_Primary")
                                    .resizable()
                                    .scaledToFit()
                                    .frame(height: 20)
                                Text("Facebook")
                            } // HStack facebook content
                        } // Button action: openFacebook
                        .buttonStyle(.bordered)
                        .accessibilityLabel("Facebook")
                        .accessibilityAddTraits(.isButton)
                    } // HStack facebook
                    .padding(.bottom, 10)

                    // --- Settings Button ---
                    HStack {
                        Button(action: { showingSettings = true }) {
                            HStack {
                                Image(systemName: "gearshape")
                                Text(LocalizedStringKey("settings_button_label"))
                            } // HStack settings content
                        } // Button action: show settings
                        .buttonStyle(.bordered)
                        .disabled(usesTwoColumns)
                    } // HStack settings
                    .padding(.bottom, 20)                } // VStack main content
                .frame(width: geo.size.width, alignment: .center)
                .background(Color.green.ignoresSafeArea())
                .foregroundStyle(.black)
            } // ScrollView
        } // GeometryReader
    } // mainDisplay

    var body: some View {
        HStack(spacing: 0) {
            NavigationView {
                mainDisplay
                    .background(Color(.green)
                        .ignoresSafeArea())
                    .overlay(alignment: .bottom) {
                        BottomBannerView()
                    }
            }
            .navigationViewStyle(StackNavigationViewStyle())
            .environment(\.layoutDirection, layoutDirection)
            .frame(maxWidth: .infinity)

            if usesTwoColumns {
                Divider()
                SettingsView(model: model, isEmbedded: true)
                    .environment(\.locale, Locale(identifier: appLanguage))
                    .environment(\.layoutDirection, layoutDirection)
                    .frame(maxWidth: .infinity)
            }
        }
        // Keep the requested physical column order in right-to-left languages.
        .environment(\.layoutDirection, .leftToRight)
        .onChange(of: usesTwoColumns) { _, isExpanded in
            if isExpanded {
                showingSettings = false
            }
        }
        .onAppear {
            LanguageBundle.set(appLanguage) // ensure bundle matches stored language
            if appLanguage.isEmpty {
                let preferred = Locale.preferredLanguages.first ?? "en"
                let code = String(preferred.prefix(2))
                if preferred.hasPrefix("hmn") {
                    appLanguage = "hmn"
                } else if preferred.hasPrefix("arz") {
                    appLanguage = "arz"
                } else if preferred.hasPrefix("zh-Hans") {
                    appLanguage = "zh-Hans"
                } else if ["en", "es", "fr", "ar", "de", "pl", "uk", "hi", "tl", "vi", "ko", "ru", "gu", "km", "lo"].contains(code) {
                    appLanguage = code
                } else {
                    appLanguage = "en"
                } // if/else preferred
            } // if appLanguage.isEmpty
            if (model.clientId?.isEmpty ?? true) && !usesTwoColumns {
                showingSettings = true
            } // if clientId empty

            if let barcode = UserDefaults.standard.string(forKey: "LastScannedBarcode"),
               let clientId = model.clientId {

                switch clientDisplayStyle {
                case "qrcode":
                    if let qrImage = generateQRCodeImage(
                        from: barcode,
                        size: CGSize(width: 280, height: 280)
                    ) {
                        PhoneWatchSessionManager.shared.sendBarcodeAndClientId(
                            barcodeImage: qrImage,
                            clientId: clientId
                        )
                    }

                case "number":
                    // Render a simple image containing just the number so the Watch mirrors the setting
                    let rendererSize = CGSize(width: 800, height: 200)
                    let numberImage = UIGraphicsImageRenderer(size: rendererSize).image { ctx in
                        // Fill background with system background color
                        UIColor.systemBackground.setFill()
                        ctx.fill(CGRect(origin: .zero, size: rendererSize))

                        // Draw the number centered with a monospaced bold font
                        let paragraph = NSMutableParagraphStyle()
                        paragraph.alignment = .center

                        let attrs: [NSAttributedString.Key: Any] = [
                            .font: UIFont.monospacedSystemFont(ofSize: 64, weight: .bold),
                            .foregroundColor: UIColor.label,
                            .paragraphStyle: paragraph
                        ]

                        let attributed = NSAttributedString(string: barcode, attributes: attrs)
                        let textRect = CGRect(x: 0, y: (rendererSize.height - 72) / 2, width: rendererSize.width, height: 72)
                        attributed.draw(in: textRect)
                    }
                    PhoneWatchSessionManager.shared.sendBarcodeAndClientId(
                        barcodeImage: numberImage,
                        clientId: clientId
                    )

                case "barcode": fallthrough
                default:
                    if let barcodeImage = generateCode128BarcodeImage(from: barcode, size: CGSize(width: 600, height: 200)) {
                        PhoneWatchSessionManager.shared.sendBarcodeAndClientId(
                            barcodeImage: barcodeImage,
                            clientId: clientId
                        )
                    }
                }
            }
        } // onAppear
        .sheet(isPresented: Binding(
            get: { showingSettings && !usesTwoColumns },
            set: { showingSettings = $0 }
        )) {
            SettingsView(model: model)
                .environment(\.locale, Locale(identifier: appLanguage))
                .id(appLanguage)
        } // sheet
        .navigationBarTitleDisplayMode(.inline)
    } // body

    private func openWebsite() {
        if let url = URL(string: websiteURL) {
            UIApplication.shared.open(url)
        } // if open url
    } // openWebsite

    private func openFacebook() {
        if let url = URL(string: facebookURL) {
            UIApplication.shared.open(url)
        } // if open url
    } // openFacebook

} // MainScreen

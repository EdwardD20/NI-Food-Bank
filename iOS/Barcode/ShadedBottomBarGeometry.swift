//
//  Barcode/ShadedBottomBarGeometry.swift
//
//  Created by Ed Danley on 4/24/26.
//


import SwiftUI

/// Draws a frosted, shaded bar that reaches the *real* bottom edge.
/// - Parameters:
///   - height: visible bar height above the safe area (e.g. 30)
struct ShadedBottomBarGeometry: View {
    let height: CGFloat

    var body: some View {
        GeometryReader { proxy in
            let bottomInset = proxy.safeAreaInsets.bottom

            Rectangle().fill(.green)
            .frame(height: height + bottomInset)
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottom)
            .ignoresSafeArea(edges: .bottom)
        } // GeometryReader
        .allowsHitTesting(false)
        .accessibilityHidden(true)
    } // body
} // struct ShadedBottomBarGeometry

/// Tappable label layer that sits on top of the shaded bar.
/// Supports an optional icon (used for Feel Good Deals / Facebook).
struct ShadedBottomBarTapLayer: View {
    let height: CGFloat
    let titleKey: LocalizedStringKey
    let iconName: String?          // e.g. "Facebook_Logo_Primary" or nil
    let onTap: () -> Void

    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        GeometryReader { proxy in
            let bottomInset = proxy.safeAreaInsets.bottom

            let textColor: Color = .black
            let backgroundColor: Color = .green

            ZStack(alignment: .bottom) {
                // Transparent hit area (full bar)
                Color.clear
                    .frame(height: height + bottomInset)
                    .frame(maxWidth: .infinity, alignment: .bottom)
                    .contentShape(Rectangle())
                    .onTapGesture { onTap() }
                // Color.clear (tap area)

                // Visible label row
                HStack(spacing: 8) {
                    if let iconName {
                        Image(iconName)
                            .resizable()
                            .renderingMode(.original)      // keep brand colors
                            .scaledToFit()
                            .frame(height: 18)
                            .accessibilityHidden(true)
                    } // if iconName

                    Text(titleKey)
                        .font(.title2.weight(.bold))
                        .foregroundColor(textColor)
                        .lineLimit(1)
                        .minimumScaleFactor(0.6)
                        .allowsTightening(true)
                        .multilineTextAlignment(.center)
                } // HStack
                .padding(.vertical, 6)
                .padding(.horizontal, 12)
                .frame(maxWidth: .infinity)
                .background(backgroundColor)
                .padding(.bottom, bottomInset - 15)
                .accessibilityAddTraits(.isButton)
                .accessibilityLabel(Text(titleKey))
            } // ZStack(alignment: .bottom)
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottom)
            .ignoresSafeArea(edges: .bottom)
        } // GeometryReader
    } // body
} // struct ShadedBottomBarTapLayer

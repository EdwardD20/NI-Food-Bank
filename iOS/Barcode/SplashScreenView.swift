//
//  Barcode/SplashScreenView.swift
//
//  Created by Ed Danley on 4/24/26.
//

import SwiftUI
import Combine

struct SplashScreenView: View {
    @Environment(\.colorScheme) private var colorScheme
    @Environment(\.layoutDirection) private var layoutDirection
    @State private var isVisible = false
    @State private var countdown: Int = Int(Globals.splashDuration)
    @State private var isAnimating: Bool = false
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    let timer = Timer.publish(every: 1, on: .main, in: .common).autoconnect()

    var body: some View {
        GeometryReader { geo in
            ZStack {
                Color(.green)
                    .ignoresSafeArea()

                VStack(alignment: .center, spacing: 16) {

                    Image("Logo")
                        .resizable()
                        .scaledToFit()
                        .frame(
                            maxWidth: geo.size.width,
                            maxHeight: geo.size.height * 0.25
                        )
                        .accessibilityLabel("Logo")
                        .opacity(isVisible ? 1 : 0)
                        .scaleEffect(isVisible ? 1 : 0.9)
                        .animation(reduceMotion ? nil : .easeOut(duration: 0.6), value: isVisible)

                    Image("NEIGHBORS_EMPOWERED")
                        .resizable()
                        .scaledToFit()
                        .frame(
                            maxWidth: geo.size.width,
                            maxHeight: geo.size.height * 0.25
                        )
                        .accessibilityLabel("Neighbors. Empowered.")
                        .opacity(isVisible ? 1 : 0)
                        .scaleEffect(isVisible ? 1 : 0.9)
                        .animation(reduceMotion ? nil : .easeOut(duration: 0.6), value: isVisible)


                    Text("Providing nutritious food and resources for our neighbors with dignity, equity and convenience.")
                        .font(.system(size: geo.size.height * 0.03, weight: .bold))
                        .foregroundColor(.black)
                        .minimumScaleFactor(0.5)
                        .lineLimit(nil)
                        .multilineTextAlignment(.center)
                        .frame(maxWidth: .infinity)
                        .accessibilityLabel("Mission Statement")
                    
                } // VStack
                .frame(maxHeight: .infinity, alignment: .top)

                if countdown > 0 {
                    // Not so subtle countdown in lower-right corner
                    VStack {
                        Spacer()
                        HStack {
                            Spacer()
                            Text("\(countdown)")
                                .font(.system(size: geo.size.height * 0.10, weight: .black, design: .rounded))
                                .foregroundStyle(
                                    LinearGradient(
                                        colors: [.green, .orange, .yellow],
                                        startPoint: .leading,
                                        endPoint: .trailing
                                    )
                                )
                                .shadow(color: .green.opacity(0.8), radius: 6)
                                .shadow(color: .orange.opacity(0.6), radius: 12)
                                .scaleEffect(isAnimating ? 1.15 : 0.95)
                                .animation(
                                    .easeInOut(duration: 0.6).repeatForever(autoreverses: true),
                                    value: isAnimating
                                )
                                .onAppear {
                                    isAnimating = true
                                } // onAppear
                        } // HStack
                    } // VStack
                } // if countdown > 0

            } // ZStack
        } // GeometryReader
        .onAppear {
            isVisible = true
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
                UIAccessibility.post(notification: .screenChanged, argument: nil)
                UIAccessibility.post(notification: .announcement, argument: "Feel Good Deals")
            } // dispatch
        } // .onAppear
        .onReceive(timer) { _ in
            if countdown > 0 {
                countdown -= 1
            } // if countdown
        } // .onReceive
    } // body
} // SplashScreenView

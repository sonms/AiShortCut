import UIKit
import SwiftUI
import Shared

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Self.Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Self.Context) {}
}

struct ContentView: View {
    // DRAFT wiring -- see AppleTranslator.swift. Verify on a device.
    @StateObject private var translator = AppleTranslator()

    var body: some View {
        ComposeView()
            .ignoresSafeArea()
            .translationBridge(translator)
            .onAppear { IosTranslatorHolder.shared.backend = translator }
    }
}

import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}

struct ContentView: View {
    @State private var statusBarHidden = false

    var body: some View {
        ComposeView()
            .ignoresSafeArea()
            .statusBarHidden(statusBarHidden)
            .onReceive(NotificationCenter.default.publisher(for: .comfyStatusBarHiddenChanged)) { _ in
                statusBarHidden = UserDefaults.standard.bool(forKey: "comfyStatusBarHidden")
            }
    }
}

extension Notification.Name {
    static let comfyStatusBarHiddenChanged = Notification.Name("ComfyStatusBarHiddenChanged")
}



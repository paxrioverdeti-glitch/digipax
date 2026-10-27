import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        return MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    @State private var isReady = false

    var body: some View {
        Group {
            if isReady {
                ComposeView()
                    .ignoresSafeArea()
            } else {
                Color.white
                    .ignoresSafeArea()
                    .onAppear {
                        DispatchQueue.main.async {
                            isReady = true
                        }
                    }
            }
        }
    }
}




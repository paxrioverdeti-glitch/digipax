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
    @State private var showCompose = false

    var body: some View {
        if showCompose {
            ComposeView()
                .ignoresSafeArea()
        } else {
            VStack(spacing: 24) {
                Image(systemName: "checkmark.seal.fill")
                    .font(.system(size: 64))
                    .foregroundColor(.green)

                Text("DigiPax iOS")
                    .font(.title)
                    .bold()

                Text("Versão 2.0.9 (Build 11)")
                    .font(.caption)
                    .foregroundColor(.gray)

                Button(action: {
                    showCompose = true
                }) {
                    Text("Entrar no Aplicativo")
                        .fontWeight(.semibold)
                        .foregroundColor(.white)
                        .padding()
                        .frame(maxWidth: .infinity)
                        .background(Color.green)
                        .cornerRadius(12)
                }
                .padding(.horizontal, 40)
            }
            .padding()
        }
    }
}




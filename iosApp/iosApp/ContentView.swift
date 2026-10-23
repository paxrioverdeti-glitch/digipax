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
    @State private var isComposeLoaded = false
    @State private var loadError: String? = null

    var body: some View {
        ZStack {
            if isComposeLoaded {
                ComposeView()
                    .ignoresSafeArea()
            } else {
                VStack(spacing: 20) {
                    Image(systemName: "checkmark.shield.fill")
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(width: 80, height: 80)
                        .foregroundColor(.green)

                    Text("DigiPax iOS")
                        .font(.largeTitle)
                        .fontWeight(.bold)

                    Text("Sistema Corporativo de Gestão")
                        .font(.subheadline)
                        .foregroundColor(.gray)

                    if let error = loadError {
                        VStack(spacing: 8) {
                            Text("Erro de Inicialização:")
                                .font(.caption)
                                .fontWeight(.bold)
                                .foregroundColor(.red)

                            Text(error)
                                .font(.caption2)
                                .foregroundColor(.red)
                                .multilineTextAlignment(.center)
                                .padding()
                                .background(Color.red.opacity(0.1))
                                .cornerRadius(8)
                        }
                        .padding(.horizontal)
                    }

                    Button(action: {
                        loadComposeApp()
                    }) {
                        Text("Iniciar Aplicativo")
                            .font(.headline)
                            .foregroundColor(.white)
                            .padding()
                            .frame(maxWidth: 240)
                            .background(Color.green)
                            .cornerRadius(12)
                    }
                    .padding(.top, 10)
                }
                .padding()
                .onAppear {
                    // Tenta carregar automaticamente após 1 segundo
                    DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
                        loadComposeApp()
                    }
                }
            }
        }
    }

    private func loadComposeApp() {
        do {
            isComposeLoaded = true
        } catch {
            loadError = error.localizedDescription
        }
    }
}




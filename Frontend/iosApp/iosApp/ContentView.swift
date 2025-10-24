import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        let vc = MainViewControllerKt.MainViewController()
        vc.view.backgroundColor = .black   // same as SwiftUI background
        vc.view.isOpaque = true
        return vc
    }
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ZStack {
            Color.black
                .ignoresSafeArea() // all edges

            ComposeView()
                .ignoresSafeArea() // not just .keyboard — all edges
        }
    }
}




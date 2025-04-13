import SwiftUI

@main
struct LifeOSApp: App {
    @StateObject private var statusBarManager = StatusBarManager()
    
    var body: some Scene {
        MenuBarExtra {
            MenuView()
        } label: {
            if let image = NSImage(named: "MenuBarIcon") {
                // Ensure the image is treated as a template
                image.isTemplate = true
                Image(nsImage: image)
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .frame(width: 18, height: 18)
            } else {
                Image(systemName: "list.bullet")
            }
        }
        .menuBarExtraStyle(.window)
    }
}

class StatusBarManager: ObservableObject {
    // We can add more functionality here later
}

struct MenuView: View {
    let items = ["Item 1", "Item 2", "Item 3", "Item 4", "Item 5"]
    
    var body: some View {
        VStack {
            ForEach(items, id: \.self) { item in
                Button(item) {
                    // Action when item is clicked
                    print("Selected: \(item)")
                }
                .buttonStyle(.plain)
                .padding(.horizontal)
                .padding(.vertical, 8)
            }
            
            Divider()
            
            Button("Quit") {
                NSApplication.shared.terminate(nil)
            }
            .buttonStyle(.plain)
            .padding(.horizontal)
            .padding(.vertical, 8)
        }
        .padding(.vertical, 8)
    }
} 
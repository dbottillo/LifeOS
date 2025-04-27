// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "LifeOS",
    platforms: [
        .macOS(.v13)
    ],
    products: [
        .executable(
            name: "LifeOS",
            targets: ["LifeOS"]
        )
    ],
    targets: [
        .executableTarget(
            name: "LifeOS",
            path: ".",
            resources: [
                .copy("Icon.icns"),
                .copy("menubar_icon.png")
            ]
        )
    ]
) 
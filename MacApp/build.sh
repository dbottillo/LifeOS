#!/bin/bash

# Clean previous build
rm -rf .build
rm -rf LifeOS.app

# Build the app
swift build -c release

# Create app bundle structure
mkdir -p LifeOS.app/Contents/MacOS
mkdir -p LifeOS.app/Contents/Resources

# Copy the executable
cp .build/release/LifeOS LifeOS.app/Contents/MacOS/

# Copy Info.plist
cp Info.plist LifeOS.app/Contents/

# Copy app icon if it exists
if [ -f "Icon.icns" ]; then
    cp Icon.icns LifeOS.app/Contents/Resources/AppIcon.icns
fi

# Copy menu bar icon if it exists
if [ -f "menubar_icon.icns" ]; then
    cp menubar_icon.icns LifeOS.app/Contents/Resources/
fi

# Set executable permissions
chmod +x LifeOS.app/Contents/MacOS/LifeOS

# Remove extended attributes and quarantine flags
xattr -cr LifeOS.app

# Ad-hoc signing (this allows the app to run without an Apple Developer account)
codesign --force --deep --sign - LifeOS.app

echo "Build complete! App bundle created at LifeOS.app"
echo "To run the app for the first time:"
echo "1. Right-click on LifeOS.app"
echo "2. Select 'Open' from the context menu"
echo "3. Click 'Open' in the security dialog" 
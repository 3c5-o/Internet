# Internet Archive Link Extractor

A small native Android app for opening Internet Archive pages and extracting video-file URLs from the metadata of the item currently open in the in-app browser.

## Current features

- Open Internet Archive and sign in through its own website inside Android WebView.
- Open an item by URL or identifier.
- Read the item's metadata using the same logged-in WebView session.
- Find common video file formats and build direct download URLs.
- Copy one URL or all found URLs.
- Try opening a URL in an installed external player.
- Keep the Internet Archive password out of app code and app settings.

## Important limitations

- This initial version extracts files from the item currently open; it does not automatically enumerate every public and private upload in an account.
- Internet Archive access rules still apply. A private/restricted item may require authentication, and a copied URL may not work in an external player without the same session or permissions.
- The app does not bypass restrictions, remove access controls, or make private items public.
- A direct file URL does not guarantee every player supports the file's codec or access method.
- Only test with items you own or are authorized to access.

## Build APK

Pushing to the main branch starts the GitHub Actions workflow named Build Android APK. After it succeeds, download the artifact named internet-link-extractor-debug-apk from that workflow run. The debug APK is for testing; it is not a production-signed release.

## Project

- Android package: com.internet.linkextractor
- Minimum Android version: API 23
- Build: Gradle 8.9 / Android Gradle Plugin 8.7.3

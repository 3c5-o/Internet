# Internet Archive Link Extractor

A native Android app for opening Internet Archive, browsing a signed-in account, listing the account's publicly searchable video items, selecting several items, and extracting direct file URLs.

## Features

- Sign in through the official Internet Archive website inside the app's WebView.
- Open an item by URL or identifier.
- Open the account's My Library/profile page and list video items indexed for that account.
- Select multiple items with checkboxes, load more results, and extract all selected items in one operation.
- Show each item's title above its video-file name and direct URL.
- Copy all extracted results in order, including item titles and URLs, or copy an individual URL.
- Try opening a direct URL in an installed external player.
- The app does not ask for or store the Internet Archive password outside the official website session.

## Limitations and access

- The account list uses Internet Archive's searchable item index. Private or restricted uploads may not appear in search results even when signed in.
- Internet Archive access rules still apply. A restricted item may require authentication, and a copied URL may not work in an external player without the same session or permissions.
- The app does not bypass restrictions, remove access controls, or make private items public.
- A direct file URL does not guarantee every player supports the file's codec or access method.
- Only use the app with items you own or are authorized to access.

## Build APK

Pushing to the `main` branch starts the GitHub Actions workflow named **Build Android APK**. After it succeeds, download the artifact named `internet-link-extractor-debug-apk` from that workflow run. The debug APK is for testing; it is not a production-signed release.

## Project

- Android package: `com.internet.linkextractor`
- Minimum Android version: API 23
- Build: Gradle 8.9 / Android Gradle Plugin 8.7.3

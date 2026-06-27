# 3D Live Wallpapers Android

Android Native Kotlin application for live and static mobile wallpapers.

## Current features

- Native Kotlin Android app.
- Live Wallpapers tab.
- Static Wallpapers tab.
- Favorites tab.
- Category chips.
- Wallpaper preview screen.
- Static wallpaper setter.
- Native animated `WallpaperService` for live wallpaper.
- GitHub Actions workflow to build APK artifacts.
- Release signing prepared through GitHub Secrets.

## Package

`com.walhero.wallpapers`

Keep this package unchanged if you want future updates to install over the first release version.

## Update rule

For every update to install over the previous app, Android requires:

1. Same `applicationId`.
2. Same release signing key.
3. Higher `versionCode`.

The workflow uses `github.run_number` as `VERSION_CODE`, so every new GitHub Actions run increases the version code automatically.

## GitHub Secrets required for signed release APK

Add these repository secrets:

- `KS_B64`: release keystore file encoded as Base64.
- `STORE_PASS`: keystore store passphrase.
- `KEY_ALIAS`: key alias, recommended: `walhero-wallpapers`.
- `KEY_PASS`: key passphrase.

Path:

`Repository > Settings > Secrets and variables > Actions > New repository secret`

## Build APK

Open GitHub Actions and run **Android APK Build**.

Artifacts:

- `3d-live-wallpapers-debug-apk`: debug APK for testing only.
- `3d-live-wallpapers-release-signed-apk`: signed release APK, available only after adding the signing secrets.

Use the signed release APK as the first real install if you want future updates to install directly over it.

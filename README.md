# Kei Van Life: Ultimate Garage & Expedition

A relaxing Suzuki Every driving game in 3D: cruise around Quezon City, fill up, buy groceries and bring them home. `index.html` is the whole game (three.js is built in, so it runs offline); the rest of this repo turns it into an Android app.

The start screen shows the app version. In the APK it reads `3.0.<build number>`, where the build number is the GitHub Actions run.

## Put it on GitHub (about 10 minutes, all in the browser)

1. Create a **public** repository named `kei-van-life` under `gnotesamsung7-hue`.
2. Unzip this folder and upload **everything inside it** with *Add file > Upload files*, then commit.
3. If `.github/workflows/main.yml` did not come through (browsers often skip folders that start with a dot), create it by hand: *Add file > Create new file*, type `.github/workflows/main.yml` as the name, paste the contents of `main.yml` from the zip, commit.
4. *Settings > Actions > General > Workflow permissions*: choose **Read and write permissions**, save.
5. *Actions > Build APK > Run workflow*. After about 3 minutes the APK appears under *Releases*.
6. *Settings > Pages*: source **Deploy from a branch**, branch `main`, folder `/ (root)`, save.

## Links (these are what the QR codes point to)

- Android app: https://github.com/gnotesamsung7-hue/kei-van-life/releases/latest/download/KeiVanLife.apk
- Play in a browser: https://gnotesamsung7-hue.github.io/kei-van-life/

Printable QR cards are in `share/`. The same codes are inside the game under *Share the game*.

If you use a different repository name, the QR code inside the browser version adjusts by itself. For the APK and the PNG cards, change `GH` near the top of the game script in `index.html` and ask for new cards.

## Updating the game

Replace `index.html` and commit. Every commit to `main` builds a new APK and publishes it as the latest release, so the QR code never changes. All builds are signed with the key in `keystore/`, so a new version installs over the old one and keeps the player's progress.

## What is where

| Path | What it is |
|---|---|
| `index.html` | The game (HTML, CSS, JavaScript in one file) |
| `app/` | Android wrapper: one full-screen landscape WebView |
| `.github/workflows/main.yml` | Builds the APK and publishes the release |
| `keystore/keivanlife.jks` | Signing key (password `keivanlife`). Fine for a hobby game; make a private one before any Play Store release |
| `share/` | QR cards |
| `kei_van_life_art_prompt.md` | Prompts for generating painted art, with the file name to save each picture under |

## Matching your van's paint

Garage > Paint > *Photograph your van*, then tap the paint in the picture. In the APK this opens the phone's camera app, so the game itself needs no camera permission. *Choose a photo* uses a picture already on the phone.

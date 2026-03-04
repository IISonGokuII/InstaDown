# InstaDown 📥

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-blue.svg)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-API%2026+-green.svg)](https://developer.android.com)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Eine native Android-App zum Herunterladen von Instagram-Medien mit session-basierten Web-Requests, robustem Anti-Detection-Layer und moderner Kotlin/Jetpack Compose Architektur.

## ✨ Features

### Kernfunktionen
- 📸 **Posts** - Einzelbilder und Karussells (bis zu 10 Medien)
- 🎬 **Reels** - Videos mit Audio-Extraktion
- 📖 **Stories** - 24h-Content mit Expiry-Check
- ⭐ **Highlights** - Dauerhafte Story-Sammlungen
- 📺 **IGTV** - Long-Form Videos
- 👤 **Profilbilder** - HD-Version via GraphQL

### Download-Engine
- 🔄 **Streaming-Downloads** - Byte-Range-Requests für große Dateien
- ⏸️ **Pause/Resume** - Unterbrechbare Downloads
- 📶 **Smart-Duplicate-Detection** - SHA-256 Hash-Prüfung
- 📊 **Bandbreiten-Limitierung** - Konfigurierbare Limits
- 🔋 **Batterie-Optimierung** - Nur bei >30% Ladung
- 📡 **WiFi-Only Modus** - Option für mobile Daten

### Architektur
- 🏗️ **Clean Architecture** - Data/Domain/Presentation Layer
- 🎨 **Jetpack Compose** - Moderne deklarative UI
- 🔄 **MVI Pattern** - Sealed Classes für Intent/State/Effect
- 💉 **Hilt DI** - Dependency Injection
- 🗄️ **Room Database** - Lokale Datenpersistenz
- ⚡ **WorkManager** - Background Downloads

### Sicherheit & Privacy
- 🔐 **Biometrische Authentifizierung** - Fingerprint/Face Unlock
- 🛡️ **App-Lock** - PIN oder Biometrie
- 🤫 **Geheimer Modus** - Versteckte Ordner
- 🔒 **EncryptedSharedPreferences** - Sichere Speicherung
- 🚫 **Screenshot-Blockierung** - In sensiblen Bereichen

## 🛠️ Technischer Stack

| Komponente | Technologie |
|------------|-------------|
| Language | Kotlin 2.0+ |
| UI | Jetpack Compose (Material 3) |
| Architektur | Clean Architecture + MVI |
| Dependency Injection | Hilt |
| Networking | Ktor 2.3.7 + OkHttp |
| Database | Room |
| Preferences | DataStore |
| Security | EncryptedSharedPreferences |
| Background | WorkManager |
| Images | Coil 3.x |
| Media | Media3 ExoPlayer |

## 📋 Anforderungen

- Android 8.0 (API 26) oder höher
- Internetverbindung
- Für volle Funktionalität: Instagram-Account (optional)

## 🚀 Installation

### Von GitHub
1. Repository klonen:
```bash
git clone https://github.com/IISonGokuII/InstaDown.git
```

2. Projekt in Android Studio öffnen

3. Gradle Sync ausführen

4. App auf Gerät/Emulator deployen

## 📱 Nutzung

1. **URL einfügen** - Kopiere einen Instagram-Link in die Zwischenablage oder füge ihn manuell ein
2. **Download starten** - Tippe auf den Download-Button
3. **Fortschritt verfolgen** - Beobachte den Download in der Queue
4. **Galerie ansehen** - Öffne heruntergeladene Medien in der Galerie

### Automatischer Download
Aktiviere den **Quick Settings Tile** für schnellen Zugriff:
- Wische von oben nach unten
- Tippe auf "InstaDown Quick Download"
- Der Link aus der Zwischenablage wird automatisch verarbeitet

## ⚙️ Konfiguration

### Einstellungen
- **Theme** - System/Light/Dark/AMOLED
- **Download-Qualität** - Original/HD/SD
- **Parallel Downloads** - 1-5 gleichzeitig
- **WiFi-Only** - Mobile Daten sperren
- **App-Lock** - Biometrie/PIN aktivieren
- **Benachrichtigungen** - Erfolg/Fehler/Fortschritt

## 🏗️ Projektstruktur

```
com.instadown/
├── data/
│   ├── local/          # Room Database, DataStore, Encrypted Prefs
│   ├── remote/         # Ktor API Service
│   ├── model/          # Data Models
│   └── repository/     # Repository Pattern
├── domain/             # Use Cases, Domain Models
├── ui/
│   ├── screens/        # Compose Screens
│   ├── components/     # Reusable UI Components
│   ├── theme/          # Material Theme
│   └── viewmodel/      # ViewModels
├── di/                 # Hilt Modules
├── download/           # Download Engine
├── service/            # Foreground Services
├── security/           # Encryption, Biometric
└── utils/              # Extensions, Helpers
```

## 🔒 Sicherheitshinweise

- Session-Cookies werden verschlüsselt gespeichert
- Keine Passwörter werden gespeichert
- Downloads werden lokal verarbeitet
- Keine Daten werden an Dritte übertragen

## ⚠️ Haftungsausschluss

Diese App ist für den persönlichen Gebrauch bestimmt. Respektiere die Urheberrechte und Nutzungsbedingungen von Instagram. Der Nutzer ist allein verantwortlich für die Einhaltung aller geltenden Gesetze und Bestimmungen.

## 📄 Lizenz

MIT License - siehe [LICENSE](LICENSE) für Details.

## 🙏 Credits

- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Ktor](https://ktor.io/)
- [Hilt](https://dagger.dev/hilt/)
- [Coil](https://coil-kt.github.io/coil/)

---

**Hinweis**: Dies ist kein offizielles Instagram-Produkt. Instagram und das Instagram-Logo sind Marken von Meta Platforms, Inc.

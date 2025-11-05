# 📜 Changelog – 4D Viewer

Alle nennenswerten Änderungen an diesem Projekt werden in dieser Datei dokumentiert.
Das Format orientiert sich an [Keep a Changelog](https://keepachangelog.com/de/1.1.0/) und folgt
dem [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Geplante und in Entwicklung befindliche Funktionen

- **Erweiterung des Reiters „Bearbeiten“**
	- Neues Panel mit Schaltflächen für gängige Aktionen
	- Zoom- und Zuschneidefunktionen (Crop)
	- Regler für Helligkeit, Kontrast und Sättigung

- **Überarbeitung des Reiters „Navigieren“**
	- Neues Panel mit Schaltflächen für gängige Aktionen
	- Vereinfachte und intuitive Bildnavigation

- **Allgemeine Verbesserungen (geplant)**
	- Leistungsoptimierungen beim Laden großer Datensätze
	- Erweiterte Einstellungsoptionen (Tastenkürzel)

### Hinzugefügt

- **Internationalisierung (Deutsch / Englisch)** implementiert
- **Markierungsfunktion** mit frei definierbaren Formen, Farben und Größen
- **Einstellungsverwaltung** mit persistentem Speichern im Appverzeichnis
- **Einstellungsdialog** mit Profilverwaltung (Sprache, Farbschema, Schriftgröße)
- **Automatisches Projektspeichern** (Konfigurationen & Markierungen)
- **Modernisiertes UI-Design** mit abgerundeten Komponenten und blauen Akzenten als primärer Farbgebung

### Geändert

- **Code modularisiert** zur besseren Wartbarkeit und Erweiterbarkeit (Klassen feiner voneinander in Pakete abgegrenzt)
- **Tooltips** und kontextsensitive Hilfe deutlich verbessert

## [2.0] - 23.11.2023

### Hinzugefügt

- **FlatLaf** als neues Look & Feel integriert
- **Darstellungsoptionen**: Unterstützung für Dark- und Light-Mode
- **Unterstützte Formate**: GIF und PNG
- **Bildbearbeitung** Drehen von Bildern
- **Tastenkombinationen (Shortcuts)** implementiert
- **Menüleiste** hinzugefügt

### Verbessert

- **Code modularisiert** Klassen nach Funktionstyp in Pakete untergliedert (Actions, Handler, ...)
- **Ladezeiten** großer Bildverzeichnisse deutlich reduziert
- **Ladedialog** mit Fortschrittsanzeige und besserem Benutzerfeedback

### Gelöscht

- **Unterstütztes Format**: LWF

## [1.3] - 17.02.2013

### Übernommene Basisversion

Diese Version stellt die erste bekannte und weiterentwickelte Codebasis dar,
auf der die aktuelle Entwicklungsreihe aufbaut.

### Funktionen und Merkmale

- **4D-Visualisierung** (3D-Raum + Zeitdimension)
- **Unterstützte Formate**: JPEG, LWF und TIFF
- **Navigationssteuerung**: Grundlegende Steuerung und Anzeigeparameter
- **Bildbearbeitung**: Horizontale / vertikale Spiegelung von Bildern
- **Einheiten**: Neue, konfigurierbare Zeit- und Ebenen-Einheiten
- **Momentaufnahmen**: Speichern einzelner Frames
- **Checkpoint**: Möglichkeit, während der Navigation in der Zeitachse einen Referenzpunkt zu setzen.
  Beim erneuten Aufrufen wird die Zeitdifferenz zwischen dem aktuellen Frame und dem gesetzten Checkpoint automatisch
  berechnet.
- **Projektspezifische Konfigurationen**: Speichern und Wiederherstellen projektbezogener Einstellungen (Dateityp,
  Zeit-/Ebenentrenner, gewählte Zeit- und Ebeneneinheit)

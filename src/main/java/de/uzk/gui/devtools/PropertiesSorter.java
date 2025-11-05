package de.uzk.gui.devtools;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Der {@code PropertiesSorter} dient dazu {@code .properties-Dateien} im Resource-Verzeichnis einzulesen
 * und deren Inhalt strukturiert und alphabetisch zu sortieren.
 *
 * <p>Die Sortierung erfolgt abschnittsweise:
 * <ul>
 *   <li>Abschnitte beginnen mit einer Zeile, die mit {@code #} startet.</li>
 *   <li>Innerhalb eines Abschnitts werden leere Zeilen als Unterabschnitts-Trenner interpretiert.</li>
 *   <li>Jede Untersektion wird alphabetisch sortiert (case-insensitive).</li>
 *   <li>Die Abschnitte selbst werden ebenfalls alphabetisch (case-insensitive) sortiert.</li>
 * </ul>
 *
 * <p>Die Klasse kann über die {@link #main(String[])}-Methode eigenständig gestartet werden.
 */
public class PropertiesSorter {

    /**
     * Pfad zum Ressourcen-Verzeichnis
     */
    private static final Path RESOURCES_DIRECTORY = Paths.get("src/main/resources");

    /**
     * Dateimuster zum Filtern von {@code .properties-Dateien}
     */
    private static final String GLOB = "*.properties";

    /**
     * Hauptmethode zum Testen.
     * <p>
     * Liest alle {@code .properties-Dateien} aus dem Ressourcenordner, sortiert sie und überschreibt sie bei Zustimmung.
     */
    public static void main(String[] args) throws IOException {
        Path[] propertiesFiles = getPropertiesPaths();

        for (Path file : propertiesFiles) {
            System.out.printf("Reading file '%s'%n", file);

            // Charset ermitteln
            Charset charset = CharsetDetector.detectCharset(file);
            System.out.println("Detected charset: " + charset.displayName());

            List<String> originalLines = readAllLines(file, charset);
            List<String> sortedLines = sortProperties(originalLines);

            // Entfernt Leerzeilen am Anfang und Ende
            trimEmptyEdges(sortedLines);

            // Wenn Datei bereits sortiert → überspringen
            if (originalLines.equals(sortedLines)) {
                System.out.printf("⏭️ File ‘%s’ is already sorted.%n%n", file);
                continue;
            }

            // Nutzer fragen, ob die Datei überschrieben werden soll
            String question = "⚠️ Should the file be overwritten?";
            if (!askUserForConfirmation(question)) {
                System.out.printf("❌ File ‘%s’ is skipped.%n%n", file);
                continue;
            }

            // Datei überschreiben
            if (writeAllLines(file, sortedLines, charset)) {
                System.out.printf("✅ File ‘%s’ has been successfully overwritten.%n%n", file);
            }
        }
    }

    /**
     * Liest alle Zeilen aus einer Datei ein (sicher, ressourcenschonend).
     *
     * @param file    Pfad zur Datei
     * @param charset Charset der Datei
     * @return Liste der Zeilen; leer bei Fehlern
     */
    private static List<String> readAllLines(Path file, Charset charset) {
        List<String> lines = new ArrayList<>();
        if (file == null || !Files.isRegularFile(file)) return lines;

        try (BufferedReader reader = new BufferedReader(new FileReader(file.toFile(), charset))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            System.err.printf("Failed reading file '%s'%n", file);
        }

        return lines;
    }

    /**
     * Schreibt alle Zeilen in eine Datei (sicher, ressourcenschonend).
     * <p>
     * Bestehende Inhalte werden überschrieben. Falls das Zielverzeichnis nicht existiert,
     * wird abgebrochen. Jeder Eintrag der Liste entspricht einer Zeile in der Datei.
     *
     * @param file    Pfad zur Zieldatei
     * @param lines   Liste der Zeilen, die geschrieben werden sollen
     * @param charset Charset der Datei
     * @return {@code true}, wenn das Schreiben erfolgreich war, sonst {@code false}
     */
    private static boolean writeAllLines(Path file, List<String> lines, Charset charset) {
        if (file == null || lines == null) return false;

        try {
            // Stellt sicher, dass das Zielverzeichnis existiert
            Path parentDir = file.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                return false;
            }

            // Datei schreiben (UTF-8)
            try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                    new FileOutputStream(file.toFile()), charset))) {

                for (int i = 0; i < lines.size(); i++) {
                    writer.write(lines.get(i));
                    if (i < lines.size() - 1) {
                        writer.newLine(); // keine Leerzeile am Ende der Datei
                    }
                }
            }
            return true;
        } catch (IOException e) {
            System.err.printf("Failed writing file '%s'%n", file);
            return false;
        }
    }

    /**
     * Entfernt leere Zeilen am Anfang und Ende einer Liste.
     *
     * @param lines Liste von Strings, die bearbeitet wird
     */
    public static void trimEmptyEdges(List<String> lines) {
        if (lines == null || lines.isEmpty()) return;

        int start = 0;
        // Führe Zeilenindex so lange hoch, bis die erste nicht-leere Zeile gefunden ist
        while (start < lines.size() && lines.get(start).trim().isEmpty()) {
            start++;
        }

        int end = lines.size() - 1;
        // Reduziere Index, bis die letzte nicht-leere Zeile gefunden ist
        while (end >= start && lines.get(end).trim().isEmpty()) {
            end--;
        }

        // Nur den Bereich mit tatsächlichem Inhalt behalten
        if (start > 0 || end < lines.size() - 1) {
            List<String> trimmedContent = new ArrayList<>(lines.subList(start, end + 1));
            lines.clear();
            lines.addAll(trimmedContent);
        }
    }

    /**
     * Liest alle Pfade zu {@code .properties-Dateien} aus dem Resource Verzeichnis.
     *
     * @return Array mit allen gefundenen properties-Dateien, ggf. leer
     */
    private static Path[] getPropertiesPaths() {
        Set<Path> propertyFiles = new TreeSet<>();
        if (!Files.isDirectory(RESOURCES_DIRECTORY)) return new Path[0];

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(RESOURCES_DIRECTORY, GLOB)) {
            for (Path file : stream) {
                if (!Files.isRegularFile(file)) continue;
                propertyFiles.add(file);
            }
        } catch (IOException e) {
            System.err.printf("Failed reading the directory '%s'%n", RESOURCES_DIRECTORY);
        }

        return propertyFiles.toArray(Path[]::new);
    }

    /**
     * Sortiert die Zeilen einer Properties-Datei anhand von Abschnitten.
     *
     * @param lines Ursprüngliche Zeilen der Datei
     * @return Alphabetisch sortierte Zeilen
     */
    public static List<String> sortProperties(List<String> lines) {
        List<Section> sections = parseSections(lines);
        sections.sort(Section::compareTo);

        List<String> sorted = new ArrayList<>();
        for (Section section : sections) {
            sorted.addAll(section.toList());
        }

        return sorted;
    }

    /**
     * Teilt eine Liste von Zeilen in Abschnitte auf, die durch ein '#' eingeleitet werden.
     *
     * @param lines Zeilen der Datei
     * @return Liste von Abschnitten
     */
    private static List<Section> parseSections(List<String> lines) {
        List<Section> sections = new ArrayList<>();
        Section currentSection = null;

        for (String rawLine : lines) {
            String trimmedLine = rawLine.trim();

            if (trimmedLine.startsWith("#")) {
                // Neuen Abschnitt beginnen
                if (currentSection != null) {
                    sections.add(currentSection);
                }
                currentSection = new Section(trimmedLine);
            } else {
                // Falls keine Sektion bisher vorhanden ist → Dummy-Sektion anlegen
                if (currentSection == null && !trimmedLine.isEmpty()) {
                    currentSection = new Section(null);
                }

                if (currentSection != null) {
                    currentSection.addLine(trimmedLine);
                }
            }
        }

        // Letzte Sektion hinzufügen, falls vorhanden
        if (currentSection != null) {
            sections.add(currentSection);
        }

        return sections;
    }

    /**
     * Fragt den Benutzer im Terminal nach einer Bestätigung.
     *
     * @return {@code true}, wenn der Benutzer "yes" eingibt, andernfalls {@code false}.
     */
    public static boolean askUserForConfirmation(String question) {
        Scanner scanner = new Scanner(System.in);
        String input;

        while (true) {
            String normalizedQuestion = question == null || !question.isBlank() ? question + " (yes/no): " : "yes/no: ";
            System.out.print(normalizedQuestion);
            input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("yes")) return true;
            if (input.equals("no")) return false;
        }
    }

    /**
     * Innere Klasse, die einen Abschnitt innerhalb der Datei repräsentiert.
     * Jeder Abschnitt enthält:
     * <ul>
     *     <li>Einen Header (optional, beginnend mit #)</li>
     *     <li>Eine Liste von Unterabschnitten, getrennt durch Leerzeilen</li>
     * </ul>
     */
    private static class Section implements Comparable<Section> {
        /**
         * Abschnittsüberschrift oder null
         */
        private final String header;
        /**
         * Liste der Unterabschnitte, jeweils als sortierte Menge
         */
        private final List<Set<String>> subSections;

        /**
         * Erstellt eine neue Sektion.
         *
         * @param header Kopfzeile, welche null sein kann
         */
        public Section(String header) {
            this.header = header;
            this.subSections = new ArrayList<>();
            addNewSubSection();
        }

        /**
         * Fügt eine neue Untersektion (als sortiertes Set) hinzu.
         */
        private void addNewSubSection() {
            // TreeSet mit case-insensitive Sortierung
            this.subSections.add(new TreeSet<>(String.CASE_INSENSITIVE_ORDER));
        }

        /**
         * Fügt eine Zeile zum aktuellen Unterabschnitt hinzu.
         * Leere Zeilen erzeugen neue Unterabschnitte.
         *
         * @param line Zeile, die hinzugefügt werden soll
         */
        public void addLine(String line) {
            Set<String> currentSubSection = this.subSections.get(this.subSections.size() - 1);

            if (line.isEmpty()) {
                // Neue Untersektion nur starten, wenn die aktuelle nicht leer ist
                if (!currentSubSection.isEmpty()) addNewSubSection();
            } else {
                currentSubSection.add(line);
            }
        }

        /**
         * Gibt den gesamten Abschnitt als Liste von Strings zurück.
         * Enthält Header, sortierte Unterabschnitte und Leerzeilen zur Trennung.
         *
         * @return sortierte Zeilen dieses Abschnitts
         */
        public List<String> toList() {
            List<String> result = new ArrayList<>();

            if (header != null && !header.isEmpty()) {
                result.add(header);
            }

            for (Set<String> subSection : subSections) {
                result.addAll(subSection);
                if (!subSection.isEmpty()) {
                    result.add(""); // Leerzeile nach Unterabschnitt
                }
            }

            return result;
        }

        /**
         * Vergleich zweier Abschnitte (alphabetisch, case-insensitive).
         * Null-Header stehen immer vor nicht-null Headern.
         *
         * @param other anderer Abschnitt
         * @return Vergleichsergebnis nach sortierbarer Ordnung
         */
        @Override
        public int compareTo(@NotNull Section other) {
            if (this.header == null && other.header == null) return 0;
            if (this.header == null) return -1;
            if (other.header == null) return 1;
            return this.header.compareToIgnoreCase(other.header);
        }
    }
}

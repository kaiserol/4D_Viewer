package de.uzk.devtools;

import de.uzk.io.PathManager;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static de.uzk.Main.logger;

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
 * <br>
 * Die Klasse ist als {@code final} deklariert, um eine Vererbung zu verhindern.
 * Da sämtliche Funktionalitäten über statische Methoden bereitgestellt werden,
 * besitzt die Klasse einen privaten Konstruktor, um eine Instanziierung zu
 * unterbinden.
 */
public final class PropertiesSorter {
    /**
     * Hauptmethode zum Testen.
     * <p>
     * Liest alle {@code .properties-Dateien} aus dem Ressourcenordner, sortiert sie und überschreibt sie bei Zustimmung.
     */
    public static void main(String[] args) {
        boolean skipConfirmations = (args.length == 1 && "--skip-confirm".equalsIgnoreCase(args[0]));
        Path[] propertiesPaths = getPropertiesPaths();

        for (Path propertyPath : propertiesPaths) {
            printLine("Reading file '%s'".formatted(propertyPath.toAbsolutePath()));

            // Charset ermitteln
            Charset charset = CharsetDetector.detectCharset(propertyPath);
            printLine("Detected charset '%s'".formatted(charset.displayName()));

            List<String> originalLines = readAllLines(propertyPath, charset);
            List<String> sortedLines = sortProperties(originalLines);

            // Entfernt Leerzeilen am Anfang und Ende
            trimList(sortedLines);

            // Wenn Datei bereits sortiert → überspringen
            if (Objects.equals(originalLines, sortedLines)) {
                printLine("⏭️ File ‘%s’ is already sorted.%n".formatted(propertyPath));
                continue;
            }

            if (skipConfirmations) {
                printLine("⚠️ File will be overwritten. (Confirmation was skipped)");
            } else {
                // Nutzer fragen, ob die Datei überschrieben werden soll
                String question = "⚠️ Should the file be overwritten?";
                if (!askUserForConfirmation(question)) {
                    printLine("❌ File ‘%s’ is skipped.%n".formatted(propertyPath));
                    continue;
                }
            }

            // Datei überschreiben
            if (writeAllLines(propertyPath, sortedLines, charset)) {
                printLine("✅ File ‘%s’ has been successfully overwritten.%n".formatted(propertyPath));
            }
        }
    }

    /**
     * Gibt das angegebene Objekt an die Standardausgabe aus.
     *
     * @param object Das auszugebende Objekt. Seine Zeichenfolgendarstellung wird an die Standardausgabe gesendet.
     */
    private static void printLine(Object object) {
        System.out.println(object);
    }

    /**
     * Privater Konstruktor, um eine Instanziierung dieser Klasse zu unterbinden.
     */
    private PropertiesSorter() {
        // Verhindert die Instanziierung dieser Klasse
    }

    /**
     * Liest alle Zeilen aus einer Datei und gibt sie als Liste zurück.
     *
     * @param filePath Pfad zur Datei
     * @param charset  Zeichensatz, mit dem die Datei gelesen werden soll
     * @return Liste der Zeilen; leer, wenn Datei ungültig oder ein Fehler aufgetreten ist
     */
    private static List<String> readAllLines(Path filePath, Charset charset) {
        List<String> lines = new ArrayList<>();
        if (filePath == null || !Files.isRegularFile(filePath)) return lines;

        // Datei zeilenweise einlesen
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile(), charset))) {
            String line;

            // Solange noch Zeilen vorhanden sind, füge sie der Liste hinzu
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            logger.warn("Could not read from the property-file '%s'.".formatted(filePath));
        }
        return lines;
    }

    /**
     * Schreibt alle Zeilen aus einer Liste in eine Datei. Existierende Inhalte werden vollständig überschrieben.
     * Jeder Eintrag in der Liste entspricht genau einer Zeile in der Datei.
     *
     * @param filePath Pfad zur Datei
     * @param lines    Liste der Zeilen, die geschrieben werden sollen
     * @param charset  Zeichensatz, in dem geschrieben werden soll
     * @return {@code true}, wenn das Schreiben erfolgreich war, sonst {@code false}
     */
    private static boolean writeAllLines(Path filePath, List<String> lines, Charset charset) {
        if (filePath == null || lines == null || !Files.isRegularFile(filePath)) return false;

        // Dateiinhalt überschreiben
        try (BufferedWriter writer = new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(filePath.toFile()), charset))) {

            // Jede Zeile schreiben
            for (int i = 0; i < lines.size(); i++) {
                writer.write(lines.get(i));

                // Zeilenumbruch hinzufügen, außer nach der letzten Zeile
                if (i < lines.size() - 1) {
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            logger.warn("Could not write in the property-file '%s'.".formatted(filePath));
            return false;
        }
        return true;
    }

    /**
     * Entfernt leere Zeilen am Anfang und Ende einer Liste.
     *
     * @param lines Liste von Strings, die bearbeitet wird
     */
    private static void trimList(List<String> lines) {
        if (lines == null || lines.isEmpty()) return;

        int startIndex = 0;
        // Führe Zeilenindex so lange hoch, bis die erste nicht-leere Zeile gefunden ist
        while (startIndex < lines.size() && lines.get(startIndex).isBlank()) {
            startIndex++;
        }

        int endIndex = lines.size() - 1;
        // Reduziere Index, bis die letzte nicht-leere Zeile gefunden ist
        while (endIndex >= startIndex && lines.get(endIndex).isBlank()) {
            endIndex--;
        }

        // Nur den Bereich mit tatsächlichem Inhalt behalten
        if (startIndex > 0 || endIndex < lines.size() - 1) {
            List<String> trimmedContent = new ArrayList<>(lines.subList(startIndex, endIndex + 1));
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
        Set<Path> propertyPaths = new TreeSet<>();
        if (!Files.isDirectory(PathManager.RESOURCES_DIRECTORY)) return new Path[0];

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(PathManager.RESOURCES_DIRECTORY,
            PathManager.PROPERTIES_FILE_NAME_PATTERN)) {

            for (Path filePath : stream) {
                if (!Files.isRegularFile(filePath)) continue;
                propertyPaths.add(filePath);
            }
        } catch (IOException e) {
            logger.error("Failed to stream the resources-directory '%s'.".formatted(PathManager.RESOURCES_DIRECTORY));
        }

        return propertyPaths.toArray(Path[]::new);
    }

    /**
     * Sortiert die Zeilen einer Properties-Datei anhand von Abschnitten.
     *
     * @param lines Ursprüngliche Zeilen der Datei
     * @return Alphabetisch sortierte Zeilen
     */
    private static List<String> sortProperties(List<String> lines) {
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
                if (currentSection == null && !trimmedLine.isBlank()) {
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
    private static boolean askUserForConfirmation(String question) {
        Scanner scanner = new Scanner(System.in);
        String input;

        String YES = "YES";
        String NO = "NO";
        String YES_NO = YES + "/" + NO;
        while (true) {
            String normalizedQuestion = question != null && !question.isBlank() ? (question + " (" + YES_NO + "): ") : YES_NO + ": ";
            System.out.print(normalizedQuestion);
            input = scanner.nextLine().trim().toUpperCase();

            if (Objects.equals(input, YES)) return true;
            if (Objects.equals(input, NO)) return false;
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
         * Comparator für Property-Zeilen.
         * Vergleicht nur den Property-Namen (links vom '='),
         * falls vorhanden – ansonsten den gesamten String.
         * Vergleich erfolgt case-insensitive und trimmed.
         */
        private static final Comparator<String> PROPERTY_NAME_COMPARATOR = (s1, s2) -> {
            if (s1 == null && s2 == null) return 0;
            if (s1 == null) return -1;
            if (s2 == null) return 1;

            // Hilfsmethode für Property-Namen
            String key1 = extractKey(s1);
            String key2 = extractKey(s2);

            return key1.compareToIgnoreCase(key2);
        };

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
        }

        /**
         * Fügt eine neue Untersektion (als sortiertes Set) hinzu.
         */
        private void addNewSubSection() {
            // TreeSet mit case-insensitive Sortierung
            this.subSections.add(new TreeSet<>(PROPERTY_NAME_COMPARATOR));
        }

        /**
         * Fügt eine Zeile zum aktuellen Unterabschnitt hinzu.
         * - Leere Zeilen erzeugen neue Unterabschnitte.
         * - Bei Property-Zeilen wird vor und nach dem '=' getrimmt.
         *
         * @param line Zeile, die hinzugefügt werden soll
         */
        public void addLine(String line) {
            // Falls keine Untersektion existiert, initialisieren
            if (this.subSections.isEmpty()) {
                addNewSubSection();
            }

            Set<String> currentSubSection = this.subSections.get(this.subSections.size() - 1);
            if (line.isBlank()) {
                // Neue Untersektion nur starten, wenn die aktuelle nicht leer ist
                if (!currentSubSection.isEmpty()) {
                    addNewSubSection();
                }
                return;
            }

            // Property-Zeile erkennen und säubern
            String propertyLine = processPropertyLine(line);
            currentSubSection.add(propertyLine);
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

        /**
         * Formatiert eine Property-Zeile nach dem Muster "key=value".
         * <p>
         * Erkennt Zeilen, die ein Gleichheitszeichen enthalten, und entfernt
         * überflüssige Leerzeichen um Schlüssel und Wert. Enthält die Zeile
         * kein "=", wird sie unverändert zurückgegeben.
         *
         * @param line Zu verarbeitende Zeile (nicht {@code null})
         * @return Die bereinigte Property-Zeile
         */
        private static String processPropertyLine(String line) {
            if (line == null) return "";

            int equalsIndex = line.indexOf('=');
            if (equalsIndex == -1) return line;

            // Key und Value getrennt trimmen
            String key = line.substring(0, equalsIndex).trim();
            String value = line.substring(equalsIndex + 1).trim();
            return key + "=" + value;
        }

        /**
         * Extrahiert den Property-Namen (linke Seite des '=') aus einer Zeile.
         * <p>
         * Falls kein Gleichheitszeichen vorhanden ist, wird die gesamte Zeile
         * getrimmt zurückgegeben.
         *
         * @param line Zu analysierende Zeile (nicht {@code null})
         * @return Die bereinigten Property-Namen
         */
        private static String extractKey(String line) {
            if (line == null) return "";

            int equalsIndex = line.indexOf('=');
            if (equalsIndex == -1) return line;

            // Nur den linken Teil (Property-Name), getrimmt
            return line.substring(0, equalsIndex);
        }
    }
}

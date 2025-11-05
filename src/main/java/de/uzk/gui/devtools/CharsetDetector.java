package de.uzk.gui.devtools;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility-Klasse zur Erkennung des wahrscheinlichsten Charsets einer Textdatei.
 * <p>
 * Diese Implementierung:
 * <ul>
 *   <li>Erkennt UTF-8 anhand valider Bytefolgen</li>
 *   <li>Erkennt UTF-16 durch BOM</li>
 *   <li>Fällt auf ISO-8859-1 (Latin-1) zurück, falls keine sichere Erkennung möglich ist</li>
 * </ul>
 * <p>
 * Hinweis: Diese Erkennung ist heuristisch und nicht 100 % zuverlässig.
 * Für sicher reproduzierbare Ergebnisse sollten alle Textdateien konsequent in UTF-8 gespeichert werden.
 */
public final class CharsetDetector {

    /**
     * Liste der zu testenden Charsets (Reihenfolge ist wichtig).
     */
    private static final Charset[] COMMON_CHARSETS = {
        StandardCharsets.UTF_8,
        StandardCharsets.ISO_8859_1,
        StandardCharsets.UTF_16,
        StandardCharsets.UTF_16BE,
        StandardCharsets.UTF_16LE,
    };

    private CharsetDetector() {
        // Utility class – keine Instanzen erlaubt
    }

    /**
     * Bestimmt das wahrscheinlichste Charset einer Datei.
     *
     * @param file Pfad zur Datei
     * @return erkannter Charset oder {@link StandardCharsets#ISO_8859_1}, falls nicht ermittelbar
     */
    public static Charset detectCharset(Path file) {
        if (file == null || !Files.isRegularFile(file)) {
            return StandardCharsets.ISO_8859_1;
        }

        try (InputStream in = new BufferedInputStream(Files.newInputStream(file))) {
            // BOM prüfen (Byte Order Mark)
            Charset bomCharset = detectBom(in);
            if (bomCharset != null) {
                return bomCharset;
            }

            // Falls keine BOM, heuristisch testen
            byte[] bytes = Files.readAllBytes(file);
            if (looksLikeUtf8(bytes)) return StandardCharsets.UTF_8;

            // Teste andere bekannte Charsets
            for (Charset charset : COMMON_CHARSETS) {
                if (canDecodeWithoutReplacement(bytes, charset)) {
                    return charset;
                }
            }
        } catch (IOException e) {
            System.err.printf("Failed reading file '%s%n", file);
        }
        // Fallback
        return StandardCharsets.ISO_8859_1;
    }

    /**
     * Prüft, ob eine Datei ein BOM (Byte Order Mark) enthält.
     */
    private static Charset detectBom(InputStream in) throws IOException {
        in.mark(4);
        byte[] bom = new byte[4];
        int n = in.read(bom, 0, bom.length);
        in.reset();

        if (n >= 3 && bom[0] == (byte) 0xEF && bom[1] == (byte) 0xBB && bom[2] == (byte) 0xBF) {
            return StandardCharsets.UTF_8;
        } else if (n >= 2) {
            if (bom[0] == (byte) 0xFE && bom[1] == (byte) 0xFF)
                return StandardCharsets.UTF_16BE;
            else if (bom[0] == (byte) 0xFF && bom[1] == (byte) 0xFE)
                return StandardCharsets.UTF_16LE;
        }
        return null;
    }

    /**
     * Prüft, ob der Byteinhalt wie valides UTF-8 aussieht.
     */
    private static boolean looksLikeUtf8(byte[] bytes) {
        int i = 0;
        while (i < bytes.length) {
            int b = bytes[i] & 0xFF;

            if (b < 0x80) { // ASCII
                i++;
            } else if ((b >> 5) == 0x6) { // 2-Byte-Sequenz
                if (i + 1 >= bytes.length) return false;
                if ((bytes[i + 1] & 0xC0) != 0x80) return false;
                i += 2;
            } else if ((b >> 4) == 0xE) { // 3-Byte-Sequenz
                if (i + 2 >= bytes.length) return false;
                if ((bytes[i + 1] & 0xC0) != 0x80 || (bytes[i + 2] & 0xC0) != 0x80) return false;
                i += 3;
            } else if ((b >> 3) == 0x1E) { // 4-Byte-Sequenz
                if (i + 3 >= bytes.length) return false;
                if ((bytes[i + 1] & 0xC0) != 0x80 || (bytes[i + 2] & 0xC0) != 0x80 || (bytes[i + 3] & 0xC0) != 0x80)
                    return false;
                i += 4;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Prüft, ob die Bytes mit einem Charset ohne Ersetzungszeichen (�) dekodierbar sind.
     */
    private static boolean canDecodeWithoutReplacement(byte[] bytes, Charset charset) {
        CharsetDecoder decoder = charset.newDecoder();
        decoder.onMalformedInput(CodingErrorAction.REPORT);
        decoder.onUnmappableCharacter(CodingErrorAction.REPORT);

        try {
            decoder.decode(ByteBuffer.wrap(bytes));
            return true;
        } catch (CharacterCodingException e) {
            return false;
        }
    }
}
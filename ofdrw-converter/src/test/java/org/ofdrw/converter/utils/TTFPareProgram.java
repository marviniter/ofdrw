package org.ofdrw.converter.utils;

import org.apache.fontbox.cff.CFFFont;
import org.apache.fontbox.cff.CFFParser;
import org.junit.jupiter.api.Test;


import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 权观宇
 * @since 2021-04-13 22:55:07
 */
public class TTFPareProgram {
    @Test
    public  void testParse() throws IOException {

        final Path path = Paths.get("src/test/resources/font_13132_0_edit.ttf");
        try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "r")) {
            // Version: 4 byte
            int v1 = raf.readUnsignedShort();
            int v2 = raf.readUnsignedShort();
            System.out.printf("Version: %02X %02X\n", v1, v2);
            // Number of Tables: 2 byte
            int numberOfTables = raf.readUnsignedShort();
            System.out.printf("Number of Tables: %d\n", numberOfTables);
            // Search Range: 2 byte
            int searchRange = raf.readUnsignedShort();
            System.out.printf("Search Range: %d\n", searchRange);
            // Entry Selector: 2 byte
            int entrySelector = raf.readUnsignedShort();
            System.out.printf("Entry Selector: %d\n", entrySelector);
            // Range Shift: 2 byte
            int rangeShift = raf.readUnsignedShort();
            System.out.printf("Range Shift: %d\n", rangeShift);
            System.out.println("\nTables List:\n");
            Map<String, int[]> tables = new HashMap<>();
            for (int i = 0; i < numberOfTables; i++) {
                byte[] buff = new byte[4];
                raf.read(buff);                 // 4byte
                String tag = new String(buff, StandardCharsets.ISO_8859_1);
                int checkSum = raf.readInt();   // 4 byte
                int offset = raf.readInt();     // 4 byte
                int length = raf.readInt();     // 4 byte
                System.out.printf("Name: %s Offset: %d Length: %d\n", tag, offset, length);
                tables.put(tag, new int[]{offset, length});
            }

            final int[] maxpsArr = tables.get("maxp");
            raf.seek(maxpsArr[0]);
            int maxpVersion = raf.readInt();
            int numGlyphs = raf.readUnsignedShort();
            System.out.printf("maxp Version: 0x%08X Number of glyphs: %d \n", maxpVersion, numGlyphs);

            final int[] headArr = tables.get("head");
            raf.seek(headArr[0] + 50);
            // 0 for short offsets (Offset16), 1 for long (Offset32).
            final int indexToLocFormat = raf.readUnsignedShort();
            System.out.printf("IndexToLocFormat: %s (%d)\n", (indexToLocFormat == 0) ? "Offset16" : "Offset32", indexToLocFormat);

            // IndexToLocFormat: 0 for short offsets (Offset16), 1 for long (Offset32).
            // [offset | len] [offset | len] [offset | len] ...
            final int[] locaArr = tables.get("loca");
            System.out.println(locaArr[0]);
            raf.seek(locaArr[0]);

            long[] fontOffsetArr = new long[numGlyphs + 1];
            for (int i = 0; i < numGlyphs + 1; i++) {
                if (indexToLocFormat == 1) {
                    fontOffsetArr[i] = raf.readInt();
                } else {
                    fontOffsetArr[i] = raf.readUnsignedShort() * 2L;
                }
                System.out.printf("[%d]%d, %s", i, fontOffsetArr[i], i % 4 == 0 ? "\n" : "");

            }
        }
    }
    @Test
    public void testCFF() throws IOException {
        final Path path = Paths.get("src/test/resources/font_83_83.cff");
        CFFParser parser = new CFFParser();
        final List<CFFFont> fontList = parser.parse(Files.readAllBytes(path));

        System.out.println(fontList);
    }
}

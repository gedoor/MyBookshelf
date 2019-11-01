package com.kunfei.bookshelf.help;

public class UTF8BOMFighter {
    private static final byte[] UTF8_BOM_BYTES = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    private UTF8BOMFighter() {
    }

    static public String removeUTF8BOM(String xmlText) {
        byte[] bytes = xmlText.getBytes();
        boolean containsBOM = bytes.length > 3
                && bytes[0] == UTF8_BOM_BYTES[0]
                && bytes[1] == UTF8_BOM_BYTES[1]
                && bytes[2] == UTF8_BOM_BYTES[2];
        if (containsBOM) {
            xmlText = new String(bytes, 3, bytes.length - 3);
        }
        return xmlText;
    }

    static public byte[] removeUTF8BOM(byte[] bytes) {
        boolean containsBOM = bytes.length > 3
                && bytes[0] == UTF8_BOM_BYTES[0]
                && bytes[1] == UTF8_BOM_BYTES[1]
                && bytes[2] == UTF8_BOM_BYTES[2];
        if (containsBOM) {
            byte[] copy = new byte[bytes.length - 3];
            System.arraycopy(bytes, 3, copy, 0, bytes.length - 3);
            return copy;
        }
        return bytes;
    }
}

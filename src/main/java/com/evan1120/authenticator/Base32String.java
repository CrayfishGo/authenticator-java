package com.evan1120.authenticator;

import java.util.HashMap;
import java.util.Locale;

/**
 * Base32中只有A-Z和2-7这些字符。
 */
class Base32String {

    //  RFC 4648/3548
    private static final Base32String INSTANCE = new Base32String("ABCDEFGHIJKLMNOPQRSTUVWXYZ234567");

    private static Base32String getInstance() {
        return INSTANCE;
    }

    private int MASK;
    private int SHIFT;
    private HashMap<Character, Integer> CHAR_MAP;

    private static final String SEPARATOR = "-";

    private Base32String(String alphabet) {
        char[] DIGITS = alphabet.toCharArray();
        MASK = DIGITS.length - 1;
        SHIFT = Integer.numberOfTrailingZeros(DIGITS.length);
        CHAR_MAP = new HashMap<>();
        for (int i = 0; i < DIGITS.length; i++) {
            CHAR_MAP.put(DIGITS[i], i);
        }
    }

    static byte[] decode(String encoded) throws DecodingException {
        return getInstance().decodeInternal(encoded);
    }

    private byte[] decodeInternal(String encoded) throws DecodingException {
        encoded = encoded.trim().replaceAll(SEPARATOR, "").replaceAll(" ", "");
        encoded = encoded.replaceFirst("[=]*$", "");
        encoded = encoded.toUpperCase(Locale.US);
        if (encoded.length() == 0) {
            return new byte[0];
        }
        int encodedLength = encoded.length();
        int outLength = encodedLength * SHIFT / 8;
        byte[] result = new byte[outLength];
        int buffer = 0;
        int next = 0;
        int bitsLeft = 0;
        for (char c : encoded.toCharArray()) {
            if (!CHAR_MAP.containsKey(c)) {
                throw new DecodingException("Illegal character: " + c);
            }
            buffer <<= SHIFT;
            buffer |= CHAR_MAP.get(c) & MASK;
            bitsLeft += SHIFT;
            if (bitsLeft >= 8) {
                result[next++] = (byte) (buffer >> (bitsLeft - 8));
                bitsLeft -= 8;
            }
        }
        return result;
    }

    static class DecodingException extends Exception {
        DecodingException(String message) {
            super(message);
        }
    }

}
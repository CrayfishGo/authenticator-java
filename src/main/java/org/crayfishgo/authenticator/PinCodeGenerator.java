package org.crayfishgo.authenticator;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;

/**
 *
 */
class PinCodeGenerator {

    private static final int MAX_PINCODE_LENGTH = 9;

    private static final int[] DIGITS_POWER
            // 0  1   2     3      4      5        6        7          8         9
            = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000};

    private final Signer signer;
    private final int codeLength;

    interface Signer {
        byte[] sign(byte[] data) throws GeneralSecurityException;
    }

    PinCodeGenerator(Signer signer, int passCodeLength) {
        if ((passCodeLength < 0) || (passCodeLength > MAX_PINCODE_LENGTH)) {
            throw new IllegalArgumentException("PinCode Length must be between 1 and " + MAX_PINCODE_LENGTH + " digits");
        }
        this.signer = signer;
        this.codeLength = passCodeLength;
    }

    String generateResponseCode(long otp_state) throws GeneralSecurityException {
        byte[] value = ByteBuffer.allocate(8).putLong(otp_state).array();
        byte[] hash = signer.sign(value);
        int offset = hash[hash.length - 1] & 0xF;
        int truncatedHash = hashToInt(hash, offset) & 0x7FFFFFFF;
        int pinValue = truncatedHash % DIGITS_POWER[codeLength];
        return padOutput(pinValue);
    }

    private String padOutput(int value) {
        StringBuilder result = new StringBuilder(Integer.toString(value));
        for (int i = result.length(); i < codeLength; i++) {
            result.insert(0, "0");
        }
        return result.toString();
    }

    private int hashToInt(byte[] bytes, int start) {
        DataInput input = new DataInputStream(new ByteArrayInputStream(bytes, start, bytes.length - start));
        try {
            return input.readInt();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
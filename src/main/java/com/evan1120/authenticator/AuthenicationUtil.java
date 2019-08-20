package com.evan1120.authenticator;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 *
 */
public class AuthenicationUtil {

    private static long mStartTime = 0;
    private static final long mTimeStep = 30;

    public static String getCurrentCode(String secret) throws OtpSourceException {
        long otp_state = getValueAtTime(System.currentTimeMillis() / 1000);
        return computePin(secret, otp_state);
    }

    private static String computePin(String secret, long otp_state)
            throws OtpSourceException {
        if (secret == null || secret.length() == 0) {
            throw new OtpSourceException("Null or empty secret");
        }
        try {
            PasscodeGenerator.Signer signer = getSigningOracle(secret);
            PasscodeGenerator pcg = new PasscodeGenerator(signer, 6);

            return pcg.generateResponseCode(otp_state);

        } catch (GeneralSecurityException e) {
            throw new OtpSourceException("Crypto failure", e);
        }
    }


    public static long getValueAtTime(long time) {
        long timeSinceStartTime = time - mStartTime;
        if (timeSinceStartTime >= 0) {
            return timeSinceStartTime / mTimeStep;
        } else {
            return (timeSinceStartTime - (mTimeStep - 1)) / mTimeStep;
        }
    }


    private static PasscodeGenerator.Signer getSigningOracle(String secret) {
        try {
            byte[] keyBytes = Base32String.decode(secret);
            final Mac mac = Mac.getInstance("HMACSHA1");
            mac.init(new SecretKeySpec(keyBytes, ""));
            return mac::doFinal;
        } catch (Base32String.DecodingException | NoSuchAlgorithmException | InvalidKeyException error) {
            error.printStackTrace();
        }

        return null;
    }


    public static class OtpSourceException extends Exception {

        public OtpSourceException(String message) {
            super(message);
        }

        public OtpSourceException(String message, Throwable cause) {
            super(message, cause);
        }
    }


}
package com.evan1120.authenticator;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 *
 */
public class PinCodeUtil {

    private static final long TIME_STEP = 30;

    /**
     * 根据密钥获取当前pin code
     *
     * @param secret
     * @return
     * @throws OtpSourceException
     */
    public static String getCurrentCode(String secret) throws OtpSourceException {
        long otp_state = getValueAtTime(System.currentTimeMillis() / 1000);
        return computePin(secret, otp_state);
    }

    // 计算pin code
    private static String computePin(String secret, long otp_state) throws OtpSourceException {
        if (secret == null || secret.trim().length() == 0) {
            throw new OtpSourceException("Null or empty secret is not allowed");
        }
        try {
            PinCodeGenerator.Signer signer = getSigning(secret);
            PinCodeGenerator pcg = new PinCodeGenerator(signer, 6);
            return pcg.generateResponseCode(otp_state);
        } catch (GeneralSecurityException e) {
            throw new OtpSourceException("Crypto failure", e);
        }
    }

    // 获取时间计数器
    private static long getValueAtTime(long time) {
        long startTime = 0;
        long timeSinceStartTime = time - startTime;
        if (timeSinceStartTime >= 0) {
            return timeSinceStartTime / TIME_STEP;
        } else {
            return (timeSinceStartTime - (TIME_STEP - 1)) / TIME_STEP;
        }
    }

    // 根据密钥获取签名
    private static PinCodeGenerator.Signer getSigning(String secret) {
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
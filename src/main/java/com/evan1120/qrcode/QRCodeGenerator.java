package com.evan1120.qrcode;

import com.evan1120.util.ZxingUtils;

/**
 * @author evan
 * @title: QRCodeGenerator
 * @projectName google-authenticator
 * @description: 验证器二维码生成  URI的格式： otpauth://totp/myGitlab.com:admin@example.com?secret=dgf3j5csiu2jn6wehechiuuclyhcnyaw&issuer=myGitlab.com
 * @date 2019-08-21 14:22
 */
public class QRCodeGenerator {


    /**
     * @param secret
     * @param account
     * @return
     */
    public static String generateQRCodeWithBase64Code(final String secret, final String account) {
        String uri = "otpauth://totp/" + account + "?secret=" + secret;
        try {
            return ZxingUtils.createORCode(uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}

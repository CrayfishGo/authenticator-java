package org.crayfishgo.qrcode;

import org.crayfishgo.util.ZxingUtils;

/**
 * @author evan
 * @title: QRCodeGenerator
 * @projectName google-authenticator
 * @description: 验证器二维码生成  URI的格式： otpauth://totp/myGitlab.com:admin@example.com?secret=dgf3j5csiu2jn6wehechiuuclyhcnyaw&issuer=myGitlab.com
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

    public static void main(String[] args) {
        System.out.println(generateQRCodeWithBase64Code("abf3j5csiu2jn6wehechiuuclyh44yaw", "mesapp"));
    }

}

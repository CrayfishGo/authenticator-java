package com.evan1120.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;

/**
 * @author evan
 * @title: ZxingUtils
 * @projectName google-authenticator
 * @description: 二维码生成工具类
 * @date 2019-08-21 14:22
 */
public class ZxingUtils {

    private static final List<String> SUPPORT_FILE_SUFFIX = Lists.newArrayList("jpg", "png");

    private static Logger logger = LoggerFactory.getLogger(ZxingUtils.class);

    private static final int width = 300;//默认二维码宽度

    private static final int height = 300;//默认二维码高度

    private static final String DEFAULT_FILE_SUFFIX = "png";//默认二维码格式

    private static final Map<EncodeHintType, Object> encodeHints = Maps.newHashMap(); //二维码参数
    private static final Map<DecodeHintType, Object> decodeHints = Maps.newHashMap(); //二维码参数

    private static final int logo_width = 60;//默认logo宽度

    private static final int logo_height = 60;//默认logo高度


    static {
        encodeHints.put(EncodeHintType.CHARACTER_SET, "utf-8");//字符编码
        encodeHints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);//容错等级L/M/Q/H其中L为最低，H为最高
        encodeHints.put(EncodeHintType.MARGIN, 2);//二维码与图片边距

        decodeHints.put(DecodeHintType.CHARACTER_SET, "utf-8"); // 字符集
    }

    /**
     * 生成 默认尺寸、默认名字 无logo 二维码
     *
     * @param srcUrl
     * @return
     * @throws Exception
     */
    public static String createORCode(String srcUrl) throws Exception {
        return createORCode(srcUrl, width, height);
    }

    /**
     * 生成 指定尺寸，默认名称，无logo 的二维码
     *
     * @param srcUrl
     * @param imgWidth
     * @param imgHeight
     * @return
     * @throws WriterException
     * @throws IOException
     */
    public static String createORCode(String srcUrl, int imgWidth, int imgHeight) throws Exception {
        return createORCode(srcUrl, imgWidth, imgHeight, null, false);
    }

    /**
     * 生成 默认尺寸，默认名称，包含logo 二维码
     *
     * @param srcUrl
     * @param logoPath
     * @param needCompress
     * @return
     */
    public static String createORCodeLogo(String srcUrl, String logoPath, boolean needCompress) throws Exception {
        return createORCodeLogo(srcUrl, width, height, logoPath, needCompress);
    }

    /**
     * 生成 指定大小 指定名称 包含logo 二维码
     *
     * @param srcUrl
     * @param imgWidth
     * @param imgHeight
     * @param logoPath
     * @param needCompress
     * @return
     * @throws Exception
     */
    public static String createORCodeLogo(String srcUrl, int imgWidth, int imgHeight, String logoPath, boolean needCompress) throws Exception {
        return createORCode(srcUrl, imgWidth, imgHeight, logoPath, needCompress);
    }

    /**
     * 生成 指定尺寸、指定名称、包含logo 的二维码
     * 如果logoPath为null，则默认生成不包含logo的二维码
     * 如果确定生成不包含logo的二维码，则可以调用另外指定方法
     *
     * @param srcUrl       需要生成二维码地址
     * @param imgWidth     需要生成二维码的宽度
     * @param imgHeight    需要生成二维码的高度
     * @param logoPath     需要生成二维码的中间logo
     * @param needCompress logo是否需要压缩
     * @return
     * @throws Exception
     */
    private static String createORCode(String srcUrl, int imgWidth, int imgHeight, String logoPath, boolean needCompress) throws Exception {
        String file;
        //如果logo图片是null，生成不包含logo的二维码，否则生成含有logo的二维码
        if (StringUtils.isNotBlank(logoPath)) {
            boolean b = checkImg(logoPath);
            if (b) {
                file = createORCodeForLogo(srcUrl, imgWidth, imgHeight, logoPath, needCompress);
            } else {
                logger.info("请用户选择正确的logo格式jpg or png");
                throw new Exception("错误图片后缀");
            }
        } else {
            file = createORCodeNotLogo(srcUrl, imgWidth, imgHeight);
        }
        return file;
    }


    /**
     * 创建一个 指定大小、指定名称、包含logo 二维码
     *
     * @param srcUrl
     * @param imgWidth
     * @param imgHeight
     * @param logoPath
     * @param needCompress
     * @throws WriterException
     * @throws IOException
     */
    private static String createORCodeForLogo(String srcUrl, int imgWidth, int imgHeight, String logoPath, boolean needCompress) throws WriterException, IOException {
        BufferedImage bufferedImage = drawQR(srcUrl, imgWidth, imgHeight);
        Image src = enterFinalLogoImg(logoPath, needCompress);
        insertLogo(bufferedImage, src);
        return outImgToBase64(bufferedImage);
    }

    /**
     * 生成指定大小，指定名称，无logo的二维码
     *
     * @param srcUrl
     * @param imgWidth
     * @param imgHeight
     * @return
     * @throws WriterException
     * @throws IOException
     */
    private static String createORCodeNotLogo(String srcUrl, int imgWidth, int imgHeight) throws WriterException, IOException {
        BufferedImage bufferedImage = drawQR(srcUrl, imgWidth, imgHeight);
        return outImgToBase64(bufferedImage);
    }

    /**
     * 绘制二维码
     *
     * @param srcUrl
     * @param imgWidth
     * @param imgHeight
     * @return
     * @throws WriterException
     */
    private static BufferedImage drawQR(String srcUrl, int imgWidth, int imgHeight) throws WriterException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(srcUrl, BarcodeFormat.QR_CODE, imgWidth, imgHeight, encodeHints);
        BufferedImage bufferedImage = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_BGR);
        //绘制成二维码（应该）
        for (int x = 0; x < imgWidth; x++) {
            for (int y = 0; y < imgHeight; y++) {
                bufferedImage.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        return bufferedImage;
    }

    /**
     * 确定最终需要添加的logo大小（是否需要压缩）
     *
     * @param logoPath
     * @param needCompress
     * @return
     * @throws IOException
     */
    private static Image enterFinalLogoImg(String logoPath, boolean needCompress) throws IOException {
        //插入logo
        BufferedImage logoImage = ImageIO.read(new File(logoPath));
        int tempWidth = logoImage.getWidth(null);
        int tempHeight = logoImage.getHeight(null);
        //最终确定的logo图片
        Image src = logoImage;
        //需要压缩
        if (needCompress) {
            if (tempWidth > logo_width) {
                tempWidth = logo_width;
            }
            if (tempHeight > logo_height) {
                tempHeight = logo_height;
            }
            Image scaledInstance = logoImage.getScaledInstance(tempWidth, tempHeight, Image.SCALE_SMOOTH);
            BufferedImage tag = new BufferedImage(width, height,
                    BufferedImage.TYPE_INT_RGB);
            Graphics graphics = tag.getGraphics();
            graphics.drawImage(scaledInstance, 0, 0, null);
            graphics.dispose();
            src = scaledInstance;
        }
        return src;
    }

    /**
     * 将二维码中间插入logo
     *
     * @param img
     * @param logo
     */
    private static void insertLogo(BufferedImage img, Image logo) {
        Graphics2D graphics2D = img.createGraphics();
        int imgWidth = img.getWidth(null);
        int imgHeight = img.getHeight(null);
        //将logo定位到中间位置
        int logoWidth = logo.getWidth(null);
        int logoHeight = logo.getHeight(null);
        int x = (imgWidth - logoWidth) / 2;
        int y = (imgHeight - logoHeight) / 2;
        graphics2D.drawImage(logo, x, y, logoWidth, logoHeight, null);
        Shape shape = new RoundRectangle2D.Float(x, y, logoWidth, logoHeight, 6, 6);
        graphics2D.setStroke(new BasicStroke(3f));
        graphics2D.draw(shape);
        graphics2D.dispose();
    }

    /**
     * 将二维码保存到指定位置
     *
     * @param image
     * @return 返回生成二维码的图片地址
     * @throws IOException
     */
    private static String outImgToBase64(BufferedImage image) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, DEFAULT_FILE_SUFFIX, baos);
            byte[] imageByte = baos.toByteArray();
            return Base64.getEncoder().encodeToString(imageByte);
        }
    }


    /**
     * 检测该参数是图片后缀的
     * 暂时只支持jpg png
     *
     * @param path
     * @return
     */
    public static boolean checkImg(String path) {
        File file = new File(path);
        String fileName = file.getName();
        int i = fileName.indexOf(".");
        String suff = "";
        if (i != -1) {
            suff = fileName.substring(i + 1);
        }
        boolean isImg = false;
        if (StringUtils.isNotBlank(suff)) {
            boolean contains = SUPPORT_FILE_SUFFIX.contains(suff);
            if (contains)
                isImg = true;
        }
        return isImg;
    }

    /**
     * 解析二维码
     *
     * @param path
     * @return
     */
    public static Map parseQR(String path) {
        Map<String, Object> resMap = Maps.newHashMap();  // 返回出去的map集合
        try {
            MultiFormatReader formatReader = new MultiFormatReader();
            File file = new File(path);
            BufferedImage image = ImageIO.read(file);    //读取文件
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
            Result result = formatReader.decode(binaryBitmap, decodeHints);
            resMap.put("RESULT", result);    //总结果集
            resMap.put("FORMAT", result.getBarcodeFormat());    //被解析的二维码格式
            resMap.put("TEXT", result.getText());    //被解析的二维码含有的文本内容
            resMap.put("STATE", "SUCCESS");        //解析状态
        } catch (Exception e) {
            e.printStackTrace();
            resMap.put("STATE", "ERROR");
        }
        return resMap;

    }


}


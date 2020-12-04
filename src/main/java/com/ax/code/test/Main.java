package com.ax.code.test;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import sun.misc.BASE64Encoder;

/**
 * @author lj
 */
public class Main {

    public static void main(String[] args) throws CharacterCodingException {
        TestA testA = new TestA(1L, "AAA", "aasdasd");
        TestA testB = new TestA(1L, "BBB", "asdf");
        TestA testC = new TestA(2L, "CCC", "asdf");
        TestA testD = new TestA(3L, "DDD", "asdf");
        TestA testE = new TestA(2L, "EEE", "asdf");
        List<TestA> list = new ArrayList<>();
        list.add(testA);
        list.add(testB);
        list.add(testC);
        list.add(testD);
        list.add(testE);
        Map<Long, List<TestA>> map = list.stream().collect(Collectors.groupingBy(TestA::getId, Collectors.toList()));
        System.out.println(map.size());
    }

    public static String getSerialNumber(String drive) {
        StringBuilder result = new StringBuilder();
        try {
            File file = File.createTempFile("realhowto", ".vbs");
            file.deleteOnExit();
            FileWriter fw = new java.io.FileWriter(file);

            String vbs = "Set objFSO = CreateObject(\"Scripting.FileSystemObject\")\n"
                    + "Set colDrives = objFSO.Drives\n"
                    + "Set objDrive = colDrives.item(\""
                    + drive
                    + "\")\n"
                    + "Wscript.Echo objDrive.SerialNumber"; // see note
            fw.write(vbs);
            fw.close();
            String path = file.getPath().replace("%20", " ");
            Process p = Runtime.getRuntime().exec(
                    "cscript //NoLogo " + path);
            BufferedReader input = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                result.append(line);
            }
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString().trim();
    }

    //统计字符串中文个数
    private static int chineseCharNum(String str) {
        int num = 0;
        for (char c : str.toCharArray()) {
            if (String.valueOf(c).getBytes(StandardCharsets.UTF_8).length == 3) {
                num++;
            }
           /* if (String.valueOf(c).matches("[\u4e00-\u9fa5]")) {
                num++;
            }*/
        }
        return num;
    }

    /*
     * 定义二维码的宽高
     */
    private static int WIDTH = 300;
    private static int HEIGHT = 300;
    private static String FORMAT = "png";//二维码格式
    private static final int FRONT_COLOR = 0x000000;
    private static final int BACKGROUND_COLOR = 0xFFFFFF;

    /**
     * generateCode 根据code生成相应的一维码
     *
     * @param file   一维码目标文件
     * @param code   一维码内容
     * @param width  图片宽度
     * @param height 图片高度
     */
    public static void generateCode(File file, String code, int width, int height) {
        //定义位图矩阵BitMatrix
        BitMatrix matrix = null;
        try {
            /*com.google.zxing.EncodeHintType：编码提示类型,枚举类型
             * EncodeHintType.CHARACTER_SET：设置字符编码类型
             * EncodeHintType.ERROR_CORRECTION：设置误差校正
             *      ErrorCorrectionLevel：误差校正等级，L = ~7% correction、M = ~15% correction、Q = ~25% correction、H = ~30% correction
             *      不设置时，默认为 L 等级，等级不一样，生成的图案不同，但扫描的结果是一样的
             * EncodeHintType.MARGIN：设置二维码边距，单位像素，值越小，二维码距离四周越近
             */
          /*  Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1);*/
            //配置条码参数
            Map<EncodeHintType, Object> hints = new HashMap<>();
            //设置条码两边空白边距为0，默认为10，如果宽度不是条码自动生成宽度的倍数则MARGIN无效
            //hints.put(EncodeHintType.MARGIN, 10);

            //为了无边距，需设置宽度为条码自动生成规则的宽度
            width = new Code128Writer().encode(code).length;
            //前端可控制高度，不影响识别
            //int height = 70;
            //条码放大倍数
            int codeMultiples = 1;
            //获取条码内容的宽，不含两边距，当EncodeHintType.MARGIN为0时即为条码宽度
            int codeWidth = width * codeMultiples;
            /*
             * MultiFormatWriter:多格式写入，这是一个工厂类，里面重载了两个 encode 方法，用于写入条形码或二维码
             *      encode(String contents,BarcodeFormat format,int width, int height,Map<EncodeHintType,?> hints)
             *      contents:条形码/二维码内容
             *      format：编码类型，如 条形码，二维码 等
             *      width：码的宽度
             *      height：码的高度
             *      hints：码内容的编码类型
             * BarcodeFormat：枚举该程序包已知的条形码格式，即创建何种码，如 1 维的条形码，2 维的二维码 等
             * BitMatrix：位(比特)矩阵或叫2D矩阵，也就是需要的二维码
             */
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            BitMatrix bitMatrix = multiFormatWriter.encode(code, BarcodeFormat.CODE_128, width, height, hints);
            /**java.awt.image.BufferedImage：具有图像数据的可访问缓冲图像，实现了 RenderedImage 接口
             * BitMatrix 的 get(int x, int y) 获取比特矩阵内容，指定位置有值，则返回true，将其设置为前景色，否则设置为背景色
             * BufferedImage 的 setRGB(int x, int y, int rgb) 方法设置图像像素
             *      x：像素位置的横坐标，即列
             *      y：像素位置的纵坐标，即行
             *      rgb：像素的值，采用 16 进制,如 0xFFFFFF 白色
             */
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "png", stream);
            BASE64Encoder encoder = new BASE64Encoder();
            String encode = encoder.encode(stream.toByteArray());
            System.out.println(encode);
            //matrix = writer.encode(code, BarcodeFormat.CODE_128, width, height, hints);
            //matrix = writer.encode(code,BarcodeFormat.EAN_13, width, height, null);
        } catch (WriterException | IOException e) {
            e.printStackTrace();
        }
    }


    //生成二维码
    public static void generateQRCode(File file, String content) {
        //定义二维码参数
        Map<EncodeHintType, Object> hints = new HashMap<>();

        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");//设置编码
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);//设置容错等级
        hints.put(EncodeHintType.MARGIN, 2);//设置边距默认是5

        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, WIDTH, HEIGHT, hints);
            Path path = file.toPath();
            MatrixToImageWriter.writeToPath(bitMatrix, FORMAT, path);//写到指定路径下

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}

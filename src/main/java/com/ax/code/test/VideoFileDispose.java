package com.ax.code.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * @author lj
 */
public class VideoFileDispose {

    private final static List<String> FILE_TYPE_LIST;

    private final static long MIN_FILE_SIZE = 1024 * 1024 * 10;

    private final static File OUT_FILE;

    private static String DISK_LABEL = "2TB硬盘";

    private static int maxFileNameLength = 0;

    private static int maxFilePathLength = 0;

    private static final String X = "-";

    private static final String Y = "|";

    private static List<VideoFileInfo> outFileInfoList = new ArrayList<>();

    private static int originalFileInfoListLength = 0;

    static {
        OUT_FILE = new File("statistics.txt");
        if (OUT_FILE.exists()) {
            outFileInfoList = originalFileContentRead();
            originalFileInfoListLength = outFileInfoList.size();
        }
    }

    static {
        FILE_TYPE_LIST = new ArrayList<>();
        FILE_TYPE_LIST.add("avi");
        FILE_TYPE_LIST.add("rmvb");
        FILE_TYPE_LIST.add("rm");
        FILE_TYPE_LIST.add("asf");
        FILE_TYPE_LIST.add("divx");
        FILE_TYPE_LIST.add("mpg");
        FILE_TYPE_LIST.add("mpeg");
        FILE_TYPE_LIST.add("mpe");
        FILE_TYPE_LIST.add("wmv");
        FILE_TYPE_LIST.add("mp4");
        FILE_TYPE_LIST.add("mkv");
        FILE_TYPE_LIST.add("vob");
        FILE_TYPE_LIST.add("WAV");
        FILE_TYPE_LIST.add("jpg");
    }

    public static void main(String[] s) throws Exception {
        System.out.println("请选择...");
        Scanner scanner = new Scanner(System.in);
        long beginDate = System.currentTimeMillis();
        File rootDirectory = new File("K:\\Backups");
        fileDispose(rootDirectory);
        if (outFileInfoList.size() == originalFileInfoListLength) {
            System.out.println("无信息更新...");
            return;
        }
        outFileStatisticsInfo();
        System.out.println("耗时：" + (System.currentTimeMillis() - beginDate));
        fileInfoOut(fileVagueSearch("D1"));
    }

    private static void fileDispose(File fileDirectory) {
        final File[] files = fileDirectory.listFiles();
        if (null == files) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                fileDispose(file);
                continue;
            }
            String fileName = file.getName();
            if (file.getTotalSpace() >= MIN_FILE_SIZE && FILE_TYPE_LIST.contains(fileName.substring(fileName.lastIndexOf(".") + 1))) {
                VideoFileInfo fileInfo = new VideoFileInfo(fileName, file.getAbsolutePath(), DISK_LABEL);
                if (!outFileInfoList.contains(fileInfo)) {
                    filter(fileInfo);
                    outFileInfoList.add(fileInfo);
                }
            }
        }
    }

    /**
     * 输出文件
     */
    private static void outFileStatisticsInfo() throws Exception {
        outFileInfoList.sort((o1, o2) -> o1.fileName.compareToIgnoreCase(o2.fileName));
        int nameLength = maxFileNameLength + 3;
        int pathLength = maxFilePathLength + 3;
        int labelMaxLength = getStrByteLength(DISK_LABEL) + 4;
        int tableMaxLength = nameLength + pathLength + labelMaxLength;
        StringBuilder xSb = generateAssignNumChars(tableMaxLength, X);
        String xStr = xSb
                .replace(0, 1, " ")
                .replace(tableMaxLength - 1, tableMaxLength, " ")
                .toString();
        try (PrintStream writer = new PrintStream(OUT_FILE, StandardCharsets.UTF_8.name())) {
            writer.println(xStr);
            for (VideoFileInfo fileInfo : outFileInfoList) {
                StringBuilder sb = new StringBuilder();
                //name 值填充
                sb.append(Y).append(" ").append(fileInfo.fileName);
                strFillSpace(sb, maxFileNameLength - getStrByteLength(fileInfo.fileName) + 1);
                //path 值填充
                sb.append(Y).append(" ").append(fileInfo.filePath);
                strFillSpace(sb, maxFilePathLength - getStrByteLength(fileInfo.filePath) + 1);
                //label 值填充
                sb.append(Y).append(" ").append(DISK_LABEL).append(" ").append(Y);
                writer.println(sb.toString());
                writer.println(xStr);
            }
            writer.flush();
        }
    }

    private static List<VideoFileInfo> originalFileContentRead() {
        List<VideoFileInfo> fileInfoSet = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(VideoFileDispose.OUT_FILE), StandardCharsets.UTF_8))) {
            String str;
            VideoFileInfo fileInfo;
            // 按行读取字符串
            while ((str = reader.readLine()) != null) {
                if (!str.contains(Y)) {
                    continue;
                }
                String[] split = str.split("\\" + Y);
                if (split.length < 4) {
                    continue;
                }
                fileInfo = new VideoFileInfo(split[1].trim(), split[2].trim(), split[3].trim());
                filter(fileInfo);
                fileInfoSet.add(fileInfo);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileInfoSet;
    }

    private static void filter(VideoFileInfo fileInfo) {
        int nameLength = getStrByteLength(fileInfo.fileName);
        int pathLength = getStrByteLength(fileInfo.filePath);
        if (nameLength > maxFileNameLength) {
            maxFileNameLength = nameLength;
        }
        if (pathLength > maxFilePathLength) {
            maxFilePathLength = pathLength;
        }
    }

    private static void strFillSpace(StringBuilder sb, int num) {
        for (int i = 0; i < num; i++) {
            sb.append(" ");
        }
    }

    private static StringBuilder generateAssignNumChars(int num, String chars) {
        StringBuilder x = new StringBuilder();
        for (int i = 0; i < num; i++) {
            x.append(chars);
        }
        return x;
    }

    /**
     * 中文与中文字符抵两个空格，但只占一个字符位。
     */
    private static int getStrByteLength(String str) {
        return str.length() + chineseCharNum(str);
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

    private static List<VideoFileInfo> fileVagueSearch(String searchFileName) {
        List<VideoFileInfo> resultList = new ArrayList<>();
        if (!outFileInfoList.isEmpty()) {
            outFileInfoList.forEach(fileInfo -> {
                String fileName = fileInfo.fileName.contains(".") ? fileInfo.fileName.substring(0, fileInfo.fileName.lastIndexOf(".")) : fileInfo.fileName;
                String name = searchFileName.contains(".") ? searchFileName.substring(0, searchFileName.lastIndexOf(".")) : searchFileName;
                fileName = fileName.trim().toLowerCase();
                name = name.trim().toLowerCase();
                if (fileName.contains(name) || name.contains(fileName)) {
                    resultList.add(fileInfo);
                }
            });
        }
        return resultList;
    }

    private static void fileInfoOut(List<VideoFileInfo> fileInfoList) {
        if (null == fileInfoList || fileInfoList.isEmpty()) {
            return;
        }
        int maxNameLength = 0;
        int maxPathLength = 0;
        int maxLabelLength = 0;
        for (VideoFileInfo fileInfo : fileInfoList) {
            int nameLength = getStrByteLength(fileInfo.fileName);
            int pathLength = getStrByteLength(fileInfo.filePath);
            int labelLength = getStrByteLength(fileInfo.diskLabel);
            if (nameLength > maxNameLength) {
                maxNameLength = nameLength;
            }
            if (pathLength > maxPathLength) {
                maxPathLength = pathLength;
            }
            if (labelLength > maxLabelLength) {
                maxLabelLength = labelLength;
            }
        }
        int tableMaxLength = maxNameLength + 3 + maxPathLength + 3 + maxLabelLength + 4;
        StringBuilder xSb = generateAssignNumChars(tableMaxLength, X);
        String xStr = xSb
                .replace(0, 1, " ")
                .replace(tableMaxLength - 1, tableMaxLength, " ")
                .toString();
        System.out.println(xStr);
        for (VideoFileInfo fileInfo : fileInfoList) {
            StringBuilder sb = new StringBuilder();
            //name 值填充
            sb.append(Y).append(" ").append(fileInfo.fileName);
            strFillSpace(sb, maxNameLength - getStrByteLength(fileInfo.fileName) + 1);
            //path 值填充
            sb.append(Y).append(" ").append(fileInfo.filePath);
            strFillSpace(sb, maxPathLength - getStrByteLength(fileInfo.filePath) + 1);
            //label 值填充
            sb.append(Y).append(" ").append(fileInfo.diskLabel).append(" ").append(Y);
            System.out.println(sb.toString());
            System.out.println(xStr);
        }
    }

    static class VideoFileInfo {

        String fileName;

        String filePath;

        String diskLabel;

        VideoFileInfo(String fileName, String filePath, String diskLabel) {
            this.fileName = fileName;
            this.filePath = filePath;
            this.diskLabel = diskLabel;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof VideoFileInfo)) {
                return false;
            }
            VideoFileInfo fileInfo = (VideoFileInfo) o;
            if (null == fileInfo.fileName || null == this.fileName || !fileInfo.fileName.equals(this.fileName)) {
                return false;
            }
            if (null == fileInfo.diskLabel || null == this.diskLabel || !fileInfo.diskLabel.equals(this.diskLabel)) {
                return false;
            }
            if (null == fileInfo.filePath || null == this.filePath || fileInfo.filePath.length() <= 2 || this.filePath.length() <= 2) {
                return false;
            }
            if (!fileInfo.filePath.substring(2).equals(this.filePath.substring(2))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int result = 1;
            result = result * 59 + (fileName == null ? 43 : fileName.hashCode());
            result = result * 59 + (filePath == null ? 43 : filePath.hashCode());
            result = result * 59 + (diskLabel == null ? 43 : diskLabel.hashCode());
            return result;
        }

    }
}

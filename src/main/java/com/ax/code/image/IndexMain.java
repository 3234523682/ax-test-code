package com.ax.code.image;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import javax.imageio.ImageIO;
import lombok.SneakyThrows;
import net.semanticmetadata.lire.builders.GlobalDocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.global.JCD;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

/**
 * @author lj
 */
public class IndexMain {

    public static void main(String[] args) throws Exception {
        //System.out.println(Paths.get(IndexMain.class.getClassLoader().getResource("image/data").toURI()).toString());
        long begin = System.currentTimeMillis();
        Set<Path> imageFilePathList = new HashSet<>();
        gainImageFilePath(getImageDirPath(), imageFilePathList);
        indexFile(imageFilePathList, getIndexPath());
        System.out.println("耗时：" + (System.currentTimeMillis() - begin) / 1000 + "秒");
    }

    public static Path getIndexPath() throws IOException {
        Properties props = new Properties();
        InputStream in = IndexMain.class.getClassLoader().getResourceAsStream("image/conf/lucene.properties");
        props.load(in);
        return Paths.get(props.getProperty("image.search.path"));
    }

    private static Path getImageDirPath() throws URISyntaxException {
        return Paths.get(IndexMain.class.getClassLoader().getResource("image/data").toURI());
    }

    @SneakyThrows
    private static void gainImageFilePath(Path imageDirPath, Collection<Path> imageFilePathList) {
        Files.list(imageDirPath).forEach(path -> {
            if (Files.isDirectory(path)) {
                gainImageFilePath(path, imageFilePathList);
            } else {
                imageFilePathList.add(path);
            }
        });
    }

    private static void indexFile(Collection<Path> imageFilePathList, Path indexPath) {
        IndexWriterConfig conf = new IndexWriterConfig(new WhitespaceAnalyzer());
        try (IndexWriter indexWriter = new IndexWriter(FSDirectory.open(indexPath), conf);) {
            GlobalDocumentBuilder globalDocumentBuilder = new GlobalDocumentBuilder();
            //globalDocumentBuilder.addExtractor(CEDD.class);
            //globalDocumentBuilder.addExtractor(FCTH.class);
            globalDocumentBuilder.addExtractor(JCD.class);
            //globalDocumentBuilder.addExtractor(AutoColorCorrelogram.class);
            for (Path imageFilePath : imageFilePathList) {
                BufferedImage img = ImageIO.read(new FileInputStream(imageFilePath.toString()));
                // 创建基于图片生成的索引文档
                Document document = globalDocumentBuilder.createDocument(img, imageFilePath.toString());
                //新增文档属性
                document.add(new StringField("image", imageFilePath.toString(), Field.Store.YES));
                indexWriter.addDocument(document);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

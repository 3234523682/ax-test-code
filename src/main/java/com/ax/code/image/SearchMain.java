package com.ax.code.image;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.global.JCD;
import net.semanticmetadata.lire.searchers.GenericFastImageSearcher;
import net.semanticmetadata.lire.searchers.ImageSearchHits;
import net.semanticmetadata.lire.searchers.ImageSearcher;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

/**
 * @author lj
 */
public class SearchMain {

    public static void main(String[] args) throws IOException {
        String imageFile = "K:\\Backups\\头像\\666.jpg";
        search(Paths.get(imageFile), IndexMain.getIndexPath());
    }

    /**
     * 图片检索
     *
     * @param imagePath 检索图片路径
     * @param indexPath 索引图片目录路径
     */
    private static void search(Path imagePath, Path indexPath) {
        BufferedImage img;
        try {
            img = ImageIO.read(imagePath.toFile());
            IndexReader indexReader = DirectoryReader.open(FSDirectory.open(indexPath));
            // 最大10个文件检索
            ImageSearcher searcher = new GenericFastImageSearcher(10, JCD.class);
            ImageSearchHits hits = searcher.search(img, indexReader);
            for (int i = 0; i < hits.length(); i++) {
                Set<String> fields = new HashSet<>();
                fields.add(DocumentBuilder.FIELD_NAME_IDENTIFIER);
                String identifier = indexReader
                        .document(hits.documentID(i), fields)
                        .getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue();
                // 输出匹配图像的得分和图片路径
                System.out.println("Score:" + hits.score(i) + "\tFile:" + identifier);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package com.ax.code;

import java.io.IOException;
import java.util.regex.Pattern;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * Hello world!
 */
public class App {

    public static final Pattern REGISTRY_SPLIT_PATTERN = Pattern.compile("\\s*[|;]+\\s*");

    public static void main(String[] args) throws Exception {
        try {
            long nowTime = System.currentTimeMillis();
            Thumbnails.of("C:\\Users\\Mr_lin\\Downloads\\a4.jpg").
                    scale(1).
                    outputQuality(0.6). // 图片压缩80%质量
                    toFile("C:\\Users\\Mr_lin\\Downloads\\a_4.jpg");
            System.out.println("耗时：" + (System.currentTimeMillis() - nowTime));
        } catch (IOException e) {
            System.out.println("原因: " + e.getMessage());
        }

    }

    public static void sss(String... ss) {
        for (String val : ss) {
            System.out.println(val);
        }
    }

    private static void lock() throws Exception {
        //创建zookeeper的客户端
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.newClient("47.107.111.51:2181", retryPolicy);
        client.start();

        //创建分布式锁, 锁空间的根节点路径为/curator/lock
        InterProcessMutex mutex = new InterProcessMutex(client, "/curator/lock");
        mutex.acquire();
        //获得了锁, 进行业务流程
        System.out.println("Enter mutex");
        //完成业务流程, 释放锁
        mutex.release();

        //关闭客户端
        client.close();
    }


    static class A {

        private Long a;

        public A() {
            System.out.println("============ A 创建！");
        }

        public Long getA() {
            return a;
        }

        public void setA(Long a) {
            this.a = a;
        }
    }

}

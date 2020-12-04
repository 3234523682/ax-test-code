package com.ax.code.test;

import javax.swing.*;

/**
 * @author lj
 */
public class New extends JFrame {

    public static void main(String[] args) {
        new New();
    }

    public New() {
        // 设置窗口x y坐标           
        this.setLocation(400, 300); // ------------
        // 设置窗口大小
        this.setSize(214, 152);
        // 设置窗口可视(True Or False)
        this.setVisible(true);
        // <h3>设置窗口关闭程序就停止<h3>
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // 设置窗口不可移动
        this.setResizable(false);
    }
}

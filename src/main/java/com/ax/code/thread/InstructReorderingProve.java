package com.ax.code.thread;

/**
 * cpu指令重排序证明（反证法）
 *
 * @author lj
 */
public class InstructReorderingProve {

    private static int a = 0, b = 0, x = 0, y = 0;

    public static void main(String[] args) throws InterruptedException {
        int i = 0;
        while (true) {
            i++;
            a = 0;
            b = 0;
            x = 0;
            y = 0;
            Thread thread_1 = new Thread(() -> {
                a = 1;
                x = b;
            });
            Thread thread_2 = new Thread(() -> {
                b = 1;
                y = a;
            });
            thread_1.start();
            thread_2.start();
            thread_1.join();
            thread_2.join();
            if (x == 0 && y == 0) {
                System.out.println(i + "次");
                return;
            }
        }
    }
}

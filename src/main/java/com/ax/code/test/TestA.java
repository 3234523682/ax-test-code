package com.ax.code.test;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lj
 */
@Data
public class TestA implements Serializable {

    private Long id;

    private String name;

    private String describe;

    public static void setDesc(String describe) {
        System.out.println(describe);
    }

    public TestA() {
    }

    public TestA(Long id, String name, String describe) {
        this.id = id;
        this.name = name;
        this.describe = describe;
    }
}

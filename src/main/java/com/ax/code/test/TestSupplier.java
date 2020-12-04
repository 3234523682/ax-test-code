package com.ax.code.test;

import java.util.function.LongSupplier;
import lombok.Getter;

/**
 * @author lj
 */
@Getter
public class TestSupplier implements LongSupplier {

    private long prevVal = 0;

    private long currentVal = 1;

    @Override
    public long getAsLong() {
        long val = prevVal + currentVal;
        this.prevVal = currentVal;
        this.currentVal = val;
        return val;
    }
}

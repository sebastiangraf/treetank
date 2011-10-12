package org.treetank.encryption.benchmarking;

import org.perfidix.meter.AbstractMeter;

public class KtsMeter extends AbstractMeter {

    private double counter = 0;

    private static final KtsMeter SINGLETON = new KtsMeter();

    private final static String NAME = "BLABLUBB";

    private KtsMeter() {
    }

    @Override
    public String getName() {
        return "Key Trail Meter";
    }

    @Override
    public String getUnit() {
        return "kts";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = NAME.hashCode();
        result = prime * result + (int)(temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        return this.hashCode() == obj.hashCode();
    }

    @Override
    public String getUnitDescription() {
        return "kts";
    }

    @Override
    public double getValue() {
        return counter;
    }

    public void count(final int count) {
        counter += count;
    }

    public void reset() {
        counter = 0;
    }

    public static KtsMeter getInstance() {
        return SINGLETON;
    }

}

package com.treetank.www2010;

import com.treetank.api.IFilter;
import com.treetank.api.IReadTransaction;

import org.perfidix.meter.AbstractMeter;
import org.perfidix.meter.CountingMeter;

public class CounterFilter extends AbstractMeter implements IFilter {

    private final IFilter mFilter;
    private final CountingMeter mMeter;

    public CounterFilter(final IFilter paramFilter, final CountingMeter paramMeter) {
        this.mFilter = paramFilter;
        mMeter = paramMeter;
    }

    @Override
    public IReadTransaction getTransaction() {
        return this.mFilter.getTransaction();
    }

    @Override
    public boolean filter() {
        if (mFilter.filter()) {
            mMeter.tick();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public double getValue() {
        return mMeter.getValue();
    }

    @Override
    public String getUnitDescription() {
        return mMeter.getUnitDescription();
    }

    @Override
    public String getName() {
        return mMeter.getName();
    }

    @Override
    public int hashCode() {
        return mMeter.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this.hashCode() == obj.hashCode();
    }

    @Override
    public String getUnit() {
        return mMeter.getUnit();
    }

}

package com.treetank.www2010;

import com.treetank.api.IAxis;
import com.treetank.api.IFilter;
import com.treetank.axis.DescendantAxis;

import org.perfidix.meter.AbstractMeter;

public class NodeCounter extends AbstractMeter {

    private final IFilter mFilter;

    private long counter;

    public NodeCounter(final IFilter paramFilter) {
        this.mFilter = paramFilter;
        counter = 0;
    }

    public void call() {
        final IAxis desc = new DescendantAxis(mFilter.getTransaction());
        while (desc.hasNext()) {
            desc.next();
            if (mFilter.filter()) {
                counter++;
            }
        }
    }

    public void reset() {
        counter = 0;
    }

    @Override
    public double getValue() {
        return counter;
    }

    @Override
    public String getUnitDescription() {
        return "nodes touched";
    }

    @Override
    public String getName() {
        return "nodes";
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return true;
    }

    @Override
    public String getUnit() {
        return "n";
    }

    public IFilter getFilter() {
        return this.mFilter;
    }

}

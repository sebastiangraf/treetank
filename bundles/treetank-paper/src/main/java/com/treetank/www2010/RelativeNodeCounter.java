package com.treetank.www2010;

import com.treetank.api.IAxis;
import com.treetank.api.IFilter;
import com.treetank.axis.DescendantAxis;

import org.perfidix.meter.AbstractMeter;

public class RelativeNodeCounter extends AbstractMeter {

    private final IFilter mFilter;

    private long counter;
    private long overallCounter;

    public RelativeNodeCounter(final IFilter paramFilter) {
        this.mFilter = paramFilter;
        counter = 0;
        overallCounter = 1;
    }

    public void call() {
        final IAxis desc = new DescendantAxis(mFilter.getTransaction());
        while (desc.hasNext()) {
            desc.next();
            if (mFilter.filter()) {
                counter++;
            }
            overallCounter++;
        }
    }

    public void reset() {
        counter = 0;
        overallCounter = 1;
    }

    @Override
    public double getValue() {
        return counter / overallCounter;
    }

    @Override
    public String getUnitDescription() {
        return "relative nodes touched";
    }

    @Override
    public String getName() {
        return "relative nodes touched";
    }

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass() == this.getClass();
    }

    @Override
    public String getUnit() {
        return "n/n";
    }

    public IFilter getFilter() {
        return this.mFilter;
    }

}

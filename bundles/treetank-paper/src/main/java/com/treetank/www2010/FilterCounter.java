package com.treetank.www2010;

import java.util.concurrent.Callable;

import com.treetank.api.IFilter;
import com.treetank.api.IReadTransaction;
import com.treetank.axis.AbsAxis;
import com.treetank.axis.DescendantAxis;
import com.treetank.www2010.CollisionTester.HashFilter;

import org.perfidix.meter.AbstractMeter;

public class FilterCounter extends AbstractMeter implements Callable<Void> {

    private final HashFilter mFilter;

    private long counter;

    public FilterCounter(final HashFilter paramFilter) {
        this.mFilter = paramFilter;
        counter = 0;
    }

    public Void call() {
        final AbsAxis desc = new DescendantAxis(mFilter.getTransaction());
        while (desc.hasNext()) {
            desc.next();
            if (mFilter.filter()) {
                counter++;
            }
        }
        return null;
    }

    public void reset() {
        counter = 0;
        ((HashFilter)mFilter).reset();
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
        return "filter nodes";
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass() == this.getClass();
    }

    @Override
    public String getUnit() {
        return "n";
    }

    public IFilter getFilter() {
        return this.mFilter;
    }

}

package org.treetank.www2010;

import java.util.concurrent.Callable;

import com.treetank.api.IReadTransaction;
import com.treetank.axis.AbsAxis;
import com.treetank.axis.DescendantAxis;

import org.perfidix.meter.AbstractMeter;

public class NodeCounter extends AbstractMeter implements Callable<Void> {

    private IReadTransaction rtx;
    private long counter;

    public NodeCounter() {
        counter = 0;
    }

    public Void call() {
        final AbsAxis desc = new DescendantAxis(rtx);
        while (desc.hasNext()) {
            desc.next();
            counter++;
        }
        return null;
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
        return "all nodes";
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

    public void setRtx(IReadTransaction rtx) {
        this.rtx = rtx;
    }

    public IReadTransaction getRtx() {
        return rtx;
    }

}

package org.treetank.service.xml.xpath.xmark;

import org.perfidix.element.KindOfArrangement;
import org.perfidix.meter.AbstractMeter;
import org.perfidix.meter.MemMeter;
import org.perfidix.meter.Memory;
import org.perfidix.meter.Time;
import org.perfidix.meter.TimeMeter;
import org.perfidix.ouput.AbstractOutput;
import org.perfidix.AbstractConfig;

public class BenchConfigClass extends AbstractConfig {

    private final static int RUNS = 10;
    private final static AbstractMeter[] METERS = {
        new TimeMeter(Time.Seconds), new MemMeter(Memory.Mebibyte)
    };
    private final static AbstractOutput[] OUTPUT = {/*
                                                     * new
                                                     * TabularSummaryOutput()
                                                     */};
    private final static KindOfArrangement ARRAN = KindOfArrangement.SequentialMethodArrangement;
    private final static double GCPROB = 1.0d;

    public BenchConfigClass() {
        super(RUNS, METERS, OUTPUT, ARRAN, GCPROB);

    }

}

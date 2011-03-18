package org.treetank.service.xml.xpath.concurrent;

import org.perfidix.AbstractConfig;
import org.perfidix.element.KindOfArrangement;
import org.perfidix.meter.AbstractMeter;
import org.perfidix.meter.MemMeter;
import org.perfidix.meter.Memory;
import org.perfidix.meter.Time;
import org.perfidix.meter.TimeMeter;
import org.perfidix.ouput.AbstractOutput;

/**
 * Config class for Perfidix test settings.
 * 
 * @author Patrick Lang, University of Konstanz
 */
public class ConcurrentBenchConfig extends AbstractConfig {

    /**
     * Number of runs.
     */
    private final static int RUNS = 20;

    /**
     * Test units of time and memory.
     */
    private final static AbstractMeter[] METERS = {
        new TimeMeter(Time.Seconds), new MemMeter(Memory.Mebibyte)
    };

    /**
     * Kind of output result viewer.
     */
    private static final AbstractOutput[] OUTPUT = {/*
                                                     * new
                                                     * TabularSummaryOutput()
                                                     */};
    /**
     * Kind of arrangement.
     */
    private final static KindOfArrangement ARRAN = KindOfArrangement.SequentialMethodArrangement;

    /**
     * Member gcProb.
     */
    private final static double GCPROB = 1.0d;

    /**
     * Constructor to set settings.
     */
    public ConcurrentBenchConfig() {
        super(RUNS, METERS, OUTPUT, ARRAN, GCPROB);

    }

}

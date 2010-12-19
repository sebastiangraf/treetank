package com.treetank.xpath.concurrentaxislayer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Exchanger;

/**
 * <h1>ConcurrentExchanger</h1>
 * <p>
 * This class is a singleton for an Exchanger to exchange content of threads.
 * </p>
 * 
 * @author Patrick Lang, Konstanz University
 */
public class ConcurrentExchanger {

    /**
     * Singleton instance that is initialized only once on first class access through JVM.
     */
    private static Exchanger<BlockingQueue<Long>> EXCHANGER = new Exchanger<BlockingQueue<Long>>();

    /**
     * Default constructor.
     */
    private ConcurrentExchanger() {
    }

    /**
     * Return exchanger instance.
     * 
     * @return exchanger instance
     */
    public static Exchanger<BlockingQueue<Long>> getInstance() {
        return EXCHANGER;
    }

}

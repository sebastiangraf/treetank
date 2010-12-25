/*
 * Copyright (c) 2010, Patrick Lang (Master Project), University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */

package com.treetank.service.xml.xpath.concurrent;

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

/**
 * Copyright (c) 2010, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */
package com.treetank.wikipedia.hadoop;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.xml.stream.events.XMLEvent;

import org.apache.hadoop.mapreduce.Reducer;

/**
 * <h1>XMLReducer</h1>
 * 
 * <p>
 * After sorting and grouping key's the reducer just emits the results (identity reducer).
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class XMLReduce extends Reducer<Date, List<XMLEvent>, Date, List<XMLEvent>> {

    /** 
     * Empty Constructor. 
     */
    public XMLReduce() {
        // To make Checkstyle happy.
    }
    
    @Override
    public void reduce(final Date paramKey, final Iterable<List<XMLEvent>> paramValue,
        final Context paramContext) throws IOException, InterruptedException {
        for (final List<XMLEvent> events : paramValue) {
            paramContext.write(paramKey, events);
        }
    }
}

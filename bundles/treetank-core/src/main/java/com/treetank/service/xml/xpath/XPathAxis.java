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

package com.treetank.service.xml.xpath;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.treetank.api.IReadTransaction;
import com.treetank.axis.AbsAxis;
import com.treetank.exception.TTXPathException;
import com.treetank.service.xml.xpath.parser.XPathParser;

/**
 * <h1>XPath Axis</h1>
 * <p>
 * Evaluates a given XPath query.
 * </p>
 * <p>
 * Axis to iterate over the items (more precisely the item keys) of the query's result sequence.
 * <code>XPathAxis</code> extends treetanks <code>IAxis</code> that extends the well-known Java
 * <code>Iterator&lt;Long&gt;</code> and <code>Iterable&lt;Long&gt;</code> interfaces.
 * </p>
 * <h2>User Example</h2>
 * <p>
 * In order to use it, at first a treetank session has to be bound to the XML document in question or an tnk
 * file and a <code>ReadTransaction</code> with an <code>IItemList</code> as argument has to be started on it.
 * (For more information how to do that, see the treetank documentation.) Then the <code>XPathAxis</code> can
 * be used like this:
 * <p>
 * 
 * <pre>
 *   ...
 *   IReadTransaction rtx = session.beginReadTransaction(new ItemList());
 *   
 *   final String query = 
 *   &quot;for $a in /articles/article[@name = \&quot;book\&quot;] return $a/price&quot;;
 *   
 *   final IAxis axis = new XPathAxis(rtx, query);
 *   while (axis.hasNext()) {
 *     // Move transaction cursor to do something.
 *     axis.next();
 *     System.out.println(rtx.getValueAsInt()););
 *   }
 *   ...
 * </pre>
 * 
 * <pre>
 *   ...
 *   for (final long key : new XPathAxis(rtx, query)) {
 *      ...
 *   }
 *   ...
 * </pre>
 * 
 * </p>
 */
public final class XPathAxis extends AbsAxis {

    /** Declares if the evaluation is compatible to XPath 1.0 or not. */
    public static final boolean XPATH_10_COMP = true;

    /** Size of thread pool for executor service. */
    private static int THREADPOOLSIZE = 2;

    /** Executor Service holding the execution plan for future tasks. */
    public static ExecutorService EXECUTOR;

    /** Axis holding the consecutive query execution plans of the query. */
    private AbsAxis mPipeline;

    /**
     * <p>
     * Constructor initializing internal state.
     * </p>
     * <p>
     * Starts the query scanning and parsing and retrieves the builded query execution plan from the parser.
     * </p>
     * 
     * @param rtx
     *            Transaction to operate with.
     * @param mQuery
     *            XPath query to process.
     * @throws TTXPathException
     *             throw a treetank xpath exception.
     */
    public XPathAxis(final IReadTransaction rtx, final String mQuery) throws TTXPathException {

        super(rtx);

        /** Initializing executor service with fixed thread pool. */
        EXECUTOR = Executors.newFixedThreadPool(THREADPOOLSIZE);

        // start parsing and get execution plans
        final XPathParser parser = new XPathParser(getTransaction(), mQuery);
        parser.parseQuery();
        mPipeline = parser.getQueryPipeline();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {

        resetToLastKey();

        if (mPipeline.hasNext()) {
            return true;
        } else {

            resetToStartKey();
            return false;
        }

    }

}

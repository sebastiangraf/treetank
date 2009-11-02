/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
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
 * $Id: TreeTankDebugger.java 4414 2008-08-27 20:01:07Z kramis $
 */

package com.treetank.utils;

import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.exception.TreetankException;
import com.treetank.session.Session;

/**
 * <h1>TreeTankDebugger</h1>
 * 
 * <p>
 * Print the core data structure for debugging purposes.
 * </p>
 */
public final class TreeTankDebugger {

    /**
     * Hidden constructor.
     */
    private TreeTankDebugger() {
        // Hidden.
    }

    /**
     * Simple public static void main to start.
     * 
     * @param args
     *            must be a filepath to the file evaluated
     */
    public static void main(final String... args) {
        try {
            if (args.length != 1) {
                System.out.println("Usage: TreeTankDebugger <file>");
                System.exit(1);
            }

            final ISession session = Session.beginSession(args[0]);
            final IReadTransaction rtx = session.beginReadTransaction();

            // Print header.
            System.out.println("TreeTank '" + session.getFileName() + "':");
            System.out
                    .println("----------------------------------------------------------------------------------------------------------");
            System.out
                    .println("|   K|    PK|    FCK|    LSK|    RSK|     CC| Name                       | Value                         |");
            System.out
                    .println("----------------------------------------------------------------------------------------------------------");

            // Print node by node.
            final long size = rtx.getNodeCount();
            for (long i = 0; i < size; i++) {
                if (rtx.moveTo(i)) {
                    System.out
                            .printf(
                                    "|%4d|\t%4d|\t%4d|\t%4d|\t%4d|\t%4d|\t%-25s|\t%-25s|\n",
                                    rtx.getNode().getNodeKey(), rtx.getNode()
                                            .getParentKey(), rtx.getNode()
                                            .getFirstChildKey(), rtx.getNode()
                                            .getLeftSiblingKey(), rtx.getNode()
                                            .getRightSiblingKey(), rtx
                                            .getNode().getChildCount(), rtx
                                            .nameForKey(rtx.getNode()
                                                    .getNameKey()), rtx
                                            .getNode().isText() ? TypedValue
                                            .parseString(rtx.getNode()
                                                    .getRawValue()) : "");
                } else {
                    System.out.println("ERROR while debugging.");
                    System.exit(1);
                }
            }

            rtx.close();
            session.close();
        } catch (final TreetankException exc) {
            exc.printStackTrace();
            System.exit(-1);
        }
    }
}

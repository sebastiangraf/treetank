package com.treetank.service.xml.xpath.filter;

///*
// * Copyright (c) 2008, Tina Scherer (Master Thesis), University of Konstanz
// * 
// * Permission to use, copy, modify, and/or distribute this software for any
// * purpose with or without fee is hereby granted, provided that the above
// * copyright notice and this permission notice appear in all copies.
// * 
// * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
// * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
// * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
// * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
// * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
// * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
// * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
// * 
// * $Id: PosFilter.java 4238 2008-07-03 13:32:30Z scherer $
// */
//
//package org.treetank.xpath.filter;
//
//import org.treetank.api.IAxis;
//import org.treetank.api.IReadTransaction;
//import org.treetank.axislayer.AbstractAxis;
//
///**
// * @author Tina Scherer
// */
//public class PosFilter extends AbstractAxis implements IAxis {
//
//  private final int mExpectedPos;
//
//  /** The position of the current item. */
//  private int mPosCount;
//
//  /**
//   * Constructor. Initializes the internal state.
//   * 
//   * @param rtx
//   *          Exclusive (immutable) trx to iterate with.
//   * @param expectedPos
//   *          he expected position
//   */
//  public PosFilter(final IReadTransaction rtx, final int expectedPos) {
//
//    super(rtx);
//    mExpectedPos = expectedPos;
//    mPosCount = 0;
//  }
//
//  /**
//   * {@inheritDoc}
//   */
//  @Override
//  public final void reset(final long nodeKey) {
//
//    super.reset(nodeKey);
//    mPosCount = 0;
//  }
//
//  /**
//   * {@inheritDoc}
//   */
//  public final boolean hasNext() {
//
//    resetToLastKey();
//
//    // a predicate has to evaluate to true only once.
//    if (mExpectedPos == ++mPosCount) {
//      return true;
//    }
//
//    resetToStartKey();
//    return false;
//
//  }
//
// }
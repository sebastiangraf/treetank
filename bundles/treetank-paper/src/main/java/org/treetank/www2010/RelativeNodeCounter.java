/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.www2010;

import java.util.concurrent.Callable;

import com.treetank.api.IFilter;
import com.treetank.axis.AbsAxis;
import com.treetank.axis.DescendantAxis;

import org.perfidix.meter.AbstractMeter;
import org.treetank.www2010.CollisionTester.HashFilter;

public class RelativeNodeCounter extends AbstractMeter implements Callable<Void> {

    private final HashFilter mFilter;

    private double counter;
    private double overallCounter;

    public RelativeNodeCounter(final HashFilter paramFilter) {
        this.mFilter = paramFilter;
        counter = 0;
        overallCounter = 1;
    }

    public Void call() {
        final AbsAxis desc = new DescendantAxis(mFilter.getTransaction());
        while (desc.hasNext()) {
            desc.next();
            if (mFilter.filter()) {
                counter++;
            }
            overallCounter++;
        }
        return null;
    }

    public void reset() {
        counter = 0;
        overallCounter = 1;
        ((HashFilter)mFilter).reset();
    }

    @Override
    public double getValue() {
        return counter / overallCounter * 100;
    }

    @Override
    public String getUnitDescription() {
        return "relative nodes touched";
    }

    @Override
    public String getName() {
        return "relative nodes touched";
    }

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass() == this.getClass();
    }

    @Override
    public String getUnit() {
        return "n/n";
    }

    public IFilter getFilter() {
        return this.mFilter;
    }

}

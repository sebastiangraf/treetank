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
 *     * Neither the name of the <organization> nor the
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

package org.treetank.wikipedia.hadoop;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.hadoop.io.WritableComparable;
import org.slf4j.LoggerFactory;

import com.treetank.service.xml.shredder.XMLShredder;
import com.treetank.utils.LogWrapper;

/**
 * <h1>DateWritable</h1>
 * 
 * <p>
 * Simple date wrapper which implements {@link WritableComparable}, so it can be used as a key.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class DateWritable implements WritableComparable<DateWritable> {

    /**
     * {@link LogWrapper} used for logging.
     */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory.getLogger(XMLShredder.class));

    /** {@link DateFormat}. */
    private final DateFormat mFormatter = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss", Locale.ENGLISH);

    /** {@link Date} used as the key. */
    private Date mTimestamp;

    /**
     * Constructor.
     */
    public DateWritable() {
        
    }
    
    /**
     * Constructor.
     * 
     * @param paramTimestamp
     *                  The {@link Date} to use as the key.
     */
    public DateWritable(final Date paramTimestamp) {
        mTimestamp = paramTimestamp;
    }
    
    /**
     * Set timestamp.
     * 
     * @param paramTimestamp
     *            The Timestamp to set.
     */
    public void setTimestamp(final Date paramTimestamp) {
        mTimestamp = paramTimestamp;
    }

    @Override
    public void readFields(final DataInput paramIn) throws IOException {
        try {
            mTimestamp = mFormatter.parse(paramIn.readUTF());
        } catch (final ParseException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }
    }

    @Override
    public void write(final DataOutput paramOut) throws IOException {
        paramOut.writeUTF(mFormatter.format(mTimestamp));
    }

    @Override
    public int compareTo(final DateWritable paramDate) {
        int retVal = 0;

        if (paramDate.mTimestamp.before(this.mTimestamp)) {
            retVal = 1;
        } else if (paramDate.mTimestamp.after(this.mTimestamp)) {
            retVal = -1;
        }

        return retVal;
    }
}

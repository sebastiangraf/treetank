/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
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

package org.treetank.service.xml.shredder;

/**
 * Container for hierarichal network data (Continent, Country, AS, Prefix).
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public class NDataTuple {

    /** Continent. */
    private transient String mContinent;

    /** Country. */
    private transient String mCountry;

    /** Autonomous System. */
    private transient String mAS;

    /** IP-Prefix. */
    private transient String mPrefix;

    /**
     * Constructor.
     * 
     * @param paramContinent
     *            Continent.
     * @param paramCountry
     *            Country.
     * @param paramAS
     *            Autonomous System.
     * @param paramPrefix
     *            IP-Prefix.
     */
    public NDataTuple(final String paramContinent, final String paramCountry, final String paramAS,
        final String paramPrefix) {
        mContinent = paramContinent;
        mCountry = paramCountry;
        mAS = paramAS;
        mPrefix = paramPrefix;
    }

    /**
     * @return the mContinent
     */
    public String getmContinent() {
        return mContinent;
    }

    /**
     * @param mContinent
     *            the mContinent to set
     */
    public void setmContinent(String mContinent) {
        this.mContinent = mContinent;
    }

    /**
     * @return the mCountry
     */
    public String getmCountry() {
        return mCountry;
    }

    /**
     * @param mCountry
     *            the mCountry to set
     */
    public void setmCountry(String mCountry) {
        this.mCountry = mCountry;
    }

    /**
     * @return the mAS
     */
    public String getmAS() {
        return mAS;
    }

    /**
     * @param mAS
     *            the mAS to set
     */
    public void setmAS(String mAS) {
        this.mAS = mAS;
    }

    /**
     * @return the mPrefix
     */
    public String getmPrefix() {
        return mPrefix;
    }

    /**
     * @param mPrefix
     *            the mPrefix to set
     */
    public void setmPrefix(String mPrefix) {
        this.mPrefix = mPrefix;
    }

}

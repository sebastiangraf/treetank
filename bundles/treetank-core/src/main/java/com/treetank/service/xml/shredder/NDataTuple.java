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
package com.treetank.service.xml.shredder;

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
     *                        Continent.
     * @param paramCountry
     *                        Country.
     * @param paramAS
     *                        Autonomous System.
     * @param paramPrefix
     *                        IP-Prefix.
     */
    public NDataTuple(final String paramContinent, final String paramCountry, final String paramAS, final String paramPrefix) {
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
     * @param mContinent the mContinent to set
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
     * @param mCountry the mCountry to set
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
     * @param mAS the mAS to set
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
     * @param mPrefix the mPrefix to set
     */
    public void setmPrefix(String mPrefix) {
        this.mPrefix = mPrefix;
    } 
    
    
}

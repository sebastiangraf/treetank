package org.treetank.jscsi.buffering;

/**
 * If a write task hasn't been performed yet
 * but the newest version has to be read,
 * collisions determine which bytes to overwrite and
 * send back.
 * 
 * @author Andreas Rain
 *
 */
public class Collision{
    private final int mStart;
    private final int mEnd;
    private final byte[] mBytes;
    
    public Collision(int pStart, int pEnd, byte[] pBytes) {
        super();
        this.mStart = pStart;
        this.mEnd = pEnd;
        this.mBytes = pBytes;
    }

    public int getStart() {
        return mStart;
    }

    public int getEnd() {
        return mEnd;
    }

    public byte[] getBytes() {
        return mBytes;
    }
    
}

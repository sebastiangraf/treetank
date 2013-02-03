package org.treetank.log;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

/**
 * Binding for serializing LogKeys in the BDB.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class LogKeyBinding extends TupleBinding<LogKey> {

    /**
     * {@inheritDoc}
     */
    @Override
    public LogKey entryToObject(TupleInput arg0) {
        final ByteArrayDataInput data = ByteStreams.newDataInput(arg0.getBufferBytes());
        final LogKey key = new LogKey(data.readBoolean(), data.readLong(), data.readLong());
        return key;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void objectToEntry(LogKey arg0, TupleOutput arg1) {
        final ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeBoolean(arg0.isRootLevel());
        output.writeLong(arg0.getLevel());
        output.writeLong(arg0.getSeq());
        arg1.write(output.toByteArray());
    }

}

package org.treetank.cache;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class LogKeyBinding extends TupleBinding<LogKey> {

    @Override
    public LogKey entryToObject(TupleInput arg0) {
        final ByteArrayDataInput data = ByteStreams.newDataInput(arg0.getBufferBytes());
        final LogKey key = new LogKey(data.readLong(), data.readLong());
        return key;
    }

    @Override
    public void objectToEntry(LogKey arg0, TupleOutput arg1) {
        final ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeLong(arg0.getLevel());
        output.writeLong(arg0.getSeq());
        arg1.write(output.toByteArray());
    }

}

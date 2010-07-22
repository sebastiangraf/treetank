package com.treetank.service.xml.serialize;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.Callable;

import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.utils.IConstants;

public class RevsionedXMLSerializer /*
                                     * extends AbsSerializeStorage implements
                                     * Callable<Void>
                                     */{

    // /** Offset that must be added to digit to make it ASCII. */
    // private static final int ASCII_OFFSET = 48;
    //
    // /** Precalculated powers of each available long digit. */
    // private static final long[] LONG_POWERS = { 1L, 10L, 100L, 1000L, 10000L,
    // 100000L, 1000000L, 10000000L, 100000000L, 1000000000L,
    // 10000000000L, 100000000000L, 1000000000000L, 10000000000000L,
    // 100000000000000L, 1000000000000000L, 10000000000000000L,
    // 100000000000000000L, 1000000000000000000L };
    //
    // private final long[] mVersions;
    // private final ISession mSession;
    // private final OutputStream mStream;
    // private final boolean mTimeStampUsed;
    //
    // final ByteArrayOutputStream out = new ByteArrayOutputStream();
    //
    // public RevsionedXMLSerializer(final OutputStream stream,
    // final ISession session, final boolean timeStampUsed,
    // final long... versions) {
    // mVersions = versions;
    // mSession = session;
    // mStream = stream;
    // mTimeStampUsed = timeStampUsed;
    //
    // }
    //
    // @Override
    // public void serialize() throws Exception {
    // XMLSerializer serializer;
    //
    // IReadTransaction rtx = mSession.beginReadTransaction();
    // final long lastVersion = rtx.getRevisionNumber();
    // rtx.close();
    //
    // write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
    // write("<ttexport>");
    //
    // for (long i = 0; i <= (mVersions.length == 0 ? lastVersion
    // : mVersions.length - 1); i++) {
    // out.reset();
    // rtx = mSession.beginReadTransaction(mVersions.length == 0 ? i
    // : mVersions[(int) i]);
    // write("<tt ");
    // if (mTimeStampUsed)
    // write("timestamp=\"" + rtx.getRevisionTimestamp() + "\" ");
    // write("revision=\""
    // + (mVersions.length == 0 ? i : mVersions[(int) i]) + "\">");
    // serializer = new XMLSerializer(rtx, out, false, false);
    // serializer.call();
    // write(out.toString());
    // write("</tt>");
    //
    // }
    //
    // write("</ttexport>");
    // rtx.close();
    // }
    //
    // @Override
    // public void emitNode() throws IOException {
    // // TODO Auto-generated method stub
    //
    // }
    //
    // @Override
    // public void emitEndElement() throws IOException {
    // // TODO Auto-generated method stub
    //
    // }
    //
    // /**
    // * Write characters of string.
    // *
    // * @throws IOException
    // * @throws UnsupportedEncodingException
    // */
    // private void write(final String string)
    // throws UnsupportedEncodingException, IOException {
    // mStream.write(string.getBytes(IConstants.DEFAULT_ENCODING));
    // }

}

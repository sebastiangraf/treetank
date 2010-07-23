package com.treetank.service.xml.serialize;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.Callable;

import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.axis.DescendantAxis;
import com.treetank.service.xml.serialize.SerializerBuilder.RevisionedXMLSerializerBuilder;
import com.treetank.utils.IConstants;

public class RevisionedXMLSerializer extends XMLSerializer
    implements
    Callable<Void> {

  /** Treetank session {@link ISession}. */
  private final ISession mSession;

  /** Builder used to build a instance of this class {@link SerializerBuilder}. */
  private final RevisionedXMLSerializerBuilder mBuilder;

  /** Output stream internally wrapped into a. */
  private final OutputStream mStream;

  /** Determines if a timestamp is used or not. */
  private final boolean mIsTimestampUsed;

  private final long[] mVersions;

  final ByteArrayOutputStream out = new ByteArrayOutputStream();

  public RevisionedXMLSerializer(
      final ISession paramSession,
      final RevisionedXMLSerializerBuilder builder) {
    super(null, builder);
    mBuilder = builder;
    mSession = paramSession;
    mStream = builder.getStream();
    mIsTimestampUsed = builder.isTimestamp();
    mVersions = new long[builder.getVersions().length];
    System.arraycopy(builder.getVersions(), 0, mVersions, 0, mVersions.length);

  }

  @Override
  public void serialize() throws Exception {
    XMLSerializer serializer;

    IReadTransaction rtx = mSession.beginReadTransaction();
    final long lastVersion = rtx.getRevisionNumber();
    rtx.close();

    write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
    write("<ttexport>");

    for (long i = 0; i <= (mVersions.length == 0
        ? lastVersion
        : mVersions.length - 1); i++) {
      out.reset();
      rtx =
          mSession.beginReadTransaction(mBuilder.getVersions().length == 0
              ? i
              : mBuilder.getVersions()[(int) i]);
      write("<tt ");
      if (mIsTimestampUsed)
        write("timestamp=\"" + rtx.getRevisionTimestamp() + "\" ");
      write("revision=\""
          + (mVersions.length == 0 ? i : mVersions[(int) i])
          + "\">");
      serializer = new XMLSerializer(new DescendantAxis(rtx), mBuilder);
      serializer.call();
      write(out.toString());
      write("</tt>");

    }

    write("</ttexport>");
    rtx.close();
  }

  @Override
  public void emitNode() throws IOException {
    throw new UnsupportedOperationException(
        "Emit node not used in revisioned XML Shredder!");
  }

  @Override
  public void emitEndElement() throws IOException {
    throw new UnsupportedOperationException(
        "Emit node not used in revisioned XML Shredder!");
  }

  /**
   * Write characters of string.
   * 
   * @throws IOException
   * @throws UnsupportedEncodingException
   */
  private void write(final String string)
      throws UnsupportedEncodingException,
      IOException {
    mStream.write(string.getBytes(IConstants.DEFAULT_ENCODING));
  }

  @Override
  public Void call() throws Exception {
    serialize();
    return null;
  }

}

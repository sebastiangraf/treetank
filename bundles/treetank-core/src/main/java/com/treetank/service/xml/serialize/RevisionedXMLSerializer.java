package com.treetank.service.xml.serialize;

import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.axis.DescendantAxis;
import com.treetank.service.xml.serialize.SerializerBuilder.RevisionedXMLSerializerBuilder;

public class RevisionedXMLSerializer extends XMLSerializer {

    /** Treetank session {@link ISession}. */
    private final ISession mSession;

    /**
     * Builder used to build a instance of this class {@link SerializerBuilder}.
     */
    private final RevisionedXMLSerializerBuilder mBuilder;

    /** Determines if a timestamp is used or not. */
    private final boolean mIsTimestampUsed;

    /**
     * versions which should be serialized, if not set all versions are
     * serialized
     */
    private final long[] mVersions;

    public RevisionedXMLSerializer(final ISession paramSession,
            final RevisionedXMLSerializerBuilder builder) {
        super(null, builder);
        mBuilder = builder;
        mSession = paramSession;
        mIsTimestampUsed = builder.isTimestamp();
        mVersions = new long[builder.getVersions().length];
        System.arraycopy(builder.getVersions(), 0, mVersions, 0,
                mVersions.length);

    }

    @Override
    protected void serialize() throws Exception {
        XMLSerializer serializer;

        IReadTransaction rtx = mSession.beginReadTransaction();
        final long lastVersion = rtx.getRevisionNumber();
        rtx.close();

        write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        write("<ttexport>");

        for (long i = 0; i <= (mVersions.length == 0 ? lastVersion
                : mVersions.length - 1); i++) {
            rtx = mSession
                    .beginReadTransaction(mBuilder.getVersions().length == 0 ? i
                            : mBuilder.getVersions()[(int) i]);
            write("<tt ");
            if (mIsTimestampUsed)
                write("timestamp=\"" + rtx.getRevisionTimestamp() + "\" ");
            write("revision=\""
                    + (mVersions.length == 0 ? i : mVersions[(int) i]) + "\">");
            serializer = new XMLSerializer(new DescendantAxis(rtx), mBuilder);
            serializer.call();
            write("</tt>");

        }

        write("</ttexport>");
        rtx.close();
    }

}

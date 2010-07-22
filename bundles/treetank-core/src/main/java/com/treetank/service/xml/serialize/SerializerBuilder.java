package com.treetank.service.xml.serialize;

import java.io.OutputStream;

import com.treetank.api.IReadTransaction;

public abstract class SerializerBuilder {

    /**
     * Intermediate {@link IReadTransaction} for builder, necessary
     */
    final IReadTransaction mIntermediateRtx;

    /**
     * Intermediate {@link OutputStream} for stream
     */
    OutputStream mIntermediateStream = System.out;

    /**
     * Intermediate boolean for intendtion, not necessary
     */
    boolean mIntermediateIntend = false;

    /**
     * Intermediate boolean for rest serialization, not necessary
     */
    boolean mIntermediateSerializeRest = false;

    /**
     * Intermediate boolean for rest serialization, not necessary
     */
    boolean mIntermediateDeclaration = true;

    /**
     * Intermediate boolean for ids, not necessary
     */
    boolean mIntermediateId = false;

    /**
     * Constructor for the builder;
     * 
     * @param rtx
     * @param stream
     */
    public SerializerBuilder(final IReadTransaction rtx) {
        mIntermediateRtx = rtx;
    }

    public void setIntermediateIntend(boolean mIntermediateIntend) {
        this.mIntermediateIntend = mIntermediateIntend;
    }

    public void setIntermediateSerializeRest(boolean mIntermediateSerializeRest) {
        this.mIntermediateSerializeRest = mIntermediateSerializeRest;
    }

    public void setIntermediateDeclaration(boolean mIntermediateDeclaration) {
        this.mIntermediateDeclaration = mIntermediateDeclaration;
    }

    public void setIntermediateId(boolean mIntermediateId) {
        this.mIntermediateId = mIntermediateId;
    }

    public void setIntermediateStream(OutputStream mIntermediateStream) {
        this.mIntermediateStream = mIntermediateStream;
    }

    public abstract AbsSerializeStorage build();

    public static class XMLSerializerBuilder extends SerializerBuilder {

        public XMLSerializerBuilder(final IReadTransaction rtx) {
            super(rtx);
        }

        @Override
        public XMLSerializer build() {
            return new XMLSerializer(this);
        }

    }

    public static class StAXSerializerBuilder extends SerializerBuilder {

        public StAXSerializerBuilder(IReadTransaction rtx) {
            super(rtx);
        }

        @Override
        public StAXSerializer build() {
            return new StAXSerializer(this);
        }
    }

}

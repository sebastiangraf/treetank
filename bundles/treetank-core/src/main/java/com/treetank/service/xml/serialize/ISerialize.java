package com.treetank.service.xml.serialize;

import java.io.IOException;

/**
 * Interface every serializer has to implement.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public interface ISerialize {
    /**
     * Main serialization algorithm.
     */
    void serialize() throws Exception;

    /**
     * Emit node.
     * 
     * @throws IOException
     */
    void emitNode() throws IOException;

    /**
     * Emit end element.
     * 
     * @throws IOException
     */
    void emitEndElement() throws IOException;
}
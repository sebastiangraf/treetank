package com.treetank.service.xml.shredder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.treetank.access.Database;
import com.treetank.access.Session;
import com.treetank.access.WriteTransaction;
import com.treetank.api.IDatabase;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.utils.LogWrapper;

/**
 * <h1>XMLImport</h1>
 * 
 * <p>
 * Import of temporal data, which is either available as exactly one file which includes several revisions or
 * many files, whereas one file represents exactly one revision. Beforehand one or more {@link RevNode}s have
 * to be instanciated.
 * </p>
 * 
 * <p>
 * Usage example:
 * 
 * <code><pre>
 * final File file = new File("database.xml");
 * new XMLImport(file).check(new RevNode(new QName("timestamp")));
 * </pre></code>
 * 
 * <code><pre>
 * final List<File> list = new ArrayList<File>();
 * list.add("rev1.xml");
 * list.add("rev2.xml");
 * ...
 * new XMLImport(file).check(new RevNode(new QName("timestamp")));
 * </pre></code>
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class XMLImport implements IImport, Callable<Void> {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(XMLImport.class);

    /** {@link Session}. */
    private transient ISession session;

    /** {@link WriteTransaction}. */
    private transient IWriteTransaction wtx;

    /** Path to Treetank storage. */
    private transient File mTT;

    /** Log helper. */
    private transient LogWrapper log;

    /** Revision nodes {@link RevNode}. */
    private transient List<RevNode> nodes;

    /** File to shredder. */
    private transient File xml;

    /**
     * Constructor.
     * 
     * @param tt
     *            Treetank file.
     */
    public XMLImport(final File tt) {
        try {
            mTT = tt;
            log = new LogWrapper(LOGGER);
            nodes = new ArrayList<RevNode>();
            final IDatabase database = Database.openDatabase(mTT);
            session = database.getSession();
        } catch (final TreetankException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void check(final Object database, final Object obj) {
        try {
            // Setup executor service.
            final ExecutorService execService =
                Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

            if (database instanceof File) {
                // Single file.
                xml = (File)database;
                if (obj instanceof RevNode) {
                    nodes.add((RevNode)obj);
                } else if (obj instanceof List<?>) {
                    nodes = (List<RevNode>)obj;
                }
                execService.submit(this);
            } else if (database instanceof List<?>) {
                // List of files.
                final List<?> files = (List<?>)database;
                if (obj instanceof RevNode) {
                    nodes.add((RevNode)obj);
                    for (final File xmlFile : files.toArray(new File[files.size()])) {
                        xml = xmlFile;
                        execService.submit(this);
                    }
                } else if (obj instanceof List<?>) {
                    nodes = (List<RevNode>)obj;
                    for (final File xmlFile : files.toArray(new File[files.size()])) {
                        xml = xmlFile;
                        execService.submit(this);
                    }
                }
            }

            // Shutdown executor service.
            execService.shutdown();
            execService.awaitTermination(10, TimeUnit.MINUTES);
        } catch (final InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            try {
                wtx.close();
                session.close();
                Database.forceCloseDatabase(mTT);
            } catch (final TreetankException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public Void call() throws Exception {
        // Setup StAX parser.
        final XMLEventReader reader = XMLShredder.createReader(xml);
        XMLEvent event = reader.nextEvent();
        final IWriteTransaction wtx = session.beginWriteTransaction();

        // Parse file.
        boolean first = true;
        do {
            log.debug(event.toString());

            if (XMLStreamConstants.START_ELEMENT == event.getEventType()
            && checkTimestampNodes((StartElement)event, nodes.toArray(new RevNode[nodes.size()]))) {
                // Found revision node.
                wtx.moveToDocumentRoot();

                if (first) {
                    first = false;

                    // Initial shredding.
                    new XMLShredder(wtx, reader, true).call();
                } else {
                    // Subsequent shredding.
                    new XMLUpdateShredder(wtx, reader, true).call();
                }
            }

            reader.nextEvent();
        } while(reader.hasNext());
        return null;
    }

    /**
     * Check if current start element matches one of the timestamp/revision
     * nodes.
     * 
     * @param event
     *            Current parsed start element.
     * @param tsns
     *            Timestamp nodes.
     * @return true if they match, otherwise false.
     */
    private boolean checkTimestampNodes(final StartElement event, final RevNode... tsns) {
        boolean retVal = false;

        for (final RevNode tsn : tsns) {
            tsn.toString();
            // TODO
        }

        return retVal;
    }

    /**
     * <h1>RevNode</h1>
     * 
     * <p>
     * Container which holds the full qualified name of a "timestamp" node.
     * </p>
     * 
     * @author Johannes Lichtenberger, University of Konstanz
     * 
     */
    final static class RevNode {
        /** QName of the node, which has the timestamp attribute. */
        private transient final QName mQName;

        /** Attribute which specifies the timestamp value. */
        private transient final Attribute mAttribute;

        /**
         * Constructor.
         * 
         * @param qName
         *            Full qualified name of the timestamp node.
         */
        public RevNode(final QName qName) {
            this(qName, null);
        }

        /**
         * Constructor.
         * 
         * @param qName
         *            Full qualified name of the timestamp node.
         * @param att
         *            Attribute which specifies the timestamp value.
         */
        public RevNode(final QName qName, final Attribute att) {
            mQName = qName;
            mAttribute = att;
        }

        /**
         * Get mQName.
         * 
         * @return the full qualified name.
         */
        public QName getQName() {
            return mQName;
        }

        /**
         * Get attribute.
         * 
         * @return the attribute.
         */
        public Attribute getAttribute() {
            return mAttribute;
        }
    }
}

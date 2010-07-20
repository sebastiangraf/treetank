package com.treetank.service.xml.shredder;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.treetank.access.Database;
import com.treetank.access.Session;
import com.treetank.access.WriteTransaction;
import com.treetank.api.IDatabase;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.exception.TreetankIOException;
import com.treetank.exception.TreetankUsageException;
import com.treetank.utils.LogHelper;

/**
 * <h1>XMLImport</h1>
 * 
 * <p>
 * Import of temporal data, which is either available as exactly one file which
 * includes several revisions or many files, whereas one file represents exactly
 * one revision. Beforehand one or more {@link RevNode}s have to be
 * instanciated.
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
public final class XMLImport extends AbsXMLImport {

    /** Logger. */
    private static final Log LOGGER = LogFactory.getLog(XMLImport.class);

    /** {@link Session}. */
    private static ISession session;

    /** {@link WriteTransaction}. */
    private static IWriteTransaction wtx;

    /** Path to Treetank storage. */
    private static File mTT;

    /**
     * Constructor
     * 
     * 
     * @param tt
     *            Treetank file.
     */
    public XMLImport(final File tt) {
        try {
            setupTT(tt);
        } catch (final TreetankException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void check(final Object database, final Object obj) {
        try {
            final File xml = (File) database;
            final LogHelper log = new LogHelper(LOGGER);

            if (obj instanceof RevNode) {
                final RevNode tsn = (RevNode) obj;
                process(xml, log, tsn);
            } else if (obj instanceof List<?>) {
                final List<RevNode> list = (List<RevNode>) obj;
                process(xml, log, (RevNode[]) list.toArray());
            }
            // TODO: Use Java7 multi-catch feature.
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (final XMLStreamException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (final TreetankException e) {
            LOGGER.error(e.getMessage(), e);
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
    public void check(final List<File> list) {

    }

    /**
     * Setup Treetank.
     * 
     * @param tt
     *            Treetank storage.
     * @throws TreetankException
     */
    private void setupTT(final File tt) throws TreetankException {
        mTT = tt;
        final IDatabase database = Database.openDatabase(tt);
        session = database.getSession();
        wtx = session.beginWriteTransaction();
    }

    /**
     * Process timestamp node -- therefore shredder subtree as a new revision
     * into the Treetank storage.
     * 
     * @param xml
     *            XML file to shredder.
     * @param log
     *            Log helper.
     * @param tsns
     *            Timestamp/Revision nodes.
     * @throws IOException
     *             In case of any I/O operation fails.
     * @throws XMLStreamException
     *             In case of any StAX parser exception.
     * @throws TreetankUsageException
     *             In case of any TreetankUsage error.
     * @throws TreetankIOException
     *             In case of any read or write error in Treetank.
     * @throws InterruptedException
     *             In case of any interruption while running tasks.
     */
    private void process(final File xml, final LogHelper log,
            final RevNode... tsns) throws IOException, XMLStreamException,
            TreetankUsageException, TreetankIOException, InterruptedException {
        // Setup StAX parser.
        final XMLEventReader reader = XMLShredder.createReader(xml);
        XMLEvent event = reader.nextEvent();

        // Setup executor service.
        final ExecutorService execService = Executors
                .newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        // Parse file.
        boolean first = true;
        do {
            log.debug(event.toString());

            if (XMLStreamConstants.START_ELEMENT == event.getEventType()
                    && checkTimestampNodes((StartElement) event, tsns)) {
                // Found revision node.
                wtx.moveToDocumentRoot();

                if (first) {
                    first = false;

                    // Initial shredding.
                    execService.submit(new XMLShredder(wtx, reader, true));
                } else {
                    // Subsequent shredding.
                    execService
                            .submit(new XMLUpdateShredder(wtx, reader, true));
                }
            }

            reader.nextEvent();
        } while (reader.hasNext());

        execService.shutdown();
        execService.awaitTermination(10, TimeUnit.MINUTES);

        reader.close();
    }

    /**
     * Check if current start element matches one of the timestamp/revision
     * nodes.
     * 
     * @param event
     *            Current parsed start element.
     * @param tsns
     *            Timestamp nodes.
     * @return True if they match, otherwise false.
     */
    private boolean checkTimestampNodes(final StartElement event,
            final RevNode... tsns) {
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
    final class RevNode {
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

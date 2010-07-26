package com.treetank.service.xml.serialize;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.treetank.settings.ECharsForSerializing;

/**
 * <h1>XMLSerializerProperties</h1>
 * 
 * <p>
 * XMLSerializer properties.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class XMLSerializerProperties {

    // ============== Class constants. =================

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(XMLSerializerProperties.class);

    /** Properties. */
    private final ConcurrentMap<String, Object> mProps = new ConcurrentHashMap<String, Object>();

    /** Property file. */
    private static transient String mFilePath;

    /** YES maps to true. */
    private static final boolean YES = true;

    /** NO maps to false. */
    private static final boolean NO = false;

    // ============ Shredding constants. ===============

    /** Serialization parameter: yes/no. */
    static final Object[] S_INDENT = {
        "indent", NO
    };

    /** Serialize XML declaration: yes/no. */
    static final Object[] S_XMLDECL = {
        "xmldecl", YES
    };

    /** Specific serialization parameter: number of spaces to indent. */
    static final Object[] S_INDENT_SPACES = {
        "indent-spaces", 2
    };

    /** Serialize REST: yes/no. */
    static final Object[] S_REST = {
        "serialize-rest", NO
    };

    /** Serialize TT-ID: yes/no. */
    static final Object[] S_ID = {
        "serialize-id", NO
    };

    /**
     * Constructor.
     * 
     * @param filePath
     *            Path to properties file.
     */
    public XMLSerializerProperties() {
        try {
            for (final Field f : getClass().getFields()) {
                System.out.println("BLAAAA");
                final Object obj = f.get(null);
                if (!(obj instanceof Object[]))
                    continue;
                final Object[] arr = (Object[])obj;
                System.out.println(arr[0].toString());
                mProps.put(arr[0].toString(), arr[1]);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * <h2>Read properties</h2>
     * 
     * <p>
     * Read properties file into a concurrent HashMap. Format of properties file:
     * </p>
     * 
     * <ul>
     * <li>xmldecl=yes (possible values: yes/no)</li>
     * <li>indent=no (possible values: yes/no)</li>
     * <li>indent-spaces=2 (possible values: Integer)</li>
     * <li>serialize-rest=no (possible values: yes/no)</li>
     * <li>serialize-id=no (possible values: yes/no)</li>
     * </ul>
     * 
     * <p>
     * Note that currently all properties have to be set. If specific key/value pairs are specified more than
     * once the last values are preserved, so the default values are overridden by user specified values.
     * </p>
     * 
     * @return ConcurrentMap which holds property key/values.
     * @throws IOException
     *             in case of any I/O operation failed.
     */
    public ConcurrentMap<String, Object> readInProps(final String filePath) {
        mFilePath = filePath;
        if (!new File(mFilePath).exists()) {
            throw new IllegalStateException("Properties file doesn't exist!");
        }

        try {
            // Read and parse file.
            final BufferedReader buffReader = new BufferedReader(new FileReader(mFilePath));
            for (String line = buffReader.readLine(); line != null; line = buffReader.readLine()) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                final int equals = line.indexOf('=');
                if (equals < 0) {
                    LOGGER.warn("Properties file has no '=' sign in line -- parsing error!");
                }

                final String key = line.substring(0, equals).toUpperCase();
                final Object value = line.substring(equals + 1);

                mProps.put(key, value);
                buffReader.close();
            }
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return mProps;
    }

    /**
     * Writes the properties to disk.
     */
    public final synchronized void write() {
        final File file = new File(mFilePath);

        try {
            // User has already specified key/values, so cache it.
            final StringBuilder strBuilder = new StringBuilder();
            if (file.exists()) {
                final BufferedReader buffReader = new BufferedReader(new FileReader(file));

                for (String line = buffReader.readLine(); line != null; line = buffReader.readLine()) {
                    strBuilder.append(line + ECharsForSerializing.NEWLINE);
                }

                buffReader.close();
            }

            // Write map properties to file.
            final BufferedWriter buffWriter = new BufferedWriter(new FileWriter(file));
            for (final Field f : getClass().getFields()) {
                final Object obj = f.get(null);
                if (!(obj instanceof Object[]))
                    continue;
                final String key = ((Object[])obj)[0].toString();
                final Object value = ((Object[])obj)[1];
                buffWriter.write(key + " = " + value + ECharsForSerializing.NEWLINE);
            }

            // Append cached properties.
            buffWriter.write(strBuilder.toString());
            buffWriter.close();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Get properties map.
     * 
     * @return ConcurrentMap with key/value property pairs.
     */
    public ConcurrentMap<String, Object> getmProps() {
        return mProps;
    }

}

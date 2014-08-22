/*
 * (c) Copyright 2009,2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.dt.other;

import org.junit.Test;
import org.opendaylight.of.lib.dt.*;

import java.io.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Simple tests to make sure the data-type classes are serializable from
 * another package. (That is, ensure serialization works without access to
 * package private members).
 *
 * @author Simon Hunt
 */
public class SerializationTestSuite {

    private static final String DONT_DELETE_PREFIX = "DD_";
    private static final String TEMP_FILE_PREFIX = "datatype";
    private static final String TEMP_FILE_SUFFIX = ".obj";

    private static final long BIG = 0xffffffffL;
    private static final int I42 = 42;

    private static long fileSizeInBytes;


    /** Private helper method to take an object, serialize it into
     * a temporary file, and return the name of the temp file.
     *
     * @param datatype the object to be serialized
     * @return the name of the temporary file created
     * @throws java.io.IOException if there was a problem
     */
    private static String save(Object datatype) throws IOException {
        return save(datatype, keepTempFiles());
    }

    /** Private helper method to take an object, serialize it into
     * a temporary file, and return the name of the temp file.
     *
     * @param datatype the object to be serialized
     * @param dontDelete if true, the temp file is not deleted, and its '
     *                   name is written to stdout
     * @return the name of the temporary file created
     * @throws IOException if there was a problem
     */
    private static String save(Object datatype, boolean dontDelete)
            throws IOException {
        String filePrefix = dontDelete ? DONT_DELETE_PREFIX + TEMP_FILE_PREFIX
                                       : TEMP_FILE_PREFIX;
        File tempFile = File.createTempFile(filePrefix, TEMP_FILE_SUFFIX);
        FileOutputStream fos = new FileOutputStream(tempFile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(datatype);
        oos.close();
        fos.close();
        if (dontDelete)
            print("Serialized File: " + tempFile.getPath());
        return tempFile.getPath();
    }

    /** Private helper method to take the name of a temp file, and
     * deserialize the object contained within.
     *
     * @param aFileName the name of the temp file
     * @return the deserialized object
     * @throws IOException if there was a problem
     * @throws ClassNotFoundException if the deserialized class was not found
     */
    private static Object load(String aFileName)
            throws IOException, ClassNotFoundException {
        File tempFile = new File(aFileName);
        fileSizeInBytes = tempFile.length();
        FileInputStream fis = new FileInputStream(tempFile);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object datatype = ois.readObject();
        ois.close();
        fis.close();
        if (!tempFile.getName().startsWith(DONT_DELETE_PREFIX))
            tempFile.delete();
        return datatype;
    }

    /** helper method to compare the relative sizes of the serialized
     * object vs. its toString() representation. It is assumed that the
     * object passed in here was the one that was just {@link #load loaded}.
     *
     * @param o the object
     * @throws java.io.IOException if issues
     * @throws ClassNotFoundException if issues
     */
    private static void compareSizes(Object o)
            throws IOException, ClassNotFoundException {
        long serSize = fileSizeInBytes;

        // long strLen = o.toString().getBytes().length;
        // NOTE: String.getBytes() converts the string to a byte array
        // using a character encoding but strings use Unicode chars which
        // are 2 bytes per char, plus some overhead, so we serialize the
        // string to a file and examine the file size
        String asString = o.toString();
        String tmp = save(asString);
        load(tmp);
        long stringSize = fileSizeInBytes;

        print(" Comparing sizes for " + o);
        StringBuilder sb = new StringBuilder(" SIZES: String[")
                .append(asString.length())
                .append("] = ")
                .append(stringSize)
                .append(" bytes, Ser.Object = ")
                .append(serSize)
                .append(" bytes.  Delta: ")
                .append(serSize - stringSize)
                .append(EOL);
        print(sb);
    }

    //=======================================================================


    //============================
    //=== VId
    //============================

    @Test
    public void serializedVId()
            throws IOException, ClassNotFoundException {
        print(EOL + "serializedVId()");
        final int atari = 6502;
        VId vId = VId.valueOf(atari);
        print(vId);

        String tmp = save(vId);
        VId copy = (VId) load(tmp);
        compareSizes(copy);
        print(copy);

        verifyEqual(vId, copy);
        assertEquals(AM_NSR, vId, copy);
        assertEquals(AM_NEQ, atari, copy.toInt());
    }

    //============================
    //=== DataPathId
    //============================

    @Test
    public void serializedDataPathId()
            throws IOException, ClassNotFoundException {
        print(EOL + "serializedDataPathId()");
        final String spec = "6502/c001cafebabe";
        print(spec);
        DataPathId dpid = DataPathId.valueOf(spec);
        print(dpid);

        String tmp = save(dpid);
        DataPathId copy = (DataPathId) load(tmp);
        compareSizes(copy);
        print(copy);

        verifyEqual(dpid, copy);
        assertSame(AM_NSR, dpid, copy);
        assertEquals(AM_NEQ, 6502, copy.getVid().toInt());
        assertEquals(AM_NEQ, "c0:01:ca:fe:ba:be",
                copy.getMacAddress().toString());
    }


    //============================
    //=== BufferId
    //============================

    @Test
    public void serializedBufferId()
            throws IOException, ClassNotFoundException {
        print(EOL + "serializedBufferId()");
        BufferId id = BufferId.valueOf(BIG);
        print(id);

        String tmp = save(id);
        BufferId copy = (BufferId) load(tmp);
        compareSizes(copy);
        print(copy);

        verifyEqual(id, copy);
        assertEquals(AM_NSR, id, copy);
        assertEquals(AM_NEQ, BIG, copy.toLong());
    }


    //============================
    //=== GroupId
    //============================

    @Test
    public void serializedGroupId()
            throws IOException, ClassNotFoundException {
        print(EOL + "serializedGroupId()");
        GroupId id = GroupId.valueOf(BIG);
        print(id);

        String tmp = save(id);
        GroupId copy = (GroupId) load(tmp);
        compareSizes(copy);
        print(copy);

        verifyEqual(id, copy);
        assertSame(AM_NSR, id, copy);
        assertEquals(AM_NEQ, BIG, copy.toLong());
    }


    //============================
    //=== MeterId
    //============================

    @Test
    public void serializedMeterId()
            throws IOException, ClassNotFoundException {
        print(EOL + "serializedMeterId()");
        MeterId id = MeterId.valueOf(BIG);
        print(id);

        String tmp = save(id);
        MeterId copy = (MeterId) load(tmp);
        compareSizes(copy);
        print(copy);

        verifyEqual(id, copy);
        assertSame(AM_NSR, id, copy);
        assertEquals(AM_NEQ, BIG, copy.toLong());
    }


    //============================
    //=== QueueId
    //============================

    @Test
    public void serializedQueueId()
            throws IOException, ClassNotFoundException {
        print(EOL + "serializedQueueId()");
        QueueId id = QueueId.valueOf(BIG);
        print(id);

        String tmp = save(id);
        QueueId copy = (QueueId) load(tmp);
        compareSizes(copy);
        print(copy);

        verifyEqual(id, copy);
        assertSame(AM_NSR, id, copy);
        assertEquals(AM_NEQ, BIG, copy.toLong());
    }


    //============================
    //=== TableId
    //============================

    @Test
    public void serializedTableId()
            throws IOException, ClassNotFoundException {

        print(EOL + "serializedTableId()");
        TableId id = TableId.valueOf(I42);
        print(id);

        String tmp = save(id);
        TableId copy = (TableId) load(tmp);
        compareSizes(copy);
        print(copy);

        verifyEqual(id, copy);
        assertSame(AM_NSR, id, copy);
        assertEquals(AM_NEQ, I42, copy.toInt());
    }

}

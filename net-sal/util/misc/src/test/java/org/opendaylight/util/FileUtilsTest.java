/*
 * (c) Copyright 2008 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;


import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import static org.opendaylight.util.junit.TestTools.*;
import static org.opendaylight.util.FileUtils.*;
import static org.opendaylight.util.StringUtils.EOL;
import static org.junit.Assert.assertEquals;

/**
 * A suite of tests for the file manipulation utility.
 * 
 * @author Thomas Vachuska
 */
public class FileUtilsTest {

    @Test
    public void testReplaceERE() throws Exception {
        createFileTree(new String[] { TEST_DIR });
        
        String fp = TEST_DIR + "/foo.txt";
        writeContent(new File(fp), ("hello world" + EOL + "Hello World" + EOL).getBytes(), false);
        replaceERE(fp, "Hello", "Yo");
        
        assertEquals("incorrect replacement", "hello world" + EOL + "Yo World" + EOL, read(fp));

        replaceERE(fp, "[Ww]orld", "Earth");
        assertEquals("incorrect replacement", "hello Earth" + EOL + "Yo Earth" + EOL, read(fp));
        
        scrubTestData(TEST_DIR);
    }
    
    @Test(expected=FileNotFoundException.class)
    public void testBadFileReplaceERE() throws Exception {
        replaceERE(TEST_DIR + "/foo-bar.txt", "foo", "bar");
    }
    
    @Test
    public void readWrite() throws Exception {
        createFileTree(new String[] { TEST_DIR });
        String data = "Hello World!!!";
        write(TEST_DIR + "file.txt", data);
        String read = read(TEST_DIR + "file.txt").trim();
        assertEquals("incorrect data read or written", data, read);
        scrubTestData(TEST_DIR);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getResourceListingPathStartWithSlash() throws IOException, URISyntaxException {
        FileUtils.getResourceListing(getClass(), "/foo");
    }
    
    @Test
    public void writeInputStream() throws Exception {
        String testFilePath = TEST_DIR + "writeTest";
        String testResultPath = TEST_DIR + "writeResult";
        String data = "File Stream Write Test Data";
        
        File testFile = new File(testFilePath);
        write(testFilePath, data);
        
        InputStream stream = new FileInputStream(testFile);
        File testFileTarget = new File(testResultPath);
        write(stream, testFileTarget);
        stream.close();
        
        String read = read(testResultPath).trim();
        assertEquals("incorrect data written", data, read);
        scrubTestData(TEST_DIR);
    }

// TODO - figure out how to unit test the case where the resource is in a jar file
//    @Test
//    public void getResourceListing() {
//
//    }

}

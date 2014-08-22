/*
 * (c) Copyright 2010 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import static org.opendaylight.util.junit.TestTools.TEST_DIR;
import static org.opendaylight.util.junit.TestTools.createFileTree;
import static org.opendaylight.util.junit.TestTools.scrubTestData;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Suite of tests of the ZIP utilities.
 * 
 * @author Thomas Vachuska
 */
public class ZipUtilsTest {

    @Before
    public void setUp() throws Exception {
        scrubTestData(TEST_DIR);
        createFileTree(new String[] { TEST_DIR, TEST_DIR + "goo.txt", TEST_DIR + "ignored.txt", TEST_DIR + "foo/", TEST_DIR + "foo/duh.txt",
                TEST_DIR + "foo/bar/", TEST_DIR + "foo/doh.txt" });
    }

    @After
    public void tearDown() throws Exception {
        scrubTestData(TEST_DIR);
    }

    @Test
    public void testZipTools() throws IOException {
        String zfp = TEST_DIR + "test.zip";
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zfp));
        ZipUtils.write(new File(TEST_DIR + "foo/"), zos);
        ZipUtils.write(new File(TEST_DIR + "ignored.txt"), zos);
        ZipUtils.write(new File(TEST_DIR + "goo.txt"), zos);
        zos.close();

        ZipInputStream zis = new ZipInputStream(new FileInputStream(zfp));
        try {
            for (int i = 0; i < 4; i++)
                assertNotNull("more entries expected", zis.getNextEntry());
            assertNull("no more entries expected", zis.getNextEntry());
        } finally {
            zis.close();
        }
    }
}

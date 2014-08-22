/*
 * (c) Copyright 2008 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import static org.opendaylight.util.FilePathUtils.addSeparator;
import static org.opendaylight.util.FilePathUtils.childPath;
import static org.opendaylight.util.FilePathUtils.combinePath;
import static org.opendaylight.util.FilePathUtils.drive;
import static org.opendaylight.util.FilePathUtils.extension;
import static org.opendaylight.util.FilePathUtils.filename;
import static org.opendaylight.util.FilePathUtils.fullPath;
import static org.opendaylight.util.FilePathUtils.leafName;
import static org.opendaylight.util.FilePathUtils.mountPoint;
import static org.opendaylight.util.FilePathUtils.normalizedPath;
import static org.opendaylight.util.FilePathUtils.parentPath;
import static org.opendaylight.util.FilePathUtils.prefixFilename;
import static org.opendaylight.util.FilePathUtils.relativePath;
import static org.opendaylight.util.FilePathUtils.sansExtension;
import static org.opendaylight.util.FilePathUtils.tmp;
import static org.opendaylight.util.FilePathUtils.trimSeparator;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * A suite of tests for the file path manipulation utility.
 * 
 * @author Thomas Vachuska
 */
public class FilePathUtilsTest {
    
    @Test
    public void testTmp() {
        assertNotNull("tmp should not be null", tmp());
        assertTrue("tmp should not be empty", tmp().length() > 0);
    }

    @Test
    public void testParentPath() {
        assertEquals("C:", parentPath("C:", '/'));
        assertEquals("C:", parentPath("C:/", '/'));
        assertEquals("C:/", parentPath("C:/test", '/'));
        assertEquals("C:/", parentPath("C:/test/", '/'));
        assertEquals("C:/test/foo/", parentPath("C:/test/foo/bar", '/'));
        assertEquals("C:/test/foo/", parentPath("C:/test/foo/bar/", '/'));
        assertEquals("C:/test/foo/", parentPath("C:/test/foo/foobar.txt", '/'));
        assertEquals("/dir1/dir2/", fullPath(parentPath("C:/dir1/dir2/file1.txt", '/')));
    }

    @Test
    public void testRelativePath() {
        assertEquals("/", relativePath("", "/", '/'));
        assertEquals("foo", relativePath("C:/", "C:/foo", '/'));
        assertEquals("foo/bar", relativePath("C:/", "C:/foo/bar", '/'));
        assertEquals("bar", relativePath("C:/foo/", "C:/foo/bar", '/'));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadRelativePath() {
        assertEquals("foo/bar", relativePath("D:/", "C:/foo/bar", '/'));
    }
    
    @Test
    public void testLeafName() {
        assertEquals("", leafName("", '/'));
        assertEquals("/", leafName("/", '/'));
        assertEquals("C:/", leafName("C:/", '/'));
        assertEquals("bar.txt", leafName("C:/foo/bar.txt", '/'));
        assertEquals("bardir/", leafName("C:/foo/bardir/", '/'));
    }
    
    @Test
    public void testChildPath() {
        assertEquals("", childPath("C:/", "C:/", '/'));
        assertEquals("test", childPath("C:/", "C:/test", '/'));
        assertEquals("test", childPath("C:/", "C:/test/", '/'));
        assertEquals("test", childPath("C:/", "C:/test/foobar", '/'));
        assertEquals("foobar", childPath("C:/test", "C:/test/foobar/foo/bar", '/'));
        assertEquals("foobar", childPath("C:/test/", "C:/test/foobar/foo/bar", '/'));
        assertEquals("foo", childPath("C:/test/foobar/", "C:/test/foobar/foo/bar", '/'));
        assertEquals("dud", childPath("C:/test/", "C:/test/dud/", '/'));
        assertEquals("", childPath("C:/test/dud/", "C:/test/dud/", '/'));
    }
    
    @Test
    public void testAddSeparator() {
        assertEquals("/", addSeparator("", '/'));
        assertEquals("C:/foo/", addSeparator("C:/foo", '/'));
        assertEquals("C:/foo/", addSeparator("C:/foo/", '/'));
    }
    
    @Test
    public void testTrimSeparator() {
        assertEquals("", trimSeparator("", '/'));
        assertEquals("/", trimSeparator("/", '/'));
        assertEquals("C:/foo", trimSeparator("C:/foo", '/'));
        assertEquals("C:/foo", trimSeparator("C:/foo/", '/'));
    }
    
    @Test
    public void testCombinedPath() {
        assertEquals("C:/foo/bar/test.txt", 
                     combinePath("C:/foo/bar", "test.txt", '/'));
        assertEquals("C:/foo/bar/test.txt", 
                     combinePath("C:/foo/bar/", "test.txt", '/'));
    }
    
    @Test
    public void testMountPoint() {
        assertEquals("foo", mountPoint("foo", '/'));
        assertEquals("C:", mountPoint("C:", '/'));
        assertEquals("C:/", mountPoint("C:/", '/'));
        assertEquals("C:/", mountPoint("C:/test", '/'));
        assertEquals("C:/", mountPoint("C:/test/", '/'));
        assertEquals("C:/", mountPoint("C:/test/foobar", '/'));
    }
    
    @Test
    public void testFullPath() {
        assertEquals("/foobar", fullPath("C:/foobar"));
    }

    @Test
    public void testDriveLetter() {
        assertEquals("C:", drive("C:/foobar"));
    }

    @Test
    public void testSansExtension() {
        assertEquals("c:/foobar.goof/foo", sansExtension("c:/foobar.goof/foo.bar", '/'));
        assertEquals("c:/foobar.goof/foo", sansExtension("c:/foobar.goof/foo", '/'));
        assertEquals("foobar", sansExtension("foobar", '/'));
        assertEquals("foobar", sansExtension("foobar.", '/'));
        assertEquals("", sansExtension(".foo", '/'));
    }

    @Test
    public void testExtension() {
        assertEquals("bar", extension("c:/foobar.goof/foo.bar", '/'));
        assertEquals("", extension("c:/foobar.goof/foo", '/'));
        assertEquals("oof", extension("foobar.oof", '/'));
        assertEquals("", extension("foobar", '/'));
        assertEquals("", extension("foobar.", '/'));
        assertEquals("foo", extension(".foo", '/'));
    }

    @Test
    public void testLeafExtension() {
        assertEquals("oof", extension("foobar.oof"));
        assertEquals("", extension("foobar"));
        assertEquals("", extension("foobar."));
        assertEquals("foo", extension(".foo"));
    }

    @Test
    public void testPrefixFilename() {
        assertEquals("test", prefixFilename("C:/dir/test.txt", '/'));
        assertEquals("test", prefixFilename("C:/dir/test", '/'));
        assertEquals("test", prefixFilename("test.txt", '/'));
        assertEquals("test", prefixFilename("test", '/'));
        assertEquals("test", prefixFilename("/test.txt", '/'));
        assertEquals("test", prefixFilename("/test", '/'));
        assertEquals("", prefixFilename("/dir/", '/'));
        assertEquals("", prefixFilename("/", '/'));
        assertEquals("", prefixFilename("", '/'));
    }

    @Test
    public void testFilename() {
        assertEquals("test.txt", filename("C:/dir/test.txt", '/'));
        assertEquals("test", filename("C:/dir/test", '/'));
        assertEquals("test.txt", filename("test.txt", '/'));
        assertEquals("test", filename("test", '/'));
        assertEquals("test.txt", filename("/test.txt", '/'));
        assertEquals("test", filename("/test", '/'));
        assertEquals("", filename("/dir/", '/'));
        assertEquals("", filename("/", '/'));
        assertEquals("", filename("", '/'));
    }
    
    @Test
    public void testNormalizedPath() {
        assertEquals("/test/file.txt", normalizedPath("/test/file.txt"));
        assertEquals("/test/file.txt", normalizedPath("\\test/file.txt"));
        assertEquals("/test/file.txt", normalizedPath("\\test\\file.txt"));
        assertEquals("/test/dir/", normalizedPath("/test/dir/", true));
        assertEquals("/test/dir/", normalizedPath("/test/dir", true));
        assertEquals("/test/dir/", normalizedPath("\\test\\dir", true));
    }

}

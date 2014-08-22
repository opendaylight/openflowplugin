/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.driver;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Set;

import static org.opendaylight.util.junit.TestTools.*;
import static org.junit.Assert.*;

/**
 * This JUnit test class tests the DefaultDeviceInfo class.
 *
 * @author Simon Hunt
 */
public class DefaultDeviceInfoTest extends MockDeviceDriverTestUtils {

    private DefaultDeviceType type;
    private DeviceInfo info;
    private DefaultDeviceInfo ddi;

    private static class MyDeviceType extends DefaultDeviceType {
        public MyDeviceType(String typeName) {
            super(null, typeName);
        }
    }


    @Before
    public void setUp() {
        type = new MyDeviceType(TYPE_A);
        info = new DefaultDeviceInfo(type);
        ddi = (DefaultDeviceInfo) info;
        assertTrue(AM_HUH, ddi.toDebugString().contains("<properties/>"));
    }


    @Test
    public void basic() {
        print(EOL + "basic()");
        print(ddi.toString());
        print(ddi.toDebugString());
    }

    @Test (expected = NullPointerException.class)
    public void nullDeviceType() {
        new DefaultDeviceInfo(null);
    }

    @Test
    public void twoFacetDevice() {
        type = new TwoFacetDeviceType(null);
        info = new DefaultDeviceInfo(type);

        Set<Class<? extends Facet>> fc = info.getFacetClasses();
        print(fc);
        assertEquals(AM_HUH, 2, fc.size());
        assertFalse(AM_HUH, info.getFacetClasses().contains(DeviceIdentity.class));
        assertTrue(AM_HUH, info.getFacetClasses().contains(FacetX.class));
        assertTrue(AM_HUH, info.getFacetClasses().contains(FacetY.class));

        FacetX fx = info.getFacet(FacetX.class);
        assertNotNull(AM_HUH, fx);
        assertEquals(AM_HUH, 42, fx.getXCount());
        assertEquals(AM_HUH, info, fx.getContext());
    }


    @Test
    public void typeName() {
        assertEquals(AM_HUH, TYPE_A, info.getTypeName());
    }
    
    @Test
    public void exportImport() {
        print(EOL + "exportImport()");
        DefaultDeviceInfo outddi = new DefaultDeviceInfo(new TwoFacetDeviceType(null));
        outddi.set("key1IntVal", 123);
        outddi.set("key2StrVal", "String123");
        outddi.set("node1.key3LongVal", 456L);
        outddi.set("node1.key4StrVal", "String456");
        outddi.set("node2.key5StrVal", "String789");
        
        assertEquals(AM_UXS, 5, outddi.keys().size());
        
        String outData = outddi.exportData();
        print(outData);
        
        DefaultDeviceInfo inddi = new DefaultDeviceInfo(new TwoFacetDeviceType(null));
        inddi.importData(outData);
        
        assertEquals(AM_UXS, 5, inddi.keys().size());
        
        assertEquals(AM_NEQ, 123, inddi.getInt("key1IntVal"));
        assertEquals(AM_NEQ, "String123", inddi.get("key2StrVal"));
        assertEquals(AM_NEQ, 456L, inddi.getLong("node1.key3LongVal"));
        assertEquals(AM_NEQ, "String456", inddi.get("node1.key4StrVal"));
        assertEquals(AM_NEQ, "String789", inddi.get("node2.key5StrVal"));
    }

    @Test
    public void clear() {
        DefaultDeviceInfo outddi = new DefaultDeviceInfo(new TwoFacetDeviceType(null));
        outddi.set("key1IntVal", 123);
        outddi.set("key2StrVal", "String123");
        outddi.set("node1.key3LongVal", 456L);
        outddi.set("node1.key4StrVal", "String456");
        outddi.set("node2.key5StrVal", "String789");

        assertEquals(AM_UXS, 5, outddi.keys().size());

        outddi.clear("node2");
        
        assertEquals(AM_UXS, 4, outddi.keys().size());
        
        outddi.clear("key1IntVal");
        
        assertEquals(AM_UXS, 3, outddi.keys().size());
        
        outddi.set("node1.key3LongVal", 123456L);
        
        assertEquals(AM_UXS, 3, outddi.keys().size());
        
        outddi.clear("key2StrVal");
        outddi.clear("node1.key4StrVal");
        
        assertEquals(AM_UXS, 1, outddi.keys().size());
        assertEquals(AM_HUH, 123456L, outddi.getLong("node1.key3LongVal"));
    }

    private static final String INT_KEY = "foo.int";
    private static final int INT_VALUE = 123;
    private static final int INT_DEFAULT = 321;

    private static final String LONG_KEY = "foo.long";
    private static final long LONG_VALUE = 456L;
    private static final long LONG_DEFAULT = 654L;

    private static final String STRING_KEY = "foo.string";
    private static final String STRING_VALUE = "foobar";
    private static final String STRING_DEFAULT = "barbaz";

    private static final String BOOLEAN_KEY = "foo.boolean";
    private static final boolean BOOLEAN_VALUE = true;
    private static final boolean BOOLEAN_DEFAULT = false;

    private static final String NO_SUCH_KEY = "xyyzy";

    @Test
    public void getDefaults() {
        ddi.set(INT_KEY, INT_VALUE);
        ddi.set(LONG_KEY, LONG_VALUE);
        ddi.set(STRING_KEY, STRING_VALUE);
        ddi.set(BOOLEAN_KEY, BOOLEAN_VALUE);
        
        assertTrue("key should be present", ddi.hasProperty(STRING_KEY));
        assertFalse("key should be absent", ddi.hasProperty(NO_SUCH_KEY));

        assertEquals("incorrect default string", STRING_DEFAULT, ddi.get(NO_SUCH_KEY, STRING_DEFAULT));
        assertEquals("incorrect default int", INT_DEFAULT, ddi.getInt(NO_SUCH_KEY, INT_DEFAULT));
        assertEquals("incorrect default long",  LONG_DEFAULT, ddi.getLong(NO_SUCH_KEY, LONG_DEFAULT));
        assertEquals("incorrect default boolean", BOOLEAN_DEFAULT, ddi.getBoolean(NO_SUCH_KEY, BOOLEAN_DEFAULT));

        assertEquals("incorrect value string", STRING_VALUE, ddi.get(STRING_KEY, STRING_DEFAULT));
        assertEquals("incorrect value int", INT_VALUE, ddi.getInt(INT_KEY, INT_DEFAULT));
        assertEquals("incorrect value long", LONG_VALUE, ddi.getLong(LONG_KEY, LONG_DEFAULT));
        assertEquals("incorrect value boolean", BOOLEAN_VALUE, ddi.getBoolean(BOOLEAN_KEY, BOOLEAN_DEFAULT));

        assertNull("not null for missing key (string)", ddi.get(NO_SUCH_KEY));
        assertEquals("not 0 for missing key (int)", 0, ddi.getInt(NO_SUCH_KEY));
        assertEquals("not 0L for missing key (long)", 0L, ddi.getLong(NO_SUCH_KEY));
        
        assertFalse("not false for missing key (boolean)", ddi.getBoolean(NO_SUCH_KEY));

        assertEquals("incorrect value string", STRING_VALUE, ddi.get(STRING_KEY));
        assertEquals("incorrect value int", INT_VALUE, ddi.getInt(INT_KEY));
        assertEquals("incorrect value long", LONG_VALUE, ddi.getLong(LONG_KEY));
        assertEquals("incorrect value boolean", BOOLEAN_VALUE, ddi.getBoolean(BOOLEAN_KEY));
    }

    @Test
    public void generation() {
        assertEquals("incorrect generation", 0, ddi.getGeneration());
        ddi.incrementGeneration();
        assertEquals("incorrect generation", 1, ddi.getGeneration());
    }

    private static final String DATA_WITH_COMMAS = "this is a desc, which has commas, in it";
    private static final String KEY = "DESC";

    @Test
    public void splittingStuff() {
        print(EOL + "splittingStuff()");

        // first, demonstrate the behavior of an XML Configuration with default settings
        print(EOL + "  WITH delimiter parsing...");
        XMLConfiguration xc = new XMLConfiguration();
        xc.setRootElementName(DefaultDeviceInfo.PROPERTIES_ROOT_NAME);
        xc.setProperty(KEY, DATA_WITH_COMMAS);
        print(xcToString(xc));
        Object p = xc.getProperty(KEY);
        print(p.getClass().getName() + ": >"+p+"<");
        assertEquals("unexpected class", ArrayList.class, p.getClass());
        @SuppressWarnings({"unchecked"}) ArrayList<String> al = (ArrayList<String>) p;
        assertEquals(AM_UXS, 3, al.size());

        // now, demonstrate the behavior after disabling delimiter parsing
        print(EOL + "  WITHOUT delimiter parsing...");
        xc.clear();
        xc.setDelimiterParsingDisabled(true);
        xc.setProperty(KEY, DATA_WITH_COMMAS);
        print(xcToString(xc));
        p = xc.getProperty(KEY);
        print(p.getClass().getName() + ": >"+p+"<");
        assertEquals("unexpected class", String.class, p.getClass());
        assertEquals("returned property not the same", DATA_WITH_COMMAS, p);

        // finally, show that the backing store for DefaultDeviceInfo behaves as in the second case...
        print(EOL + "  DEFAULT DEVICE INFO...");
        ddi.set(KEY, DATA_WITH_COMMAS);
        print(ddi.exportData());
        String v = ddi.get(KEY);
        assertEquals("returned property not the same", DATA_WITH_COMMAS, v);
    }

    // helper method to write an XML config as a string
    private String xcToString(XMLConfiguration xc) {
        try {
            ByteArrayOutputStream bbo = new ByteArrayOutputStream();
            xc.save(bbo);
            return bbo.toString();
        } catch (ConfigurationException e) {
            return "Bleagh! " + e.getMessage();
        }
    }

}

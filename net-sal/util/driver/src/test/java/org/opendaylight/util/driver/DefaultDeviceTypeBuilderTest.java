/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.driver;

import static org.opendaylight.util.junit.TestTools.*;

import org.opendaylight.util.net.IpAddress;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This JUnit test class tests the DefaultDeviceTypeBuilder class.
 *
 * @author Simon Hunt
 */
public class DefaultDeviceTypeBuilderTest {

    private static class MyHandler extends DefaultDeviceHandler {
        protected MyHandler(AbstractDeviceInfo deviceInfo, IpAddress ip) {
            super(deviceInfo, ip);
        }
    }

    private static class MyLoader implements DeviceLoader {
        @Override public DeviceInfo getDeviceInfo() { return null; }
        @Override public String getUID() { return null; }
        @Override public void load() throws DeviceException { }
        @Override public void save() throws DeviceException { }
    }

    private static final String PLATFORM = "test";

    private static final String TYPE_NAME = "foo";
    private static final DeviceDriverProvider PROVIDER = new DefaultDeviceDriverProvider(PLATFORM);
    private static final DefaultDeviceType PARENT_TYPE = new MockDeviceDriverTestUtils.TwoFacetDeviceType(PROVIDER);
    private static Map<Class<? extends Facet>, Class<? extends AbstractFacet>> FACET_MAP;
    private static Set<String> OIDS;
    private static final String OID_A = ".1.2.3.4";
    private static final String OID_B = ".7.8.9.10";
    private static final String OID_C = ".13.15.17.19";

    private DefaultDeviceTypeBuilder builder;
    private DefaultDeviceType type;

    @BeforeClass
    public static void classSetUp() {
        FACET_MAP = new HashMap<Class<? extends Facet>, Class<? extends AbstractFacet>>();
        FACET_MAP.put(DeviceIdentity.class, DefaultDeviceIdentity.class);
        FACET_MAP.put(DeviceIdentityHandler.class, DefaultDeviceIdentityHandler.class);
        FACET_MAP.put(Presentation.class, DefaultPresentation.class);
        // Note: Flags facet is NOT in this map
        OIDS = new HashSet<String>(3);
        OIDS.add(OID_A);
        OIDS.add(OID_B);
        OIDS.add(OID_C);
    }

    @AfterClass
    public static void classTearDown() {
    }

    @Before
    public void setUp() {
        builder = new DefaultDeviceTypeBuilder(TYPE_NAME);
    }

    @After
    public void tearDown() {
    }

    // == TESTS GO HERE ==
    @Test
    public void basic() {
        print(EOL + "basic()");
        type = builder.build();
        print(type.toDebugString());
        assertEquals(AM_NEQ, TYPE_NAME, type.getTypeName());
    }

    @Test
    public void lineage() {
        type = builder.provider(PROVIDER).parentType(PARENT_TYPE).build();
        print(type.toDebugString());
        assertSame(AM_NSR, PROVIDER, type.getProvider());
        assertSame(AM_NSR, PARENT_TYPE, type.getParentType());
    }

    private static final String VENDOR = "ProCurve Networking";
    private static final String FAMILY = "ProCurve 5400zl";
    private static final String PRODUCT = "J8697A";
    private static final String MODEL = "5406zl";
    private static final String FW = "K.13.09";
    private static final String TYPE_DATA = "-Some-Type-Data-";
    private static final String INSTANCE_DATA = "-Some-Instance-Data-";
    private static final String DESCRIPTION = "A description of this thing";
    private static final String ORIGIN = "An Origin";

    @Test
    public void identity() {
        type = builder.vendor(VENDOR).family(FAMILY).product(PRODUCT).model(MODEL).fw(FW)
                .description(DESCRIPTION).origin(ORIGIN).build();
        print(type.toDebugString());
        assertEquals(AM_NEQ, VENDOR, type.getVendor());
        assertEquals(AM_NEQ, FAMILY, type.getFamily());
        assertEquals(AM_NEQ, PRODUCT, type.getProduct());
        assertEquals(AM_NEQ, MODEL, type.getModel());
        assertEquals(AM_NEQ, FW, type.getFw());
        assertEquals(AM_NEQ, DESCRIPTION, type.getDescription());
        assertEquals(AM_NEQ, ORIGIN, type.getOrigin());
    }

    @Test
    public void metaData() {
        type = builder.typeData(TYPE_DATA).instanceData(INSTANCE_DATA).build();
        print(type.toDebugString());
        assertEquals(AM_NEQ, TYPE_DATA, type.getTypeData());
        assertEquals(AM_NEQ, INSTANCE_DATA, type.getInstanceData());
    }

    @Test
    public void classes() {
        type = builder.handlerClass(MyHandler.class).loaderClass(MyLoader.class).build();
        print(type.toDebugString());
        assertEquals(AM_NEQ, MyHandler.class, type.getHandlerClass());
        assertEquals(AM_NEQ, MyLoader.class, type.getLoaderClass());
    }

    @Test
    public void facetBindings() {
        type = builder.facetBindings(FACET_MAP).build();
        print(type.toDebugString());
        assertTrue(AM_HUH, type.isSupported(false, Presentation.class));
        assertTrue(AM_HUH, type.isSupported(false, DeviceIdentity.class));
        assertTrue(AM_HUH, type.isSupported(true, DeviceIdentityHandler.class));
    }

    @Test
    public void oids() {
        type = builder.oids(OIDS).build();
        print(type.toDebugString());
        for (String oid : OIDS) {
            assertTrue(AM_HUH, type.getOids().contains(oid));
        }
    }
}

/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.driver;

import static org.opendaylight.util.junit.TestTools.*;
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
 * This JUnit test class tests the DefaultDeviceType class.
 *
 * @author Simon Hunt
 */
public class DefaultDeviceTypeTest {

    private static final String PLATFORM = "test";

    private static Map<Class<? extends Facet>, Class<? extends AbstractFacet>> FACET_MAP;
    private static Set<String> FLAGS;
    private static Set<String> OIDS;
    private static final String OID_A = ".1.2.3.4";
    private static final String OID_B = ".7.8.9.10";
    private static final String OID_C = ".13.15.17.19";
    private static DefaultDeviceDriverProvider dddp;

    private DefaultDeviceType ddt;
    private DefaultDeviceInfo ddi;

    @BeforeClass
    public static void classSetUp() {
        dddp = new DefaultDeviceDriverProvider(PLATFORM);

        FACET_MAP = new HashMap<Class<? extends Facet>, Class<? extends AbstractFacet>>();
        FACET_MAP.put(DeviceIdentity.class, DefaultDeviceIdentity.class);
        FACET_MAP.put(DeviceIdentityHandler.class, DefaultDeviceIdentityHandler.class);
        FACET_MAP.put(Presentation.class, DefaultPresentation.class);
        // Note: Flags facet is NOT in this map

        FLAGS = new HashSet<String>();

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
        ddt = new DefaultDeviceTypeBuilder(TYPE_NAME)
                .vendor(VENDOR).family(FAMILY).product(PRODUCT).model(MODEL)
                .fw(FW).typeData(TYPE_DATA).instanceData(INSTANCE_DATA)
                .description(DESCRIPTION).origin(ORIGIN)
                .facetBindings(FACET_MAP).oids(OIDS)
                .build();
        ddi = new DefaultDeviceInfo(ddt);
    }

    @After
    public void tearDown() {
    }

    private static final String TYPE_NAME = "foo";
    private static final String VENDOR = "vendor";
    private static final String FAMILY = "family";
    private static final String PRODUCT = "product";
    private static final String MODEL = "model";
    private static final String FW = "f/w";
    private static final String TYPE_DATA = "random-TYPE-data";
    private static final String INSTANCE_DATA = "random-INSTANCE-data";
    private static final PresentationResources PRES_RES = new PresentationResources("path", "prop", "map");
    private static final String DESCRIPTION = "some description";
    private static final String ORIGIN = "some origin";


    // == TESTS GO HERE ==
    @Test
    public void simpleConstructor() {
        print(EOL + "simpleConstructor()");
        ddt = new DefaultDeviceType(dddp, TYPE_NAME);
        print(ddt);
        print(ddt.toDebugString());
        assertEquals(AM_NEQ, TYPE_NAME, ddt.getTypeName());
        assertEquals(AM_NEQ, dddp, ddt.getProvider());
    }

    @Test
    public void fullConstructor() {
        print(EOL + "fullConstructor()");
        ddt = new DefaultDeviceType(dddp, null, TYPE_NAME,
                VENDOR, FAMILY, PRODUCT, MODEL, OIDS, FW,
                TYPE_DATA, INSTANCE_DATA, PRES_RES, FACET_MAP, FLAGS,
                null, null, DESCRIPTION, ORIGIN);
        print(ddt);
        print(ddt.toDebugString());
        assertEquals(AM_NEQ, dddp, ddt.getProvider());
        assertEquals(AM_NEQ, TYPE_NAME, ddt.getTypeName());
        assertEquals(AM_NEQ, VENDOR, ddt.getVendor());
        assertEquals(AM_NEQ, FAMILY, ddt.getFamily());
        assertEquals(AM_NEQ, PRODUCT, ddt.getProduct());
        assertEquals(AM_NEQ, MODEL, ddt.getModel());
        assertEquals(AM_NEQ, FW, ddt.getFw());
        assertEquals(AM_NEQ, TYPE_DATA, ddt.getTypeData());
        assertEquals(AM_NEQ, INSTANCE_DATA, ddt.getInstanceData());
        assertEquals(AM_NEQ, DESCRIPTION, ddt.getDescription());
        assertEquals(AM_NEQ, ORIGIN, ddt.getOrigin());
    }

    private static final String PARENT_NAME = "Anakin";
    private static final String KID_ONE = "Luke";
    private static final String KID_TWO = "Leia";

    @Test
    public void iAmYourFather() {
        print(EOL + "iAmYourFather()");
        DefaultDeviceType parent = new DefaultDeviceTypeBuilder(PARENT_NAME).build();
        assertEquals(AM_UXS, 0, parent.getChildTypes().size());

        DefaultDeviceType kidOne = new DefaultDeviceTypeBuilder(KID_ONE).parentType(parent).build();
        DefaultDeviceType kidTwo = new DefaultDeviceTypeBuilder(KID_TWO).parentType(parent).build();
        // NOTE: there is no maintenance of parent-child relationship done in this class
        //       that is all handled in the DefaultDeviceDriverProvider. Thus creating
        //       a type with the stated parent does NOT update the parent's children set.
        assertEquals(AM_UXS, 0, parent.getChildTypes().size());

        // we have established the kids' parent tho
        assertSame(AM_NSR, parent, kidOne.getParentType());
        assertSame(AM_NSR, parent, kidTwo.getParentType());

        // a manual fixup of the hierarchy (which would be done by the provider)
        boolean result = parent.addChild(kidOne);
        assertTrue(AM_HUH, result); // child was added
        assertEquals(AM_UXS, 1, parent.getChildTypes().size());
        assertTrue(AM_HUH, parent.getChildTypes().contains(kidOne));

        result = parent.addChild(kidTwo);
        assertTrue(AM_HUH, result); // child was added
        assertEquals(AM_UXS, 2, parent.getChildTypes().size());
        assertTrue(AM_HUH, parent.getChildTypes().contains(kidTwo));

        // adding the same child has no effect
        result = parent.addChild(kidTwo);
        assertFalse(AM_HUH, result); // child was NOT added (already there)
        assertEquals(AM_UXS, 2, parent.getChildTypes().size());
        assertTrue(AM_HUH, parent.getChildTypes().contains(kidTwo));

        print(parent);

        // removing children is similar
        result = parent.removeChild(kidOne);
        assertTrue(AM_HUH, result);
        result = parent.removeChild(kidOne);
        assertFalse(AM_HUH, result);
        assertEquals(AM_UXS, 1, parent.getChildTypes().size());
        assertFalse(AM_HUH, parent.getChildTypes().contains(kidOne));
    }

    @Test
    public void checkFacetBindings() {
        print(EOL + "checkFacetBindings()");
        assertTrue(AM_HUH, ddt.isSupported(false, DeviceIdentity.class));
        assertTrue(AM_HUH, ddt.isSupported(true, DeviceIdentityHandler.class));
        assertTrue(AM_HUH, ddt.isSupported(false, Presentation.class));

        // check the boolean handler/not-handler parameter in reverse direction
        assertFalse(AM_HUH, ddt.isSupported(true, DeviceIdentity.class));
        assertFalse(AM_HUH, ddt.isSupported(false, DeviceIdentityHandler.class));
        assertFalse(AM_HUH, ddt.isSupported(true, Presentation.class));

        Set<Class<? extends Facet>> infoFacets = ddt.getFacetClasses(false);
        Set<Class<? extends Facet>> handlerFacets = ddt.getFacetClasses(true);
        assertEquals(AM_UXS, 2, infoFacets.size());
        assertEquals(AM_UXS, 1, handlerFacets.size());

        Set<String> infoFacetNames = ddt.getFacetClassNames(false);
        Set<String> handlerFacetNames = ddt.getFacetClassNames(true);
        print(infoFacetNames);
        print(handlerFacetNames);
        assertEquals(AM_UXS, 2, infoFacetNames.size());
        assertEquals(AM_UXS, 1, handlerFacetNames.size());

        // add a new binding:
        assertEquals(AM_UXS, 2, ddt.getFacetClasses(false).size());
        assertFalse(AM_HUH, ddt.isSupported(false, Flags.class));
        ddt.addBinding(Flags.class, DefaultFlags.class);
        assertTrue(AM_HUH, ddt.isSupported(false, Flags.class));
        assertEquals(AM_UXS, 3, ddt.getFacetClasses(false).size());

        // remove an existing binding
        ddt.removeBinding(Flags.class);
        assertEquals(AM_UXS, 2, ddt.getFacetClasses(false).size());
        assertFalse(AM_HUH, ddt.isSupported(false, Flags.class));
        assertFalse(AM_HUH, ddt.getFacetClasses(false).contains(Flags.class));
    }

    @Test
    public void createSomeFacets() {
        print(EOL + "createSomeFacets()");
        Facet f;
        f = ddt.getFacet(false, ddi, DeviceIdentity.class);
        assertNotNull(AM_HUH, f);

        f = ddt.getFacet(false, ddi, Presentation.class);
        assertNotNull(AM_HUH, f);

        f = ddt.getFacet(true, ddi, DeviceIdentityHandler.class);
        assertNotNull(AM_HUH, f);

        f = ddt.getFacet(false, ddi, Flags.class);
        assertNull(AM_HUH, f);

    }
}

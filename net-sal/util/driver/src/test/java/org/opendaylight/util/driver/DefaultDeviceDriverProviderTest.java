/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.driver;

import static org.opendaylight.util.junit.TestTools.AM_HUH;
import static org.opendaylight.util.junit.TestTools.AM_NEQ;
import static org.opendaylight.util.junit.TestTools.AM_NOEX;
import static org.opendaylight.util.junit.TestTools.AM_NSR;
import static org.opendaylight.util.junit.TestTools.AM_UXS;
import static org.opendaylight.util.junit.TestTools.EOL;
import static org.opendaylight.util.junit.TestTools.print;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Set;

import org.opendaylight.util.net.IpAddress;

import org.junit.Before;
import org.junit.Test;

/**
 * Suite of tests for the default device driver functionality.
 *
 * @author Simon Hunt
 * @author Shaila Shree
 * @author Thomas Vachuska
 */
public class DefaultDeviceDriverProviderTest {
    
    private static final String PLATFORM = "test";

    private DefaultDeviceDriverProvider ddp;
    private DefaultDeviceType ddt;
    private DefaultDeviceType ddt2;
    private DefaultDeviceInfo ddi;
    private DeviceHandler dh;
    Class<? extends AbstractFacet> facetImplClass;

    @Before
    public void setUp() throws Exception {
        ddp = new DefaultDeviceDriverProvider(PLATFORM);
        assertEquals("no types should be registered", 0, ddp.getBindingsCount());
        assertEquals("incorrect platform", PLATFORM, ddp.getPlatform());
    }

    private static final String WRAPPER_DESC = "Test driver definitions";
    private static final String WRAPPER_ORIGIN = "Hewlett-Packard";
    private static final String BDD_DESC = "Base device driver containing facets that all devices should support";
    private static final String BDD_NAME = "BaseDeviceDriver";
    private static final String BDD_ALIAS = "BaseDD";
    private static final String BS_NAME = "BaseSwitch";
    private static final String T_5406ZL = "5406zl";
    private static final IpAddress IP = IpAddress.valueOf("127.0.0.1");



    private static final Class<?>[] FACETS_DEV_IDENT = {
         DeviceIdentity.class, DefaultDeviceIdentity.class,
         DeviceIdentityHandler.class, DefaultDeviceIdentityHandler.class,
    };

    private static final Class<?>[] FACETS_FOO = {
         DeviceDriverTestUtils.Foo.class, DeviceDriverTestUtils.DefaultFoo.class,
         DeviceDriverTestUtils.FooHandler.class, DeviceDriverTestUtils.DefaultFooHandler.class,
    };


    /** Verifies that the specified facet mappings are present in the backing type.
     *
     * @param dh a device handler (from which we fish out the device type)
     * @param classes one or more arrays containing facet classes defining the required mappings
     */
    private void verifyFacetsForType(DeviceHandler dh, Class<?>[]... classes) {
        DefaultDeviceInfo ddi = (DefaultDeviceInfo) dh.getDeviceInfo();
        verifyFacetsForType(ddi, classes);
    }

    /** Verifies that the specified facet mappings are present in the backing type.
     *
     * @param ddi a device info (from which we fish out the device type)
     * @param classes one or more arrays containing facet classes defining the required mappings
     */
    private void verifyFacetsForType(DefaultDeviceInfo ddi, Class<?>[]... classes) {
        DefaultDeviceType t = ddp.getTypeForName(ddi.getTypeName());
        assertNotNull("type not bound", t);
        assertFalse("backing type for info not concrete", ddp.isAbstractType(t));

        verifyFacetsForType(t, classes);
    }

    /** Verifies that the specified facet mappings are present in the backing type. The arrays passed in as
     * the classes parameter should contain an even number of elements, where the classes at the even indices
     * are the facet interface classes and the classes at the odd indices are the corresponding
     * facet implementation classes.
     *
     * @param t the device type to examine
     * @param classes one or more arrays containing facet classes defining the required mappings
     */
    @SuppressWarnings({"unchecked"})
    private void verifyFacetsForType(DefaultDeviceType t, Class<?>[]... classes) {
        int infoFacetCount = 0;
        int handlerFacetCount = 0;

        print("Verifying facets for " + t.toShortDebugString());
        for (Class<?>[] array: classes) {
            assertTrue("odd array size", array.length % 2 == 0);
            for (int i=0; i<array.length; i+=2) {
                boolean wasHandler = 
                    verifyFacetMapping(t, (Class<? extends Facet>) array[i], 
                                       (Class<? extends AbstractFacet>) array[i+1]);
                if (wasHandler) {
                    handlerFacetCount++;
                } else {
                    infoFacetCount++;
                }
            }
        }

        assertEquals(AM_UXS, infoFacetCount, t.getFacetBindings(false).size());
        assertEquals(AM_UXS, handlerFacetCount, t.getFacetBindings(true).size());
    }

    /** helper method to ensure that the given mapping exists
     *
     * @param t the type that should contain the facet binding
     * @param facetClass the expected facet interface class
     * @param facetImplClass the expected facet implementation class
     * @return true if the facetClass extends HandlerFacet
     */
    private boolean verifyFacetMapping(DefaultDeviceType t,
                                       Class<? extends Facet> facetClass,
                                       Class<? extends AbstractFacet> facetImplClass) {

        boolean isHandler = HandlerFacet.class.isAssignableFrom(facetClass);

        Class<? extends AbstractFacet> boundClass = t.getFacetBindings(isHandler).get(facetClass);
        print("    " + facetClass + " -> " + boundClass);
        assertNotNull(AM_HUH, boundClass);
        assertEquals("mismatched facet implementation class", facetImplClass, boundClass);
        return isHandler;
    }

    /** Verifies that the handler and loader implementation classes specified on the type are correct.
     *
     * @param t the device type to examine
     * @param handlerClass the expected handler class
     * @param loaderClass the expected loader class
     */
    private void verifyHandlerAndLoader(DefaultDeviceType t,
                                        Class<? extends DeviceHandler> handlerClass,
                                        Class<? extends DeviceLoader> loaderClass) {
        assertEquals("wrong or no handler class", handlerClass, ddt.getHandlerClass());
        assertEquals("wrong or no loader class", loaderClass, ddt.getLoaderClass());
    }

    /** Verifies that the number of aliases, abstract types and concrete types in the provider are as expected.
     *
     * @param aliases the expected number of alias mappings
     * @param abstractTypes the expected number of abstract types
     * @param concreteTypes the expected number of concrete types
     */
    private void verifyProviderCounts(int aliases, int abstractTypes, int concreteTypes) {
        assertEquals(AM_UXS, aliases, ddp.getAliases().size());
        assertEquals(AM_UXS, abstractTypes, ddp.getAbstractTypes().size());
        assertEquals(AM_UXS, concreteTypes, ddp.getTypesCount());
    }

    @Test
    public void installAbstractTypes() {
        print(EOL + "installAbstractTypes()");
        DeviceDriverTestUtils.installXmlDrivers(ddp, "DDDPT-AbstractTest.xml");
        print(ddp);
        verifyProviderCounts(1, 2, 0);

        ddt = ddp.getTypeForName(BDD_NAME);
        assertTrue("type not abstract", ddp.isAbstractType(ddt));
        print(ddt.toDebugString());
        
        ddt2 = ddp.getTypeForName(BDD_ALIAS);
        assertTrue("type not abstract", ddp.isAbstractType(ddt));
        print(ddt2.toShortDebugString());
        assertSame(AM_NSR, ddt, ddt2);

        print(ddt.getDescription());
        print(ddt.getOrigin());
        assertEquals(AM_NEQ, WRAPPER_ORIGIN, ddt.getOrigin());
        assertTrue(AM_HUH, ddt.getDescription().contains(WRAPPER_DESC));
        assertTrue(AM_HUH, ddt.getDescription().contains(BDD_DESC));

        verifyFacetsForType(ddt, FACETS_DEV_IDENT);

        // ===== check the derived abstract type
        print(EOL + "verifying base switch abstract type...");
        ddt = ddp.getTypeForName(BS_NAME);
        assertTrue("type not abstract", ddp.isAbstractType(ddt));
        print(ddt.toDebugString());
        verifyFacetsForType(ddt, FACETS_DEV_IDENT, FACETS_FOO);

        // check that we inherited the handler and loader classes
        verifyHandlerAndLoader(ddt, DeviceDriverTestUtils.MyHandler.class, DeviceDriverTestUtils.MyLoader.class);
    }

    @Test
    public void installConcreteTypesOnly() {
        print(EOL + "installConcreteTypesOnly()");
        try {
            DeviceDriverTestUtils.installXmlDrivers(ddp, "DDDPT-ConcreteTest.xml");
            fail(AM_NOEX);
        } catch (DeviceException e) {
            print(e);
            assertTrue(AM_HUH, e.getMessage().contains(DefaultDeviceDriverProvider.EMSG_EXTENDED_TYPE_NOT_BOUND));
        }
        print(ddp);
        verifyProviderCounts(0, 0, 0); // the type is rejected so nothing is added to the provider
    }

    @Test
    public void installAbstractAndConcreteTypes() {
        print(EOL + "installAbstractAndConcreteTypes()");
        DeviceDriverTestUtils.installXmlDrivers(ddp, "DDDPT-AbstractTest.xml");
        DeviceDriverTestUtils.installXmlDrivers(ddp, "DDDPT-ConcreteTest.xml");
        print(ddp);
        verifyProviderCounts(1, 2, 1);

        ddi = (DefaultDeviceInfo) ddp.create(T_5406ZL);
        print(ddi.toDebugString());
        verifyFacetsForType(ddi, FACETS_DEV_IDENT, FACETS_FOO);


        ddt = ddi.getType();
        print(EOL + ddt.toDebugString());

        // check that we inherited the handler and loader classes
        verifyHandlerAndLoader(ddt, DeviceDriverTestUtils.MyHandler.class, DeviceDriverTestUtils.MyLoader.class);

        // and look -- we can create a device handler
        dh = ddp.create(T_5406ZL, IP);
        verifyFacetsForType(dh, FACETS_DEV_IDENT, FACETS_FOO);
        assertEquals(AM_NEQ, ddt, ((DefaultDeviceInfo)dh.getDeviceInfo()).getType());
        assertEquals(AM_NEQ, IP, dh.getIpAddress());

        // access the device info data via the device identity facet
        DeviceIdentity ident = ddi.getFacet(DeviceIdentity.class);
        print(EOL + ((DefaultDeviceIdentity) ident).toDebugString());
        verify5406TypeData(ident);

        // let's get the facet from the handler (which extends the info facet)
        DeviceIdentityHandler identH = dh.getFacet(DeviceIdentityHandler.class);
        verify5406TypeData(identH);

        // check that the oids are there
        Set<String> oids = ddt.getOids();
        assertTrue("missing oid", oids.contains(OID_5406));
        assertTrue("missing oid", oids.contains(OID_5406_A));
        assertTrue("missing oid", oids.contains(OID_5406_B));

        // now see if we can look up the type by the oid
        verifyTypeByOid(ddt, OID_5406);
        verifyTypeByOid(ddt, OID_5406_A);
        verifyTypeByOid(ddt, OID_5406_B);

        // finally, check the number of bindings and types:
        assertEquals(AM_UXS, 4, ddp.getBindingsCount());
        assertEquals(AM_UXS, 1, ddp.getTypesCount());
    }

    private void verifyTypeByOid(DefaultDeviceType expectedType, String oid) {
        DefaultDeviceType t = ddp.getDeviceType(oid);
        assertNotNull("type not referenced by OID", t);
        print(" oid: " + oid + " -> " + t);
        assertEquals("type mismatch", expectedType, t);
    }

    private static final String OID_5406 = ".1.3.6.1.4.1.11.2.3.7.11.50";
    private static final String OID_5406_A = ".1.3.6.1.4.1.11.2.3.7.11.501";
    private static final String OID_5406_B = ".1.3.6.1.4.1.11.2.3.7.11.502";

    private void verify5406TypeData(DeviceIdentity ident) {
        assertEquals("wrong vendor", "Hewlett-Packard", ident.getVendor());
        assertEquals("wrong family", "ProCurve 5400zl", ident.getFamily());
        assertEquals("wrong product", "J8697A", ident.getProductNumber());
        assertEquals("wrong model", "5406zl", ident.getModelNumber());
    }

    @Test
    public void testClassLoader() {
        DefaultDeviceDriverProvider dddp =
                new DefaultDeviceDriverProvider(PLATFORM);
        boolean failed = true;
        try {
            dddp.setClassLoader(null);
        } catch (IllegalArgumentException e) {
            failed = false;
        }
        assertFalse("Loader allowed null", failed);

        dddp.setClassLoader(this.getClass().getClassLoader());
    }

}

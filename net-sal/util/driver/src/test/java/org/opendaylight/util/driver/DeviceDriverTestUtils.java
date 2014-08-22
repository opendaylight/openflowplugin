/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.driver;

import org.opendaylight.util.driver.*;
import org.opendaylight.util.net.IpAddress;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

/**
 * A set of useful methods and fixture classes for testing device driver
 * related code, providing fixtures for
 * DeviceType, DeviceInfo, DeviceHandler, DeviceLoader etc.
 *
 * @author Simon Hunt
 */
public final class DeviceDriverTestUtils {

    static final String WRONG_TYPE_INSTALLED = "wrong type installed in info";

    static final String TYPE_A = "typeA";
    static final String TYPE_B = "typeB";
    static final String TYPE_C = "typeC";
    static final String TYPE_D = "typeD";

    static final String TYPE_FOO = "FooType";
    static final String TYPE_BAR = "BarType";
    static final String TYPE_BAZ = "BazType";

    static final String TYPE_FRIK = "FrikType";
    static final String TYPE_FRAK = "FrakType";

    static final String VENDOR = "Vendor-Packard";
    static final String FAMILY = "Product Family";
    static final String PRODUCT = "Product Name";
    static final String MODEL = "J-Model-A";
    static final String FW = "F/W-1.00.07";

    static final String OID = ".1.2.3.4.5.6.7.8.9";
    static final IpAddress IP = IpAddress.valueOf("15.16.17.18");
    static final String UID = "UnIqUe";


    /** Creates a default device type with some hardcoded properties.
     *
     * @param provider the type's provider
     * @param typeName the type name
     * @return a device type instance
     */
    public static DefaultDeviceType createCannedType(DeviceDriverProvider provider,
                                                 String typeName) {
        return new DefaultDeviceTypeBuilder(typeName).provider(provider)
                .vendor(VENDOR).family(FAMILY).product(PRODUCT).model(MODEL).fw(FW)
                .build();
    }
    
    /** Creates a default device type with bare minimum of info.
     *
     * @param provider the type's provider
     * @param typeName the type name
     * @return a device type instance
     */
    public static DefaultDeviceType createEmptyType(DeviceDriverProvider provider,
                                                String typeName) {
        return new DefaultDeviceTypeBuilder(typeName).provider(provider)
                .build();
    }

    /** Creates a default device type with bare minimum of info, but that
     * has a parent type
     *
     * @param provider the type's provider
     * @param parent the type's parent type
     * @param typeName the type name
     * @return a device type instance
     */
    public static DefaultDeviceType createEmptyType(DeviceDriverProvider provider,
                                                DefaultDeviceType parent,
                                                String typeName) {
        return new DefaultDeviceTypeBuilder(typeName).provider(provider).parentType(parent)
                .build();
    }

    /** Creates a default device type with bare minimum of info, but that
     * has injected instance data
     *
     * @param provider the type's provider
     * @param typeName the type name
     * @param injectedInfo the instance data
     * @return a device type instance
     */
    public static DefaultDeviceType createEmptyType(DeviceDriverProvider provider,
                                                String typeName,
                                                String injectedInfo) {
        return new DefaultDeviceTypeBuilder(typeName).provider(provider).instanceData(injectedInfo)
                .build();
    }

    /** Creates a default device type with bare minimum of info, but that
     * has handler and loader classes specified.
     *
     * @param provider the type's provider
     * @param typeName the type name
     * @param handlerClass handler class
     * @param loaderClass loader class
     * @return a device type instance
     */
    public static DefaultDeviceType createEmptyType(DeviceDriverProvider provider,
                                                String typeName,
                                                Class<? extends DeviceHandler> handlerClass,
                                                Class<? extends DeviceLoader> loaderClass) {
        return new DefaultDeviceTypeBuilder(typeName).provider(provider)
                .handlerClass(handlerClass).loaderClass(loaderClass)
                .build();
    }

    /** Retrieves the specified XML file from the class path and installs it into the specified
     *  device driver provider.
     *
     * @param provider the device driver provider
     * @param resourceFilename the name of the XML device driver file
     */
    public static void installXmlDrivers(DefaultDeviceDriverProvider provider, String resourceFilename) {
        InputStream is  = provider.getClass().getResourceAsStream(resourceFilename);
        assertNotNull("no input stream", is);
        provider.addDriver(is);
    }


    //=========== FIXTURES ==========================================

    /** Fixture provider */
    public static class MyProvider extends AbstractDeviceDriverProvider { }

    /** Fixture device handler */
    public static class MyHandler extends DefaultDeviceHandler {
        public MyHandler(AbstractDeviceInfo deviceInfo, IpAddress ip) {
            super(deviceInfo, ip);
        }
    }

    /** Fixture device loader */
    public static class MyLoader extends AbstractDeviceLoader {
        public MyLoader(AbstractDeviceInfo deviceInfo, String uid) {
            super(deviceInfo, uid);
        }
        @Override public void load() throws DeviceException { }
        @Override public void save() throws DeviceException { }
    }

    /** Custom Facet interface */
    public static interface Foo extends Facet { }
    /** Custom FacetHandler interface */
    public static interface FooHandler extends HandlerFacet { }

    /** Custom Facet implementation */
    public static class DefaultFoo extends AbstractFacet implements Foo {
        public DefaultFoo(DeviceInfo context) { super(context); }
    }

    /** Custom FacetHandler implementation */
    public static class DefaultFooHandler extends DefaultFoo implements FooHandler {
        public DefaultFooHandler(DeviceInfo context) { super(context); }
        @Override public void fetch() { }
        @Override public void apply() { }
    }


}

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
import static org.easymock.EasyMock.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * An abstract superclass to be used as a basis for writing unit tests
 * for device driver related code, providing an easy way to create
 * DeviceType, DeviceInfo, DeviceHandler, DeviceLoader fixtures.
 * <p>
 * Note that this mocks interface implementations using EasyMock.
 *
 * @author Simon Hunt
 */
public abstract class MockDeviceDriverTestUtils {

    protected static final String WRMSG = "Wrong Exception Message";

    protected static final String TYPE_A = "typeA";
    protected static final String TYPE_B = "typeB";
    protected static final String TYPE_C = "typeC";
    protected static final String TYPE_D = "typeD";

    protected static final String UID = "UnIqUe";

    // helper method to create a set of strings
    protected Set<String> createSet(String... values) {
        Set<String> set = new HashSet<String>();
        set.addAll(Arrays.asList(values));
        return set;
    }

    // helper method to create a mock provider expecting to be registered
    protected DeviceDriverProvider createProvider(boolean replay, String... typeNames) {
        DeviceDriverProvider mock = createMock(DeviceDriverProvider.class);
        expect(mock.getDeviceTypeNames())
                .andReturn(createSet(typeNames))
                .atLeastOnce()
                ;
        if (replay)
            replay(mock);
        return mock;
    }

    protected DeviceLoader createDeviceLoader(DeviceInfo info, String uid) {
        DeviceLoader dl = createMock(DeviceLoader.class);

        // return the bound info when requested
        expect(dl.getDeviceInfo()).andReturn(info).anyTimes();
        // return the bound UID when requested
        expect(dl.getUID()).andReturn(uid).anyTimes();

        replay(dl);
        return dl;
    }
    
    protected DeviceHandler createDeviceHandler(boolean replay, DeviceInfo info, IpAddress ip) {
        DeviceHandler dh = createMock(DeviceHandler.class);

        // return the bound info when requested
        expect(dh.getDeviceInfo()).andReturn(info).anyTimes();
        // return the bound target when requested
        expect(dh.getIpAddress()).andReturn(ip).anyTimes();

        if (replay)
            replay(dh);
        return dh;
    }

    protected DeviceHandler createDeviceHandler(DeviceInfo info, IpAddress ip) {
        return createDeviceHandler(true, info, ip);
    }

    protected DeviceInfo createDeviceInfo(DeviceType dt) {
        DeviceInfo di = createMock(DeviceInfo.class);

        // return the type name when requested
        expect(di.getTypeName()).andReturn(dt.getTypeName()).anyTimes();

        replay(di);
        return di;
    }

    protected DeviceType createDeviceType(String type) {
        DeviceType dt = createMock(DeviceType.class);

        // return the type name when requested
        expect(dt.getTypeName()).andReturn(type).anyTimes();

        replay(dt);
        return dt;
    }


    // == a couple of facet interfaces

    public static interface FacetX extends Facet {
        public int getXCount();
    }

    public static interface FacetY extends Facet {
        public boolean isY();
    }



    public static class FacetXImpl extends AbstractFacet implements FacetX {

        public FacetXImpl(DeviceInfo context) { super(context); }

        @Override
        public int getXCount() { return 42; }

        @Override
        public String toString() { return "[Facet X Impl]"; }
    }

    public static class FacetYImpl extends AbstractFacet implements FacetY {

        public FacetYImpl(DeviceInfo context) { super(context); }

        @Override
        public boolean isY() { return true; }

        @Override
        public String toString() { return "[Facet Y Impl]"; }
    }

    public static class TwoFacetDeviceType extends DefaultDeviceType {

        protected TwoFacetDeviceType(DeviceDriverProvider provider) {
            super(provider, null);
            addBinding(FacetX.class, FacetXImpl.class);
            addBinding(FacetY.class, FacetYImpl.class);
        }

        public static final String NAME = "TWO-FACET";

        @Override public String getTypeName() { return NAME; }
        @Override public DeviceInfo evolve(DeviceInfo deviceInfo) { return null; }
        @Override public String getProduct() { return "MyProduct"; }
        @Override public String getModel() { return "MyModel "; }

    }
}

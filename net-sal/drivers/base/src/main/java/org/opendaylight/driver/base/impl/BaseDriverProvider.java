/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.driver.base.impl;

import org.apache.felix.scr.annotations.*;
import org.opendaylight.net.driver.DeviceDriverSuppliersBroker;
import org.opendaylight.util.driver.DefaultDeviceDriverProvider;
import org.opendaylight.util.driver.DeviceDriverBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component that submits itself to the device driver service as a set of
 * base drivers.
 *
 * @author Uyen Chau
 * @author Thomas Vachuska
 */
@Component(immediate = true)
public class BaseDriverProvider extends DefaultDeviceDriverProvider {

    private final Logger log = LoggerFactory.getLogger(BaseDriverProvider.class);

    private static final String MSG_STARTED = "BaseDriverProvider started";
    private static final String MSG_STOPPED = "BaseDriverProvider stopped";

    private static final String BASE_DEFINITIONS = "driver.xml";

    // TODO: address the best way to handle this
    private static final String SDN_CONTROLLER = "org.opendaylight";

    @Reference(name = "DeviceDriverSuppliersBroker", policy = ReferencePolicy.DYNAMIC,
               cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceDriverSuppliersBroker broker;

    /**
     * Create a driver provider with the specified platform context.
     */
    public BaseDriverProvider() {
        super(SDN_CONTROLLER);
    }

    @Activate
    public void activate() {
        // Load definitions first
        addDriver(getClass().getResourceAsStream(BASE_DEFINITIONS));

        // Then register ourselves; then, for now separately, register type names
        broker.addProvider(this);


        // FIXME: rather than coding this in here...
        // a) extend DefaultDeviceDriverProvider that will read XML with mfr/hw/fw -> typeName bindings
        // or b) modify the existing XML to carry that information, cf. OIDS
        // broker.addTypeName(mfr, hw, fw, sometype);

        log.info(MSG_STARTED);
    }

    @Deactivate
    public void deactivate() {
        // FIXME: Remove the primordial bindings

        // Unregister ourselves
        broker.removeProvider(this);
        log.info(MSG_STOPPED);
    }

}

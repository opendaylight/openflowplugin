/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.driver.impl;


import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;
import org.opendaylight.net.driver.DeviceDriverService;
import org.opendaylight.net.driver.DeviceDriverSuppliersBroker;
import org.opendaylight.util.driver.DeviceDriverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component serving as the system-wide device driver broker.
 *
 * @author Thomas Vachuska
 */
@Service
@Component(immediate = true, metatype = false)
public class DeviceDriverManager extends DeviceDriverFactory
        implements DeviceDriverService, DeviceDriverSuppliersBroker {

    private final Logger log = LoggerFactory.getLogger(DeviceDriverManager.class);

    private static final String MSG_START = "DeviceDriverManager started";
    private static final String MSG_STOP = "DeviceDriverManager stopped";

    private final DeviceTypeNameCache cache = new DeviceTypeNameCache();

    @Activate
    protected void activate() {
        log.info(MSG_START);
    }

    @Deactivate
    protected void deactivate() {
        log.info(MSG_STOP);
    }

    @Override
    public String getTypeName(String mfr, String hw, String fw) {
        return cache.getTypeName(mfr, hw, fw);
    }

    @Override
    public void addTypeName(String mfr, String hw, String fw, String typeName) {
        cache.addTypeName(mfr, hw, fw, typeName);
    }

    @Override
    public void removeTypeName(String mfr, String hw, String fw) {
        cache.removeTypeName(mfr, hw, fw);
    }
}

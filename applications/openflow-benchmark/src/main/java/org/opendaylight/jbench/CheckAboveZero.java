/*
 * Copyright (c) 2015 Intel Corp. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.jbench;

import com.beust.jcommander.IParameterValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class exposes validate method that ensures that the value passed to option is greater than zero.
 * @author Raksha Madhava Bangera
 *
 */
public class CheckAboveZero implements IParameterValidator {

    private static final Logger LOG = LoggerFactory.getLogger("Jbench");

    /* (non-Javadoc)
     * @see com.beust.jcommander.IParameterValidator#validate(java.lang.String, java.lang.String)
     */
    @Override
    public void validate(String name, String value) {
        try {
            int numValue = Integer.parseInt(value);
            if ( numValue <= 0) {
                LOG.info("{} should be greater than zero", name);
                System.exit(1);
            }
        } catch (NumberFormatException nfe) {
            LOG.error("NumberFormatException: " + nfe.getMessage());
        }
    }
}

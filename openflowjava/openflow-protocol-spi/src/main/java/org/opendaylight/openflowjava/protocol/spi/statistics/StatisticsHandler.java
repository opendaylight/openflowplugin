/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.spi.statistics;

/**
 * Used for JConsole service
 *
 * @author michal.polkorab
 */
public interface StatisticsHandler {

    /**
     * Resets all counters
     */
    public void resetCounters();

    /**
     * Prints statistics
     * @return statistics
     */
    public String printStatistics();
}
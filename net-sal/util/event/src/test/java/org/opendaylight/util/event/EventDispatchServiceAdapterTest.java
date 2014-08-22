/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.event;

import org.opendaylight.util.junit.GenericAdapterTest;

/**
 * {@link EventDispatchServiceAdapter} tests.
 */
public class EventDispatchServiceAdapterTest extends GenericAdapterTest<EventDispatchServiceAdapter> {

    @Override
    protected EventDispatchServiceAdapter instance() {
        return new EventDispatchServiceAdapter();
    }

}

/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.link;

import org.opendaylight.util.junit.GenericAdapterTest;

/**
 * Unit test for the {@link LinkServiceAdapter} API
 *
 * @author Marjorie Krueger
 */
public class LinkServiceAdapterTest extends GenericAdapterTest<LinkServiceAdapter> {
    @Override
    protected LinkServiceAdapter instance() {
        return new LinkServiceAdapter();
    }
}

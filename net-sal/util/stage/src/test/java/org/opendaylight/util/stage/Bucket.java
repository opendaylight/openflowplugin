/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.stage;

import java.util.ArrayList;
import java.util.List;

/**
 * Test fixture outlet for capturing results or discards
 * 
 * @param <T> type of items accepted in the bucket
 */
public class Bucket<T> implements Outlet<T> {

    public List<T> items = new ArrayList<T>();

    @Override
    public boolean accept(T item) {
        return items.add(item);
    }

}

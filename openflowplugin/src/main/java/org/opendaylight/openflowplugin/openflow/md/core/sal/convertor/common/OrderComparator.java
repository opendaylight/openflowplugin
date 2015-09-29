/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common;

import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.Ordered;
import java.util.Comparator;

/**
 * Comparator for comparing objects which extend Ordered.
 *
 * @param <T> T
 */
public class OrderComparator<T extends Ordered> implements Comparator<T> {

    @SuppressWarnings("rawtypes")
    private static final OrderComparator INSTANCE = new OrderComparator();

    @SuppressWarnings("unchecked")
    public static <T extends Ordered> OrderComparator<T> build() {
        return INSTANCE;
    }

    @Override
    public int compare(T obj1, T obj2) {
        if(obj1 ==null || obj2==null ) {
            throw new NullPointerException("Cannot compare null Actions");
        } else if (obj1.getOrder() == null) {
            throw new NullPointerException(errorMsg(obj1));
        } else if (obj2.getOrder() == null) {
            throw new NullPointerException(errorMsg(obj2));
        }
        return obj1.getOrder().compareTo(obj2.getOrder());
    }

    private String errorMsg(T obj) {
        return "The comparing model " + obj + "has getOrder() == null. An order is mandatory";
    }
}

/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.util.Comparator;

import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;

/**
 * Compare two Actions from a list
 * @author readams
 */
public class ActionComparator implements Comparator<Action> {
    protected static final ActionComparator INSTANCE = new ActionComparator();

    @Override
    public int compare(Action arg0, Action arg1) {
        return ComparisonChain.start()
            .compare(arg0.getOrder(), 
                     arg1.getOrder(), 
                     Ordering.natural().nullsLast())
            .result();
    }   

}

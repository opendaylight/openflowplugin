/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.api.path;

import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.InstanceIdentifierBuilder;

/**
 * @author msunal
 *
 */
final class Util {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    static <T extends DataObject> InstanceIdentifierBuilder<T> createIIdBuilderFor(Class<T> input) {
        return InstanceIdentifier.builder((Class) input);
    }

}

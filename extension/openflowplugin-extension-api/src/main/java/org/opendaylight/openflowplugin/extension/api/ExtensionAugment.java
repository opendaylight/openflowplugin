/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.api;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yangtools.yang.binding.Augmentation;

/**
 * Extension augmentation.
 *
 * @author msunal
 * @param <T> type of wrapped augmentation
 */
public class ExtensionAugment<T extends Augmentation<Extension>> extends AugmentTuple<Extension> {

    private final Class<? extends ExtensionKey> key;

    public ExtensionAugment(Class<T> augmentationClass, T augmentationObject, Class<? extends ExtensionKey> key) {
        super(augmentationClass, augmentationObject);
        this.key = key;
    }

    /**
     * Returns the key which represents an extension type.
     */
    public Class<? extends ExtensionKey> getKey() {
        return key;
    }
}

/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor;

import org.opendaylight.openflowplugin.extension.api.path.AugmentationPath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;

/**
 * @author msunal
 *
 */
public class CodecPreconditionException extends RuntimeException {

    private static final long serialVersionUID = 4602742888420994730L;

    public CodecPreconditionException(String msg) {
        super(msg);
    }

    public CodecPreconditionException(AugmentationPath path) {
        super("Augmentation for path " + path + " is not implemented.");
    }

    public CodecPreconditionException(Extension extension) {
        super("Extension " + extension.getClass() + " does not contain any known augmentation. " + extension);
    }
}

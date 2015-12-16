/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extensions.hpe;

import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MatchField;
import org.opendaylight.yangtools.yang.binding.DataObject;

public class HpeExtension {
    private final Class<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MatchField> matchField;
    private final HpeAbstractCodec codec;
    private final HpeAbstractConvertor<? extends DataObject> convertor;

    public HpeExtension(Class<? extends MatchField> matchField, HpeAbstractCodec codec, HpeAbstractConvertor<? extends DataObject> convertor) {
        this.matchField = matchField;
        this.codec = codec;
        this.convertor = convertor;
    }

    public Class<? extends MatchField> getMatchField() {
        return matchField;
    }

    public HpeAbstractCodec getCodec() {
        return codec;
    }

    public HpeAbstractConvertor<? extends DataObject> getConvertor() {
        return convertor;
    }
}

/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.translator;

import org.opendaylight.openflowplugin.openflow.md.core.ByteUtil;
import org.opendaylight.openflowplugin.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mirehak
 *
 */
public class ErrorTranslator implements IMDMessageTranslator<OfHeader, DataObject> {

    private static final Logger LOG = LoggerFactory
            .getLogger(ErrorTranslator.class);
    
    @Override
    public DataObject translate(SwitchConnectionDistinguisher cookie,
            SessionContext sc, OfHeader msg) {
        ErrorMessage errorMsg = (ErrorMessage) msg;
        LOG.error("errorMessage arrived: type={}, code={}, data={} | [{}]", 
                errorMsg.getType(), errorMsg.getCode(),
                new String(errorMsg.getData()), ByteUtil.bytesToHexstring(errorMsg.getData(), " "));
        //TODO:: add sal-error wrapper
        return null;
    }


}

/**
 * Copyright (c) 2014 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.translator;

import java.util.Collections;
import java.util.List;
import org.opendaylight.openflowplugin.api.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExperimenterTranslator implements IMDMessageTranslator<OfHeader, List<DataObject>> {

	private static final Logger LOG = LoggerFactory.getLogger(ExperimenterTranslator.class);

	@Override
	public List<DataObject> translate(SwitchConnectionDistinguisher cookie,
			SessionContext sc, OfHeader msg) {
		if( msg instanceof ExperimenterMessage) {
		    // TODO - implement functionality to fully support Experimenter framework
		    return null;
		}else {
			LOG.error( "Message is not of Experimenter Error Message " ) ;
			return Collections.emptyList();
		}
	}


}

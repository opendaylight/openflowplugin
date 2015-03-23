/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.device;

import java.util.concurrent.Future;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.NodeConfigService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.SetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.SetConfigOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * @author joe
 * 
 */
// TODO: implement this
public class NodeConfigServiceImpl extends CommonService implements NodeConfigService {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.NodeConfigService#setConfig(org.opendaylight
     * .yang.gen.v1.urn.opendaylight.module.config.rev141015.SetConfigInput)
     */
    @Override
    public Future<RpcResult<SetConfigOutput>> setConfig(final SetConfigInput input) {
        // TODO Auto-generated method stub
        return null;
    }

}

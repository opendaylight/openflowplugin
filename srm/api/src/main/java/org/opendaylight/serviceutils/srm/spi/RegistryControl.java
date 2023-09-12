/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.serviceutils.srm.spi;

import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.RecoverInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.RecoverOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.ReinstallInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.rpc.rev180626.ReinstallOutput;

/**
 * Control interface for ServiceRecoveryRegistry implementation.
 */
public interface RegistryControl {

    RecoverOutput recover(RecoverInput input);

    ReinstallOutput reinstall(ReinstallInput input);
}

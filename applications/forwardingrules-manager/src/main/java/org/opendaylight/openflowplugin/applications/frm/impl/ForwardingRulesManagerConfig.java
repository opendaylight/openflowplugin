/**
 * Copyright (c) 2015 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm.impl;

/**
 * Created by efiijjp on 10/14/2015.
 */
public class ForwardingRulesManagerConfig {

    private final boolean staleMarkingEnabled;

    private ForwardingRulesManagerConfig(ForwardingRulesManagerConfigBuilder builder){
        this.staleMarkingEnabled = builder.isStaleMarkingEnabled();
    }

    public boolean isStaleMarkingEnabled(){
        return staleMarkingEnabled;
    }


    public static ForwardingRulesManagerConfigBuilder builder(){
        return new ForwardingRulesManagerConfigBuilder();
    }



    public static class ForwardingRulesManagerConfigBuilder {
        private boolean staleMarkingEnabled;

        public boolean isStaleMarkingEnabled(){
            return staleMarkingEnabled;
        }

        public void setStaleMarkingEnabled(boolean staleMarkingEnabledFlag){
            staleMarkingEnabled = staleMarkingEnabledFlag;
        }

        public ForwardingRulesManagerConfig build(){
            return new ForwardingRulesManagerConfig(this);
        }
    }

}

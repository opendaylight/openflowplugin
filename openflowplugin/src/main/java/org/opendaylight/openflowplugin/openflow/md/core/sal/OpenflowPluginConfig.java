/*
 * Copyright (c) 2016 Ericsson Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal;

/**
 * Created by eshuvka on 5/16/2016.
 */
public class OpenflowPluginConfig {

    private final boolean skipTableFeatures;

    private OpenflowPluginConfig (OpenflowPluginConfigBuilder builder){
        skipTableFeatures = builder.skipTableFeatures();
    }

    public boolean skipTableFeatures(){
        return skipTableFeatures;
    }

    public static OpenflowPluginConfigBuilder builder(){
        return new OpenflowPluginConfigBuilder();
    }

    public static class OpenflowPluginConfigBuilder{
        private boolean skipTableFeatures;

        public boolean skipTableFeatures(){
            return skipTableFeatures;
        }

        public void setSkipTableFeatures(boolean skip){
            skipTableFeatures = skip;
        }

        public OpenflowPluginConfig build() {return new OpenflowPluginConfig(this);}
    }
}

package org.opendaylight.openflowplugin.api.openflow.configuration;

import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenflowConfig {
        private static final Logger LOG = LoggerFactory.getLogger(OpenflowConfig.class);
        private static OpenflowConfig instance;

        private static ConfigurationService configurationService;

        public OpenflowConfig(ConfigurationService configurationService) {
                this.configurationService = configurationService;
                LOG.info("In OpenflowConfig constructor");

        }

        public void start() {
                LOG.info("OpenflowConfig started");
/*
                instance = new OpenflowConfig(configurationService);
*/
        }

        public static synchronized OpenflowConfig getInstance() {
                return instance;
        }

        public static ConfigurationService getConfigurationService() {
                return configurationService;
        }
}

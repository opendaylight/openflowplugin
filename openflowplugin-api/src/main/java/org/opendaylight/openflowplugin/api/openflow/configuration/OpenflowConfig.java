package org.opendaylight.openflowplugin.api.openflow.configuration;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenflowConfig {
        private static final Logger LOG = LoggerFactory.getLogger(OpenflowConfig.class);
        private static volatile OpenflowConfig instance;

        private static ConfigurationService configurationService;

        public static OpenflowConfig newInstance(ConfigurationService configurationService) {
                OpenflowConfig.configurationService = configurationService;
                return instance;
        }

//        public void start() {
//                LOG.info("OpenflowConfig started");
//        }

        public static synchronized OpenflowConfig getInstance() {
                return instance;
        }

        public static ConfigurationService getConfigurationService() {
                return configurationService;
        }
}

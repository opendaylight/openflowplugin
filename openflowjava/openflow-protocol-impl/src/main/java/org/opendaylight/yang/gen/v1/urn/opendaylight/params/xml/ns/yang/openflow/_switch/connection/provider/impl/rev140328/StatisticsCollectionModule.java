package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow._switch.connection.provider.impl.rev140328;

import org.opendaylight.openflowjava.protocol.api.connection.StatisticsConfiguration;
import org.opendaylight.openflowjava.protocol.spi.statistics.StatisticsHandler;
import org.opendaylight.openflowjava.statistics.StatisticsCounters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* This is the definition of statistics collection module identity.
*/
public class StatisticsCollectionModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow._switch.connection.provider.impl.rev140328.AbstractStatisticsCollectionModule {

    private static final Logger LOG = LoggerFactory.getLogger(StatisticsCollectionModule.class);

    public StatisticsCollectionModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public StatisticsCollectionModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow._switch.connection.provider.impl.rev140328.StatisticsCollectionModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        final Statistics statistics = getStatistics();
        final StatisticsCounters statsCounter = StatisticsCounters.getInstance();
        StatisticsConfiguration statsConfig = null;
        if (statistics != null) {
            statsConfig = new StatisticsConfiguration() {

                @Override
                public boolean getStatisticsCollect() {
                    if (statistics.getStatisticsCollect() != null) {
                        return statistics.getStatisticsCollect().booleanValue();
                    }
                    return false;
                }

                @Override
                public int getLogReportDelay() {
                    if (statistics.getLogReportDelay() != null) {
                        return statistics.getLogReportDelay().intValue();
                    }
                    return 0;
                }
            };
        }
        if (statsConfig != null) {
            statsCounter.startCounting(statsConfig.getStatisticsCollect(), statsConfig.getLogReportDelay());
        } else {
            LOG.debug("Unable to start StatisticCounter - wrong configuration");
        }

        /* Internal MXBean implementation */
        final StatisticsCollectionRuntimeMXBean collectionBean = new StatisticsCollectionRuntimeMXBean() {

            @Override
            public String printOfjavaStatistics() {
                if (statsCounter != null) {
                    return statsCounter.printStatistics();
                }
                return "Statistics collection is not avaliable.";
            }
            @Override
            public String getMsgStatistics() {
                return printOfjavaStatistics();
            }
            @Override
            public String resetOfjavaStatistics() {
                statsCounter.resetCounters();
                return "Statistics have been reset";
            }
        };

        /* MXBean registration */
        final StatisticsCollectionRuntimeRegistration runtimeReg =
                getRootRuntimeBeanRegistratorWrapper().register(collectionBean);

        /* Internal StatisticsCollectionService implementation */
        final class AutoClosableStatisticsCollection implements StatisticsHandler, AutoCloseable {

            @Override
            public void close() {
                if (runtimeReg != null) {
                    try {
                        runtimeReg.close();
                    }
                    catch (Exception e) {
                        String errMsg = "Error by stoping StatisticsCollectionService.";
                        LOG.error(errMsg, e);
                        throw new IllegalStateException(errMsg, e);
                    }
                }
                LOG.info("StatisticsCollection Service consumer (instance {} turn down.)", this);
            }

            @Override
            public void resetCounters() {
                statsCounter.resetCounters();
            }

            @Override
            public String printStatistics() {
                return statsCounter.printStatistics();
            }
        }

        AutoCloseable ret = new AutoClosableStatisticsCollection();
        LOG.info("StatisticsCollection service (instance {}) initialized.", ret);
        return ret;
    }
}
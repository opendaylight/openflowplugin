package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.common.config.impl.rev140326;

import java.text.SimpleDateFormat;
import java.util.List;

import org.opendaylight.openflowplugin.openflow.md.core.sal.OpenflowPluginProvider;
import org.opendaylight.openflowplugin.api.openflow.statistics.MessageCountDumper;
import org.opendaylight.openflowplugin.api.openflow.statistics.MessageObservatory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MsgSpyServiceModul implements and register own MsgSpyServiceRuntimeMXBean
 * which is linked to {@link MessageObservatory} from {@link OpenflowPluginProvider}
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 *
 */
public class MsgSpyServiceModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.common.config.impl.rev140326.AbstractMsgSpyServiceModule {
    private static final Logger log = LoggerFactory.getLogger(MsgSpyServiceModule.class);

    private static final SimpleDateFormat ft = new SimpleDateFormat ("E yyyy.MM.dd 'at' hh:mm:ss a zzz");

    public MsgSpyServiceModule(final org.opendaylight.controller.config.api.ModuleIdentifier identifier,
            final org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {

        super(identifier, dependencyResolver);
    }

    public MsgSpyServiceModule(final org.opendaylight.controller.config.api.ModuleIdentifier identifier,
            final org.opendaylight.controller.config.api.DependencyResolver dependencyResolver,
            final MsgSpyServiceModule oldModule, final java.lang.AutoCloseable oldInstance) {

        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // No need to validate dependencies, since all dependencies are mandatory
        // config-subsystem will perform the validation
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        final MessageCountDumper msg = getOpenflowPluginProviderDependency().getMessageCountDumper();

        /* Internal MXBean implementation -> make statMsg from dumpMessageCounst only yet */
        final MsgSpyServiceRuntimeMXBean msgSpyBean = new MsgSpyServiceRuntimeMXBean() {

            @Override
            public String makeMsgStatistics() {
                if (msg == null) {
                    return "Message Spy Count Dumper is not avaliable.";
                }
                List<String> statList = msg.dumpMessageCounts();

                StringBuilder strBuilder = new StringBuilder(ft.format(System.currentTimeMillis()));
                for (String stat : statList) {
                    strBuilder.append("\n").append(stat);
                }
                return strBuilder.toString();
            }

            @Override
            public String getMsgStatistics() {
                return makeMsgStatistics();
            }
        };

        /* MXBean registration */
        final MsgSpyServiceRuntimeRegistration runtimeReg =
                getRootRuntimeBeanRegistratorWrapper().register(msgSpyBean);

        /* Internal MsgSpyService implementation */
        final class AutoClosableMsgSpyService implements MessageCountDumper, AutoCloseable {

            @Override
            public void close() {
                if (runtimeReg != null) {
                    try {
                        runtimeReg.close();
                    }
                    catch (Exception e) {
                        String errMsg = "Error by stop MsgSpyService.";
                        log.error(errMsg, e);
                        throw new IllegalStateException(errMsg, e);
                    }
                }
                log.info(" Msg Stat Service consumer (instance {} turn down.)", this);
            }

            @Override
            public List<String> dumpMessageCounts() {
                return msg.dumpMessageCounts();
            }
        }

        AutoCloseable ret = new AutoClosableMsgSpyService();
        log.info("MsgStatService (instance {}) initialized.", ret);
        return ret;
    }

}

/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.mdsal;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.MoreExecutors;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.stream.XMLStreamException;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow._switch.connection.config.rev160506.SwitchConnectionConfig;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.codec.xml.XmlParserStream;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A component responsible for populating default (factory) configuration.
 */
@Component(service = { })
public final class OSGiFactorySwitchConnectionConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(OSGiFactorySwitchConnectionConfiguration.class);
    private static final Path INITIAL_CONFIG_DIR = Path.of("etc", "opendaylight", "datastore", "initial", "config");

    @Activate
    public OSGiFactorySwitchConnectionConfiguration(@Reference final DOMDataBroker dataBroker,
            @Reference final DOMSchemaService schemaService) {
        final var inference = Inference.ofDataTreePath(schemaService.getGlobalContext(), SwitchConnectionConfig.QNAME);

        // Create OF switch connection provider on port 6653 (default)
        writeIfNotPresent(dataBroker, inference, "default-openflow-connection-config.xml");
        // Create OF switch connection provider on port 6633 (legacy)
        writeIfNotPresent(dataBroker, inference, "legacy-openflow-connection-config.xml");
    }

    private static void writeIfNotPresent(final DOMDataBroker dataBroker, final Inference inference,
            final String fileName) {
        final var path = INITIAL_CONFIG_DIR.resolve(fileName);
        final var resultHolder = new NormalizationResultHolder();
        final var writer = ImmutableNormalizedNodeStreamWriter.from(resultHolder);

        try (var xmlParser = XmlParserStream.create(writer, inference)) {
            xmlParser.parse(UntrustedXML.createXMLStreamReader(Files.newInputStream(path)));
        } catch (IOException | XMLStreamException e) {
            LOG.warn("Cannot parse {}, skipping configuration deployment", path, e);
            return;
        }

        final var result = resultHolder.getResult().data();
        if (!(result instanceof MapNode map)) {
            LOG.warn("Skipping configuration deployment of non-MapNode {}", result.prettyTree());
            return;
        }
        final var size = map.size();
        if (size != 1) {
            LOG.warn("Skipping configuration deployment of multi-entry {}", map.prettyTree());
            return;
        }
        final var entry = map.body().iterator().next();
        final var name = entry.name();
        if (!SwitchConnectionConfig.QNAME.equals(name.getNodeType())) {
            LOG.warn("Skipping configuration deployemtn of unrecognized {}", entry.prettyTree());
            return;
        }

        LOG.info("Checking presence of configuration for {}", name);
        final var sw = Stopwatch.createStarted();
        final var iid = YangInstanceIdentifier.builder().node(SwitchConnectionConfig.QNAME).node(name).build();

        final var tx = dataBroker.newReadWriteTransaction();
        tx.exists(LogicalDatastoreType.CONFIGURATION, iid).addCallback(new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(final Boolean result) {
                LOG.debug("Presence of configuration for {} ascertained in {}", name, sw);
                if (result) {
                    LOG.info("Configuration for {} already present", name);
                    tx.cancel();
                    return;
                }

                tx.put(LogicalDatastoreType.CONFIGURATION, iid, entry);
                tx.commit().addCallback(new FutureCallback<CommitInfo>() {
                    @Override
                    public void onSuccess(final CommitInfo result) {
                        LOG.info("Configuration for {} populated", name);
                    }

                    @Override
                    public void onFailure(final Throwable cause) {
                        LOG.warn("Failed to populated configuration for {}", name, cause);
                    }
                }, MoreExecutors.directExecutor());
            }

            @Override
            public void onFailure(final Throwable cause) {
                LOG.warn("Failed to ascertain presence of configuration for {} after {}", name, sw, cause);
                tx.cancel();
            }
        }, MoreExecutors.directExecutor());
    }
}

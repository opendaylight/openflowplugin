package org.opendaylight.openflowplugin.impl.util;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.openflowplugin.impl.karaf.Dpn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShellUtil {
        private static final Logger LOG = LoggerFactory.getLogger(ShellUtil.class);

        public static List<Dpn> getAllDpns(DataBroker broker) {
                List<Dpn> dpnList = new ArrayList<>();
                List<Node> nodes = new ArrayList<>();
                ReadOnlyTransaction tx = broker.newReadOnlyTransaction();
                InstanceIdentifier<Nodes> path = InstanceIdentifier.builder(Nodes.class).build();
                Optional<Nodes> result = Optional.absent();
                try {
                        CheckedFuture<Optional<Nodes>, ReadFailedException> checkedFuture =
                                tx.read(LogicalDatastoreType.OPERATIONAL, path);
                        result = checkedFuture.get();
                        if (result.isPresent()) {
                                nodes = result.get().getNode();
                        }
                } catch (Exception e) {
                        LOG.error("getAllDpns Error reading nodes from Inventory DS: {}", e);
                }
                if (nodes != null) {
                        for (Node node : nodes) {
                                try {
                                        String[] nodId = node.getId().getValue().split(":");
                                        String name = null;
                                        try {
                                                name = node.<FlowCapableNode>getAugmentation(FlowCapableNode.class).getDescription();
                                        } catch (Exception e) {
                                                LOG.error("getAllDpns Error while converting Node:{} to FlowCapableNode: {}",
                                                        node.getId(), e);
                                        }
                                        Dpn dpn = new Dpn(Long.parseLong(nodId[1]), name, null);
                                        if (dpn != null) {
                                                LOG.trace("getAllDpn. Added Dpn: {} to the list", dpn.getDpId());
                                                dpnList.add(dpn);
                                        }
                                } catch (Exception ex) {
                                        LOG.error("getAllDpns Error while getting Dpn:{} {}", node.getId(),
                                                ex);
                                }
                        }
                        Collections.sort(dpnList);
                }
                return dpnList;
        }

}

package org.opendaylight.openflowplugin.openflow.md.util;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: Kamal Rameshan (kramesha@cisco.com)
 * @since : 6/2/14
 */
public class OpenflowPortsUtilTest {
    private static Map<String, Long> mapOF10Ports;
    private static Map<String, Long> mapOF13Ports;
    private static Map<OpenflowVersion, Map<String, Long>> mapVersionToPorts;

    @BeforeClass
    public static void setupClass() {
        OpenflowPortsUtil.init();

        mapOF10Ports = new HashMap<String, Long>();
        mapOF10Ports.put(OutputPortValues.MAX.toString(), 65280L);
        mapOF10Ports.put(OutputPortValues.INPORT.toString(), 65528L);
        mapOF10Ports.put(OutputPortValues.TABLE.toString(), 65529L);
        mapOF10Ports.put(OutputPortValues.NORMAL.toString(), 65530L);
        mapOF10Ports.put(OutputPortValues.FLOOD.toString(), 65531L);
        mapOF10Ports.put(OutputPortValues.ALL.toString(), 65532L);
        mapOF10Ports.put(OutputPortValues.CONTROLLER.toString(), 65533L);
        mapOF10Ports.put(OutputPortValues.LOCAL.toString(), 65534L);
        mapOF10Ports.put(OutputPortValues.NONE.toString(), 65535L);

        mapOF13Ports = new HashMap<String, Long>();
        mapOF13Ports.put(OutputPortValues.MAX.toString(), 4294967040L);
        mapOF13Ports.put(OutputPortValues.INPORT.toString(), 4294967288L);
        mapOF13Ports.put(OutputPortValues.TABLE.toString(), 4294967289L);
        mapOF13Ports.put(OutputPortValues.NORMAL.toString(), 4294967290L);
        mapOF13Ports.put(OutputPortValues.FLOOD.toString(), 4294967291L);
        mapOF13Ports.put(OutputPortValues.ALL.toString(), 4294967292L);
        mapOF13Ports.put(OutputPortValues.CONTROLLER.toString(), 4294967293L);
        mapOF13Ports.put(OutputPortValues.LOCAL.toString(), 4294967294L);
        mapOF13Ports.put(OutputPortValues.ANY.toString(), 4294967295L);

        mapVersionToPorts = new HashMap<OpenflowVersion, Map<String, Long>>();
        mapVersionToPorts.put(OpenflowVersion.OF10, mapOF10Ports);
        mapVersionToPorts.put(OpenflowVersion.OF13, mapOF13Ports);

    }

    @AfterClass
    public static void tearDownClass() {
        OpenflowPortsUtil.close();
        mapOF10Ports.clear();
        mapOF13Ports.clear();
        mapVersionToPorts.clear();
    }

    //helper
    private void matchGetLogicalName(OpenflowVersion version, String logicalName) {
        Assert.assertEquals("Controller reserve port not matching to logical-name for "+ version,
                logicalName,
                OpenflowPortsUtil.getPortLogicalName(version, mapVersionToPorts.get(version).get(logicalName)));
    }

    //helper
    private void matchGetPortfromLogicalName(OpenflowVersion version, String logicalName) {
        Assert.assertEquals("Controller reserve port not matching to logical-name for "+ version,
                mapVersionToPorts.get(version).get(logicalName), OpenflowPortsUtil.getPortFromLogicalName(version, logicalName));
    }

    @Test
    public void testGetPortLogicalName() {

        matchGetLogicalName(OpenflowVersion.OF10, OutputPortValues.MAX.toString());
        matchGetLogicalName(OpenflowVersion.OF10, OutputPortValues.INPORT.toString());
        matchGetLogicalName(OpenflowVersion.OF10, OutputPortValues.TABLE.toString());
        matchGetLogicalName(OpenflowVersion.OF10, OutputPortValues.NORMAL.toString());
        matchGetLogicalName(OpenflowVersion.OF10, OutputPortValues.FLOOD.toString());
        matchGetLogicalName(OpenflowVersion.OF10, OutputPortValues.ALL.toString());
        matchGetLogicalName(OpenflowVersion.OF10, OutputPortValues.CONTROLLER.toString());
        matchGetLogicalName(OpenflowVersion.OF10, OutputPortValues.LOCAL.toString());
        matchGetLogicalName(OpenflowVersion.OF10, OutputPortValues.NONE.toString());

        matchGetLogicalName(OpenflowVersion.OF13, OutputPortValues.MAX.toString());
        matchGetLogicalName(OpenflowVersion.OF13, OutputPortValues.INPORT.toString());
        matchGetLogicalName(OpenflowVersion.OF13, OutputPortValues.TABLE.toString());
        matchGetLogicalName(OpenflowVersion.OF13, OutputPortValues.NORMAL.toString());
        matchGetLogicalName(OpenflowVersion.OF13, OutputPortValues.FLOOD.toString());
        matchGetLogicalName(OpenflowVersion.OF13, OutputPortValues.ALL.toString());
        matchGetLogicalName(OpenflowVersion.OF13, OutputPortValues.CONTROLLER.toString());
        matchGetLogicalName(OpenflowVersion.OF13, OutputPortValues.LOCAL.toString());
        matchGetLogicalName(OpenflowVersion.OF13, OutputPortValues.ANY.toString());

        Assert.assertNull("Invalid port number should return a null",
                OpenflowPortsUtil.getPortLogicalName(OpenflowVersion.OF10, 99999L));

        Assert.assertNull("Invalid port number should return a null",
                OpenflowPortsUtil.getPortLogicalName(OpenflowVersion.OF13, 99999L));
    }


    @Test
    public void testGetPortFromLogicalName() {

        matchGetPortfromLogicalName(OpenflowVersion.OF10, OutputPortValues.MAX.toString());
        matchGetPortfromLogicalName(OpenflowVersion.OF10, OutputPortValues.INPORT.toString());
        matchGetPortfromLogicalName(OpenflowVersion.OF10, OutputPortValues.TABLE.toString());
        matchGetPortfromLogicalName(OpenflowVersion.OF10, OutputPortValues.NORMAL.toString());
        matchGetPortfromLogicalName(OpenflowVersion.OF10, OutputPortValues.FLOOD.toString());
        matchGetPortfromLogicalName(OpenflowVersion.OF10, OutputPortValues.ALL.toString());
        matchGetPortfromLogicalName(OpenflowVersion.OF10, OutputPortValues.CONTROLLER.toString());
        matchGetPortfromLogicalName(OpenflowVersion.OF10, OutputPortValues.LOCAL.toString());
        matchGetPortfromLogicalName(OpenflowVersion.OF10, OutputPortValues.NONE.toString());

        matchGetPortfromLogicalName(OpenflowVersion.OF13, OutputPortValues.MAX.toString());
        matchGetPortfromLogicalName(OpenflowVersion.OF13, OutputPortValues.INPORT.toString());
        matchGetPortfromLogicalName(OpenflowVersion.OF13, OutputPortValues.TABLE.toString());
        matchGetPortfromLogicalName(OpenflowVersion.OF13, OutputPortValues.NORMAL.toString());
        matchGetPortfromLogicalName(OpenflowVersion.OF13, OutputPortValues.FLOOD.toString());
        matchGetPortfromLogicalName(OpenflowVersion.OF13, OutputPortValues.ALL.toString());
        matchGetPortfromLogicalName(OpenflowVersion.OF13, OutputPortValues.CONTROLLER.toString());
        matchGetPortfromLogicalName(OpenflowVersion.OF13, OutputPortValues.LOCAL.toString());
        matchGetPortfromLogicalName(OpenflowVersion.OF13, OutputPortValues.ANY.toString());

        Assert.assertNull("Invalid port logical name should return a null",
                OpenflowPortsUtil.getPortFromLogicalName(OpenflowVersion.OF10, "abc"));

        Assert.assertNull("Invalid port logical name should return a null",
                OpenflowPortsUtil.getPortFromLogicalName(OpenflowVersion.OF13, "abc"));

    }

}

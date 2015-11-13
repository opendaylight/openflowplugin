package org.opendaylight.jbench;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SdnControllerTest {

    @Test
    public void extractIpAndPortTestHostNameAndNonNullPort() {
        SdnController sdnController = new SdnController();
        String expectedIP = "localhost";
        Integer expectedPort = 6640;
        sdnController.extractIpAndPort("localhost:6640");
        assertEquals(expectedIP, sdnController.getHost());
        assertEquals(expectedPort, sdnController.getPort());
    }

    @Test
    public void extractIpAndPortTestIpAndNonNullPort() {
        SdnController sdnController = new SdnController();
        String expectedIP = "10.0.0.1";
        Integer expectedPort = 6000;
        sdnController.extractIpAndPort("10.0.0.1:6000");
        assertEquals(expectedIP, sdnController.getHost());
        assertEquals(expectedPort, sdnController.getPort());
    }

    @Test
    public void extractIpAndPortTestThreeTuples() {
        SdnController sdnController = new SdnController();
        String expectedIP = "192.168.0.1";
        Integer expectedPort = 5000;
        sdnController.extractIpAndPort("192.168.0.1:5000:RandomString");
        assertEquals(expectedIP, sdnController.getHost());
        assertEquals(expectedPort, sdnController.getPort());
    }

    @Test
    public void extractIpAndPortTestNoPort() {
        SdnController sdnController = new SdnController();
        String expectedIP = "225.172.168.53";
        Integer expectedPort = 6633;
        sdnController.extractIpAndPort("225.172.168.53:");
        assertEquals(expectedIP, sdnController.getHost());
        assertEquals(expectedPort, sdnController.getPort());
    }

    @Test
    public void extractIpAndPortTestNoColon() {
        SdnController sdnController = new SdnController();
        String expectedIP = "225.172.168.52";
        Integer expectedPort = 6633;
        sdnController.extractIpAndPort("225.172.168.52");
        assertEquals(expectedIP, sdnController.getHost());
        assertEquals(expectedPort, sdnController.getPort());
    }

    @Test
    public void extractIpAndPortTestNoIpPort() {
        SdnController sdnController = new SdnController();
        String expectedIP = "localhost";
        Integer expectedPort = 6633;
        sdnController.extractIpAndPort(":");
        assertEquals(expectedIP, sdnController.getHost());
        assertEquals(expectedPort, sdnController.getPort());
    }

    @Test
    public void extractIpAndPortTestNoIp() {
        SdnController sdnController = new SdnController();
        String expectedIP = "localhost";
        Integer expectedPort = 6640;
        sdnController.extractIpAndPort(":6640");
        assertEquals(expectedIP, sdnController.getHost());
        assertEquals(expectedPort, sdnController.getPort());
    }
}

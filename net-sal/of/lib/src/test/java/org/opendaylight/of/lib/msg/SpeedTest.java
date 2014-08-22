/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opendaylight.of.lib.MessageParseException;
import org.opendaylight.of.lib.OfPacketReader;
import org.opendaylight.util.ByteArrayGenerator;
import org.opendaylight.util.junit.BenchmarkRunner;
import org.opendaylight.util.junit.BenchmarkSubject;
import org.opendaylight.util.junit.SlowestTests;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.IpRange;
import org.opendaylight.util.net.MacAddress;
import org.opendaylight.util.net.MacRange;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static org.opendaylight.util.junit.BenchmarkRunner.num;
import static org.opendaylight.util.junit.BenchmarkRunner.numMicro;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests to measure the speed of parsing openflow messages.
 *
 * @author Simon Hunt
 * @author Thomas Vachuska
 */
@Category(SlowestTests.class)
public class SpeedTest extends OfmTest {

    // fail the test if we don't get this throughput.. 100K Msgs / Sec
    private static final double MSGS_PER_SEC_SLA = 100000.0;

    private static final String MAC_SPEC = "00:00:1e:*:*:*";
    private static final String IP_SPEC = "15.254.*.*";
    private static final MacRange MAC_R = MacRange.valueOf(MAC_SPEC);
    private static final IpRange IP_R = IpRange.valueOf(IP_SPEC);
    private static final int MAC_OFFSET = 136;
    private static final int IP_OFFSET = 152;

    private static final String XID_SPEC = "ba:be:*:*";
    private static final ByteArrayGenerator XID_R =
            ByteArrayGenerator.createFromHex(XID_SPEC);
    private static final int XID_OFFSET = 4;

    @BeforeClass
    public static void classSetUp() {
        assumeTrue(!isUnderCoverage() && !ignoreSpeedTests());
    }

    private OfPacketReader pkt;
    private OfPacketReader[] pkts;

    private BenchmarkRunner bRunner = new BenchmarkRunner();

    private BenchmarkRunner longRunner = new BenchmarkRunner() {
        @Override protected int numLoops() { return 10000000; }
    };

    /** Benchmark subject for parsing flow mod messages, where MAC and
     *  IP address are tweaked before each parsing.
     */
    private class FlowModParseBS implements BenchmarkSubject {
        @Override
        public void prepare() {
//            pkt.resetIndex();
            // Pick a new MAC address
            System.arraycopy(MAC_R.random().toByteArray(), 0,
                             pkt.array(),  MAC_OFFSET, MacAddress.MAC_ADDR_SIZE);
            // ... and a new IP address
            System.arraycopy(IP_R.random().toByteArray(), 0,
                             pkt.array(),  IP_OFFSET, IpAddress.IP_V4_ADDR_SIZE);
        }

        @Override
        public void run() throws Exception {
            MessageFactory.parseMessage(pkt);
        }
    }

    /** Benchmark subject for encoding a given openflow message. */
    private class EncodeBS implements BenchmarkSubject {
        private OpenflowMessage msg;
        private MutableMessage mmsg;

        public EncodeBS(OpenflowMessage msg) {
            this.msg = msg;
        }

        @Override
        public void prepare() {
            mmsg = MessageFactory.mutableCopy(msg);
        }

        @Override
        public void run() throws Exception {
            MessageFactory.encodeMessage(mmsg.toImmutable());
        }
    }

    private static final String FMT_MSGS_PER_SEC =
            "*** Processing at an average rate of {} msgs/sec";
    private static final String FMT_AV_MSG =
            "Average time for processing a msg: {} ms";

    // Auxiliary method to invoke a benchmark and report results.
    private BenchmarkRunner.Stats benchmark(BenchmarkRunner runner,
                                            BenchmarkSubject subject) {
        BenchmarkRunner.Stats result = runner.benchmark(subject);
        print(EOL + result.toDebugString());

        print(EOL + FMT_AV_MSG, numMicro(result.avRunDurationMs()));
        print(FMT_MSGS_PER_SEC, num(result.avRunsPerSecond()));
        return result;
    }

    @Test
    public void packetInDecode() throws Exception {
        print(EOL + "packetInDecode()");
        pkt = getPacketReader("msg/v10/packetIn" + HEX);
        benchmark(longRunner, new BenchmarkSubject() {
            @Override public void prepare() { pkt.resetIndex(); }
            @Override public void run() throws Exception {
                MessageFactory.parseMessage(pkt);
            }
        });
    }

    @Test
    public void packetOutEncode() throws Exception {
        print(EOL + "packetOutEncode()");
        pkt = getPacketReader("msg/v10/packetOut" + HEX);
        benchmark(longRunner, new BenchmarkSubject() {
            OfmPacketOut pout = (OfmPacketOut) MessageFactory.parseMessage(pkt);
            @Override public void prepare() {}
            @Override public void run() throws Exception {
                MessageFactory.encodeMessage(pout);
            }
        });
    }

    @Test
    public void packetInOutDecodeEncode() throws Exception {
        print(EOL + "packetInOutDecodeEncode()");
        pkt = getPacketReader("msg/v10/packetIn" + HEX);
        final OfPacketReader pkto = getPacketReader("msg/v10/packetOut" + HEX);
        benchmark(longRunner, new BenchmarkSubject() {
            OfmPacketOut pout = (OfmPacketOut) MessageFactory.parseMessage(pkto);
            @Override public void prepare() { pkt.resetIndex(); }
            @Override public void run() throws Exception {
                MessageFactory.parseMessage(pkt);
                MessageFactory.encodeMessage(pout);
            }
        });
    }

    @Test
    public void packetInOutDecodeEncode2() throws Exception {
        print(EOL + "packetInOutDecodeEncode2()");
        final ByteBuffer ib = ByteBuffer.wrap(slurpedBytes("msg/v10/packetIn" + HEX));
        final ByteBuffer ob = ByteBuffer.allocateDirect(1024);
        final OfPacketReader pkto = getPacketReader("msg/v10/packetOut" + HEX);
        benchmark(longRunner, new BenchmarkSubject() {
            OfmPacketOut pout = (OfmPacketOut) MessageFactory.parseMessage(pkto);
            @Override public void prepare() { ib.rewind(); ob.clear(); }
            @Override public void run() throws Exception {
                MessageFactory.parseMessage(ib);
                MessageFactory.encodeMessage(pout, ob);
            }
        });
    }

    @Test @Ignore("threshold assertions dicey")
    public void benchmarkFlowModModdedParse() {
        print(EOL + "benchmarkFlowModModdedParse()");
        pkt = getPacketReader("msg/v13/flowMod" + HEX);
        print("Bytes in the FlowMod message: {}", pkt.array().length);

        BenchmarkRunner.Stats result = benchmark(bRunner, new FlowModParseBS());
        assertAboveThreshold("parses/sec", MSGS_PER_SEC_SLA,
                             result.avRunsPerSecond());
    }

    @Test @Ignore("threshold assertions dicey")
    public void benchmarkFlowModEncode() throws MessageParseException {
        print(EOL + "benchmarkFlowEncode()");
        pkt = getPacketReader("msg/v13/flowMod" + HEX);
        print("Bytes in the FlowMod message: {}", pkt.array().length);
        EncodeBS subject = new EncodeBS(MessageFactory.parseMessage(pkt));

        BenchmarkRunner.Stats result = benchmark(bRunner, subject);
        assertAboveThreshold("encodes/sec", MSGS_PER_SEC_SLA,
                             result.avRunsPerSecond());
    }

    private static final String TEST_MSGS_ROOT = "src/test/resources/" + TEST_FILE_ROOT;
    private static final int PREFIX_LENGTH = TEST_MSGS_ROOT.length();
    private static final String TEST_MSGS = TEST_MSGS_ROOT + "msg/";

    /**
     * Scans through the given directory, locate all *.hex files and insert
     * their relative paths (stripped from .../com/hp/net/of) into the
     * supplied list.
     *
     * @param dir directory file descriptor
     * @param paths list of relative file names
     * @return the original list of paths
     */
    private static List<String> findMessageFiles(File dir, List<String> paths) {
        if (!dir.isDirectory())
            return paths;
        for (File f: dir.listFiles()) {
            if (f.isDirectory() && supported(f.getName()))
                findMessageFiles(f, paths);
            else if (isHexFile(f))
                paths.add(f.getPath().substring(PREFIX_LENGTH));
        }

        return paths;
    }

    private static boolean supported(String path) {
        return path.endsWith("v10") || path.endsWith("v13");
    }

    // NOTE: double-escaped-backslash for Windows paths, slash for linux paths
    private static final String RE_SUBDIR = ".*(\\\\|/)(v10|v13)(\\\\|/).*";

    /**
     * Predicate indicating whether or not the given file is a test hex file.
     *
     * @param f file descriptor
     * @return true if the file is a test hex file
     */
    private static boolean isHexFile(File f) {
        String name = f.getPath();
        return name.matches(RE_SUBDIR) && name.endsWith(".hex");
    }


    private BenchmarkSubject subjectCollection = new BenchmarkSubject() {
        @Override
        public void prepare() {
            for (OfPacketReader p : pkts) {
                p.resetIndex();
                // Pick a new XID
                System.arraycopy(XID_R.generate(), 0, p.array(),
                        XID_OFFSET, XID_R.size());
            }
        }

        @Override
        public void run() throws Exception {
            for (OfPacketReader p : pkts)
                MessageFactory.parseMessage(p);
        }
    };

    @Test
    public void benchmarkSelection() {
        print(EOL + "benchmarkSelection()");

        // first, get the test data into memory...
        List<String> files = findMessageFiles(new File(TEST_MSGS),
                                              new ArrayList<String>());
        final int n = files.size();
        assertTrue("No hex files found", n > 0);
        print("Found {} sample files to parse...", n);

        pkts = new OfPacketReader[n];
        int totalBytes = 0;

        // Create packet readers using the contents of each message file.
        for (int i=0; i<n; i++) {
            String file = files.get(i);
            pkts[i] = getPacketReader(file);
            int byteCount = pkts[i].array().length;
            totalBytes += byteCount;
        }
        double averageBytesPerMsg = (double) totalBytes / (double) n;
        print("Average bytes per message: {}", num(averageBytesPerMsg));

        BenchmarkRunner.Stats result = bRunner.benchmark(subjectCollection);
        print(EOL + result.toDebugString());

        // N messages per run, so for one, divide by N
        double avRunForOne = result.avRunDurationMs() / n;
        // N messages per run, so per second, multiply by N
        double perSec = result.avRunsPerSecond() * n;

        print(EOL + FMT_AV_MSG, numMicro(avRunForOne));
        print(FMT_MSGS_PER_SEC, num(perSec));
        assertAboveThreshold("parses/sec", MSGS_PER_SEC_SLA, perSec);
    }
}

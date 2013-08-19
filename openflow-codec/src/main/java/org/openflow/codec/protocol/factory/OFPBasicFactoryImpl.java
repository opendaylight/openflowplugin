package org.openflow.codec.protocol.factory;

import java.util.ArrayList;
import java.util.List;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.OFPMessage;
import org.openflow.codec.protocol.OFPType;
import org.openflow.codec.protocol.action.OFPAction;
import org.openflow.codec.protocol.action.OFPActionType;
import org.openflow.codec.protocol.instruction.OFPInstruction;
import org.openflow.codec.protocol.instruction.OFPInstructionType;
import org.openflow.codec.protocol.queue.OFPQueueProperty;
import org.openflow.codec.protocol.queue.OFPQueuePropertyType;
import org.openflow.codec.protocol.statistics.OFPExtStatistics;
import org.openflow.codec.protocol.statistics.OFPMultipartTypes;
import org.openflow.codec.protocol.statistics.OFPStatistics;

/**
 * A basic OpenFlow factory that supports naive creation of both Messages and
 * Actions.
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 * @author Rob Sherwood (rob.sherwood@stanford.edu)
 *
 */
public class OFPBasicFactoryImpl implements OFPMessageFactory, OFPActionFactory, OFPQueuePropertyFactory,
        OFPStatisticsFactory, OFPInstructionFactory {
    @Override
    public OFPMessage getMessage(OFPType t) {
        return t.newInstance();
    }

    @Override
    public List<OFPMessage> parseMessages(IDataBuffer data) {
        return parseMessages(data, 0);
    }

    @Override
    public List<OFPMessage> parseMessages(IDataBuffer data, int limit) {
        List<OFPMessage> results = new ArrayList<OFPMessage>();
        OFPMessage demux = new OFPMessage();
        OFPMessage ofm;

        while (limit == 0 || results.size() <= limit) {
            if (data.remaining() < OFPMessage.MINIMUM_LENGTH)
                return results;

            data.mark();
            demux.readFrom(data);
            data.reset();

            if (demux.getLengthU() > data.remaining())
                return results;

            ofm = getMessage(demux.getType());
            if (ofm instanceof OFPActionFactoryAware) {
                ((OFPActionFactoryAware) ofm).setActionFactory(this);
            }
            if (ofm instanceof OFPInstructionFactoryAware) {
                ((OFPInstructionFactoryAware) ofm).setInstructionFactory(this);
            }
            if (ofm instanceof OFPMessageFactoryAware) {
                ((OFPMessageFactoryAware) ofm).setMessageFactory(this);
            }
            if (ofm instanceof OFPQueuePropertyFactoryAware) {
                ((OFPQueuePropertyFactoryAware) ofm).setQueuePropertyFactory(this);
            }
            if (ofm instanceof OFPStatisticsFactoryAware) {
                ((OFPStatisticsFactoryAware) ofm).setStatisticsFactory(this);
            }
            ofm.readFrom(data);
            if (OFPMessage.class.equals(ofm.getClass())) {
                // advance the position for un-implemented messages
                data.position(data.position() + (ofm.getLengthU() - OFPMessage.MINIMUM_LENGTH));
            }
            results.add(ofm);
        }

        return results;
    }

    @Override
    public OFPAction getAction(OFPActionType t) {
        return t.newInstance();
    }

    @Override
    public List<OFPAction> parseActions(IDataBuffer data, int length) {
        return parseActions(data, length, 0);
    }

    @Override
    public List<OFPAction> parseActions(IDataBuffer data, int length, int limit) {
        List<OFPAction> results = new ArrayList<OFPAction>();
        OFPAction demux = new OFPAction();
        OFPAction ofa;
        int end = data.position() + length;

        while (limit == 0 || results.size() <= limit) {
            if (data.remaining() < OFPAction.MINIMUM_LENGTH || (data.position() + OFPAction.MINIMUM_LENGTH) > end)
                return results;

            data.mark();
            demux.readFrom(data);
            data.reset();

            if (demux.getLengthU() > data.remaining() || (data.position() + demux.getLengthU()) > end)
                return results;

            ofa = getAction(demux.getType());
            ofa.readFrom(data);
            if (OFPAction.class.equals(ofa.getClass())) {
                // advance the position for un-implemented messages
                data.position(data.position() + (ofa.getLengthU() - OFPAction.MINIMUM_LENGTH));
            }
            results.add(ofa);
        }

        return results;
    }

    @Override
    public OFPActionFactory getActionFactory() {
        return this;
    }

    @Override
    public OFPStatistics getStatistics(OFPType t, OFPMultipartTypes st) {
        return st.newInstance(t);
    }

    @Override
    public List<OFPStatistics> parseStatistics(OFPType t, OFPMultipartTypes st, IDataBuffer data, int length) {
        return parseStatistics(t, st, data, length, 0);
    }

    /**
     * @param t
     *            OFPMessage type: should be one of stats_request or stats_reply
     * @param st
     *            statistics type of this message, e.g., DESC, TABLE
     * @param data
     *            buffer to read from
     * @param length
     *            length of statistics
     * @param limit
     *            number of statistics to grab; 0 == all
     *
     * @return list of statistics
     */

    @Override
    public List<OFPStatistics> parseStatistics(OFPType t, OFPMultipartTypes st, IDataBuffer data, int length, int limit) {
        List<OFPStatistics> results = new ArrayList<OFPStatistics>();
        OFPStatistics statistics = getStatistics(t, st);

        int start = data.position();
        int count = 0;

        while (limit == 0 || results.size() <= limit) {
            // set the length in case of OFPExtStatistics
            if (statistics instanceof OFPExtStatistics)
                ((OFPExtStatistics) statistics).setLength(length);

            /**
             * can't use data.remaining() here, b/c there could be other data
             * buffered past this message
             */
            if ((length - count) >= statistics.getLength()) {
                if (statistics instanceof OFPActionFactoryAware)
                    ((OFPActionFactoryAware) statistics).setActionFactory(this);
                statistics.readFrom(data);
                results.add(statistics);
                count += statistics.getLength();
                statistics = getStatistics(t, st);
            } else {
                if (count < length) {
                    /**
                     * Nasty case: partial/incomplete statistic found even
                     * though we have a full message. Found when NOX sent
                     * agg_stats request with wrong agg statistics length (52
                     * instead of 56)
                     *
                     * just throw the rest away, or we will break framing
                     */
                    data.position(start + length);
                }
                return results;
            }
        }
        return results; // empty; no statistics at all
    }

    @Override
    public OFPQueueProperty getQueueProperty(OFPQueuePropertyType t) {
        return t.newInstance();
    }

    @Override
    public List<OFPQueueProperty> parseQueueProperties(IDataBuffer data, int length) {
        return parseQueueProperties(data, length, 0);
    }

    @Override
    public List<OFPQueueProperty> parseQueueProperties(IDataBuffer data, int length, int limit) {
        List<OFPQueueProperty> results = new ArrayList<OFPQueueProperty>();
        OFPQueueProperty demux = new OFPQueueProperty();
        OFPQueueProperty ofqp;
        int end = data.position() + length;

        while (limit == 0 || results.size() <= limit) {
            if (data.remaining() < OFPQueueProperty.MINIMUM_LENGTH
                    || (data.position() + OFPQueueProperty.MINIMUM_LENGTH) > end)
                return results;

            data.mark();
            demux.readFrom(data);
            data.reset();

            if (demux.getLengthU() > data.remaining() || (data.position() + demux.getLengthU()) > end)
                return results;

            ofqp = getQueueProperty(demux.getType());
            ofqp.readFrom(data);
            if (OFPQueueProperty.class.equals(ofqp.getClass())) {
                // advance the position for un-implemented messages
                data.position(data.position() + (ofqp.getLengthU() - OFPQueueProperty.MINIMUM_LENGTH));
            }
            results.add(ofqp);
        }

        return results;
    }

    @Override
    public OFPInstruction getInstruction(OFPInstructionType type) {

        return type.newInstance();
    }

    @Override
    public List<OFPInstruction> parseInstructions(IDataBuffer data, int length) {
        return this.parseInstructions(data, length, 0);
    }

    @Override
    public List<OFPInstruction> parseInstructions(IDataBuffer data, int length, int limit) {
        List<OFPInstruction> results = new ArrayList<OFPInstruction>();
        OFPInstruction demux = new OFPInstruction();
        OFPInstruction ofInstruction;
        int end = data.position() + length;

        while (limit == 0 || results.size() <= limit) {
            if (data.remaining() < OFPInstruction.MINIMUM_LENGTH
                    || (data.position() + OFPInstruction.MINIMUM_LENGTH) > end)
                return results;

            data.mark();
            demux.readFrom(data);
            data.reset();

            if (demux.getLengthU() > data.remaining() || (data.position() + demux.getLengthU()) > end) {
                return results;
            }
            // get instruction type
            ofInstruction = getInstruction(demux.getOFInstructionType());
            // set action factory
            if (ofInstruction instanceof OFPActionFactoryAware) {
                ((OFPActionFactoryAware) ofInstruction).setActionFactory(this);
            }
            ofInstruction.readFrom(data);
            // TBD - about commented part
            // if (OFPInstruction.class.equals(ofInstruction.getClass())) {
            // // advance the position for un-implemented messages
            // data.position(data.position()+(ofInstruction.getLengthU() -
            // OFPInstruction.MINIMUM_LENGTH));
            // }
            results.add(ofInstruction);
        }

        return results;
    }
}

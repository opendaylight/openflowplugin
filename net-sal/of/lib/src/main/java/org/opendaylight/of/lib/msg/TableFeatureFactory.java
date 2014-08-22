/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.*;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.instr.*;
import org.opendaylight.of.lib.match.*;
import org.opendaylight.util.ResourceUtils;
import org.opendaylight.util.StringUtils;

import java.util.*;

import static org.opendaylight.of.lib.CommonUtils.*;
import static org.opendaylight.of.lib.instr.ActionFactory.parseActionHeaders;
import static org.opendaylight.of.lib.instr.InstructionFactory.createInstructionHeaders;
import static org.opendaylight.of.lib.instr.InstructionFactory.parseInstructionHeaders;
import static org.opendaylight.of.lib.match.FieldFactory.parseFieldHeaders;

/**
 * A factory for {@link TableFeatureProp table feature properties}.
 *
 * @author Simon Hunt
 */
public class TableFeatureFactory extends AbstractFactory {

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            TableFeatureFactory.class, "tableFeatureFactory");

    private static final String E_BAD_TABLE_FEATURE_LENGTH = RES
            .getString("e_bad_table_feature_length");

    static final int PROP_HEADER_LEN = 4;
    static final int INSTR_HEADER_LEN = 4;
    static final int ACTION_HEADER_LEN = 4;
    static final int BASIC_FIELD_HEADER_LEN = 4;
    static final int EXP_ID_AND_TYPE_LEN = 8;

    static final TableFeatureFactory TFF = new TableFeatureFactory();

    // no instantiation except here
    private TableFeatureFactory() { }

    /** Returns an identifying tag for the table feature factory.
     *
     * @return an identifying tag
     */
    @Override
    protected String tag() {
        return "TFF";
    }

    // ========================================================= PARSING ====

    /**
     * Parses a list of Table Feature properties from the supplied buffer.
     * The caller must calculate and specify the target reader index of the
     * buffer that marks the end of the list, so we know when to stop.
     * <p>
     * Note that this method causes the reader index of the underlying
     * {@code PacketBuffer} to be advanced by the length of the list, which
     * should leave the read index at {@code targetRi}.
     * <p>
     * This method delegates to {@link #parseProp} for each individual
     * property.
     *
     * @param targetRi the target reader index
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return a list of parsed table feature properties
     * @throws MessageParseException if a problem parsing the buffer
     * @throws VersionMismatchException if version is &lt; 1.3
     */
    public static List<TableFeatureProp> parsePropList(int targetRi,
                                                       OfPacketReader pkt,
                                                       ProtocolVersion pv)
            throws MessageParseException {
        verMin13(pv);
        List<TableFeatureProp> propList = new ArrayList<TableFeatureProp>();
        while(pkt.ri() < targetRi) {
            TableFeatureProp p = parseProp(pkt, pv);
            propList.add(p);
        }
        if (pkt.ri() != targetRi) {
            int offby = pkt.ri() - targetRi;
            throw TFF.mpe(pkt, E_BAD_TABLE_FEATURE_LENGTH + offby);
        }
        return propList;
    }

    /** Parses the given packet buffer as a table feature property structure.
     *
     * @param pkt the packet buffer
     * @param pv the protocol version
     * @return the parsed property instance
     * @throws MessageParseException if a problem parsing the buffer
     */
    static TableFeatureProp parseProp(OfPacketReader pkt, ProtocolVersion pv)
            throws MessageParseException {
        try {
            TableFeatureProp.Header hdr = parseHeader(pkt, pv);
            return createParsedPropInstance(hdr, pkt, pv);
        } catch (Exception e) {
            throw TFF.mpe(pkt, e);
        }
    }

    /** Parses a property header from the buffer.
     *
     * @param pkt the buffer
     * @param pv the protocol version
     * @return the parsed property header
     * @throws MessageParseException if there is an issue parsing the header
     * @throws DecodeException if there is an issue parsing the property type
     */
    private static TableFeatureProp.Header parseHeader(OfPacketReader pkt,
                                                       ProtocolVersion pv)
            throws MessageParseException, DecodeException {
        TableFeatureProp.Header hdr = new TableFeatureProp.Header();
        int code = pkt.readU16();
        hdr.type = TableFeaturePropType.decode(code, pv);
        hdr.length = pkt.readU16();
        return hdr;
    }

    /** Creates the correct concrete instance for the property type.
     *
     * @param hdr the property header
     * @param pkt the buffer to continue reading from
     * @param pv the protocol version
     * @return the property instance
     * @throws MessageParseException if unable to parse the element
     */
    private static TableFeatureProp
    createParsedPropInstance(TableFeatureProp.Header hdr, OfPacketReader pkt,
                             ProtocolVersion pv) throws MessageParseException {
        TableFeatureProp tfp = null;
        switch (hdr.type) {
            case INSTRUCTIONS:
            case INSTRUCTIONS_MISS:
                tfp = readInstr(new TableFeaturePropInstr(hdr), pkt, pv);
                break;

            case NEXT_TABLES:
            case NEXT_TABLES_MISS:
                tfp = readNextTable(new TableFeaturePropNextTable(hdr),
                        pkt, pv);
                break;

            case WRITE_ACTIONS:
            case WRITE_ACTIONS_MISS:
            case APPLY_ACTIONS:
            case APPLY_ACTIONS_MISS:
                tfp = readAction(new TableFeaturePropAction(hdr), pkt, pv);
                break;

            case MATCH:
            case WILDCARDS:
            case WRITE_SETFIELD:
            case WRITE_SETFIELD_MISS:
            case APPLY_SETFIELD:
            case APPLY_SETFIELD_MISS:
                tfp = readOxm(new TableFeaturePropOxm(hdr), pkt, pv);
                break;


            case EXPERIMENTER:
            case EXPERIMENTER_MISS:
                tfp = readExper(new TableFeaturePropExper(hdr), pkt, pv);
        }
        return tfp;
    }

    /** Reads an "instructions" property structure from the specified reader.
     *
     * @param prop table feature property
     * @param pkt packet reader
     * @param pv protocol version
     * @return table feature property descriptor
     * @throws MessageParseException if unable to parse the element
     */
    private static TableFeatureProp readInstr(TableFeaturePropInstr prop,
                                              OfPacketReader pkt,
                                              ProtocolVersion pv)
            throws MessageParseException {
        final int targetRi = pkt.ri() + prop.header.length -
                TableFeatureProp.HEADER_LEN;
        prop.supportedInstr = new TreeSet<InstructionType>();
        prop.experInstr = new ArrayList<InstrExperimenter>();
        List<Instruction> insList = parseInstructionHeaders(targetRi, pkt, pv);
        for (Instruction ins: insList) {
            if (InstrExperimenter.class.isInstance(ins))
                prop.experInstr.add((InstrExperimenter) ins);
            else
                prop.supportedInstr.add(ins.getInstructionType());
        }
        pkt.skip(calcPadding(prop.header.length));
        return prop;
    }

    /** Reads a "next table" property structure from the specified reader.
     *
     * @param prop table feature property
     * @param pkt the packet reader
     * @param pv the protocol version
     * @return table feature property descriptor
     */
    private static TableFeatureProp
    readNextTable(TableFeaturePropNextTable prop, OfPacketReader pkt,
                  ProtocolVersion pv) {
        final int targetRi = pkt.ri() + prop.header.length -
                TableFeatureProp.HEADER_LEN;
        prop.nextTables = new TreeSet<TableId>();
        while (pkt.ri() < targetRi)
            prop.nextTables.add(pkt.readTableId());
        pkt.skip(calcPadding(prop.header.length));
        return prop;
    }

    /** Reads an "Actions" property structure from the specified reader.
     *
     * @param prop table feature property
     * @param pkt packet reader
     * @param pv protocol version
     * @return table feature property descriptor
     * @throws MessageParseException if unable to parse the element
     */
    private static TableFeatureProp readAction(TableFeaturePropAction prop,
                                               OfPacketReader pkt,
                                               ProtocolVersion pv)
            throws MessageParseException {
        final int targetRi = pkt.ri() + prop.header.length -
                TableFeatureProp.HEADER_LEN;
        prop.supportedActions = new TreeSet<ActionType>();
        prop.experActions = new ArrayList<ActExperimenter>();
        List<Action> actList = parseActionHeaders(targetRi, pkt, pv);
        for (Action act: actList) {
            if (ActExperimenter.class.isInstance(act))
                prop.experActions.add((ActExperimenter) act);
            else
                prop.supportedActions.add(act.getActionType());
        }
        pkt.skip(calcPadding(prop.header.length));
        return prop;
    }

    private static TableFeatureProp readOxm(TableFeaturePropOxm prop,
                                            OfPacketReader pkt,
                                            ProtocolVersion pv)
            throws MessageParseException {
        final int targetRi = pkt.ri() + prop.header.length -
                TableFeatureProp.HEADER_LEN;
        prop.experFields = new ArrayList<MFieldExperimenter>();
        prop.fieldAndMask = new TreeMap<OxmBasicFieldType, Boolean>();

        List<MatchField> fields = parseFieldHeaders(targetRi, pkt, pv);
        for (MatchField mf: fields) {
            if (MFieldBasicHeader.class.isInstance(mf)) {
                MFieldBasicHeader bh = (MFieldBasicHeader) mf;
                OxmBasicFieldType ft = (OxmBasicFieldType) bh.getFieldType();
                prop.fieldAndMask.put(ft, mf.hasMask());
            } else if (MFieldExperimenter.class.isInstance(mf)) {
                prop.experFields.add((MFieldExperimenter) mf);
            }
        }
        pkt.skip(calcPadding(prop.header.length));
        return prop;
    }

    private static TableFeatureProp readExper(TableFeaturePropExper prop,
                                              OfPacketReader pkt,
                                              ProtocolVersion pv) {
        final int targetRi = pkt.ri() +
                prop.header.length - TableFeatureProp.HEADER_LEN;
        prop.encodedExpId = pkt.readInt();
        prop.expDefinedType = pkt.readU32();
        final int dataSize = targetRi - pkt.ri();
        if (dataSize > 0)
            prop.data = pkt.readBytes(dataSize);
        pkt.skip(calcPadding(prop.header.length));
        return prop;
    }

    // ============================================= CREATING / ENCODING ====

    private static final String E_UNEX_TYPE = RES.getString("e_unex_type");

    /** Create a property header, setting the length to the default value.
     *
     * @param pv the protocol version
     * @param type the property type
     * @return the header
     */
    private static TableFeatureProp.Header createHeader(ProtocolVersion pv,
                                                        TableFeaturePropType type) {
        TableFeatureProp.Header header = new TableFeatureProp.Header();
        header.type = type;
        header.length = PROP_HEADER_LEN;
        return header;
    }

    /** Creates a table features instructions property, for the given
     * protocol version, indicating support for the specified instruction
     * types.
     * <p>
     * The {@code type} parameter should be one of:
     * <ul>
     *     <li>INSTRUCTIONS</li>
     *     <li>INSTRUCTIONS_MISS</li>
     * </ul>
     *
     * @param pv the protocol version
     * @param type the type of property
     * @param ins supported instructions
     * @return the property
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if type is inappropriate
     */
    public static TableFeaturePropInstr
    createInstrProp(ProtocolVersion pv, TableFeaturePropType type,
                    Set<InstructionType> ins) {
        return createInstrProp(pv, type, ins, null);
    }

    /** Creates a table features instructions property, for the given
     * protocol version, indicating support for the specified instruction
     * types, as well as the specified experimenter instructions.
     * <p>
     * The {@code type} parameter should be one of:
     * <ul>
     *     <li>INSTRUCTIONS</li>
     *     <li>INSTRUCTIONS_MISS</li>
     * </ul>
     *
     * @param pv the protocol version
     * @param type the type of property
     * @param ins supported instructions
     * @param insExp supported experimenter instructions
     * @return the property
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if type is inappropriate
     */
    public static TableFeaturePropInstr
    createInstrProp(ProtocolVersion pv, TableFeaturePropType type,
                    Set<InstructionType> ins, List<InstrExperimenter> insExp) {
        MessageFactory.checkVersionSupported(pv);
        verMin13(pv);
        notNull(type, ins);
        TableFeatureProp.Header hdr = createHeader(pv, type);
        TableFeaturePropInstr prop;
        switch (type) {
            case INSTRUCTIONS:
            case INSTRUCTIONS_MISS:
                prop = new TableFeaturePropInstr(hdr);
                prop.supportedInstr = new TreeSet<InstructionType>(ins);
                prop.header.length += ins.size() * INSTR_HEADER_LEN;
                if (insExp != null) {
                    prop.experInstr = new ArrayList<InstrExperimenter>(insExp);
                    for (InstrExperimenter ie: insExp)
                        prop.header.length += ie.getTotalLength();
                }
                break;

            default:
                throw new IllegalArgumentException(E_UNEX_TYPE + type);
        }
        return prop;
    }

    /** Creates a table features next-tables property, for the given
     * protocol version, and the specified table IDs.
     * <p>
     * The {@code type} parameter should be one of:
     * <ul>
     *     <li>NEXT_TABLES</li>
     *     <li>NEXT_TABLES_MISS</li>
     * </ul>
     *
     * @param pv the protocol version
     * @param type the type of property
     * @param tableIds the table IDs
     * @return the property
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if type is inappropriate
     */
    public static TableFeaturePropNextTable
    createNextTablesProp(ProtocolVersion pv, TableFeaturePropType type,
                         Set<TableId> tableIds) {
        MessageFactory.checkVersionSupported(pv);
        verMin13(pv);
        notNull(type, tableIds);
        TableFeatureProp.Header hdr = createHeader(pv, type);
        TableFeaturePropNextTable prop;
        switch (type) {
            case NEXT_TABLES:
            case NEXT_TABLES_MISS:
                prop = new TableFeaturePropNextTable(hdr);
                prop.nextTables = new TreeSet<TableId>(tableIds);
                prop.header.length += tableIds.size();
                break;

            default:
                throw new IllegalArgumentException(E_UNEX_TYPE + type);
        }
        return prop;
    }

    /** Creates a table features action property, for the given
     * protocol version, and the specified action types.
     * <p>
     * The {@code type} parameter should be one of:
     * <ul>
     *     <li>WRITE_ACTIONS</li>
     *     <li>WRITE_ACTIONS_MISS</li>
     *     <li>APPLY_ACTIONS</li>
     *     <li>APPLY_ACTIONS_MISS</li>
     * </ul>
     *
     * @param pv the protocol version
     * @param type the type of property
     * @param actionTypes the supported action types
     * @return the property
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if type is inappropriate
     */
    public static TableFeaturePropAction
    createActionProp(ProtocolVersion pv, TableFeaturePropType type,
                     Set<ActionType> actionTypes) {
        return createActionProp(pv, type, actionTypes, null);
    }

    /** Creates a table features action property, for the given
     * protocol version, and the specified action types and experimenter
     * action types.
     * <p>
     * The {@code type} parameter should be one of:
     * <ul>
     *     <li>WRITE_ACTIONS</li>
     *     <li>WRITE_ACTIONS_MISS</li>
     *     <li>APPLY_ACTIONS</li>
     *     <li>APPLY_ACTIONS_MISS</li>
     * </ul>
     *
     * @param pv the protocol version
     * @param type the type of property
     * @param actionTypes the supported action types
     * @param actionExper the supported experimenter actions
     * @return the property
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if type is inappropriate
     */
    public static TableFeaturePropAction
    createActionProp(ProtocolVersion pv, TableFeaturePropType type,
                     Set<ActionType> actionTypes,
                     List<ActExperimenter> actionExper) {
        MessageFactory.checkVersionSupported(pv);
        verMin13(pv);
        notNull(type, actionTypes);
        TableFeatureProp.Header hdr = createHeader(pv, type);
        TableFeaturePropAction prop;
        switch (type) {
            case WRITE_ACTIONS:
            case WRITE_ACTIONS_MISS:
            case APPLY_ACTIONS:
            case APPLY_ACTIONS_MISS:
                prop = new TableFeaturePropAction(hdr);
                prop.supportedActions = new TreeSet<ActionType>(actionTypes);
                prop.header.length += actionTypes.size() * ACTION_HEADER_LEN;
                if (actionExper != null) {
                    prop.experActions =
                            new ArrayList<ActExperimenter>(actionExper);
                    for (ActExperimenter ae: actionExper)
                        prop.header.length += ae.getTotalLength();
                }
                break;

            default:
                throw new IllegalArgumentException(E_UNEX_TYPE + type);
        }
        return prop;
    }

    /** Creates a table features OXM property, for the given
     * protocol version, and the specified match fields. A map is used to
     * denote the supported types and whether they are maskable or not.
     * <p>
     * The {@code type} parameter should be one of:
     * <ul>
     *     <li>MATCH</li>
     *     <li>WILDCARDS</li>
     *     <li>WRITE_SETFIELD</li>
     *     <li>WRITE_SETFIELD_MISS</li>
     *     <li>APPLY_SETFIELD</li>
     *     <li>APPLY_SETFIELD_MISS</li>
     * </ul>
     *
     * @param pv the protocol version
     * @param type the type of property
     * @param fields the supported basic fields, and whether they are maskable
     * @return the property
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if type is inappropriate
     */
    public static TableFeaturePropOxm
    createOxmProp(ProtocolVersion pv, TableFeaturePropType type,
                  Map<OxmBasicFieldType, Boolean> fields) {
        return createOxmProp(pv, type, fields, null);
    }

    /** Creates a table features OXM property, for the given
     * protocol version, and the specified match fields. For the basic
     * field types a map is used to denote the supported types and whether
     * they are maskable or not; experimenter fields are passed in as a list.
     * <p>
     * The {@code type} parameter should be one of:
     * <ul>
     *     <li>MATCH</li>
     *     <li>WILDCARDS</li>
     *     <li>WRITE_SETFIELD</li>
     *     <li>WRITE_SETFIELD_MISS</li>
     *     <li>APPLY_SETFIELD</li>
     *     <li>APPLY_SETFIELD_MISS</li>
     * </ul>
     *
     * @param pv the protocol version
     * @param type the type of property
     * @param fields the supported basic fields, and whether they are maskable
     * @param expFields the supported experimenter fields
     * @return the property
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if type is inappropriate
     */
    public static TableFeaturePropOxm
    createOxmProp(ProtocolVersion pv, TableFeaturePropType type,
                  Map<OxmBasicFieldType, Boolean> fields,
                  List<MFieldExperimenter> expFields) {
        MessageFactory.checkVersionSupported(pv);
        verMin13(pv);
        notNull(type, fields);
        TableFeatureProp.Header hdr = createHeader(pv, type);
        TableFeaturePropOxm prop;
        switch (type) {
            case MATCH:
            case WILDCARDS:
            case WRITE_SETFIELD:
            case WRITE_SETFIELD_MISS:
            case APPLY_SETFIELD:
            case APPLY_SETFIELD_MISS:
                prop = new TableFeaturePropOxm(hdr);
                prop.experFields = expFields == null ? null :
                        new ArrayList<MFieldExperimenter>(expFields);
                prop.fieldAndMask =
                        new TreeMap<OxmBasicFieldType, Boolean>(fields);
                // account for lengths
                prop.header.length += BASIC_FIELD_HEADER_LEN * fields.size();
                if (expFields != null)
                    for (MFieldExperimenter mfe: expFields)
                        prop.header.length += mfe.getTotalLength();
                 break;

            default:
                throw new IllegalArgumentException(E_UNEX_TYPE + type);
        }
        return prop;
    }

    /** Creates a table features experimenter property, for the given
     * protocol version, and the specified experimenter data.
     * <p>
     * The {@code type} parameter should be one of:
     * <ul>
     *     <li>EXPERIMENTER</li>
     *     <li>EXPERIMENTER_MISS</li>
     * </ul>
     *
     * @param pv the protocol version
     * @param type the type of property
     * @param eid the experimenter ID
     * @param expDefType the experimenter defined type
     * @param data the experimenter defined data
     * @return the property
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if type is inappropriate
     */
    public static TableFeaturePropExper
    createExperProp(ProtocolVersion pv, TableFeaturePropType type,
                    ExperimenterId eid, long expDefType, byte[] data)   {
        return createExperProp(pv, type, eid.encodedId(), expDefType, data);
    }

    /** Creates a table features experimenter property, for the given
     * protocol version, and the specified experimenter data.
     * <p>
     * The {@code type} parameter should be one of:
     * <ul>
     *     <li>EXPERIMENTER</li>
     *     <li>EXPERIMENTER_MISS</li>
     * </ul>
     *
     * @param pv the protocol version
     * @param type the type of property
     * @param id the encoded experimenter ID
     * @param expDefType the experimenter defined type
     * @param data the experimenter defined data
     * @return the property
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if type is inappropriate
     */
    public static TableFeaturePropExper
    createExperProp(ProtocolVersion pv, TableFeaturePropType type,
                    int id, long expDefType, byte[] data)   {
        MessageFactory.checkVersionSupported(pv);
        verMin13(pv);
        notNull(type);
        TableFeatureProp.Header hdr = createHeader(pv, type);
        TableFeaturePropExper prop;
        switch (type) {
            case EXPERIMENTER:
            case EXPERIMENTER_MISS:
                prop = new TableFeaturePropExper(hdr);
                prop.encodedExpId = id;
                prop.expDefinedType = expDefType;
                prop.data = data == null ? null : data.clone();
                int dataLen = data == null ? 0 : data.length;
                prop.header.length = PROP_HEADER_LEN + EXP_ID_AND_TYPE_LEN +
                        dataLen;
                break;

            default:
                throw new IllegalArgumentException(E_UNEX_TYPE + type);
        }
        return prop;
    }


    //======================================================================
    // === Encoding

    /** Encodes a list of table feature properties, writing them into the
     * supplied buffer. Note that this method causes the writer index of the
     * underlying {@code PacketBuffer} to be advanced by the length of all the
     * written properties.
     *
     * @param pv the protocol version
     * @param props the properties
     * @param pkt the buffer into which the properties should be written
     */
    public static void encodePropList(ProtocolVersion pv,
                                      List<TableFeatureProp> props,
                                      OfPacketWriter pkt) {
        for (TableFeatureProp p: props)
            encodeProp(pv, p, pkt);
    }

    /** Encodes a table feature property, writing it into the supplied buffer.
     * Note that this method causes the writer index of the underlying
     * {@code PacketBuffer} to be advanced by the length of the property.
     *
     * @param pv the protocol version
     * @param prop the property
     * @param pkt the buffer into which the property is to be written
     */
    public static void encodeProp(ProtocolVersion pv, TableFeatureProp prop,
                                  OfPacketWriter pkt) {
        // first, write out the header...
        TableFeaturePropType type = prop.getType();
        pkt.writeU16(type.getCode(pv));
        pkt.writeU16(prop.getTotalLength());

        // now deal with the payload, based on type
        switch (type) {

            case INSTRUCTIONS:
            case INSTRUCTIONS_MISS:
                encodeInstr((TableFeaturePropInstr) prop, pkt, pv);
                break;

            case NEXT_TABLES:
            case NEXT_TABLES_MISS:
                encodeNextTables((TableFeaturePropNextTable) prop, pkt, pv);
                break;

            case WRITE_ACTIONS:
            case WRITE_ACTIONS_MISS:
            case APPLY_ACTIONS:
            case APPLY_ACTIONS_MISS:
                encodeActions((TableFeaturePropAction) prop, pkt, pv);
                break;

            case MATCH:
            case WILDCARDS:
            case WRITE_SETFIELD:
            case WRITE_SETFIELD_MISS:
            case APPLY_SETFIELD:
            case APPLY_SETFIELD_MISS:
                encodeOxm((TableFeaturePropOxm) prop, pkt, pv);
                break;

            case EXPERIMENTER:
            case EXPERIMENTER_MISS:
                encodeExper((TableFeaturePropExper) prop, pkt, pv);
                break;
        }
    }

    private static void encodeInstr(TableFeaturePropInstr prop,
                                    OfPacketWriter pkt, ProtocolVersion pv) {
        List<Instruction> iHdrs =
                createInstructionHeaders(pv, prop.supportedInstr);
        InstructionFactory.encodeInstructionList(iHdrs, pkt);
        if (prop.experInstr != null)
            InstructionFactory.encodeInstrExperList(prop.experInstr, pkt);
        pkt.writeZeros(calcPadding(prop.getTotalLength()));
    }

    private static void encodeNextTables(TableFeaturePropNextTable prop,
                                         OfPacketWriter pkt, ProtocolVersion pv) {
        for (TableId t: prop.nextTables)
            pkt.write(t);
        pkt.writeZeros(calcPadding(prop.getTotalLength()));
    }

    private static void encodeActions(TableFeaturePropAction prop,
                                      OfPacketWriter pkt, ProtocolVersion pv) {
        List<Action> aHdrs =
                ActionFactory.createActionHeaders(pv, prop.supportedActions);
        ActionFactory.encodeActionList(aHdrs, pkt);
        if (prop.experActions != null)
            ActionFactory.encodeActionExperList(prop.experActions, pkt);
        pkt.writeZeros(calcPadding(prop.getTotalLength()));
    }

    private static void encodeOxm(TableFeaturePropOxm prop,
                                  OfPacketWriter pkt, ProtocolVersion pv) {
        List<MatchField> fHdrs =
                FieldFactory.createFieldHeaders(pv, prop.fieldAndMask);
        FieldFactory.encodeFieldList(fHdrs, pkt);
        if (prop.experFields != null)
            FieldFactory.encodeFieldExperList(prop.experFields, pkt);
        pkt.writeZeros(calcPadding(prop.getTotalLength()));
    }

    private static void encodeExper(TableFeaturePropExper prop,
                                    OfPacketWriter pkt, ProtocolVersion pv) {
        pkt.writeInt(prop.encodedExpId);
        pkt.writeU32(prop.expDefinedType);
        if (prop.data != null)
            pkt.writeBytes(prop.data);
        pkt.writeZeros(calcPadding(prop.getTotalLength()));
    }

    //======================================================================
    // === Utilities

    /** Returns the length of the table feature property in bytes, when
     * encoded. This method is provided to allow proper encoding of a table
     * features multipart message body.
     *
     * @param prop the property
     * @return its length in bytes, when encoded
     */
    public static int getPropLength(TableFeatureProp prop) {
        return prop.getTotalLength();
    }

    /** Calculates how many zero-filled bytes of padding are required at the
     * end of the property structure, so that the end of the structure lands
     * on an 8-byte boundary.
     *
     * @param len unpadded length
     * @return the number of padding bytes required
     */
    static int calcPadding(int len) {
        // See section A.2.3.1 (p.39) of the 1.3 spec for details...
        return ((len + 7) / 8 * 8 - len);
    }

    /** Outputs a list of table feature properties in debug string format.
     *
     * @param props the list of properties
     * @return a multi-line string representation of the list of properties
     */
    public static String toDebugString(List<TableFeatureProp> props) {
        return toDebugString(0, props);
    }

    /** Outputs a list of table feature properties in debug string format.
     *
     * @param indent the additional indent (number of spaces)
     * @param props the list of properties
     * @return a multi-line string representation of the list of properties
     */
    public static String toDebugString(int indent,
                                       List<TableFeatureProp> props) {
        if (props == null || props.size() == 0)
            return CommonUtils.NONE;
        final String indStr = EOLI + StringUtils.spaces(indent);
        StringBuilder sb = new StringBuilder();
        for (TableFeatureProp p: props)
            sb.append(indStr).append(p.toDebugString(indent + 2));
        return sb.toString();
    }
}
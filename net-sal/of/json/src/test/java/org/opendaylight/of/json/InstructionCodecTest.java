/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.json;

import org.junit.Test;
import org.opendaylight.of.lib.instr.InstrWriteActions;
import org.opendaylight.of.lib.instr.Instruction;
import org.opendaylight.util.json.JsonFactory;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.json.InstructionCodec.ROOT;
import static org.opendaylight.of.json.InstructionCodec.ROOTS;
import static org.opendaylight.of.json.InstructionCodecTestUtils.*;
import static org.opendaylight.util.StringUtils.getFileContents;
import static org.opendaylight.util.StringUtils.normalizeEOL;
import static org.opendaylight.util.json.JsonValidator.validate;
import static org.opendaylight.util.junit.TestTools.AM_NEQ;


/**
 * Unit tests for {@link InstructionCodec}.
 *
 * @author Shaila Shree
 */
public class InstructionCodecTest {

    private static final JsonFactory factory = OfJsonFactory.instance();
    private static final InstructionCodec codec = (InstructionCodec)
            factory.codec(Instruction.class);

    private static final String EXP_JSON_PATH = "org/opendaylight/of/json/v13/";

    private static final String INSTR = "instruction.json";
    private static final String INSTRS = "instructions.json";

    private final ClassLoader cl = getClass().getClassLoader();

    private String getJson(String name) throws IOException {
        return getFileContents(EXP_JSON_PATH + name, cl);
    }

    @Test
    public void encode() throws IOException {
        String exp = getJson(INSTR);
        String actual = codec.encode(createInstruction(), true);
        assertEquals(AM_NEQ, normalizeEOL(exp), normalizeEOL(actual));
    }

    @Test
    public void decode() throws IOException {
        String actual = getJson(INSTR);
        verifyInstruction((InstrWriteActions) codec.decode(actual));
        validate(actual, ROOT);
    }

    @Test
    public void encodeList() throws IOException {
        String exp = getJson(INSTRS);
        String actual = codec.encodeList(createAllInstructions(), true);
        assertEquals(AM_NEQ, normalizeEOL(exp), normalizeEOL(actual));
    }

    @Test
    public void decodeList() throws IOException {
        String actual = getJson(INSTRS);
        verifyAllInstructions(codec.decodeList(actual));
        validate(actual, ROOTS);
    }
}


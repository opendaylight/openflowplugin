/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyShort;
import static org.mockito.Mockito.when;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializationFactory;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 *
 * @author jameshall
 */
public class OFDecoderTest {

    @Mock ChannelHandlerContext mockChHndlrCtx ;
    @Mock DeserializationFactory mockDeserializationFactory ;
    @Mock DataObject mockDataObject ;

    OFDecoder ofDecoder ;
    private ByteBuf writeObj;
    private VersionMessageWrapper inMsg;
    private List<Object> outList;

    /**
     * Sets up test environment
     *
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ofDecoder = new OFDecoder() ;
        ofDecoder.setDeserializationFactory( mockDeserializationFactory ) ;
        writeObj = ByteBufUtils.hexStringToByteBuf("16 03 01 00");
        inMsg = new VersionMessageWrapper( (short)8, writeObj );
        outList = new ArrayList<>();
    }

    /**
     *
     */
    @Test
    public void testDecode() {
        when(mockDeserializationFactory.deserialize( any(ByteBuf.class), anyShort() )).thenReturn(mockDataObject);
        try {
            ofDecoder.decode(mockChHndlrCtx, inMsg, outList);
        } catch (Exception e) {
            Assert.fail();
        }

        // Verify that the message buf was released...
        assertEquals( mockDataObject, outList.get(0) ) ;
        assertEquals( 0, writeObj.refCnt() ) ;
    }

    /**
     *
     */
    @Test
    public void testDecodeDeserializeException() {
        when(mockDeserializationFactory.deserialize( any(ByteBuf.class), anyShort() ))
        .thenThrow(new IllegalArgumentException()) ;

        try {
            ofDecoder.decode(mockChHndlrCtx, inMsg, outList);
        } catch (Exception e) {
            Assert.fail();
        }

        // Verify that the message buf was released...
        assertEquals( 0, outList.size() ) ;
        assertEquals( 0, writeObj.refCnt() ) ;
    }

    /**
     *
     */
    @Test
    public void testDecodeDeserializeNull() {
        when(mockDeserializationFactory.deserialize( any(ByteBuf.class), anyShort() ))
        .thenReturn(null) ;

        try {
            ofDecoder.decode(mockChHndlrCtx, inMsg, outList);
        } catch (Exception e) {
            Assert.fail();
        }

        // Verify that the message buf was released...
        assertEquals( 0, outList.size() ) ;
        assertEquals( 0, writeObj.refCnt() ) ;
    }
}

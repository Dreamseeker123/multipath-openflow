/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.serialization.factories;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.openflowjava.protocol.impl.util.ByteBufUtils;
import org.opendaylight.openflowjava.protocol.impl.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.IpAddressAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.IpAddressActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PortAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PortActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetNwDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetTpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFlagsV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowWildcardsV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.match.v10.grouping.MatchV10Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;

/**
 * @author michal.polkorab
 *
 */
public class OF10FlowModInputMessageFactoryTest {

    /**
     * @throws Exception 
     * Testing of {@link OF10FlowModInputMessageFactory} for correct translation from POJO
     */
    @Test
    public void testFlowModInputMessageFactory() throws Exception {
        FlowModInputBuilder builder = new FlowModInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF10_VERSION_ID);
        MatchV10Builder matchBuilder = new MatchV10Builder();
        matchBuilder.setWildcards(new FlowWildcardsV10(true, true, true, true, true, true, true, true, true, true));
        matchBuilder.setNwSrcMask((short) 0);
        matchBuilder.setNwDstMask((short) 0);
        matchBuilder.setInPort(58);
        matchBuilder.setDlSrc(new MacAddress("01:01:01:01:01:01"));
        matchBuilder.setDlDst(new MacAddress("ff:ff:ff:ff:ff:ff"));
        matchBuilder.setDlVlan(18);
        matchBuilder.setDlVlanPcp((short) 5);
        matchBuilder.setDlType(42);
        matchBuilder.setNwTos((short) 4);
        matchBuilder.setNwProto((short) 7);
        matchBuilder.setNwSrc(new Ipv4Address("8.8.8.8"));
        matchBuilder.setNwDst(new Ipv4Address("16.16.16.16"));
        matchBuilder.setTpSrc(6653);
        matchBuilder.setTpDst(6633);
        builder.setMatchV10(matchBuilder.build());
        byte[] cookie = new byte[]{(byte) 0xFF, 0x01, 0x04, 0x01, 0x06, 0x00, 0x07, 0x01};
        builder.setCookie(new BigInteger(1, cookie));
        builder.setCommand(FlowModCommand.forValue(0));
        builder.setIdleTimeout(12);
        builder.setHardTimeout(16);
        builder.setPriority(1);
        builder.setBufferId(2L);
        builder.setOutPort(new PortNumber(4422L));
        builder.setFlagsV10(new FlowModFlagsV10(true, false, true));
        List<Action> actions = new ArrayList<>();
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setType(SetNwDst.class);
        IpAddressActionBuilder ipBuilder = new IpAddressActionBuilder();
        ipBuilder.setIpAddress(new Ipv4Address("2.2.2.2"));
        actionBuilder.addAugmentation(IpAddressAction.class, ipBuilder.build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        actionBuilder.setType(SetTpSrc.class);
        PortActionBuilder portBuilder = new PortActionBuilder();
        portBuilder.setPort(new PortNumber(42L));
        actionBuilder.addAugmentation(PortAction.class, portBuilder.build());
        actions.add(actionBuilder.build());
        builder.setAction(actions);
        FlowModInput message = builder.build();
        
        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        OF10FlowModInputMessageFactory factory = OF10FlowModInputMessageFactory.getInstance();
        factory.messageToBuffer(EncodeConstants.OF10_VERSION_ID, out, message);
        
        BufferHelper.checkHeaderV10(out, factory.getMessageType(), factory.computeLength(message));
        Assert.assertEquals("Wrong wildcards", 3678463, out.readUnsignedInt());
        Assert.assertEquals("Wrong inPort", 58, out.readUnsignedShort());
        byte[] dlSrc = new byte[6];
        out.readBytes(dlSrc);
        Assert.assertEquals("Wrong dlSrc", "01:01:01:01:01:01", ByteBufUtils.macAddressToString(dlSrc));
        byte[] dlDst = new byte[6];
        out.readBytes(dlDst);
        Assert.assertEquals("Wrong dlDst", "FF:FF:FF:FF:FF:FF", ByteBufUtils.macAddressToString(dlDst));
        Assert.assertEquals("Wrong dlVlan", 18, out.readUnsignedShort());
        Assert.assertEquals("Wrong dlVlanPcp", 5, out.readUnsignedByte());
        out.skipBytes(1);
        Assert.assertEquals("Wrong dlType", 42, out.readUnsignedShort());
        Assert.assertEquals("Wrong nwTos", 4, out.readUnsignedByte());
        Assert.assertEquals("Wrong nwProto", 7, out.readUnsignedByte());
        out.skipBytes(2);
        Assert.assertEquals("Wrong nwSrc", 134744072, out.readUnsignedInt());
        Assert.assertEquals("Wrong nwDst", 269488144, out.readUnsignedInt());
        Assert.assertEquals("Wrong tpSrc", 6653, out.readUnsignedShort());
        Assert.assertEquals("Wrong tpDst", 6633, out.readUnsignedShort());
        byte[] cookieRead = new byte[8];
        out.readBytes(cookieRead);
        Assert.assertArrayEquals("Wrong cookie", cookie, cookieRead);
        Assert.assertEquals("Wrong command", 0, out.readUnsignedShort());
        Assert.assertEquals("Wrong idleTimeOut", 12, out.readUnsignedShort());
        Assert.assertEquals("Wrong hardTimeOut", 16, out.readUnsignedShort());
        Assert.assertEquals("Wrong priority", 1, out.readUnsignedShort());
        Assert.assertEquals("Wrong bufferId", 2, out.readUnsignedInt());
        Assert.assertEquals("Wrong outPort", 4422, out.readUnsignedShort());
        Assert.assertEquals("Wrong flags", 3, out.readUnsignedShort());
        Assert.assertEquals("Wrong action - type", 7, out.readUnsignedShort());
        Assert.assertEquals("Wrong action - length", 8, out.readUnsignedShort());
        Assert.assertEquals("Wrong flags", 33686018, out.readUnsignedInt());
        Assert.assertEquals("Wrong action - type", 9, out.readUnsignedShort());
        Assert.assertEquals("Wrong action - length", 8, out.readUnsignedShort());
        Assert.assertEquals("Wrong flags", 42, out.readUnsignedShort());
        out.skipBytes(2);
    }

}

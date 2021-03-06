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

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.opendaylight.openflowjava.protocol.impl.deserialization.factories.HelloMessageFactoryTest;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.openflowjava.protocol.impl.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandDropCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandDscpRemarkCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandExperimenterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.meter.band.drop._case.MeterBandDropBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.meter.band.dscp.remark._case.MeterBandDscpRemarkBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.meter.band.experimenter._case.MeterBandExperimenterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.mod.Bands;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.mod.BandsBuilder;

/**
 * @author timotej.kubas
 * @author michal.polkorab
 */
public class MeterModInputMessageFactoryTest {

    /**
     * @throws Exception 
     * Testing of {@link MeterModInputMessageFactory} for correct translation from POJO
     */
    @Test
    public void testMeterModInputMessage() throws Exception {
        MeterModInputBuilder builder = new MeterModInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setCommand(MeterModCommand.forValue(1));
        builder.setFlags(new MeterFlags(false, true, true, false));
        builder.setMeterId(new MeterId(2248L));
        builder.setBands(createBandsList());
        MeterModInput message = builder.build();
        
        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        MeterModInputMessageFactory factory = MeterModInputMessageFactory.getInstance();
        factory.messageToBuffer(HelloMessageFactoryTest.VERSION_YET_SUPPORTED, out, message);
        
        BufferHelper.checkHeaderV13(out, factory.getMessageType(), 64);
        Assert.assertEquals("Wrong meterModCommand", message.getCommand().getIntValue(), out.readUnsignedShort());
        Assert.assertEquals("Wrong meterFlags", message.getFlags(), decodeMeterModFlags(out.readShort()));
        Assert.assertEquals("Wrong meterId", message.getMeterId().getValue().intValue(), out.readUnsignedInt());
        Assert.assertEquals("Wrong bands", message.getBands(), decodeBandsList(out));
    }
    
    private static MeterFlags decodeMeterModFlags(short input){
        final Boolean _oFPMFKBPS = (input & (1 << 0)) > 0;
        final Boolean _oFPMFPKTPS = (input & (1 << 1)) > 0;
        final Boolean _oFPMFBURST = (input & (1 << 2)) > 0; 
        final Boolean _oFPMFSTATS = (input & (1 << 3)) > 0;
        return new MeterFlags(_oFPMFBURST, _oFPMFKBPS, _oFPMFPKTPS, _oFPMFSTATS);
    }
    
    private static List<Bands> createBandsList(){
        List<Bands> bandsList = new ArrayList<>();
        BandsBuilder bandsBuilder = new BandsBuilder();
        MeterBandDropCaseBuilder dropCaseBuilder = new MeterBandDropCaseBuilder();
        MeterBandDropBuilder dropBand = new MeterBandDropBuilder();
        dropBand.setType(MeterBandType.OFPMBTDROP);
        dropBand.setRate(1L);
        dropBand.setBurstSize(2L);
        dropCaseBuilder.setMeterBandDrop(dropBand.build());
        bandsList.add(bandsBuilder.setMeterBand(dropCaseBuilder.build()).build());
        MeterBandDscpRemarkCaseBuilder dscpCaseBuilder = new MeterBandDscpRemarkCaseBuilder();
        MeterBandDscpRemarkBuilder dscpRemarkBand = new MeterBandDscpRemarkBuilder();
        dscpRemarkBand.setType(MeterBandType.OFPMBTDSCPREMARK);
        dscpRemarkBand.setRate(1L);
        dscpRemarkBand.setBurstSize(2L);
        dscpRemarkBand.setPrecLevel((short) 3);
        dscpCaseBuilder.setMeterBandDscpRemark(dscpRemarkBand.build());
        bandsList.add(bandsBuilder.setMeterBand(dscpCaseBuilder.build()).build());
        MeterBandExperimenterCaseBuilder expCaseBuilder = new MeterBandExperimenterCaseBuilder();
        MeterBandExperimenterBuilder experimenterBand = new MeterBandExperimenterBuilder();
        experimenterBand.setType(MeterBandType.OFPMBTEXPERIMENTER);
        experimenterBand.setRate(1L);
        experimenterBand.setBurstSize(2L);
        experimenterBand.setExperimenter(4L);
        expCaseBuilder.setMeterBandExperimenter(experimenterBand.build());
        bandsList.add(bandsBuilder.setMeterBand(expCaseBuilder.build()).build());
        return bandsList;
    }
    
    private static List<Bands> decodeBandsList(ByteBuf input){
        List<Bands> bandsList = new ArrayList<>();
        BandsBuilder bandsBuilder = new BandsBuilder();
        MeterBandDropCaseBuilder dropCaseBuilder = new MeterBandDropCaseBuilder();
        MeterBandDropBuilder dropBand = new MeterBandDropBuilder();
        dropBand.setType(MeterBandType.forValue(input.readUnsignedShort()));
        input.skipBytes(Short.SIZE/Byte.SIZE);
        dropBand.setRate(input.readUnsignedInt());
        dropBand.setBurstSize(input.readUnsignedInt());
        input.skipBytes(4);
        dropCaseBuilder.setMeterBandDrop(dropBand.build());
        bandsList.add(bandsBuilder.setMeterBand(dropCaseBuilder.build()).build());
        MeterBandDscpRemarkCaseBuilder dscpCaseBuilder = new MeterBandDscpRemarkCaseBuilder();
        MeterBandDscpRemarkBuilder dscpRemarkBand = new MeterBandDscpRemarkBuilder();
        dscpRemarkBand.setType(MeterBandType.forValue(input.readUnsignedShort()));
        input.skipBytes(Short.SIZE/Byte.SIZE);
        dscpRemarkBand.setRate(input.readUnsignedInt());
        dscpRemarkBand.setBurstSize(input.readUnsignedInt());
        dscpRemarkBand.setPrecLevel(input.readUnsignedByte());
        input.skipBytes(3);
        dscpCaseBuilder.setMeterBandDscpRemark(dscpRemarkBand.build());
        bandsList.add(bandsBuilder.setMeterBand(dscpCaseBuilder.build()).build());
        MeterBandExperimenterCaseBuilder expCaseBuilder = new MeterBandExperimenterCaseBuilder();
        MeterBandExperimenterBuilder experimenterBand = new MeterBandExperimenterBuilder();
        experimenterBand.setType(MeterBandType.forValue(input.readUnsignedShort()));
        input.skipBytes(Short.SIZE/Byte.SIZE);
        experimenterBand.setRate(input.readUnsignedInt());
        experimenterBand.setBurstSize(input.readUnsignedInt());
        experimenterBand.setExperimenter(input.readUnsignedInt());
        expCaseBuilder.setMeterBandExperimenter(experimenterBand.build());
        bandsList.add(bandsBuilder.setMeterBand(expCaseBuilder.build()).build());
        return bandsList;
    }
    
}

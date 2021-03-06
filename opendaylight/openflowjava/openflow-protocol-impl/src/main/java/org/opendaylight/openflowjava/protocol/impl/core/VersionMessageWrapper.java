/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.core;

import io.netty.buffer.ByteBuf;

/**
 * Wraps received messages (includes version)
 * @author michal.polkorab
 */
public class VersionMessageWrapper {

    private short version;
    private ByteBuf messageBuffer;
    
    /**
     * Constructor
     * @param version version decoded in {@link OFVersionDetector}
     * @param messageBuffer message received from {@link OFFrameDecoder}
     */
    public VersionMessageWrapper(short version, ByteBuf messageBuffer) {
        this.version = version;
        this.messageBuffer = messageBuffer;
    }

    /**
     * @return the version version decoded in {@link OFVersionDetector}
     */
    public short getVersion() {
        return version;
    }

    /**
     * @return the messageBuffer message received from {@link OFFrameDecoder}
     */
    public ByteBuf getMessageBuffer() {
        return messageBuffer;
    }
    
    
}

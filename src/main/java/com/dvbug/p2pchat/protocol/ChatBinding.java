/*
 * Copyright (C) Vito
 * By Vito on 2022/1/12 11:34
 */
package com.dvbug.p2pchat.protocol;

import io.libp2p.core.multistream.StrictProtocolBinding;

public class ChatBinding extends StrictProtocolBinding<ChatController> {
    public ChatBinding(ChatProtocol protocol) {
        super(protocol.getAnnounce(), protocol);
    }
}

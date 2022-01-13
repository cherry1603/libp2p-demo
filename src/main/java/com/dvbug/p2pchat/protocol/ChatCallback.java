/*
 * Copyright (C) Vito
 * By Vito on 2022/1/12 11:09
 */
package com.dvbug.p2pchat.protocol;

import io.libp2p.core.PeerId;
import io.libp2p.core.multiformats.Multiaddr;

public interface ChatCallback {
    void onChatMessage(PeerId peerId, Multiaddr peerAddr, String message);
}

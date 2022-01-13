/*
 * Copyright (C) Vito
 * By Vito on 2022/1/12 12:37
 */
package com.dvbug.p2pchat.chat;

import io.libp2p.core.PeerId;

public interface ChatHandler {
    void onConnected(PeerId peerId);
    void onDisconnected(PeerId peerId);
    void messageReceived(PeerId peerId, String message);
}

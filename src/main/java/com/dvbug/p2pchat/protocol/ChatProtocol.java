/*
 * Copyright (C) Vito
 * By Vito on 2022/1/12 11:10
 */
package com.dvbug.p2pchat.protocol;

import io.libp2p.core.P2PChannel;
import io.libp2p.core.PeerId;
import io.libp2p.core.Stream;
import io.libp2p.core.multiformats.Multiaddr;
import io.libp2p.protocol.ProtocolHandler;
import io.libp2p.protocol.ProtocolMessageHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

public class ChatProtocol extends ProtocolHandler<ChatController> {
    // private final String announce = "/chat/3.1.0";
    private final String announce = "/p2p/chat/3.1.0";
    private final ChatCallback chatCallback;
    private final boolean useLimit;

    public ChatProtocol(ChatCallback chatCallback) {
        super(-1, -1);
        this.useLimit = false;
        this.chatCallback = chatCallback;
    }

    public ChatProtocol(long initiatorTrafficLimit, long responderTrafficLimit, ChatCallback chatCallback) {
        super(initiatorTrafficLimit, responderTrafficLimit);
        this.useLimit = true;
        this.chatCallback = chatCallback;
    }

    public String getAnnounce() {
        return announce;
    }

    @Override
    public CompletableFuture<ChatController> initChannel(P2PChannel ch) {
        if (useLimit) {
            return super.initChannel(ch);
        } else {
            Stream stream = (Stream) ch;
            initProtocolStream(stream);

            if (stream.isInitiator()) {
                return onStartInitiator(stream);
            } else {
                return onStartResponder(stream);
            }
        }
    }

    @Override
    protected CompletableFuture<ChatController> onStartInitiator(Stream stream) {
        return onStart(stream);
    }

    @Override
    protected CompletableFuture<ChatController> onStartResponder(Stream stream) {
        return onStart(stream);
    }

    private CompletableFuture<ChatController> onStart(Stream stream) {
        CompletableFuture<Void> ready = new CompletableFuture<>();
        ChatResponder chatResponder = new ChatResponder(chatCallback, ready);
        stream.pushHandler(chatResponder);
        return ready.thenApply(unused -> chatResponder);
    }

    public static class ChatResponder implements ChatController, ProtocolMessageHandler<ByteBuf> {
        private Stream stream;
        private final ChatCallback chatCallback;
        private final CompletableFuture<Void> ready;

        public ChatResponder(ChatCallback chatCallback, CompletableFuture<Void> ready) {
            this.chatCallback = chatCallback;
            this.ready = ready;
        }

        @Override
        public void fireMessage(Stream stream, Object o) {
            onMessage(stream, (ByteBuf) o);
        }

        @Override
        public void onActivated(Stream stream) {
            this.stream = stream;
            ready.complete(null);
        }

        @Override
        public void onClosed(Stream stream) {

        }

        @Override
        public void onException(Throwable throwable) {

        }

        @Override
        public void onMessage(Stream stream, ByteBuf message) {
            String msgStr = message.toString(Charset.defaultCharset());
            PeerId peerId = stream.remotePeerId();
            Multiaddr multiaddr = new Multiaddr(stream.getConnection().remoteAddress(), peerId);
            chatCallback.onChatMessage(peerId, multiaddr, msgStr);
        }

        @Override
        public void send(String message) {
            byte[] data = message.getBytes(Charset.defaultCharset());
            stream.writeAndFlush(Unpooled.wrappedBuffer(data));
        }
    }
}

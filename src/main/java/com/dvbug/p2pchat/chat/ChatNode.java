/*
 * Copyright (C) Vito
 * By Vito on 2022/1/12 11:06
 */
package com.dvbug.p2pchat.chat;

import com.dvbug.p2pchat.protocol.Chat;
import com.dvbug.p2pchat.protocol.ChatController;
import com.dvbug.p2pchat.util.NetworkUtil;
import io.libp2p.core.Discoverer;
import io.libp2p.core.Host;
import io.libp2p.core.PeerId;
import io.libp2p.core.StreamPromise;
import io.libp2p.core.dsl.HostBuilder;
import io.libp2p.core.multiformats.Multiaddr;
import io.libp2p.discovery.MDnsDiscovery;
import kotlin.Pair;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
public class ChatNode {
    @Data
    @AllArgsConstructor
    private static class Friend {
        private String name;
        private ChatController controller;
    }

    private String aliasName;
    private Discoverer peerFinder;
    private final Set<PeerId> knownNodes = new HashSet<>();
    private final Map<PeerId, Friend> peers = new HashMap<>();
    private final Host chatHost;
    private final InetAddress privateAddress;
    private final Chat chat;
    private final ChatHandler messageHandler = new ChatHandler() {
        @Override
        public void onConnected(PeerId peerId) {
            log.info("Connected to new peer {}", peerId.toBase58());
        }

        @Override
        public void onDisconnected(PeerId peerId) {
            log.info("Peer {} disconnected", peerId.toBase58());
        }

        @Override
        public void messageReceived(PeerId peerId, String message) {
            Friend friend = peers.get(peerId);
            String alias = (null == friend.getName() || friend.getName().isEmpty()) ?
                    peerId.toBase58() : friend.getName();
            log.info("{} > {}", alias, message);
        }
    };

    public ChatNode() {
        this(-1);
    }

    public ChatNode(int port) {
        privateAddress = NetworkUtil.getLocalHostAddress();
        chat = new Chat(this::messageReceived);
        chatHost = new HostBuilder()
                .protocol(chat)
                .listen(String.format("/ip4/%s/tcp/%d", privateAddress.getHostAddress(), Math.max(port, 0)))
                .build();
    }

    @SneakyThrows
    public void conn(String addr) {
        Multiaddr multiaddr = Multiaddr.fromString(addr);
        // StreamPromise<? extends ChatController> dial = chat.dial(chatHost, multiaddr);
        // ArrayList<Multiaddr> addrs = new ArrayList<>();
        // addrs.add(multiaddr);
        Pair<PeerId, Multiaddr> peerIdAndAddr = multiaddr.toPeerIdAndAddr();
        peerFound(peerIdAndAddr.getFirst(), multiaddr);
    }

    public void start() {
        chatHost.start().thenAccept(unused -> {
            chatHost.listenAddresses().forEach(addr -> {
                log.info("Node listened on {}", addr.toString());
            });
            aliasName = chatHost.getPeerId().toBase58();

            peerFinder = new MDnsDiscovery(chatHost,
                    MDnsDiscovery.Companion.getServiceTag(),
                    MDnsDiscovery.Companion.getQueryInterval(),
                    privateAddress);
            peerFinder.getNewPeerFoundListeners().add(info -> {
                log.info("new peer found: {}", info);
                peerFound(info.getPeerId(), info.getAddresses().get(0));
                return null;
            });
            peerFinder.start().thenAccept(unused2 -> {
                log.info("peer finder started");
            });
        });
    }

    public void broadcast(String message) {
        if (message.startsWith("alias ")) {
            aliasName = message.substring(6).trim();
        }

        if (peers.isEmpty()) {
            log.warn("No peers on {}", aliasName);
        }

        peers.forEach((peerId, friend) -> {
            friend.getController().send(message);
        });
    }

    public void send(String peerId, String message) {
        if (message.startsWith("alias ")) {
            aliasName = message.substring(6).trim();
        }

        peers.computeIfPresent(PeerId.fromBase58(peerId), (pid, friend) -> {
            friend.getController().send(message);
            return friend;
        });
    }

    public void stop() {
        peerFinder.stop();
        chatHost.stop();
    }

    private void messageReceived(PeerId peerId, Multiaddr peerAddr, String message) {
        log.debug("received: {}, {} - {}", peerAddr, peerId, message);
        Friend friend = peers.get(peerId);
        if (null == friend) {
            conn(peerAddr.toString());
            friend = peers.get(peerId);
        }
        if ("/who".equals(message)) {
            friend.getController().send("alias " + aliasName);
            return;
        }

        if (message.startsWith("alias ")) {
            peers.computeIfPresent(peerId, (p, f) -> {
                String prevAlias = f.getName();
                String newAlias = message.substring(6).trim();
                if (!newAlias.equals(prevAlias)) {
                    f.setName(newAlias);
                    log.info("changed peer[{}] alias name: {} -> {}", peerId.toBase58(), prevAlias, newAlias);
                }
                return f;
            });
            return;
        }

        messageHandler.messageReceived(peerId, message);
    }

    private void peerFound(PeerId peerId, Multiaddr addr) {
        if (peerId == chatHost.getPeerId() || knownNodes.contains(peerId)) return;

        StreamPromise<? extends ChatController> chat =
                new Chat(this::messageReceived).dial(chatHost, peerId, addr);


        chat.getStream().thenAccept(s -> {
            s.closeFuture().thenAccept(unit -> {
                peers.remove(peerId);
                knownNodes.remove(peerId);
                messageHandler.onDisconnected(peerId);
            });
        });

        knownNodes.add(peerId);
        messageHandler.onConnected(peerId);
        chat.getController().thenAccept(c -> {
            c.send("/who");
            peers.put(peerId, new Friend(peerId.toBase58(), c));
        });

    }

    // private Pair<CompletableFuture<Stream>, CompletableFuture<ChatController>> connectChat(PeerId peerId, Multiaddr addr) {
    //     try {
    //         StreamPromise<? extends ChatController> chat =
    //                 new Chat(this::messageReceived).dial(chatHost, peerId, addr);
    //         return new Pair<>(chat.getStream(), chat.getController());
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         return null;
    //     }
    // }
}

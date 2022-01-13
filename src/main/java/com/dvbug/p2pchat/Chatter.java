/*
 * Copyright (C) Vito
 * By Vito on 2022/1/12 14:14
 */
package com.dvbug.p2pchat;

import com.dvbug.p2pchat.chat.ChatNode;
import lombok.SneakyThrows;

import java.util.Scanner;

public class Chatter {
    @SneakyThrows
    public static void main(String[] args) {
        // /ip4/xxxxx/tcp/43861/ipfs/Qmd3m7JAvkAKyXuAoi2eznxQGZASeXaLg5EyU41sv2hZzx
        // Multiaddr multiaddr = Multiaddr.fromString(args[0]);

        String message;
        ChatNode chat = new ChatNode();
        chat.start();
        Scanner scanner = new Scanner(System.in);
        do {

            message = scanner.nextLine();
            if (null == message || message.isEmpty()) continue;

            if ("bye".equals(message)) break;

            if (message.startsWith("conn ")) {
                chat.conn(message.substring(5));
                continue;
            }

            chat.broadcast(message);
        } while (true);

        chat.stop();
    }
}

/*
 * Copyright (C) Vito
 * By Vito on 2022/1/12 11:37
 */
package com.dvbug.p2pchat.protocol;


public class Chat extends ChatBinding {
    public Chat(ChatCallback callback) {
        super(new ChatProtocol(callback));
    }
}

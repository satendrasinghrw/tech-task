package com.smaato.client;

public enum State {
    INSTANCE;
    volatile boolean serverActive = false;
}

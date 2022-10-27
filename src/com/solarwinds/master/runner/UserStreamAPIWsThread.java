package com.solarwinds.master.runner;

import java.util.Objects;

public class UserStreamAPIWsThread extends Thread {
    private  UserStreamApiWs stream;
    private volatile boolean running = false;

    public UserStreamAPIWsThread(UserStreamApiWs stream) {
        this.stream = (UserStreamApiWs) Objects.requireNonNull(stream, "stream may not be null.");
    }

    public void run() {
        this.running = true;

        while(this.running) {
            try {
                Thread.sleep(300000L);
                this.stream.updateListenKey();
            } catch (InterruptedException var2) {
            }
        }

    }

    public void interrupt() {
        this.running = false;
        super.interrupt();

        try {
            this.join();
        } catch (InterruptedException var2) {
        }

    }
}

package com.solarwinds.master.runner;

import fr.rowlaxx.binanceapi.api.Platform;
import fr.rowlaxx.binanceapi.client.BinanceClient;
import fr.rowlaxx.binanceapi.client.http.BaseEndpoints;
import fr.rowlaxx.binanceapi.client.http.BinanceHttpRequest;
import fr.rowlaxx.binanceapi.client.websocket.StreamAPI;
import fr.rowlaxx.binanceapi.client.websocket.UserStreamAPIThread;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

public class UserStreamApiWs extends StreamAPI {
    private  String endpoint;
    private  BinanceClient client;
    private UserStreamAPIWsThread thread;

    public UserStreamApiWs(String baseUrl, BinanceClient client, String endpoint) {
        super(baseUrl);
        this.endpoint = (String) Objects.requireNonNull(endpoint, "endpoint may not be null.");
        this.client = (BinanceClient)Objects.requireNonNull(client, "client may not be null.");
        if (client.isGuest()) {
            throw new IllegalArgumentException("User streams are not for guest client.");
        } else {
            this.updateListenKey();
            this.initThread();
        }
    }

    public UserStreamApiWs(String baseUrl) {
        super(baseUrl);
    }

    private void initThread() {
        if (this.thread == null) {
            this.thread = new UserStreamAPIWsThread(this);
            this.thread.start();
        }

    }

    @Override
    public void clearEvents() {

    }

    @Override
    protected void onJson(JSONObject jsonObject) {

    }

    public void close() {
        this.thread.interrupt();
        this.thread = null;
        super.close();
    }

    boolean updateListenKey() {
        BinanceHttpRequest request = BinanceHttpRequest.newBuilder(this.endpoint, BinanceHttpRequest.Method.POST).addSignature(false).setBaseEndpoint(this.getBaseUrl()).build();

        try {
            JSONObject response = (JSONObject)this.client.getHttpClient().execute(request);

            System.out.println(response);
            return this.setListenKey(response.getString("listenKey"));
        } catch (IOException var3) {
            return false;
        }
    }

    @Override
    public Platform getApiPlatform() {
        return null;
    }
}

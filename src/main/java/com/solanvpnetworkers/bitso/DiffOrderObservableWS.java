/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.solanvpnetworkers.bitso;

import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import java.net.URI;
import java.util.concurrent.Future;
import org.codehaus.jackson.map.ObjectMapper;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import com.solanvpnetworkers.bitso.entity.DiffOrderResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author solanvp
 */
@WebSocket
public class DiffOrderObservableWS implements ObservableOnSubscribe
{
    
    private ObservableEmitter emitter;
    private WebSocketClient client;
    
    
    public DiffOrderObservableWS()
    {
        String url = "wss://ws.bitso.com";

        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setTrustAll(true); // The magic

        client = new WebSocketClient(sslContextFactory);
        try {
            client.start();
            client.connect(this,URI.create(url));
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @OnWebSocketConnect
    public void onConnect(Session sess)
    {
        System.out.println("Websocket Connected");
        try {
            String subscribeString = "{ \"action\": \"subscribe\", \"book\": \"btc_mxn\", \"type\": \"diff-orders\" }";
            sess.getRemote().sendString(subscribeString);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason)
    {
        System.out.println("Websocket Closed: "+ reason);
    }

    @OnWebSocketError
    public void onError(Throwable cause)
    {
	emitter.onError(cause); 
    }

    @OnWebSocketMessage
    public void onMessage(String msg)
    {
        if(msg.contains("\"action\":\"subscribe\"")){
            System.out.println("Websocket: Subscribed to diff-orders");
            return;
        }
        if(msg.contains("\"type\":\"ka\"")){
            System.out.println("Websocket: ka message");
            return;
        }
        
//        System.out.println("Websocket: diff-order received");
        try {
            ObjectMapper mapper = new ObjectMapper();
            DiffOrderResponse diffOrderResponse = mapper.readValue(msg, DiffOrderResponse.class);
            emitter.onNext(diffOrderResponse);
        } catch (IOException ex) {
            Logger.getLogger(DiffOrderObservableWS.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void subscribe(ObservableEmitter oe) throws Exception {
        this.emitter = oe;
    }
    
    public void disconnect() {
        try {
            client.stop();
        } catch (Exception e) {
        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.solanvpnetworkers.bitso;

import com.solanvpnetworkers.bitso.entity.DiffOrder;
import com.solanvpnetworkers.bitso.entity.OrderBook;
import com.solanvpnetworkers.bitso.entity.DiffOrderResponse;
import com.solanvpnetworkers.bitso.entity.OrderBookResponse;
import com.solanvpnetworkers.bitso.entity.Order;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author solanvp
 */
public class OrderBookManager {
    private Thread managerThread;
    
    private OrderBook orderBook;
    private boolean orderBookInitialized = false;
    private Queue< Map.Entry<DiffOrder,Long> > pedingDiffOrdersQueue;
    private Integer maxOrdersDisplayed;
    
    private final BtcMxnFxApp appWindow;
    
    private DiffOrderObservableWS observableWebsocket;
    private Disposable websocketObserver;
    private Disposable restServiceObserver;
    
    public OrderBookManager(BtcMxnFxApp appWindow, Integer maxOrdersDisplayed){
        this.appWindow = appWindow;
        this.maxOrdersDisplayed = maxOrdersDisplayed;
    }
    
    public void start(){
        managerThread = new Thread(() -> {
            pedingDiffOrdersQueue = new LinkedList<>();
            getOrders();
            getOrderBook();
        });
        managerThread.start();
    }
    
    public void stop(){
        observableWebsocket.disconnect();
        websocketObserver.dispose();
        restServiceObserver.dispose();
        managerThread.interrupt();
    }
    
    public void setMaxOrdersDisplayed(Integer maxOrdersDisplayed){
        this.maxOrdersDisplayed = maxOrdersDisplayed;
        if(orderBookInitialized){
            displayBestAsks();
            displayBestBids();
        }
    }
    
    private void getOrderBook(){
        
        Single<OrderBook> orderBookRestSingle = Single.fromCallable(() -> {
            System.out.println("Fetching order book from bisto public rest service");
            Client client = ClientBuilder.newClient();
            OrderBookResponse response = client.target("https://api.bitso.com/v3/order_book?book=btc_mxn&aggregate=false")
                    .request(MediaType.APPLICATION_JSON)
                    .get(OrderBookResponse.class);
            return response.getPayload();
        });
        
        restServiceObserver = orderBookRestSingle.subscribe(orderBookResponse -> {
            System.out.println("Order book received");
            this.orderBook = orderBookResponse;
            orderBookInitialized = true;
            displayBestBids();
            displayBestAsks();
            
            while (!pedingDiffOrdersQueue.isEmpty()) {  // Process pending DiffOrders
                Map.Entry<DiffOrder,Long> entry = pedingDiffOrdersQueue.remove();
                processDiffOrder(entry.getKey(), entry.getValue());
            }
        });
    }
    
    private void getOrders() {
        observableWebsocket = new DiffOrderObservableWS();
        Observable<DiffOrderResponse> eventObservable = Observable.create(observableWebsocket);
        
        websocketObserver = eventObservable.subscribe(diffOrderResponse -> {
            diffOrderResponse.getPayload().stream()
                    .forEach(diffOrder -> this.processDiffOrder(diffOrder, diffOrderResponse.getSequence()));
        } );
    }
    
    private void processDiffOrder(DiffOrder diffOrder, Long sequence) {
        
        if(! orderBookInitialized){ // Add to queue
            Map.Entry<DiffOrder,Long> entry = new AbstractMap.SimpleEntry<>(diffOrder,sequence);
            pedingDiffOrdersQueue.add(entry);
            return;
        }
        
        if(sequence < orderBook.getSequence()){
            return;
        }
            
        List<Order> orderList;
        if (diffOrder.getT() == 0) {          // Buy  order, update Bid list
            orderList = orderBook.getBids();
        } else {                              // Sell order, update Ask list
            orderList = orderBook.getAsks();
        }

        Optional<Order> optOrder = orderList.stream()
                .filter(order -> order.getOid().equals(diffOrder.getO()))
                .findFirst();

        if (optOrder.isPresent()) {                         // Existing Order
            if (null != diffOrder.getA()) {                 // Update Order
                optOrder.get().setAmount(diffOrder.getA());
                optOrder.get().setPrice(diffOrder.getR());
            } else {                                        // Delete Order
                orderList.remove(optOrder.get());
            }
        } else if (null != diffOrder.getA()) {              // Add Order
            Order o = new Order();
            o.setOid(diffOrder.getO());
            o.setAmount(diffOrder.getA());
            o.setPrice(diffOrder.getR());
            orderList.add(o);
        }

        if (diffOrder.getT() == 0) {    // Buy  order, update Bid list
            displayBestBids();
        } else {                        // Sell order, update Ask list
            displayBestAsks();
        }
    }
       
    private void displayBestBids(){
        List<String> bidsDisplayList = orderBook.getBids().stream()
                .sorted((o1,o2) -> Double.compare(o2.getPrice(), o1.getPrice()))
                .limit(maxOrdersDisplayed)
                .map(o -> String.format("%.8f", o.getAmount())+ " btc \t@ " + o.fomattedPrice() + " mxn")
                .collect(Collectors.toList());
        
        appWindow.updateBestBidsAsksList(bidsDisplayList, true);
    }
    
    
    private void displayBestAsks(){
        List<String> asksDisplayList = orderBook.getAsks().stream()
                .sorted((o1,o2) -> Double.compare(o1.getPrice(), o2.getPrice()))
                .limit(maxOrdersDisplayed)
                .map(o -> String.format("%.8f", o.getAmount()) + " btc \t@ " + o.fomattedPrice() + " mxn")
                .collect(Collectors.toList());
        
        appWindow.updateBestBidsAsksList(asksDisplayList, false);
    }
    
}

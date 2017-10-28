package com.solanvpnetworkers.bitso;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.solanvpnetworkers.bitso.entity.Trade;
import com.solanvpnetworkers.bitso.entity.TradeResponse;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;

public class TradeManager {
    private Thread managerThread;

    private final List<Trade> trades;
    private Trade lastTrade;

    private final BtcMxnFxApp uiWindow;
    
    private Integer placeSellTradeOnMuPticks;
    private Integer placeBuyTradeOnNdOwnticks;
    private final Integer countingTicksStartWindow;
    
    private int uptick = 0, downtick = 0;

    public TradeManager(BtcMxnFxApp uiWindow, Integer M, Integer N) {
        this.uiWindow = uiWindow;
        trades = new LinkedList<>();
        placeSellTradeOnMuPticks = M;
        placeBuyTradeOnNdOwnticks = N;
        
        // Start counting 'ticks' starting from
        int wRef = M > N ? M : N;
        countingTicksStartWindow = 25 - (wRef < 25 ? wRef : 0); // Rest response gives 25 trades most of the time
    }
    
    public void start(){
        managerThread = new Thread(() -> {
            
            while (true) {
                processSampledTrades(getTrades());
                try { Thread.sleep(10000);} catch (InterruptedException e1) {  // Wait 10 seconds before calling again
                    Thread.currentThread().stop();  // Force stop
                } 
            }
            
        });
        managerThread.start();
    }
    
    public void stop(){
        managerThread.interrupt();
    }

    public void setPlaceSellTradeOnMuPticks(Integer placeSellTradeOnMuPticks) {
        this.placeSellTradeOnMuPticks = placeSellTradeOnMuPticks;
    }

    public void setPlaceBuyTradeOnNdOwnticks(Integer placeBuyTradeOnNdOwnticks) {
        this.placeBuyTradeOnNdOwnticks = placeBuyTradeOnNdOwnticks;
    }

    private List<Trade> getTrades() {
        System.out.println("Fetching trades from bisto public rest service");
        Client client = ClientBuilder.newClient();
        String response = client.target("https://api.bitso.com/v3/trades?book=btc_mxn")
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ").create();
        return gson.fromJson(response, TradeResponse.class).getPayload();
    }

    private void processSampledTrades(List<Trade> sampledTrades) {

        Collections.reverse(sampledTrades); // First trades first

        if (trades.isEmpty()) {
            System.out.println("Trades received");
            sampledTrades.stream().forEach(this::addNewTrade);
        } else {

            OptionalInt optinalIndex = IntStream.range(0, sampledTrades.size())
                    .filter(i -> sampledTrades.get(i).equals(lastTrade))
                    .findFirst();

            if (optinalIndex.isPresent()) {
                List<Trade> newTrades = sampledTrades
                        .subList(optinalIndex.getAsInt(), sampledTrades.size()).stream()
                        .skip(1).collect(Collectors.toList());
                if(newTrades.isEmpty()){
                    System.out.println("No new trades");
                } else {
                    System.out.println("New trades received");
                    newTrades.stream().forEach(this::addNewTrade);
                }
            } else {  // Trades were posibly skipped in between calls
                System.out.println("New trades received (posible gap)");
                sampledTrades.stream().forEach(this::addNewTrade);
            }

        }
    }

    private synchronized void addNewTrade(Trade trade) {
        trades.add(trade);
        if (uiWindow != null) {
            //try { Thread.sleep(200);} catch (InterruptedException e1) {}
            TradeType tt = trade.getMaker_side().equals("sell") ? TradeType.SELL : TradeType.BUY;
            uiWindow.addDataToSeries(trade.getCreated_at().getTime(), trade.getPrice(), tt);
            uiWindow.addTextToUiConsole(trade.toString(), tt);
        }

        // Process contrarian strategy
        if (trades.size() > countingTicksStartWindow) { 

            int comp = trade.getPrice().compareTo(lastTrade.getPrice());
            if (comp > 0) {         // Uptick
                uptick++;
                downtick = 0;
            } else if (comp < 0) {  // Downtick
                downtick++;
                uptick = 0;
            }

            if (uptick >= placeSellTradeOnMuPticks) {  // Place mock sell trade
                System.out.println("Placing mock sell trade");
                Trade mockSellTrade = new Trade(true, trade.getPrice());
                trades.add(mockSellTrade);
                uiWindow.addDataToSeries(mockSellTrade.getCreated_at().getTime(), mockSellTrade.getPrice(), TradeType.MOCK_SELL);
                uiWindow.addTextToUiConsole(mockSellTrade.toString(), TradeType.MOCK_SELL);
                uptick = 0;
                downtick = 0;
            }
            if (downtick >= placeBuyTradeOnNdOwnticks) {  // Place mock buy trade
                System.out.println("Placing mock buy trade");
                Trade mockBuyTrade = new Trade(false, trade.getPrice());
                trades.add(mockBuyTrade);
                uiWindow.addDataToSeries(mockBuyTrade.getCreated_at().getTime(), mockBuyTrade.getPrice(), TradeType.MOCK_BUY);
                uiWindow.addTextToUiConsole(mockBuyTrade.toString(), TradeType.MOCK_BUY);
                uptick = 0;
                downtick = 0;
            }
        }

        lastTrade = trade;
    }
    
}

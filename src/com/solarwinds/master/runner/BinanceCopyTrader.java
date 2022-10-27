package com.solarwinds.master.runner;

import com.binance.client.RequestOptions;
import com.binance.client.SyncRequestClient;
import com.binance.client.model.enums.*;
import com.binance.client.model.trade.Order;
import com.binance.client.model.trade.Position;
import com.google.gson.Gson;
import com.solarwinds.master.Config;
import com.solarwinds.master.model.O;
import com.solarwinds.master.model.OrderUpdate;
import fr.rowlaxx.binanceapi.client.BinanceClient;
import fr.rowlaxx.binanceapi.client.BinanceCredenticals;
import fr.rowlaxx.binanceapi.client.websocket.BinanceWebSocket;
import fr.rowlaxx.binanceapi.client.websocket.OnJson;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static fr.rowlaxx.binanceapi.core.OrderStatus.CANCELED;
import static fr.rowlaxx.binanceapi.core.OrderStatus.NEW;
import static fr.rowlaxx.binanceapi.core.futures.trade.FutureOrderTypes.LIMIT;
import static fr.rowlaxx.binanceapi.core.futures.trade.FutureOrderTypes.MARKET;


public class BinanceCopyTrader implements OnJson {

    private static final String EVENT ="e" ;
    private static final String OREDERUPDATEKEY ="ORDER_TRADE_UPDATE" ;
     @SuppressWarnings("unused")
    private Config config;
    private boolean shouldTerminate = false;
    private RequestOptions options;
    private SyncRequestClient traderClient;
    private SyncRequestClient copierClient;
    private ConcurrentHashMap<Long, Long> existingOrders;


    public BinanceCopyTrader(Config config) {
        this.config = config;
        options = new RequestOptions();
        options.setUrl(config.getApiUrl().trim());
        traderClient= SyncRequestClient.create(config.getTraderApikey(), config.getTraderSecretkey(), options);
        copierClient = SyncRequestClient.create(config.getCopierApikeys(), config.getCopierSecretkeys(), options);
        existingOrders = new ConcurrentHashMap<Long, Long>();

    }

    public void run() throws InterruptedException {

        BinanceCredenticals d = new BinanceCredenticals(config.getTraderApikey(),config.getTraderSecretkey());
        BinanceClient c = BinanceClient.create(d);
        UserStreamApiWs ws = new UserStreamApiWs(config.getApiUrl(),c,"/fapi/v1/listenKey");
        BinanceWebSocket webSocket = new BinanceWebSocket(config.getWsUrl(),ws.getListenKey(),this);
        webSocket.subscribe("userTrades");


    }

    @Override
    public void onJson(JSONObject jsonObject) {
        Gson gson4 = new Gson();

        if(jsonObject.get(EVENT).toString().equalsIgnoreCase(OREDERUPDATEKEY)){
            OrderUpdate data=    gson4.fromJson(jsonObject.toString(), OrderUpdate.class);
            String quantity = data.getO().getQ();
            String price = data.getO().getP();
            String symbol = data.getO().getS();
            String buyorsell = data.getO().getSide();
            String tradestatus = data.getO().getxTrade();
            String tradetype = data.getO().getOt();
            Boolean R = data.getO().getR1();
            Long orderId = data.getO().getI1();

            System.out.println("quantity quantity "+quantity);

            OrderSide os = OrderSide.valueOf(buyorsell);
            PositionSide pos = PositionSide.BOTH;
            OrderType ot = OrderType.valueOf(tradetype);
            TimeInForce tm = TimeInForce.valueOf(data.getO().getF());
            List<Position> ps = traderClient.getAccountInformation().getPositions();
            ps = ps.stream().filter(p -> p.getSymbol().equalsIgnoreCase(symbol)).toList();
            BigDecimal leverage = ps.get(0).getLeverage();
            Integer lev = Integer.parseInt(leverage.toString());

            BigDecimal traderMargin = traderClient.getAccountInformation().getTotalWalletBalance();
            BigDecimal traderMargin2 = traderClient.getAccountInformation().getTotalPositionInitialMargin();
            BigDecimal clientMargin = copierClient.getAccountInformation().getTotalWalletBalance();
            System.out.println("trader Wallet Max "+traderMargin);
            System.out.println("client Wallet Max "+clientMargin);

            Double tradeRatio = clientMargin.doubleValue()/traderMargin.doubleValue();
            System.out.println("tradeRatio   "+tradeRatio);

            Double tradeValue = tradeRatio*Double.parseDouble(quantity);

            System.out.println("tradeValue quantity "+tradeValue);


            System.out.println("Calculated quantity "+tradeValue);
            Double newQuantity =   BigDecimal.valueOf(tradeValue)
                    .setScale(3, RoundingMode.HALF_UP)
                    .doubleValue();

            copierClient.changeInitialLeverage(symbol, lev);
            OrderType newOrderType = OrderType.valueOf(tradetype);
            if(data.getO().getOt().equals(OrderType.MARKET.toString()))
            {
                tm=null;
                price="0";
            }


            if(tradestatus.equalsIgnoreCase(NEW.toString()) &&(tradetype.equalsIgnoreCase(LIMIT.toString())||tradetype.equalsIgnoreCase(MARKET.toString()))){
                if(!R) {
                    System.out.println(" Creating New open Position ");
                    System.out.println("quantity"+":"+quantity);
                    System.out.println("price"+":"+price);
                    System.out.println("symbol"+":"+symbol);
                    System.out.println("buyorsell"+":"+buyorsell);
                    System.out.println("tradestatus"+":"+tradestatus);
                    System.out.println("tradetype"+":"+tradetype);



                    try {
                        Order neworder =  copierClient.postOrder(symbol, os, pos, ot, tm,
                                newQuantity + "", price, null, null, null, WorkingType.CONTRACT_PRICE, NewOrderRespType.RESULT);

                        existingOrders.put(orderId,neworder.getOrderId());
                    }catch(Exception e){
                        System.out.println(e.getLocalizedMessage());
                    }
                }
                else {
                    if(R && existingOrders.containsKey(orderId))
                        System.out.println(" Closing a open Position ");
                   try {
                       Order neworder =  copierClient.postOrder(symbol, os, pos, ot, tm,
                               newQuantity + "", null, "true", null, null, WorkingType.CONTRACT_PRICE, NewOrderRespType.RESULT);
                       existingOrders.put(orderId,neworder.getOrderId());
                   }catch (Exception e){
                       System.out.println(e.getLocalizedMessage());
                   }
                }

            } else {
                if(tradestatus.equals(CANCELED.toString()) && existingOrders.containsKey(orderId)){
                    Long ordercancel = existingOrders.get(orderId).longValue();
                    System.out.println(" Closing a open LIMIT Position "+orderId+":"+data.getO().getC());
                   try {
                        copierClient.cancelOrder(symbol, ordercancel,null);
                    }catch (Exception e){
                       System.out.println(e.getLocalizedMessage());
                   }
                } else {
                    System.out.println("Waiting for New Orders from Trader");
                }
            }


        }

    }
}

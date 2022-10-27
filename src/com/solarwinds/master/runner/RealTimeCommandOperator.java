package com.solarwinds.master.runner;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.binance.client.RequestOptions;
import com.binance.client.SyncRequestClient;
import com.binance.client.model.enums.*;
import com.binance.client.model.market.ExchangeInfoEntry;
import com.binance.client.model.market.ExchangeInformation;
import com.binance.client.model.trade.Order;
import com.binance.client.model.trade.Position;
import com.solarwinds.master.Config;

public class RealTimeCommandOperator {

	@SuppressWarnings("unused")
	private Config config;
	private boolean shouldTerminate = false;
	private RequestOptions options;
	private SyncRequestClient traderClient;
	private SyncRequestClient copierClient;
	private HashMap<Long, String> executedOrders;
	@SuppressWarnings("unused")
	private Long startTime;

	public RealTimeCommandOperator(Config config) {
		this.config = config;
		options = new RequestOptions();
		options.setUrl(config.getApiUrl().trim());
		traderClient = SyncRequestClient.create(config.getTraderApikey(), config.getTraderSecretkey(), options);
		copierClient = SyncRequestClient.create(config.getCopierApikeys(), config.getCopierSecretkeys(), options);
		executedOrders = new HashMap<Long, String>();
		startTime = new Date().getTime();

	}

	public void run() throws InterruptedException {

		ExchangeInformation einf = traderClient.getExchangeInformation();
 		while (!shouldTerminate) {
			Thread.sleep(500);
			List<Position> ps = traderClient.getAccountInformation().getPositions();
			List<Order> openOrders = traderClient.getAllOrders(null, null, null, null, 10);
			for (Order o : openOrders) {

				Long orderId = o.getOrderId();
				String side = o.getStatus();
				String symbol = o.getSymbol();
				Long updateTime = o.getUpdateTime();


				ExchangeInfoEntry traderSymbol =einf.getSymbols().stream().filter(exchangeInfoEntry -> exchangeInfoEntry.getSymbol().equalsIgnoreCase(o.getSymbol())).toList().get(0);


					OrderSide neworder = OrderSide.valueOf(o.getSide());
					PositionSide newPos = PositionSide.valueOf(o.getPositionSide());
					OrderType newOrderType = OrderType.valueOf(o.getType());
					BigDecimal amount = o.getOrigQty().round(new MathContext(o.getPrice().scale()));
					BigDecimal price = o.getPrice().round(new MathContext(o.getPrice().scale()));


				BigDecimal ratioCal =  (BigDecimal.valueOf(Double.valueOf(config.getTraderatio())).divide(BigDecimal.TEN.multiply(BigDecimal.TEN)));
				amount = amount.multiply(ratioCal).round(new MathContext(o.getPrice().scale()));

					ps = ps.stream().filter(p -> p.getSymbol().equalsIgnoreCase(symbol)).toList();
					// if cancelled then cancel
					//
					BigDecimal leverage = ps.get(0).getLeverage();
					Integer lev = Integer.parseInt(leverage.toString());
					copierClient.changeInitialLeverage(symbol, lev);
					
					TimeInForce tm = TimeInForce.GTC;
					
					if(o.getType().equals(OrderType.MARKET.toString()))
						{
							tm=null;
							price=null;
						}

					try {
						if (!executedOrders.containsKey(o.getUpdateTime())  
								&& !o.getStatus().equals("CANCELED")
								
								&& o.getUpdateTime()-2000 > startTime
								) {

							System.out.println(o);
							System.out.println(traderSymbol);
							System.out.println("Executing New Order Recieved for Copying " + orderId + ":" + side + ":" + symbol +":"+amount.toString()+":"+ price.toString()
									+ ":" + updateTime);
							copierClient.postOrder(symbol, neworder, newPos, newOrderType, tm,
									amount.toString(), price.toString(), null, null, null, WorkingType.valueOf(o.getWorkingType()), NewOrderRespType.RESULT);

							executedOrders.put(o.getUpdateTime(), o.getStatus());
						}
					}catch(Exception e) {
						System.out.println(e.getLocalizedMessage());
					}
	//Order[clientOrderId=web_1fVKnGWzosHxvQBnpWHm,cumQuote=4672.5001,executedQty=0.199,orderId=3082478850,
				// origQty=0.199,price=0,reduceOnly=true,side=BUY,positionSide=BOTH,status=FILLED,stopPrice=0,
				// symbol=BTCUSDT,timeInForce=GTC,type=MARKET,updateTime=1658534878820,workingType=CONTRACT_PRICE]
	//ExchangeInfoEntry[symbol=BTCUSDT,status=TRADING,maintMarginPercent=2.5,requiredMarginPercent=5,
				// baseAsset=BTC,quoteAsset=USDT,pricePrecision=2,quantityPrecision=3,baseAssetPrecision=8,quotePrecision=8,
				// orderTypes=[LIMIT, MARKET, STOP, STOP_MARKET, TAKE_PROFIT, TAKE_PROFIT_MARKET, TRAILING_STOP_MARKET],
				// timeInForce=[LIMIT, MARKET, STOP, STOP_MARKET, TAKE_PROFIT, TAKE_PROFIT_MARKET, TRAILING_STOP_MARKET],
				// filters=[[{minPrice=227.20}, {maxPrice=704424}, {filterType=PRICE_FILTER}, {tickSize=0.10}], [{stepSize=0.001}, {filterType=LOT_SIZE}, {maxQty=1000}, {minQty=0.001}], [{stepSize=0.001}, {filterType=MARKET_LOT_SIZE}, {maxQty=1000}, {minQty=0.001}], [{limit=200}, {filterType=MAX_NUM_ORDERS}], [{limit=10}, {filterType=MAX_NUM_ALGO_ORDERS}], [{notional=10}, {filterType=MIN_NOTIONAL}], [{multiplierDown=0.5454}, {multiplierUp=1.1000}, {multiplierDecimal=4}, {filterType=PERCENT_PRICE}]]]

 
			}

		}
	}

}

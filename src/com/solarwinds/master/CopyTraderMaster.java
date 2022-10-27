package com.solarwinds.master;

import com.solarwinds.master.runner.BinanceCopyTrader;

public class CopyTraderMaster {

	private static ConfigLoader configLoader;

	public static void main(String[] args) throws InterruptedException  {

		configLoader = new ConfigLoader(args[0]);
		//RealTimeCommandOperator realTimeCommandOperator = new RealTimeCommandOperator(configLoader.config);
	//	realTimeCommandOperator.run();

		BinanceCopyTrader realTimeCommandOperator = new BinanceCopyTrader(configLoader.config);
		realTimeCommandOperator.run();

	}
}

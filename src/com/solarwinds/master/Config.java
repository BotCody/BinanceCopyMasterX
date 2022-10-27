package com.solarwinds.master;

public class Config {
	public static final int THREAD_NUM = 5;
	private String traderApikey;
	private String traderSecretkey;
	private String tradetype;
	private String trademode;
	private int traderatio;
	private int tradefixed;
	private String basecurrency;
	private String copierApikeys;
	private String copierSecretkeys;	
	private String apiUrl;

	private String wsUrl;
	public String getTraderApikey() {
		return traderApikey;
	}

	public void setTraderApikey(String traderApikey) {
		this.traderApikey = traderApikey;
	}

	public String getTraderSecretkey() {
		return traderSecretkey;
	}

	public void setTraderSecretkey(String traderSecretkey) {
		this.traderSecretkey = traderSecretkey;
	}

	public String getTradetype() {
		return tradetype;
	}

	public void setTradetype(String tradetype) {
		this.tradetype = tradetype;
	}

	public String getTrademode() {
		return trademode;
	}

	public void setTrademode(String trademode) {
		this.trademode = trademode;
	}

	public int getTraderatio() {
		return traderatio;
	}

	public void setTraderatio(int traderatio) {
		this.traderatio = traderatio;
	}

	public int getTradefixed() {
		return tradefixed;
	}

	public void setTradefixed(int tradefixed) {
		this.tradefixed = tradefixed;
	}

	public String getBasecurrency() {
		return basecurrency;
	}

	public void setBasecurrency(String basecurrency) {
		this.basecurrency = basecurrency;
	}

	public String getCopierApikeys() {
		return copierApikeys;
	}

	public void setCopierApikeys(String copierApikeys) {
		this.copierApikeys = copierApikeys;
	}

	public String getCopierSecretkeys() {
		return copierSecretkeys;
	}

	public void setCopierSecretkeys(String copierSecretkeys) {
		this.copierSecretkeys = copierSecretkeys;
	}

	public String getApiUrl() {
		return apiUrl;
	}

	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

	public String getWsUrl() {
		return wsUrl;
	}

	public void setWsUrl(String wsUrl) {
		this.wsUrl = wsUrl;
	}
}

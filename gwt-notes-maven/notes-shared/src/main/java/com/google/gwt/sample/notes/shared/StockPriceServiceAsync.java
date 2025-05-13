package com.google.gwt.sample.notes.shared;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface StockPriceServiceAsync {

    void getPrices(String[] symbols, AsyncCallback<StockPrice[]> callback);

    void loadSymbols(AsyncCallback<String[]> callback);

	void saveSymbols(String[] symbols, AsyncCallback<Void> callback);
}

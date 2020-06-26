package ch.hsr.appquest.coincollector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CoinManager {

    private List<Coin> coinList;

    public CoinManager() {

        this.coinList = new ArrayList<>();
    }

    public void addToList(Coin c) {

        this.coinList.add(c);
    }

    public int[] retrieveFoundIds() {

        int[] intArr = new int[coinList.size()];

        for (int i = 0; i < intArr.length; i++) {

            intArr[i] = coinList.get(i).major;
        }

        return intArr;
    }

    public JSONObject logJson() throws JSONException {
        JSONObject logEntry = new JSONObject();
        logEntry.put("task", "Muenzensammler");
        JSONArray jCoins = new JSONArray();
        for (Coin coin : coinList) {
            JSONObject jCoin = new JSONObject();
            jCoin.put("major", coin.major);
            jCoin.put("minor", coin.minor);
            jCoins.put(jCoin);
        }
        logEntry.put("coins", jCoins);
        return logEntry;
    }
}

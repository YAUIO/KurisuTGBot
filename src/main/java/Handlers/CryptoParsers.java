package Handlers;

import com.google.gson.*;
import okhttp3.*;

import java.io.*;
import java.util.*;

public class CryptoParsers {
    //private static final String API_URL = "https://streaming.bitquery.io/eap";
    //private static final String API_KEY = csvParser.readSQLKey();

    public static HashSet<String> extractMintAddresses(String json) {
        HashSet<String> addresses = new HashSet<>();
        try {
            JsonArray jsonArray = JsonParser.parseString(json).getAsJsonObject().get("data").getAsJsonObject().get("Solana").getAsJsonObject().getAsJsonArray("BalanceUpdates");

            for (JsonElement element : jsonArray) {
                JsonObject balanceUpdate = element.getAsJsonObject().getAsJsonObject("BalanceUpdate");
                if (balanceUpdate != null) {
                    JsonObject currency = balanceUpdate.getAsJsonObject("Currency");
                    if (currency != null && currency.has("MintAddress")) {
                        addresses.add(currency.get("MintAddress").getAsString());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(json + " failed to extract mint...");
        }
        return addresses;
    }

    public static HashMap<String, String> parseCoins(List<String> coinAddreses) {
        HashMap<String, String> coins = new HashMap<>();
        for (String coin : coinAddreses) {
            coins.put(coin, getCoinCreator(coin));
            synchronized (Thread.currentThread()) {
                try {
                    Thread.currentThread().wait(400);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return coins;
    }

    public static String getCoinCreator(String addr) {
        try {
            String apiUrl = "https://frontend-api.pump.fun/coins/" + addr;

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(apiUrl).get().build();

            Response response = client.newCall(request).execute();
            if (response.body() != null) {
                try {
                    return JsonParser.parseString(response.body().string()).getAsJsonObject().get("creator").getAsString();
                } catch (Exception e) {
                    System.out.print("Probably ratelimited or vpn issue: " + response.code() + " error code.");
                }
            }
        } catch (Exception e) {
            System.out.println("Couldn't get coin creator due to error: " + e.getClass() + " " + e.getMessage());
        }

        return null;
    }

    public static HashSet<String> getWalletCoins(String wallet) throws IOException {
        HashSet<String> coins = new HashSet<>();

        try {
            String apiUrl = "https://frontend-api-v3.pump.fun/balances/" + wallet + "?limit=50&offset=0&minBalance=-1";

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(apiUrl).get().build();

            Response response = client.newCall(request).execute();
            if (response.body() != null) {
                try {
                    JsonArray jsoncoins = JsonParser.parseString(response.body().string()).getAsJsonArray();

                    for (JsonElement el : jsoncoins) {
                        coins.add(el.getAsJsonObject().get("mint").getAsString());
                    }

                    if (coins.isEmpty()) {
                        System.out.println("Coins list for wallet " + wallet + " is empty. Response " + response);
                    }

                } catch (Exception _) {
                    System.out.print("Probably ratelimited or vpn issue: " + response.code() + " error code.");
                }
            }

            return coins;
        } catch (Exception e) {
            System.out.println("Couldn't get Coins from Wallet due to error: " + e.getClass() + " " + e.getMessage());
        }

        return coins;
    }

    public static void writeHashMapToFile(Map<String, HashMap<String, String>> map) {
        try {
            File f = new File("data.csv");

            if (f.exists()) f.delete();
            f.createNewFile();
            FileOutputStream fos = new FileOutputStream(f);
            PrintWriter writer = new PrintWriter(fos, true);

            for (String key : map.keySet()) {
                writer.println(key);
                if (map.get(key) != null) {
                    for (String k : map.get(key).keySet()) {
                        writer.print(k + " ");
                        writer.println(map.get(key).get(k));
                    }
                }
                writer.println();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static HashMap<String, HashMap<String, String>> readHashMapFromFile() throws RuntimeException {
        HashMap<String, HashMap<String, String>> map = new HashMap<>();
        try {
            File f = new File("data.csv");
            if (!f.exists()) throw new RuntimeException("data.csv does not exist...skipping reading data");
            FileInputStream fis = new FileInputStream(f);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));

            while (br.ready()) {
                String key = br.readLine();
                if (key != null && !key.isEmpty() && !key.isBlank()) {
                    map.put(key, new HashMap<>());

                    for (String buf = br.readLine(); buf != null && !buf.isEmpty() && !buf.isBlank(); buf = br.readLine()) {
                        String[] split = buf.split(" ");

                        if (split.length == 2 && split[1].equals("null")) {
                            split[1] = null;
                        }

                        if (split.length == 1) {
                            split = new String[]{split[0], null};
                        }

                        map.get(key).put(split[0], split[1]);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error while reading data: " + e.getMessage());
        }

        return map;
    }
}

package Handlers;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.IOException;
import java.util.*;

public class CreationListener extends Thread {
    private HashMap<String, HashMap<String, String>> addresses = new HashMap<>();
    private TelegramClient tgclient;
    public Map<String, HashMap<String, String>> viewport;
    public final HashSet<Long> chat_id;
    public final int pollingRate;

    public CreationListener(TelegramClient tgclient, HashSet<Long> chat_id) {
        pollingRate = csvParser.readPollingRate();
        this.tgclient = tgclient;
        this.chat_id = chat_id;
        viewport = Collections.synchronizedMap(addresses);
        try {
            viewport = CryptoParsers.readHashMapFromFile();
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Listener initialized");
        this.start();
    }

    @Override
    public void run() {
        while (true) {
            synchronized (viewport) {
                for (String wallet : viewport.keySet()) {
                    try {
                        HashSet<String> coins = CryptoParsers.getWalletCoins(wallet);
                        viewport.computeIfAbsent(wallet, k -> new HashMap<>());
                        if (coins != null && !viewport.get(wallet).keySet().containsAll(coins)) {
                            coins.removeAll(viewport.get(wallet).keySet());
                            if (coins.size() > 4) { //to not check old values
                                for (String coin : coins) {
                                    viewport.get(wallet).put(coin, null);
                                }
                            } else {
                                for (String coin : coins) {
                                    String creator = CryptoParsers.getCoinCreator(coin);
                                    if (creator != null && creator.equals(wallet)) {
                                        String msg = "New coin detected: https://neo.bullx.io/terminal?chainId=1399811149&address=" + coin + " \n https://pump.fun/coin/" + coin + " \n";
                                        viewport.get(wallet).put(coin, creator);
                                        synchronized (chat_id) {
                                            for (Long id : chat_id) {
                                                SendMessage message = SendMessage // Create a message object
                                                        .builder()
                                                        .chatId(id)
                                                        .text(msg)
                                                        .build();
                                                try {
                                                    tgclient.execute(message); // Sending our message object to user
                                                } catch (TelegramApiException e) {
                                                    System.out.println("Exception while reporting new token " + e.getClass() + ": " + e.getMessage() + "\n " + msg);
                                                }
                                            }
                                        }
                                    }
                                    viewport.get(wallet).put(coin, creator);
                                }
                            }
                        }
                    } catch (IOException e) {
                        System.out.println("Exception while getting coins from a wallet: " + e.getClass() + " " + e.getMessage());
                    }
                }
            }

            synchronized (this) {
                try {
                    this.wait(pollingRate);
                } catch (InterruptedException _) {
                    CryptoParsers.writeHashMapToFile(viewport);
                    System.exit(0);
                }
            }
        }
    }

    public void sync() {
        //synchronized (viewport) {
        //    System.out.println(viewport.keySet());
        //    System.out.println("|||||||||||||||||||||");
        //    System.out.println(viewport.keySet());
        //}
    }
}

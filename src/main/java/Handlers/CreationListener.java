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
    public volatile long chat_id;

    public CreationListener(TelegramClient tgclient) {
        this.tgclient = tgclient;
        chat_id = csvParser.readChatKey();
        try {
            addresses = CryptoParsers.readHashMapFromFile();
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
        viewport = Collections.synchronizedMap(addresses);
        System.out.println("Listener initialized");
        this.start();
    }

    @Override
    public void run() {
        while (true) {
            synchronized (addresses) {
                for (String wallet : addresses.keySet()) {
                    try {
                        HashSet<String> coins = CryptoParsers.getWalletCoins(wallet);
                        addresses.computeIfAbsent(wallet, k -> new HashMap<>());
                        if (coins != null && !addresses.get(wallet).keySet().containsAll(coins)) {
                            coins.removeAll(addresses.get(wallet).keySet());
                            if (coins.size() > 2) { //to not check old values
                                for (String coin : coins) {
                                    addresses.get(wallet).put(coin, null);
                                }
                            } else {
                                for (String coin : coins) {
                                    String creator = CryptoParsers.getCoinCreator(coin);
                                    if (creator != null && creator.equals(wallet)) {
                                        String msg = "New coin detected: https://neo.bullx.io/terminal?chainId=1399811149&address=" + coin + " \n https://pump.fun/profile/" + coin + " \n";
                                        addresses.get(wallet).put(coin, creator);
                                        SendMessage message = SendMessage // Create a message object
                                                .builder()
                                                .chatId(chat_id)
                                                .text(msg)
                                                .build();
                                        try {
                                            tgclient.execute(message); // Sending our message object to user
                                        } catch (TelegramApiException e) {
                                            System.out.println("Exception while reporting new token " + e.getClass() + ": " + e.getMessage() + "\n " + msg);
                                        }
                                    }
                                    addresses.get(wallet).put(coin, creator);
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
                    this.wait(100);
                } catch (InterruptedException _) {
                    CryptoParsers.writeHashMapToFile(addresses);
                    System.exit(0);
                }
            }
        }
    }

    public void sync () {
        //synchronized (addresses) {
        //    System.out.println(addresses.keySet());
        //    System.out.println("|||||||||||||||||||||");
        //    System.out.println(viewport.keySet());
        //}
    }
}

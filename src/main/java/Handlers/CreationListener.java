package Handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.*;
import java.util.concurrent.CompletionStage;

public class CreationListener {
    private static final String WS_URL = "wss://pumpportal.fun/api/data";

    private HashSet<String> addresses = new HashSet<>();
    private HashSet<String> favorite = new HashSet<>();
    private TelegramClient tgclient;
    public Set<String> viewport;
    public Set<String> favorite_viewport;
    public final HashSet<Long> chat_id;
    private WebSocket webSocket;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CreationListener(TelegramClient tgclient, HashSet<Long> chat_id) {
        webSocket = connectWebSocket();
        this.tgclient = tgclient;
        this.chat_id = chat_id;
        viewport = Collections.synchronizedSet(addresses);
        favorite_viewport = Collections.synchronizedSet(favorite);
        try {
            CryptoParsers.readHashSetFromFile(viewport, favorite_viewport);
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
        }
        //alert("test", "4EXk21LNQakX4djoeEjpAXpqbKnF56chy6fUCaeaiXXu", true);
        System.out.println("Listener initialized");
    }

    public void close() {
        webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Closing connection");
    }

    private WebSocket connectWebSocket() {
        return HttpClient.newHttpClient().newWebSocketBuilder()
                .buildAsync(URI.create(WS_URL), new WebSocket.Listener() {
                    @Override
                    public void onOpen(WebSocket webSocket) {
                        System.out.println("Connected to WebSocket: " + WS_URL);
                        // Subscribe to new token events
                        sendJson(webSocket, Map.of("method", "subscribeNewToken"));
                        webSocket.request(1);
                    }

                    @Override
                    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                        // Parse JSON
                        try {
                            JsonNode jsonNode = objectMapper.readTree(data.toString());
                            JsonNode trader = jsonNode.get("traderPublicKey");
                            if (trader != null) {
                                String creator = trader.asText();
                                if (viewport.contains(creator)) {
                                    String coin = jsonNode.get("mint").asText();
                                    if (coin != null) {
                                        alert(coin, creator, favorite_viewport.contains(coin));
                                    } else {
                                        System.err.println("Couldn't alert because coin is null");
                                    }
                                }
                            } else {
                                System.err.println(jsonNode.asText());
                            }
                        } catch (Exception e) {
                            System.err.println("Error parsing JSON: " + e.getMessage());
                        }
                        webSocket.request(1);
                        return WebSocket.Listener.super.onText(webSocket, data, last);
                    }

                    @Override
                    public void onError(WebSocket webSocket, Throwable error) {
                        System.err.println("WebSocket error: " + error.getMessage());
                    }

                    @Override
                    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                        System.out.println("WebSocket closed: " + reason);
                        return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
                    }
                }).join();
    }

    private void sendJson(WebSocket webSocket, Map<String, Object> payload) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(payload);
            webSocket.sendText(jsonMessage, true);
            System.out.println("Sent: " + jsonMessage);
        } catch (Exception e) {
            System.err.println("Failed to send JSON: " + e.getMessage());
        }
    }

    private void alert (String coin, String creator, boolean favorite) {
        //long startTime = System.currentTimeMillis();
        String msg = "";
        HashSet<Message> sentMsgs = new HashSet<>();
        if (favorite) {
            msg = "\uD83D\uDEA8\uD83D\uDEA8\uD83D\uDEA8 ";
        }
        msg += "New coin detected: https://neo.bullx.io/terminal?chainId=1399811149&address=" + coin + " \n https://pump.fun/coin/" + coin + " \n";
        synchronized (chat_id) {
            SendMessage message = SendMessage // Create a message object
                    .builder()
                    .text(msg)
                    .chatId(chat_id.iterator().next())
                    .build();
            for (Long id : chat_id) {
                message.setChatId(id);
                try {
                    sentMsgs.add(tgclient.execute(message)); // Sending our message object to user
                } catch (TelegramApiException e) {
                    System.err.println("Exception while reporting new token " + e.getClass() + ": " + e.getMessage() + "\n " + msg);
                }
            }

            //System.out.println("Reported text in " + (System.currentTimeMillis()-startTime) + "ms");

            File img = null;
            try {
                img = ImageGenerator.generateImage(creator);
            } catch (IOException e) {
                System.err.println("Couldn't generate image due to error: " + e.getMessage());
            }

            if (img != null) {
                EditMessageMedia emm = EditMessageMedia
                        .builder()
                        .media(new InputMediaPhoto(img, "coins.png"))
                        .chatId(chat_id.iterator().next())
                        .messageId(sentMsgs.iterator().next().getMessageId())
                        .build();

                EditMessageCaption emc = EditMessageCaption
                        .builder()
                        .caption(msg)
                        .chatId(chat_id.iterator().next())
                        .messageId(sentMsgs.iterator().next().getMessageId())
                        .build();

                for (Message m : sentMsgs) {
                    emm.setMessageId(m.getMessageId());
                    emm.setChatId(m.getChatId());
                    emc.setMessageId(m.getMessageId());
                    emc.setChatId(m.getChatId());
                    try {
                        tgclient.execute(emm);
                        tgclient.execute(emc);
                    } catch (TelegramApiException e) {
                        System.err.println("Exception while reporting new token " + e.getClass() + ": " + e.getMessage() + "\n " + msg);
                    }
                }
            }

            //System.out.println("Reported full msg in " + (System.currentTimeMillis()-startTime) + "ms");
        }
    }
}

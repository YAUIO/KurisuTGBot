import Handlers.CommandParser;
import Handlers.CreationListener;
import Handlers.csvParser;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class CryptoBot implements LongPollingSingleThreadUpdateConsumer {
    protected TelegramClient telegramClient = new OkHttpTelegramClient(csvParser.readTGKey());
    protected HashSet<Long> clients = csvParser.readChatKey();
    protected CreationListener listener = new CreationListener(telegramClient, clients);

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message_text = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();
            if (!clients.contains(chat_id)) {
                clients.add(chat_id);
                csvParser.writeChatKey(clients);
            }

            List<String> parsedResponse = new ArrayList<>();
            String response = CommandParser.parse(message_text, listener);

            do {
                int split = Math.min(response.length(), 4000);
                split = response.indexOf('\n',split-40);
                parsedResponse.add(response.substring(0, split));
                response = response.substring(split);
            } while (response.length() >= 4000);
            parsedResponse.add(response);

            for (Long chatid : clients) {
                for (String msg : parsedResponse) {
                    SendMessage message = SendMessage // Create a message object
                            .builder()
                            .chatId(chatid)
                            .text(msg)
                            .build();
                    try {
                        telegramClient.execute(message); // Sending our message object to user
                    } catch (TelegramApiException e) {
                        System.out.println("Exception " + e.getClass() + ": " + e.getMessage());
                    }
                }
            }
        }
    }
}

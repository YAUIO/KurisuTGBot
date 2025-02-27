import Handlers.CommandParser;
import Handlers.CreationListener;
import Handlers.csvParser;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.HashSet;

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

            String response = CommandParser.parse(message_text, listener);

            for (Long chatid : clients) {
                SendMessage message = SendMessage // Create a message object
                        .builder()
                        .chatId(chatid)
                        .text(response)
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

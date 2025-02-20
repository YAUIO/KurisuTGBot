import Handlers.CommandParser;
import Handlers.CreationListener;
import Handlers.csvParser;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class CryptoBot implements LongPollingSingleThreadUpdateConsumer {
    protected TelegramClient telegramClient = new OkHttpTelegramClient(csvParser.readTGKey());
    protected CreationListener creationListener = new CreationListener(telegramClient);

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message_text = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();
            if (creationListener.chat_id == 0) {
                creationListener.chat_id = chat_id;
                csvParser.writeChatKey(chat_id);
            }

            SendMessage message = SendMessage // Create a message object
                    .builder()
                    .chatId(chat_id)
                    .text(CommandParser.parse(message_text, creationListener))
                    .build();
            try {
                telegramClient.execute(message); // Sending our message object to user
            } catch (TelegramApiException e) {
                System.out.println("Exception " + e.getClass() + ": " + e.getMessage());
            }
        }
    }
}

import Handlers.CreationListener;
import Handlers.csvParser;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Main {
    public static void main(String[] args) {
        try {
            TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
            String botToken = csvParser.readTGKey();
            botsApplication.registerBot(botToken, new CryptoBot());
            System.out.println(CreationListener.getTime() + " :: kurisubot successfully started!");
        } catch (TelegramApiException e) {
            System.out.println(e.getClass() + ": " + e.getMessage());
        }
    }
}

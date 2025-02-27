package Handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class ImageGenerator {
    private static final String API_URL = "https://frontend-api-v3.pump.fun/coins/user-created-coins/COINS_ENDPOINT?offset=0&limit=10&includeNsfw=false"; // Replace with actual API URL
    private static final int WIDTH = 520;

    private static JsonNode fetchLastCoins(String username) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(API_URL.replace("COINS_ENDPOINT", username))
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Failed to fetch data");

            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readTree(response.body().string()); // Get coins array
        }
    }

    public static File generateImage(String username) throws IOException {
        JsonNode coins = null;
        try {
            coins = fetchLastCoins(username);
        } catch (Exception e) {
            System.err.println("Error while generating image: " + e.getMessage());
        }

        if (coins == null) return null;

        int HEIGHT = 40 + (100 * Math.min(coins.size(), 5));

        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Set background
        g2d.setColor(new Color(20, 20, 20)); // Dark theme
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // Font settings
        Font titleFont = new Font("Arial", Font.BOLD, 16);
        Font textFont = new Font("Arial", Font.PLAIN, 14);
        g2d.setFont(titleFont);
        g2d.setColor(Color.WHITE);

        int y = 30;
        g2d.drawString("Last 5 Coins Created", 20, y);
        y += 30;

        for (int i = 0; i < Math.min(coins.size(), 5); i++) {
            JsonNode coin = coins.get(i);
            String name = coin.get("name").asText();
            String symbol = coin.get("symbol").asText();
            String marketCap = String.format("$%.2fK", coin.get("usd_market_cap").asDouble() / 1000);
            int replies = coin.get("reply_count").asInt();
            String creator = coin.get("creator").asText();
            String imageUrl = coin.get("image_uri").asText();

            // Draw coin image
            try {
                BufferedImage coinImage = ImageIO.read(new URL(imageUrl));
                g2d.drawImage(coinImage, 20, y, 64, 64, null);
            } catch (Exception e) {
                System.out.println("Failed to load image: " + imageUrl);
            }

            // Draw coin info
            g2d.setFont(titleFont);
            g2d.drawString(name + " (" + symbol + ")", 100, y + 20);
            g2d.setFont(textFont);
            g2d.setColor(Color.GREEN);
            g2d.drawString("Market Cap: " + marketCap, 100, y + 40);
            g2d.setColor(Color.CYAN);
            g2d.drawString("Replies: " + replies, 100, y + 60);
            g2d.setColor(Color.GRAY);
            g2d.drawString("Creator: " + creator, 100, y + 80);

            y += 90; // Move down for the next coin
        }

        // Save image
        g2d.dispose();
        File outputFile = new File("coins.png");
        ImageIO.write(image, "png", outputFile);
        return outputFile;
    }
}

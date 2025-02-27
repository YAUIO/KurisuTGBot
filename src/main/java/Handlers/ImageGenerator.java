package Handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverInfo;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.time.Duration;

public class ImageGenerator {
    static {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        driver = new ChromeDriver(options);
    }
    private static final int WIDTH = 520;
    public static final WebDriver driver;

    private static JsonNode fetchLastCoins(String username) throws Exception {
        driver.get("https://frontend-api-v3.pump.fun/coins/user-created-coins/" + username + "?offset=0&limit=10&includeNsfw=false");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement preTag = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("pre")));

        String jsonResponse = preTag.getText();


        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readTree(jsonResponse);
        } catch (Exception e) {
            throw new Exception("\nParse error with response: " + jsonResponse,e);
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

        int HEIGHT = 60 + (100 * Math.min(coins.size(), 5));

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

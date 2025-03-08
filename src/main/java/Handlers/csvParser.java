package Handlers;

import java.io.*;
import java.util.*;

public class csvParser {
    public static String readTGKey() {
        try {
            FileInputStream fis = new FileInputStream("token.csv");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String key = br.readLine();
            if (key == null || key.isBlank() || key.isEmpty()) throw new Exception();
            return key;
        } catch (Exception e) {
            System.out.println("Could not read api key from token.csv. Api key should be in form 12345:asdfgh");
            throw new RuntimeException(e);
        }
    }

    public static String readProxyServer() {
        try {
            File f = new File("proxy.csv");
            if (!f.exists()) f.createNewFile();
            FileInputStream fis = new FileInputStream("proxy.csv");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String key = br.readLine();
            if (key == null || key.isBlank() || key.isEmpty()) throw new Exception();
            return key;
        } catch (Exception e) {
            System.out.println("Could not read api key from token.csv. Second line of the file should be solana API token https://account.bitquery.io/user/api_v2/access_tokens");
            throw new RuntimeException(e);
        }
    }

    public static HashSet<Long> readChatKey() {
        try {
            FileInputStream fis = new FileInputStream("chatkey.csv");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            HashSet<Long> keys = new HashSet<>();
            while (br.ready()) {
                try {
                    keys.add(Long.parseLong(br.readLine()));
                } catch (Exception _) {}
            }
            if (keys.isEmpty()) throw new RuntimeException("Keys list is empty");
            return keys;
        } catch (Exception e) {
            System.out.println("Could not read chatid...");
        }
        return new HashSet<>();
    }

    public static void writeChatKey(Collection<Long> l) {
        try {
            File f = new File("chatkey.csv");
            if (!f.exists()) {
                f.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream("chatkey.csv");
            PrintWriter pw = new PrintWriter(fos, true);
            for (Long lon : l) {
                pw.println(lon);
            }
        } catch (Exception e) {
            System.out.println("Could not write chatKey " + e.getClass() + " " + e.getMessage());
        }
    }

    public static int readPollingRate() {
        try {
            FileInputStream fis = new FileInputStream("config.csv");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            return Integer.parseInt(br.readLine());
        } catch (Exception e) {
            System.out.println("Could not read polling rate...");
        }
        File f = new File("config.csv");
        if (!f.exists()) {
            try {
                f.createNewFile();
                writePollingRate(100);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return 0;
    }

    public static void writePollingRate(int l) {
        try {
            File f = new File("config.csv");
            if (f.exists()) {
                f.delete();
            }
            f.createNewFile();
            FileOutputStream fos = new FileOutputStream(f);
            PrintWriter pw = new PrintWriter(fos, true);
            pw.println(l);
        } catch (Exception e) {
            System.out.println("Could not write polling rate " + e.getClass() + " " + e.getMessage());
        }
    }
}

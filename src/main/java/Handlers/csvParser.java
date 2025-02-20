package Handlers;

import java.io.*;

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

    public static String readSQLKey() {
        try {
            FileInputStream fis = new FileInputStream("token.csv");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            br.readLine();
            String key = br.readLine();
            if (key == null || key.isBlank() || key.isEmpty()) throw new Exception();
            return key;
        } catch (Exception e) {
            System.out.println("Could not read api key from token.csv. Second line of the file should be solana API token https://account.bitquery.io/user/api_v2/access_tokens");
            throw new RuntimeException(e);
        }
    }

    public static long readChatKey() {
        try {
            FileInputStream fis = new FileInputStream("chatkey.csv");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            return Long.parseLong(br.readLine());
        } catch (Exception e) {
            System.out.println("Could not read chatid...");
        }
        return 0;
    }

    public static void writeChatKey(long l) {
        try {
            File f = new File("chatkey.csv");
            if (f.exists()) {
                f.delete();
            }
            f.createNewFile();
            FileOutputStream fos = new FileOutputStream("chatkey.csv");
            PrintWriter pw = new PrintWriter(fos, true);
            pw.println(l);
        } catch (Exception e) {
            System.out.println("Could not write chatKey " + e.getClass() + " " + e.getMessage());
        }
    }
}

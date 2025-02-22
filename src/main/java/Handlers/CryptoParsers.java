package Handlers;

import java.io.*;
import java.util.*;

public class CryptoParsers {
    public static void writeHashSetToFile(Set<String> set) {
        try {
            File f = new File("data.csv");
            if (f.exists()) f.delete();
            f.createNewFile();
            FileOutputStream fos = new FileOutputStream(f);
            PrintWriter writer = new PrintWriter(fos, true);

            for (String key : set) {
                writer.println(key);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static HashSet<String> readHashSetFromFile() throws RuntimeException {
        HashSet<String> set = new HashSet<>();
        try {
            File f = new File("data.csv");
            if (!f.exists()) throw new RuntimeException("data.csv does not exist...skipping reading data");
            FileInputStream fis = new FileInputStream(f);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));

            while (br.ready()) {
                set.add(br.readLine());
            }
        } catch (Exception e) {
            System.out.println("Error while reading data: " + e.getMessage());
        }

        return set;
    }
}

package Handlers;

import java.io.*;
import java.util.*;

public class CryptoParsers {
    public static void writeHashSetToFile(Set<String> set, Set<String> favs) {
        try {
            File f = new File("data.csv");
            if (f.exists()) f.delete();
            f.createNewFile();
            FileOutputStream fos = new FileOutputStream(f);
            PrintWriter writer = new PrintWriter(fos, true);

            for (String key : set) {
                writer.print(key);
                writer.print(" ");
                if (favs.contains(key)) {
                    writer.print("+");
                } else {
                    writer.print("-");
                }
                writer.println();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void readHashSetFromFile(Set<String> addrs, Set<String> favs) throws RuntimeException {
        try {
            File f = new File("data.csv");
            if (!f.exists()) throw new RuntimeException("data.csv does not exist...skipping reading data");
            FileInputStream fis = new FileInputStream(f);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));

            while (br.ready()) {
                String line = br.readLine();
                String[] split = line.split(" ");
                if (split.length == 2) {
                    addrs.add(split[0]);
                    if (split[1].equals("+")) {
                        favs.add(split[0]);
                    }
                } else {
                    addrs.add(line);
                }
            }
        } catch (Exception e) {
            System.err.println("Error while reading data: " + e.getMessage());
        }
    }
}

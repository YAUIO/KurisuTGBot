package Handlers;

import java.util.HashMap;

public class CommandParser {
    public static String parse (String msg, CreationListener cl) {
        System.out.println(msg);
        String[] split = null;
        if (!msg.startsWith("/")) {
            msg = "/help";
        } else {
            split = msg.split(" ");
            msg = split[0];
        }
        switch (msg) {
            case "/osu" -> {
                return "osu is cool";
            }

            case "/help" -> {
                return """
                        Help
                        /add
                        /rm
                        /list
                        /stop
                        """;
            }

            case "/add" -> {
                try {
                    add(split[1], cl);
                    return "Added " + split[1] + " successfully!";
                } catch (Exception e) {
                    return "Error while adding "  + split[1] + ": " + e.getClass() + " " + e.getMessage();
                }
            }

            case "/rm" -> {
                try {
                    rm(split[1], cl);
                    return "Removed " + split[1] + " successfully!";
                } catch (Exception e) {
                    return "Error while removing "  + split[1] +  ": " + e.getClass() + " " + e.getMessage();
                }
            }

            case "/list" -> {
                return list(cl);
            }

            case "/stop" -> {
                stop(cl);
                return "Stopping...";
            }
        }

        return "Error. Incorrect input";
    }

    private static void add(String s, CreationListener cl) throws Exception{
        synchronized (cl.viewport) {
            cl.viewport.put(s, new HashMap<>());
            cl.sync();
        }
    }

    private static void rm(String s, CreationListener cl) throws Exception {
        synchronized (cl.viewport) {
            cl.viewport.remove(s);
            cl.sync();
        }
    }

    private static String list(CreationListener cl){
        int i = 1;
        StringBuilder ret = new StringBuilder();
        ret.append("List of wallets: \n");
        synchronized (cl.viewport) {
            if (cl.viewport != null) {
                for (String wallet : cl.viewport.keySet()) {
                    ret.append("Wallet ").append(i).append(": ").append(wallet).append("\n");
                    i++;
                }
            }
        }
        return ret.toString();
    }

    private static void stop(CreationListener cl) {
        synchronized (cl) {
            cl.interrupt();
        }
    }
}

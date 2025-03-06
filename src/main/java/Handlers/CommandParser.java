package Handlers;

import java.util.Iterator;

public class CommandParser {
    public static String parse(String msg, CreationListener cl) {
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
                        /add <addr>
                        /rm <addr/number in "/list">
                        /fav <addr/number in "/list">
                        /unfav <addr/number in "/list fav">
                        /list
                        /list fav
                        /stop
                        """;
            }

            case "/add" -> {
                try {
                    add(split[1], cl);
                    return "Added " + split[1] + " successfully!";
                } catch (Exception e) {
                    return "Error while adding " + split[1] + ": " + e.getClass() + " " + e.getMessage();
                }
            }

            case "/rm" -> {
                try {
                    rm(split[1], cl);
                    return "Removed " + split[1] + " successfully!";
                } catch (Exception e) {
                    return "Error while removing " + split[1] + ": " + e.getClass() + " " + e.getMessage();
                }
            }

            case "/fav" -> {
                try {
                    addfav(split[1], cl);
                    return "Added " + split[1] + " to favorites successfully!";
                } catch (Exception e) {
                    return "Error while adding " + split[1] + ": " + e.getClass() + " " + e.getMessage();
                }
            }

            case "/unfav" -> {
                try {
                    rmfav(split[1], cl);
                    return "Removed " + split[1] + "  from favorites successfully!";
                } catch (Exception e) {
                    return "Error while removing " + split[1] + ": " + e.getClass() + " " + e.getMessage();
                }
            }

            case "/list" -> {
                if (split.length == 2) {
                    if (split[1].equals("fav")) {
                        return listFav(cl);
                    }
                }
                return list(cl);
            }

            case "/stop" -> {
                stop(cl);
                return "Stopping...";
            }

            case "/test" -> {
                cl.alert("test", "3PpKrMdZMUg2Pj4wyyhY2AD311LFg8xkxNbmMfRTMN8i", true);
                return "Testing...";
            }
            case "/start" -> {
                return "Welcome to the new version of KurisuBOT!";
            }
        }

        return "Error. Incorrect input";
    }

    private static void addfav(String s, CreationListener cl) throws Exception {
        if (s.length() < 4) {
            Iterator<String> it = cl.viewport.iterator();
            for (int i = 0; i < Integer.parseInt(s) - 1; i++) {
                it.next();
            }
            s = it.next();
        }
        cl.viewport.add(s);
        cl.favorite_viewport.add(s);
    }

    private static void rmfav(String s, CreationListener cl) throws Exception {
        if (s.length() < 4) {
            Iterator<String> it = cl.favorite_viewport.iterator();
            for (int i = 0; i < Integer.parseInt(s) - 1; i++) {
                it.next();
            }
            s = it.next();
        }
        cl.favorite_viewport.remove(s);
    }

    private static void add(String s, CreationListener cl) throws Exception {
        cl.viewport.add(s);
    }

    private static void rm(String s, CreationListener cl) throws Exception {
        if (s.length() < 4) {
            Iterator<String> it = cl.viewport.iterator();
            for (int i = 0; i < Integer.parseInt(s) - 1; i++) {
                it.next();
            }
            s = it.next();
        }
        cl.viewport.remove(s);
        cl.favorite_viewport.remove(s);
    }

    private static String list(CreationListener cl) {
        int i = 1;
        StringBuilder ret = new StringBuilder();
        ret.append("List of wallets: \n");
        if (cl.viewport != null) {
            for (String wallet : cl.viewport) {
                ret.append("Wallet ").append(i).append(": ").append(wallet).append("\n");
                i++;
            }
        }
        return ret.toString();
    }

    private static String listFav(CreationListener cl) {
        int i = 1;
        StringBuilder ret = new StringBuilder();
        ret.append("List of favorite wallets: \n");
        if (cl.favorite_viewport != null) {
            for (String wallet : cl.favorite_viewport) {
                ret.append("Wallet ").append(i).append(": ").append(wallet).append("\n");
                i++;
            }
        }
        return ret.toString();
    }

    private static void stop(CreationListener cl) {
        cl.close();
        CryptoParsers.writeHashSetToFile(cl.viewport, cl.favorite_viewport);
        System.exit(0);
    }
}

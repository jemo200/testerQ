package testerq.client;

import java.io.Console;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import testerq.core.Inventory;
import testerq.core.Item;
import testerq.core.Member;
import testerq.core.MemberStats;
import testerq.core.QuestLog;
import testerq.core.Skill;
import testerq.core.ZoneMessage;
import testerq.core.map.MapManager;

public class Main {
    static String name;
    static int viewWidth = 40;
    static int viewHeight = viewWidth / 2;
    static Console console = System.console();
    //static PrintWriter mOut;
    static ObjectOutputStream mOOut;
    static boolean signedIn = false;
    
    private static HashMap<String, Member> members = new HashMap<String, Member>();
    private static LinkedList<String> events = new LinkedList<>();
    public static MapManager mapManager = new MapManager();
    private static String[][] currentMap;
    
    static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win");

    public static void main(String[] args) {
        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        boolean running = true;
        
        if (console == null) {
            System.out.println("Console is not supported");
            System.exit(1);
        }
        if(IS_WINDOWS) {
            try {
                new ProcessBuilder("cmd", "/c", "chcp 65001").inheritIO().start().waitFor();
                System.setOut(new PrintStream(System.out, true, "UTF-8"));
            } catch (UnsupportedEncodingException | InterruptedException ex) {
            } catch (IOException ex) {
                System.out.println("fail");
            }
        }

        try {
            SocketFactory ssf = SSLSocketFactory.getDefault();
            SSLSocket clientSocket = (SSLSocket)ssf.createSocket(hostName, portNumber);
            clientSocket.setEnabledCipherSuites(clientSocket.getEnabledCipherSuites());
            clientSocket.setEnabledProtocols(clientSocket.getEnabledProtocols());
            clientSocket.startHandshake();
            
            //Socket clientSocket = new Socket(hostName, portNumber);
            //PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            ObjectOutputStream oOut = new ObjectOutputStream(clientSocket.getOutputStream());
            //mOut = out;
            mOOut = oOut;
            //BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            ObjectInputStream oIn = new ObjectInputStream(clientSocket.getInputStream());
            new InsHandler(oIn).start();
            String fromUser = "";

            while (running) {
                if (signedIn) {
                    Thread.sleep(300);
                    fromUser = console.readLine("action: ");
                    if (!fromUser.equals("exit")) {
                        if (fromUser.split(" ")[0].compareTo("config") == 0) {
                            if (fromUser.split(" ").length == 3) {
                                if (fromUser.split(" ")[1].compareTo("vwidth") == 0) {
                                    viewWidth = Integer.parseInt(fromUser.split(" ")[2]);
                                    clearMap();
                                    printMap();
                                } else if (fromUser.split(" ")[1].compareTo("vheight") == 0) {
                                    viewHeight = Integer.parseInt(fromUser.split(" ")[2]);
                                    clearMap();
                                    printMap();
                                }
                            } else {
                                events.push("Config command format (config types: vwidth, vheight): config <config type> <number>");
                                events.push("Default vwidth: 40, vheight 20");
                                clearMap();
                                printMap();
                            }
                        } else if (fromUser.length() >= 3 && fromUser.substring(0, 3).equals("say")) {
                            if (fromUser.length() > 4) {
                                ZoneMessage zMsg = new ZoneMessage();
                                zMsg.msg = fromUser.substring(4, fromUser.length());
                                oOut.writeObject(zMsg);
                            } else {
                                events.push("Say command format: say <message>");
                                clearMap();
                                printMap();
                            }
                        } else if (fromUser.split(" ")[0].compareTo("inspect") == 0){
                            String[] actions = fromUser.split(" ");
                            if (actions.length == 2) {
                                if (actions[1].compareTo("north") == 0 || actions[1].compareTo("n") == 0 || actions[1].compareTo("up") == 0) {
                                    //x - 1
                                    String cellSprite = currentMap[members.get(name).getPositionX() - 1][members.get(name).getPositionY()];
                                    handleInspect(cellSprite);
                                } else if (actions[1].compareTo("south") == 0 || actions[1].compareTo("s") == 0 ||actions[1].compareTo("down") == 0) {
                                    //x + 1
                                    String cellSprite = currentMap[members.get(name).getPositionX() + 1][members.get(name).getPositionY()];
                                    handleInspect(cellSprite);
                                } else if (actions[1].compareTo("west") == 0 || actions[1].compareTo("w") == 0 || actions[1].compareTo("left") == 0) {
                                    //y - 1
                                    String cellSprite = currentMap[members.get(name).getPositionX()][members.get(name).getPositionY() - 1];
                                    handleInspect(cellSprite);
                                } else if (actions[1].compareTo("east") == 0 || actions[1].compareTo("e") == 0 || actions[1].compareTo("right") == 0) {
                                    //y + 1
                                    String cellSprite = currentMap[members.get(name).getPositionX()][members.get(name).getPositionY() + 1];
                                    handleInspect(cellSprite);
                                }
                            } else {
                                events.push("Inspect command format: inspect <direction>");
                                clearMap();
                                printMap();
                            }
                        } else if (fromUser.split(" ")[0].compareTo("interact") == 0){
                            String[] actions = fromUser.split(" ");
                            if (actions.length == 2) {
                                if (actions[1].compareTo("north") == 0 || actions[1].compareTo("n") == 0 || actions[1].compareTo("up") == 0) {
                                    //x - 1
                                    String cellSprite = currentMap[members.get(name).getPositionX() - 1][members.get(name).getPositionY()];
                                    handleInteract(cellSprite);
                                } else if (actions[1].compareTo("south") == 0 || actions[1].compareTo("s") == 0 ||actions[1].compareTo("down") == 0) {
                                    //x + 1
                                    String cellSprite = currentMap[members.get(name).getPositionX() + 1][members.get(name).getPositionY()];
                                    handleInteract(cellSprite);
                                } else if (actions[1].compareTo("west") == 0 || actions[1].compareTo("w") == 0 || actions[1].compareTo("left") == 0) {
                                    //y - 1
                                    String cellSprite = currentMap[members.get(name).getPositionX()][members.get(name).getPositionY() - 1];
                                    handleInteract(cellSprite);
                                } else if (actions[1].compareTo("east") == 0 || actions[1].compareTo("e") == 0 || actions[1].compareTo("right") == 0) {
                                    //y + 1
                                    String cellSprite = currentMap[members.get(name).getPositionX()][members.get(name).getPositionY() + 1];
                                    handleInteract(cellSprite);
                                }
                            } else {
                                events.push("Interact command format: interact <direction>");
                                clearMap();
                                printMap();
                            }
                        } else if (fromUser.split(" ")[0].compareTo("list") == 0){
                            String[] actions = fromUser.split(" ");
                            if (actions.length == 2) {
                                String invent = "";
                                if (actions[1].compareTo("inventory") == 0 || actions[1].compareTo("inv") == 0) {
                                    if(members.get(name).inventory != null) {
                                        for (Map.Entry<String, Item> entry : members.get(name).inventory.inventory.entrySet()) {
                                            invent += entry.getKey() + ": " + entry.getValue().quantity + ", ";
                                        }
                                        events.push("Inventory -> " + invent);
                                    }
                                    clearMap();
                                    printMap();  
                                } else if (actions[1].compareTo("stats") == 0) {
                                    if(members.get(name).stats != null) {
                                        for (Map.Entry<String, Skill> entry : members.get(name).stats.stats.entrySet()) {
                                            invent += entry.getKey() + ": " + entry.getValue().getLevel() + ", ";
                                        }
                                        events.push("Member stats -> " + invent);
                                    }
                                    clearMap();
                                    printMap();  
                                } else if (actions[1].compareTo("experience") == 0 || actions[1].compareTo("exp") == 0) {
                                    if(members.get(name).stats != null) {
                                        for (Map.Entry<String, Skill> entry : members.get(name).stats.stats.entrySet()) {
                                            invent += entry.getKey() + ": " + entry.getValue().getExp() + ", ";
                                        }
                                        events.push("Skills experience -> " + invent);
                                    }
                                    clearMap();
                                    printMap();  
                                }
                            } else if (actions.length == 3) {
                                oOut.writeObject("command::" + fromUser);
                            } else {
                                events.push("List command format (types include: inventory, stats, experience): list <type> OR list stats <member name>");
                                clearMap();
                                printMap();
                            }
                        } else if (fromUser.split(" ")[0].compareTo("help") == 0){
                            events.push("Available commands are: config, say, inspect, interact, list, chop, trade, trading. Enter these for usage info.");
                            events.push("To exit play enter: exit");
                            clearMap();
                            printMap();
                            
                        } else {
                            oOut.writeObject("command::" + fromUser);
                        }
                    } else {
                        goodBye();
                        oOut.close();
                        oIn.close();
                        clientSocket.close();
                        running = false;
                    }
                } else {
                    Thread.sleep(500);
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        } catch (InterruptedException ex) {
            
        }
        
    }
    
    private static void handlePositionSync(String syncData) {
        String[] chunks = syncData.split(Pattern.quote("||"));
        String nameIn = chunks[0];
        int cellX = Integer.parseInt(chunks[1]);
        int cellY = Integer.parseInt(chunks[2]);
        members.get(nameIn).setPositionX(cellX);
        members.get(nameIn).setPositionY(cellY);
        clearMap();
        printMap();
    }

    private static void handleSpawn(String spawnData) {
        String[] chunks = spawnData.split(Pattern.quote("++"));
        String nameIn = chunks[0];
        int x = Integer.parseInt(chunks[1]);
        int y = Integer.parseInt(chunks[2]);
        String avatar = chunks[3];
        String worldZone = chunks[4];
        Member mem = new Member(nameIn, x, y, avatar, worldZone);
        mem.inventory = new Inventory();
        mem.questLog = new QuestLog();
        if(nameIn.equals(Main.name)) {
            currentMap = mapManager.zones.get(mem.getWorldZone()).zone2DArray;
        }
        members.put(nameIn, mem);
        clearMap();
        printMap();
        
    }
    
    private static void handleUnSpawn(String spawnData) {
        
        String[] chunks = spawnData.split(Pattern.quote("--"));
        String nameIn = chunks[0];
        members.remove(nameIn);
        if (nameIn.equals(name)) {
            members.clear();
        }
        if (!nameIn.equals(name)) {
            clearMap();
            printMap();
        }
        
    }
    
    private static void printMap() {
        clearConsole();
        //PRINT MAP
        System.out.println(System.getProperty("line.separator"));
        Member member = members.get(name);
        for(int i=member.getPositionX() - (viewHeight / 2); i<(member.getPositionX() - (viewHeight / 2)) + viewHeight; i++) {
            String line = "";
            for(int j=member.getPositionY() - (viewWidth / 2); j< (member.getPositionY() - (viewWidth / 2)) + viewWidth; j++) {
                if (i < 0 || j < 0 || i >= currentMap.length || j >= currentMap[0].length) {
                    line += "@";
                } else {
                    line += currentMap[i][j];
                }
            }
            System.out.println(line);
        }
        //PRINT ACTIVE TAB
        String line = "";
        if(events.size() > 10) {
            for (int i = 9; i >= 0; i--) {
                System.out.println(events.get(i));
            }
        } else {
            if (events.size() != 0) {
                for (int i = events.size() - 1; i >= 0; i--) {
                    System.out.println(events.get(i));
                }
            }
            for (int i = 0; i < 10 - events.size(); i++) {
                System.out.println("~");
            }
        }
        System.out.println(line);
    }
    
    public final static void clearConsole() {
        try {
        if (System.getProperty("os.name").contains("Windows"))
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        else
            Runtime.getRuntime().exec("clear");
        } catch (IOException | InterruptedException ex) {}
    }
    
    public static void clearMap() {
        for(int i=0; i<currentMap.length; i++) {
            for(int j=0; j<currentMap[i].length; j++) {
                if("\u263A\u263B\u2665\u2666\u2663\u2660".contains(currentMap[i][j])) {
                    currentMap[i][j] = " ";
                }
            }
        }
        for (Map.Entry<String, Member> entry : members.entrySet()) {
            if (entry.getValue().getWorldZone().equals(members.get(name).getWorldZone())) {
                currentMap[entry.getValue().getPositionX()][entry.getValue().getPositionY()] = entry.getValue().getSprite();
            }
        }
    }
    
    public static void welcome() {
        try {
            String fromUser = Main.console.readLine("Welcome! 1)Login 2)New User");
            if (fromUser.compareTo("1") == 0) {
                Main.mOOut.writeObject("login");
            } else if (fromUser.compareTo("2") == 0) {
                Main.mOOut.writeObject("new");
            } else {

            }
        } catch (IOException ex) {
        }
    }
    
    public static void getUsername() {
        String fromUser = Main.console.readLine("Username: ");
        Main.name = fromUser;
        try {
            //Main.mOut.println(fromUser);
            Main.mOOut.writeObject(fromUser);
        } catch (IOException ex) {
        }
    }
    
    public static void getUserPw() {
        char[] fromUser = Main.console.readPassword("Password: ");
        try {
            Main.mOOut.writeObject(new String(fromUser));
        } catch (IOException ex) {
        }
    }
    
    public static void getAvatar() {
        try {
            String fromUser = Main.console.readLine("Choose Avatar 1)\u263A 2)\u263B 3)\u2665 4)\u2666 5)\u2663 6)\u2660");
            if (fromUser.compareTo("1") == 0) {
                //Main.mOut.println("\u263A");
                Main.mOOut.writeObject("\u263A");
            } else if (fromUser.compareTo("2") == 0) {
                //Main.mOut.println("\u263B");
                Main.mOOut.writeObject("\u263B");
            } else if (fromUser.compareTo("3") == 0) {
                //Main.mOut.println("\u2665");
                Main.mOOut.writeObject("\u2665");
            } else if (fromUser.compareTo("4") == 0) {
                //Main.mOut.println("\u2666");
                Main.mOOut.writeObject("\u2666");
            } else if (fromUser.compareTo("5") == 0) {
                //Main.mOut.println("\u2663");
                Main.mOOut.writeObject("\u2663");
            } else if (fromUser.compareTo("6") == 0) {
                //Main.mOut.println("\u2660");
                Main.mOOut.writeObject("\u2660");
            }
        } catch (IOException ex) {
        }
    }
    
    private static void goodBye() {
        System.out.println("|---\\ \\   / |----");
        System.out.println("|   |  \\ /  |    ");
        System.out.println("|  /    |   |____");
        System.out.println("|/--    |   |    ");
        System.out.println("|   |   |   |    ");
        System.out.println("|__/    |   |____");
    }
    
    private static void handleInspect(String sprite) {
        if (sprite.equals("#")) {
            events.push("A healthy tree.");
        } else if (sprite.equals("^")) {
            events.push("A minable rock formation.");
        } else if (sprite.equals("|")) {
            events.push("A wall.");
        } else if (sprite.equals("$")) {
            events.push("A great king.");
        } else if (sprite.equals("R")) {
            events.push("A citizen.");
        } else if (sprite.equals("M")) {
            events.push("Royal Smelter Macari");
        }
        clearMap();
        printMap();
    }
    
    private static void handleInteract(String sprite) {
        System.out.println(members.get(name));
            System.out.println(members.get(name).questLog);
            System.out.println(members.get(name).questLog.questLog);
            System.out.println(members.get(name).questLog.questLog.get("quest1"));
        if (sprite.equals("$")) {
            events.push("(King Leroy) Greetings. Our lands are in need of a hero like yourself! Find Macari and help him for a reward.");
            
            if(members.get(name).questLog.questLog.get("quest1").currentTask == 0) {
                members.get(name).questLog.questLog.get("quest1").currentTask = 1;
            }
        } else if (sprite.equals("R")) {
            events.push("(citizen) Good day to you.");
        } else if (sprite.equals("M")) {
            if(members.get(name).questLog.questLog.get("quest1").complete == true) {
                events.push("(Macari) Hope you are putting that pickaxe to good use!");
            } else if ((members.get(name).inventory.inventory.containsKey("treelogs") && members.get(name).inventory.inventory.get("treelogs").quantity >= 20) && members.get(name).questLog.questLog.get("quest1").currentTask == 2) {
                events.push("(Macari) Thank you for help. Here is a small reward. **Quest Complete**");
                members.get(name).inventory.inventory.get("treelogs").quantity -= 20;
                members.get(name).inventory.inventory.put("pickaxe", members.get(name).questLog.questLog.get("quest1").reward.get(0));
                events.push("You recieved a sturdy pickaxe.");
                members.get(name).questLog.questLog.get("quest1").complete = true;
            } else if ((members.get(name).inventory.inventory.get("treelogs") == null || members.get(name).inventory.inventory.get("treelogs").quantity < 20) && members.get(name).questLog.questLog.get("quest1").currentTask == 2) {
                events.push("(Macari) Could you bring me those 20 wood logs?");
            } else if (members.get(name).questLog.questLog.get("quest1").currentTask == 1) {
            events.push("(Macari) Hero! I an in need of wood to fuel my smelting operation. Could you bring me 20 wood logs?");
            members.get(name).questLog.questLog.get("quest1").currentTask = 2;
            } else {
                events.push("(Macari) Have you ever been in the castle? The King loves visitors!");
            }
        }
        try {
            Main.mOOut.writeObject(members.get(name).questLog);
            Main.mOOut.reset();
            Main.mOOut.writeObject(members.get(name).inventory);
            Main.mOOut.reset();
        } catch (IOException ex) {
        }
        clearMap();
        printMap();
    }
    
    private static class InsHandler extends Thread{
        private ObjectInputStream ins;
        
        InsHandler(ObjectInputStream ins) {
            this.ins = ins;
        }
        
        String input;
        Object objIn;
        public void run() {
            
            try {
                while((input = (String)ins.readObject()) != null) {
                    if (input.compareTo("Welcome") == 0) {
                        Main.welcome();
                    } else if (input.compareTo("Username:") == 0) {
                        Main.getUsername();
                    } else if (input.compareTo("Password:") == 0) {
                        Main.getUserPw();
                    } else if (input.compareTo("Avatar") == 0) {
                        Main.getAvatar();
                    } else if (input.compareTo("Logged In Successfully") == 0) {
                        break;
                    } else if (input.compareTo("Account Created Successfully") == 0) {
                        break;
                    }
                }
                Main.signedIn = true;
                events.push("Available commands are: config, say, inspect, interact, list, chop, trade, trading. Enter these for usage info.");
                events.push("To exit play, enter: exit");
                events.push("For help enter: help");
                
                while ((objIn = ins.readObject()) != null) {
                    if (objIn.getClass().toString().contains("java.lang.String")) {
                        input = (String)objIn;
                        if (input.equals("Bye.")) {
                            break;
                        } else if (input.length() > 3 && input.substring(0, 4).equals("+_)(")) {
                            events.push(input.substring(5, input.length()));
                            clearMap();
                            printMap();
                        }else if (input.split(Pattern.quote("||")).length > 1) {
                            Main.handlePositionSync(input);
                        } else if (input.split(Pattern.quote("++")).length > 1) {
                            Main.handleSpawn(input);
                        } else if (input.split(Pattern.quote("--")).length >= 1) {
                            Main.handleUnSpawn(input);
                        }
                    } else if (objIn.getClass().toString().contains("testerq.core.Inventory")){
                            Inventory inv = (Inventory)objIn;
                            members.get(name).inventory = inv;
                        
                    } else if (objIn.getClass().toString().contains("testerq.core.QuestLog")) {
                        QuestLog ql = (QuestLog)objIn;
                        members.get(name).questLog = ql;
                    } else if (objIn.getClass().toString().contains("testerq.core.MemberStats")) {
                        MemberStats stats = (MemberStats)objIn;
                        members.get(name).stats = stats;
                    }
                }
                
            } catch (IOException ex) {
            } catch (ClassNotFoundException ex) {
            }
        }
    }
     
}

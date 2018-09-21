/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testerq.client;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import testerq.core.Member;
import testerq.core.map.MapManager;
/**
 *
 * @author emoj
 */
public class Main {
    static String name;
    static int viewWidth = 40;
    static int viewHeight = viewWidth / 2;
    static Console console = System.console();
    static PrintWriter mOut;
    static boolean signedIn = false;
    
    private static HashMap<String, Member> members = new HashMap<String, Member>();
    private static LinkedList<String> events = new LinkedList<>();
    public static MapManager mapManager = new MapManager();
    private static String[][] currentMap;
    
    static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win");

    public static void main(String[] args) {
        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        
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
            Socket clientSocket = new Socket(hostName, portNumber);
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            mOut = out;
            BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
            
            new InsHandler(in).start();
            
            String fromUser = "";

            while (true) {
                if (signedIn) {
                    Thread.sleep(300);
                    fromUser = console.readLine("action: ");
                    if (fromUser != null) {
                        if (fromUser.split(" ")[0].compareTo("config") != 0) {
                            out.println("command::" + fromUser);
                        } else {
                            if (fromUser.split(" ")[1].compareTo("vwidth") == 0) {
                                viewWidth = Integer.parseInt(fromUser.split(" ")[2]);
                                clearMap();
                                printMap();
                            } else if (fromUser.split(" ")[1].compareTo("vheight") == 0) {
                                viewHeight = Integer.parseInt(fromUser.split(" ")[2]);
                                clearMap();
                                printMap();
                            }
                        }
                        //clearMap();
                        //printMap();
                    } else {
                        out.close();
                        in.close();
                        clientSocket.close();
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
        String name = chunks[0];
        int cellX = Integer.parseInt(chunks[1]);
        int cellY = Integer.parseInt(chunks[2]);
        members.get(name).cellX = cellX;
        members.get(name).cellY = cellY;
        clearMap();
        printMap();
    }

    private static void handleSpawn(String spawnData) {
        String[] chunks = spawnData.split(Pattern.quote("++"));
        String name = chunks[0];
        int x = Integer.parseInt(chunks[1]);
        int y = Integer.parseInt(chunks[2]);
        String avatar = chunks[3];
        String worldZone = chunks[4];
        Member mem = new Member(name, x, y, avatar);
        mem.worldZone = worldZone;
        if(name.equals(Main.name)) {
            currentMap = mapManager.zones.get(mem.worldZone).zone2DArray;
        }
        members.put(name, mem);
        clearMap();
        printMap();
        
    }
    
    private static void handleUnSpawn(String spawnData) {
        String[] chunks = spawnData.split(Pattern.quote("--"));
        String name = chunks[0];

        members.remove(name);
        clearMap();
        printMap();
        
    }
    
    private static void printMap() {
        //clearConsole();
        //PRINT MAP
        System.out.println(System.getProperty("line.separator"));
        Member player = members.get(name);
        for(int i=player.cellX - (viewHeight / 2); i<(player.cellX - (viewHeight / 2)) + viewHeight; i++) {
            String line = "";
            for(int j=player.cellY - (viewWidth / 2); j< (player.cellY - (viewWidth / 2)) + viewWidth; j++) {
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
            currentMap[entry.getValue().cellX][entry.getValue().cellY] = entry.getValue().avatar;
        }
    }
    
    public static void getUsername() {
        String fromUser = Main.console.readLine("Username: ");
        Main.name = fromUser;
        Main.mOut.println(fromUser);
    }
    
    public static void getAvatar() {
        String fromUser = Main.console.readLine("Choose Avatar 1)\u263A 2)\u263B 3)\u2665 4)\u2666 5)\u2663 6)\u2660");
        if (fromUser.compareTo("1") == 0) {
            Main.mOut.println("\u263A");
        } else if (fromUser.compareTo("2") == 0) {
            Main.mOut.println("\u263B");
        } else if (fromUser.compareTo("3") == 0) {
            Main.mOut.println("\u2665");
        } else if (fromUser.compareTo("4") == 0) {
            Main.mOut.println("\u2666");
        } else if (fromUser.compareTo("5") == 0) {
            Main.mOut.println("\u2663");
        } else if (fromUser.compareTo("6") == 0) {
            Main.mOut.println("\u2660");
        }
    }
    
    private static class InsHandler extends Thread{
        private BufferedReader ins;
        
        InsHandler(BufferedReader ins) {
            this.ins = ins;
        }
        String input;
        public void run() {
            
            try {
                while((input = ins.readLine()) != null) {
                    if (input.compareTo("Username:") == 0) {
                        Main.getUsername();
                    } else if (input.compareTo("Logged In Successfully") == 0) {
                        break;
                    }
                }
                
                while((input = ins.readLine()) != null) {
                    if (input.compareTo("Avatar") == 0) {
                        Main.getAvatar();
                    } else if (input.compareTo("Avatar Chosen") == 0) {
                        Main.signedIn = true;
                        break;
                    }
                }
                
                while ((input = ins.readLine()) != null) {
                    events.push(input);
                    if (input.equals("Bye.")) {
                        break;
                    } else if (input.split(Pattern.quote("||")).length > 1) {
                        Main.handlePositionSync(input);
                    } else if (input.split(Pattern.quote("++")).length > 1) {
                        Main.handleSpawn(input);
                    } else if (input.split(Pattern.quote("--")).length > 1) {
                        Main.handleUnSpawn(input);
                    }
                }
                
            } catch (IOException ex) {
            }
        }
    }
    
}

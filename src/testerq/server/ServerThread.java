
package testerq.server;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.spec.SecretKeySpec;
import testerq.core.Direction;
import testerq.core.Inventory;
import testerq.core.Item;
import testerq.core.MapTransfer;
import testerq.core.Member;
import testerq.core.MemberAccount;
import testerq.core.MemberSave;
import testerq.core.MemberStats;
import testerq.core.Quest;
import testerq.core.QuestLog;
import testerq.core.Skill;
import testerq.core.Task;
import testerq.core.ZoneMessage;

public class ServerThread extends Thread {
    private static final byte[] key = "MyDifficultPassw".getBytes();
    private static final String transformation = "AES/ECB/PKCS5Padding";
    Member member;
    int gridX = 0;
    int gridY = 0;
    int cellX = 15;
    int cellY = 15;
    int nextCellX = 0;
    int nextCellY = 0;
    Object input = null;
    String inpStr = "";
    ObjectInputStream oIn = null;
    ObjectOutputStream oOut = null;

    Socket s = null;

    public ServerThread(Socket s) {
        this.s = s;
    }

    public void run() {
        try {
            oIn = new ObjectInputStream(s.getInputStream());
            oOut = new ObjectOutputStream(s.getOutputStream());
            
            boolean isNew = true;
            while (true) {
                oOut.writeObject("Welcome");
                String loginOrNew = (String) oIn.readObject();
                if(loginOrNew.compareTo("new") == 0) {
                    while (true) {
                        oOut.writeObject("Username:");
                        String name = (String)oIn.readObject();
                        if (name == null || (NetworkServer.getMembers().get(name) != null)) {
                            continue;
                        }
                        oOut.writeObject("Password:");
                        String pwIn = (String)oIn.readObject();
                        if (pwIn == null) {
                            continue;
                        }
                        oOut.writeObject("Avatar");
                        String avatar = (String)oIn.readObject();
                        if (avatar == null) {
                            return;
                        }
                        MemberAccount memAcc = new MemberAccount();
                        memAcc.userName = name;
                        memAcc.userPw = pwIn;
                        FileOutputStream fOut = new FileOutputStream(name + "Account");
                        ObjectOutputStream credentialOut = new ObjectOutputStream(fOut);
                        encrypt(memAcc, credentialOut);
                        //credentialOut.writeObject(memAcc);
                        //credentialOut.close();

                        member = new Member(name, cellX, cellY, avatar, "area1zone1");
                        member.inventory = new Inventory();
                        member.questLog = new QuestLog();
                        member.stats = new MemberStats();
                        member.stats.stats.put("woodcutting", new Skill());
                        Quest quest1 = new Quest();
                        quest1.complete = false;
                        quest1.currentTask = 0;
                        ArrayList<Task> tasks = new ArrayList();
                        tasks.add(new Task("interact King Leroy"));
                        tasks.add(new Task("interact Macari"));
                        tasks.add(new Task("interact Macari 20 treelogs"));
                        quest1.tasks = tasks;
                        ArrayList<Item> reward = new ArrayList();
                        Item item1 = new Item("pickaxe", 00002, 1);
                        reward.add(item1);
                        quest1.reward = reward;
                        member.questLog.questLog.put("quest1", quest1);
                        member.inventory.inventory.put("coins", new Item("coins", 00004, 10));
                        NetworkServer.AddMember(member);
                        NetworkServer.AddListener(member.name, oOut);
                        oOut.writeObject("Account Created Successfully");
                        break;
                    }
                } else if(loginOrNew.compareTo("login") == 0) {
                    while (true) {
                        oOut.writeObject("Username:");
                        String name = (String)oIn.readObject();
                        if (name == null || (NetworkServer.getMembers().get(name) != null)) {
                            continue;
                        }
                        oOut.writeObject("Password:");
                        String pwIn = (String)oIn.readObject();
                        if (pwIn == null) {
                            continue;
                        }
                        FileInputStream fIn = new FileInputStream(name + "Account");
                        ObjectInputStream credentialIn = new ObjectInputStream(fIn);
                        //MemberAccount memAcc = (MemberAccount)credentialIn.readObject();
                        MemberAccount memAcc = (MemberAccount)decrypt(credentialIn);
                        if(memAcc.userPw.compareTo(pwIn) == 0) {
                            FileInputStream lIn = new FileInputStream(name);
                            ObjectInputStream persistenceIn = new ObjectInputStream(lIn);
                            //MemberSave memSave = (MemberSave)persistenceIn.readObject();
                            MemberSave memSave = (MemberSave)decrypt(persistenceIn);
                            member = new Member(name, memSave.position.x, memSave.position.y, memSave.avatar, memSave.zone);
                            member.inventory = memSave.inventory;
                            member.questLog = memSave.questlog;
                            member.stats = memSave.stats;
                            NetworkServer.AddMember(member);
                            NetworkServer.AddListener(member.name, oOut);
                            persistenceIn.close();
                            oOut.writeObject("Logged In Successfully");
                        }
                        credentialIn.close();
                        break;
                    }
                } else {
                    continue;
                }
                break;
            }
            
            //send spawn broadcast
            NetworkServer.Broadcast(member.getWorldZone(), member.name + "++" + member.getPositionX() + "++" + member.getPositionY() + "++" + member.getSprite() + "++" + member.getWorldZone());
            
            //spawn all other currently active members
            for (Map.Entry<String, Member> entry : NetworkServer.getMembers().entrySet()) {
                if(!entry.getKey().equals(member.name) && entry.getValue().getWorldZone().equals(member.getWorldZone())) {
                    oOut.writeObject(entry.getValue().name + "++" + entry.getValue().getPositionX() + "++" + entry.getValue().getPositionY() + "++" + entry.getValue().getSprite() + "++" + entry.getValue().getWorldZone());
                }
            }
            oOut.writeObject(member.inventory);
            oOut.reset();
            oOut.writeObject(member.questLog);
            oOut.reset();
            oOut.writeObject(member.stats);
            oOut.reset();

            while (true) {
                input = oIn.readObject();
                if(input.getClass().toString().contains("java.lang.String")) {
                    inpStr = (String)input;
                    if (inpStr.split(Pattern.quote("::")).length > 1) {
                        handleAction(inpStr.split(Pattern.quote("::"))[1]);
                    }
                } else if (input.getClass().toString().contains("testerq.core.ZoneMessage")) {
                    ZoneMessage zMsg = new ZoneMessage();
                    zMsg = (ZoneMessage)input;
                    handleZoneMessage(zMsg);
                } else if (input.getClass().toString().contains("testerq.core.QuestLog")){
                    QuestLog ql = (QuestLog)input;
                    member.questLog = ql;
                } else if (input.getClass().toString().contains("testerq.core.Inventory")){
                    Inventory inv = (Inventory)input;
                    member.inventory = inv;
                }     
            }
        } catch (IOException e) {
            input = this.getName();
            System.out.println("IO Error/ Client " + input + " terminated abruptly");
        } catch (NullPointerException e) {
            e.printStackTrace();
            input = this.getName();
            System.out.println("Client " + input + " Closed");
        } catch (ClassNotFoundException ex) {
            System.out.println(ex);
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            try {
                //unspawn member
                NetworkServer.Broadcast(member.name + "--");
                //save current member state
                FileOutputStream lOut = new FileOutputStream(member.name);
                ObjectOutputStream persistenceOut = new ObjectOutputStream(lOut);
                MemberSave memSave = new MemberSave();
                memSave.avatar = member.getSprite();
                memSave.position = member.getPosition();
                memSave.zone = member.getWorldZone();
                memSave.inventory = member.inventory;
                memSave.questlog = member.questLog;
                memSave.stats = member.stats;
                //persistenceOut.writeObject(memSave);
                try {
                    encrypt(memSave, persistenceOut);
                } catch (Exception ex) {
                    System.out.println(ex);
                }
                //persistenceOut.close();
                System.out.println("Connection Closing..");
                if (oIn != null) {
                    oIn.close();
                    System.out.println(" Socket Input Stream Closed");
                }
                if (oOut != null) {
                    NetworkServer.RemoveListener(member.name);
                    NetworkServer.RemoveMember(member);
                    oOut.close();
                    System.out.println("Socket Out Closed");
                }
                if (s != null) {
                    s.close();
                    System.out.println("Socket Closed");
                }

            } catch (IOException ie) {
                System.out.println("Socket Close Error");
            }
        }
    }
    
    private void handleAction(String action) {
            int playerX = NetworkServer.getMembers().get(member.name).getPositionX();
            int playerY = NetworkServer.getMembers().get(member.name).getPositionY();
            String[] actions = action.split(" ");
            if(actions[0].compareTo("move") == 0 || actions[0].compareTo("mv") == 0) {
                if (actions.length == 2 || actions.length == 3) {
                    String[][] worldArray = NetworkServer.mapManager.zones.get(member.getWorldZone()).zone2DArray;
                    if (actions[1].compareTo("north") == 0 || actions[1].compareTo("n") == 0 || actions[1].compareTo("up") == 0) {
                        if (actions.length == 3) {
                            int numMoves = Integer.parseInt(actions[2]);
                            handleMove(worldArray, Direction.North, playerX, playerY, numMoves);
                        } else {
                            handleMove(worldArray, Direction.North, playerX, playerY, 1);
                        }

                    } else if (actions[1].compareTo("south") == 0 || actions[1].compareTo("s") == 0 ||actions[1].compareTo("down") == 0) {
                        if (actions.length == 3) {
                            int numMoves = Integer.parseInt(actions[2]);
                            handleMove(worldArray, Direction.South, playerX, playerY, numMoves);
                        } else {
                            handleMove(worldArray, Direction.South, playerX, playerY, 1);
                        }

                    } else if (actions[1].compareTo("west") == 0 || actions[1].compareTo("w") == 0 || actions[1].compareTo("left") == 0) {
                        if (actions.length == 3) {
                            int numMoves = Integer.parseInt(actions[2]);
                            handleMove(worldArray, Direction.West, playerX, playerY, numMoves);
                        } else {
                            handleMove(worldArray, Direction.West, playerX, playerY, 1);     
                        }

                    } else if (actions[1].compareTo("east") == 0 || actions[1].compareTo("e") == 0 || actions[1].compareTo("right") == 0) {
                        if (actions.length == 3) {
                            int numMoves = Integer.parseInt(actions[2]);
                            handleMove(worldArray, Direction.East, playerX, playerY, numMoves);
                        } else {
                            handleMove(worldArray, Direction.East, playerX, playerY, 1);     
                        }
                    }
                } else {
                    try {
                        oOut.writeObject("+_)( " + "[System] Move command format: move <direction> OR move <direction> <number steps>");
                    } catch (IOException ex) {
                    }
                }
            } else if (actions[0].compareTo("chop") == 0) {
                if (actions.length == 2) {
                    String[][] worldArray = NetworkServer.mapManager.zones.get(member.getWorldZone()).zone2DArray;
                    String cellSprite = null;
                    if (actions[1].compareTo("north") == 0 || actions[1].compareTo("n") == 0 || actions[1].compareTo("up") == 0) {
                        cellSprite = worldArray[playerX - 1][playerY];
                    } else if (actions[1].compareTo("south") == 0 || actions[1].compareTo("s") == 0 ||actions[1].compareTo("down") == 0) {
                        cellSprite = worldArray[playerX + 1][playerY];
                    } else if (actions[1].compareTo("west") == 0 || actions[1].compareTo("w") == 0 || actions[1].compareTo("left") == 0) {
                        cellSprite = worldArray[playerX][playerY - 1];
                    } else if (actions[1].compareTo("east") == 0 || actions[1].compareTo("e") == 0 || actions[1].compareTo("right") == 0) {
                        cellSprite = worldArray[playerX][playerY + 1];
                    }
                    if (cellSprite != null) {
                        if (cellSprite.equals("#")) {
                            if(member.inventory.inventory.get("treelogs") != null) {
                                member.inventory.inventory.get("treelogs").quantity += 3;
                                member.stats.stats.get("woodcutting").addExp(1);
                            } else {
                                Item logs = new Item("treelogs", 00001, 3);
                                member.inventory.inventory.put("treelogs", logs);
                                member.stats.stats.get("woodcutting").addExp(1);
                            }
                            try {
                                oOut.writeObject(member.inventory);
                                oOut.writeObject(member.stats);
                                oOut.writeObject("+_)( " + "[System] You picked up 3 treelogs");
                                if(member.stats.stats.get("woodcutting").getExp() % 10 == 0) {
                                   oOut.writeObject("+_)( " + "[System] ** You've leveled up your woodcutting skill! **"); 
                                }
                                oOut.reset();
                            } catch (IOException ex) {
                            }
                        }
                    }
                } else {
                    try {
                        oOut.writeObject("+_)( " + "[System] Chop command format: chop <direction>");
                    } catch (IOException ex) {
                    }
                }
            } else if (actions[0].compareTo("trade") == 0) {
                if (actions.length == 2) {
                    if(NetworkServer.tradeManager.trades.containsKey(actions[1])) {
                        Item tradingFor = NetworkServer.tradeManager.trades.get(actions[1]).tradingFor;
                        Item trading = NetworkServer.tradeManager.trades.get(actions[1]).trading;
                        Member tradeHost = NetworkServer.getMembers().get(actions[1]);
                        if(member.inventory.inventory.containsKey(tradingFor.name) && member.inventory.inventory.get(tradingFor.name).quantity >= tradingFor.quantity) {
                            //This member
                            member.inventory.inventory.get(tradingFor.name).quantity -= tradingFor.quantity;
                            if(member.inventory.inventory.get(tradingFor.name).quantity == 0) {
                                member.inventory.inventory.remove(tradingFor.name);
                            }
                            if(member.inventory.inventory.containsKey(trading.name)){
                               member.inventory.inventory.get(trading.name).quantity += trading.quantity; 
                            } else {
                                member.inventory.inventory.put(trading.name, new Item(trading.name, trading.itemId, trading.quantity));
                            }
                            try {
                                oOut.writeObject(member.inventory);
                                oOut.writeObject("+_)( " + "[System] Traded " + tradingFor.quantity + " " + tradingFor.name + " for " + trading.quantity + " " + trading.name + " with " + tradeHost.name);
                                oOut.reset();
                            } catch (IOException ex) {
                            }
                            //Member who hosted trade
                            tradeHost.inventory.inventory.get(trading.name).quantity -= trading.quantity;
                            if(tradeHost.inventory.inventory.get(trading.name).quantity == 0) {
                                tradeHost.inventory.inventory.remove(trading.name);
                            }
                            if(tradeHost.inventory.inventory.containsKey(tradingFor.name)){
                               tradeHost.inventory.inventory.get(tradingFor.name).quantity += tradingFor.quantity; 
                            } else {
                                if (tradingFor.quantity > 0) {
                                tradeHost.inventory.inventory.put(tradingFor.name, new Item(tradingFor.name, tradingFor.itemId, tradingFor.quantity));
                                }
                            }
                            try {
                                NetworkServer.getWriters().get(tradeHost.name).writeObject(tradeHost.inventory);
                                NetworkServer.getWriters().get(tradeHost.name).writeObject("+_)( " + "[System] Traded " + trading.quantity + " " + trading.name + " for " + tradingFor.quantity + " " + tradingFor.name + " with " + member.name);
                                NetworkServer.getWriters().get(tradeHost.name).reset();
                            } catch (IOException ex) {
                            }
                            NetworkServer.tradeManager.trades.remove(tradeHost.name);

                        } else {
                            try {
                                oOut.writeObject("+_)( " + "[System] You do not meet the trade requirements.");
                            } catch (IOException ex) {
                            }
                        }
                    } else {
                        try {
                            oOut.writeObject("+_)( " + "[System] That trade is not available.");
                        } catch (IOException ex) {
                        }
                    }
                } else {
                    try {
                        oOut.writeObject("+_)( " + "[System] Trade command format: trade <member name>");
                    } catch (IOException ex) {
                    }
                }
            } else if (actions[0].compareTo("trading") == 0) {
                if (actions.length == 6) {
                    int tradingQuantity = Integer.parseInt(actions[1]);
                    String tradingItemName = actions[2];
                    int tradingForQuantity = Integer.parseInt(actions[4]);
                    String tradingForItemName = actions[5];
                    if(member.inventory.inventory.containsKey(tradingItemName) && member.inventory.inventory.get(tradingItemName).quantity >= tradingQuantity) {
                        Trade trade = new Trade();
                        Item trading = new Item(tradingItemName, NetworkServer.items.items.get(tradingItemName).itemId, tradingQuantity);
                        Item tradingFor = new Item(tradingForItemName, NetworkServer.items.items.get(tradingForItemName).itemId, tradingForQuantity);
                        trade.trading = trading;
                        trade.tradingFor = tradingFor;
                        NetworkServer.tradeManager.trades.put(member.name, trade);
                        NetworkServer.Broadcast("+_)( " + "[" + member.name + "] trading " + trading.quantity + " " + trading.name + " for " + tradingFor.quantity + " " + tradingFor.name);
                    } else {
                        try {
                            oOut.writeObject("+_)( " + "[System] You do not have a sufficient number of that item.");
                        } catch (IOException ex) {
                        }
                    }
                } else {
                    try {
                        oOut.writeObject("+_)( " + "[System] Trading command format: trading <quanity> <item> for <quantity> <item>");
                    } catch (IOException ex) {
                    }
                }
            } else if (actions[0].compareTo("list") == 0) {
                if (actions.length == 3) {
                    String stats = "";
                    if (actions[1].compareTo("stats") == 0) {
                        if(NetworkServer.getMembers().get(actions[2]).stats != null) {
                            for (Map.Entry<String, Skill> entry : NetworkServer.getMembers().get(actions[2]).stats.stats.entrySet()) {
                                stats += entry.getKey() + ": " + entry.getValue().getLevel() + ", ";
                            }
                            try {
                                oOut.writeObject("+_)( " + "["+actions[2]+"] stats -> " + stats);
                            } catch (IOException ex) {
                            }
                        }
                    } 
                }
                
            } else {
                try {
                    oOut.writeObject("+_)( " + "[System] Invalid Command");
                } catch (IOException ex) {
                }
            }
    }
        
    private boolean validMove(String contents) {
        return " \u263A\u263B\u2665\u2666\u2663\u2660".contains(contents);
    }
    
    private boolean isTransfer(String contents) {
        return "I".contains(contents);
    }
    
    private void handleMove(String[][] worldArray, Direction dir, int playerX, int playerY, int numMoves) {
        if (numMoves != 1) {
            for (int i = 0; i < numMoves; i++) {
                boolean cont = moveLogic(worldArray, dir, playerX, playerY);
                playerX = NetworkServer.getMembers().get(member.name).getPositionX();
                playerY = NetworkServer.getMembers().get(member.name).getPositionY();
                if(!cont) {
                    //either invalid move or transfer reached so stop loop
                    break;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                }

            }
        } else {
            moveLogic(worldArray, dir, playerX, playerY);
        }
    }
    
    private boolean moveLogic(String[][] worldArray, Direction dir, int playerX, int playerY) {
        boolean valid = false;
        boolean transfer = false;
        if (null != dir) switch (dir) {
            case North:
                nextCellX = playerX - 1;
                valid = validMove(worldArray[nextCellX][playerY]);
                if (!valid) {
                        transfer = isTransfer(worldArray[nextCellX][playerY]);
                }
                break;
            case East:
                nextCellY = playerY + 1;
                valid = validMove(worldArray[playerX][nextCellY]);
                if (!valid) {
                        transfer = isTransfer(worldArray[playerX][nextCellY]);
                }
                break;
            case South:
                nextCellX = playerX + 1;
                valid = validMove(worldArray[nextCellX][playerY]);
                if (!valid) {
                        transfer = isTransfer(worldArray[nextCellX][playerY]);
                }
                break;
            case West:
                nextCellY = playerY - 1;
                valid = validMove(worldArray[playerX][nextCellY]);
                if (!valid) {
                        transfer = isTransfer(worldArray[playerX][nextCellY]);
                }
                break;
            default:
                break;
        }
        if (valid && (dir == Direction.North || dir == Direction.South)) {
            NetworkServer.getMembers().get(member.name).setPositionX(nextCellX);
            NetworkServer.Broadcast(member.getWorldZone(), member.name + "||" + nextCellX + "||" + playerY);
            return true;
        } else if (valid && (dir == Direction.East || dir == Direction.West)) {
            NetworkServer.getMembers().get(member.name).setPositionY(nextCellY);
            NetworkServer.Broadcast(member.getWorldZone(), member.name + "||" + playerX + "||" + nextCellY);
            return true;
        } else if (transfer) {
            //Unspawn player
            NetworkServer.Broadcast(member.name + "--");
            //Spawn player in new map
            MapTransfer trans = null;
            if (dir == Direction.North || dir == Direction.South) {
                trans = NetworkServer.mapManager.transfers.get(nextCellX + "" + playerY);
            } else if (dir == Direction.East || dir == Direction.West) {
                trans = NetworkServer.mapManager.transfers.get(playerX + "" + nextCellY);
            }
            member.setPositionX(trans.x);
            member.setPositionY(trans.y);
            member.setWorldZone(trans.map);
            //overwrite member with new zone/position
            NetworkServer.getMembers().put(member.name, member);
            //send spawn to all members in zone
            NetworkServer.Broadcast(member.getWorldZone(), member.name + "++" + trans.x + "++" + trans.y + "++" + member.getSprite() + "++" + trans.map);
            //spawn all other currently active members
            try {
                for (Map.Entry<String, Member> entry : NetworkServer.getMembers().entrySet()) {
                    if(!entry.getKey().equals(member.name) && entry.getValue().getWorldZone().equals(member.getWorldZone())) {
                        oOut.writeObject(entry.getValue().name + "++" + entry.getValue().getPositionX() + "++" + entry.getValue().getPositionY() + "++" + entry.getValue().getSprite() + "++" + entry.getValue().getWorldZone());
                    }
                }
                oOut.writeObject(member.inventory);
                oOut.reset();
                oOut.writeObject(member.questLog);
                oOut.reset();
            } catch (IOException e) {
                
            }
            return false;
        }
        return false;
    }
    
    private void handleZoneMessage(ZoneMessage zMsg) {
        NetworkServer.Broadcast(member.getWorldZone(), "+_)( " + "[" + member.name + "]: " +zMsg.msg);
    }
    
    public static void encrypt(Serializable object, OutputStream ostream) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException {
        try {
            // Length is 16 byte
            SecretKeySpec sks = new SecretKeySpec(key, "AES");

            // Create cipher
            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.ENCRYPT_MODE, sks);
            SealedObject sealedObject = new SealedObject(object, cipher);

            // Wrap the output stream
            CipherOutputStream cos = new CipherOutputStream(ostream, cipher);
            ObjectOutputStream outputStream = new ObjectOutputStream(cos);
            outputStream.writeObject(sealedObject);
            outputStream.close();
        } catch (IllegalBlockSizeException e) {
        }
    }
    
    public static Object decrypt(InputStream istream) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        SecretKeySpec sks = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(Cipher.DECRYPT_MODE, sks);

        CipherInputStream cipherInputStream = new CipherInputStream(istream, cipher);
        ObjectInputStream inputStream = new ObjectInputStream(cipherInputStream);
        SealedObject sealedObject;
        try {
            sealedObject = (SealedObject) inputStream.readObject();
            return sealedObject.getObject(cipher);
        } catch (ClassNotFoundException | IllegalBlockSizeException | BadPaddingException e) {
            return null;
        }
    }
}

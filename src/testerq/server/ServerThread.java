
package testerq.server;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import testerq.core.Direction;
import testerq.core.Item;
import testerq.core.MapTransfer;
import testerq.core.Member;
import testerq.core.MemberAccount;
import testerq.core.MemberSave;
import testerq.core.ZoneMessage;

public class ServerThread extends Thread {
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
                        credentialOut.writeObject(memAcc);
                        credentialOut.close();

                        member = new Member(name, cellX, cellY, avatar, "area1zone1");
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
                        MemberAccount memAcc = (MemberAccount)credentialIn.readObject();
                        if(memAcc.userPw.compareTo(pwIn) == 0) {
                            FileInputStream lIn = new FileInputStream(name);
                            ObjectInputStream persistenceIn = new ObjectInputStream(lIn);
                            MemberSave memSave = (MemberSave)persistenceIn.readObject();
                            member = new Member(name, memSave.position.x, memSave.position.y, memSave.avatar, memSave.zone);
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
                persistenceOut.writeObject(memSave);
                persistenceOut.close();
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
            } else if (actions[0].compareTo("chop") == 0) {
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
                        if(member.inventory.get("treelogs") != null) {
                            member.inventory.get("treelogs").quantity += 3;
                            System.out.println(member.inventory.get("treelogs").quantity);
                        } else {
                            Item logs = new Item();
                            logs.itemId = 00001;
                            logs.name = "wood logs";
                            logs.quantity = 3;
                            member.inventory.put("treelogs", logs);
                        }
                    }
                    HashMap<String, Item> inventory = new HashMap<>();
                    for (Entry<String,Item> e : member.inventory.entrySet()) {
                        inventory.put(e.getKey(),e.getValue());
                    }
                    try {
                        oOut.writeObject(inventory);
                        oOut.reset();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
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
            } catch (IOException e) {
                
            }
            return false;
        }
        return false;
    }
    
    private void handleZoneMessage(ZoneMessage zMsg) {
        NetworkServer.Broadcast(member.getWorldZone(), "+_)( " + "[" + member.name + "]: " +zMsg.msg);
    }
}

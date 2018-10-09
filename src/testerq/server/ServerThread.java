
package testerq.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.regex.Pattern;
import testerq.core.Direction;
import testerq.core.MapTransfer;
import testerq.core.Member;
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

            String name = null;
            String avatar = null;
            while (true) {
                oOut.writeObject("Username:");
                name = (String)oIn.readObject();
                if (name == null || (NetworkServer.getMembers().get(name) != null)) {
                    continue;
                }
                break;
            }
            oOut.writeObject("Logged In Successfully");
            while (true) {
                oOut.writeObject("Avatar");
                avatar = (String)oIn.readObject();
                if (avatar == null) {
                    return;
                }
                oOut.writeObject("Avatar Chosen");
                member = new Member(name, cellX, cellY, avatar, "area1zone1");
                NetworkServer.AddMember(member);
                NetworkServer.AddListener(member.name, oOut);
                break;
            } 
            
            //send spawn broadcast
            NetworkServer.Broadcast(member.getWorldZone(), member.name + "++" + member.getPositionX() + "++" + member.getPositionY() + "++" + member.getSprite() + "++" + member.getWorldZone());
            
            //spawn all other currently active members
            for (Map.Entry<String, Member> entry : NetworkServer.getMembers().entrySet()) {
                if(!entry.getKey().equals(member.name)) {
                    oOut.writeObject(entry.getValue().name + "++" + entry.getValue().getPositionX() + "++" + entry.getValue().getPositionY() + "++" + entry.getValue().getSprite() + "++" + entry.getValue().getWorldZone());
                }
            }

            while (true) {
                input = oIn.readObject();
                System.out.println(input.getClass());
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
            input = this.getName();
            System.out.println("Client " + input + " Closed");
        } catch (ClassNotFoundException ex) {
        } finally {
            try {
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
            NetworkServer.Broadcast(member.getWorldZone(), member.name + "--");
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
            NetworkServer.getMembers().put(member.name, member);
            NetworkServer.Broadcast(member.getWorldZone(), member.name + "++" + trans.x + "++" + trans.y + "++" + member.getSprite() + "++" + trans.map);
            return false;
        }
        return false;
    }
    
    private void handleZoneMessage(ZoneMessage zMsg) {
        System.out.println(zMsg.msg);
        NetworkServer.Broadcast(member.getWorldZone(), "+_)( " + "[" + member.name + "]: " +zMsg.msg);
    }
}

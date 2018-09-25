
package testerq.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.regex.Pattern;
import testerq.core.Direction;
import testerq.core.MapTransfer;
import testerq.core.Member;

public class ServerThread extends Thread {
    Member member;
    int gridX = 0;
    int gridY = 0;
    int cellX = 15;
    int cellY = 15;
    int nextCellX = 0;
    int nextCellY = 0;
    String input = null;
    BufferedReader ins = null;
    PrintWriter outs = null;
    Socket s = null;

    public ServerThread(Socket s) {
        this.s = s;
    }

    public void run() {
        try {
            ins = new BufferedReader(new InputStreamReader(s.getInputStream()));
            outs = new PrintWriter(s.getOutputStream(), true);
            String name = null;
            String avatar = null;
            while (true) {
                outs.println("Username:");
                name = ins.readLine();
                if (name == null || (NetworkServer.getMembers().get(name) != null)) {
                    continue;
                }
                break;
            }
            System.out.println("succ");
            outs.println("Logged In Successfully");
            while (true) {
                outs.println("Avatar");
                avatar = ins.readLine();
                if (avatar == null) {
                    return;
                }
                outs.println("Avatar Chosen");
                member = new Member(name, cellX, cellY, avatar);
                member.worldZone = "area1zone1";
                NetworkServer.AddMember(member);
                NetworkServer.AddListener(member.name, outs);
                break;
            } 
            
            //send spawn broadcast
            System.out.println("broadcasting " + member.name + "++" + member.cellX + "++" + member.cellY + "++" + member.avatar + "++" + member.worldZone);
            NetworkServer.Broadcast(member.worldZone, member.name + "++" + member.cellX + "++" + member.cellY + "++" + member.avatar + "++" + member.worldZone);
            
            //spawn all other currently active members

            for (Map.Entry<String, Member> entry : NetworkServer.getMembers().entrySet()) {
                if(!entry.getKey().equals(member.name)) {
                    outs.println(entry.getValue().name + "++" + entry.getValue().cellX + "++" + entry.getValue().cellY + "++" + entry.getValue().avatar + "++" + entry.getValue().worldZone);
                }
            }

            while (true) {
                input = ins.readLine();
                System.out.println("Input " + input);
                if (input == null) {
                    return;
                } else if (input.split(Pattern.quote("::")).length > 1) {
                    handleAction(input.split(Pattern.quote("::"))[1]);
                }
                
            }
        } catch (IOException e) {

            input = this.getName(); //reused String line for getting thread name
            System.out.println("IO Error/ Client " + input + " terminated abruptly");
        } catch (NullPointerException e) {
            input = this.getName(); //reused String line for getting thread name
            System.out.println("Client " + input + " Closed");
        } finally {
            try {
                System.out.println("Connection Closing..");
                if (ins != null) {
                    ins.close();
                    System.out.println(" Socket Input Stream Closed");
                }

                if (outs != null) {
                    NetworkServer.RemoveListener(member.name);
                    NetworkServer.RemoveMember(member);
                    outs.close();
                    System.out.println("Socket Out Closed");
                }
                if (s != null) {
                    s.close();
                    System.out.println("Socket Closed");
                }

            } catch (IOException ie) {
                System.out.println("Socket Close Error");
            }
        }//end finally
    }
    
    private void handleAction(String action) {
            int playerX = NetworkServer.getMembers().get(member.name).cellX;
            int playerY = NetworkServer.getMembers().get(member.name).cellY;
            System.out.println("handl act " + member.name + " " + playerX + " " + playerY);
            String[] actions = action.split(" ");
            if(actions[0].compareTo("move") == 0 || actions[0].compareTo("mv") == 0) {
                String[][] worldArray = NetworkServer.mapManager.zones.get(member.worldZone).zone2DArray;
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
        System.out.println("contents " + contents);
        return " \u263A\u263B\u2665\u2666\u2663\u2660".contains(contents);
    }
    
    private boolean isTransfer(String contents) {
        return "I".contains(contents);
    }
    
    private void handleMove(String[][] worldArray, Direction dir, int playerX, int playerY, int numMoves) {
        if (numMoves != 1) {
            for (int i = 0; i < numMoves; i++) {
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
                    NetworkServer.getMembers().get(member.name).cellX = nextCellX;
                    NetworkServer.Broadcast(member.worldZone, member.name + "||" + nextCellX + "||" + playerY);
                    playerX = NetworkServer.getMembers().get(member.name).cellX;
                } else if (valid && (dir == Direction.East || dir == Direction.West)) {
                    NetworkServer.getMembers().get(member.name).cellY = nextCellY;
                    NetworkServer.Broadcast(member.worldZone, member.name + "||" + playerX + "||" + nextCellY);
                    playerY = NetworkServer.getMembers().get(member.name).cellY;
                } else if (transfer) {
                    //Unspawn player
                    System.out.println("UNSPAWN " + member.name);
                    NetworkServer.Broadcast(member.worldZone, member.name + "--");
                    //Spawn player in new map
                    MapTransfer trans = null;
                    System.out.println("trans " + nextCellX + " " + playerY);
                    if (dir == Direction.North || dir == Direction.South) {
                        trans = NetworkServer.mapManager.transfers.get(nextCellX + "" + playerY);
                    } else if (dir == Direction.East || dir == Direction.West) {
                        trans = NetworkServer.mapManager.transfers.get(playerX + "" + nextCellY);
                    }
                    member.cellX = trans.x;
                    member.cellY = trans.y;
                    member.worldZone = trans.map;
                    NetworkServer.getMembers().put(member.name, member);
                    NetworkServer.Broadcast(member.worldZone, member.name + "++" + trans.x + "++" + trans.y + "++" + member.avatar + "++" + trans.map);
                    break;
                } else {
                    break;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                }

            }
        } else {
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
                NetworkServer.getMembers().get(member.name).cellX = nextCellX;
                NetworkServer.Broadcast(member.worldZone, member.name + "||" + nextCellX + "||" + playerY);
            } else if (valid && (dir == Direction.East || dir == Direction.West)) {
                NetworkServer.getMembers().get(member.name).cellY = nextCellY;
                NetworkServer.Broadcast(member.worldZone, member.name + "||" + playerX + "||" + nextCellY);
            } else if (transfer) {
                //Unspawn player
                NetworkServer.Broadcast(member.worldZone, member.name + "--");
                //Spawn player in new map
                MapTransfer trans = null;
                System.out.println("trans " + nextCellX + " " + playerY);
                if (dir == Direction.North || dir == Direction.South) {
                    System.out.println(NetworkServer.mapManager.transfers.size());
                    trans = NetworkServer.mapManager.transfers.get(nextCellX + "" + playerY);
                    System.out.println(trans);
                } else if (dir == Direction.East || dir == Direction.West) {
                    trans = NetworkServer.mapManager.transfers.get(playerX + "" + nextCellY);
                }
                member.cellX = trans.x;
                member.cellY = trans.y;
                member.worldZone = trans.map;
                NetworkServer.getMembers().put(member.name, member);
                NetworkServer.Broadcast(member.worldZone, member.name + "++" + trans.x + "++" + trans.y + "++" + member.avatar + "++" + trans.map);
            }
        }
    }
}

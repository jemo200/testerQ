/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testerq.server;


import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import testerq.core.Member;
import testerq.core.map.MapManager;

/**
 *
 * @author emoj
 */
public class NetworkServer {
    private static NetworkServer s_Instance;
    
    private static HashMap<String, Member> members = new HashMap<String, Member>();

    private static HashMap<String, PrintWriter> writers = new HashMap<String, PrintWriter>();
    
    public static MapManager mapManager = new MapManager();;
    
    private NetworkServer() {
        
    }
    
    public static NetworkServer getInstance()
    {
        if (s_Instance == null)
            s_Instance = new NetworkServer();
 
        return s_Instance;
    }
    
    public static void Listen(int portNumber) {
        new ListenThread(portNumber).start();     
    }
    
    public static boolean SpawnObjects() {
        return true;
    }
    
    public static boolean AddMember(Member member) {
        synchronized (members) {
            if (members.get(member.name) == null) {
                members.put(member.name, member);
                return true;
            }
            return false;
        }
    }
    
    public static boolean RemoveMember(Member member) {
        synchronized (members) {
            if (members.get(member.name) != null) {
                members.remove(member.name);
                return true;
            }
            return false;
        }
    }
    
    public static boolean AddListener(String name, PrintWriter listener) {
        synchronized (writers) {
            if (writers.get(name) == null) {
                writers.put(name, listener);
                return true;
            }
            return false;
        }
    }
    
    public static boolean RemoveListener(String name) {
        synchronized (writers) {
            if (writers.get(name) != null) {
                writers.remove(name);
                return true;
            }
            return false;
        }
    }
    
    public static HashMap<String, Member> getMembers() {
        return members;
    }
    
    public static void Broadcast(String message) {
        for (Map.Entry<String, PrintWriter> entry : writers.entrySet()) {
            entry.getValue().println(message);
        }
    }
    
    public static void Broadcast(String worldZone, String message) {
        for (Map.Entry<String, Member> entry : members.entrySet()) {
            if(entry.getValue().worldZone.compareTo(worldZone) == 0) {
                writers.get(entry.getValue().name).println(message);
            }
        }
    }
    
}

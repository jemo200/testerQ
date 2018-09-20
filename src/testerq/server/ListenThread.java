/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testerq.server;

import java.io.IOException;
import java.net.ServerSocket;

/**
 *
 * @author emoj
 */
public class ListenThread extends Thread {
    
    int port;
    
    public ListenThread(int port) {
        this.port = port;
    }
    
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            try {
                while (true) {
                    new ServerThread(serverSocket.accept()).start();
                }
            } finally {
                serverSocket.close();
            }

        } catch (IOException e) {
            
        }
    }
}

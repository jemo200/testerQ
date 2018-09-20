package testerq.client;

import testerq.core.MsgType;
import testerq.core.NetworkClient;


public class NetworkManagerClient {
    
    public static NetworkManagerClient singleton;
    
    public boolean isNetworkActive;
    public NetworkClient client;
    
    void InitializeSingleton() {
        if (singleton != null && singleton == this) {
            return;
        }
        singleton = this;
    }
    
    private void RegisterClientMessages(NetworkClient client) {
        /*client.RegisterHandler(MsgType.Connect, OnClientConnectInternal);
        client.RegisterHandler(MsgType.Disconnect, OnClientDisconnectInternal);
        client.RegisterHandler(MsgType.NotReady, OnClientNotReadyMessageInternal);
        client.RegisterHandler(MsgType.Error, OnClientErrorInternal);
        client.RegisterHandler(MsgType.Scene, OnClientSceneInternal);

        if (m_PlayerPrefab != null)
        {
            ClientScene.RegisterPrefab(m_PlayerPrefab);
        }
        foreach (var prefab in m_SpawnPrefabs)
        {
            if (prefab != null)
            {
                ClientScene.RegisterPrefab(prefab);
            }
        }*/
    }
    
    public NetworkClient startClient() {
        InitializeSingleton();
        
        isNetworkActive = true;
        
        client = new NetworkClient();
        
        RegisterClientMessages(client);
        return null;
    }
    
}

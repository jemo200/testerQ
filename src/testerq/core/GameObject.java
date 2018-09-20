package testerq.core;

public class GameObject {
    private int mNetworkId = 0;
    private NetworkIdentity netId;

    public NetworkIdentity getNetId() {
        return netId;
    }

    public void setNetId(NetworkIdentity netId) {
        this.netId = netId;
    }
    
    public GameObject() {
        
    }

    public int getNetworkId() {
        return mNetworkId;
    }

    public void setNetworkId(int inNetworkId) {
        this.mNetworkId = inNetworkId;
    }
    
    public void update() {
        
    }
}

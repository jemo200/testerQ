
package testerq.core;

public class PlayerController {
    public short playerControllerId = -1;
    public NetworkIdentity unetView;
    public GameObject gameObject;
    
    public PlayerController() {
        
    }
    
    public boolean isValid() {
        return playerControllerId != -1;
    }
    
    private PlayerController(GameObject go, short playerControllerId) {
        gameObject = go;
        unetView = go.getNetId();
        this.playerControllerId = playerControllerId;
    }
}

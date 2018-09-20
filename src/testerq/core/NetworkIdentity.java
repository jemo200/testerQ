/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testerq.core;

import java.io.Serializable;

/**
 *
 * @author emoj
 */
public class NetworkIdentity implements Serializable {
    public NetworkHash128 m_AssetId;
    
    private NetworkInstanceId m_NetId;

    public NetworkInstanceId getM_NetId() {
        return m_NetId;
    }

    public void setM_NetId(NetworkInstanceId m_NetId) {
        this.m_NetId = m_NetId;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testerq.core;

/**
 *
 * @author emoj
 */
public class NetworkInstanceId {
    
    private final int m_Value;
    
    NetworkInstanceId(int value) {
        this.m_Value = value;
    }
    
    public int getValue() {
        return m_Value;
    }
    
}

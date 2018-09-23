/*
 * Copyright (c) 2012. Gianluca Vegetti - iuk@iukonline.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.iukonline.amule.ec;

import java.util.zip.DataFormatException;

import com.iukonline.amule.ec.exceptions.ECTagParsingException;

public class ECConnState {
    
    private final static int CONNECTED_ED2K             = 0x1;
    private final static int CONNECTED_ED2K_CONNECTING  = 0x2;
    private final static int CONNECTED_KAD              = 0x4;
    private final static int CONNECTED_KAD_FIREWALLED   = 0x8;
    private final static int CONNECTED_KAD_RUNNING      = 0x10;
    
    
    
    private byte detailLevel;
    
    private int connState;
    private long clientId;
    private long ed2kId;
    private ECServer server;
    
    
    public ECConnState(ECTag t1, byte d) throws ECTagParsingException   {
        
        detailLevel = d;
        ECTag t;

        try {
            
            switch (detailLevel) {
            case ECCodes.EC_DETAIL_FULL:
            case ECCodes.EC_DETAIL_WEB:
            case ECCodes.EC_DETAIL_CMD:
                
                connState = (int) t1.getTagValueUInt();
                
                if (isConnectedEd2k()) {
                    t = t1.getSubTagByName(ECCodes.EC_TAG_SERVER);
                    if (t != null) server = new ECServer(t, detailLevel);
                    else throw new ECTagParsingException("Missing EC_TAG_SERVER in server response");
                    
                    t = t1.getSubTagByName(ECCodes.EC_TAG_ED2K_ID);
                    if (t != null) ed2kId = t.getTagValueUInt();
                    else throw new ECTagParsingException("Missing EC_TAG_ED2K_ID in server response");           
                }
                
                t = t1.getSubTagByName(ECCodes.EC_TAG_CLIENT_ID);
                if (t != null) clientId = t.getTagValueUInt();
                else throw new ECTagParsingException("Missing EC_TAG_CLIENT_ID in server response");     
                
                break;
                
            default:
                throw new ECTagParsingException("Unknown detail level " + detailLevel + " for EC_TAG_SERVER");

            }
            
        } catch (DataFormatException e) {
            throw new ECTagParsingException("One or more unexpected type in EC_TAG_SERVER tags", e);
        }
        
    }
    
    public boolean isConnectedEd2k() {
        return ((connState & CONNECTED_ED2K) == CONNECTED_ED2K);
    }
    
    public boolean isConnectingEd2k() {
        return ((connState & CONNECTED_ED2K_CONNECTING) == CONNECTED_ED2K_CONNECTING);
    }
    
    
    public boolean isConnectedKad() {
        return ((connState & CONNECTED_KAD) == CONNECTED_KAD);
    }
    
    public boolean isKadRunning() {
        return ((connState & CONNECTED_KAD_RUNNING) == CONNECTED_KAD_RUNNING);
    }
    
    public boolean isKadFirewalled() {
        return ((connState & CONNECTED_KAD_FIREWALLED) == CONNECTED_KAD_FIREWALLED);
    }
    
    


    public byte getDetailLevel() {
        return detailLevel;
    }
    
    
    public int getConnState() {
        return connState;
    }
    
    
    public long getClientId() {
        return clientId;
    }
    
    
    public long getEd2kId() {
        return ed2kId;
    }
    
    
    public ECServer getServer() {
        return server;
    }


    @Override
    public String toString() {
        return String.format("ECConnState [detailLevel=%s, connState=%s, clientId=%s, ed2kId=%s, server=%s]", detailLevel, connState, clientId, ed2kId, server);
    }

}

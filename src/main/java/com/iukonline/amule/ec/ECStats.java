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

public class ECStats {
    
    protected byte detailLevel;
    
    protected long upOverhead;
    protected long downOverhead;
    protected long bannedCount;
    protected long ulSpeed;
    protected long dlSpeed;
    protected long ulSpeedLimit;
    protected long dlSpeedLimit;
    protected long ulQueueLen;
    protected long totalSrcCount;
    protected long ed2kUsers;
    protected long kadUsers;
    protected long ed2kFiles;
    protected long kadFiles;
    
    protected ECConnState connState;
    
    
    public ECStats(ECPacket p, byte d) throws ECTagParsingException  {
        
        detailLevel = d;
        ECTag t;

        try {
            
            switch (detailLevel) {
            case ECCodes.EC_DETAIL_FULL:
                t = p.getTagByName(ECCodes.EC_TAG_STATS_UP_OVERHEAD);
                if (t != null) upOverhead = t.getTagValueUInt();
                else throw new ECTagParsingException("Missing EC_TAG_STATS_UP_OVERHEAD in server response");
                
                t = p.getTagByName(ECCodes.EC_TAG_STATS_DOWN_OVERHEAD);
                if (t != null) downOverhead = t.getTagValueUInt();
                else throw new ECTagParsingException("Missing EC_TAG_STATS_DOWN_OVERHEAD in server response");
                
                t = p.getTagByName(ECCodes.EC_TAG_STATS_BANNED_COUNT);
                if (t != null) bannedCount = t.getTagValueUInt();
                else throw new ECTagParsingException("Missing EC_TAG_STATS_BANNED_COUNT in server response");

                // Continue to next case...
                
            case ECCodes.EC_DETAIL_WEB:
            case ECCodes.EC_DETAIL_CMD:
                
                t = p.getTagByName(ECCodes.EC_TAG_STATS_UL_SPEED);
                if (t != null) ulSpeed = t.getTagValueUInt();
                else throw new ECTagParsingException("Missing EC_TAG_STATS_UL_SPEED in server response");
                
                t = p.getTagByName(ECCodes.EC_TAG_STATS_DL_SPEED);
                if (t != null) dlSpeed = t.getTagValueUInt();
                else throw new ECTagParsingException("Missing EC_TAG_STATS_DL_SPEED in server response");
                
                t = p.getTagByName(ECCodes.EC_TAG_STATS_UL_SPEED_LIMIT);
                if (t != null) ulSpeedLimit = t.getTagValueUInt();
                else throw new ECTagParsingException("Missing EC_TAG_STATS_UL_SPEED_LIMIT in server response");
                
                t = p.getTagByName(ECCodes.EC_TAG_STATS_DL_SPEED_LIMIT);
                if (t != null) dlSpeedLimit = t.getTagValueUInt();
                else throw new ECTagParsingException("Missing EC_TAG_STATS_DL_SPEED_LIMIT in server response");
                
                t = p.getTagByName(ECCodes.EC_TAG_STATS_UL_QUEUE_LEN);
                if (t != null) ulQueueLen = t.getTagValueUInt();
                else throw new ECTagParsingException("Missing EC_TAG_STATS_UL_QUEUE_LEN in server response");
                
                t = p.getTagByName(ECCodes.EC_TAG_STATS_TOTAL_SRC_COUNT);
                if (t != null) totalSrcCount = t.getTagValueUInt();
                else throw new ECTagParsingException("Missing EC_TAG_STATS_TOTAL_SRC_COUNT in server response");
                
                t = p.getTagByName(ECCodes.EC_TAG_STATS_ED2K_USERS);
                if (t != null) ed2kUsers = t.getTagValueUInt();
                else throw new ECTagParsingException("Missing EC_TAG_STATS_ED2K_USERS in server response");
                
                t = p.getTagByName(ECCodes.EC_TAG_STATS_KAD_USERS);
                if (t != null) kadUsers = t.getTagValueUInt();
                else throw new ECTagParsingException("Missing EC_TAG_STATS_KAD_USERS in server response");
                
                t = p.getTagByName(ECCodes.EC_TAG_STATS_ED2K_FILES);
                if (t != null) ed2kFiles = t.getTagValueUInt();
                else throw new ECTagParsingException("Missing EC_TAG_STATS_ED2K_FILES in server response");
                
                t = p.getTagByName(ECCodes.EC_TAG_STATS_KAD_FILES);
                if (t != null) kadFiles = t.getTagValueUInt();
                else throw new ECTagParsingException("Missing EC_TAG_STATS_KAD_FILES in server response");
                
                t = p.getTagByName(ECCodes.EC_TAG_CONNSTATE);
                if (t != null) connState = new ECConnState(t, detailLevel);
                else throw new ECTagParsingException("Missing EC_TAG_CONNSTATE in server response");
                
                break;
                
            default:
                throw new ECTagParsingException("Unknown detail level " + detailLevel + " for EC_STATS");

            }
            
        } catch (DataFormatException e) {
            throw new ECTagParsingException("One or more unexpected type in EC_STATS tags", e);
        }
        
    }


    public ECStats() {
    }


    public long getUpOverhead() {
        return detailLevel;
    }


    public long getDownOverhead() {
        return downOverhead;
    }


    public long getBannedCount() {
        return bannedCount;
    }


    public long getUlSpeed() {
        return ulSpeed;
    }


    public long getDlSpeed() {
        return dlSpeed;
    }


    public long getUlSpeedLimit() {
        return ulSpeedLimit;
    }


    public long getDlSpeedLimit() {
        return dlSpeedLimit;
    }


    public long getUlQueueLen() {
        return ulQueueLen;
    }


    public long getTotalSrcCount() {
        return totalSrcCount;
    }


    public long getEd2kUsers() {
        return ed2kUsers;
    }


    public long getKadUsers() {
        return kadUsers;
    }


    public long getEd2kFiles() {
        return ed2kFiles;
    }


    public long getKadFiles() {
        return kadFiles;
    }


    public ECConnState getConnState() {
        return connState;
    }

    @Override
    public String toString() {
        return String.format(
                        "ECStats [detailLevel=%s, upOverhead=%s, downOverhead=%s, bannedCount=%s, ulSpeed=%s, dlSpeed=%s, ulSpeedLimit=%s, dlSpeedLimit=%s, ulQueueLen=%s, totalSrcCount=%s, ed2kUsers=%s, kadUsers=%s, ed2kFiles=%s, kadFiles=%s, connState=%s]",
                        detailLevel, upOverhead, downOverhead, bannedCount, ulSpeed, dlSpeed, ulSpeedLimit, dlSpeedLimit, ulQueueLen, totalSrcCount, ed2kUsers, kadUsers,
                        ed2kFiles, kadFiles, connState);
    }
    
    
    
}

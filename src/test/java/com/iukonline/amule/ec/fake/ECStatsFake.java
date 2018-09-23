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

package com.iukonline.amule.ec.fake;

import java.util.zip.DataFormatException;

import com.iukonline.amule.ec.ECCodes;
import com.iukonline.amule.ec.ECPacket;
import com.iukonline.amule.ec.ECStats;
import com.iukonline.amule.ec.ECTag;
import com.iukonline.amule.ec.exceptions.ECTagParsingException;

public class ECStatsFake extends ECStats {
    
    public ECStatsFake(ECPacket p, byte d) throws ECTagParsingException  {
        
        detailLevel = d;
        ECTag t;

        try {
            
                t = p.getTagByName(ECCodes.EC_TAG_STATS_UL_SPEED);
                if (t != null) ulSpeed = t.getTagValueUInt();
                else throw new ECTagParsingException("Missing EC_TAG_STATS_UL_SPEED in server response");
                
                t = p.getTagByName(ECCodes.EC_TAG_STATS_DL_SPEED);
                if (t != null) dlSpeed = t.getTagValueUInt();
                else throw new ECTagParsingException("Missing EC_TAG_STATS_DL_SPEED in server response");
            
        } catch (DataFormatException e) {
            throw new ECTagParsingException("One or more unexpected type in EC_STATS tags", e);
        }
        
    }

    

}

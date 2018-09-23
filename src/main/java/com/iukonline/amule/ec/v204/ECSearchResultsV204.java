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

package com.iukonline.amule.ec.v204;

import java.util.ArrayList;
import java.util.zip.DataFormatException;

import com.iukonline.amule.ec.ECSearchFile;
import com.iukonline.amule.ec.ECSearchResults;
import com.iukonline.amule.ec.ECTag;
import com.iukonline.amule.ec.exceptions.ECTagParsingException;

public class ECSearchResultsV204 extends ECSearchResults {
    
    public ECSearchResultsV204(ArrayList <ECTag> tagList) throws ECTagParsingException {
        updateSearchResults(tagList);
    }
    
    public ECSearchResultsV204(byte detail) {
    }
    
    public void updateSearchResults(ArrayList <ECTag> tagList) throws ECTagParsingException {
        
        for (ECTag t : tagList) {
            if (t.getTagName() != ECCodesV204.EC_TAG_SEARCHFILE) throw new ECTagParsingException("Unexpected tag " + t.getTagName() + " found while looking for EC_TAG_SEARCHFILE");

            long id;
            try {
                id = t.getTagValueUInt();
            } catch (DataFormatException e) {
                throw new ECTagParsingException("Unexpected tag type for EC_TAG_SEARCHFILE", e);
            }
            
            if (resultMap.containsKey(id)) {
                resultMap.get(id).updateFromECTag(t);
            } else {
                resultMap.put(id, new ECSearchFileV204(t));
            }
        }
        
        for (ECSearchFile s : resultMap.values()) {
            ECSearchFileV204 s204 = (ECSearchFileV204) s;
            long parentId = s204.getParentId();
            if (parentId > 0) {
                ECSearchFileV204 p = (ECSearchFileV204) resultMap.get(parentId);
                if (p == null) throw new ECTagParsingException("Search result is referring to an unknown parent " + parentId);
                s204.setParent(p);
                p.addChild(s204);
            }
        }
        
    }

    @Override
    public String toString() {
        return String.format("ECSearchResultsV204 [resultMap=%s]", resultMap);
    }
    
    
}

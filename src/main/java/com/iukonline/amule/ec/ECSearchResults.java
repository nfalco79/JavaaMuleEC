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

import java.util.ArrayList;
import java.util.HashMap;

import com.iukonline.amule.ec.exceptions.ECTagParsingException;


public class ECSearchResults {
    
    public HashMap<Object, ECSearchFile> resultMap = new HashMap<Object, ECSearchFile>();
    
    public ECSearchResults(ArrayList <ECTag> tagList) throws ECTagParsingException {
        updateSearchResults(tagList);
    }
    
    public ECSearchResults(byte detail) {
    }
    
    public ECSearchResults() {
    }

    public void updateSearchResults(ArrayList <ECTag> tagList) throws ECTagParsingException {
        
    }
    
    public int getTotalSources() {
        int count = 0;
        for (ECSearchFile s : resultMap.values()) {
            count += s.getSourceCount();
        }
        return count;
    }
    
}

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
import com.iukonline.amule.ec.ECTag;
import com.iukonline.amule.ec.exceptions.ECTagParsingException;


public class ECSearchFileV204 extends ECSearchFile {
    
    protected long id = -1;
    protected long parentId;

    protected ECSearchFileV204 parent;
    protected ArrayList<ECSearchFileV204> children;
    
    public ECSearchFileV204(ECTag st) throws ECTagParsingException {
        updateFromECTag(st);
    }
    
    public void updateFromECTag(ECTag st) throws ECTagParsingException {
        
        try {
            if (id >= 0 && id != st.getTagValueUInt()) throw new ECTagParsingException("Trying to update search result with different id");
            ECTag t;
            
            t = st.getSubTagByName(ECCodesV204.EC_TAG_PARTFILE_NAME);
            if (t != null) fileName = t.getTagValueString();
            else if (id < 0) throw new ECTagParsingException("Missing EC_TAG_PARTFILE_NAME in server response");           
            
            t = st.getSubTagByName(ECCodesV204.EC_TAG_PARTFILE_SIZE_FULL);
            if (t != null) sizeFull = t.getTagValueUInt();
            else if (id < 0) throw new ECTagParsingException("Missing EC_TAG_PARTFILE_SIZE_FULL in server response");
            
            t = st.getSubTagByName(ECCodesV204.EC_TAG_PARTFILE_HASH);
            if (t != null) hash = t.getTagValueHash();
            else if (id < 0) throw new ECTagParsingException("Missing EC_TAG_PARTFILE_SIZE_HASH in server response");
            
            t = st.getSubTagByName(ECCodesV204.EC_TAG_SEARCH_PARENT);
            if (t != null) parentId = t.getTagValueUInt();
            else if (id < 0) parentId = -1;

            
            t = st.getSubTagByName(ECCodesV204.EC_TAG_PARTFILE_SOURCE_COUNT);
            if (t != null) sourceCount = (int) t.getTagValueUInt();
            else if (id < 0) throw new ECTagParsingException("Missing EC_TAG_PARTFILE_SOURCE_COUNT in server response");

            t = st.getSubTagByName(ECCodesV204.EC_TAG_PARTFILE_SOURCE_COUNT_XFER);
            if (t != null) sourceXfer = (int) t.getTagValueUInt();
            else if (id < 0) throw new ECTagParsingException("Missing EC_TAG_PARTFILE_SOURCE_COUNT_XFER in server response");

            t = st.getSubTagByName(ECCodesV204.EC_TAG_PARTFILE_STATUS);
            if (t != null) status = (byte) t.getTagValueUInt();
            else if (id < 0) throw new ECTagParsingException("Missing EC_TAG_PARTFILE_STATUS in server response");
                
            id = st.getTagValueUInt();
            
        } catch (DataFormatException e) {
            throw new ECTagParsingException("One or more unexpected type in EC_TAG_SEARCHFILE tag", e);        
        } 
        
    }

    public long getId() {
        return id;
    }

    public long getParentId() {
        return parentId;
    }

    public int getSourceCount() {
        if (children == null || children.isEmpty()) { 
            return sourceCount;
        } else {
            int c = 0;
            for (ECSearchFileV204 child : children) c += child.getSourceCount();
            return c;
        }
           
    }

    public int getSourceXfer() {
        if (children == null || children.isEmpty()) { 
            return sourceXfer;
        } else {
            int c = 0;
            for (ECSearchFileV204 child : children) c += child.getSourceXfer();
            return c;
        }
    }

    public ECSearchFileV204 getParent() {
        return parent;
    }

    public void setParent(ECSearchFileV204 parent) {
        this.parent = parent;
    }
    
    public void addChild(ECSearchFileV204 child) {
        if (children == null) children = new ArrayList <ECSearchFileV204>();
        if (! children.contains(child)) {
            children.add(child);
        }
    }

    @Override
    public String toString() {
        return String.format("ECSearchFileV204 [id=%s, parent=%s, hash=%s, fileName=%s, status=%s, sourceCount=%s, sourceXfer=%s, sizeFull=%s]", id, parent,
                        hash, fileName, status, sourceCount, sourceXfer, sizeFull);
    }
    
}

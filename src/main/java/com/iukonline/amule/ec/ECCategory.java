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

public class ECCategory {
    
    public static final long NEW_CATEGORY_ID = 0xffffffffL;

    private byte detailLevel;
    
    private long id = NEW_CATEGORY_ID;
    private String title;
    private String path;
    private String comment;
    private long color;
    private byte prio;
    
    public ECCategory(String t, String p1, String c1, long c2, byte p2) {
        title = t;
        path = p1;
        comment = c1;
        color = c2;
        prio = p2;
    }
    
    public ECCategory(long id, String t, String p1, String c1, long c2, byte p2) {
    	this.id = id;
        title = t;
        path = p1;
        comment = c1;
        color = c2;
        prio = p2;
    }
    
    public ECCategory(ECTag t1, byte d) throws ECTagParsingException {
        
        detailLevel = d;
        ECTag t;

        try {
            
            switch (detailLevel) {
            case ECCodes.EC_DETAIL_FULL:
            case ECCodes.EC_DETAIL_WEB:
                
                t = t1.getSubTagByName(ECCodes.EC_TAG_CATEGORY_PATH);
                if (t != null) path = t.getTagValueString();
                else throw new ECTagParsingException("Missing EC_TAG_CATEGORY_PATH in server response");
                
                t = t1.getSubTagByName(ECCodes.EC_TAG_CATEGORY_COMMENT);
                if (t != null) comment = t.getTagValueString();
                else throw new ECTagParsingException("Missing EC_TAG_CATEGORY_COMMENT in server response");
                
                
                t = t1.getSubTagByName(ECCodes.EC_TAG_CATEGORY_COLOR);
                if (t != null) color = t.getTagValueUInt();
                else throw new ECTagParsingException("Missing EC_TAG_CATEGORY_COLOR in server response");
                
                t = t1.getSubTagByName(ECCodes.EC_TAG_CATEGORY_PRIO);
                if (t != null) prio = (byte) t.getTagValueUInt();
                else throw new ECTagParsingException("Missing EC_TAG_CATEGORY_PRIO in server response");
                
                
            case ECCodes.EC_DETAIL_CMD:
                
                id = t1.getTagValueUInt();
                
                t = t1.getSubTagByName(ECCodes.EC_TAG_CATEGORY_TITLE);
                if (t != null) title = t.getTagValueString();
                else throw new ECTagParsingException("Missing EC_TAG_CATEGORY_TITLE in server response");
                
                break;
                
            default:
                throw new ECTagParsingException("Unknown detail level " + detailLevel + " for EC_TAG_SERVER");

            }
            
        } catch (DataFormatException e) {
            throw new ECTagParsingException("One or more unexpected type in EC_TAG_PREFS_CATEGORIES tags", e);
        }

    }
    
    public ECTag toECTag() {
        
        
        ECTag t = null;
        
        try {
            t = new ECTag(ECCodes.EC_TAG_CATEGORY, ECTagTypes.EC_TAGTYPE_UINT32, id);
            t.addSubTag(new ECTag(ECCodes.EC_TAG_CATEGORY_TITLE, ECTagTypes.EC_TAGTYPE_STRING, title));
            t.addSubTag(new ECTag(ECCodes.EC_TAG_CATEGORY_PATH, ECTagTypes.EC_TAGTYPE_STRING, path));
            t.addSubTag(new ECTag(ECCodes.EC_TAG_CATEGORY_COMMENT, ECTagTypes.EC_TAGTYPE_STRING, comment));
            t.addSubTag(new ECTag(ECCodes.EC_TAG_CATEGORY_COLOR, ECTagTypes.EC_TAGTYPE_UINT32, color));
            t.addSubTag(new ECTag(ECCodes.EC_TAG_CATEGORY_PRIO, ECTagTypes.EC_TAGTYPE_UINT8, prio));
        } catch (DataFormatException e) {
        }
        
        return t;
    }
    
    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getPath() {
        return path;
    }
    
    public String getComment() {
        return comment;
    }

    public long getColor() {
        return color;
    }

    public byte getPrio() {
        return prio;
    }
    
    

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setColor(long color) {
        this.color = color;
    }

    public void setPrio(byte prio) {
        this.prio = prio;
    }

    @Override
    public String toString() {
        return String.format("ECCategory [detailLevel=%s, id=%s, title=%s, path=%s, color=%s, prio=%s]", detailLevel, id, title, path, color, prio);
    }

    
    
    
    
    
}

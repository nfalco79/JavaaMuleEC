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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;

import com.iukonline.amule.ec.exceptions.ECPacketParsingException;



public class ECPacket {
    
    private byte opCode = ECCodes.EC_OP_NOOP;
    private ArrayList<ECTag> tags;
    
    
    
    // Default values;
    private boolean isUTF8Compressed = false;
    private boolean isZlibCompressed = false;
    private boolean hasId = false;
    private boolean acceptsUTF8 = true;
    private boolean acceptsZlib = true;
    
    private ECRawPacket encodedPacket;
    
    public ECRawPacket getEncodedPacket() {
        return encodedPacket;
    }

    public ECPacket() {
        tags = new ArrayList<ECTag>();
    }
    
    public void setUTF8Compressed(boolean isUTF8Compressed) { this.isUTF8Compressed = isUTF8Compressed; }
    public boolean isUTF8Compressed() { return isUTF8Compressed; }
   
    public boolean isZlibCompressed() { return isZlibCompressed; }
    public void setZlibCompressed(boolean isZlibCompressed) { this.isZlibCompressed = isZlibCompressed; }

    public boolean hasId() { return hasId; }
    public void setHasId(boolean hasId) { this.hasId = hasId; }

    public boolean acceptsUTF8() { return acceptsUTF8; }
    public void setAcceptsUTF8(boolean acceptsUTF8) { this.acceptsUTF8 = acceptsUTF8; }

    public boolean acceptsZlib() { return acceptsZlib; }
    public void setAcceptsZlib(boolean acceptsZlib) { this.acceptsZlib = acceptsZlib; }

    public byte getOpCode() { return opCode; }
    public void setOpCode(byte opCode) { this.opCode = opCode; }
    
    public ECRawPacket getRawPacket() { return encodedPacket; }
    
    public void addTag(ECTag tag) { tags.add(tag); }
    
    public ArrayList<ECTag> getTags() { return tags; }


    public ECTag getTagByName(short tagName) {
        if (! tags.isEmpty()) {            
            Iterator<ECTag> itr = tags.iterator();
            while(itr.hasNext()) {
                ECTag tag = itr.next();
                if (tag.getTagName() == tagName) {
                    return tag;
                }
            }
        }
        return null;
    }
    

    public void writeToStream(OutputStream out, Class <? extends ECRawPacket> parser) throws ECPacketParsingException, IOException   {
        //encodedPacket = new ECRawPacket(this);
        
        try {
            encodedPacket = parser.getConstructor(ECPacket.class).newInstance(this);
        } catch (Exception e) {
            throw new ECPacketParsingException("Cannot get a packet parser", null, e);
        }
        out.write(encodedPacket.asByteArray());
    }
    
    public void writeToStream(OutputStream out) throws ECPacketParsingException, IOException {
        writeToStream(out, ECRawPacket.class);
    }
    
    public static ECPacket readFromStream(InputStream in, Class <? extends ECRawPacket> parser) throws IOException, ECPacketParsingException {
        
        ECRawPacket raw;
        try {
            raw = parser.getConstructor(InputStream.class).newInstance(in);
        } catch (InvocationTargetException e) {
            Throwable cause =  e.getCause();
            if (cause instanceof IOException) {
                throw ((IOException) cause);
            }
            else if (cause instanceof ECPacketParsingException) {
                throw ((ECPacketParsingException) cause);
            } else {
                throw new ECPacketParsingException("Cannot get a packet parser", null, cause);
            }
        } catch (Exception e) {
            throw new ECPacketParsingException("Cannot get a packet parser", null, e);
        }
        
        //ECRawPacket raw = new ECRawPacket(in);
        ECPacket n;
        n = raw.parse();
        n.encodedPacket = raw;
        return n;

    }
    
    public static ECPacket readFromStream(InputStream in) throws IOException, ECPacketParsingException {
        return readFromStream(in, ECRawPacket.class);
    }    
    
    
    @Override
    public String toString() {


        
        StringBuilder out = new StringBuilder();
        Formatter f = new Formatter(out);
        f.format("isUTF8Compressed: %s, isZlibCompressed: %s, hasId: %s, acceptsUTF8: %s, acceptsZlib: %s\n", isUTF8Compressed, isZlibCompressed, hasId, acceptsUTF8, acceptsZlib);
        f.close();
        out.append("OP Code: <" + Integer.toHexString(opCode) + ">\n");
        
        if (! tags.isEmpty()) {           
            Iterator<ECTag> itr = tags.iterator();
            while(itr.hasNext()) {
                    out.append(itr.next().toString());
            }
        }
        
        if (encodedPacket != null) {
            out.append("Encoded packet\n");
            out.append(encodedPacket.dump());
        }
        
        return out.toString();
    }
    
}

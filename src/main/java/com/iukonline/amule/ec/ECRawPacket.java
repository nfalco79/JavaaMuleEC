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
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.CharacterCodingException;
import java.util.ArrayList;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import com.iukonline.amule.ec.exceptions.ECPacketParsingException;
import com.iukonline.amule.ec.exceptions.ECTagParsingException;
import com.iukonline.amule.ec.exceptions.ECDebugException;

public class ECRawPacket {
    
    final static int hexPerRow = 16;
    final static int EC_FLAG_DEFAULT_ACCEPTS = ECCodes.EC_FLAG_UTF8_NUMBERS | ECCodes.EC_FLAG_ZLIB;  
    
    protected byte [] rawFlags;
    protected byte [] rawAccepts;
    protected byte [] rawLen;
    protected byte [] rawPayload;
    protected byte [] compressedPayload;
    protected ECRawTag[] rawTagsList;
    
    long flags = 0x20L;
    long accepts = EC_FLAG_DEFAULT_ACCEPTS;
    int len;
    int compressedLen;

    
    int opCodeIndex = 0;
    int tagsCountIndex = 1;
    int tagsIndex;
    
    boolean debug = false;
    
    protected ECRawTag getNewECRawTag(ECTag t, int startIndex) throws ECPacketParsingException {
        return new ECRawTag(t, startIndex);
    }
    
    protected ECRawTag getNewECRawTag(int index)  {
        return new ECRawTag(index);
    }

    public ECRawPacket (InputStream is) throws IOException, ECPacketParsingException {
        
        rawFlags = new byte[4];
        ECUtils.readAllBytes(is, rawFlags, 0, 4, debug);
        if (debug) System.out.println("ECRawPacket: Got transmission flags: " + ECUtils.byteArrayToHexString(rawFlags));
        flags = ECUtils.bytesToUint(rawFlags, 4, true);
        
        if ((flags & ECCodes.EC_FLAG_UNKNOWN_MASK) != 0 ) throw new ECPacketParsingException("Unknown trasmission flags", this);

        rawAccepts = new byte[4];
        if (hasAccepts()) {
            ECUtils.readAllBytes(is, rawAccepts, 0, 4, debug);
            if (debug) System.out.println("ECRawPacket: Got accepts flags: " + ECUtils.byteArrayToHexString(rawAccepts));
            accepts = ECUtils.bytesToUint(rawAccepts, 4, true);
        }
        
        rawLen = new byte[4];
        ECUtils.readAllBytes(is, rawLen, 0, 4, debug);
        if (debug) System.out.println("ECRawPacket: Got length: " + ECUtils.byteArrayToHexString(rawLen));
        // TODO: Handle packet with len > 2,147,483,647 (max int)? Doesn't seem to be useful...
        len = (int) ECUtils.bytesToUint(rawLen, 4, true);
        
        if (len == 0) throw new ECPacketParsingException("Invalid EC Packet payload length (0)", this);
        
        if (isZlibCompressed()) {
            
            if (debug) System.out.println("ECRawPacket: Payload is compressed!");
            
            compressedPayload = new byte[len];
            ECUtils.readAllBytes(is, compressedPayload , 0, len, debug);
            compressedLen = len;
            

            Inflater unzipper = new Inflater();
            unzipper.setInput(compressedPayload);
            
            byte[] buf = new byte[8192];
            int read = 0;
            try {
                read = unzipper.inflate(buf);
            } catch (DataFormatException e) {
                throw new ECPacketParsingException("Error inflating zlib compressed payload", this, e);
            }
            int totRead = 0;
            
            while (read > 0) {
                if (debug) System.out.println("ECRawPacket: " + read + " more bytes extracted...");
                
                totRead += read;

                byte[] tmpPayload = rawPayload;
                rawPayload = new byte[totRead];
                if (tmpPayload != null) {
                    System.arraycopy(tmpPayload, 0, rawPayload, 0, tmpPayload.length);
                    System.arraycopy(buf, 0, rawPayload, tmpPayload.length, read);
                } else {
                    System.arraycopy(buf, 0, rawPayload, 0, read);
                }

                try {
                    read = unzipper.inflate(buf);
                } catch (DataFormatException e) {
                    throw new ECPacketParsingException("Error inflating zlib compressed payload", this, e);
                }
            }
            len = rawPayload.length;
            
        } else {
        
            rawPayload = new byte[len];
            ECUtils.readAllBytes(is, rawPayload, 0, len, debug);
        }
        if (debug) System.out.println("ECRawPacket: Got payload: " + ECUtils.byteArrayToHexString(rawPayload));
        
    }
    
    public ECRawPacket (ECPacket p) throws ECPacketParsingException  {
        
        // TODO: UTF-8 Compression

        setUTF8Compressed(p.isUTF8Compressed());
        setZlibCompressed(p.isZlibCompressed());
        setAcceptsUTF8(p.acceptsUTF8());
        setAcceptsZlib(p.acceptsZlib());
        setHasAccepts(accepts != EC_FLAG_DEFAULT_ACCEPTS);
        
        rawFlags = ECUtils.uintToBytes(flags, 4, true);
        rawAccepts = ECUtils.uintToBytes(accepts, 4, true);

        
        ArrayList <ECTag> tags = p.getTags();
        
        int tagsLen = 0;
        for (int i = 0; i < tags.size(); i++) {
            // TODO Brutto, trovare altra soluzione che non richieda implementazione in ECTag
            try {
                tagsLen += tags.get(i).getLength(true, false);
            } catch (ECTagParsingException e) {
                // TODO All getLength exceptions are related to UTF-8, so no need to catch them until UTF-8 Compression is implemented 

            }
        }
        
        
        
        tagsIndex = 3; // opCode (1) + tagsCount(2)
        len = tagsIndex + tagsLen; 

        if (debug) System.out.println("ECRawPacket: total size will be " + len);
        
        rawLen = ECUtils.uintToBytes(len, 4, true);
        
        rawPayload = new byte[len];
        
        if (debug) System.out.println("ECRawPacket: packet dump before starting payload:\n" + this.dump());   
        
        rawPayload[0] = p.getOpCode();
        if (debug) System.out.println("ECRawPacket: packet dump after opCode:\n" + this.dump());
        System.arraycopy(ECUtils.uintToBytes(tags.size(), 2, true), 0, rawPayload, 1, 2);
        if (debug) System.out.println("ECRawPacket: packet dump after tagsCount:\n" + this.dump());   
        
        
        if (tags.size() > 0) {
            rawTagsList = new ECRawTag[tags.size()];
            int index = tagsIndex;
            for (int i = 0; i < tags.size(); i++) {
                rawTagsList[i] = getNewECRawTag(tags.get(i), index);
                if (debug) System.out.println("ECRawPacket: packet dump after tag " + i + ":\n" + this.dump());
                index = rawTagsList[i].tagEnd + 1;
            }
        }
        
        
    }
    
    
    
    public ECPacket parse() throws ECPacketParsingException {
        try {
            tagsIndex = tagsCountIndex + (isUTF8Compressed() ? ECUtils.getUTF8SequenceLength(rawPayload[tagsCountIndex], debug) : 2);
        } catch (CharacterCodingException e) {
            throw new ECPacketParsingException("Error parsing tags count - Invalid UTF8 sequence.", this, e);
        }
        if (debug) System.out.println("ECRawPacket.parse: tagsIndex = " + tagsIndex);
        int tagsCount = getTagsCount();
        if (debug) System.out.println("ECRawPacket.parse: tagsCount = " + tagsCount);
        int index = tagsIndex;
        int tagCounter = 0;
        rawTagsList = new ECRawTag[tagsCount];
        
        ECPacket ep = new ECPacket();
        
        ep.setUTF8Compressed(isUTF8Compressed());
        ep.setZlibCompressed(isZlibCompressed());
        ep.setHasId(hasId());
        ep.setAcceptsUTF8(acceptsUTF8());
        ep.setAcceptsZlib(acceptsZlib());
        
        ep.setOpCode(getOpCode());
       
        while (index < rawPayload.length && tagCounter < tagsCount) {
            if (debug) System.out.println("ECRawPacket.parse: parsing tag " + tagCounter + " starting at " + index);
            ECRawTag t = getNewECRawTag(index);
            ep.addTag(t.parse());
            index = t.getTagEnd() + 1;
            rawTagsList[tagCounter++] = t;
        }
        
        if (index != rawPayload.length) {
            throw new ECPacketParsingException("Error parsing tags list - Expected len " + rawPayload.length + " found length " + index, this);
        }
        
        if (tagCounter != tagsCount) {
            throw new ECPacketParsingException("Error parsing tags list - Expected tags " + tagsCount + " found tags " + tagCounter, this);
        }
        
        return ep;
    }
     
    public byte[] asByteArray() {
        
        // TODO Zlib Compression
        
        int totLen = rawPayload.length + 8 + (hasAccepts() ? 4 : 0);
        byte[] ret = new byte[totLen];
        
        System.arraycopy(rawFlags, 0, ret, 0, 4);
        int index = 4;
        if (hasAccepts()) {
            index = 8;
            System.arraycopy(rawAccepts, 0, ret, 4, 4);
        }
        System.arraycopy(rawLen, 0, ret, index, 4);
        System.arraycopy(rawPayload, 0, ret, index + 4, rawPayload.length);
        
        return ret;
    }
    
    public boolean hasAccepts() { return (flags & ECCodes.EC_FLAG_ACCEPTS) == ECCodes.EC_FLAG_ACCEPTS; }
    public void setHasAccepts( boolean hasAccepts ) { 
        if (debug) System.out.println("Setting hasAccepts to " + hasAccepts);
        if (hasAccepts) {
            flags |= ECCodes.EC_FLAG_ACCEPTS; 
        } else {
            if (hasAccepts()) flags ^= ECCodes.EC_FLAG_ACCEPTS;
        }
        if (debug) System.out.println("hasAccepts now is " + hasAccepts());
    }
    
    
    public boolean isUTF8Compressed() { return (flags & ECCodes.EC_FLAG_UTF8_NUMBERS) == ECCodes.EC_FLAG_UTF8_NUMBERS; }
    public void setUTF8Compressed( boolean isUTF8Compressed ) {
        if (isUTF8Compressed) {
            flags |= ECCodes.EC_FLAG_UTF8_NUMBERS; 
        } else {
            if (isUTF8Compressed()) flags ^= ECCodes.EC_FLAG_UTF8_NUMBERS;
        }
    }
    
    public boolean isZlibCompressed() { return (flags & ECCodes.EC_FLAG_ZLIB) == ECCodes.EC_FLAG_ZLIB; }
    public void setZlibCompressed( boolean isZlibCompressed ) { 
       if (isZlibCompressed) {
            flags |= ECCodes.EC_FLAG_ZLIB; 
        } else {
            if (isZlibCompressed()) flags ^= ECCodes.EC_FLAG_ZLIB;
        }
    }


    public boolean hasId() { return (flags & ECCodes.EC_FLAG_HAS_ID) == ECCodes.EC_FLAG_HAS_ID; }
    
    public boolean acceptsUTF8() { return (hasAccepts() ? true : (accepts & ECCodes.EC_FLAG_UTF8_NUMBERS) == ECCodes.EC_FLAG_UTF8_NUMBERS); }
    public void setAcceptsUTF8(boolean acceptsUTF8) {
        if (acceptsUTF8) {
            accepts |= ECCodes.EC_FLAG_UTF8_NUMBERS; 
        } else {
            if (acceptsUTF8()) accepts ^= ECCodes.EC_FLAG_UTF8_NUMBERS;
        }
    }

    public boolean acceptsZlib() { return (hasAccepts() ? true : (accepts & ECCodes.EC_FLAG_ZLIB) == ECCodes.EC_FLAG_ZLIB); }
    public void setAcceptsZlib( boolean acceptsZlib ) { 
        if (acceptsZlib) {
            accepts |= ECCodes.EC_FLAG_ZLIB; 
        } else {
            if (acceptsZlib()) accepts ^= ECCodes.EC_FLAG_ZLIB;
        }
    }

    
    public byte getOpCode() {
        return rawPayload[0];
    }
    
    public int getTagsCount() throws ECPacketParsingException {
        if (! isUTF8Compressed()) {
            return (int) ECUtils.bytesToUint(rawPayload, tagsCountIndex, tagsIndex - tagsCountIndex, true, debug);
        } else {
            try {
                return (int) ECUtils.decodeUTF8number(rawPayload, tagsCountIndex, debug);
            } catch (CharacterCodingException e) {
                throw new ECPacketParsingException("Error parsing tags count - invalid encoding.", this, e);
            }
        }
    }  
    
    protected String getOpCodeString() {
        switch(rawPayload[0]) {
        case ECCodes.EC_OP_NOOP: return "EC_OP_NOOP";
        case ECCodes.EC_OP_AUTH_REQ: return "EC_OP_AUTH_REQ";
        case ECCodes.EC_OP_AUTH_FAIL: return "EC_OP_AUTH_FAIL";
        case ECCodes.EC_OP_AUTH_OK: return "EC_OP_AUTH_OK";
        case ECCodes.EC_OP_FAILED: return "EC_OP_FAILED";
        case ECCodes.EC_OP_STRINGS: return "EC_OP_STRINGS";
        case ECCodes.EC_OP_MISC_DATA: return "EC_OP_MISC_DATA";
        case ECCodes.EC_OP_SHUTDOWN: return "EC_OP_SHUTDOWN";
        case ECCodes.EC_OP_ADD_LINK: return "EC_OP_ADD_LINK";
        case ECCodes.EC_OP_STAT_REQ: return "EC_OP_STAT_REQ";
        case ECCodes.EC_OP_GET_CONNSTATE: return "EC_OP_GET_CONNSTATE";
        case ECCodes.EC_OP_STATS: return "EC_OP_STATS";
        case ECCodes.EC_OP_GET_DLOAD_QUEUE: return "EC_OP_GET_DLOAD_QUEUE";
        case ECCodes.EC_OP_GET_ULOAD_QUEUE: return "EC_OP_GET_ULOAD_QUEUE";
        case ECCodes.EC_OP_GET_WAIT_QUEUE: return "EC_OP_GET_WAIT_QUEUE";
        case ECCodes.EC_OP_GET_SHARED_FILES: return "EC_OP_GET_SHARED_FILES";
        case ECCodes.EC_OP_SHARED_SET_PRIO: return "EC_OP_SHARED_SET_PRIO";
        case ECCodes.EC_OP_PARTFILE_REMOVE_NO_NEEDED: return "EC_OP_PARTFILE_REMOVE_NO_NEEDED";
        case ECCodes.EC_OP_PARTFILE_REMOVE_FULL_QUEUE: return "EC_OP_PARTFILE_REMOVE_FULL_QUEUE";
        case ECCodes.EC_OP_PARTFILE_REMOVE_HIGH_QUEUE: return "EC_OP_PARTFILE_REMOVE_HIGH_QUEUE";
        case ECCodes.EC_OP_PARTFILE_CLEANUP_SOURCES: return "EC_OP_PARTFILE_CLEANUP_SOURCES";
        case ECCodes.EC_OP_PARTFILE_SWAP_A4AF_THIS: return "EC_OP_PARTFILE_SWAP_A4AF_THIS";
        case ECCodes.EC_OP_PARTFILE_SWAP_A4AF_THIS_AUTO: return "EC_OP_PARTFILE_SWAP_A4AF_THIS_AUTO";
        case ECCodes.EC_OP_PARTFILE_SWAP_A4AF_OTHERS: return "EC_OP_PARTFILE_SWAP_A4AF_OTHERS";
        case ECCodes.EC_OP_PARTFILE_PAUSE: return "EC_OP_PARTFILE_PAUSE";
        case ECCodes.EC_OP_PARTFILE_RESUME: return "EC_OP_PARTFILE_RESUME";
        case ECCodes.EC_OP_PARTFILE_STOP: return "EC_OP_PARTFILE_STOP";
        case ECCodes.EC_OP_PARTFILE_PRIO_SET: return "EC_OP_PARTFILE_PRIO_SET";
        case ECCodes.EC_OP_PARTFILE_DELETE: return "EC_OP_PARTFILE_DELETE";
        case ECCodes.EC_OP_PARTFILE_SET_CAT: return "EC_OP_PARTFILE_SET_CAT";
        case ECCodes.EC_OP_DLOAD_QUEUE: return "EC_OP_DLOAD_QUEUE";
        case ECCodes.EC_OP_ULOAD_QUEUE: return "EC_OP_ULOAD_QUEUE";
        case ECCodes.EC_OP_WAIT_QUEUE: return "EC_OP_WAIT_QUEUE";
        case ECCodes.EC_OP_SHARED_FILES: return "EC_OP_SHARED_FILES";
        case ECCodes.EC_OP_SHAREDFILES_RELOAD: return "EC_OP_SHAREDFILES_RELOAD";
        case ECCodes.EC_OP_SHAREDFILES_ADD_DIRECTORY: return "EC_OP_SHAREDFILES_ADD_DIRECTORY";
        case ECCodes.EC_OP_RENAME_FILE: return "EC_OP_RENAME_FILE";
        case ECCodes.EC_OP_SEARCH_START: return "EC_OP_SEARCH_START";
        case ECCodes.EC_OP_SEARCH_STOP: return "EC_OP_SEARCH_STOP";
        case ECCodes.EC_OP_SEARCH_RESULTS: return "EC_OP_SEARCH_RESULTS";
        case ECCodes.EC_OP_SEARCH_PROGRESS: return "EC_OP_SEARCH_PROGRESS";
        case ECCodes.EC_OP_DOWNLOAD_SEARCH_RESULT: return "EC_OP_DOWNLOAD_SEARCH_RESULT";
        case ECCodes.EC_OP_IPFILTER_RELOAD: return "EC_OP_IPFILTER_RELOAD";
        case ECCodes.EC_OP_GET_SERVER_LIST: return "EC_OP_GET_SERVER_LIST";
        case ECCodes.EC_OP_SERVER_LIST: return "EC_OP_SERVER_LIST";
        case ECCodes.EC_OP_SERVER_DISCONNECT: return "EC_OP_SERVER_DISCONNECT";
        case ECCodes.EC_OP_SERVER_CONNECT: return "EC_OP_SERVER_CONNECT";
        case ECCodes.EC_OP_SERVER_REMOVE: return "EC_OP_SERVER_REMOVE";
        case ECCodes.EC_OP_SERVER_ADD: return "EC_OP_SERVER_ADD";
        case ECCodes.EC_OP_SERVER_UPDATE_FROM_URL: return "EC_OP_SERVER_UPDATE_FROM_URL";
        case ECCodes.EC_OP_ADDLOGLINE: return "EC_OP_ADDLOGLINE";
        case ECCodes.EC_OP_ADDDEBUGLOGLINE: return "EC_OP_ADDDEBUGLOGLINE";
        case ECCodes.EC_OP_GET_LOG: return "EC_OP_GET_LOG";
        case ECCodes.EC_OP_GET_DEBUGLOG: return "EC_OP_GET_DEBUGLOG";
        case ECCodes.EC_OP_GET_SERVERINFO: return "EC_OP_GET_SERVERINFO";
        case ECCodes.EC_OP_LOG: return "EC_OP_LOG";
        case ECCodes.EC_OP_DEBUGLOG: return "EC_OP_DEBUGLOG";
        case ECCodes.EC_OP_SERVERINFO: return "EC_OP_SERVERINFO";
        case ECCodes.EC_OP_RESET_LOG: return "EC_OP_RESET_LOG";
        case ECCodes.EC_OP_RESET_DEBUGLOG: return "EC_OP_RESET_DEBUGLOG";
        case ECCodes.EC_OP_CLEAR_SERVERINFO: return "EC_OP_CLEAR_SERVERINFO";
        case ECCodes.EC_OP_GET_LAST_LOG_ENTRY: return "EC_OP_GET_LAST_LOG_ENTRY";
        case ECCodes.EC_OP_GET_PREFERENCES: return "EC_OP_GET_PREFERENCES";
        case ECCodes.EC_OP_SET_PREFERENCES: return "EC_OP_SET_PREFERENCES";
        case ECCodes.EC_OP_CREATE_CATEGORY: return "EC_OP_CREATE_CATEGORY";
        case ECCodes.EC_OP_UPDATE_CATEGORY: return "EC_OP_UPDATE_CATEGORY";
        case ECCodes.EC_OP_DELETE_CATEGORY: return "EC_OP_DELETE_CATEGORY";
        case ECCodes.EC_OP_GET_STATSGRAPHS: return "EC_OP_GET_STATSGRAPHS";
        case ECCodes.EC_OP_STATSGRAPHS: return "EC_OP_STATSGRAPHS";
        case ECCodes.EC_OP_GET_STATSTREE: return "EC_OP_GET_STATSTREE";
        case ECCodes.EC_OP_STATSTREE: return "EC_OP_STATSTREE";
        case ECCodes.EC_OP_KAD_START: return "EC_OP_KAD_START";
        case ECCodes.EC_OP_KAD_STOP: return "EC_OP_KAD_STOP";
        case ECCodes.EC_OP_CONNECT: return "EC_OP_CONNECT";
        case ECCodes.EC_OP_DISCONNECT: return "EC_OP_DISCONNECT";
        case ECCodes.EC_OP_GET_DLOAD_QUEUE_DETAIL: return "EC_OP_GET_DLOAD_QUEUE_DETAIL";
        case ECCodes.EC_OP_KAD_UPDATE_FROM_URL: return "EC_OP_KAD_UPDATE_FROM_URL";
        case ECCodes.EC_OP_KAD_BOOTSTRAP_FROM_IP: return "EC_OP_KAD_BOOTSTRAP_FROM_IP";
        default: return "UNKNOWN";
        }
    }
        
    private String flagsToString(byte[] flags, boolean printAccepts) {

        if (! printAccepts) {
            return String.format("ZLIB=%s, UTF8NUMERS=%s, HAS_ID=%s, ACCEPTS=%s", isZlibCompressed(), isUTF8Compressed(), hasId(), hasAccepts());
        } else {
            return String.format("ZLIB=%s, UTF8NUMERS=%s", acceptsUTF8(), this.acceptsZlib());
        }
    }
    
    public String dump() {

        StringBuilder s = new StringBuilder();
        
        boolean flagsDumped = false;
        boolean acceptsDumped = false;
        boolean lenDumped = false;
        int nextIndex = 0;
        
        
        try {
            s.append(ECUtils.hexDecode(rawFlags, 0, 4, "Transmission flags: " + flagsToString(rawFlags, false), hexPerRow, 0));
            flagsDumped = true;
            if (hasAccepts()) {
                s.append(ECUtils.hexDecode(rawAccepts, 0, 4, "Accepts flags: " + flagsToString(rawAccepts, true), hexPerRow, 0));
            }
            acceptsDumped = true;
            s.append(ECUtils.hexDecode(rawLen, 0, 4, "len=" + len, hexPerRow, 0));
            lenDumped = true;
            
            s.append(ECUtils.hexDecode(rawPayload, 0, 1, "opCode=" + getOpCodeString(), hexPerRow, 0));
            nextIndex = tagsCountIndex;
            
            int tagsCount = 0;
            tagsCount = getTagsCount();
            s.append(ECUtils.hexDecode(rawPayload, tagsCountIndex, tagsIndex - tagsCountIndex, "tagsCount=" + getTagsCount(), hexPerRow, 0));
            nextIndex = tagsIndex;
            
            for (int i = 0; i < tagsCount; i++) {
                if (rawTagsList == null) throw new ECPacketParsingException("Tag list not decoded while it was expeted to be", this);
                if (i >= rawTagsList.length) throw new ECPacketParsingException("Expecting " + tagsCount + " tags, found only " + rawTagsList.length, this);
                if (rawTagsList[i] == null) throw new ECPacketParsingException("Tag " + i + " not present where expected", this);
                s.append(rawTagsList[i].dump(0));
                nextIndex = rawTagsList[i].tagEnd + 1;
            }
            
            if (nextIndex < rawPayload.length) {
                s.append(ECUtils.hexDecode(rawPayload, nextIndex, rawPayload.length - nextIndex, "Unexpected remaining payload", hexPerRow, 0));
            }
            
        } catch (Exception e) {
            try {
                if (! flagsDumped) {
                    s.append(ECUtils.hexDecode(rawFlags, 0, 4, "ERROR -- Can't decode flags: " + e.getMessage(), hexPerRow, 0));
                    if (hasAccepts()) s.append(ECUtils.hexDecode(rawAccepts, 0, 4, "", hexPerRow, 0));
                    s.append(ECUtils.hexDecode(rawLen, 0, 4, "", hexPerRow, 0));
                    s.append(ECUtils.hexDecode(rawPayload, 0, rawPayload.length, "", hexPerRow, 0));
                } else if (! acceptsDumped && hasAccepts()) {
                    s.append(ECUtils.hexDecode(rawAccepts, 0, 4, "ERROR -- Can't decode accepts: " + e.getMessage(), hexPerRow, 0));
                    s.append(ECUtils.hexDecode(rawLen, 0, 4, "", hexPerRow, 0));
                    s.append(ECUtils.hexDecode(rawPayload, 0, rawPayload.length, "", hexPerRow, 0));
                } else if (! lenDumped) {
                    s.append(ECUtils.hexDecode(rawLen, 0, 4, "ERROR -- Can't decode length: " + e.getMessage(), hexPerRow, 0));
                    s.append(ECUtils.hexDecode(rawPayload, 0, rawPayload.length, "", hexPerRow, 0));
                } else {
                    s.append(ECUtils.hexDecode(rawPayload, nextIndex, rawPayload.length - nextIndex, "ERROR -- Can't decode payload: " + e.getMessage(), hexPerRow, 0));
                }
            } catch (ECDebugException e1) {
                if (! flagsDumped) {
                    s.append("Can't decode flags " + e.getMessage());
                } else if (! acceptsDumped && hasAccepts()) {
                    s.append("Can't decode accepts " + e.getMessage());
                } else if (! lenDumped) {
                    s.append("Can't decode len " + e.getMessage());
                } else {
                    s.append("Can't decode payload " + e.getMessage());
                }
                
            }
        }
        return s.toString();
    }
    
    
    
    
    
    protected class ECRawTag {
        //ECRawPacket rawPacket;
        int tagStart;
        int tagEnd;
        int tagNameIndex;
        int tagTypeIndex;
        int tagLenIndex;
        int subTagsCountIndex;
        int subTagsCount;

        int subTagsIndex;
        ECRawTag[] rawSubTagsList;
        int tagValueIndex;

        
        public ECRawTag(int index) {
            tagStart = index;
        }
        
        public ECRawTag(ECTag t, int startIndex) throws ECPacketParsingException {
            
            // TODO UTF-8 Compression - Complesso perchè per i subtag la lunghezza è calcolata come se non fossero compressi...
            
            tagStart = startIndex;
            tagNameIndex = tagStart;
            tagTypeIndex = tagNameIndex + 2;
            tagLenIndex = tagTypeIndex + 1;
            subTagsCountIndex = tagLenIndex + 4;
            
            
            
            setTagName(t.getTagName());
            if (debug) System.out.println("ECRawTag: packet dump after setting tagName:\n" + ECRawPacket.this.dump());
            setTagType(t.getTagType());
            if (debug) System.out.println("ECRawTag: packet dump after setting tagType:\n" + ECRawPacket.this.dump());
            
            ArrayList<ECTag> subTags = t.getSubTags();
            
            int tagLen = 0;

            tagValueIndex = subTagsCountIndex;
            subTagsIndex = tagValueIndex + 2; // TODO UTF-8 Compression

            if (subTags.size() > 0) {
                tagValueIndex = subTagsIndex;
                setHasSubTags(true);
                if (debug) System.out.println("ECRawTag: packet dump after setting hasSubTags:\n" + ECRawPacket.this.dump());
                
                rawSubTagsList = new ECRawTag[subTags.size()];
                for (int i = 0; i < subTags.size(); i++) {
                    if (debug) System.out.println("ECRawTag: adding subTag " + i + ":\n");
                    ECRawTag sub = getNewECRawTag(subTags.get(i), tagValueIndex);
                    if (debug) System.out.println("ECRawTag: packet dump after setting suBTag " + i + ":\n" + ECRawPacket.this.dump());
                    tagValueIndex = sub.tagEnd + 1;
                    tagLen += sub.getUncompressedTagLength();
                    rawSubTagsList[i] = sub;

                }
                if (debug) System.out.println("ECRawTag: packet dump after setting subTags:\n" + ECRawPacket.this.dump());
                
                setSubTagsCount(subTags.size());
                if (debug) System.out.println("ECRawTag: packet dump after setting subTagsCount:\n" + ECRawPacket.this.dump());

                
            }
            
            
            
            byte[] value;
            try {
                switch (t.getTagType()) {
                case ECTagTypes.EC_TAGTYPE_CUSTOM:
                    value = t.getTagValueCustom();
                    break;
                case ECTagTypes.EC_TAGTYPE_UINT8:
                    value = ECUtils.uintToBytes(t.getTagValueUInt(), 1, true);
                    break;
                case ECTagTypes.EC_TAGTYPE_UINT16:
                    value = ECUtils.uintToBytes(t.getTagValueUInt(), 2, true);
                    break;
                case ECTagTypes.EC_TAGTYPE_UINT32:
                    value = ECUtils.uintToBytes(t.getTagValueUInt(), 4, true);
                    break;
                case ECTagTypes.EC_TAGTYPE_UINT64:
                    value = ECUtils.uintToBytes(t.getTagValueUInt(), 8, true);
                    break;
                case ECTagTypes.EC_TAGTYPE_STRING:
                    String strVal = t.getTagValueString();
                    byte[] tmp = strVal.getBytes();
                    value = new byte[tmp.length + 1];
                    System.arraycopy(tmp, 0, value, 0, tmp.length);
                    value[value.length - 1] = 0x0;
                    break;
                case ECTagTypes.EC_TAGTYPE_DOUBLE:
                    String strDouble = Double.toString(t.getTagValueDouble());
                    byte[] tmpDouble = strDouble.getBytes();
                    value = new byte[tmpDouble.length + 1];
                    System.arraycopy(tmpDouble, 0, value, 0, tmpDouble.length);
                    value[value.length - 1] = 0x0;
                    break;
                case ECTagTypes.EC_TAGTYPE_IPV4:
                    InetSocketAddress sock = t.getTagValueIPv4();
                    InetAddress addr = sock.getAddress();
                    int port = sock.getPort();
                    value = new byte[6];
                    System.arraycopy(addr.getAddress(), 0, value, 0, 4);
                    System.arraycopy(ECUtils.uintToBytes(port, 4, true), 0, value, 4, 2);
                    break;
                    
                case ECTagTypes.EC_TAGTYPE_HASH16:
                    value = t.getTagValueHash();
                    break;
                default:
                    throw new ECPacketParsingException("Unknownt tag type - " + t.getTagType(), ECRawPacket.this);
                }
            } catch (DataFormatException e) {
                // This should never happen. Type and value are checked by ECTag class
                throw new ECPacketParsingException("Tag type and value are not matching", ECRawPacket.this, e);
                
            }
    
            if (value != null) {
                setTagLen(tagLen + value.length);
                if (debug) System.out.println("ECRawTag: packet dump after setting tagLen:\n" + ECRawPacket.this.dump());
                setTagValue(value);
                if (debug) System.out.println("ECRawTag: packet dump after setting tagValue:\n" + ECRawPacket.this.dump());
            } else {
                setTagLen(tagLen);
                if (debug) System.out.println("ECRawTag: packet dump after setting tagLen:\n" + ECRawPacket.this.dump());
            }
            
            tagEnd = tagValueIndex + value.length - 1;
            
        }
        
        public ECTag parse() throws ECPacketParsingException {
            
            tagNameIndex = tagStart;
            try {
                tagTypeIndex = tagNameIndex + (isUTF8Compressed() ? ECUtils.getUTF8SequenceLength(rawPayload[tagNameIndex], debug) : 2);
            } catch (CharacterCodingException e) {
                throw new ECPacketParsingException("Error parsing tagName, invalid UTF-8 sequence", ECRawPacket.this, e);
            }
            tagLenIndex = tagTypeIndex + 1;
            try {
                subTagsCountIndex = tagLenIndex + (isUTF8Compressed() ? ECUtils.getUTF8SequenceLength(rawPayload[tagLenIndex], debug) : 4);
            } catch (CharacterCodingException e) {
                throw new ECPacketParsingException("Error parsing tagLength, invalid UTF-8 sequence", ECRawPacket.this, e);
            }

            
            if (debug) {
                System.out.println("ECtag.parse: tagNameIndex = " + tagNameIndex);
                System.out.println("ECtag.parse: tagTypeIndex = " + tagTypeIndex);
                System.out.println("ECtag.parse: tagLenIndex = " + tagLenIndex);
                System.out.println("ECtag.parse: subTagsCountIndex = " + subTagsCountIndex);
            }
            
            int tagLen = getTagLen();
            if (debug) System.out.println("ECtag.parse: tagLen = " + tagLen);

            
            ECTag et = new ECTag();
            et.rawTag = this;
            et.setTagName(getTagName());

            
            if (! hasSubTags()) {
                subTagsCount = 0;
                tagValueIndex = subTagsCountIndex;
                tagEnd = tagValueIndex + tagLen - 1;
            } else {
                
                if (debug) System.out.println("ECtag.parse: has sub tags");
                
                try {
                    subTagsIndex = subTagsCountIndex + (isUTF8Compressed() ?  ECUtils.getUTF8SequenceLength(rawPayload[subTagsCountIndex], debug) : 2);
                } catch (CharacterCodingException e) {
                    throw new ECPacketParsingException("Error parsing subTagsCount, invalid UTF-8 value", ECRawPacket.this, e);
                }
                subTagsCount = getSubTagsCount();
                
                
                if (debug) System.out.println("ECtag.parse: subTagsIndex="+subTagsIndex);
                if (debug) System.out.println("ECtag.parse: subTagsCount="+subTagsCount);
                
                int index = subTagsIndex;
                int tagCounter = 0;
                rawSubTagsList = new ECRawTag[subTagsCount];
                
                
                // Value found in tagLen considers subtags as not compressed, so we have to consider the uncompressed length. 
                int remainingLen = tagLen; 
                while (tagCounter < subTagsCount && remainingLen > 0) {
                    if (debug) System.out.println("ECRawPacket.parse: parsing tag " + tagCounter + " starting at " + index);
                    ECRawTag t = getNewECRawTag(index);
                    et.addSubTag(t.parse());
                    index = t.getTagEnd() + 1;
                    remainingLen -= t.getUncompressedTagLength();
                    rawSubTagsList[tagCounter++] = t;
                }
                
                if (remainingLen < 0) {
                    throw new ECPacketParsingException("Error parsing subTags list. Last parsed tag ended " + (-remainingLen) + "bytes ot of expected bounds", ECRawPacket.this);
                } else if (tagCounter != subTagsCount) {
                    throw new ECPacketParsingException("Error parsing subTags list. Expecting " + subTagsCount + " tags, found " + tagCounter, ECRawPacket.this);
                }
                
                tagValueIndex = index;
                tagEnd = index + remainingLen - 1;
            }
            
            if (debug) {

                System.out.println("ECtag.parse: tagValueIndex = " + tagValueIndex);
                System.out.println("ECtag.parse: tagEnd = " + tagEnd);
                
                System.out.println("ECTag.parse: DUMP");
                System.out.println(this.dump(0));
                
            }
            
            
            byte[] value = getTagValue();
            try {
                et.setTagType(getTagType());
            } catch (DataFormatException e) {
                throw new ECPacketParsingException("Invalid tag type found while parsing", ECRawPacket.this, e);
            }
            
            try {
                switch (getTagType()) {
                case ECTagTypes.EC_TAGTYPE_CUSTOM:
                    et.setTagValueCustom(value);
                    break;
                case ECTagTypes.EC_TAGTYPE_UINT8:
                    if (value.length == 0) {
                        et.setTagValueUInt(0);                    
                    } else if (value.length  != 1) {
                        throw new ECPacketParsingException("Wrong length for UINT8 tag (" + value.length + ")", ECRawPacket.this);
                    } else {
                        et.setTagValueUInt(ECUtils.bytesToUint(value, 1, true));
                    }
                    break;
                case ECTagTypes.EC_TAGTYPE_UINT16:
                    if (value.length != 2) throw new ECPacketParsingException("Wrong length for UINT8 tag (" + value.length + ")", ECRawPacket.this);
                    et.setTagValueUInt(ECUtils.bytesToUint(value, 2, true));            
                    break;
                case ECTagTypes.EC_TAGTYPE_UINT32:
                    if (value.length != 4) throw new ECPacketParsingException("Wrong length for UINT8 tag (" + value.length + ")", ECRawPacket.this);
                    et.setTagValueUInt(ECUtils.bytesToUint(value, 4, true));            
                    break;
                case ECTagTypes.EC_TAGTYPE_UINT64:
                    if (value.length != 8) throw new ECPacketParsingException("Wrong length for UINT8 tag (" + value.length + ")", ECRawPacket.this);
                    et.setTagValueUInt(ECUtils.bytesToUint(value, 8, true));            
                    break;
                case ECTagTypes.EC_TAGTYPE_STRING:
                    if (value[value.length - 1] != 0x0) throw new ECPacketParsingException("String tag value is not terminated with 0x0", ECRawPacket.this);
                    et.setTagValueString(new String(value, 0, value.length - 1, "UTF-8"));
                    break;
                case ECTagTypes.EC_TAGTYPE_DOUBLE:
                    if (value[value.length - 1] != 0x00) throw new ECPacketParsingException("Double string value is not terminated with 00... Wrong length?", ECRawPacket.this);
                    et.setTagValueDouble(Double.parseDouble(new String(value, 0, value.length - 1, "UTF-8")));
                    break;
                case ECTagTypes.EC_TAGTYPE_IPV4:
                    if (value.length != 6) throw new ECPacketParsingException("Wrong length for IPV4 tag value (" + value.length + ")", ECRawPacket.this);
    
                    byte [] ipAddr = new byte[4];
                    System.arraycopy(value, 0, ipAddr, 0, 4);
                    int port = (int) ECUtils.bytesToUint(value, 4, 2, true, debug);
                    et.setTagValueIPv4(new InetSocketAddress(InetAddress.getByAddress(ipAddr), port));
                    break;
                case ECTagTypes.EC_TAGTYPE_HASH16:
                    if (value.length != 16) throw new ECPacketParsingException("Wrong length for HASH16 tag (" + value.length + ")", ECRawPacket.this);
                    et.setTagValueHash(value);
                    break;
                default:
                    // This should never happen as tag type is checked by previous try
                    throw new ECPacketParsingException("Tag type " + getTagType() + " not implemented", ECRawPacket.this);
                }
            } catch (DataFormatException e) {
                // This should never happen either
                throw new ECPacketParsingException("Severe error: mismatch between tag type and value not expected", ECRawPacket.this);
            } catch (UnsupportedEncodingException e) {
                
                throw new ECPacketParsingException("Severe error: UTF-8 encoding not supported for strings", ECRawPacket.this, e);
            } catch (UnknownHostException e) {
                // TBV if InetAddress + InetSocketAddress is the best class to be used, as they can throw 
                // this exception
                throw new ECPacketParsingException("Unknown host in IPV4 tag value", ECRawPacket.this, e);
            }

            return et;
            
        }
        
        
        
        
        public int getUncompressedTagLength() throws ECPacketParsingException {
            int l = 7;       // Tagname (2) + tagtype (1) + taglen (4) 
            if (hasSubTags()) { 
                l += 2;          // Subtags count (2)
                for (int i = 0; i < rawSubTagsList.length; i++) {
                    l += rawSubTagsList[i].getUncompressedTagLength();
                }
            }
            l += tagEnd - tagValueIndex + 1;
            return l;
        }
        
        public boolean hasSubTags() throws ECPacketParsingException {
            return !((getUnshiftedTagName() & 0x1) == 0);
        }
        
        public void setHasSubTags(boolean has) throws ECPacketParsingException {
            int tagName = getUnshiftedTagName();
            if (has) {
                tagName |= 0x1;
            } else {
                if (hasSubTags()) {
                    tagName ^= 0x1;
                }
            }
            setUnshiftedTagName(tagName);
        }
        
        public int getSubTagsCount() throws ECPacketParsingException {
            if (! isUTF8Compressed()) {
                return (int) ECUtils.bytesToUint(rawPayload, subTagsCountIndex, subTagsIndex - subTagsCountIndex, true, debug);
            } else {
                try {
                    return (int) ECUtils.decodeUTF8number(rawPayload, subTagsCountIndex, debug);
                } catch (CharacterCodingException e) {
                    throw new ECPacketParsingException("Error parsing tagLen - invalid UTF8 sequence.", ECRawPacket.this, e);
                }
            }
        }
        
        
        public void setSubTagsCount(int count) {
            // TODO UTF-8 Compression
            if (debug) System.out.println("ECRawTag.setSubTagsCount: setting count " + count + " between indexes " + subTagsCountIndex + " and " + subTagsIndex);
            int l = subTagsIndex - subTagsCountIndex;
            System.arraycopy(ECUtils.uintToBytes(count, l, true), 0, rawPayload, subTagsCountIndex, l);
        }
        
        public int getTagEnd() {
            return tagEnd;
        }
        
        public int getTagLen() throws ECPacketParsingException {
            if (! isUTF8Compressed()) {
                return (int) ECUtils.bytesToUint(rawPayload, tagLenIndex, subTagsCountIndex - tagLenIndex, true, debug);
            } else {
                try {
                    return (int) ECUtils.decodeUTF8number(rawPayload, tagLenIndex, debug);
                } catch (CharacterCodingException e) {
                    throw new ECPacketParsingException("Error parsing tagLen - invalid UTF8 sequence.", ECRawPacket.this, e);
                }
            }
        }
        
        public void setTagLen(int value) {
            // TODO UTF-8 Compression
            if (debug) System.out.println("ECRawTag.setTagLen: setting lenght " + value + " between indexes " + tagLenIndex + " and " + subTagsCountIndex);
            int l = subTagsCountIndex - tagLenIndex;
            System.arraycopy(ECUtils.uintToBytes(value, l, true), 0, rawPayload, tagLenIndex, l);
        }
        
        private int getUnshiftedTagName() throws ECPacketParsingException {
            if (! isUTF8Compressed()) {
                return (int) ECUtils.bytesToUint(rawPayload, tagNameIndex, tagTypeIndex - tagNameIndex, true, debug);
            } else {
                try {
                    return (int) ECUtils.decodeUTF8number(rawPayload, tagNameIndex, debug);
                } catch (CharacterCodingException e) {
                    throw new ECPacketParsingException("Error parsing tagName - invalid UTF-8 sequence.", ECRawPacket.this, e);                
                }
            }
        }
        
        private void setUnshiftedTagName(int tagName) {
            // TODO UTF-8 COMPRESSION
            int tagNameLen = tagTypeIndex - tagNameIndex;
            System.arraycopy(ECUtils.uintToBytes(tagName, tagNameLen, true), 0, rawPayload, tagNameIndex, tagNameLen);
        }
        
        public short getTagName() throws ECPacketParsingException {
            return  (short) (getUnshiftedTagName() >> 1);
        }
        
        public void setTagName(int i) throws ECPacketParsingException {
            
            int tagName = i;
            tagName <<= 1;
            if (hasSubTags()) tagName |= 0x1;
            setUnshiftedTagName(tagName);
        }
        
        protected String getTagNameString() {
            try {
                switch(getTagName()) {
                case ECCodes.EC_TAG_STRING: return "EC_TAG_STRING";
                case ECCodes.EC_TAG_PASSWD_HASH: return "EC_TAG_PASSWD_HASH"; 
                case ECCodes.EC_TAG_PROTOCOL_VERSION: return "EC_TAG_PROTOCOL_VERSION";
                case ECCodes.EC_TAG_VERSION_ID: return "EC_TAG_VERSION_ID";
                case ECCodes.EC_TAG_DETAIL_LEVEL: return "EC_TAG_DETAIL_LEVEL";
                case ECCodes.EC_TAG_CONNSTATE: return "EC_TAG_CONNSTATE";
                case ECCodes.EC_TAG_ED2K_ID: return "EC_TAG_ED2K_ID";
                case ECCodes.EC_TAG_LOG_TO_STATUS: return "EC_TAG_LOG_TO_STATUS";
                case ECCodes.EC_TAG_BOOTSTRAP_IP: return "EC_TAG_BOOTSTRAP_IP";
                case ECCodes.EC_TAG_BOOTSTRAP_PORT: return "EC_TAG_BOOTSTRAP_PORT";
                case ECCodes.EC_TAG_CLIENT_ID: return "EC_TAG_CLIENT_ID";
                case ECCodes.EC_TAG_CLIENT_NAME: return "EC_TAG_CLIENT_NAME";
                case ECCodes.EC_TAG_CLIENT_VERSION: return "EC_TAG_CLIENT_VERSION";
                case ECCodes.EC_TAG_CLIENT_MOD: return "EC_TAG_CLIENT_MOD";
                case ECCodes.EC_TAG_STATS_UL_SPEED: return "EC_TAG_STATS_UL_SPEED";
                case ECCodes.EC_TAG_STATS_DL_SPEED: return "EC_TAG_STATS_DL_SPEED";
                case ECCodes.EC_TAG_STATS_UL_SPEED_LIMIT: return "EC_TAG_STATS_UL_SPEED_LIMIT";
                case ECCodes.EC_TAG_STATS_DL_SPEED_LIMIT: return "EC_TAG_STATS_DL_SPEED_LIMIT";
                case ECCodes.EC_TAG_STATS_UP_OVERHEAD: return "EC_TAG_STATS_UP_OVERHEAD";
                case ECCodes.EC_TAG_STATS_DOWN_OVERHEAD: return "EC_TAG_STATS_DOWN_OVERHEAD";
                case ECCodes.EC_TAG_STATS_TOTAL_SRC_COUNT: return "EC_TAG_STATS_TOTAL_SRC_COUNT";
                case ECCodes.EC_TAG_STATS_BANNED_COUNT: return "EC_TAG_STATS_BANNED_COUNT";
                case ECCodes.EC_TAG_STATS_UL_QUEUE_LEN: return "EC_TAG_STATS_UL_QUEUE_LEN";
                case ECCodes.EC_TAG_STATS_ED2K_USERS: return "EC_TAG_STATS_ED2K_USERS";
                case ECCodes.EC_TAG_STATS_KAD_USERS: return "EC_TAG_STATS_KAD_USERS";
                case ECCodes.EC_TAG_STATS_ED2K_FILES: return "EC_TAG_STATS_ED2K_FILES";
                case ECCodes.EC_TAG_STATS_KAD_FILES: return "EC_TAG_STATS_KAD_FILES";
                case ECCodes.EC_TAG_PARTFILE: return "EC_TAG_PARTFILE";
                case ECCodes.EC_TAG_PARTFILE_NAME: return "EC_TAG_PARTFILE_NAME";
                case ECCodes.EC_TAG_PARTFILE_PARTMETID: return "EC_TAG_PARTFILE_PARTMETID";
                case ECCodes.EC_TAG_PARTFILE_SIZE_FULL: return "EC_TAG_PARTFILE_SIZE_FULL";
                case ECCodes.EC_TAG_PARTFILE_SIZE_XFER: return "EC_TAG_PARTFILE_SIZE_XFER";
                case ECCodes.EC_TAG_PARTFILE_SIZE_XFER_UP: return "EC_TAG_PARTFILE_SIZE_XFER_UP";
                case ECCodes.EC_TAG_PARTFILE_SIZE_DONE: return "EC_TAG_PARTFILE_SIZE_DONE";
                case ECCodes.EC_TAG_PARTFILE_SPEED: return "EC_TAG_PARTFILE_SPEED";
                case ECCodes.EC_TAG_PARTFILE_STATUS: return "EC_TAG_PARTFILE_STATUS";
                case ECCodes.EC_TAG_PARTFILE_PRIO: return "EC_TAG_PARTFILE_PRIO";
                case ECCodes.EC_TAG_PARTFILE_SOURCE_COUNT: return "EC_TAG_PARTFILE_SOURCE_COUNT";
                case ECCodes.EC_TAG_PARTFILE_SOURCE_COUNT_A4AF: return "EC_TAG_PARTFILE_SOURCE_COUNT_A4AF";
                case ECCodes.EC_TAG_PARTFILE_SOURCE_COUNT_NOT_CURRENT: return "EC_TAG_PARTFILE_SOURCE_COUNT_NOT_CURRENT";
                case ECCodes.EC_TAG_PARTFILE_SOURCE_COUNT_XFER: return "EC_TAG_PARTFILE_SOURCE_COUNT_XFER";
                case ECCodes.EC_TAG_PARTFILE_ED2K_LINK: return "EC_TAG_PARTFILE_ED2K_LINK";
                case ECCodes.EC_TAG_PARTFILE_CAT: return "EC_TAG_PARTFILE_CAT";
                case ECCodes.EC_TAG_PARTFILE_LAST_RECV: return "EC_TAG_PARTFILE_LAST_RECV";
                case ECCodes.EC_TAG_PARTFILE_LAST_SEEN_COMP: return "EC_TAG_PARTFILE_LAST_SEEN_COMP";
                case ECCodes.EC_TAG_PARTFILE_PART_STATUS: return "EC_TAG_PARTFILE_PART_STATUS";
                case ECCodes.EC_TAG_PARTFILE_GAP_STATUS: return "EC_TAG_PARTFILE_GAP_STATUS";
                case ECCodes.EC_TAG_PARTFILE_REQ_STATUS: return "EC_TAG_PARTFILE_REQ_STATUS";
                case ECCodes.EC_TAG_PARTFILE_SOURCE_NAMES: return "EC_TAG_PARTFILE_SOURCE_NAMES";
                case ECCodes.EC_TAG_PARTFILE_COMMENTS: return "EC_TAG_PARTFILE_COMMENTS";
                case ECCodes.EC_TAG_KNOWNFILE: return "EC_TAG_KNOWNFILE";
                case ECCodes.EC_TAG_KNOWNFILE_XFERRED: return "EC_TAG_KNOWNFILE_XFERRED";
                case ECCodes.EC_TAG_KNOWNFILE_XFERRED_ALL: return "EC_TAG_KNOWNFILE_XFERRED_ALL";
                case ECCodes.EC_TAG_KNOWNFILE_REQ_COUNT: return "EC_TAG_KNOWNFILE_REQ_COUNT";
                case ECCodes.EC_TAG_KNOWNFILE_REQ_COUNT_ALL: return "EC_TAG_KNOWNFILE_REQ_COUNT_ALL";
                case ECCodes.EC_TAG_KNOWNFILE_ACCEPT_COUNT: return "EC_TAG_KNOWNFILE_ACCEPT_COUNT";
                case ECCodes.EC_TAG_KNOWNFILE_ACCEPT_COUNT_ALL: return "EC_TAG_KNOWNFILE_ACCEPT_COUNT_ALL";
                case ECCodes.EC_TAG_KNOWNFILE_AICH_MASTERHASH: return "EC_TAG_KNOWNFILE_AICH_MASTERHASH";
                case ECCodes.EC_TAG_SERVER: return "EC_TAG_SERVER";
                case ECCodes.EC_TAG_SERVER_NAME: return "EC_TAG_SERVER_NAME";
                case ECCodes.EC_TAG_SERVER_DESC: return "EC_TAG_SERVER_DESC";
                case ECCodes.EC_TAG_SERVER_ADDRESS: return "EC_TAG_SERVER_ADDRESS";
                case ECCodes.EC_TAG_SERVER_PING: return "EC_TAG_SERVER_PING";
                case ECCodes.EC_TAG_SERVER_USERS: return "EC_TAG_SERVER_USERS";
                case ECCodes.EC_TAG_SERVER_USERS_MAX: return "EC_TAG_SERVER_USERS_MAX";
                case ECCodes.EC_TAG_SERVER_FILES: return "EC_TAG_SERVER_FILES";
                case ECCodes.EC_TAG_SERVER_PRIO: return "EC_TAG_SERVER_PRIO";
                case ECCodes.EC_TAG_SERVER_FAILED: return "EC_TAG_SERVER_FAILED";
                case ECCodes.EC_TAG_SERVER_STATIC: return "EC_TAG_SERVER_STATIC";
                case ECCodes.EC_TAG_SERVER_VERSION: return "EC_TAG_SERVER_VERSION";
                case ECCodes.EC_TAG_CLIENT: return "EC_TAG_CLIENT";
                case ECCodes.EC_TAG_CLIENT_SOFTWARE: return "EC_TAG_CLIENT_SOFTWARE";
                case ECCodes.EC_TAG_CLIENT_SCORE: return "EC_TAG_CLIENT_SCORE";
                case ECCodes.EC_TAG_CLIENT_HASH: return "EC_TAG_CLIENT_HASH";
                case ECCodes.EC_TAG_CLIENT_FRIEND: return "EC_TAG_CLIENT_FRIEND";
                case ECCodes.EC_TAG_CLIENT_WAIT_TIME: return "EC_TAG_CLIENT_WAIT_TIME";
                case ECCodes.EC_TAG_CLIENT_XFER_TIME: return "EC_TAG_CLIENT_XFER_TIME";
                case ECCodes.EC_TAG_CLIENT_QUEUE_TIME: return "EC_TAG_CLIENT_QUEUE_TIME";
                case ECCodes.EC_TAG_CLIENT_LAST_TIME: return "EC_TAG_CLIENT_LAST_TIME";
                case ECCodes.EC_TAG_CLIENT_UPLOAD_SESSION: return "EC_TAG_CLIENT_UPLOAD_SESSION";
                case ECCodes.EC_TAG_CLIENT_UPLOAD_TOTAL: return "EC_TAG_CLIENT_UPLOAD_TOTAL";
                case ECCodes.EC_TAG_CLIENT_DOWNLOAD_TOTAL: return "EC_TAG_CLIENT_DOWNLOAD_TOTAL";
                case ECCodes.EC_TAG_CLIENT_STATE: return "EC_TAG_CLIENT_STATE";
                case ECCodes.EC_TAG_CLIENT_UP_SPEED: return "EC_TAG_CLIENT_UP_SPEED";
                case ECCodes.EC_TAG_CLIENT_DOWN_SPEED: return "EC_TAG_CLIENT_DOWN_SPEED";
                case ECCodes.EC_TAG_CLIENT_FROM: return "EC_TAG_CLIENT_FROM";
                case ECCodes.EC_TAG_CLIENT_USER_IP: return "EC_TAG_CLIENT_USER_IP";
                case ECCodes.EC_TAG_CLIENT_USER_PORT: return "EC_TAG_CLIENT_USER_PORT";
                case ECCodes.EC_TAG_CLIENT_SERVER_IP: return "EC_TAG_CLIENT_SERVER_IP";
                case ECCodes.EC_TAG_CLIENT_SERVER_PORT: return "EC_TAG_CLIENT_SERVER_PORT";
                case ECCodes.EC_TAG_CLIENT_SERVER_NAME: return "EC_TAG_CLIENT_SERVER_NAME";
                case ECCodes.EC_TAG_CLIENT_SOFT_VER_STR: return "EC_TAG_CLIENT_SOFT_VER_STR";
                case ECCodes.EC_TAG_CLIENT_WAITING_POSITION: return "EC_TAG_CLIENT_WAITING_POSITION";
                case ECCodes.EC_TAG_SEARCHFILE: return "EC_TAG_SEARCHFILE";
                case ECCodes.EC_TAG_SEARCH_TYPE: return "EC_TAG_SEARCH_TYPE";
                case ECCodes.EC_TAG_SEARCH_NAME: return "EC_TAG_SEARCH_NAME";
                case ECCodes.EC_TAG_SEARCH_MIN_SIZE: return "EC_TAG_SEARCH_MIN_SIZE";
                case ECCodes.EC_TAG_SEARCH_MAX_SIZE: return "EC_TAG_SEARCH_MAX_SIZE";
                case ECCodes.EC_TAG_SEARCH_FILE_TYPE: return "EC_TAG_SEARCH_FILE_TYPE";
                case ECCodes.EC_TAG_SEARCH_EXTENSION: return "EC_TAG_SEARCH_EXTENSION";
                case ECCodes.EC_TAG_SEARCH_AVAILABILITY: return "EC_TAG_SEARCH_AVAILABILITY";
                case ECCodes.EC_TAG_SEARCH_STATUS: return "EC_TAG_SEARCH_STATUS";
                case ECCodes.EC_TAG_SELECT_PREFS: return "EC_TAG_SELECT_PREFS";
                case ECCodes.EC_TAG_PREFS_CATEGORIES: return "EC_TAG_PREFS_CATEGORIES";
                case ECCodes.EC_TAG_CATEGORY: return "EC_TAG_CATEGORY";
                case ECCodes.EC_TAG_CATEGORY_TITLE: return "EC_TAG_CATEGORY_TITLE";
                case ECCodes.EC_TAG_CATEGORY_PATH: return "EC_TAG_CATEGORY_PATH";
                case ECCodes.EC_TAG_CATEGORY_COMMENT: return "EC_TAG_CATEGORY_COMMENT";
                case ECCodes.EC_TAG_CATEGORY_COLOR: return "EC_TAG_CATEGORY_COLOR";
                case ECCodes.EC_TAG_CATEGORY_PRIO: return "EC_TAG_CATEGORY_PRIO";
                case ECCodes.EC_TAG_PREFS_GENERAL: return "EC_TAG_PREFS_GENERAL";
                case ECCodes.EC_TAG_USER_NICK: return "EC_TAG_USER_NICK";
                case ECCodes.EC_TAG_USER_HASH: return "EC_TAG_USER_HASH";
                case ECCodes.EC_TAG_USER_HOST: return "EC_TAG_USER_HOST";
                case ECCodes.EC_TAG_PREFS_CONNECTIONS: return "EC_TAG_PREFS_CONNECTIONS";
                case ECCodes.EC_TAG_CONN_DL_CAP: return "EC_TAG_CONN_DL_CAP";
                case ECCodes.EC_TAG_CONN_UL_CAP: return "EC_TAG_CONN_UL_CAP";
                case ECCodes.EC_TAG_CONN_MAX_DL: return "EC_TAG_CONN_MAX_DL";
                case ECCodes.EC_TAG_CONN_MAX_UL: return "EC_TAG_CONN_MAX_UL";
                case ECCodes.EC_TAG_CONN_SLOT_ALLOCATION: return "EC_TAG_CONN_SLOT_ALLOCATION";
                case ECCodes.EC_TAG_CONN_TCP_PORT: return "EC_TAG_CONN_TCP_PORT";
                case ECCodes.EC_TAG_CONN_UDP_PORT: return "EC_TAG_CONN_UDP_PORT";
                case ECCodes.EC_TAG_CONN_UDP_DISABLE: return "EC_TAG_CONN_UDP_DISABLE";
                case ECCodes.EC_TAG_CONN_MAX_FILE_SOURCES: return "EC_TAG_CONN_MAX_FILE_SOURCES";
                case ECCodes.EC_TAG_CONN_MAX_CONN: return "EC_TAG_CONN_MAX_CONN";
                case ECCodes.EC_TAG_CONN_AUTOCONNECT: return "EC_TAG_CONN_AUTOCONNECT";
                case ECCodes.EC_TAG_CONN_RECONNECT: return "EC_TAG_CONN_RECONNECT";
                case ECCodes.EC_TAG_NETWORK_ED2K: return "EC_TAG_NETWORK_ED2K";
                case ECCodes.EC_TAG_NETWORK_KADEMLIA: return "EC_TAG_NETWORK_KADEMLIA";
                case ECCodes.EC_TAG_PREFS_MESSAGEFILTER: return "EC_TAG_PREFS_MESSAGEFILTER";
                case ECCodes.EC_TAG_MSGFILTER_ENABLED: return "EC_TAG_MSGFILTER_ENABLED";
                case ECCodes.EC_TAG_MSGFILTER_ALL: return "EC_TAG_MSGFILTER_ALL";
                case ECCodes.EC_TAG_MSGFILTER_FRIENDS: return "EC_TAG_MSGFILTER_FRIENDS";
                case ECCodes.EC_TAG_MSGFILTER_SECURE: return "EC_TAG_MSGFILTER_SECURE";
                case ECCodes.EC_TAG_MSGFILTER_BY_KEYWORD: return "EC_TAG_MSGFILTER_BY_KEYWORD";
                case ECCodes.EC_TAG_MSGFILTER_KEYWORDS: return "EC_TAG_MSGFILTER_KEYWORDS";
                case ECCodes.EC_TAG_PREFS_REMOTECTRL: return "EC_TAG_PREFS_REMOTECTRL";
                case ECCodes.EC_TAG_WEBSERVER_AUTORUN: return "EC_TAG_WEBSERVER_AUTORUN";
                case ECCodes.EC_TAG_WEBSERVER_PORT: return "EC_TAG_WEBSERVER_PORT";
                case ECCodes.EC_TAG_WEBSERVER_GUEST: return "EC_TAG_WEBSERVER_GUEST";
                case ECCodes.EC_TAG_WEBSERVER_USEGZIP: return "EC_TAG_WEBSERVER_USEGZIP";
                case ECCodes.EC_TAG_WEBSERVER_REFRESH: return "EC_TAG_WEBSERVER_REFRESH";
                case ECCodes.EC_TAG_WEBSERVER_TEMPLATE: return "EC_TAG_WEBSERVER_TEMPLATE";
                case ECCodes.EC_TAG_PREFS_ONLINESIG: return "EC_TAG_PREFS_ONLINESIG";
                case ECCodes.EC_TAG_ONLINESIG_ENABLED: return "EC_TAG_ONLINESIG_ENABLED";
                case ECCodes.EC_TAG_PREFS_SERVERS: return "EC_TAG_PREFS_SERVERS";
                case ECCodes.EC_TAG_SERVERS_REMOVE_DEAD: return "EC_TAG_SERVERS_REMOVE_DEAD";
                case ECCodes.EC_TAG_SERVERS_DEAD_SERVER_RETRIES: return "EC_TAG_SERVERS_DEAD_SERVER_RETRIES";
                case ECCodes.EC_TAG_SERVERS_AUTO_UPDATE: return "EC_TAG_SERVERS_AUTO_UPDATE";
                case ECCodes.EC_TAG_SERVERS_URL_LIST: return "EC_TAG_SERVERS_URL_LIST";
                case ECCodes.EC_TAG_SERVERS_ADD_FROM_SERVER: return "EC_TAG_SERVERS_ADD_FROM_SERVER";
                case ECCodes.EC_TAG_SERVERS_ADD_FROM_CLIENT: return "EC_TAG_SERVERS_ADD_FROM_CLIENT";
                case ECCodes.EC_TAG_SERVERS_USE_SCORE_SYSTEM: return "EC_TAG_SERVERS_USE_SCORE_SYSTEM";
                case ECCodes.EC_TAG_SERVERS_SMART_ID_CHECK: return "EC_TAG_SERVERS_SMART_ID_CHECK";
                case ECCodes.EC_TAG_SERVERS_SAFE_SERVER_CONNECT: return "EC_TAG_SERVERS_SAFE_SERVER_CONNECT";
                case ECCodes.EC_TAG_SERVERS_AUTOCONN_STATIC_ONLY: return "EC_TAG_SERVERS_AUTOCONN_STATIC_ONLY";
                case ECCodes.EC_TAG_SERVERS_MANUAL_HIGH_PRIO: return "EC_TAG_SERVERS_MANUAL_HIGH_PRIO";
                case ECCodes.EC_TAG_SERVERS_UPDATE_URL: return "EC_TAG_SERVERS_UPDATE_URL";
                case ECCodes.EC_TAG_PREFS_FILES: return "EC_TAG_PREFS_FILES";
                case ECCodes.EC_TAG_FILES_ICH_ENABLED: return "EC_TAG_FILES_ICH_ENABLED";
                case ECCodes.EC_TAG_FILES_AICH_TRUST: return "EC_TAG_FILES_AICH_TRUST";
                case ECCodes.EC_TAG_FILES_NEW_PAUSED: return "EC_TAG_FILES_NEW_PAUSED";
                case ECCodes.EC_TAG_FILES_NEW_AUTO_DL_PRIO: return "EC_TAG_FILES_NEW_AUTO_DL_PRIO";
                case ECCodes.EC_TAG_FILES_PREVIEW_PRIO: return "EC_TAG_FILES_PREVIEW_PRIO";
                case ECCodes.EC_TAG_FILES_NEW_AUTO_UL_PRIO: return "EC_TAG_FILES_NEW_AUTO_UL_PRIO";
                case ECCodes.EC_TAG_FILES_UL_FULL_CHUNKS: return "EC_TAG_FILES_UL_FULL_CHUNKS";
                case ECCodes.EC_TAG_FILES_START_NEXT_PAUSED: return "EC_TAG_FILES_START_NEXT_PAUSED";
                case ECCodes.EC_TAG_FILES_RESUME_SAME_CAT: return "EC_TAG_FILES_RESUME_SAME_CAT";
                case ECCodes.EC_TAG_FILES_SAVE_SOURCES: return "EC_TAG_FILES_SAVE_SOURCES";
                case ECCodes.EC_TAG_FILES_EXTRACT_METADATA: return "EC_TAG_FILES_EXTRACT_METADATA";
                case ECCodes.EC_TAG_FILES_ALLOC_FULL_SIZE: return "EC_TAG_FILES_ALLOC_FULL_SIZE";
                case ECCodes.EC_TAG_FILES_CHECK_FREE_SPACE: return "EC_TAG_FILES_CHECK_FREE_SPACE";
                case ECCodes.EC_TAG_FILES_MIN_FREE_SPACE: return "EC_TAG_FILES_MIN_FREE_SPACE";
                case ECCodes.EC_TAG_PREFS_SRCDROP: return "EC_TAG_PREFS_SRCDROP";
                case ECCodes.EC_TAG_SRCDROP_NONEEDED: return "EC_TAG_SRCDROP_NONEEDED";
                case ECCodes.EC_TAG_SRCDROP_DROP_FQS: return "EC_TAG_SRCDROP_DROP_FQS";
                case ECCodes.EC_TAG_SRCDROP_DROP_HQRS: return "EC_TAG_SRCDROP_DROP_HQRS";
                case ECCodes.EC_TAG_SRCDROP_HQRS_VALUE: return "EC_TAG_SRCDROP_HQRS_VALUE";
                case ECCodes.EC_TAG_SRCDROP_AUTODROP_TIMER: return "EC_TAG_SRCDROP_AUTODROP_TIMER";
                case ECCodes.EC_TAG_PREFS_DIRECTORIES: return "EC_TAG_PREFS_DIRECTORIES";
                case ECCodes.EC_TAG_PREFS_STATISTICS: return "EC_TAG_PREFS_STATISTICS";
                case ECCodes.EC_TAG_STATSGRAPH_WIDTH: return "EC_TAG_STATSGRAPH_WIDTH";
                case ECCodes.EC_TAG_STATSGRAPH_SCALE: return "EC_TAG_STATSGRAPH_SCALE";
                case ECCodes.EC_TAG_STATSGRAPH_LAST: return "EC_TAG_STATSGRAPH_LAST";
                case ECCodes.EC_TAG_STATSGRAPH_DATA: return "EC_TAG_STATSGRAPH_DATA";
                case ECCodes.EC_TAG_STATTREE_CAPPING: return "EC_TAG_STATTREE_CAPPING";
                case ECCodes.EC_TAG_STATTREE_NODE: return "EC_TAG_STATTREE_NODE";
                case ECCodes.EC_TAG_STAT_NODE_VALUE: return "EC_TAG_STAT_NODE_VALUE";
                case ECCodes.EC_TAG_STAT_VALUE_TYPE: return "EC_TAG_STAT_VALUE_TYPE";
                case ECCodes.EC_TAG_STATTREE_NODEID: return "EC_TAG_STATTREE_NODEID";
                case ECCodes.EC_TAG_PREFS_SECURITY: return "EC_TAG_PREFS_SECURITY";
                case ECCodes.EC_TAG_SECURITY_CAN_SEE_SHARES: return "EC_TAG_SECURITY_CAN_SEE_SHARES";
                case ECCodes.EC_TAG_IPFILTER_CLIENTS: return "EC_TAG_IPFILTER_CLIENTS";
                case ECCodes.EC_TAG_IPFILTER_SERVERS: return "EC_TAG_IPFILTER_SERVERS";
                case ECCodes.EC_TAG_IPFILTER_AUTO_UPDATE: return "EC_TAG_IPFILTER_AUTO_UPDATE";
                case ECCodes.EC_TAG_IPFILTER_UPDATE_URL: return "EC_TAG_IPFILTER_UPDATE_URL";
                case ECCodes.EC_TAG_IPFILTER_LEVEL: return "EC_TAG_IPFILTER_LEVEL";
                case ECCodes.EC_TAG_IPFILTER_FILTER_LAN: return "EC_TAG_IPFILTER_FILTER_LAN";
                case ECCodes.EC_TAG_SECURITY_USE_SECIDENT: return "EC_TAG_SECURITY_USE_SECIDENT";
                case ECCodes.EC_TAG_SECURITY_OBFUSCATION_SUPPORTED: return "EC_TAG_SECURITY_OBFUSCATION_SUPPORTED";
                case ECCodes.EC_TAG_SECURITY_OBFUSCATION_REQUESTED: return "EC_TAG_SECURITY_OBFUSCATION_REQUESTED";
                case ECCodes.EC_TAG_SECURITY_OBFUSCATION_REQUIRED: return "EC_TAG_SECURITY_OBFUSCATION_REQUIRED";
                case ECCodes.EC_TAG_PREFS_CORETWEAKS: return "EC_TAG_PREFS_CORETWEAKS";
                case ECCodes.EC_TAG_CORETW_MAX_CONN_PER_FIVE: return "EC_TAG_CORETW_MAX_CONN_PER_FIVE";
                case ECCodes.EC_TAG_CORETW_VERBOSE: return "EC_TAG_CORETW_VERBOSE";
                case ECCodes.EC_TAG_CORETW_FILEBUFFER: return "EC_TAG_CORETW_FILEBUFFER";
                case ECCodes.EC_TAG_CORETW_UL_QUEUE: return "EC_TAG_CORETW_UL_QUEUE";
                case ECCodes.EC_TAG_CORETW_SRV_KEEPALIVE_TIMEOUT: return "EC_TAG_CORETW_SRV_KEEPALIVE_TIMEOUT";
                case ECCodes.EC_TAG_PREFS_KADEMLIA: return "EC_TAG_PREFS_KADEMLIA";
                case ECCodes.EC_TAG_KADEMLIA_UPDATE_URL: return "EC_TAG_KADEMLIA_UPDATE_URL";
                default:
                    return "UNKNOWN";
                }
            } catch (ECPacketParsingException e) {
                return "INVALID";
            }
        }
        
        public byte getTagType() {
            return rawPayload[tagTypeIndex];
        }
        
        public void setTagType(byte type) {
            rawPayload[tagTypeIndex] = type;
        }
        
        public String getTagTypeString() {
            switch(getTagType()) {
            case ECTagTypes.EC_TAGTYPE_UNKNOWN: return "EC_TAGTYPE_UNKNOWN";
            case ECTagTypes.EC_TAGTYPE_CUSTOM: return "EC_TAGTYPE_CUSTOM";
            case ECTagTypes.EC_TAGTYPE_UINT8: return "EC_TAGTYPE_UINT8";
            case ECTagTypes.EC_TAGTYPE_UINT16: return "EC_TAGTYPE_UINT16";
            case ECTagTypes.EC_TAGTYPE_UINT32: return "EC_TAGTYPE_UINT32";
            case ECTagTypes.EC_TAGTYPE_UINT64: return "EC_TAGTYPE_UINT64";
            case ECTagTypes.EC_TAGTYPE_STRING: return "EC_TAGTYPE_STRING";
            case ECTagTypes.EC_TAGTYPE_DOUBLE: return "EC_TAGTYPE_DOUBLE";
            case ECTagTypes.EC_TAGTYPE_IPV4: return "EC_TAGTYPE_IPV4";
            case ECTagTypes.EC_TAGTYPE_HASH16: return "EC_TAGTYPE_HASH16";
            default: return "UNKNOWN";
            }
        }
        
        public byte[] getTagValue() {
            if (tagEnd < tagValueIndex) return null;
            
            byte[] ret = new byte[tagEnd - tagValueIndex + 1];
            System.arraycopy(rawPayload, tagValueIndex, ret, 0, ret.length);
            return ret;
        }
        
        public void setTagValue(byte[] value) {
            if (debug) System.out.println("ECRawTag.setTagLen: setting value " + ECUtils.byteArrayToHexString(value) + " between indexes " + tagValueIndex + " and " + (tagValueIndex + value.length));
            if (value != null && value.length > 0) System.arraycopy(value, 0, rawPayload, tagValueIndex, value.length);
        }
        
        public String dump(int indent) {
            
            StringBuilder s = new StringBuilder();
            
            int nextIndex = -1;
            
            
            try {
            
                s.append(ECUtils.hexDecode(rawPayload, tagNameIndex, tagTypeIndex - tagNameIndex, "tagName=" + getTagNameString() + " hasSubTags=" + hasSubTags(), hexPerRow, indent));
                nextIndex = tagTypeIndex;
                s.append(ECUtils.hexDecode(rawPayload, tagTypeIndex, 1, "tagType=" + getTagTypeString(), hexPerRow, indent));
                nextIndex = tagLenIndex;
                s.append(ECUtils.hexDecode(rawPayload, tagLenIndex, subTagsCountIndex - tagLenIndex , "tagLen=" + getTagLen(), hexPerRow, indent));
                nextIndex = subTagsCountIndex;
                
                if (hasSubTags()) {
                    int subTagsCount = 0;
                    subTagsCount = getSubTagsCount();
                    s.append(ECUtils.hexDecode(rawPayload, subTagsCountIndex, subTagsIndex - subTagsCountIndex, "subTagsCount=" + subTagsCount, hexPerRow, indent));
                    nextIndex = subTagsIndex;
                    
                    for (int i = 0; i < subTagsCount; i++) {
                        
                        if (rawSubTagsList == null) throw new ECPacketParsingException("subTagsList not decoded while it was expected to be");
                        if (i >= rawSubTagsList.length) throw new ECPacketParsingException("Expecting " + subTagsCount + " subTags, found only " + rawSubTagsList.length);
                        if (rawSubTagsList[i] == null) throw new ECPacketParsingException("subTag " + i + " not present where expected");

                        
                        
                        s.append(rawSubTagsList[i].dump(indent + 1));
                        nextIndex = rawSubTagsList[i].tagEnd + 1;
                    }
                }
                
                int tagValueLen = tagEnd - tagValueIndex + 1;
                
                if (tagValueLen > 0) {
                    String tagValue = "";
                    switch(getTagType()) {
                    case ECTagTypes.EC_TAGTYPE_UNKNOWN: 
                        tagValue = "--- Can't decode ---";
                        break;
                    case ECTagTypes.EC_TAGTYPE_CUSTOM: 
                        tagValue = "--- Can't decode ---";
                        break;
                    case ECTagTypes.EC_TAGTYPE_UINT8: 
                    case ECTagTypes.EC_TAGTYPE_UINT16:
                    case ECTagTypes.EC_TAGTYPE_UINT32:
                    case ECTagTypes.EC_TAGTYPE_UINT64:
                        tagValue=Long.toString(ECUtils.bytesToUint(rawPayload, tagValueIndex, tagValueLen, true, debug));
                        break;
                    case ECTagTypes.EC_TAGTYPE_STRING:
                    case ECTagTypes.EC_TAGTYPE_DOUBLE:
                        try {
                            tagValue = new String(rawPayload, tagValueIndex, tagValueLen, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            tagValue = "Can't decode as UTF-8 encoding is not supported by this Java environment";
                        }
                        break;
                    case ECTagTypes.EC_TAGTYPE_IPV4: 
                        tagValue = String.format("%d.%d.%d.%d:%d",
                                        ECUtils.bytesToUint(rawPayload, tagValueIndex, 1, true, debug),
                                        ECUtils.bytesToUint(rawPayload, tagValueIndex + 1, 1, true, debug),
                                        ECUtils.bytesToUint(rawPayload, tagValueIndex + 2, 1, true, debug),
                                        ECUtils.bytesToUint(rawPayload, tagValueIndex + 3, 1, true, debug),
                                        ECUtils.bytesToUint(rawPayload, tagValueIndex + 4, 2, true, debug)
                                        );
                        break;
                    case ECTagTypes.EC_TAGTYPE_HASH16:
                        tagValue = "--- No need to decode ---";
                        break;
                    default:
                        tagValue = "--- Can't decode ---";
                        break;
                    }
                    
                    nextIndex = tagValueIndex;
                    s.append(ECUtils.hexDecode(rawPayload, tagValueIndex, tagValueLen, "tagValue="+tagValue , hexPerRow, indent));
                }
            } catch (Exception e) {
                int dumpEnd = (tagEnd > 0 && tagEnd < rawPayload.length) ? tagEnd : rawPayload.length - 1;
                int dumpStart = nextIndex >= 0 ? nextIndex : tagStart; 
                try {
                    s.append(ECUtils.hexDecode(rawPayload, dumpStart, dumpEnd - dumpStart + 1, "ERROR -- Can't decode payload: " + e.getMessage(), hexPerRow, indent));
                } catch (ECDebugException e1) {
                    s.append("Can't decode payload");
                }
            }
            
            return s.toString();
        }
        

        
    }
    

    
    
    
}

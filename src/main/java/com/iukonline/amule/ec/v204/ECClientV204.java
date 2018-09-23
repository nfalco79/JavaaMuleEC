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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.DataFormatException;

import com.iukonline.amule.ec.ECClient;
import com.iukonline.amule.ec.ECCodes;
import com.iukonline.amule.ec.ECPacket;
import com.iukonline.amule.ec.ECPartFile;
import com.iukonline.amule.ec.ECSearchResults;
import com.iukonline.amule.ec.ECTag;
import com.iukonline.amule.ec.ECTagTypes;
import com.iukonline.amule.ec.ECUtils;
import com.iukonline.amule.ec.exceptions.ECClientException;
import com.iukonline.amule.ec.exceptions.ECPacketParsingException;
import com.iukonline.amule.ec.exceptions.ECServerException;
import com.iukonline.amule.ec.exceptions.ECTagParsingException;

public class ECClientV204 extends ECClient {
    
    public ECClientV204() {
        packetParser = ECRawPacketV204.class;
        partFileBuilder = ECPartFileV204.class; 
    }
    
    public boolean isStateful() { return true; }

    @Override
    protected ECPacket buildLoginRequest(long protoVersion) throws ECClientException {
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECCodesV204.EC_OP_AUTH_REQ);
        try {
            epReq.addTag(new ECTag(ECCodesV204.EC_TAG_CLIENT_NAME, clientName));
            epReq.addTag(new ECTag(ECCodesV204.EC_TAG_CLIENT_VERSION, clientVersion));
            epReq.addTag(new ECTag(ECCodesV204.EC_TAG_PROTOCOL_VERSION, ECTagTypes.EC_TAGTYPE_UINT16, protoVersion));
            if (acceptUTF8) {
                epReq.addTag(new ECTag(ECCodesV204.EC_TAG_CAN_UTF8_NUMBERS, ECTagTypes.EC_TAGTYPE_CUSTOM, new byte[0]));
            }
            if (acceptZlib) {
                epReq.addTag(new ECTag(ECCodesV204.EC_TAG_CAN_ZLIB, ECTagTypes.EC_TAGTYPE_CUSTOM, new byte[0]));
            }

        } catch (DataFormatException e) {
            throw new ECClientException("Cannot create login request", e);
        }
        return epReq;
    }
    
    @Override
    protected ECPacket buildLoginRequest() throws ECClientException {
        return buildLoginRequest(ECCodesV204.EC_CURRENT_PROTOCOL_VERSION);
    }
    
    
    @Override
    protected boolean parseLoginResponse(ECPacket epReq, ECPacket epResp) throws ECPacketParsingException, ECServerException, ECClientException, IOException {
        switch (epResp.getOpCode()) {
        case ECCodesV204.EC_OP_AUTH_SALT:
            
            MessageDigest digest;
            try {
                digest = java.security.MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new ECClientException("Cannot get and MD5 digest", e);
            }
            
            // Reverse engineered...
            long passSalt;
            try {
                passSalt = epResp.getTagByName(ECCodesV204.EC_TAG_PASSWD_SALT).getTagValueUInt();
            } catch (DataFormatException e) {
                throw new ECPacketParsingException("Unexpected format for password salt", epResp.getRawPacket(), e);
            }
            if (tracer != null) tracer.println("Got auth salt (long) " + passSalt);
            
            
            // TODO: Check what happens if salt has leading zeros
            
            
            //byte saltHexBytes[] = ECUtils.uintToBytes(passSalt, 8, true);
            //if (tracer != null) tracer.println("Got auth salt " + ECUtils.byteArrayToHexString(saltHexBytes, 8, 0, null));

            String hexBytesString = String.format("%X", passSalt);
            if (tracer != null) tracer.println("Got auth salt " + hexBytesString);
            
            //byte[] saltHash = digest.digest(ECUtils.byteArrayToHexString(saltHexBytes, 8, 0, null).getBytes());
            byte[] saltHash = digest.digest(hexBytesString.getBytes());
            if (tracer != null) tracer.println("Hashed salt " + ECUtils.byteArrayToHexString(saltHash, 16, 0, null));

            digest.reset();
            digest.update(ECUtils.byteArrayToHexString(hashedPassword, 16, 0, null).toLowerCase().getBytes());
            digest.update(ECUtils.byteArrayToHexString(saltHash, 16, 0, null).toLowerCase().getBytes());
            
            byte responseHash[] = digest.digest();
            if (tracer != null) tracer.println("Sending challenge response " + ECUtils.byteArrayToHexString(responseHash, 16, 0, null));

            ECPacket epPassword = new ECPacket();
            epPassword.setOpCode(ECCodesV204.EC_OP_AUTH_PASSWD);
            try {
                epPassword.addTag(new ECTag(ECCodesV204.EC_TAG_PASSWD_HASH, ECTagTypes.EC_TAGTYPE_HASH16, responseHash));
            } catch (DataFormatException e) {
                throw new ECClientException("Error building authentication response", e);
            }            

            ECPacket epFinalResp = sendRequestAndWaitResponse(epPassword, false);
            return super.parseLoginResponse(epPassword, epFinalResp);
        case ECCodes.EC_OP_AUTH_FAIL:
            String errMsg = "No error returned.";
            ECTag tagError = epResp.getTagByName((short) ECCodesV204.EC_TAG_STRING);
            if (tagError != null) {
                try {
                    errMsg = tagError.getTagValueString();
                } catch (DataFormatException e) {
                    throw new ECPacketParsingException("Cannot read returned error message", epResp.getRawPacket(), e);
                }
            }
            throw new ECServerException("Login failed - " + errMsg, epReq, epResp);
            
        default:
            throw new ECPacketParsingException("Unexpected response to login request", epResp.getRawPacket());
        }     
    }

    
    @Override
    protected ECPacket sendGetDloadQueueReq(ECPartFile p, byte detailLevel) throws IOException, ECClientException, ECPacketParsingException, ECServerException {
        if (p != null && p instanceof ECPartFileV204) {
            long id = ((ECPartFileV204) p).getId();
            ECPacket epReq = new ECPacket();
            
            try {
                epReq.addTag(new ECTag(ECCodes.EC_TAG_DETAIL_LEVEL, ECTagTypes.EC_TAGTYPE_UINT8, detailLevel));
            } catch (DataFormatException e) {
                // Should never happen
                throw new ECClientException("Cannot greate GetDloadQueue request", e);
            }
            
            epReq.setOpCode(ECCodesV204.EC_OP_GET_DLOAD_QUEUE);
            try {
                epReq.addTag(new ECTag(ECCodesV204.EC_TAG_PARTFILE, ECTagTypes.EC_TAGTYPE_UINT32, id ));
            } catch (DataFormatException e) {
                throw new ECClientException("Invalid id provided", e);
            } 
            
            ECPacket epResp;
            epResp = sendRequestAndWaitResponse(epReq);
            
            switch (epResp.getOpCode()) {
            case ECCodes.EC_OP_DLOAD_QUEUE:
                if (epResp.getTags().size() > 1) throw new ECPacketParsingException("Unexpected response for single part file GET_DLOAD_QUEUE", epResp.getRawPacket());
                return epResp;
            default:
                throw new ECPacketParsingException("Unexpected response for GET_DLOAD_QUEUE", epResp.getRawPacket());
            }
        } else {
            return super.sendGetDloadQueueReq(p, detailLevel);
        }
    }

    @Override
    public void refreshDlQueue(HashMap<String, ECPartFile> previousQueue, byte detailLevel) throws IOException, ECClientException, ECPacketParsingException, ECServerException {
        
        if (previousQueue == null) return;
        
        ECPacket partFilePacket = sendGetDloadQueueReq(null, detailLevel);
        ArrayList <ECTag> tags = partFilePacket.getTags();
        
        ArrayList <String> oldPartFilesToBeDeleted = new ArrayList<String>(previousQueue.keySet());

        
        for (int i = 0; i < tags.size(); i++) {
            ECTag pt = tags.get(i);
            
            ECTag ht = pt.getSubTagByName(ECCodesV204.EC_TAG_PARTFILE_HASH);
            if (ht == null) throw new ECPacketParsingException("Subtag EC_TAG_PARTFILE_HASH not found", partFilePacket.getRawPacket());
            String hashString;
            try {
                hashString = ECUtils.byteArrayToHexString(ht.getTagValueHash());
            } catch (DataFormatException e) {
                throw new ECPacketParsingException("Subtag EC_TAG_PARTFILE_HASH has uenxptected tag type", partFilePacket.getRawPacket());
            }
            
            ECPartFile prevPartFile = previousQueue.get(hashString);
            if (prevPartFile == null) {
                ECPartFile newPartFile;
                try {
                    newPartFile = partFileBuilder.getConstructor(ECTag.class, Byte.TYPE).newInstance(tags.get(i), detailLevel);
                } catch (InvocationTargetException e) {
                    if (e.getCause() instanceof ECTagParsingException) {
                        throw new ECPacketParsingException("Error parsing partFile packet - " + e.getMessage(), partFilePacket.getRawPacket(), e.getCause());
                    } else {
                        throw new ECPacketParsingException("Error creating partFile builder", partFilePacket.getRawPacket(), e);
                    }
                } catch (Exception e) {
                    throw new ECPacketParsingException("Error creating partFile builder", partFilePacket.getRawPacket(), e);
                }
                
                previousQueue.put(hashString, newPartFile);
            } else {
                try {
                    previousQueue.get(hashString).fillFromTag(pt, detailLevel);
                } catch (ECTagParsingException e) {
                    throw new ECPacketParsingException("Error parsing partFile packet - " + e.getMessage(), partFilePacket.getRawPacket(), e);
                }
                oldPartFilesToBeDeleted.remove(hashString);
            }
        }
        
        for (String hashTBD : oldPartFilesToBeDeleted) {
            previousQueue.remove(hashTBD);
        }
        
    }

    @Override
    public String searchStart(String searchString, String typeText, String extension, long minSize, long maxSize, long availability, byte searchType) throws ECClientException, IOException, ECPacketParsingException, ECServerException {
        ECTag t;
        try {
            t = new ECTag(ECCodesV204.EC_TAG_SEARCH_TYPE, ECTagTypes.EC_TAGTYPE_UINT8, searchType);
            t.addSubTag(new ECTag(ECCodesV204.EC_TAG_SEARCH_NAME, searchString));
            if (typeText != null && typeText.length() > 0) t.addSubTag(new ECTag(ECCodesV204.EC_TAG_SEARCH_FILE_TYPE, typeText));
            if (extension != null && extension.length() > 0) t.addSubTag(new ECTag(ECCodesV204.EC_TAG_SEARCH_EXTENSION, extension));
            if (minSize >= 0) t.addSubTag(new ECTag(ECCodesV204.EC_TAG_SEARCH_MIN_SIZE, ECTagTypes.EC_TAGTYPE_UINT64, minSize));
            if (maxSize >= 0) t.addSubTag(new ECTag(ECCodesV204.EC_TAG_SEARCH_MAX_SIZE, ECTagTypes.EC_TAGTYPE_UINT64, maxSize));
            if (availability > 0) t.addSubTag(new ECTag(ECCodesV204.EC_TAG_SEARCH_AVAILABILITY, ECTagTypes.EC_TAGTYPE_UINT64, availability));
        } catch (DataFormatException e) {
            throw new ECClientException("Cannot create search start request", e);
        }
        
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECCodesV204.EC_OP_SEARCH_START);
        epReq.addTag(t);
        
        ECPacket epResp = sendRequestAndWaitResponse(epReq);
        
        switch (epResp.getOpCode()) {
        case ECCodesV204.EC_OP_STRINGS:
            try {
                ECTag s = epResp.getTagByName(ECCodesV204.EC_TAG_STRING);
                if (s == null) throw new ECPacketParsingException("Expected string not found in search start response", epResp.getRawPacket()); 
                return s.getTagValueString();
            } catch (DataFormatException e) {
                throw new ECPacketParsingException("Unexpected format for search start response", epResp.getRawPacket(), e);
            }
        default:
            throw new ECPacketParsingException("Unexpected response to start search", epResp.getRawPacket());
        }
    }

    @Override
    public void searchStop() throws IOException, ECPacketParsingException, ECServerException, ECClientException  {
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECCodesV204.EC_OP_SEARCH_STOP);
        
        ECPacket epResp = sendRequestAndWaitResponse(epReq);
        
        if (epResp.getOpCode() != ECCodesV204.EC_OP_MISC_DATA) throw new ECPacketParsingException("Unexpected response to sttop search", epResp.getRawPacket());
    }
    
    @Override
    public byte searchProgress() throws ECPacketParsingException, IOException, ECServerException, ECClientException {
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECCodesV204.EC_OP_SEARCH_PROGRESS);
        
        ECPacket epResp = sendRequestAndWaitResponse(epReq);
        
        switch (epResp.getOpCode()) {
        case ECCodesV204.EC_OP_SEARCH_PROGRESS:
            try {
                ECTag s = epResp.getTagByName(ECCodesV204.EC_TAG_SEARCH_STATUS);
                if (s == null) throw new ECPacketParsingException("Expected tag EC_TAG_SEARCH_STATUS not found in search progress response", epResp.getRawPacket());
                long status = s.getTagValueUInt();
                
                if (status >= 0 && status <= 100) return ((byte) status);
                else if (status == 0xffff || status == 0xfffe) return 100;
                else throw new ECPacketParsingException("Unexpected status value returned ", epResp.getRawPacket());

            } catch (DataFormatException e) {
                throw new ECPacketParsingException("Unexpected format for search start response", epResp.getRawPacket(), e);
            }
        default:
            throw new ECPacketParsingException("Unexpected response to search progress", epResp.getRawPacket());
        }
    }
    
    @Override
    public ECSearchResults searchGetReults(ECSearchResults results) throws IOException, ECPacketParsingException, ECServerException, ECClientException {
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECCodesV204.EC_OP_SEARCH_RESULTS);
        try {
            epReq.addTag(new ECTag(ECCodesV204.EC_TAG_DETAIL_LEVEL, ECTagTypes.EC_TAGTYPE_UINT8, ECCodesV204.EC_DETAIL_INC_UPDATE));
        } catch (DataFormatException e) {
            throw new ECClientException("Cannot create search results request", e);
        }
        
        ECPacket epResp = sendRequestAndWaitResponse(epReq);
        
        switch (epResp.getOpCode()) {
        case ECCodesV204.EC_OP_SEARCH_RESULTS:
            ArrayList <ECTag> resTags = epResp.getTags();
            if (resTags == null || resTags.isEmpty()) return new ECSearchResultsV204(ECCodesV204.EC_DETAIL_INC_UPDATE);
            try {
                if (results == null) {
                    return new ECSearchResultsV204(resTags);
                } else {
                    results.updateSearchResults(resTags);
                    return results;
                }
            } catch (ECTagParsingException e) {
                throw new ECPacketParsingException("Error parsing search result response - " + e.getMessage(), epResp.getRawPacket(), e);
            }
        default:
            throw new ECPacketParsingException("Unexpected response to search results", epResp.getRawPacket());
        }
    }
    
}

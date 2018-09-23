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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.DataFormatException;

import com.iukonline.amule.ec.exceptions.ECClientException;
import com.iukonline.amule.ec.exceptions.ECPacketParsingException;
import com.iukonline.amule.ec.exceptions.ECServerException;
import com.iukonline.amule.ec.exceptions.ECTagParsingException;




public class ECClient {
    
    protected Class <? extends ECRawPacket> packetParser = ECRawPacket.class;
    protected Class <? extends ECPartFile> partFileBuilder = ECPartFile.class;
    
    private byte defaultDetailLevel = ECCodes.EC_DETAIL_FULL;
    
    protected String clientName;
    protected String clientVersion;
    protected byte[] hashedPassword;
    private Socket socket;
    protected PrintStream tracer;
    
    private String serverVersion;
    
    private boolean isLoggedIn = false;
    
    protected boolean acceptUTF8 = true;
    protected boolean acceptZlib = true;
    
    public boolean isStateful() { return false; }
    
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }
    
    public void setHashedPassword(byte[] hashedPassword) {
        this.hashedPassword = hashedPassword;
    }
    
    public void setHashedPassword(String hashedPassword) {
        setHashedPassword(ECUtils.hexStringToByteArray(hashedPassword));
    }
    
    public void setPassword(String password) throws NoSuchAlgorithmException, UnsupportedEncodingException  {
        MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
        digest.update(password.getBytes("UTF-8"));
        setHashedPassword(digest.digest());
    }
    
    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setTracer(PrintStream tracer) {
        this.tracer = tracer;
    }
    
    public String getServerVersion() {
        return serverVersion;
    }
    
    public ECPacket sendRequestAndWaitResponse(ECPacket epReq, boolean tryLogin) throws IOException, ECPacketParsingException, ECServerException, ECClientException {

        if (tryLogin && !isLoggedIn) login();
        
        OutputStream os = socket.getOutputStream();
        if (tracer != null) tracer.print("Sending EC packet...\n" + epReq.toString() + "\n\n");

        try {
            epReq.writeToStream(os, packetParser);
        } catch (ECPacketParsingException e) {
            throw new ECPacketParsingException("Error sending request", epReq.getRawPacket(), e);
        }
        if (tracer != null) tracer.print("Sent EC packet...\n" + epReq.toString() + "\n\n");


        BufferedInputStream is = new BufferedInputStream(socket.getInputStream());
        ECPacket epResp;
        try {
            epResp = ECPacket.readFromStream(is, packetParser);
        } catch (ECPacketParsingException e) {
            throw new ECPacketParsingException("Error reading response", e.getCausePacket(), e);
        }
        
        if (tracer != null) tracer.print("Received EC packet...\n" + epResp.toString() + "\n\n");
        
        if (epResp.getOpCode() == ECCodes.EC_OP_FAILED) {
            
            String errMsg = "No error returned.";
            ECTag tagError = epResp.getTagByName((short) ECCodes.EC_TAG_STRING);
            if (tagError != null) {
                try {
                    errMsg = tagError.getTagValueString();
                } catch (DataFormatException e) {
                    throw new ECPacketParsingException("Cannot read returned error message", epResp.getRawPacket(), e);
                }
            }
            
            throw new ECServerException(errMsg, epReq, epResp);
        }
        
        return epResp;

    }
    
    public ECPacket sendRequestAndWaitResponse(ECPacket epReq) throws IOException, ECPacketParsingException, ECServerException, ECClientException {
        return sendRequestAndWaitResponse(epReq, true);
    }
    
    protected ECPacket buildLoginRequest(long protoVersion) throws ECClientException {
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECCodes.EC_OP_AUTH_REQ);
        try {
            epReq.addTag(new ECTag(ECCodes.EC_TAG_CLIENT_NAME, clientName));
            epReq.addTag(new ECTag(ECCodes.EC_TAG_CLIENT_VERSION, clientVersion));
            epReq.addTag(new ECTag(ECCodes.EC_TAG_PROTOCOL_VERSION, ECTagTypes.EC_TAGTYPE_UINT16, protoVersion));
            epReq.addTag(new ECTag(ECCodes.EC_TAG_PASSWD_HASH, ECTagTypes.EC_TAGTYPE_HASH16, hashedPassword));
        } catch (DataFormatException e) {
            throw new ECClientException("Cannot create login request", e);
        }
        return epReq;
    }
    
    
    protected ECPacket buildLoginRequest() throws ECClientException {
        return buildLoginRequest(ECCodes.EC_CURRENT_PROTOCOL_VERSION);
    }
    
    protected boolean parseLoginResponse(ECPacket epReq, ECPacket epResp) throws ECPacketParsingException, ECServerException, ECClientException, IOException {
        
        
        // TEST:
        // boolean a = true;
        // if (a) throw new ECPacketParsingException("TEST EX", epResp.getRawPacket());
        
        
        switch (epResp.getOpCode()) {
        case ECCodes.EC_OP_AUTH_OK:
            ECTag versionTag = epResp.getTagByName(ECCodes.EC_TAG_SERVER_VERSION);
            if (versionTag == null) throw new ECPacketParsingException("Server version not present in auth response", epResp.getRawPacket());
            try {
                serverVersion = versionTag.getTagValueString();
            } catch (DataFormatException e) {
                throw new ECPacketParsingException("Unexpected format for server version", epResp.getRawPacket(), e);
            }            
            isLoggedIn = true;
            return true;
        case ECCodes.EC_OP_AUTH_FAIL:
            String errMsg = "No error returned.";
            ECTag tagError = epResp.getTagByName((short) ECCodes.EC_TAG_STRING);
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

    public boolean login() throws IOException, ECPacketParsingException, ECServerException, ECClientException  {
        ECPacket epReq = buildLoginRequest();
        ECPacket epResp = sendRequestAndWaitResponse(epReq, false);
        return parseLoginResponse(epReq, epResp);
    }
    
    

    protected ECPacket sendGetDloadQueueReq(ECPartFile p, byte detailLevel) throws IOException, ECClientException, ECPacketParsingException, ECServerException {
            
            ECPacket epReq = new ECPacket();
            
            try {
                epReq.addTag(new ECTag(ECCodes.EC_TAG_DETAIL_LEVEL, ECTagTypes.EC_TAGTYPE_UINT8, detailLevel));
            } catch (DataFormatException e) {
                // Should never happen
                throw new ECClientException("Cannot greate GetDloadQueue request", e);
            }
            
            if (p != null) {
                byte hash[] = p.getHash();
                epReq.setOpCode(ECCodes.EC_OP_GET_DLOAD_QUEUE_DETAIL);
                
                try {
                    epReq.addTag(new ECTag(ECCodes.EC_TAG_PARTFILE, ECTagTypes.EC_TAGTYPE_HASH16, hash ));
                } catch (DataFormatException e) {
                    throw new ECClientException("Invalid hash provided", e);
                }
            } else {
                epReq.setOpCode(ECCodes.EC_OP_GET_DLOAD_QUEUE);
            }
            
            ECPacket epResp;
            epResp = sendRequestAndWaitResponse(epReq);

            
            switch (epResp.getOpCode()) {
            case ECCodes.EC_OP_DLOAD_QUEUE:
                if (p != null && epResp.getTags().size() > 1) throw new ECPacketParsingException("Unexpected response for single part file GET_DLOAD_QUEUE", epResp.getRawPacket());
                return epResp;
            default:
                throw new ECPacketParsingException("Unexpected response for GET_DLOAD_QUEUE", epResp.getRawPacket());
            }        
    }
    
    public void refreshPartFile(ECPartFile p, byte detailLevel) throws IOException, ECClientException, ECPacketParsingException, ECServerException {
        ECPacket partFilePacket = sendGetDloadQueueReq(p, detailLevel);
        if (partFilePacket.getTags().isEmpty()) throw new ECClientException("Part file not found");
        try {
            p.fillFromTag(partFilePacket.getTagByName(ECCodes.EC_TAG_PARTFILE), detailLevel);
        } catch (ECTagParsingException e) {
            throw new ECPacketParsingException("Error parsing partFile packet - " + e.getMessage(), partFilePacket.getRawPacket(), e);
        }
    }
    
    public void refreshPartFile(ECPartFile p) throws IOException, ECClientException, ECPacketParsingException, ECServerException {
        refreshPartFile(p, defaultDetailLevel);
    }
    
    public HashMap<String, ECPartFile> getDownloadQueue(byte detailLevel) throws IOException, ECClientException, ECPacketParsingException, ECServerException {
        
        ECPacket partFilePacket = sendGetDloadQueueReq(null, detailLevel);
        
        ArrayList <ECTag> tags = partFilePacket.getTags();
        
        HashMap<String, ECPartFile> dlQueue = new HashMap<String, ECPartFile>(tags.size());
        
        for (int i = 0; i < tags.size(); i++) {
            ECPartFile p;
            try {
                p = partFileBuilder.getConstructor(ECTag.class, Byte.TYPE).newInstance(tags.get(i), detailLevel);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof ECTagParsingException) {
                    throw new ECPacketParsingException("Error parsing partFile packet - " + e.getCause().getMessage(), partFilePacket.getRawPacket(), e.getCause());
                } else {
                    throw new ECPacketParsingException("Error creating partFile builder", partFilePacket.getRawPacket(), e);
                }
            } catch (Exception e) {
                throw new ECPacketParsingException("Error creating partFile builder", partFilePacket.getRawPacket(), e);
            }
            dlQueue.put(ECUtils.byteArrayToHexString(p.getHash()), p);
        }
        return dlQueue;
    }
    
    public HashMap<String, ECPartFile> getDownloadQueue() throws IOException, ECClientException, ECPacketParsingException, ECServerException {
        return getDownloadQueue(defaultDetailLevel);
    }
    
    public void refreshDlQueue(HashMap<String, ECPartFile> previousQueue) throws IOException, ECClientException, ECPacketParsingException, ECServerException {
        refreshDlQueue(previousQueue, defaultDetailLevel);
    }
    
    public void refreshDlQueue(HashMap<String, ECPartFile> previousQueue, byte detailLevel) throws IOException, ECClientException, ECPacketParsingException, ECServerException {
        
        if (previousQueue == null) return;
        
        HashMap<String, ECPartFile> newQueue = getDownloadQueue(detailLevel);
        ArrayList <String> hashesToBeRemoved = new ArrayList<String>();
        
        Iterator<ECPartFile> iPrev = previousQueue.values().iterator();
        while (iPrev.hasNext()) {
            ECPartFile p = iPrev.next();
            String hString = p.getHashAsString();
            if (newQueue.containsKey(hString)) {
                p.copyValuesFromPartFile(newQueue.get(hString));
                newQueue.remove(hString);
            } else {
                hashesToBeRemoved.add(hString);
            }
        }
        
        Iterator<String> iRemove = hashesToBeRemoved.iterator();
        while (iRemove.hasNext()) {
            previousQueue.remove(iRemove.next());
        }
        
        Iterator<ECPartFile> iNew = newQueue.values().iterator();
        while (iNew.hasNext()) {
            ECPartFile p = iNew.next();
            previousQueue.put(p.getHashAsString(), p);
        }
        
    }
    
    public ECStats getStats() throws IOException, ECClientException, ECPacketParsingException, ECServerException {
        return getStats(defaultDetailLevel);
    }
    
    public ECStats getStats(byte detailLevel) throws IOException, ECClientException, ECPacketParsingException, ECServerException  {
        
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECCodes.EC_OP_STAT_REQ);
        
        try {
            epReq.addTag(new ECTag(ECCodes.EC_TAG_DETAIL_LEVEL, ECTagTypes.EC_TAGTYPE_UINT8, detailLevel));
        } catch (DataFormatException e) {
            throw new ECClientException("Cannot build get stats request", e);
        }
        
        ECPacket epResp = sendRequestAndWaitResponse(epReq);
        switch (epResp.getOpCode()) {
        case ECCodes.EC_OP_STATS:
            try {
                return new ECStats(epResp, detailLevel);
            } catch (ECTagParsingException e) {
                throw new ECPacketParsingException("Error parsing response to OP_STAT_REQ - " + e.getMessage(), epResp.getRawPacket(), e);
            }
        default:
            throw new ECPacketParsingException("Unexpected response to OP_STAT_REQ", epResp.getRawPacket());
        }
        
    }

    
    public void changeDownloadStatus(byte[] hash, byte opCode) throws IOException, ECClientException, ECPacketParsingException, ECServerException {

        ECPacket epReq = new ECPacket();
        epReq.setOpCode(opCode);
        try {
            epReq.addTag(new ECTag(ECCodes.EC_TAG_PARTFILE, ECTagTypes.EC_TAGTYPE_HASH16, hash));
        } catch (DataFormatException e) {
           
            throw new ECClientException("Cannot create change download status request", e);
        }
        
        ECPacket epResp = sendRequestAndWaitResponse(epReq);
        
        if (epResp.getOpCode() != ECCodes.EC_OP_NOOP) throw new ECPacketParsingException("Unexpected response to change download status request", epResp.getRawPacket());
    }

    public void addED2KLink(String url) throws IOException, ECClientException, ECPacketParsingException, ECServerException {
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECCodes.EC_OP_ADD_LINK);
        try {
            epReq.addTag(new ECTag(ECCodes.EC_TAG_PARTFILE_ED2K_LINK, ECTagTypes.EC_TAGTYPE_STRING, url));
        } catch (DataFormatException e) {
            throw new ECClientException("Invalid URI provided", e);
        }
        ECPacket epResp = sendRequestAndWaitResponse(epReq);
        if (epResp.getOpCode() != ECCodes.EC_OP_NOOP) throw new ECPacketParsingException("Unexpected response to add link request", epResp.getRawPacket());        
    }

    
    public void renamePartFile(byte[] hash, String newName) throws IOException, ECClientException, ECPacketParsingException, ECServerException {
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECCodes.EC_OP_RENAME_FILE);
        try {
            epReq.addTag(new ECTag(ECCodes.EC_TAG_KNOWNFILE, ECTagTypes.EC_TAGTYPE_HASH16, hash));
            epReq.addTag(new ECTag(ECCodes.EC_TAG_PARTFILE_NAME, ECTagTypes.EC_TAGTYPE_STRING, newName));
        } catch (DataFormatException e) {
            throw new ECClientException("Error creating rename request", e);
        }
        
        ECPacket epResp = sendRequestAndWaitResponse(epReq);
        if (epResp.getOpCode() != ECCodes.EC_OP_NOOP) throw new ECPacketParsingException("Unexpected response to rename request", epResp.getRawPacket());        
    }
    
    
    public void setPartFilePriority(byte[] hash, byte prio) throws IOException, ECClientException, ECPacketParsingException, ECServerException {
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECCodes.EC_OP_PARTFILE_PRIO_SET);
        try {
            ECTag t = new ECTag(ECCodes.EC_TAG_PARTFILE, ECTagTypes.EC_TAGTYPE_HASH16, hash);
            t.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_PRIO, ECTagTypes.EC_TAGTYPE_UINT8, prio));
            epReq.addTag(t);
            
        } catch (DataFormatException e) {
            throw new ECClientException("Cannot create set priorityrequest", e);
        }
        
        ECPacket epResp = sendRequestAndWaitResponse(epReq);
        if (epResp.getOpCode() != ECCodes.EC_OP_NOOP) throw new ECPacketParsingException("Unexpected response to set priority", epResp.getRawPacket());        
    }
    
    public void setPartFileCategory(byte[] hash, long catId) throws IOException, ECClientException, ECPacketParsingException, ECServerException {
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECCodes.EC_OP_PARTFILE_SET_CAT);
        try {
            ECTag t = new ECTag(ECCodes.EC_TAG_PARTFILE, ECTagTypes.EC_TAGTYPE_HASH16, hash);
            t.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_CAT, ECTagTypes.EC_TAGTYPE_UINT64, catId));
            epReq.addTag(t);
            
        } catch (DataFormatException e) {
            throw new ECClientException("Cannot create set priorityrequest", e);
        }
        
        ECPacket epResp = sendRequestAndWaitResponse(epReq);
        if (epResp.getOpCode() != ECCodes.EC_OP_NOOP) throw new ECPacketParsingException("Unexpected response to set category", epResp.getRawPacket());        
    }
    
    public ECCategory[] getCategories(byte detailLevel) throws IOException, ECClientException, ECPacketParsingException, ECServerException {
        
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECCodes.EC_OP_GET_PREFERENCES);
        try {
            epReq.addTag(new ECTag(ECCodes.EC_TAG_SELECT_PREFS, ECTagTypes.EC_TAGTYPE_UINT32, ECCodes.EC_PREFS_CATEGORIES));
            epReq.addTag(new ECTag(ECCodes.EC_TAG_DETAIL_LEVEL, ECTagTypes.EC_TAGTYPE_UINT8, detailLevel));
        } catch (DataFormatException e) {
            throw new ECClientException("Cannot create get categories request", e);
        }
      
        ECPacket epResp = sendRequestAndWaitResponse(epReq);
        switch (epResp.getOpCode()) {
        case ECCodes.EC_OP_SET_PREFERENCES:

            ECTag t = epResp.getTagByName(ECCodes.EC_TAG_PREFS_CATEGORIES);
            if (t == null) return null;
            
            ArrayList <ECTag> l = t.getSubTags();
            if (l.size() == 0) return null;
            
            ECCategory[] categoryList = new ECCategory[l.size()];
            for (int i = 0; i < l.size(); i++) {
                ECTag t1 = l.get(i);
                if (t1.getTagName() != ECCodes.EC_TAG_CATEGORY) throw new ECPacketParsingException("Unexpected tag " + t1.getTagName() + " found while looking for EC_TAG_CATEGORY", epResp.getRawPacket());
                try {
                    categoryList[i] = new ECCategory(t1, detailLevel);
                } catch (ECTagParsingException e) {
                    throw new ECPacketParsingException("Error parsing response to get categories", epResp.getRawPacket(), e);
                }
            }
            return categoryList;
        default:
            throw new ECPacketParsingException("Unexpected response to get categories", epResp.getRawPacket());        
        }
    }
    
    
    public void createCategory(ECCategory c) throws IOException, ECPacketParsingException, ECServerException, ECClientException {
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECCodes.EC_OP_CREATE_CATEGORY);
        epReq.addTag(c.toECTag());

        ECPacket epResp = sendRequestAndWaitResponse(epReq);
        if (epResp.getOpCode() != ECCodes.EC_OP_NOOP) throw new ECPacketParsingException("Unexpected response to create category", epResp.getRawPacket());        
    }

    
    public void updateCategory(ECCategory c) throws IOException, ECPacketParsingException, ECServerException, ECClientException {
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECCodes.EC_OP_UPDATE_CATEGORY);
        epReq.addTag(c.toECTag());
        
        ECPacket epResp = sendRequestAndWaitResponse(epReq);
        if (epResp.getOpCode() != ECCodes.EC_OP_NOOP) throw new ECPacketParsingException("Unexpected response to update category", epResp.getRawPacket());        
    }
    
    public void deleteCategory(long id) throws IOException, ECPacketParsingException, ECServerException, ECClientException {
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECCodes.EC_OP_DELETE_CATEGORY);
        try {
            epReq.addTag(new ECTag(ECCodes.EC_TAG_CATEGORY, ECTagTypes.EC_TAGTYPE_UINT32, id));
        } catch (DataFormatException e) {
            throw new ECClientException("Cannot craete delete category request", e);
        }
      
        ECPacket epResp = sendRequestAndWaitResponse(epReq);
        if (epResp.getOpCode() != ECCodes.EC_OP_NOOP) throw new ECPacketParsingException("Unexpected response to delete category", epResp.getRawPacket());        
    }
    
    
    
    
    public String searchStart(String searchString, String typeText, String extension, long minSize, long maxSize, long availability, byte searchType) throws ECClientException, IOException, ECPacketParsingException, ECServerException {
        throw new ECClientException("Search not implemented yet for this server version");
    }

    public void searchStop() throws IOException, ECPacketParsingException, ECServerException, ECClientException  {
        throw new ECClientException("Search not implemented yet for this server version");
    }
    
    public byte searchProgress() throws ECPacketParsingException, IOException, ECServerException, ECClientException {
        throw new ECClientException("Search not implemented tey for this server version");
    }
    
    public ECSearchResults searchGetReults(ECSearchResults results) throws IOException, ECPacketParsingException, ECServerException, ECClientException {
        throw new ECClientException("Search not implemented yet for this server version");

    }
    
    public void searchStartResult(ECSearchFile f) throws IOException, ECPacketParsingException, ECServerException, ECClientException {
        
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECCodes.EC_OP_DOWNLOAD_SEARCH_RESULT);
        try {
            epReq.addTag(new ECTag(ECCodes.EC_TAG_PARTFILE, ECTagTypes.EC_TAGTYPE_HASH16, f.getHash()));
        } catch (DataFormatException e) {
            throw new ECClientException("Cannot create start download search result request", e);
        }
        
        ECPacket epResp = sendRequestAndWaitResponse(epReq);

        if (epResp.getOpCode() != ECCodes.EC_OP_STRINGS) {
            throw new ECPacketParsingException("Unexpected response to start download search result", epResp.getRawPacket());    
        }
    }
    
    
    
    
    
    

    @Override
    public String toString() {
        //return String.format("ECClient [clientName=%s, clientVersion=%s, socket=%s]", clientName, clientVersion, socket);
        return String.format("ECClient [socket=%s]", socket);
    }
}

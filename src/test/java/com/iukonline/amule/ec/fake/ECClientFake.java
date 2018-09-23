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

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.DataFormatException;

import com.iukonline.amule.ec.ECClient;
import com.iukonline.amule.ec.ECCodes;
import com.iukonline.amule.ec.ECPacket;
import com.iukonline.amule.ec.ECPartFile;
import com.iukonline.amule.ec.ECStats;
import com.iukonline.amule.ec.ECTag;
import com.iukonline.amule.ec.ECTagTypes;
import com.iukonline.amule.ec.ECUtils;
import com.iukonline.amule.ec.exceptions.ECClientException;
import com.iukonline.amule.ec.exceptions.ECPacketParsingException;
import com.iukonline.amule.ec.exceptions.ECServerException;
import com.iukonline.amule.ec.exceptions.ECTagParsingException;

public class ECClientFake extends ECClient {
    
    static HashMap <String, ECTag> partFileList = new HashMap<String, ECTag> ();
    
    static {
        try {
            byte[] hash1 = { 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01 }; 
            
            ECTag pf1;
            
            pf1 = new ECTag(ECCodes.EC_TAG_PARTFILE, ECTagTypes.EC_TAGTYPE_HASH16, hash1);
            pf1.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_NAME, ECTagTypes.EC_TAGTYPE_STRING, "FILE number 1.xxx"));
            pf1.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_PARTMETID, ECTagTypes.EC_TAGTYPE_UINT8, 1));

            
            pf1.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_NAME, ECTagTypes.EC_TAGTYPE_STRING, "FILE number 1.xxx"));
            pf1.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_ED2K_LINK, ECTagTypes.EC_TAGTYPE_STRING, "ed2k://sssss/"));
            pf1.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_STATUS, ECTagTypes.EC_TAGTYPE_UINT8, ECPartFile.PS_READY));
            pf1.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_PRIO, ECTagTypes.EC_TAGTYPE_UINT8, ECPartFile.PR_AUTO_HIGH));
            pf1.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_SPEED, ECTagTypes.EC_TAGTYPE_UINT8, 100));
            pf1.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_SIZE_FULL, ECTagTypes.EC_TAGTYPE_UINT16, 30000));
            pf1.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_SIZE_DONE, ECTagTypes.EC_TAGTYPE_UINT16, 10000));
            pf1.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_SIZE_XFER, ECTagTypes.EC_TAGTYPE_UINT16, 10000));

            pf1.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_SOURCE_COUNT, ECTagTypes.EC_TAGTYPE_UINT8, 100));
            pf1.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_SOURCE_COUNT_A4AF, ECTagTypes.EC_TAGTYPE_UINT8, 5));
            pf1.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_SOURCE_COUNT_XFER, ECTagTypes.EC_TAGTYPE_UINT8, 1));
            pf1.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_SOURCE_COUNT_NOT_CURRENT, ECTagTypes.EC_TAGTYPE_UINT8, 2));
            pf1.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_LAST_SEEN_COMP, ECTagTypes.EC_TAGTYPE_UINT8, 1));
            pf1.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_CAT, ECTagTypes.EC_TAGTYPE_UINT8, 1));

            
            ECTag com1 = new ECTag(ECCodes.EC_TAG_PARTFILE_COMMENTS, ECTagTypes.EC_TAGTYPE_UINT8, 0);
            
            com1.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_COMMENTS, ECTagTypes.EC_TAGTYPE_STRING, "AUTHOR 1"));
            com1.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_COMMENTS, ECTagTypes.EC_TAGTYPE_STRING, "PART NAME 1"));
            com1.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_COMMENTS, ECTagTypes.EC_TAGTYPE_UINT8, ECPartFile.RATING_POOR));
            com1.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_COMMENTS, ECTagTypes.EC_TAGTYPE_STRING, "COMMENT 1"));
            
            com1.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_COMMENTS, ECTagTypes.EC_TAGTYPE_STRING, "AUTHOR 2"));
            com1.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_COMMENTS, ECTagTypes.EC_TAGTYPE_STRING, "PART NAME 2"));
            com1.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_COMMENTS, ECTagTypes.EC_TAGTYPE_UINT8, ECPartFile.RATING_GOOD));
            com1.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_COMMENTS, ECTagTypes.EC_TAGTYPE_STRING, "COMMENT 2"));
            
            com1.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_COMMENTS, ECTagTypes.EC_TAGTYPE_STRING, "AUTHOR 3"));
            com1.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_COMMENTS, ECTagTypes.EC_TAGTYPE_STRING, "PART NAME 3"));
            com1.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_COMMENTS, ECTagTypes.EC_TAGTYPE_UINT8, ECPartFile.RATING_EXCELLENT));
            com1.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_COMMENTS, ECTagTypes.EC_TAGTYPE_STRING, "COMMENT 3"));
            
            pf1.addSubTag(com1);
            
            ECTag sn1 = new ECTag(ECCodes.EC_TAG_PARTFILE_SOURCE_NAMES, ECTagTypes.EC_TAGTYPE_UINT8, 0);
            
            sn1.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_SOURCE_NAMES, ECTagTypes.EC_TAGTYPE_STRING, "PART NAME 1"));
            sn1.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_SOURCE_NAMES, ECTagTypes.EC_TAGTYPE_UINT8, 1));
            sn1.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_SOURCE_NAMES, ECTagTypes.EC_TAGTYPE_STRING, "PART NAME 2"));
            sn1.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_SOURCE_NAMES, ECTagTypes.EC_TAGTYPE_UINT8, 2));
            sn1.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_SOURCE_NAMES, ECTagTypes.EC_TAGTYPE_STRING, "PART NAME 3"));
            sn1.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_SOURCE_NAMES, ECTagTypes.EC_TAGTYPE_UINT8, 3));
            sn1.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_SOURCE_NAMES, ECTagTypes.EC_TAGTYPE_STRING, "PART NAME 4"));
            sn1.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_SOURCE_NAMES, ECTagTypes.EC_TAGTYPE_UINT8, 4));
            sn1.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_SOURCE_NAMES, ECTagTypes.EC_TAGTYPE_STRING, "PART NAME 5"));
            sn1.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_SOURCE_NAMES, ECTagTypes.EC_TAGTYPE_UINT8, 5));
            
            pf1.addSubTag(sn1);
            
            byte[] hash2 = { 0x02, 0x02, 0x02, 0x02,0x02, 0x02, 0x02, 0x02,0x02, 0x02, 0x02, 0x02,0x02, 0x02, 0x02, 0x02 };    
            
            ECTag pf2;
            
            pf2 = new ECTag(ECCodes.EC_TAG_PARTFILE, ECTagTypes.EC_TAGTYPE_HASH16, hash2);
            pf2.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_NAME, ECTagTypes.EC_TAGTYPE_STRING, "FILE number 2.xxx"));
            pf2.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_PARTMETID, ECTagTypes.EC_TAGTYPE_UINT8, 2));
            
            pf2.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_NAME, ECTagTypes.EC_TAGTYPE_STRING, "FILE number 2.xxx"));
            pf2.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_ED2K_LINK, ECTagTypes.EC_TAGTYPE_STRING, "ed2k://ttttt/"));
            pf2.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_STATUS, ECTagTypes.EC_TAGTYPE_UINT8, ECPartFile.PS_READY));
            pf2.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_PRIO, ECTagTypes.EC_TAGTYPE_UINT8, ECPartFile.PR_AUTO_HIGH));
            pf2.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_SPEED, ECTagTypes.EC_TAGTYPE_UINT8, 100));
            pf2.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_SIZE_FULL, ECTagTypes.EC_TAGTYPE_UINT16, 30000));
            pf2.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_SIZE_DONE, ECTagTypes.EC_TAGTYPE_UINT16, 10000));
            pf2.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_SIZE_XFER, ECTagTypes.EC_TAGTYPE_UINT16, 10000));
            pf2.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_SOURCE_COUNT, ECTagTypes.EC_TAGTYPE_UINT8, 100));
            pf2.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_SOURCE_COUNT_A4AF, ECTagTypes.EC_TAGTYPE_UINT8, 5));
            pf2.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_SOURCE_COUNT_XFER, ECTagTypes.EC_TAGTYPE_UINT8, 1));
            pf2.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_SOURCE_COUNT_NOT_CURRENT, ECTagTypes.EC_TAGTYPE_UINT8, 2));
            pf2.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_LAST_SEEN_COMP, ECTagTypes.EC_TAGTYPE_UINT8, 1));
            pf2.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_CAT, ECTagTypes.EC_TAGTYPE_UINT8, 0));
            
            partFileList.put(ECUtils.byteArrayToHexString(hash1), pf1);
            partFileList.put(ECUtils.byteArrayToHexString(hash2), pf2);
            
        
        } catch (DataFormatException e) {
            e.printStackTrace();
        }
        
    }


    @Override
    public boolean login() throws IOException, ECPacketParsingException, ECServerException, ECClientException {
        return true;
    }

    @Override
    public ECPacket sendRequestAndWaitResponse(ECPacket epReq, boolean tryLogin) throws IOException, ECPacketParsingException, ECServerException,
                    ECClientException {
        
        ECPacket epResp;
        
        try { Thread.sleep(2000); } catch (InterruptedException e1) { e1.printStackTrace(); }
        
        switch (epReq.getOpCode()) {
        case ECCodes.EC_OP_GET_DLOAD_QUEUE:
            epResp = generateDlQueuePacket();
            break;
            
        case ECCodes.EC_OP_GET_DLOAD_QUEUE_DETAIL:
            epResp = generateDlQueuePacketDetail(epReq);
            break;
            
        case ECCodes.EC_OP_STAT_REQ:
            epResp = generateStatsPacket();
            break;
            
        case ECCodes.EC_OP_PARTFILE_PAUSE:
        case ECCodes.EC_OP_PARTFILE_RESUME:
        case ECCodes.EC_OP_PARTFILE_DELETE:
        case ECCodes.EC_OP_PARTFILE_SWAP_A4AF_THIS:
        case ECCodes.EC_OP_PARTFILE_SWAP_A4AF_THIS_AUTO:
        case ECCodes.EC_OP_PARTFILE_SWAP_A4AF_OTHERS:
            epResp = doActionOnPartFile(epReq);
            break;
        case ECCodes.EC_OP_PARTFILE_PRIO_SET:
            epResp = changePartFilePrio(epReq);
            break;
        case ECCodes.EC_OP_PARTFILE_SET_CAT:
            epResp = changePartFileCat(epReq);
            break;
        case ECCodes.EC_OP_RENAME_FILE:
            epResp = renamePartFile(epReq);
            break;
        case ECCodes.EC_OP_GET_PREFERENCES:
            epResp = generatePreferencesPacket(epReq);
            break;
            
        case ECCodes.EC_OP_ADD_LINK:
            epResp = generateAddLinkPacket(epReq);
            break;
            
            
        default:
            epResp = new ECPacket();
            epResp.setOpCode(ECCodes.EC_OP_FAILED);
            try {
                epResp.addTag(new ECTag(ECCodes.EC_TAG_STRING, ECTagTypes.EC_TAGTYPE_STRING, "FAKE SERVER - Unknown op code received"));
            } catch (DataFormatException e) {

                e.printStackTrace();
            }
            break;
        }
        return epResp;

    }

    private ECPacket generateStatsPacket() {
        ECPacket stats = new ECPacket();
        stats.setOpCode(ECCodes.EC_OP_STATS);
        try {
            stats.addTag(new ECTag(ECCodes.EC_TAG_STATS_DL_SPEED, ECTagTypes.EC_TAGTYPE_UINT16, 100));
            stats.addTag(new ECTag(ECCodes.EC_TAG_STATS_UL_SPEED, ECTagTypes.EC_TAGTYPE_UINT16, 200));
        } catch (DataFormatException e) {

            e.printStackTrace();
        }
        
        
        
        return stats;
        
    }
    
    private ECPacket generateDlQueuePacket() {
        ECPacket dlQueue = new ECPacket();
        dlQueue.setOpCode(ECCodes.EC_OP_DLOAD_QUEUE);
        
        Iterator <ECTag> i = partFileList.values().iterator();
        while (i.hasNext()) {
            dlQueue.addTag(i.next());
        }
        return dlQueue;
    }
    
    private ECPacket generateDlQueuePacketDetail(ECPacket epReq) {
        ECPacket detail = new ECPacket();
        
        
        try {
            byte[] hash = epReq.getTagByName(ECCodes.EC_TAG_PARTFILE).getTagValueHash();
            ECTag t = partFileList.get(ECUtils.byteArrayToHexString(hash));
            
            if (t == null) {
                detail.setOpCode(ECCodes.EC_OP_FAILED);
                detail.addTag(new ECTag(ECCodes.EC_TAG_STRING, ECTagTypes.EC_TAGTYPE_STRING, "Invalid partfile requested"));
            } else {
                detail.setOpCode(ECCodes.EC_OP_DLOAD_QUEUE);
                detail.addTag(partFileList.get(ECUtils.byteArrayToHexString(hash)));
            }
            
        } catch (DataFormatException e1) {

            e1.printStackTrace();
        }
        return detail;
    }
    
    private ECPacket generateAddLinkPacket(ECPacket epReq) {
        ECPacket result = new ECPacket();
        
        try {
            String link = epReq.getTagByName(ECCodes.EC_TAG_PARTFILE_ED2K_LINK).getTagValueString();

            Iterator <ECTag> it = partFileList.values().iterator();
            if (it.hasNext()) {
                
                String linkParts[] = link.split("\\|");
                String newHash;
                //System.out.println("len = " + linkParts.length);
                if (linkParts.length >= 5) {
                    newHash = linkParts[4];
                } else {
                    newHash = "";
                    for (int i = 0; i < 32; i++) {
                       int randChar = (int) (Math.random() * 16);
                       newHash += String.format("%X", randChar);
                    }
                }
                //System.out.println("newHash - " + newHash);
                ECTag newFile = new ECTag(ECCodes.EC_TAG_PARTFILE, ECTagTypes.EC_TAGTYPE_HASH16, ECUtils.hexStringToByteArray(newHash));
                ECTag copy = it.next();
                for (ECTag t : copy.getSubTags()) {
                    if (t.getTagName() != ECCodes.EC_TAG_PARTFILE_ED2K_LINK) {
                        newFile.addSubTag(new ECTag(t));
                    }
                }
                newFile.addSubTag(new ECTag(ECCodes.EC_TAG_PARTFILE_ED2K_LINK, ECTagTypes.EC_TAGTYPE_STRING, link));
                partFileList.put(ECUtils.byteArrayToHexString(ECUtils.hexStringToByteArray(newHash)), newFile);
                
                result.setOpCode(ECCodes.EC_OP_NOOP);
            } else {
                result.setOpCode(ECCodes.EC_OP_FAILED);
                result.addTag(new ECTag(ECCodes.EC_TAG_STRING, ECTagTypes.EC_TAGTYPE_STRING, "List empty - no file to copy"));

            }
        } catch (DataFormatException e) {
            result.setOpCode(ECCodes.EC_OP_FAILED);
            try {
                result.addTag(new ECTag(ECCodes.EC_TAG_STRING, ECTagTypes.EC_TAGTYPE_STRING, "Error adding link..." + e.getMessage()));
            } catch (DataFormatException e1) {
            }

        }
        
        return result;
    }
    
    private ECPacket doActionOnPartFile(ECPacket epReq) {
        ECPacket result = new ECPacket();
        
        try {
            byte[] hash = epReq.getTagByName(ECCodes.EC_TAG_PARTFILE).getTagValueHash();
            ECTag t = partFileList.get(ECUtils.byteArrayToHexString(hash));
            
            if (t == null) {
                result.setOpCode(ECCodes.EC_OP_FAILED);
                result.addTag(new ECTag(ECCodes.EC_TAG_STRING, ECTagTypes.EC_TAGTYPE_STRING, "Invalid partfile requested"));
            } else {
                
                switch (epReq.getOpCode()) {
                case ECCodes.EC_OP_PARTFILE_PAUSE:
                    t.getSubTagByName(ECCodes.EC_TAG_PARTFILE_STATUS).setTagValueUInt(ECPartFile.PS_PAUSED);
                    t.getSubTagByName(ECCodes.EC_TAG_PARTFILE_SPEED).setTagValueUInt(0);
                    t.getSubTagByName(ECCodes.EC_TAG_PARTFILE_SOURCE_COUNT_XFER).setTagValueUInt(0);
                    break;
                case ECCodes.EC_OP_PARTFILE_RESUME:
                    t.getSubTagByName(ECCodes.EC_TAG_PARTFILE_STATUS).setTagValueUInt(ECPartFile.PS_READY);
                    t.getSubTagByName(ECCodes.EC_TAG_PARTFILE_SPEED).setTagValueUInt(100);
                    t.getSubTagByName(ECCodes.EC_TAG_PARTFILE_SOURCE_COUNT_XFER).setTagValueUInt(1);
                    break;

                case ECCodes.EC_OP_PARTFILE_DELETE:
                    partFileList.remove(ECUtils.byteArrayToHexString(hash));
                    
                case ECCodes.EC_OP_PARTFILE_SWAP_A4AF_THIS:
                case ECCodes.EC_OP_PARTFILE_SWAP_A4AF_THIS_AUTO:
                    t.getSubTagByName(ECCodes.EC_TAG_PARTFILE_SOURCE_COUNT_A4AF).setTagValueUInt(0);
                    break;
                case ECCodes.EC_OP_PARTFILE_SWAP_A4AF_OTHERS:
                    t.getSubTagByName(ECCodes.EC_TAG_PARTFILE_SOURCE_COUNT_A4AF).setTagValueUInt(1);
                    break;
                }
                
                result.setOpCode(ECCodes.EC_OP_NOOP);
                
            }
            
        } catch (DataFormatException e1) {
            
            e1.printStackTrace();
        }
        
        return result;
        
    }
    
    private ECPacket changePartFilePrio(ECPacket epReq) {
        ECPacket result = new ECPacket();
        
        try {
            byte[] hash = epReq.getTagByName(ECCodes.EC_TAG_PARTFILE).getTagValueHash();
            ECTag t = partFileList.get(ECUtils.byteArrayToHexString(hash));
            
            if (t == null) {
                result.setOpCode(ECCodes.EC_OP_FAILED);
                result.addTag(new ECTag(ECCodes.EC_TAG_STRING, ECTagTypes.EC_TAGTYPE_STRING, "Invalid partfile requested"));
            } else {
                result.setOpCode(ECCodes.EC_OP_NOOP);
                t.getSubTagByName(ECCodes.EC_TAG_PARTFILE_PRIO).setTagValueUInt(epReq.getTagByName(ECCodes.EC_TAG_PARTFILE).getSubTagByName(ECCodes.EC_TAG_PARTFILE_PRIO).getTagValueUInt());
            }
        } catch (DataFormatException e1) {
            
            e1.printStackTrace();
        }
        
        return result;
        
    }

    private ECPacket changePartFileCat(ECPacket epReq) {
        ECPacket result = new ECPacket();
        
        try {
            byte[] hash = epReq.getTagByName(ECCodes.EC_TAG_PARTFILE).getTagValueHash();
            ECTag t = partFileList.get(ECUtils.byteArrayToHexString(hash));
            
            if (t == null) {
                result.setOpCode(ECCodes.EC_OP_FAILED);
                result.addTag(new ECTag(ECCodes.EC_TAG_STRING, ECTagTypes.EC_TAGTYPE_STRING, "Invalid partfile requested"));
            } else {
                result.setOpCode(ECCodes.EC_OP_NOOP);
                t.getSubTagByName(ECCodes.EC_TAG_PARTFILE_CAT).setTagValueUInt(epReq.getTagByName(ECCodes.EC_TAG_PARTFILE).getSubTagByName(ECCodes.EC_TAG_PARTFILE_CAT).getTagValueUInt());
            }
        } catch (DataFormatException e1) {
            
            e1.printStackTrace();
        }
        
        return result;
        
    }
    
    private ECPacket renamePartFile(ECPacket epReq) {
        ECPacket result = new ECPacket();
        
        try {
            byte[] hash = epReq.getTagByName(ECCodes.EC_TAG_PARTFILE).getTagValueHash();
            ECTag t = partFileList.get(ECUtils.byteArrayToHexString(hash));
            
            if (t == null) {
                result.setOpCode(ECCodes.EC_OP_FAILED);
                result.addTag(new ECTag(ECCodes.EC_TAG_STRING, ECTagTypes.EC_TAGTYPE_STRING, "Invalid partfile requested"));
            } else {
                result.setOpCode(ECCodes.EC_OP_NOOP);
                t.getSubTagByName(ECCodes.EC_TAG_PARTFILE_NAME).setTagValueString(epReq.getTagByName(ECCodes.EC_TAG_PARTFILE).getSubTagByName(ECCodes.EC_TAG_PARTFILE_NAME).getTagValueString());
            }
        } catch (DataFormatException e1) {
            
            e1.printStackTrace();
        }
        
        return result;
        
    }
    
    private ECPacket generatePreferencesPacket(ECPacket epReq) {
        ECTag p = epReq.getTagByName(ECCodes.EC_TAG_SELECT_PREFS);
        if (p != null) {
            try {
                switch ((int) p.getTagValueUInt()) {
                case ECCodes.EC_PREFS_CATEGORIES:
                    return generateCategoriesPacket();
                }
            } catch (DataFormatException e) {
            }
        }
        
        ECPacket epResp = new ECPacket();
        epResp.setOpCode(ECCodes.EC_OP_FAILED);
        try {
            epResp.addTag(new ECTag(ECCodes.EC_TAG_STRING, ECTagTypes.EC_TAGTYPE_STRING, "FAKE SERVER - Unknown op code received"));
        } catch (DataFormatException e) {
        }
        return epResp;

    }
    
    private ECPacket generateCategoriesPacket() {
        ECPacket result = new ECPacket();
        result.setOpCode(ECCodes.EC_OP_SET_PREFERENCES);
        try {
            ECTag catsTag = new ECTag(ECCodes.EC_TAG_PREFS_CATEGORIES, ECTagTypes.EC_TAGTYPE_CUSTOM);
            
            
            ECTag noCatTag = new ECTag(ECCodes.EC_TAG_CATEGORY, ECTagTypes.EC_TAGTYPE_UINT8, 0);
            noCatTag.addSubTag(new ECTag(ECCodes.EC_TAG_CATEGORY_PATH, ECTagTypes.EC_TAGTYPE_STRING, "/"));
            noCatTag.addSubTag(new ECTag(ECCodes.EC_TAG_CATEGORY_PRIO, ECTagTypes.EC_TAGTYPE_UINT8, 0));
            noCatTag.addSubTag(new ECTag(ECCodes.EC_TAG_CATEGORY_COLOR, ECTagTypes.EC_TAGTYPE_UINT8, 16711680L));
            noCatTag.addSubTag(new ECTag(ECCodes.EC_TAG_CATEGORY_COMMENT, ECTagTypes.EC_TAGTYPE_STRING, "Comment"));
            noCatTag.addSubTag(new ECTag(ECCodes.EC_TAG_CATEGORY_TITLE, ECTagTypes.EC_TAGTYPE_STRING, "Other"));
            
            catsTag.addSubTag(noCatTag);
            
            ECTag cat1Tag = new ECTag(ECCodes.EC_TAG_CATEGORY, ECTagTypes.EC_TAGTYPE_UINT8, 1);
            cat1Tag.addSubTag(new ECTag(ECCodes.EC_TAG_CATEGORY_PATH, ECTagTypes.EC_TAGTYPE_STRING, "/"));
            cat1Tag.addSubTag(new ECTag(ECCodes.EC_TAG_CATEGORY_PRIO, ECTagTypes.EC_TAGTYPE_UINT8, 0));
            cat1Tag.addSubTag(new ECTag(ECCodes.EC_TAG_CATEGORY_COLOR, ECTagTypes.EC_TAGTYPE_UINT8, 255));
            cat1Tag.addSubTag(new ECTag(ECCodes.EC_TAG_CATEGORY_COMMENT, ECTagTypes.EC_TAGTYPE_STRING, "Comment"));
            cat1Tag.addSubTag(new ECTag(ECCodes.EC_TAG_CATEGORY_TITLE, ECTagTypes.EC_TAGTYPE_STRING, "Cat 1"));
            
            catsTag.addSubTag(cat1Tag);

            
            result.addTag(catsTag);
            
        } catch (DataFormatException e) {
        }
        return result;
    }

    
    
    
    @Override
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
                return new ECStatsFake(epResp, detailLevel);
            } catch (ECTagParsingException e) {
                throw new ECPacketParsingException("Error parsing response to OP_STAT_REQ - " + e.getMessage(), epResp.getRawPacket(), e);
            }
        default:
            throw new ECPacketParsingException("Unexpected response to OP_STAT_REQ", epResp.getRawPacket());
        }
        
    }

    
}

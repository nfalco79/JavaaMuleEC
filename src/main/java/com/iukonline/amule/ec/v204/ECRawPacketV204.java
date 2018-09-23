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
import java.io.InputStream;

import com.iukonline.amule.ec.ECPacket;
import com.iukonline.amule.ec.ECRawPacket;
import com.iukonline.amule.ec.ECTag;
import com.iukonline.amule.ec.exceptions.ECPacketParsingException;

public class ECRawPacketV204 extends ECRawPacket {
    
    @Override
    protected ECRawTag getNewECRawTag(ECTag t, int startIndex) throws ECPacketParsingException {
        return new ECRawTagV204(t, startIndex);
    }
    
    @Override
    protected ECRawTag getNewECRawTag(int index)  {
        return new ECRawTagV204(index);
    }

    
    public ECRawPacketV204 (InputStream is) throws IOException, ECPacketParsingException {
        super(is);
    }
    
    public ECRawPacketV204(ECPacket p) throws ECPacketParsingException {
        super(p);
    }

    // accepts is not used anymore
    @Override
    public void setHasAccepts(boolean hasAccepts) { return; }

    @Override
    public void setAcceptsUTF8(boolean acceptsUTF8) { return; }

    @Override
    public void setAcceptsZlib(boolean acceptsZlib) { return; }
    
   
    @Override
    protected String getOpCodeString() {
        switch(rawPayload[0]) {
        case ECCodesV204.EC_OP_NOOP: return "EC_OP_NOOP";
        case ECCodesV204.EC_OP_AUTH_REQ: return "EC_OP_AUTH_REQ";
        case ECCodesV204.EC_OP_AUTH_FAIL: return "EC_OP_AUTH_FAIL";
        case ECCodesV204.EC_OP_AUTH_OK: return "EC_OP_AUTH_OK";
        case ECCodesV204.EC_OP_FAILED: return "EC_OP_FAILED";
        case ECCodesV204.EC_OP_STRINGS: return "EC_OP_STRINGS";
        case ECCodesV204.EC_OP_MISC_DATA: return "EC_OP_MISC_DATA";
        case ECCodesV204.EC_OP_SHUTDOWN: return "EC_OP_SHUTDOWN";
        case ECCodesV204.EC_OP_ADD_LINK: return "EC_OP_ADD_LINK";
        case ECCodesV204.EC_OP_STAT_REQ: return "EC_OP_STAT_REQ";
        case ECCodesV204.EC_OP_GET_CONNSTATE: return "EC_OP_GET_CONNSTATE";
        case ECCodesV204.EC_OP_STATS: return "EC_OP_STATS";
        case ECCodesV204.EC_OP_GET_DLOAD_QUEUE: return "EC_OP_GET_DLOAD_QUEUE";
        case ECCodesV204.EC_OP_GET_ULOAD_QUEUE: return "EC_OP_GET_ULOAD_QUEUE";
        case ECCodesV204.EC_OP_GET_SHARED_FILES: return "EC_OP_GET_SHARED_FILES";
        case ECCodesV204.EC_OP_SHARED_SET_PRIO: return "EC_OP_SHARED_SET_PRIO";
        case ECCodesV204.EC_OP_PARTFILE_REMOVE_NO_NEEDED: return "EC_OP_PARTFILE_REMOVE_NO_NEEDED";
        case ECCodesV204.EC_OP_PARTFILE_REMOVE_FULL_QUEUE: return "EC_OP_PARTFILE_REMOVE_FULL_QUEUE";
        case ECCodesV204.EC_OP_PARTFILE_REMOVE_HIGH_QUEUE: return "EC_OP_PARTFILE_REMOVE_HIGH_QUEUE";
        case ECCodesV204.EC_OP_PARTFILE_UNUSED: return "EC_OP_PARTFILE_UNUSED";
        case ECCodesV204.EC_OP_PARTFILE_SWAP_A4AF_THIS: return "EC_OP_PARTFILE_SWAP_A4AF_THIS";
        case ECCodesV204.EC_OP_PARTFILE_SWAP_A4AF_THIS_AUTO: return "EC_OP_PARTFILE_SWAP_A4AF_THIS_AUTO";
        case ECCodesV204.EC_OP_PARTFILE_SWAP_A4AF_OTHERS: return "EC_OP_PARTFILE_SWAP_A4AF_OTHERS";
        case ECCodesV204.EC_OP_PARTFILE_PAUSE: return "EC_OP_PARTFILE_PAUSE";
        case ECCodesV204.EC_OP_PARTFILE_RESUME: return "EC_OP_PARTFILE_RESUME";
        case ECCodesV204.EC_OP_PARTFILE_STOP: return "EC_OP_PARTFILE_STOP";
        case ECCodesV204.EC_OP_PARTFILE_PRIO_SET: return "EC_OP_PARTFILE_PRIO_SET";
        case ECCodesV204.EC_OP_PARTFILE_DELETE: return "EC_OP_PARTFILE_DELETE";
        case ECCodesV204.EC_OP_PARTFILE_SET_CAT: return "EC_OP_PARTFILE_SET_CAT";
        case ECCodesV204.EC_OP_DLOAD_QUEUE: return "EC_OP_DLOAD_QUEUE";
        case ECCodesV204.EC_OP_ULOAD_QUEUE: return "EC_OP_ULOAD_QUEUE";
        case ECCodesV204.EC_OP_SHARED_FILES: return "EC_OP_SHARED_FILES";
        case ECCodesV204.EC_OP_SHAREDFILES_RELOAD: return "EC_OP_SHAREDFILES_RELOAD";
        case ECCodesV204.EC_OP_RENAME_FILE: return "EC_OP_RENAME_FILE";
        case ECCodesV204.EC_OP_SEARCH_START: return "EC_OP_SEARCH_START";
        case ECCodesV204.EC_OP_SEARCH_STOP: return "EC_OP_SEARCH_STOP";
        case ECCodesV204.EC_OP_SEARCH_RESULTS: return "EC_OP_SEARCH_RESULTS";
        case ECCodesV204.EC_OP_SEARCH_PROGRESS: return "EC_OP_SEARCH_PROGRESS";
        case ECCodesV204.EC_OP_DOWNLOAD_SEARCH_RESULT: return "EC_OP_DOWNLOAD_SEARCH_RESULT";
        case ECCodesV204.EC_OP_IPFILTER_RELOAD: return "EC_OP_IPFILTER_RELOAD";
        case ECCodesV204.EC_OP_GET_SERVER_LIST: return "EC_OP_GET_SERVER_LIST";
        case ECCodesV204.EC_OP_SERVER_LIST: return "EC_OP_SERVER_LIST";
        case ECCodesV204.EC_OP_SERVER_DISCONNECT: return "EC_OP_SERVER_DISCONNECT";
        case ECCodesV204.EC_OP_SERVER_CONNECT: return "EC_OP_SERVER_CONNECT";
        case ECCodesV204.EC_OP_SERVER_REMOVE: return "EC_OP_SERVER_REMOVE";
        case ECCodesV204.EC_OP_SERVER_ADD: return "EC_OP_SERVER_ADD";
        case ECCodesV204.EC_OP_SERVER_UPDATE_FROM_URL: return "EC_OP_SERVER_UPDATE_FROM_URL";
        case ECCodesV204.EC_OP_ADDLOGLINE: return "EC_OP_ADDLOGLINE";
        case ECCodesV204.EC_OP_ADDDEBUGLOGLINE: return "EC_OP_ADDDEBUGLOGLINE";
        case ECCodesV204.EC_OP_GET_LOG: return "EC_OP_GET_LOG";
        case ECCodesV204.EC_OP_GET_DEBUGLOG: return "EC_OP_GET_DEBUGLOG";
        case ECCodesV204.EC_OP_GET_SERVERINFO: return "EC_OP_GET_SERVERINFO";
        case ECCodesV204.EC_OP_LOG: return "EC_OP_LOG";
        case ECCodesV204.EC_OP_DEBUGLOG: return "EC_OP_DEBUGLOG";
        case ECCodesV204.EC_OP_SERVERINFO: return "EC_OP_SERVERINFO";
        case ECCodesV204.EC_OP_RESET_LOG: return "EC_OP_RESET_LOG";
        case ECCodesV204.EC_OP_RESET_DEBUGLOG: return "EC_OP_RESET_DEBUGLOG";
        case ECCodesV204.EC_OP_CLEAR_SERVERINFO: return "EC_OP_CLEAR_SERVERINFO";
        case ECCodesV204.EC_OP_GET_LAST_LOG_ENTRY: return "EC_OP_GET_LAST_LOG_ENTRY";
        case ECCodesV204.EC_OP_GET_PREFERENCES: return "EC_OP_GET_PREFERENCES";
        case ECCodesV204.EC_OP_SET_PREFERENCES: return "EC_OP_SET_PREFERENCES";
        case ECCodesV204.EC_OP_CREATE_CATEGORY: return "EC_OP_CREATE_CATEGORY";
        case ECCodesV204.EC_OP_UPDATE_CATEGORY: return "EC_OP_UPDATE_CATEGORY";
        case ECCodesV204.EC_OP_DELETE_CATEGORY: return "EC_OP_DELETE_CATEGORY";
        case ECCodesV204.EC_OP_GET_STATSGRAPHS: return "EC_OP_GET_STATSGRAPHS";
        case ECCodesV204.EC_OP_STATSGRAPHS: return "EC_OP_STATSGRAPHS";
        case ECCodesV204.EC_OP_GET_STATSTREE: return "EC_OP_GET_STATSTREE";
        case ECCodesV204.EC_OP_STATSTREE: return "EC_OP_STATSTREE";
        case ECCodesV204.EC_OP_KAD_START: return "EC_OP_KAD_START";
        case ECCodesV204.EC_OP_KAD_STOP: return "EC_OP_KAD_STOP";
        case ECCodesV204.EC_OP_CONNECT: return "EC_OP_CONNECT";
        case ECCodesV204.EC_OP_DISCONNECT: return "EC_OP_DISCONNECT";
        case ECCodesV204.EC_OP_KAD_UPDATE_FROM_URL: return "EC_OP_KAD_UPDATE_FROM_URL";
        case ECCodesV204.EC_OP_KAD_BOOTSTRAP_FROM_IP: return "EC_OP_KAD_BOOTSTRAP_FROM_IP";
        case ECCodesV204.EC_OP_AUTH_SALT: return "EC_OP_AUTH_SALT";
        case ECCodesV204.EC_OP_AUTH_PASSWD: return "EC_OP_AUTH_PASSWD";
        case ECCodesV204.EC_OP_IPFILTER_UPDATE: return "EC_OP_IPFILTER_UPDATE";
        case ECCodesV204.EC_OP_GET_UPDATE: return "EC_OP_GET_UPDATE";
        case ECCodesV204.EC_OP_CLEAR_COMPLETED: return "EC_OP_CLEAR_COMPLETED";
        case ECCodesV204.EC_OP_CLIENT_SWAP_TO_ANOTHER_FILE: return "EC_OP_CLIENT_SWAP_TO_ANOTHER_FILE";
        case ECCodesV204.EC_OP_SHARED_FILE_SET_COMMENT: return "EC_OP_SHARED_FILE_SET_COMMENT";
        case ECCodesV204.EC_OP_SERVER_SET_STATIC_PRIO: return "EC_OP_SERVER_SET_STATIC_PRIO";
        case ECCodesV204.EC_OP_FRIEND: return "EC_OP_FRIEND";
        default: return "UNKNOWN";
        }
    }
    
    protected class ECRawTagV204 extends ECRawPacket.ECRawTag {
        
        public ECRawTagV204(int index) {
            super(index);
        }
        
        public ECRawTagV204(ECTag t, int startIndex) throws ECPacketParsingException {
            super(t, startIndex);
        }
        
        @Override
        protected String getTagNameString() {
            try {
                switch(getTagName()) {
                case ECCodesV204.EC_TAG_STRING: return "EC_TAG_STRING";
                case ECCodesV204.EC_TAG_PASSWD_HASH: return "EC_TAG_PASSWD_HASH";
                case ECCodesV204.EC_TAG_PROTOCOL_VERSION: return "EC_TAG_PROTOCOL_VERSION";
                case ECCodesV204.EC_TAG_VERSION_ID: return "EC_TAG_VERSION_ID";
                case ECCodesV204.EC_TAG_DETAIL_LEVEL: return "EC_TAG_DETAIL_LEVEL";
                case ECCodesV204.EC_TAG_CONNSTATE: return "EC_TAG_CONNSTATE";
                case ECCodesV204.EC_TAG_ED2K_ID: return "EC_TAG_ED2K_ID";
                case ECCodesV204.EC_TAG_LOG_TO_STATUS: return "EC_TAG_LOG_TO_STATUS";
                case ECCodesV204.EC_TAG_BOOTSTRAP_IP: return "EC_TAG_BOOTSTRAP_IP";
                case ECCodesV204.EC_TAG_BOOTSTRAP_PORT: return "EC_TAG_BOOTSTRAP_PORT";
                case ECCodesV204.EC_TAG_CLIENT_ID: return "EC_TAG_CLIENT_ID";
                case ECCodesV204.EC_TAG_PASSWD_SALT: return "EC_TAG_PASSWD_SALT";
                case ECCodesV204.EC_TAG_CAN_ZLIB: return "EC_TAG_CAN_ZLIB";
                case ECCodesV204.EC_TAG_CAN_UTF8_NUMBERS: return "EC_TAG_CAN_UTF8_NUMBERS";
                case ECCodesV204.EC_TAG_CAN_NOTIFY: return "EC_TAG_CAN_NOTIFY";
                case ECCodesV204.EC_TAG_ECID: return "EC_TAG_ECID";
                case ECCodesV204.EC_TAG_CLIENT_NAME: return "EC_TAG_CLIENT_NAME";
                case ECCodesV204.EC_TAG_CLIENT_VERSION: return "EC_TAG_CLIENT_VERSION";
                case ECCodesV204.EC_TAG_CLIENT_MOD: return "EC_TAG_CLIENT_MOD";
                case ECCodesV204.EC_TAG_STATS_UL_SPEED: return "EC_TAG_STATS_UL_SPEED";
                case ECCodesV204.EC_TAG_STATS_DL_SPEED: return "EC_TAG_STATS_DL_SPEED";
                case ECCodesV204.EC_TAG_STATS_UL_SPEED_LIMIT: return "EC_TAG_STATS_UL_SPEED_LIMIT";
                case ECCodesV204.EC_TAG_STATS_DL_SPEED_LIMIT: return "EC_TAG_STATS_DL_SPEED_LIMIT";
                case ECCodesV204.EC_TAG_STATS_UP_OVERHEAD: return "EC_TAG_STATS_UP_OVERHEAD";
                case ECCodesV204.EC_TAG_STATS_DOWN_OVERHEAD: return "EC_TAG_STATS_DOWN_OVERHEAD";
                case ECCodesV204.EC_TAG_STATS_TOTAL_SRC_COUNT: return "EC_TAG_STATS_TOTAL_SRC_COUNT";
                case ECCodesV204.EC_TAG_STATS_BANNED_COUNT: return "EC_TAG_STATS_BANNED_COUNT";
                case ECCodesV204.EC_TAG_STATS_UL_QUEUE_LEN: return "EC_TAG_STATS_UL_QUEUE_LEN";
                case ECCodesV204.EC_TAG_STATS_ED2K_USERS: return "EC_TAG_STATS_ED2K_USERS";
                case ECCodesV204.EC_TAG_STATS_KAD_USERS: return "EC_TAG_STATS_KAD_USERS";
                case ECCodesV204.EC_TAG_STATS_ED2K_FILES: return "EC_TAG_STATS_ED2K_FILES";
                case ECCodesV204.EC_TAG_STATS_KAD_FILES: return "EC_TAG_STATS_KAD_FILES";
                case ECCodesV204.EC_TAG_STATS_LOGGER_MESSAGE: return "EC_TAG_STATS_LOGGER_MESSAGE";
                case ECCodesV204.EC_TAG_STATS_KAD_FIREWALLED_UDP: return "EC_TAG_STATS_KAD_FIREWALLED_UDP";
                case ECCodesV204.EC_TAG_STATS_KAD_INDEXED_SOURCES: return "EC_TAG_STATS_KAD_INDEXED_SOURCES";
                case ECCodesV204.EC_TAG_STATS_KAD_INDEXED_KEYWORDS: return "EC_TAG_STATS_KAD_INDEXED_KEYWORDS";
                case ECCodesV204.EC_TAG_STATS_KAD_INDEXED_NOTES: return "EC_TAG_STATS_KAD_INDEXED_NOTES";
                case ECCodesV204.EC_TAG_STATS_KAD_INDEXED_LOAD: return "EC_TAG_STATS_KAD_INDEXED_LOAD";
                case ECCodesV204.EC_TAG_STATS_KAD_IP_ADRESS: return "EC_TAG_STATS_KAD_IP_ADRESS";
                case ECCodesV204.EC_TAG_STATS_BUDDY_STATUS: return "EC_TAG_STATS_BUDDY_STATUS";
                case ECCodesV204.EC_TAG_STATS_BUDDY_IP: return "EC_TAG_STATS_BUDDY_IP";
                case ECCodesV204.EC_TAG_STATS_BUDDY_PORT: return "EC_TAG_STATS_BUDDY_PORT";
                case ECCodesV204.EC_TAG_STATS_KAD_IN_LAN_MODE: return "EC_TAG_STATS_KAD_IN_LAN_MODE";
                case ECCodesV204.EC_TAG_STATS_TOTAL_SENT_BYTES: return "EC_TAG_STATS_TOTAL_SENT_BYTES";
                case ECCodesV204.EC_TAG_STATS_TOTAL_RECEIVED_BYTES: return "EC_TAG_STATS_TOTAL_RECEIVED_BYTES";
                case ECCodesV204.EC_TAG_STATS_SHARED_FILE_COUNT: return "EC_TAG_STATS_SHARED_FILE_COUNT";
                case ECCodesV204.EC_TAG_PARTFILE: return "EC_TAG_PARTFILE";
                case ECCodesV204.EC_TAG_PARTFILE_NAME: return "EC_TAG_PARTFILE_NAME";
                case ECCodesV204.EC_TAG_PARTFILE_PARTMETID: return "EC_TAG_PARTFILE_PARTMETID";
                case ECCodesV204.EC_TAG_PARTFILE_SIZE_FULL: return "EC_TAG_PARTFILE_SIZE_FULL";
                case ECCodesV204.EC_TAG_PARTFILE_SIZE_XFER: return "EC_TAG_PARTFILE_SIZE_XFER";
                case ECCodesV204.EC_TAG_PARTFILE_SIZE_XFER_UP: return "EC_TAG_PARTFILE_SIZE_XFER_UP";
                case ECCodesV204.EC_TAG_PARTFILE_SIZE_DONE: return "EC_TAG_PARTFILE_SIZE_DONE";
                case ECCodesV204.EC_TAG_PARTFILE_SPEED: return "EC_TAG_PARTFILE_SPEED";
                case ECCodesV204.EC_TAG_PARTFILE_STATUS: return "EC_TAG_PARTFILE_STATUS";
                case ECCodesV204.EC_TAG_PARTFILE_PRIO: return "EC_TAG_PARTFILE_PRIO";
                case ECCodesV204.EC_TAG_PARTFILE_SOURCE_COUNT: return "EC_TAG_PARTFILE_SOURCE_COUNT";
                case ECCodesV204.EC_TAG_PARTFILE_SOURCE_COUNT_A4AF: return "EC_TAG_PARTFILE_SOURCE_COUNT_A4AF";
                case ECCodesV204.EC_TAG_PARTFILE_SOURCE_COUNT_NOT_CURRENT: return "EC_TAG_PARTFILE_SOURCE_COUNT_NOT_CURRENT";
                case ECCodesV204.EC_TAG_PARTFILE_SOURCE_COUNT_XFER: return "EC_TAG_PARTFILE_SOURCE_COUNT_XFER";
                case ECCodesV204.EC_TAG_PARTFILE_ED2K_LINK: return "EC_TAG_PARTFILE_ED2K_LINK";
                case ECCodesV204.EC_TAG_PARTFILE_CAT: return "EC_TAG_PARTFILE_CAT";
                case ECCodesV204.EC_TAG_PARTFILE_LAST_RECV: return "EC_TAG_PARTFILE_LAST_RECV";
                case ECCodesV204.EC_TAG_PARTFILE_LAST_SEEN_COMP: return "EC_TAG_PARTFILE_LAST_SEEN_COMP";
                case ECCodesV204.EC_TAG_PARTFILE_PART_STATUS: return "EC_TAG_PARTFILE_PART_STATUS";
                case ECCodesV204.EC_TAG_PARTFILE_GAP_STATUS: return "EC_TAG_PARTFILE_GAP_STATUS";
                case ECCodesV204.EC_TAG_PARTFILE_REQ_STATUS: return "EC_TAG_PARTFILE_REQ_STATUS";
                case ECCodesV204.EC_TAG_PARTFILE_SOURCE_NAMES: return "EC_TAG_PARTFILE_SOURCE_NAMES";
                case ECCodesV204.EC_TAG_PARTFILE_COMMENTS: return "EC_TAG_PARTFILE_COMMENTS";
                case ECCodesV204.EC_TAG_PARTFILE_STOPPED: return "EC_TAG_PARTFILE_STOPPED";
                case ECCodesV204.EC_TAG_PARTFILE_DOWNLOAD_ACTIVE: return "EC_TAG_PARTFILE_DOWNLOAD_ACTIVE";
                case ECCodesV204.EC_TAG_PARTFILE_LOST_CORRUPTION: return "EC_TAG_PARTFILE_LOST_CORRUPTION";
                case ECCodesV204.EC_TAG_PARTFILE_GAINED_COMPRESSION: return "EC_TAG_PARTFILE_GAINED_COMPRESSION";
                case ECCodesV204.EC_TAG_PARTFILE_SAVED_ICH: return "EC_TAG_PARTFILE_SAVED_ICH";
                case ECCodesV204.EC_TAG_PARTFILE_SOURCE_NAMES_COUNTS: return "EC_TAG_PARTFILE_SOURCE_NAMES_COUNTS";
                case ECCodesV204.EC_TAG_PARTFILE_AVAILABLE_PARTS: return "EC_TAG_PARTFILE_AVAILABLE_PARTS";
                case ECCodesV204.EC_TAG_PARTFILE_HASH: return "EC_TAG_PARTFILE_HASH";
                case ECCodesV204.EC_TAG_PARTFILE_SHARED: return "EC_TAG_PARTFILE_SHARED";
                case ECCodesV204.EC_TAG_PARTFILE_HASHED_PART_COUNT: return "EC_TAG_PARTFILE_HASHED_PART_COUNT";
                case ECCodesV204.EC_TAG_PARTFILE_A4AFAUTO: return "EC_TAG_PARTFILE_A4AFAUTO";
                case ECCodesV204.EC_TAG_PARTFILE_A4AF_SOURCES: return "EC_TAG_PARTFILE_A4AF_SOURCES";
                case ECCodesV204.EC_TAG_KNOWNFILE: return "EC_TAG_KNOWNFILE";
                case ECCodesV204.EC_TAG_KNOWNFILE_XFERRED: return "EC_TAG_KNOWNFILE_XFERRED";
                case ECCodesV204.EC_TAG_KNOWNFILE_XFERRED_ALL: return "EC_TAG_KNOWNFILE_XFERRED_ALL";
                case ECCodesV204.EC_TAG_KNOWNFILE_REQ_COUNT: return "EC_TAG_KNOWNFILE_REQ_COUNT";
                case ECCodesV204.EC_TAG_KNOWNFILE_REQ_COUNT_ALL: return "EC_TAG_KNOWNFILE_REQ_COUNT_ALL";
                case ECCodesV204.EC_TAG_KNOWNFILE_ACCEPT_COUNT: return "EC_TAG_KNOWNFILE_ACCEPT_COUNT";
                case ECCodesV204.EC_TAG_KNOWNFILE_ACCEPT_COUNT_ALL: return "EC_TAG_KNOWNFILE_ACCEPT_COUNT_ALL";
                case ECCodesV204.EC_TAG_KNOWNFILE_AICH_MASTERHASH: return "EC_TAG_KNOWNFILE_AICH_MASTERHASH";
                case ECCodesV204.EC_TAG_KNOWNFILE_FILENAME: return "EC_TAG_KNOWNFILE_FILENAME";
                case ECCodesV204.EC_TAG_KNOWNFILE_COMPLETE_SOURCES_LOW: return "EC_TAG_KNOWNFILE_COMPLETE_SOURCES_LOW";
                case ECCodesV204.EC_TAG_KNOWNFILE_COMPLETE_SOURCES_HIGH: return "EC_TAG_KNOWNFILE_COMPLETE_SOURCES_HIGH";
                case ECCodesV204.EC_TAG_KNOWNFILE_PRIO: return "EC_TAG_KNOWNFILE_PRIO";
                case ECCodesV204.EC_TAG_KNOWNFILE_ON_QUEUE: return "EC_TAG_KNOWNFILE_ON_QUEUE";
                case ECCodesV204.EC_TAG_KNOWNFILE_COMPLETE_SOURCES: return "EC_TAG_KNOWNFILE_COMPLETE_SOURCES";
                case ECCodesV204.EC_TAG_KNOWNFILE_COMMENT: return "EC_TAG_KNOWNFILE_COMMENT";
                case ECCodesV204.EC_TAG_KNOWNFILE_RATING: return "EC_TAG_KNOWNFILE_RATING";
                case ECCodesV204.EC_TAG_SERVER: return "EC_TAG_SERVER";
                case ECCodesV204.EC_TAG_SERVER_NAME: return "EC_TAG_SERVER_NAME";
                case ECCodesV204.EC_TAG_SERVER_DESC: return "EC_TAG_SERVER_DESC";
                case ECCodesV204.EC_TAG_SERVER_ADDRESS: return "EC_TAG_SERVER_ADDRESS";
                case ECCodesV204.EC_TAG_SERVER_PING: return "EC_TAG_SERVER_PING";
                case ECCodesV204.EC_TAG_SERVER_USERS: return "EC_TAG_SERVER_USERS";
                case ECCodesV204.EC_TAG_SERVER_USERS_MAX: return "EC_TAG_SERVER_USERS_MAX";
                case ECCodesV204.EC_TAG_SERVER_FILES: return "EC_TAG_SERVER_FILES";
                case ECCodesV204.EC_TAG_SERVER_PRIO: return "EC_TAG_SERVER_PRIO";
                case ECCodesV204.EC_TAG_SERVER_FAILED: return "EC_TAG_SERVER_FAILED";
                case ECCodesV204.EC_TAG_SERVER_STATIC: return "EC_TAG_SERVER_STATIC";
                case ECCodesV204.EC_TAG_SERVER_VERSION: return "EC_TAG_SERVER_VERSION";
                case ECCodesV204.EC_TAG_SERVER_IP: return "EC_TAG_SERVER_IP";
                case ECCodesV204.EC_TAG_SERVER_PORT: return "EC_TAG_SERVER_PORT";
                case ECCodesV204.EC_TAG_CLIENT: return "EC_TAG_CLIENT";
                case ECCodesV204.EC_TAG_CLIENT_SOFTWARE: return "EC_TAG_CLIENT_SOFTWARE";
                case ECCodesV204.EC_TAG_CLIENT_SCORE: return "EC_TAG_CLIENT_SCORE";
                case ECCodesV204.EC_TAG_CLIENT_HASH: return "EC_TAG_CLIENT_HASH";
                case ECCodesV204.EC_TAG_CLIENT_FRIEND_SLOT: return "EC_TAG_CLIENT_FRIEND_SLOT";
                case ECCodesV204.EC_TAG_CLIENT_WAIT_TIME: return "EC_TAG_CLIENT_WAIT_TIME";
                case ECCodesV204.EC_TAG_CLIENT_XFER_TIME: return "EC_TAG_CLIENT_XFER_TIME";
                case ECCodesV204.EC_TAG_CLIENT_QUEUE_TIME: return "EC_TAG_CLIENT_QUEUE_TIME";
                case ECCodesV204.EC_TAG_CLIENT_LAST_TIME: return "EC_TAG_CLIENT_LAST_TIME";
                case ECCodesV204.EC_TAG_CLIENT_UPLOAD_SESSION: return "EC_TAG_CLIENT_UPLOAD_SESSION";
                case ECCodesV204.EC_TAG_CLIENT_UPLOAD_TOTAL: return "EC_TAG_CLIENT_UPLOAD_TOTAL";
                case ECCodesV204.EC_TAG_CLIENT_DOWNLOAD_TOTAL: return "EC_TAG_CLIENT_DOWNLOAD_TOTAL";
                case ECCodesV204.EC_TAG_CLIENT_DOWNLOAD_STATE: return "EC_TAG_CLIENT_DOWNLOAD_STATE";
                case ECCodesV204.EC_TAG_CLIENT_UP_SPEED: return "EC_TAG_CLIENT_UP_SPEED";
                case ECCodesV204.EC_TAG_CLIENT_DOWN_SPEED: return "EC_TAG_CLIENT_DOWN_SPEED";
                case ECCodesV204.EC_TAG_CLIENT_FROM: return "EC_TAG_CLIENT_FROM";
                case ECCodesV204.EC_TAG_CLIENT_USER_IP: return "EC_TAG_CLIENT_USER_IP";
                case ECCodesV204.EC_TAG_CLIENT_USER_PORT: return "EC_TAG_CLIENT_USER_PORT";
                case ECCodesV204.EC_TAG_CLIENT_SERVER_IP: return "EC_TAG_CLIENT_SERVER_IP";
                case ECCodesV204.EC_TAG_CLIENT_SERVER_PORT: return "EC_TAG_CLIENT_SERVER_PORT";
                case ECCodesV204.EC_TAG_CLIENT_SERVER_NAME: return "EC_TAG_CLIENT_SERVER_NAME";
                case ECCodesV204.EC_TAG_CLIENT_SOFT_VER_STR: return "EC_TAG_CLIENT_SOFT_VER_STR";
                case ECCodesV204.EC_TAG_CLIENT_WAITING_POSITION: return "EC_TAG_CLIENT_WAITING_POSITION";
                case ECCodesV204.EC_TAG_CLIENT_IDENT_STATE: return "EC_TAG_CLIENT_IDENT_STATE";
                case ECCodesV204.EC_TAG_CLIENT_OBFUSCATION_STATUS: return "EC_TAG_CLIENT_OBFUSCATION_STATUS";
                case ECCodesV204.EC_TAG_CLIENT_CURRENTLYUNUSED1: return "EC_TAG_CLIENT_CURRENTLYUNUSED1";
                case ECCodesV204.EC_TAG_CLIENT_REMOTE_QUEUE_RANK: return "EC_TAG_CLIENT_REMOTE_QUEUE_RANK";
                case ECCodesV204.EC_TAG_CLIENT_DISABLE_VIEW_SHARED: return "EC_TAG_CLIENT_DISABLE_VIEW_SHARED";
                case ECCodesV204.EC_TAG_CLIENT_UPLOAD_STATE: return "EC_TAG_CLIENT_UPLOAD_STATE";
                case ECCodesV204.EC_TAG_CLIENT_EXT_PROTOCOL: return "EC_TAG_CLIENT_EXT_PROTOCOL";
                case ECCodesV204.EC_TAG_CLIENT_USER_ID: return "EC_TAG_CLIENT_USER_ID";
                case ECCodesV204.EC_TAG_CLIENT_UPLOAD_FILE: return "EC_TAG_CLIENT_UPLOAD_FILE";
                case ECCodesV204.EC_TAG_CLIENT_REQUEST_FILE: return "EC_TAG_CLIENT_REQUEST_FILE";
                case ECCodesV204.EC_TAG_CLIENT_A4AF_FILES: return "EC_TAG_CLIENT_A4AF_FILES";
                case ECCodesV204.EC_TAG_CLIENT_OLD_REMOTE_QUEUE_RANK: return "EC_TAG_CLIENT_OLD_REMOTE_QUEUE_RANK";
                case ECCodesV204.EC_TAG_CLIENT_KAD_PORT: return "EC_TAG_CLIENT_KAD_PORT";
                case ECCodesV204.EC_TAG_CLIENT_PART_STATUS: return "EC_TAG_CLIENT_PART_STATUS";
                case ECCodesV204.EC_TAG_CLIENT_NEXT_REQUESTED_PART: return "EC_TAG_CLIENT_NEXT_REQUESTED_PART";
                case ECCodesV204.EC_TAG_CLIENT_LAST_DOWNLOADING_PART: return "EC_TAG_CLIENT_LAST_DOWNLOADING_PART";
                case ECCodesV204.EC_TAG_CLIENT_REMOTE_FILENAME: return "EC_TAG_CLIENT_REMOTE_FILENAME";
                case ECCodesV204.EC_TAG_CLIENT_MOD_VERSION: return "EC_TAG_CLIENT_MOD_VERSION";
                case ECCodesV204.EC_TAG_CLIENT_OS_INFO: return "EC_TAG_CLIENT_OS_INFO";
                case ECCodesV204.EC_TAG_CLIENT_AVAILABLE_PARTS: return "EC_TAG_CLIENT_AVAILABLE_PARTS";
                case ECCodesV204.EC_TAG_CLIENT_UPLOAD_PART_STATUS: return "EC_TAG_CLIENT_UPLOAD_PART_STATUS";
                case ECCodesV204.EC_TAG_SEARCHFILE: return "EC_TAG_SEARCHFILE";
                case ECCodesV204.EC_TAG_SEARCH_TYPE: return "EC_TAG_SEARCH_TYPE";
                case ECCodesV204.EC_TAG_SEARCH_NAME: return "EC_TAG_SEARCH_NAME";
                case ECCodesV204.EC_TAG_SEARCH_MIN_SIZE: return "EC_TAG_SEARCH_MIN_SIZE";
                case ECCodesV204.EC_TAG_SEARCH_MAX_SIZE: return "EC_TAG_SEARCH_MAX_SIZE";
                case ECCodesV204.EC_TAG_SEARCH_FILE_TYPE: return "EC_TAG_SEARCH_FILE_TYPE";
                case ECCodesV204.EC_TAG_SEARCH_EXTENSION: return "EC_TAG_SEARCH_EXTENSION";
                case ECCodesV204.EC_TAG_SEARCH_AVAILABILITY: return "EC_TAG_SEARCH_AVAILABILITY";
                case ECCodesV204.EC_TAG_SEARCH_STATUS: return "EC_TAG_SEARCH_STATUS";
                case ECCodesV204.EC_TAG_SEARCH_PARENT: return "EC_TAG_SEARCH_PARENT";
                case ECCodesV204.EC_TAG_FRIEND: return "EC_TAG_FRIEND";
                case ECCodesV204.EC_TAG_FRIEND_NAME: return "EC_TAG_FRIEND_NAME";
                case ECCodesV204.EC_TAG_FRIEND_HASH: return "EC_TAG_FRIEND_HASH";
                case ECCodesV204.EC_TAG_FRIEND_IP: return "EC_TAG_FRIEND_IP";
                case ECCodesV204.EC_TAG_FRIEND_PORT: return "EC_TAG_FRIEND_PORT";
                case ECCodesV204.EC_TAG_FRIEND_CLIENT: return "EC_TAG_FRIEND_CLIENT";
                case ECCodesV204.EC_TAG_FRIEND_ADD: return "EC_TAG_FRIEND_ADD";
                case ECCodesV204.EC_TAG_FRIEND_REMOVE: return "EC_TAG_FRIEND_REMOVE";
                case ECCodesV204.EC_TAG_FRIEND_FRIENDSLOT: return "EC_TAG_FRIEND_FRIENDSLOT";
                case ECCodesV204.EC_TAG_FRIEND_SHARED: return "EC_TAG_FRIEND_SHARED";
                case ECCodesV204.EC_TAG_SELECT_PREFS: return "EC_TAG_SELECT_PREFS";
                case ECCodesV204.EC_TAG_PREFS_CATEGORIES: return "EC_TAG_PREFS_CATEGORIES";
                case ECCodesV204.EC_TAG_CATEGORY: return "EC_TAG_CATEGORY";
                case ECCodesV204.EC_TAG_CATEGORY_TITLE: return "EC_TAG_CATEGORY_TITLE";
                case ECCodesV204.EC_TAG_CATEGORY_PATH: return "EC_TAG_CATEGORY_PATH";
                case ECCodesV204.EC_TAG_CATEGORY_COMMENT: return "EC_TAG_CATEGORY_COMMENT";
                case ECCodesV204.EC_TAG_CATEGORY_COLOR: return "EC_TAG_CATEGORY_COLOR";
                case ECCodesV204.EC_TAG_CATEGORY_PRIO: return "EC_TAG_CATEGORY_PRIO";
                case ECCodesV204.EC_TAG_PREFS_GENERAL: return "EC_TAG_PREFS_GENERAL";
                case ECCodesV204.EC_TAG_USER_NICK: return "EC_TAG_USER_NICK";
                case ECCodesV204.EC_TAG_USER_HASH: return "EC_TAG_USER_HASH";
                case ECCodesV204.EC_TAG_USER_HOST: return "EC_TAG_USER_HOST";
                case ECCodesV204.EC_TAG_GENERAL_CHECK_NEW_VERSION: return "EC_TAG_GENERAL_CHECK_NEW_VERSION";
                case ECCodesV204.EC_TAG_PREFS_CONNECTIONS: return "EC_TAG_PREFS_CONNECTIONS";
                case ECCodesV204.EC_TAG_CONN_DL_CAP: return "EC_TAG_CONN_DL_CAP";
                case ECCodesV204.EC_TAG_CONN_UL_CAP: return "EC_TAG_CONN_UL_CAP";
                case ECCodesV204.EC_TAG_CONN_MAX_DL: return "EC_TAG_CONN_MAX_DL";
                case ECCodesV204.EC_TAG_CONN_MAX_UL: return "EC_TAG_CONN_MAX_UL";
                case ECCodesV204.EC_TAG_CONN_SLOT_ALLOCATION: return "EC_TAG_CONN_SLOT_ALLOCATION";
                case ECCodesV204.EC_TAG_CONN_TCP_PORT: return "EC_TAG_CONN_TCP_PORT";
                case ECCodesV204.EC_TAG_CONN_UDP_PORT: return "EC_TAG_CONN_UDP_PORT";
                case ECCodesV204.EC_TAG_CONN_UDP_DISABLE: return "EC_TAG_CONN_UDP_DISABLE";
                case ECCodesV204.EC_TAG_CONN_MAX_FILE_SOURCES: return "EC_TAG_CONN_MAX_FILE_SOURCES";
                case ECCodesV204.EC_TAG_CONN_MAX_CONN: return "EC_TAG_CONN_MAX_CONN";
                case ECCodesV204.EC_TAG_CONN_AUTOCONNECT: return "EC_TAG_CONN_AUTOCONNECT";
                case ECCodesV204.EC_TAG_CONN_RECONNECT: return "EC_TAG_CONN_RECONNECT";
                case ECCodesV204.EC_TAG_NETWORK_ED2K: return "EC_TAG_NETWORK_ED2K";
                case ECCodesV204.EC_TAG_NETWORK_KADEMLIA: return "EC_TAG_NETWORK_KADEMLIA";
                case ECCodesV204.EC_TAG_PREFS_MESSAGEFILTER: return "EC_TAG_PREFS_MESSAGEFILTER";
                case ECCodesV204.EC_TAG_MSGFILTER_ENABLED: return "EC_TAG_MSGFILTER_ENABLED";
                case ECCodesV204.EC_TAG_MSGFILTER_ALL: return "EC_TAG_MSGFILTER_ALL";
                case ECCodesV204.EC_TAG_MSGFILTER_FRIENDS: return "EC_TAG_MSGFILTER_FRIENDS";
                case ECCodesV204.EC_TAG_MSGFILTER_SECURE: return "EC_TAG_MSGFILTER_SECURE";
                case ECCodesV204.EC_TAG_MSGFILTER_BY_KEYWORD: return "EC_TAG_MSGFILTER_BY_KEYWORD";
                case ECCodesV204.EC_TAG_MSGFILTER_KEYWORDS: return "EC_TAG_MSGFILTER_KEYWORDS";
                case ECCodesV204.EC_TAG_PREFS_REMOTECTRL: return "EC_TAG_PREFS_REMOTECTRL";
                case ECCodesV204.EC_TAG_WEBSERVER_AUTORUN: return "EC_TAG_WEBSERVER_AUTORUN";
                case ECCodesV204.EC_TAG_WEBSERVER_PORT: return "EC_TAG_WEBSERVER_PORT";
                case ECCodesV204.EC_TAG_WEBSERVER_GUEST: return "EC_TAG_WEBSERVER_GUEST";
                case ECCodesV204.EC_TAG_WEBSERVER_USEGZIP: return "EC_TAG_WEBSERVER_USEGZIP";
                case ECCodesV204.EC_TAG_WEBSERVER_REFRESH: return "EC_TAG_WEBSERVER_REFRESH";
                case ECCodesV204.EC_TAG_WEBSERVER_TEMPLATE: return "EC_TAG_WEBSERVER_TEMPLATE";
                case ECCodesV204.EC_TAG_PREFS_ONLINESIG: return "EC_TAG_PREFS_ONLINESIG";
                case ECCodesV204.EC_TAG_ONLINESIG_ENABLED: return "EC_TAG_ONLINESIG_ENABLED";
                case ECCodesV204.EC_TAG_PREFS_SERVERS: return "EC_TAG_PREFS_SERVERS";
                case ECCodesV204.EC_TAG_SERVERS_REMOVE_DEAD: return "EC_TAG_SERVERS_REMOVE_DEAD";
                case ECCodesV204.EC_TAG_SERVERS_DEAD_SERVER_RETRIES: return "EC_TAG_SERVERS_DEAD_SERVER_RETRIES";
                case ECCodesV204.EC_TAG_SERVERS_AUTO_UPDATE: return "EC_TAG_SERVERS_AUTO_UPDATE";
                case ECCodesV204.EC_TAG_SERVERS_URL_LIST: return "EC_TAG_SERVERS_URL_LIST";
                case ECCodesV204.EC_TAG_SERVERS_ADD_FROM_SERVER: return "EC_TAG_SERVERS_ADD_FROM_SERVER";
                case ECCodesV204.EC_TAG_SERVERS_ADD_FROM_CLIENT: return "EC_TAG_SERVERS_ADD_FROM_CLIENT";
                case ECCodesV204.EC_TAG_SERVERS_USE_SCORE_SYSTEM: return "EC_TAG_SERVERS_USE_SCORE_SYSTEM";
                case ECCodesV204.EC_TAG_SERVERS_SMART_ID_CHECK: return "EC_TAG_SERVERS_SMART_ID_CHECK";
                case ECCodesV204.EC_TAG_SERVERS_SAFE_SERVER_CONNECT: return "EC_TAG_SERVERS_SAFE_SERVER_CONNECT";
                case ECCodesV204.EC_TAG_SERVERS_AUTOCONN_STATIC_ONLY: return "EC_TAG_SERVERS_AUTOCONN_STATIC_ONLY";
                case ECCodesV204.EC_TAG_SERVERS_MANUAL_HIGH_PRIO: return "EC_TAG_SERVERS_MANUAL_HIGH_PRIO";
                case ECCodesV204.EC_TAG_SERVERS_UPDATE_URL: return "EC_TAG_SERVERS_UPDATE_URL";
                case ECCodesV204.EC_TAG_PREFS_FILES: return "EC_TAG_PREFS_FILES";
                case ECCodesV204.EC_TAG_FILES_ICH_ENABLED: return "EC_TAG_FILES_ICH_ENABLED";
                case ECCodesV204.EC_TAG_FILES_AICH_TRUST: return "EC_TAG_FILES_AICH_TRUST";
                case ECCodesV204.EC_TAG_FILES_NEW_PAUSED: return "EC_TAG_FILES_NEW_PAUSED";
                case ECCodesV204.EC_TAG_FILES_NEW_AUTO_DL_PRIO: return "EC_TAG_FILES_NEW_AUTO_DL_PRIO";
                case ECCodesV204.EC_TAG_FILES_PREVIEW_PRIO: return "EC_TAG_FILES_PREVIEW_PRIO";
                case ECCodesV204.EC_TAG_FILES_NEW_AUTO_UL_PRIO: return "EC_TAG_FILES_NEW_AUTO_UL_PRIO";
                case ECCodesV204.EC_TAG_FILES_UL_FULL_CHUNKS: return "EC_TAG_FILES_UL_FULL_CHUNKS";
                case ECCodesV204.EC_TAG_FILES_START_NEXT_PAUSED: return "EC_TAG_FILES_START_NEXT_PAUSED";
                case ECCodesV204.EC_TAG_FILES_RESUME_SAME_CAT: return "EC_TAG_FILES_RESUME_SAME_CAT";
                case ECCodesV204.EC_TAG_FILES_SAVE_SOURCES: return "EC_TAG_FILES_SAVE_SOURCES";
                case ECCodesV204.EC_TAG_FILES_EXTRACT_METADATA: return "EC_TAG_FILES_EXTRACT_METADATA";
                case ECCodesV204.EC_TAG_FILES_ALLOC_FULL_SIZE: return "EC_TAG_FILES_ALLOC_FULL_SIZE";
                case ECCodesV204.EC_TAG_FILES_CHECK_FREE_SPACE: return "EC_TAG_FILES_CHECK_FREE_SPACE";
                case ECCodesV204.EC_TAG_FILES_MIN_FREE_SPACE: return "EC_TAG_FILES_MIN_FREE_SPACE";
                case ECCodesV204.EC_TAG_PREFS_SRCDROP: return "EC_TAG_PREFS_SRCDROP";
                case ECCodesV204.EC_TAG_SRCDROP_NONEEDED: return "EC_TAG_SRCDROP_NONEEDED";
                case ECCodesV204.EC_TAG_SRCDROP_DROP_FQS: return "EC_TAG_SRCDROP_DROP_FQS";
                case ECCodesV204.EC_TAG_SRCDROP_DROP_HQRS: return "EC_TAG_SRCDROP_DROP_HQRS";
                case ECCodesV204.EC_TAG_SRCDROP_HQRS_VALUE: return "EC_TAG_SRCDROP_HQRS_VALUE";
                case ECCodesV204.EC_TAG_SRCDROP_AUTODROP_TIMER: return "EC_TAG_SRCDROP_AUTODROP_TIMER";
                case ECCodesV204.EC_TAG_PREFS_DIRECTORIES: return "EC_TAG_PREFS_DIRECTORIES";
                case ECCodesV204.EC_TAG_DIRECTORIES_INCOMING: return "EC_TAG_DIRECTORIES_INCOMING";
                case ECCodesV204.EC_TAG_DIRECTORIES_TEMP: return "EC_TAG_DIRECTORIES_TEMP";
                case ECCodesV204.EC_TAG_DIRECTORIES_SHARED: return "EC_TAG_DIRECTORIES_SHARED";
                case ECCodesV204.EC_TAG_DIRECTORIES_SHARE_HIDDEN: return "EC_TAG_DIRECTORIES_SHARE_HIDDEN";
                case ECCodesV204.EC_TAG_PREFS_STATISTICS: return "EC_TAG_PREFS_STATISTICS";
                case ECCodesV204.EC_TAG_STATSGRAPH_WIDTH: return "EC_TAG_STATSGRAPH_WIDTH";
                case ECCodesV204.EC_TAG_STATSGRAPH_SCALE: return "EC_TAG_STATSGRAPH_SCALE";
                case ECCodesV204.EC_TAG_STATSGRAPH_LAST: return "EC_TAG_STATSGRAPH_LAST";
                case ECCodesV204.EC_TAG_STATSGRAPH_DATA: return "EC_TAG_STATSGRAPH_DATA";
                case ECCodesV204.EC_TAG_STATTREE_CAPPING: return "EC_TAG_STATTREE_CAPPING";
                case ECCodesV204.EC_TAG_STATTREE_NODE: return "EC_TAG_STATTREE_NODE";
                case ECCodesV204.EC_TAG_STAT_NODE_VALUE: return "EC_TAG_STAT_NODE_VALUE";
                case ECCodesV204.EC_TAG_STAT_VALUE_TYPE: return "EC_TAG_STAT_VALUE_TYPE";
                case ECCodesV204.EC_TAG_STATTREE_NODEID: return "EC_TAG_STATTREE_NODEID";
                case ECCodesV204.EC_TAG_PREFS_SECURITY: return "EC_TAG_PREFS_SECURITY";
                case ECCodesV204.EC_TAG_SECURITY_CAN_SEE_SHARES: return "EC_TAG_SECURITY_CAN_SEE_SHARES";
                case ECCodesV204.EC_TAG_IPFILTER_CLIENTS: return "EC_TAG_IPFILTER_CLIENTS";
                case ECCodesV204.EC_TAG_IPFILTER_SERVERS: return "EC_TAG_IPFILTER_SERVERS";
                case ECCodesV204.EC_TAG_IPFILTER_AUTO_UPDATE: return "EC_TAG_IPFILTER_AUTO_UPDATE";
                case ECCodesV204.EC_TAG_IPFILTER_UPDATE_URL: return "EC_TAG_IPFILTER_UPDATE_URL";
                case ECCodesV204.EC_TAG_IPFILTER_LEVEL: return "EC_TAG_IPFILTER_LEVEL";
                case ECCodesV204.EC_TAG_IPFILTER_FILTER_LAN: return "EC_TAG_IPFILTER_FILTER_LAN";
                case ECCodesV204.EC_TAG_SECURITY_USE_SECIDENT: return "EC_TAG_SECURITY_USE_SECIDENT";
                case ECCodesV204.EC_TAG_SECURITY_OBFUSCATION_SUPPORTED: return "EC_TAG_SECURITY_OBFUSCATION_SUPPORTED";
                case ECCodesV204.EC_TAG_SECURITY_OBFUSCATION_REQUESTED: return "EC_TAG_SECURITY_OBFUSCATION_REQUESTED";
                case ECCodesV204.EC_TAG_SECURITY_OBFUSCATION_REQUIRED: return "EC_TAG_SECURITY_OBFUSCATION_REQUIRED";
                case ECCodesV204.EC_TAG_PREFS_CORETWEAKS: return "EC_TAG_PREFS_CORETWEAKS";
                case ECCodesV204.EC_TAG_CORETW_MAX_CONN_PER_FIVE: return "EC_TAG_CORETW_MAX_CONN_PER_FIVE";
                case ECCodesV204.EC_TAG_CORETW_VERBOSE: return "EC_TAG_CORETW_VERBOSE";
                case ECCodesV204.EC_TAG_CORETW_FILEBUFFER: return "EC_TAG_CORETW_FILEBUFFER";
                case ECCodesV204.EC_TAG_CORETW_UL_QUEUE: return "EC_TAG_CORETW_UL_QUEUE";
                case ECCodesV204.EC_TAG_CORETW_SRV_KEEPALIVE_TIMEOUT: return "EC_TAG_CORETW_SRV_KEEPALIVE_TIMEOUT";
                case ECCodesV204.EC_TAG_PREFS_KADEMLIA: return "EC_TAG_PREFS_KADEMLIA";
                case ECCodesV204.EC_TAG_KADEMLIA_UPDATE_URL: return "EC_TAG_KADEMLIA_UPDATE_URL";
                default: return "UNKNOWN";
                }
            } catch (ECPacketParsingException e) {
                return "INVALID";
            }
        }
    }
}

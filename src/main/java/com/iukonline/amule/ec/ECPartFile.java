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
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.zip.DataFormatException;

import com.iukonline.amule.ec.exceptions.ECTagParsingException;



public class ECPartFile {
    
    public final static byte PS_READY                        = 0x0;
    public final static byte PS_EMPTY                        = 0x1;
    public final static byte PS_WAITINGFORHASH               = 0x2;
    public final static byte PS_HASHING                      = 0x3;
    public final static byte PS_ERROR                        = 0x4;
    public final static byte PS_INSUFFICIENT                 = 0x5;
    public final static byte PS_UNKNOWN                      = 0x6;
    public final static byte PS_PAUSED                       = 0x7;
    public final static byte PS_COMPLETING                   = 0x8;
    public final static byte PS_COMPLETE                     = 0x9;
    public final static byte PS_ALLOCATING                   = 0x10;
    
    public final static byte PR_LOW                          = 0x0;
    public final static byte PR_NORMAL                       = 0x1;
    public final static byte PR_HIGH                         = 0x2;
    public final static byte PR_AUTO                         = 0x5; 
    public final static byte PR_AUTO_LOW                     = 0xA;
    public final static byte PR_AUTO_NORMAL                  = 0xB;
    public final static byte PR_AUTO_HIGH                    = 0xC;
    
    public final static byte RATING_NOT_RATED                = 0x0;
    public final static byte RATING_INVALID                  = 0x1;
    public final static byte RATING_POOR                     = 0x2;
    public final static byte RATING_FAIR                     = 0x3;
    public final static byte RATING_GOOD                     = 0x4;
    public final static byte RATING_EXCELLENT                = 0x5;
    
    
    protected byte detailLevel;

    
    protected byte[] hash;           
    protected String fileName;        
    protected String ed2kLink;       
    protected byte status;      
    protected byte prio;        
    protected long cat;
    protected int sourceCount;  
    protected int metID;

    protected int sourceA4AF;   
    protected int sourceXfer;   
    protected int sourceNotCurrent;

    protected long sizeXfer;
    protected long sizeFull;       
    protected long sizeDone;       
    protected long speed;          
    
    protected Date lastSeenComp;
    protected Date lastRecv;
    
    protected byte[] partStatus;
    protected byte[] gapStatus;
    protected byte[] reqStatus;
    
    protected int commentCount;
    protected ArrayList<ECPartFileComment> comments;
    protected ArrayList<ECPartFileSourceName> sourceNames;
    

    public ECPartFile() {
        
        comments = new ArrayList<ECPartFileComment>();
        sourceNames = new ArrayList<ECPartFileSourceName>();

    }
    
    public ECPartFile(ECTag t, byte d) throws ECTagParsingException {
        fillFromTag(t, d);
    }
    
    protected byte[] getHashFromTag(ECTag pt) throws ECTagParsingException {
        try {
            return pt.getTagValueHash();
        } catch (DataFormatException e) {
            throw new ECTagParsingException("Unexpected tag type " + pt.getTagType(), e);        
        }
    }
    
    public void fillFromTag(ECTag pt, byte d) throws ECTagParsingException {
        
        detailLevel = d;
        
        if (pt.getTagName() != ECCodes.EC_TAG_PARTFILE) throw new ECTagParsingException("Unexpected tag name " + pt.getTagName());
        
        hash = getHashFromTag(pt);

        
        ECTag t;

        try {
            
            switch (detailLevel) {
            case ECCodes.EC_DETAIL_FULL:
            case ECCodes.EC_DETAIL_WEB:
            case ECCodes.EC_DETAIL_CMD:
                
                t = pt.getSubTagByName(ECCodes.EC_TAG_PARTFILE_STATUS);
                if (t != null) status = (byte) t.getTagValueUInt();
                else throw new ECTagParsingException("Missing EC_TAG_PARTFILE_STATUS in server response");
                
                t = pt.getSubTagByName(ECCodes.EC_TAG_PARTFILE_SOURCE_COUNT);
                if (t != null) sourceCount = (int) t.getTagValueUInt();
                else throw new ECTagParsingException("Missing EC_TAG_PARTFILE_SOURCE_COUNT in server response");

                t = pt.getSubTagByName(ECCodes.EC_TAG_PARTFILE_SOURCE_COUNT_NOT_CURRENT);
                if (t != null) sourceNotCurrent = (int) t.getTagValueUInt();
                else throw new ECTagParsingException("Missing EC_TAG_PARTFILE_SOURCE_COUNT_NOT_CURRENT in server response");
                
                t = pt.getSubTagByName(ECCodes.EC_TAG_PARTFILE_SOURCE_COUNT_XFER);
                if (t != null) sourceXfer = (int) t.getTagValueUInt();
                else throw new ECTagParsingException("Missing EC_TAG_PARTFILE_SOURCE_COUNT_XFER in server response");
                
                t = pt.getSubTagByName(ECCodes.EC_TAG_PARTFILE_SOURCE_COUNT_A4AF);
                if (t != null) sourceA4AF = (int) t.getTagValueUInt();
                else throw new ECTagParsingException("Missing EC_TAG_PARTFILE_SOURCE_COUNT_A4AF in server response");
                
                t = pt.getSubTagByName(ECCodes.EC_TAG_PARTFILE_SIZE_XFER);
                if (t != null) sizeXfer = t.getTagValueUInt();
                else throw new ECTagParsingException("Missing EC_TAG_PARTFILE_SIZE_XFER in server response");
                
                t = pt.getSubTagByName(ECCodes.EC_TAG_PARTFILE_SIZE_DONE);
                if (t != null) sizeDone = t.getTagValueUInt();
                else throw new ECTagParsingException("Missing EC_TAG_PARTFILE_SIZE_DONE in server response");
                
                t = pt.getSubTagByName(ECCodes.EC_TAG_PARTFILE_SIZE_FULL);
                if (t != null) sizeFull = t.getTagValueUInt();
                else throw new ECTagParsingException("Missing EC_TAG_PARTFILE_SIZE_FULL in server response");
                
                t = pt.getSubTagByName(ECCodes.EC_TAG_PARTFILE_SPEED);
                if (t != null) speed = t.getTagValueUInt();
                else throw new ECTagParsingException("Missing EC_TAG_PARTFILE_SPEED in server response");
                
                t = pt.getSubTagByName(ECCodes.EC_TAG_PARTFILE_PRIO);
                if (t != null) prio = (byte) t.getTagValueUInt();
                else throw new ECTagParsingException("Missing EC_TAG_PARTFILE_PRIO in server response");
                
                t = pt.getSubTagByName(ECCodes.EC_TAG_PARTFILE_CAT);
                if (t != null) cat = (int) t.getTagValueUInt();
                else throw new ECTagParsingException("Missing EC_TAG_PARTFILE_CAT in server response");
                
                t = pt.getSubTagByName(ECCodes.EC_TAG_PARTFILE_LAST_SEEN_COMP);
                if (t != null) lastSeenComp = new Date(t.getTagValueUInt() * 1000L); // LAST_SEEN_COMP is time_t in cpp (seconds), while Java uses milliseconds
                else throw new ECTagParsingException("Missing EC_TAG_PARTFILE_LAST_SEEN_COMP in server response");
                
                t = pt.getSubTagByName(ECCodes.EC_TAG_PARTFILE_NAME);
                if (t != null) fileName = t.getTagValueString();
                else throw new ECTagParsingException("Missing EC_TAG_PARTFILE_NAME in server response");           
                
                t = pt.getSubTagByName(ECCodes.EC_TAG_PARTFILE_PARTMETID);
                if (t != null) metID = (int) t.getTagValueUInt();
                else throw new ECTagParsingException("Missing EC_TAG_PARTFILE_PARTMETID in server response");     
                
                t = pt.getSubTagByName(ECCodes.EC_TAG_PARTFILE_ED2K_LINK);
                if (t != null) ed2kLink = t.getTagValueString();
                else throw new ECTagParsingException("Missing EC_TAG_PARTFILE_ED2K_LINK in server response");     

                
                // TODO: These are different in 2.2.6 and 2.3.1. We remove them for now
                
                
                //t = pt.getSubTagByName(ECCodes.EC_TAG_PARTFILE_PART_STATUS);
                //if (t != null) partStatus = t.getTagValueCustom();
                //else throw new ECTagParsingException("Missing EC_TAG_PARTFILE_PART_STATUS in server response"); 

                
                //t = pt.getSubTagByName(ECCodes.EC_TAG_PARTFILE_GAP_STATUS);
                //if (t != null) gapStatus = t.getTagValueCustom();
                //else throw new ECTagParsingException("Missing EC_TAG_PARTFILE_GAP_STATUS in server response"); 
                
                //t = pt.getSubTagByName(ECCodes.EC_TAG_PARTFILE_REQ_STATUS);
                //if (t != null) reqStatus = t.getTagValueCustom();
                //else throw new ECTagParsingException("Missing EC_TAG_PARTFILE_REQ_STATUS in server response"); 


                
                
                // Optional tags
                
                // TBV What should we do when refreshing if these are not present? Erase them or leave them?  
                
                t = pt.getSubTagByName(ECCodes.EC_TAG_PARTFILE_LAST_RECV);
                if (t != null) lastRecv = new Date((int) t.getTagValueUInt());
                // else throw new ECException("Missing EC_TAG_PARTFILE_LAST_RECV in server response");

                
                

                t = pt.getSubTagByName(ECCodes.EC_TAG_PARTFILE_COMMENTS);
                if (comments != null) {
                    comments.clear();
                } else {
                    comments = new ArrayList<ECPartFileComment>();
                }
                if (t == null) {
                    commentCount = 0;
                } else {
                    if (t.getSubTags() != null && ! t.getSubTags().isEmpty()) {
                        Iterator<ECTag> itr = t.getSubTags().iterator();
                        while(itr.hasNext()) comments.add(new ECPartFileComment(itr));
                        commentCount = comments.size();
                    } else {
                        //commentCount = (int) t.getTagValueUInt();
                        commentCount = 0;
                    }
                }

                parseSourceNamesList(pt);

                // Normalize content...
                if (status == PS_EMPTY && sourceCount > 0) {
                    status = PS_READY;
                } else if (status == PS_COMPLETING || status == PS_COMPLETE) {
                    sizeDone = sizeFull; 
                }
                
                // Continue to next case...
                
                
                break;
                
            default:
                throw new ECTagParsingException("Unknown detail level " + detailLevel + " for EC_TAG_PARTFILE");

            }
            
        } catch (DataFormatException e) {
            throw new ECTagParsingException("One or more unexpected type in EC_PARTFILE tags", e);
        }
        
    }
    
    protected void parseSourceNamesList(ECTag pt) throws ECTagParsingException {
        ECTag t = pt.getSubTagByName(ECCodes.EC_TAG_PARTFILE_SOURCE_NAMES);
        if (sourceNames != null) {
            sourceNames.clear();
        } else {
            sourceNames = new ArrayList<ECPartFileSourceName>();
        }
        if (t != null && t.getSubTags() != null && ! t.getSubTags().isEmpty()) {
            Iterator<ECTag> itr = t.getSubTags().iterator();
            while(itr.hasNext()) sourceNames.add(new ECPartFileSourceName(itr));
        }
    }

    public void copyValuesFromPartFile(ECPartFile p) {
        detailLevel = p.detailLevel;
        hash = new byte[16]; System.arraycopy(p.getHash(), 0, hash, 0, 16);
        fileName = p.fileName;
        ed2kLink = p.ed2kLink;
        status = p.status;
        prio = p.prio;
        cat = p.cat;
        sourceCount = p.sourceCount;
        metID = p.metID;
        sourceA4AF = p.sourceA4AF;
        sourceXfer = p.sourceXfer;
        sourceNotCurrent = p.sourceNotCurrent;
        sizeXfer = p.sizeXfer;
        sizeFull = p.sizeFull;
        sizeDone = p.sizeDone;
        speed = p.speed;
        lastSeenComp = p.lastSeenComp;
        lastRecv = p.lastRecv;
        partStatus = p.partStatus;
        gapStatus = p.gapStatus;
        reqStatus = p.reqStatus;
        commentCount = p.commentCount;
        
        //TODO refresh values instead
        comments = p.comments;
        sourceNames = p.sourceNames;
        
    }
    
    
    
    
    
    

    
    
    
    
    
    
    
    
    
    public byte getDetailLevel() {
        return detailLevel;
    }

    public byte[] getHash() {
        return hash;
    }
    
    public String getHashAsString() {
        return ECUtils.byteArrayToHexString(hash);
    }

    public String getFileName() {
        return fileName;
    }

    public String getEd2kLink() {
        return ed2kLink;
    }

    public byte getStatus() {
        return status;
    }

    public byte getPrio() {
        return prio;
    }

    public long getCat() {
        return cat;
    }

    public int getSourceCount() {
        return sourceCount;
    }

    public int getMetID() {
        return metID;
    }

    public int getSourceA4AF() {
        return sourceA4AF;
    }

    public int getSourceXfer() {
        return sourceXfer;
    }

    public int getSourceNotCurrent() {
        return sourceNotCurrent;
    }

    public long getSizeXfer() {
        return sizeXfer;
    }

    public long getSizeFull() {
        return sizeFull;
    }

    public long getSizeDone() {
        return sizeDone;
    }

    public long getSpeed() {
        return speed;
    }

    public Date getLastSeenComp() {
        return lastSeenComp;
    }

    public Date getLastRecv() {
        return lastRecv;
    }

    public byte[] getPartStatus() {
        return partStatus;
    }

    public byte[] getGapStatus() {
        return gapStatus;
    }

    public byte[] getReqStatus() {
        return reqStatus;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public ArrayList<ECPartFileComment> getComments() {
        return comments;
    }

    public ArrayList<ECPartFileSourceName> getSourceNames() {
        return sourceNames;
    }

    
    
    
    
    
    public byte getWorstRating() {
        byte ret = -1;
        
        if (comments != null) {
            Iterator <ECPartFileComment> i = comments.iterator();
            while (i.hasNext()) {
                byte thisRating = i.next().rating;
                if (ret == -1 || ret > thisRating) ret = thisRating;
            }
        }
        
        return (ret == -1 ? RATING_NOT_RATED : ret);
    }
    
    
    
    
    


    @Override
    public String toString() {
        return String.format(
                        "ECPartFile [detailLevel=%s, hash=%s, fileName=%s, ed2kLink=%s, status=%s, prio=%s, cat=%s, sourceCount=%s, metID=%s, sourceA4AF=%s, sourceXfer=%s, sourceNotCurrent=%s, sizeXfer=%s, sizeFull=%s, sizeDone=%s, speed=%s, lastSeenComp=%s, lastRecv=%s, partStatus=%s, gapStatus=%s, reqStatus=%s, commentCount=%s, comments=%s, sourceNames=%s]",
                        detailLevel,  getHashAsString(), fileName, ed2kLink, status, prio, cat, sourceCount, metID, sourceA4AF, sourceXfer, sourceNotCurrent,
                        sizeXfer, sizeFull, sizeDone, speed, lastSeenComp, lastRecv, partStatus, gapStatus, reqStatus, commentCount, comments, sourceNames);
    }







    public class ECPartFileComment {
        protected String author;
        protected String sourceName;
        protected byte rating;
        protected String comment;
        
        public ECPartFileComment(Iterator <ECTag> i) throws ECTagParsingException   {
            ECTag t;
            try {
                if (i.hasNext()) {
                    t = i.next();
                    if (t.getTagName() != ECCodes.EC_TAG_PARTFILE_COMMENTS) throw new ECTagParsingException("Unexpected tag " + t.getTagName() + " while looking for EC_TAG_PARTFILE_COMMENTS");
                    author = new String(t.getTagValueString());
    
                    if (i.hasNext()) {
                        t = i.next();
                        if (t.getTagName() != ECCodes.EC_TAG_PARTFILE_COMMENTS) throw new ECTagParsingException("Unexpected tag " + t.getTagName() + " while looking for EC_TAG_PARTFILE_COMMENTS");
                        sourceName = new String(t.getTagValueString());
                        
                        if (i.hasNext()) {
                            t = i.next();
                            if (t.getTagName() != ECCodes.EC_TAG_PARTFILE_COMMENTS) throw new ECTagParsingException("Unexpected tag " + t.getTagName() + " while looking for EC_TAG_PARTFILE_COMMENTS");
                            rating = (byte) t.getTagValueUInt();
                            
                            if (i.hasNext()) {
                                t = i.next();
                                if (t.getTagName() != ECCodes.EC_TAG_PARTFILE_COMMENTS) throw new ECTagParsingException("Unexpected tag " + t.getTagName() + " while looking for EC_TAG_PARTFILE_COMMENTS");
                                comment = new String(t.getTagValueString());
                                return;
                            }
                        }
                    }
                }
            } catch (DataFormatException e) {
                throw new ECTagParsingException("One or more unexpected type in EC_TAG_PARTFILE_COMMENTS tag", e);
            }
            throw new ECTagParsingException("Unexpected end of EC_TAG_PARTFILE_COMMENTS tags");
        }

        public String getAuthor() {
            return author;
        }

        public String getSourceName() {
            return sourceName;
        }

        public byte getRating() {
            return rating;
        }

        public String getComment() {
            return comment;
        }

        @Override
        public String toString() {
            return String.format("ECPartFileComment [author=%s, sourceName=%s, rating=%s, comment=%s]", author, sourceName, rating, comment);
        }
        
        
        
    }
    
    public class ECPartFileSourceName {
        protected String name;
        protected int count;
        
        public ECPartFileSourceName() { return; }
        
        public ECPartFileSourceName(String name, int count) {
            this.name = name;
            this.count = count;
        }
        
        public ECPartFileSourceName(Iterator <ECTag> i) throws ECTagParsingException  {
            ECTag t;
            try {
                if (i.hasNext()) {
                    t = i.next();
                    if (t.getTagName() != ECCodes.EC_TAG_PARTFILE_SOURCE_NAMES) throw new ECTagParsingException("Unexpected tag " + t.getTagName() + " while looking for EC_TAG_PARTFILE_SOURCE_NAMES");
                    name = new String(t.getTagValueString());
    
                    if (i.hasNext()) {
                        t = i.next();
                        if (t.getTagName() != ECCodes.EC_TAG_PARTFILE_SOURCE_NAMES) throw new ECTagParsingException("Unexpected tag " + t.getTagName() + " while looking for EC_TAG_PARTFILE_SOURCE_NAMES");
                        count = (int) t.getTagValueUInt();
                        return;
                    }
                }
            } catch (DataFormatException e) {
                throw new ECTagParsingException("One or more unexpected type in EC_TAG_PARTFILE_SOURCE_NAMES tag", e);
            }
            throw new ECTagParsingException("Unexpected end of EC_TAG_PARTFILE_SOURCE_NAMES tags");
        }

        public String getName() {
            return name;
        }

        public int getCount() {
            return count;
        }
        
        public void setCount(int count) {
            this.count = count;
        }


        @Override
        public String toString() {
            return String.format("ECPartFileSourceName [name=%s, count=%s]", name, count);
        }
        
    }

    
    
    
    
    
    
    
    
    public static class ECPartFileComparator implements Comparator<ECPartFile> {
        
        // TODO Implement reverse y/n and let application decide
        
        public enum ComparatorType {
            STATUS, TRANSFERED, PROGRESS, SIZE, SPEED, PRIORITY, REMAINING, LAST_SEEN_COMPLETE, FILENAME
        }
        
        private ComparatorType compType;

        
        public ECPartFileComparator(ComparatorType compType) {
            this.compType = compType;
        }

        public ComparatorType getCompType() {
            return compType;
        }

        public void setCompType(ComparatorType compType) {
            this.compType = compType;
        }

        @Override
        public int compare(ECPartFile object1, ECPartFile object2) {
            switch (compType) {
            case STATUS:
                return sortByStatus(object1, object2);
            case TRANSFERED:
                return (object2.getSizeDone() > object1.getSizeDone() ? 1 : object1.getSizeDone() == object2.getSizeDone() ? sortByFileName(object1, object2) : -1);
            case SIZE:
                return (object2.getSizeFull() > object1.getSizeFull() ? 1 : object1.getSizeFull() == object2.getSizeFull() ? sortByFileName(object1, object2) : -1);
            case PROGRESS:
                float p1 = ((float) object1.getSizeDone()) * 100f / ((float) object1.getSizeFull());
                float p2 = ((float) object2.getSizeDone()) * 100f / ((float) object2.getSizeFull());
                return p2 > p1 ? 1 : (p2 < p1 ? -1 : sortByFileName(object1, object2));
            case SPEED:
                return sortBySpeed(object1, object2);
            case PRIORITY:
                return sortByPriority(object1, object2);
            case REMAINING:
                return sortByRemaining(object1, object2);
            case LAST_SEEN_COMPLETE:
                return object2.getLastSeenComp().compareTo(object1.getLastSeenComp());
            case FILENAME:
            default:
                return sortByFileName(object1, object2);
            }
        }
        
        protected int sortByRemaining(ECPartFile object1, ECPartFile object2) {
            
            if (object1.getSpeed() == 0) {
                if (object2.getSpeed() == 0) {
                    return sortByFileName(object1, object2);
                } else {
                    return 1;
                }
            }
            
            if (object2.getSpeed() == 0) return -1;
            
            
            long r1 = (object1.getSizeFull() - object1.getSizeDone()) / object1.getSpeed() ;
            long r2 = (object2.getSizeFull() - object2.getSizeDone()) / object2.getSpeed() ;;
            return r1 > r2 ? 1 : (r1 < r2 ? -1 : 0);
        }
        
        protected int sortByFileName(ECPartFile object1, ECPartFile object2) {
            return object1.getFileName().compareToIgnoreCase(object2.getFileName());
        }
        
        protected int sortBySpeed(ECPartFile object1, ECPartFile object2) {
            return (object2.getSpeed() > object1.getSpeed()) ? 1 : (object1.getSpeed() == object2.getSpeed() ? sortByFileName(object1, object2) : -1);
        }

        protected int encodStatusOrder(byte status) {
            switch(status) {
            case PS_UNKNOWN: return 2;
            case PS_ERROR: return 3;
            case PS_PAUSED: return 4;
            case PS_WAITINGFORHASH: return 5;
            case PS_HASHING: return 6;
            case PS_EMPTY: return 7;
            case PS_INSUFFICIENT: return 8;
            case PS_READY: return 9;
            case PS_COMPLETING: return 10;
            case PS_COMPLETE: return 11;
            case PS_ALLOCATING: return 12;
            default: return 1;
            }
        }
        
        protected int encodePriorityOrder(byte prio) {
            switch (prio) {
            case PR_AUTO_LOW: return 2;
            case PR_LOW: return 3;
            case PR_AUTO_NORMAL: return 4;
            case PR_NORMAL: return 5;
            case PR_AUTO_HIGH: return 6;
            case PR_HIGH: return 7;
            default: return 1;
            }
        }

        protected int sortByPriority (ECPartFile object1, ECPartFile object2) {
            int p1 = encodePriorityOrder(object1.getPrio());
            int p2 = encodePriorityOrder(object2.getPrio());
            
            if (p1 != p2) return p2 - p1;
            return sortByFileName(object1, object2);
        }

        
        protected int sortByStatus (ECPartFile object1, ECPartFile object2) {
            int s1 = encodStatusOrder(object1.getStatus());
            int s2 = encodStatusOrder(object2.getStatus());
            
            if (s1 != s2) return s2 - s1;
            if (object1.getStatus() == ECPartFile.PS_READY) return sortBySpeed(object1, object2);
            return sortByFileName(object1, object2);
        }
    }

   

}

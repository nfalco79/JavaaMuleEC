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

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.charset.CharacterCodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.zip.DataFormatException;

import com.iukonline.amule.ec.ECRawPacket.ECRawTag;
import com.iukonline.amule.ec.exceptions.ECTagParsingException;



public class ECTag  {

    private short       tagName;
    private byte        tagType = ECTagTypes.EC_TAGTYPE_UNKNOWN;

    private int         nestingLevel = 0;

    private String            tagValueString;
    private long              tagValueUInt;
    private BigInteger        tagValueUInt128;
    private InetSocketAddress tagValueIPv4;
    private byte[]            tagValueHash;
    private byte[]            tagValueCustom;
    private double            tagValueDouble;

    ArrayList<ECTag>    subTags;        // Note: this is not synchronized... Take care if used in multiple thread

    ECRawTag rawTag;

    public ECTag() {
        subTags = new ArrayList<ECTag>();
    }

    public ECTag(ECTag t) {
        tagName = t.tagName;
        tagType = t.tagType;
        nestingLevel = t.nestingLevel;
        tagValueString = t.tagValueString;
        tagValueUInt = t.tagValueUInt;
        tagValueUInt128 = t.tagValueUInt128;
        if (t.tagValueIPv4 != null) tagValueIPv4 = new InetSocketAddress(t.tagValueIPv4.getAddress(), t.tagValueIPv4.getPort());
        if (t.tagValueHash != null) {
            tagValueHash = new byte[t.tagValueHash.length];
            System.arraycopy(t.tagValueHash, 0, tagValueHash, 0, t.tagValueHash.length);
        }
        if (t.tagValueCustom != null) {
            tagValueCustom = new byte[t.tagValueCustom.length];
            System.arraycopy(t.tagValueCustom, 0, tagValueCustom, 0, t.tagValueCustom.length);
        }
        if (subTags != null) {
            for (ECTag sub : subTags) {
                t.addSubTag(new ECTag(sub));
            }
        }

    }


    public ECTag(short tagName, byte tagType) throws DataFormatException {
        this();
        this.setTagName(tagName);
        this.setTagType(tagType);
    }

    public ECTag(short tagName, byte tagType, String tagValue) throws DataFormatException {
        this(tagName, tagType);
        this.setTagValueString(tagValue);
    }

    public ECTag(short tagName, byte tagType, long tagValue) throws DataFormatException {
        this(tagName, tagType);
        this.setTagValueUInt(tagValue);
    }

    public ECTag(short tagName, String tagValue) throws DataFormatException {
        this(tagName, ECTagTypes.EC_TAGTYPE_STRING, tagValue);
    }

    public ECTag(short tagName, byte tagType, InetSocketAddress tagValue) throws DataFormatException {
        this(tagName, tagType);
        this.setTagValueIPv4(tagValue);
    }

    public ECTag(short tagName, InetSocketAddress tagValue) throws DataFormatException {
        this(tagName, ECTagTypes.EC_TAGTYPE_IPV4, tagValue);
    }

    public ECTag(short tagName, byte tagType, byte[] tagValue) throws DataFormatException {
        this(tagName, tagType);
        switch (tagType) {
        case ECTagTypes.EC_TAGTYPE_HASH16:
            setTagValueHash(tagValue);
            break;
        case ECTagTypes.EC_TAGTYPE_CUSTOM:
            setTagValueCustom(tagValue);
            break;
        default:
            throw new DataFormatException("Tag type is neither HASH16 nor CUSTOM");

        }
    }

    public String getTagValueString() throws DataFormatException {
        if (this.tagType == ECTagTypes.EC_TAGTYPE_STRING) {
            return tagValueString;
        } else {
            throw new DataFormatException("Tag type is not STRING");
        }
    }

    public void setTagValueString(String tagValueString) throws DataFormatException {
        if (this.tagType == ECTagTypes.EC_TAGTYPE_STRING) {
            this.tagValueString = tagValueString;
        } else {
            throw new DataFormatException("Tag type is not STRING");
        }
    }

    public long getTagValueUInt() throws DataFormatException {
        switch (tagType) {
        case ECTagTypes.EC_TAGTYPE_UINT8:
        case ECTagTypes.EC_TAGTYPE_UINT16:
        case ECTagTypes.EC_TAGTYPE_UINT32:
        case ECTagTypes.EC_TAGTYPE_UINT64:
            return tagValueUInt;
        default:
            throw new DataFormatException("Tag type is not UINT*");
        }
    }

    public BigInteger getTagValueUInt128() throws DataFormatException {
    	switch (tagType) {
    	case ECTagTypes.EC_TAGTYPE_UINT128:
    		return tagValueUInt128;
    	default:
    		throw new DataFormatException("Tag type is not UINT*");
    	}
    }

    public void setTagValueUInt(long tagValueUInt) throws DataFormatException {
        switch (tagType) {
        case ECTagTypes.EC_TAGTYPE_UINT8:
        case ECTagTypes.EC_TAGTYPE_UINT16:
        case ECTagTypes.EC_TAGTYPE_UINT32:
        case ECTagTypes.EC_TAGTYPE_UINT64:
            this.tagValueUInt = tagValueUInt;
            break;
        default:
            throw new DataFormatException("Tag type is not UINT*");
        }
    }

	public void setTagValueUInt(BigInteger tagValueUInt) throws DataFormatException {
        switch (tagType) {
        case ECTagTypes.EC_TAGTYPE_UINT8:
        case ECTagTypes.EC_TAGTYPE_UINT16:
        case ECTagTypes.EC_TAGTYPE_UINT32:
        case ECTagTypes.EC_TAGTYPE_UINT64:
        case ECTagTypes.EC_TAGTYPE_UINT128:
            this.tagValueUInt128 = tagValueUInt;
            break;
        default:
            throw new DataFormatException("Tag type is not UINT*");
        }
	}

    public InetSocketAddress getTagValueIPv4() throws DataFormatException {
        if (tagType == ECTagTypes.EC_TAGTYPE_IPV4) {
            return tagValueIPv4;
        } else {
            throw new DataFormatException("Tag type is not IPV4");
        }
    }

    public void setTagValueIPv4(InetSocketAddress tagValueIPv4) throws DataFormatException {
        if (tagType == ECTagTypes.EC_TAGTYPE_IPV4) {
            this.tagValueIPv4 = tagValueIPv4;
        } else {
            throw new DataFormatException("Tag type is not IPV4");
        }
    }

    public byte[] getTagValueHash() throws DataFormatException {
        if (tagType == ECTagTypes.EC_TAGTYPE_HASH16) {
            return tagValueHash;
        } else {
            throw new DataFormatException("Tag type is not HASH16");
        }
    }

    public void setTagValueHash(byte[] tagValueHash) throws DataFormatException {
        if (tagType == ECTagTypes.EC_TAGTYPE_HASH16) {
            this.tagValueHash = tagValueHash;
        } else {
            throw new DataFormatException("Tag type is not HASH16");
        }
    }

    public byte[] getTagValueCustom() throws DataFormatException {
        if (tagType == ECTagTypes.EC_TAGTYPE_CUSTOM) {
            return tagValueCustom;
        } else {
            throw new DataFormatException("Tag type is not CUSTOM");
        }

    }

    public void setTagValueCustom(byte[] tagValueCustom) throws DataFormatException {
        if (tagType == ECTagTypes.EC_TAGTYPE_CUSTOM) {
            this.tagValueCustom = tagValueCustom;
        } else {
            throw new DataFormatException("Tag type is not CUSTOM");
        }

    }

    public void setTagValueDouble(double tagValueDouble) throws DataFormatException {
        if (tagType == ECTagTypes.EC_TAGTYPE_DOUBLE) {
            this.tagValueDouble = tagValueDouble;
        } else {
            throw new DataFormatException("Tag type is not DOUBLE");
        }
    }

    public double getTagValueDouble() throws DataFormatException {
        if (tagType == ECTagTypes.EC_TAGTYPE_DOUBLE) {
            return tagValueDouble;
        } else {
            throw new DataFormatException("Tag type is not DOUBLE");
        }
    }

    public int getTagName() {
        return tagName;
    }

    public void setTagName(short tagName) {
        this.tagName = tagName;
    }

    public byte getTagType() {
        return tagType;
    }

    public void setTagType(byte tagType) throws DataFormatException {
        switch (tagType) {
        case ECTagTypes.EC_TAGTYPE_CUSTOM:
        case ECTagTypes.EC_TAGTYPE_UINT8:
        case ECTagTypes.EC_TAGTYPE_UINT16:
        case ECTagTypes.EC_TAGTYPE_UINT32:
        case ECTagTypes.EC_TAGTYPE_UINT64:
        case ECTagTypes.EC_TAGTYPE_UINT128:
        case ECTagTypes.EC_TAGTYPE_STRING:
        case ECTagTypes.EC_TAGTYPE_IPV4:
        case ECTagTypes.EC_TAGTYPE_HASH16:
        case ECTagTypes.EC_TAGTYPE_DOUBLE:
            this.tagType = tagType;
            break;
        default:
            throw new DataFormatException("Unknown tag type " + ECUtils.byteArrayToHexString(tagType));
        }
    }








    public ArrayList<ECTag> getSubTags() {
        return subTags;
    }

    public void addSubTag(ECTag subTag) {
        this.subTags.add(subTag);
    }

    public ECTag getSubTagByName(short tagName) {
        if (! subTags.isEmpty()) {
            Iterator<ECTag> itr = subTags.iterator();
            while(itr.hasNext()) {
                ECTag tag = itr.next();
                if (tag.getTagName() == tagName) {
                    return tag;
                }
            }
        }
        return null;
    }

    public long getLength(boolean withHeader, boolean isUTF8Compressed) throws ECTagParsingException {

        //long len = withHeader ? 7 : 0; // ec_tagname_t - uint16, ec_tagtype_t - uint8, ec_taglen_t - uint32
        long len = 0L;
        if (withHeader) {
            if (!isUTF8Compressed) {
                len += 7L;
            } else {
                try {
                    len += ECUtils.UTF8Length(tagName << 1); // ec_tagname_t
                } catch (CharacterCodingException e) {
                    throw new ECTagParsingException("Invlid tagName, not UTF-8 encodable: " + tagName, e);
                }
                len += 1; // ec_tagtype_t
                try {
                    len += ECUtils.UTF8Length(getLength(false, isUTF8Compressed)); // ec_taglen_t
                } catch (CharacterCodingException e) {
                    throw new ECTagParsingException("Invlid tag length, not UTF-8 encodable: " + len, e);
                }
            }
        }

        switch (tagType) {
        case ECTagTypes.EC_TAGTYPE_CUSTOM:
            if (tagValueCustom != null) {
                len += tagValueCustom.length;
            }
            break;
        case ECTagTypes.EC_TAGTYPE_UINT8:
            len += 1;
            break;
        case ECTagTypes.EC_TAGTYPE_UINT16:
            len += 2;
            break;
        case ECTagTypes.EC_TAGTYPE_UINT32:
            len += 4;
            break;
        case ECTagTypes.EC_TAGTYPE_UINT64:
            len += 8;
            break;
        case ECTagTypes.EC_TAGTYPE_STRING:
            try {
                len += tagValueString.getBytes("UTF-8").length + 1;
            } catch (UnsupportedEncodingException e) {
                throw new ECTagParsingException("Severe error: UTF-8 not supported for string encoding", e);
            }
            break;
        case ECTagTypes.EC_TAGTYPE_DOUBLE:
            try {
                len += Double.toString(tagValueDouble).getBytes("UTF-8").length + 1;
            } catch (UnsupportedEncodingException e) {
                throw new ECTagParsingException("Severe error: UTF-8 not supported for string encoding", e);
            }
            break;
        case ECTagTypes.EC_TAGTYPE_IPV4:
            len += 4;
            break;
        case ECTagTypes.EC_TAGTYPE_HASH16:
            len += 16;
            break;
        default:
            // Don't need an exception. tagType has been checked in set method
        }

        if (! subTags.isEmpty()) {
            if (withHeader)

                if (isUTF8Compressed) {

                    try {
                        len += ECUtils.UTF8Length(subTags.size());
                    } catch (CharacterCodingException e) {
                        throw new ECTagParsingException("Invlid tag count, not UTF-8 encodable: " + len, e);
                    }
                } else {
                    len += 2;
                }

                Iterator<ECTag> itr = subTags.iterator();
                while(itr.hasNext()) {
                    // TBV It seems that packet len is computed as not comrpessed for subtags
                     len += itr.next().getLength(true, isUTF8Compressed);
                }


        }

        return len;
    }

    public long getLength() throws ECTagParsingException {
        return getLength(false, false);
    }






    @Override
    public String toString() {

        StringBuffer sbIndent = new StringBuffer("        ");
        for (int i = 0; i < nestingLevel; i++) {
            sbIndent.append("        ");
        }

        String indent = sbIndent.toString();

        StringBuffer out = new StringBuffer(indent);
        out.append("Tag Name: <" + Integer.toHexString(tagName) + ">\n");

        String type;
        String value;

        switch (tagType) {
        case ECTagTypes.EC_TAGTYPE_CUSTOM:
            type = "CUSTOM";
            value = ECUtils.byteArrayToHexString(tagValueCustom);
            break;
        case ECTagTypes.EC_TAGTYPE_UINT8:
            type = "UINT8";
            value = Long.toString(tagValueUInt);
            break;
        case ECTagTypes.EC_TAGTYPE_UINT16:
            type = "UINT16";
            value = Long.toString(tagValueUInt);
            break;
        case ECTagTypes.EC_TAGTYPE_UINT32:
            type = "UINT32";
            value = Long.toString(tagValueUInt);
            break;
        case ECTagTypes.EC_TAGTYPE_UINT64:
            type = "UINT64";
            value = Long.toString(tagValueUInt);
            break;
        case ECTagTypes.EC_TAGTYPE_UINT128:
        	type = "UINT128";
        	value = tagValueUInt128.toString();
        	break;
        case ECTagTypes.EC_TAGTYPE_STRING:
            type = "STRING";
            value = tagValueString;
            break;
        case ECTagTypes.EC_TAGTYPE_DOUBLE:
            type = "DOUBLE";
            value = Double.toString(tagValueDouble);
            break;
        case ECTagTypes.EC_TAGTYPE_IPV4:
            type = "IPV4";
            value = tagValueIPv4.toString();
            break;
        case ECTagTypes.EC_TAGTYPE_HASH16:
            type = "HASH16";
            value = ECUtils.byteArrayToHexString(tagValueHash);
            break;
        default:
            type = "UNKNOWN TYPE (" + Integer.toHexString(tagType) + ")";
            value = "";

        }

        out.append(indent + "Tag Type: <" + type + ">\n");


        if (! this.subTags.isEmpty()) {
            out.append(indent + "Subtags (" + subTags.size() + ")\n");
            Iterator<ECTag> itr = subTags.iterator();
            while(itr.hasNext()) {
                out.append(itr.next().toString());
            }
        }

        out.append(indent + "Tag Value: <" + value + ">\n");
        return out.toString();
    }

}

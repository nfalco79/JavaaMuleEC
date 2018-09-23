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

package com.iukonline.amule.ec.exceptions;

import com.iukonline.amule.ec.ECRawPacket;

public class ECPacketParsingException extends Exception {

    ECRawPacket causePacket;
    


    private static final long serialVersionUID = -1557992414575949145L;
    
    public ECPacketParsingException(String detailMessage, ECRawPacket causePacket, Throwable throwable) {
        super(detailMessage, throwable);
        this.causePacket = causePacket;
    }
    
    public ECPacketParsingException(String detailMessage, ECRawPacket causePacket) {
        super(detailMessage);
        this.causePacket = causePacket;
    }

    public ECPacketParsingException(String detailMessage) {
        super(detailMessage);
    }
     
    
    public ECPacketParsingException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
    
    public ECRawPacket getCausePacket() {
        return causePacket;
    }

}

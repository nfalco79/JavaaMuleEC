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

import com.iukonline.amule.ec.ECPacket;


public class ECClientException extends Exception {

    

    /**
     * 
     */
    private static final long serialVersionUID = 1325784420371315051L;
    ECPacket request;
    ECPacket response;

    public ECClientException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
    
    public ECClientException(String detailMessage) {
        super(detailMessage);
    }
    
    public ECClientException(String detailMessage, ECPacket request, ECPacket response, Throwable throwable) {
        super(detailMessage, throwable);
        this.request = request;
        this.response = response;
    }
    
    public ECClientException(String detailMessage, ECPacket request, ECPacket response) {
        super(detailMessage);
        this.request = request;
        this.response = response;
    }
    
    public ECPacket getRequestPacket() { return request; }
    public ECPacket getResponsePacket() { return response; }

}

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

package com.iukonline.amule.ec.v203;

import com.iukonline.amule.ec.ECPacket;
import com.iukonline.amule.ec.exceptions.ECClientException;
import com.iukonline.amule.ec.v204.ECClientV204;

public class ECClientV203 extends ECClientV204 {
    
    @Override
    protected ECPacket buildLoginRequest() throws ECClientException {
        return buildLoginRequest((long) 0x0203);
    }
}

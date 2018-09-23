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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.DataFormatException;

import org.junit.Test;

import com.iukonline.amule.ec.exceptions.ECPacketParsingException;
import com.iukonline.amule.ec.v204.ECRawPacketV204;

public class ECRawTest {


    @Test public void testSalt() throws NoSuchAlgorithmException, UnsupportedEncodingException {

        /*
        DEBUG: saltString = F8A6A7612E8E090A
        DEBUG: saltHash = 1f00022ab969d5de848c62c47a2c53fb
        DEBUG: m_connectionPassword before = 098F6BCD4621D373CADE4E832627B4F6
        DEBUG: m_connectionPassword after = f0da283ec2405883101ec4b6dc2d3b43
        */

        long salt = -529551870334727926L;


        byte[] saltHexBytes = ECUtils.uintToBytes(salt, 8, true);
        System.out.printf("SALT PRINTF: %X\n",  salt);
        System.out.println("SALT BYTES: " + ECUtils.byteArrayToHexString(saltHexBytes));

        //byte[] saltHash = MessageDigest.getInstance("MD5").digest(ECUtils.byteArrayToHexString(saltHexBytes, 8, 0, null).getBytes());
        byte[] saltHash = MessageDigest.getInstance("MD5").digest(String.format("%X", salt).getBytes());
        System.out.println("SALT HASH: " + ECUtils.byteArrayToHexString(saltHash));

        byte[] passwd = MessageDigest.getInstance("MD5").digest(new String("test").getBytes("UTF-8"));
        System.out.println("HASH PASSWD: " + ECUtils.byteArrayToHexString(passwd));


        MessageDigest digest = MessageDigest.getInstance("MD5");

        digest.update(ECUtils.byteArrayToHexString(passwd, 16, 0, null).toLowerCase().getBytes());
        digest.update(ECUtils.byteArrayToHexString(saltHash, 16, 0, null).toLowerCase().getBytes());
        System.out.println("RESPONSE: " + ECUtils.byteArrayToHexString(digest.digest()));

    }


    @Test
    public void testTrace() throws IOException, DataFormatException, ECPacketParsingException {
        File clientFile = new File("test/com/iukonline/amule/ec/test/LogonSearch231Client.bin");
        File serverFile = new File("test/com/iukonline/amule/ec/test/LogonSearch231Server.bin");

        FileInputStream clientStream = new FileInputStream(clientFile);
        FileInputStream serverStream = new FileInputStream(serverFile);

        while (clientStream.available() > 0) {
        //for (int i = 0; i < 5; i++) {

            System.out.println("------------- REQUEST ---------------------");
            /*
              ECRawPacket req = new ECRawPacket(clientStream);

            req.parse();

            System.out.println(req.dump());

            */

            ECPacket p = ECPacket.readFromStream(clientStream, ECRawPacketV204.class);
            System.out.println(p.getEncodedPacket().dump());



            if (serverStream.available() > 0) {
                System.out.println("------------- RESPONSE --------------");
                /*ECRawPacket resp = new ECRawPacket(serverStream);
                resp.parse();*/

                ECPacket p2 = ECPacket.readFromStream(serverStream, ECRawPacketV204.class);
                System.out.println(p2.getEncodedPacket().dump());

                // System.out.println(resp.dump());

            }
        }
    }

    @Test
    public void testTags() throws DataFormatException, ECPacketParsingException {
        ECPacket epReq = new ECPacket();
        epReq.setOpCode(ECCodes.EC_OP_PARTFILE_SWAP_A4AF_THIS);
        byte[] hash = { 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0xa, 0xb, 0xc, 0xd, 0xe, 0xf , 0x10};
        epReq.addTag(new ECTag(ECCodes.EC_TAG_PARTFILE, ECTagTypes.EC_TAGTYPE_HASH16, hash));
        @SuppressWarnings("unused")
        ECRawPacket raw = new ECRawPacket(epReq);
    }

}

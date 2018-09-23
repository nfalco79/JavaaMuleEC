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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.zip.DataFormatException;

import org.junit.Test;

import com.iukonline.amule.ec.exceptions.ECPacketParsingException;
import com.iukonline.amule.ec.v204.ECCodesV204;
import com.iukonline.amule.ec.v204.ECRawPacketV204;

public class ECRawTest {

	@Test
	public void testSalt() throws NoSuchAlgorithmException, UnsupportedEncodingException {
		long salt = -529551870334727926L;
		byte[] saltHexBytes = ECUtils.uintToBytes(salt, 8, true);
		assertThat(ECUtils.byteArrayToHexString(saltHexBytes), equalTo("F8 A6 A7 61 2E 8E 09 0A"));

		byte[] saltHash = MessageDigest.getInstance("MD5").digest(String.format("%X", salt).getBytes());
		assertThat(ECUtils.byteArrayToHexString(saltHash), equalTo("1F 00 02 2A B9 69 D5 DE 84 8C 62 C4 7A 2C 53 FB"));

		byte[] passwd = MessageDigest.getInstance("MD5").digest(new String("test").getBytes("UTF-8"));
		assertThat(ECUtils.byteArrayToHexString(passwd), equalTo("09 8F 6B CD 46 21 D3 73 CA DE 4E 83 26 27 B4 F6"));

		MessageDigest digest = MessageDigest.getInstance("MD5");

		digest.update(ECUtils.byteArrayToHexString(passwd, 16, 0, null).toLowerCase().getBytes());
		digest.update(ECUtils.byteArrayToHexString(saltHash, 16, 0, null).toLowerCase().getBytes());
		assertThat(ECUtils.byteArrayToHexString(digest.digest()), equalTo("F0 DA 28 3E C2 40 58 83 10 1E C4 B6 DC 2D 3B 43"));

	}

	@Test
	public void testTrace() throws Exception {
		InputStream clientStream = this.getClass().getResourceAsStream("LogonSearch231Client.bin");
		InputStream serverStream = this.getClass().getResourceAsStream("LogonSearch231Server.bin");

		int requests = 0;
		while (clientStream.available() > 0) {
			ECPacket request = ECPacket.readFromStream(clientStream, ECRawPacketV204.class);
//			System.out.println(request.getEncodedPacket().dump());
			switch (requests) {
			case 0:
				assertRequestLogin(request);
				break;
			case 1:
				assertRequestSignIn(request);
				break;
			}

			if (serverStream.available() > 0) {
				ECPacket response = ECPacket.readFromStream(serverStream, ECRawPacketV204.class);
//				System.out.println(response.getEncodedPacket().dump());
				switch (requests) {
				case 0:
					assertResponseLogin(response);
					break;
				case 1:
					assertResponseSignIn(response);
					break;
				}
			}
			requests++;
		}

		clientStream.close();
		serverStream.close();
	}

	private void assertResponseLogin(ECPacket response) throws DataFormatException {
		assertFalse(response.isZlibCompressed());
		assertTrue(response.acceptsUTF8());
		assertFalse(response.hasId());
		assertThat(response.getOpCode(), equalTo(ECCodesV204.EC_OP_AUTH_SALT));
		List<ECTag> tags = response.getTags();
		assertThat(tags.size(), equalTo(1));
		ECTag ecTag = tags.get(0);
		assertThat(ecTag.getTagName(), equalTo(Integer.valueOf(ECCodesV204.EC_TAG_PASSWD_SALT)));
		assertThat(ecTag.getTagType(), equalTo(ECTagTypes.EC_TAGTYPE_UINT64));
		assertThat(ecTag.getTagValueUInt(), equalTo(1385283421829075617l));
		assertTrue(ecTag.getSubTags().isEmpty());
	}

	private void assertResponseSignIn(ECPacket response) throws DataFormatException {
		assertFalse(response.isZlibCompressed());
		assertTrue(response.acceptsUTF8());
		assertFalse(response.hasId());
		assertThat(response.getOpCode(), equalTo(ECCodesV204.EC_OP_AUTH_OK));
		List<ECTag> tags = response.getTags();
		assertThat(tags.size(), equalTo(1));
		ECTag ecTag = tags.get(0);
		assertThat(ecTag.getTagName(), equalTo(Integer.valueOf(ECCodesV204.EC_TAG_SERVER_VERSION)));
		assertThat(ecTag.getTagType(), equalTo(ECTagTypes.EC_TAGTYPE_STRING));
		assertThat(ecTag.getTagValueString(), equalTo("2.3.1 AdunanzA 2012.1b1"));
		assertTrue(ecTag.getSubTags().isEmpty());
	}

	private void assertRequestLogin(ECPacket request) {
		assertFalse(request.isZlibCompressed());
		assertTrue(request.acceptsUTF8());
		assertFalse(request.hasId());
		assertThat(request.getOpCode(), equalTo(ECCodes.EC_OP_AUTH_REQ));
		assertThat(request.getTags().size(), equalTo(5));
	}

	private void assertRequestSignIn(ECPacket request) {
		assertFalse(request.isZlibCompressed());
		assertTrue(request.acceptsUTF8());
		assertFalse(request.hasId());
		assertThat(request.getOpCode(), equalTo(ECCodesV204.EC_OP_AUTH_PASSWD));
		assertThat(request.getTags().size(), equalTo(1));
	}

	@Test
	public void testTags() throws DataFormatException, ECPacketParsingException {
		ECPacket epReq = new ECPacket();
		epReq.setOpCode(ECCodes.EC_OP_PARTFILE_SWAP_A4AF_THIS);
		byte[] hash = { 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0xa, 0xb, 0xc, 0xd, 0xe, 0xf, 0x10 };
		epReq.addTag(new ECTag(ECCodes.EC_TAG_PARTFILE, ECTagTypes.EC_TAGTYPE_HASH16, hash));
		@SuppressWarnings("unused")
		ECRawPacket raw = new ECRawPacket(epReq);
	}

}

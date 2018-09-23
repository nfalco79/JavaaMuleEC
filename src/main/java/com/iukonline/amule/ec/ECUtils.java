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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.CharacterCodingException;

import com.iukonline.amule.ec.exceptions.ECDebugException;

public class ECUtils {
    
    public static byte[] uintToBytes(long uint, int numBytes, boolean MSB) {
        byte[] ret = new byte[numBytes];
        
        long mask = 0xFF;
        for (int i = 0; i < numBytes; i++) {
            ret[MSB ? numBytes - 1 - i : i] = (byte)((uint & (mask << i * 8)) >> (i * 8));
            //System.out.println("INDEX " + (MSB ? numBytes - 1 - i : i ) + " VALUE " + ret[MSB ? numBytes - 1 - i : i] );
        }
        
        return ret;
    }
    
    
    static long bytesToUint(byte[] input, int numBytes, boolean MSB) {
        return bytesToUint(input, numBytes, MSB, false);
    }
    
    static long bytesToUint(byte[] input, int numBytes, boolean MSB, boolean debug) {
        return bytesToUint(input, 0, numBytes, MSB, debug);
    }
    
    static long bytesToUint(byte[] input, int offset, int numBytes, boolean MSB, boolean debug) {
        
        //debug = false;
        
        if (debug)
            System.out.println("bytesToUint: converting " + numBytes + " bytes starting from " +offset + " - "+ byteArrayToHexString(input, numBytes, offset));
        
        long ret = 0x0L;
        
         
                        
        for (int i = 0; i < numBytes; i++) {
            int index = offset + (MSB ? numBytes - 1 - i : i);
            if (debug) {
                System.out.println("bytesToUint: Byte " + index + "     - " + byteArrayToHexString(input[index]));
                System.out.println("bytesToUint: Wrong Long - " + (long) input[index]);
                System.out.print("bytesToUint: Bit        -");
            }
            
            if (debug) {
                for (int j = 7; j >= 0; j--) {
                    int bit = (input[index] >> j) & 0x1;
                    if (debug)
                        System.out.print(" " + bit);
                }
                System.out.print("\n");
            }
            
            for (int j = 7; j >= 0; j--) {
                long bit = (input[index] >> j) & 0x1;
                if (bit > 0) {
                    ret |= bit << (j + i * 8);
                }
            }
            
            if (debug) System.out.println("bytesToUint: Partial result - " + ret);
        }
        if (debug) System.out.println("bytesToUint: Final result - " + ret);
        
        return ret;
    }
    
    static String byteArrayToHexString(byte in) {
        return byteArrayToHexString(new byte[] { in });
        
    }
    
    public static String byteArrayToHexString(byte in[]) {
        if (in == null) {
            return null;
        } else {
            return byteArrayToHexString(in, in.length, 0);
        }
    }
    
    public static String byteArrayToHexString(byte in[], int max) {
        return byteArrayToHexString(in, max, 0);
    }
    
    public static String byteArrayToHexString(byte in[], int max, int offset) {
        return byteArrayToHexString(in, max, offset, " ");
    }
    
    public static String byteArrayToHexString(byte in[], int max, int offset, String separator) {

        byte ch = 0x00;
        int i = offset; 

        if (in == null || in.length <= 0)
            return null;

        String pseudo[] = {"0", "1", "2","3", "4", "5", "6", "7", "8","9", "A", "B", "C", "D", "E","F"};
        StringBuffer out = new StringBuffer(in.length * 3 - 1);
        while (i < max + offset) {
            if (i > offset && separator != null)
                out.append(separator);
            ch = (byte) (in[i] & 0xF0); // Strip off high nibble
            ch = (byte) (ch >>> 4);     // shift the bits down
            ch = (byte) (ch & 0x0F);    // must do this as high order bit is on!
            out.append(pseudo[ (int) ch]); // convert the nibble to a String Character

            ch = (byte) (in[i] & 0x0F); // Strip off low nibble 
            out.append(pseudo[ (int) ch]); // convert the nibble to a String Character
            i++;
        }

        String rslt = new String(out);
        return rslt;
    }
    
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    
    public static void readAllBytes(InputStream in, byte[] buf, int offset, int len, boolean debug) throws IOException {
        int remaining = (int) len;
        int pos = offset;
        int bytes = 0;
        
        if (debug) System.out.println("readAllBytes: reading " + len + " bytes");
        
        while (remaining > 0) {
            if (debug) System.out.println("readAllBytes: " + remaining + " bytes remaining");
            bytes = in.read(buf, pos, remaining);
            if (bytes < 0) {
                throw new IOException("Unexpected end of input stream (" + remaining + " more bytes were expected)");
            } else {
                remaining -= bytes;
                pos += bytes;
            }
        }
        if (debug) System.out.println("readAllBytes: done");
    }
    
    
    public static int getUTF8SequenceLength(byte firstByte, boolean debug) throws CharacterCodingException {
        
        //debug=false;
        
        if (debug) System.out.println("getUTF8SequenceLength: Evaulating length for first byte " + ECUtils.byteArrayToHexString(firstByte));
        int bitShift = 7;
        while (bitShift > 0 && (((int) (firstByte >> bitShift)) & 0x1) == 0x01) {
            if (debug) System.out.println("getUTF8SequenceLength: Bit " + bitShift + " is set");
            bitShift --;
        }
        if (bitShift == 0 || bitShift == 6) throw new CharacterCodingException();
        
        int bytesToBeRead = (bitShift == 7 ? 0 : 6 - bitShift);
        if (debug) System.out.println("getUTF8SequenceLength: Sequence is " + (bytesToBeRead + 1) + " bytes long");
        
        return bytesToBeRead + 1;
        
    }
    
    public static long decodeUTF8number(byte[] sequence, int offset, boolean debug) throws CharacterCodingException {
        
        debug=false;
        
        int len = getUTF8SequenceLength(sequence[offset], debug);
        
        int bitShift = (len == 1 ? 7 : (7 - len)); 
        
        int decodedBitLen = (len - 1) * 6 + bitShift;
        int decodedByteLen = bitShift % 8 == 0 ? bitShift / 8 : (bitShift / 8) + 1;

        if (debug) System.out.println("decodeUTF8int: Encoded sequence is " + ECUtils.byteArrayToHexString(sequence, len, offset));
        if (debug) System.out.println("decodeUTF8int: Decoded sequence will be " + decodedByteLen + " bytes long (" + decodedBitLen + " bits)");

        long result = 0L;
        
        for (int i = 0; i < decodedBitLen; i++) {
            int encodedByte = i < bitShift ? 0 : (((i - bitShift) / 6) + 1);
            int encodedBit = encodedByte == 0 ? 8 - bitShift + i : ((i - bitShift) % 6 + 2);
            
            if (debug) System.out.println("decodeUTF8int: Decoding bit " + encodedBit + " of byte " + encodedByte + " - "+ ECUtils.byteArrayToHexString(sequence, 1, offset + encodedByte));
            
            if (encodedByte > 0 && ((sequence[offset + encodedByte] >> 6) & 0x3) != 0x2) throw new CharacterCodingException();

            int bitValue = (sequence[offset + encodedByte] >> (7 - encodedBit)) & 0x01;
            result |= bitValue << (decodedBitLen - i - 1);

            if (debug) System.out.println("decodeUTF8int: Bit value is " + bitValue + ", partial result is " + result);
        }
        
        return result;
        
        
    }
    
    public static int UTF8Length(long number) throws CharacterCodingException {
        // TODO Better handle exceptions
        
        if (number < 128L) {
            return 1;
        } else if (number < 2048L) {
            return 2;
        } else if (number < 262144L) {
            return 3;
        } else if (number < 2097152L) {
            return 4;
        } else {
            throw new CharacterCodingException();
        }
    }

    public static String hexDecode(byte[] value, int offset, int numBytes, String description, int hexPerRow, int indentSpaces) throws ECDebugException {

        //System.out.printf("offset: %d, numButes: %d, description: %s, hexPerRow: %d, indentSpaces: %d\n", offset, numBytes, description, hexPerRow, indentSpaces);
        
        if (value == null) throw new ECDebugException("Trying to hexDump null payload");
        if (numBytes < 0) throw new ECDebugException("Trying to hexDump negative length (" + numBytes + ")");
        if (offset >= value.length) throw new ECDebugException("hexDump start (" + offset + ") is greater than payload length (" + value.length + ")");
        if (offset + numBytes > value.length) throw new ECDebugException("hexDump end (" + (offset + numBytes) + ") is outside payloadlength (" + value.length + ")");
        
        StringBuilder s = new StringBuilder();
        
        String rowFormat = "%-" + (hexPerRow * 3) + "s%" + (4 * indentSpaces + 1) + "s%s\n";
        int hexDumpRows = numBytes % hexPerRow == 0 ? numBytes / hexPerRow : (numBytes / hexPerRow) + 1 ;
        
        for (int i = 0; i < hexDumpRows; i++) {
            int thisRowHex = i == hexDumpRows - 1 ? numBytes - i * hexPerRow : hexPerRow;
            s.append(String.format(rowFormat, byteArrayToHexString(value, thisRowHex, offset + i * hexPerRow), "", i == 0 ? description : ""));
        }
        return s.toString();
    }


}

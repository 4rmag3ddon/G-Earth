package main.protocol.crypto;
/*
 * Copyright (C) 2003 Clarence Ho (clarence@clarenceho.net)
 * All rights reserved.
 *
 * Redistribution and use of this software for non-profit, educational,
 * or persoanl purposes, in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the author nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * In case of using this software for other purposes not stated above,
 * please conact Clarence Ho (clarence@clarenceho.net) for permission.
 *
 * THIS SOFTWARE IS PROVIDED BY CLARENCE HO "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

import main.protocol.HPacket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is a simple implementation of the RC4 (tm) encryption algorithm.  The
 * author implemented this class for some simple applications
 * that don't need/want/require the Sun's JCE framework.
 * <p>
 * But if you are looking for encryption algorithms for a
 * full-blown application,
 * it would be better to stick with Sun's JCE framework.  You can find
 * a *free* JCE implementation with RC4 (tm) at
 * Cryptix (http://www.cryptix.org/).
 * <p>
 * Note that RC4 (tm) is a trademark of RSA Data Security, Inc.
 * Also, if you are within USA, you may need to acquire licenses from
 * RSA to use RC4.
 * Please check your local law.  The author is not
 * responsible for any illegal use of this code.
 * <p>
 * @author  Clarence Ho
 */
public class RC4 {

    private byte state[] = new byte[256];
    private int x;
    private int y;

    /**
     * Initializes the class with a string key. The length
     * of a normal key should be between 1 and 2048 bits.  But
     * this method doens't check the length at all.
     *
     * @param key   the encryption/decryption key
     */
    public RC4(String key) throws NullPointerException {
        this(key.getBytes());
    }

    /**
     * Initializes the class with a byte array key.  The length
     * of a normal key should be between 1 and 2048 bits.  But
     * this method doens't check the length at all.
     *
     * @param key   the encryption/decryption key
     */
    public RC4(byte[] key) throws NullPointerException {

        for (int i=0; i < 256; i++) {
            state[i] = (byte)i;
        }

        x = 0;
        y = 0;

        int index1 = 0;
        int index2 = 0;

        byte tmp;

        if (key == null || key.length == 0) {
            throw new NullPointerException();
        }

        for (int i=0; i < 256; i++) {

            index2 = ((key[index1] & 0xff) + (state[i] & 0xff) + index2) & 0xff;

            tmp = state[i];
            state[i] = state[index2];
            state[index2] = tmp;

            index1 = (index1 + 1) % key.length;
        }



    }

    public RC4(byte[] state, int x, int y) {
        this.x = x;
        this.y = y;
        this.state = state;
    }

    //copyconstructor
    public RC4 deepCopy() {
        return new RC4(Arrays.copyOf(state, 256), x, y);
    }

    /**
     * RC4 encryption/decryption.
     *
     * @param data  the data to be encrypted/decrypted
     * @return the result of the encryption/decryption
     */
    public byte[] rc4(String data) {

        if (data == null) {
            return null;
        }

        byte[] tmp = data.getBytes();

        this.rc4(tmp);

        return tmp;
    }

    /**
     * RC4 encryption/decryption.
     *
     * @param buf  the data to be encrypted/decrypted
     * @return the result of the encryption/decryption
     */
    public byte[] rc4(byte[] buf) {

        //int lx = this.x;
        //int ly = this.y;

        int xorIndex;
        byte tmp;

        if (buf == null) {
            return null;
        }

        byte[] result = new byte[buf.length];

        for (int i=0; i < buf.length; i++) {

            x = (x + 1) & 0xff;
            y = ((state[x] & 0xff) + y) & 0xff;

            tmp = state[x];
            state[x] = state[y];
            state[y] = tmp;

            xorIndex = ((state[x] &0xff) + (state[y] & 0xff)) & 0xff;
            result[i] = (byte)(buf[i] ^ state[xorIndex]);
        }

        //this.x = lx;
        //this.y = ly;

        return result;
    }

    public boolean couldBeFresh() {
        return (x == 0 && y == 0);
    }

    public void undoRc4(byte[] buf) {

        byte tmp;

        for (int i = buf.length - 1; i >= 0; i--) {

            tmp = state[x];
            state[x] = state[y];
            state[y] = tmp;

            y = (y - (state[x] & 0xff)) & 0xff;
            x = (x - 1) & 0xff;
        }

    }


    public void printKey() {
        System.out.println(new HPacket(state).toString());
    }


    private static void printState(byte[] booleans) {
        StringBuilder builder = new StringBuilder("state: ");
        for (byte bool : booleans) {
            builder.append(bool);
            builder.append(",");
        }
        System.out.println(builder);
    }

    public static void main(String[] args) {

        byte[] sharedKey = new byte[27];
        List<Byte> allbytesEncrypted = new ArrayList<>();
        RC4 p1 = new RC4(sharedKey);

        System.out.println("original:");
        printState(p1.state);
        System.out.println("x: " + p1.x + ", y: " + p1.y);

        byte[] enc1 = p1.rc4("hallo dit istoch wel redelijk veel tekst ofzo denk k".getBytes());
        for (int i = 0; i < enc1.length; i++) {
            allbytesEncrypted.add(enc1[i]);
        }

        byte[] enc2 = p1.rc4("dit is ook redelijk wa tekst maar mag nog veel meer zijn eigelijk in principe hoor".getBytes());
        for (int i = 0; i < enc2.length; i++) {
            allbytesEncrypted.add(enc2[i]);
        }

        System.out.println("-----------");
        System.out.println("after being sent:");
        printState(p1.state);
        System.out.println("x: " + p1.x + ", y: " + p1.y);


        byte[] allencrypted = new byte[allbytesEncrypted.size()];
        for (int i = 0; i < allbytesEncrypted.size(); i++) {
            allencrypted[i] = allbytesEncrypted.get(i);
        }

        p1.undoRc4(allencrypted);

        System.out.println("-----------");
        System.out.println("after undo:");
        printState(p1.state);
        System.out.println("x: " + p1.x + ", y: " + p1.y);


//        byte[] sharedKey = new byte[27];
//
//        RC4 p1 = new RC4(sharedKey);
//        RC4 p2 = new RC4(sharedKey);
//
//        p1.printKey();
//        p2.printKey();
//        byte[] enc = p1.rc4("hallo".getBytes());
//        System.out.println(new String(p2.rc4(enc)));
//
//        p1.printKey();
//        p2.printKey();
//
//        enc = p1.rc4("hallo".getBytes());
//        System.out.println(new String(p2.rc4(enc)));
//
//        p1.printKey();
//        p2.printKey();
//
//        enc = p1.rc4("meneeeer dit zijn echt veel meer dan 27 characters dus latne we dit even proberen".getBytes());
//        System.out.println(new String(p2.rc4(enc)));
//
//        p1.printKey();
//        p2.printKey();
    }

//    public static void main(String[] args) {
//        byte[] plainData = "abc123".getBytes();
//        byte[] key = "Key".getBytes();
//        RC4 rc4 = new RC4(key);
//        byte[] cipherData = rc4.rc4(plainData);
//        System.out.println("加密后: " + new String(cipherData));
//        byte[] _plainData = rc4.rc4(cipherData);
//        System.out.println("解密后: " + new String(_plainData));
//        System.out.println(Arrays.equals(plainData, _plainData));
//    }

}
package org.die_fabrik.lelib.data;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * Created by Michael on 11.01.2018.
 */

public abstract class LeData {
    /**
     * The Charset to use for De/Encoding Jobs
     */
    protected final static String CHARSET = "UTF-8";
    
    /**
     * The logging TAG for this Object
     */
    protected final String TAG = this.getClass().getSimpleName();
    
    //TODO check whether its better to handle the ecxeption here?
    public LeData(byte[] leValue) throws UnsupportedEncodingException {
        if (leValue != null) {
            construct(leValue);
        }
    }
    
    public LeData() {
    }
    
    /**
     * called bye the (byte[]) constructor to fill the elements of this Object with data from Byte[]
     *
     * @param leValue the byte[] to convert
     */
    public abstract void construct(byte[] leValue) throws UnsupportedEncodingException;
    
    /**
     * converts this LeData Class into a byte[]
     *
     * @return a byte[]
     */
    public abstract byte[] getLeValue() throws UnsupportedEncodingException;
    
    /**
     * a helper method to get a String from a ByteBuffer
     *
     * @param bb the ByteBuffer
     * @return a String
     * @throws UnsupportedEncodingException by any problems regarding the String encoding
     */
    protected String getString(ByteBuffer bb) throws UnsupportedEncodingException {
        int l = bb.getInt();
        byte[] ba = new byte[l];
        bb.get(ba, bb.position(), l);
        return new String(ba, CHARSET);
    }
    
    /**
     * a helper method to put a string into a ByteBuffer
     *
     * @param bb the byteBuffer to put the String into
     * @param s  the string to wrap in the ByteBuffer
     * @return the ByteBuffer which contains the String
     * @throws UnsupportedEncodingException by any problems regarding the String encoding
     */
    protected ByteBuffer putString(ByteBuffer bb, String s) throws UnsupportedEncodingException {
        byte[] ba = s.getBytes(CHARSET);
        bb.putInt(ba.length);
        bb.put(ba);
        return bb;
    }
    
}

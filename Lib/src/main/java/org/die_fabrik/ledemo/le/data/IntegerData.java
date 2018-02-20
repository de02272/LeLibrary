package org.die_fabrik.ledemo.le.data;

import org.die_fabrik.lelib.data.LeData;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * Created by Michael on 13.01.2018.
 *
 */

public class IntegerData extends LeData {
    private int val;
    
    public IntegerData(int val) {
        super();
        this.val = val;
    }
    
    public IntegerData(byte[] leValue) throws UnsupportedEncodingException {
        super(leValue);
    }
    
    /**
     * called bye the (byte[]) constructor to fill the elements of this Object with data from Byte[]
     *
     * @param leValue the byte[] to convert
     */
    @Override
    public void constructLeData(byte[] leValue) throws UnsupportedEncodingException {
        ByteBuffer bb = ByteBuffer.wrap(leValue);
        this.val = bb.get();
    }
    
    /**
     * converts this LeData Class into a byte[]
     *
     * @return a byte[]
     */
    @Override
    public byte[] createLeValue() throws UnsupportedEncodingException {
        ByteBuffer bb = ByteBuffer.allocate(25/*Integer.SIZE / Byte.SIZE */);
        bb.put((byte) val);
        return bb.array();
    }
    
    public int getVal() {
        return val;
    }
}

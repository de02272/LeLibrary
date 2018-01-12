package org.die_fabrik.ledemo.le.data;

import org.die_fabrik.lelib.data.LeData;

import java.io.UnsupportedEncodingException;

/**
 * Created by Michael on 12.01.2018.
 */

public class RemoteDeviceNameData extends LeData {
    private String name;
    
    public RemoteDeviceNameData(String name) {
        super();
        this.name = name;
    }
    
    /**
     * called bye the (byte[]) constructor to fill the elements of this Object with data from Byte[]
     *
     * @param leValue the byte[] to convert
     */
    @Override
    public void construct(byte[] leValue) throws UnsupportedEncodingException {
        this.name=new String(leValue, CHARSET);
    }
    
    /**
     * converts this LeData Class into a byte[]
     *
     * @return a byte[]
     */
    @Override
    public byte[] getLeValue() throws UnsupportedEncodingException {
        return name.getBytes(CHARSET);
    }
}

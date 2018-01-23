package org.die_fabrik.lelib;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import org.die_fabrik.lelib.data.LeData;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michael on 11.01.2018.
 */

public class LeUtil {
    private final static int RowLength = 16;
    
    public static void checkExistence(Object o) {
        if (o == null) {
            throw new IllegalArgumentException("the given Object is null)");
        }
    }
    
    public static void checkRange(int val, int min, int max) {
        if (val < min || val > max) {
            throw new IllegalArgumentException("the Value: " + val + " is not in the given range (Min: " + min + ", Max: " + max + ")");
        }
    }
    
    public static void checkValue(int val, int... allowedValues) {
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < allowedValues.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            int cmp = allowedValues[i];
            if (val == cmp) {
                return;
            }
            sb.append(i);
        }
        throw new IllegalArgumentException("the Value: " + val + " is not in the given int values (" + sb.toString() + ")");
    }
    
    public static LeData createLeDataFromLeValue(byte[] leValue, Class<? extends LeData> cls) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        /*Log.v("Reflexion", "instantiating a LeData object with cls: " + cls.getSimpleName());
        logHexValue(leValue, "Reflexion");
        Constructor<?>[] clss = cls.getConstructors();
        for (Constructor<?> c : clss) {
            Log.v("Reflexion", "name: " + c.getName());
            Type[] ts = c.getGenericParameterTypes();
            for (Type t : ts) {
                Log.v("Reflexion", ""+t.toString());
            }
        }*/
        Constructor<? extends LeData> constructor = cls.getConstructor(byte[].class);
        LeData a = constructor.newInstance(leValue);
        return a;
    }
    
    public static String getAdvertiseFailure(Context ctx, int errorCode) {
        String msg = ctx.getString(R.string.start_error_prefix);
        switch (errorCode) {
            case AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED:
                msg += " " + ctx.getString(R.string.start_error_already_started);
                break;
            case AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE:
                msg += " " + ctx.getString(R.string.start_error_too_large);
                break;
            case AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                msg += " " + ctx.getString(R.string.start_error_unsupported);
                break;
            case AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR:
                msg += " " + ctx.getString(R.string.start_error_internal);
                break;
            case AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                msg += " " + ctx.getString(R.string.start_error_too_many);
                break;
            
            default:
                msg += " " + ctx.getString(R.string.start_error_unknown);
        }
        return msg;
        
    }
    
    public static final String getCharacteristicPermissions(Context ctx, int perm) {
        StringBuilder sb = new StringBuilder();
        translateItem(sb, perm, BluetoothGattCharacteristic.PERMISSION_READ, ctx.getString(R.string.gatt_characteristic_permission_read));
        translateItem(sb, perm, BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED, ctx.getString(R.string.gatt_characteristic_permission_read_encrypted));
        translateItem(sb, perm, BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED_MITM, ctx.getString(R.string.gatt_characteristic_permission_read_encrypted_mitm));
        translateItem(sb, perm, BluetoothGattCharacteristic.PERMISSION_WRITE, ctx.getString(R.string.gatt_characteristic_permission_write));
        translateItem(sb, perm, BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED, ctx.getString(R.string.gatt_characteristic_permission_write_encrypted));
        translateItem(sb, perm, BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED_MITM, ctx.getString(R.string.gatt_characteristic_permission_write_encrypted_mitm));
        translateItem(sb, perm, BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED, ctx.getString(R.string.gatt_characteristic_permission_write_signed));
        translateItem(sb, perm, BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED_MITM, ctx.getString(R.string.gatt_characteristic_permission_write_signed_mitm));
        return sb.toString();
    }
    
    public static final String getDescriptorPermissions(Context ctx, int perm) {
        StringBuilder sb = new StringBuilder();
        translateItem(sb, perm, BluetoothGattDescriptor.PERMISSION_READ, ctx.getString(R.string.gatt_descriptor_permission_read));
        translateItem(sb, perm, BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED, ctx.getString(R.string.gatt_descriptor_permission_read_encrypted));
        translateItem(sb, perm, BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED_MITM, ctx.getString(R.string.gatt_descriptor_permission_read_encrypted_mitm));
        translateItem(sb, perm, BluetoothGattDescriptor.PERMISSION_WRITE, ctx.getString(R.string.gatt_descriptor_permission_write));
        translateItem(sb, perm, BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED, ctx.getString(R.string.gatt_descriptor_permission_write_encrypted));
        translateItem(sb, perm, BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED_MITM, ctx.getString(R.string.gatt_descriptor_permission_write_encrypted_mitm));
        translateItem(sb, perm, BluetoothGattDescriptor.PERMISSION_WRITE_SIGNED, ctx.getString(R.string.gatt_descriptor_permission_write_signed));
        translateItem(sb, perm, BluetoothGattDescriptor.PERMISSION_WRITE_SIGNED_MITM, ctx.getString(R.string.gatt_descriptor_permission_write_signed_mitm));
        return sb.toString();
    }
    
    /**
     * This method translates the State Output of some callbacks into a human readable format
     *
     * @param state The state to translate(see BluetoothProfile.STATE_*)
     * @return The human readable expression
     */
    public static String getGattState(Context ctx, int state) {
        String s = "unknown";
        switch (state) {
            case BluetoothProfile.STATE_DISCONNECTED:
                s = "STATE_DISCONNECTED";
                break;
            
            case BluetoothProfile.STATE_CONNECTING:
                s = "STATE_CONNECTING";
                break;
            
            case BluetoothProfile.STATE_CONNECTED:
                s = "STATE_CONNECTED";
                break;
            
            case BluetoothProfile.STATE_DISCONNECTING:
                s = "STATE_DISCONNECTING";
                break;
        }
        return s + "(" + state + ")";
    }
    
    /**
     * This method translates the Status Output of some callbacks into a human readable format
     *
     * @param status The status to translate(see BluetoothGatt.GATT_*)
     * @return The human readable expression
     */
    public static String getGattStatus(Context ctx, int status) {
        String s = "unknown";
        switch (status) {
            case BluetoothGatt.GATT_SUCCESS:
                s = "GATT_SUCCESS";
                break;
            
            case BluetoothGatt.GATT_FAILURE:
                s = "GATT_FAILURE";
                break;
            
            case BluetoothGatt.GATT_READ_NOT_PERMITTED:
                s = "GATT_READ_NOT_PERMITTED";
                break;
            
            case BluetoothGatt.GATT_WRITE_NOT_PERMITTED:
                s = "GATT_WRITE_NOT_PERMITTED";
                break;
            
            case BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION:
                s = "GATT_INSUFFICIENT_AUTHENTICATION";
                break;
            
            case BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED:
                s = "GATT_REQUEST_NOT_SUPPORTED";
                break;
            
            case BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION:
                s = "GATT_INSUFFICIENT_ENCRYPTION";
                break;
            
            case BluetoothGatt.GATT_INVALID_OFFSET:
                s = "GATT_INVALID_OFFSET";
                break;
            
            case BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH:
                s = "GATT_INVALID_ATTRIBUTE_LENGTH";
                break;
            
            case BluetoothGatt.GATT_CONNECTION_CONGESTED:
                s = "GATT_CONNECTION_CONGESTED";
                break;
            
            
        }
        return s + "(" + status + ")";
    }
    
    public static final String getProperties(Context ctx, int prop) {
        StringBuilder sb = new StringBuilder();
        translateItem(sb, prop, BluetoothGattCharacteristic.PROPERTY_BROADCAST, ctx.getString(R.string.gatt_characteristic_property_broadcast));
        translateItem(sb, prop, BluetoothGattCharacteristic.PROPERTY_READ, ctx.getString(R.string.gatt_characteristic_property_read));
        translateItem(sb, prop, BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE, ctx.getString(R.string.gatt_characteristic_property_write_no_response));
        translateItem(sb, prop, BluetoothGattCharacteristic.PROPERTY_WRITE, ctx.getString(R.string.gatt_characteristic_property_write));
        translateItem(sb, prop, BluetoothGattCharacteristic.PROPERTY_NOTIFY, ctx.getString(R.string.gatt_characteristic_property_notify));
        translateItem(sb, prop, BluetoothGattCharacteristic.PROPERTY_INDICATE, ctx.getString(R.string.gatt_characteristic_property_indicate));
        translateItem(sb, prop, BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE, ctx.getString(R.string.gatt_characteristic_property_signed_write));
        translateItem(sb, prop, BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS, ctx.getString(R.string.gatt_characteristic_property_extended_props));
        return sb.toString();
    }
    
    public static String getScanCallbackType(Context ctx, int callbackType) {
        switch (callbackType) {
            case ScanSettings.CALLBACK_TYPE_ALL_MATCHES:
                return ctx.getString(R.string.callback_type_all_matches);
            
            case ScanSettings.CALLBACK_TYPE_FIRST_MATCH:
                return ctx.getString(R.string.callback_type_first_match);
            
            case ScanSettings.CALLBACK_TYPE_MATCH_LOST:
                return ctx.getString(R.string.callback_type_match_lost);
            
            default:
                return ctx.getString(R.string.callback_type_unknown);
            
        }
    }
    
    public static String getScanErrorCode(Context ctx, int errorCode) {
        switch (errorCode) {
            case ScanCallback.SCAN_FAILED_ALREADY_STARTED:
                return ctx.getString(R.string.scan_failure_already_started);
            
            case ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                return ctx.getString(R.string.scan_failure_application_registration_failed);
            
            case ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED:
                return ctx.getString(R.string.scan_failure_feature_unsupported);
            
            case ScanCallback.SCAN_FAILED_INTERNAL_ERROR:
                return ctx.getString(R.string.scan_failure_internal_error);
            
            default:
                return ctx.getString(R.string.scan_failure_unknown_error);
        }
    }
    
    public static String getScanMode(Context ctx, int scanMode) {
        switch (scanMode) {
            case ScanSettings.SCAN_MODE_BALANCED:
                return ctx.getString(R.string.scan_mode_balanced);
            
            case ScanSettings.SCAN_MODE_LOW_LATENCY:
                return ctx.getString(R.string.scan_mode_low_latency);
            
            case ScanSettings.SCAN_MODE_LOW_POWER:
                return ctx.getString(R.string.scan_mode_low_power);
            
            case ScanSettings.SCAN_MODE_OPPORTUNISTIC:
                return ctx.getString(R.string.scan_mode_opportunistic);
            default:
                return ctx.getString(R.string.scan_mode_unknown);
            
        }
    }
    
    public static String getServiceType(int serviceType) {
        StringBuilder sb = new StringBuilder();
        switch (serviceType) {
            case BluetoothGattService.SERVICE_TYPE_PRIMARY:
                sb.append("SERVICE_TYPE_PRIMARY (").append(BluetoothGattService.SERVICE_TYPE_PRIMARY).append(")");
                break;
            
            case BluetoothGattService.SERVICE_TYPE_SECONDARY:
                sb.append("SERVICE_TYPE_SECONDARY (").append(BluetoothGattService.SERVICE_TYPE_SECONDARY).append(")");
                break;
            
            default:
                sb.append("UNKNOWN (").append(serviceType).append(")");
        }
        return sb.toString();
    }
    
    public static String[] hexValue(byte[] value) {
        List<String> lines = new ArrayList<>();
        if (value != null) {
            int valueLength = value.length;
            int rest = valueLength % RowLength;
            int columns = (valueLength / RowLength) + (rest > 0 ? 1 : 0);
            for (int i = 0; i < columns; i++) {
                StringBuilder sb = new StringBuilder("");
                if (i == 0 && columns > 1) {
                    sb.append("[[");
                } else {
                    sb.append(" [");
                }
                
                for (int j = 0; j < RowLength; j++) {
                    int idx = i * RowLength + j;
                    if (idx < valueLength) {
                        byte b = value[idx];
                        sb.append(String.format("%02X ", b & 0xFF));
                    } else {
                        if (columns > 1) {
                            sb.append("XX ");
                        } else {
                            break;
                        }
                    }
                }
                sb.append("]");
                if (i == columns - 1 && columns > 1) {
                    sb.append("]");
                }
                lines.add(sb.toString());
            }
        } else {
            lines.add("no data in byte[] to log");
        }
        return lines.toArray(new String[lines.size()]);
    }
    
    public static String[] logAdapterCapabilities(BluetoothAdapter adapter) {
        List<String> lines = new ArrayList<>();
        lines.add("adapter.isEnabled(): " + adapter.isEnabled());
        lines.add("adapter.isDiscovering(): " + adapter.isDiscovering());
        lines.add("adapter.isMultipleAdvertisementSupported(): " + adapter.isMultipleAdvertisementSupported());
        lines.add("adapter.isOffloadedFilteringSupported(): " + adapter.isOffloadedFilteringSupported());
        lines.add("adapter.isOffloadedScanBatchingSupported(): " + adapter.isOffloadedScanBatchingSupported());
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            lines.addAll(logAdapterCapabilitiesExtended(adapter));
        }
        return lines.toArray(new String[lines.size()]);
    }
    
    @TargetApi(26)
    private static List<String> logAdapterCapabilitiesExtended(BluetoothAdapter adapter) {
        List<String> lines = new ArrayList<>();
        lines.add("adapter.isLe2MPhySupported(): " + adapter.isLe2MPhySupported());
        lines.add("adapter.isLeCodedPhySupported(): " + adapter.isLeCodedPhySupported());
        lines.add("adapter.isLeExtendedAdvertisingSupported(): " + adapter.isLeExtendedAdvertisingSupported());
        lines.add("adapter.isLePeriodicAdvertisingSupported(): " + adapter.isLePeriodicAdvertisingSupported());
        lines.add("adapter.getLeMaximumAdvertisingDataLength(): " + adapter.getLeMaximumAdvertisingDataLength());
        return lines;
    }
    
    public static void logHexValue(byte[] value, String TAG) {
        for (String line : hexValue(value)) {
            Log.v(TAG, line);
        }
        
    }
    
    public static void logLines(String[] lines, String TAG) {
        for (String line : lines) {
            Log.v(TAG, line);
        }
    }
    
    private static final void translateItem(StringBuilder sb, int prop, int comp, String val) {
        if ((prop & comp) > 0) {
            if (sb.length() > 0) {
                sb.append(" | ");
            }
            sb.append(val).append(" (").append(comp).append(")");
            ;
        }
    }
    
    public String getAdvertiserFailure(Context ctx, int errorCode) {
        switch (errorCode) {
            case AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED:
                return ctx.getString(R.string.advertise_failed_already_started);
            
            case AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE:
                return ctx.getString(R.string.advertise_failed_data_too_large);
            
            case AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                return ctx.getString(R.string.advertise_failed_feature_unsupported);
            
            case AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR:
                return ctx.getString(R.string.advertise_failed_internal_error);
            
            case AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                return ctx.getString(R.string.advertise_failed_too_many_advertisers);
            default:
                return ctx.getString(R.string.advertise_failed_unknown);
        }
    }
    
}

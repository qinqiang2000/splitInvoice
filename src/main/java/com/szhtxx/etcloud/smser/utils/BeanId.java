package com.szhtxx.etcloud.smser.utils;

import java.util.concurrent.atomic.*;
import org.slf4j.*;
import java.security.*;
import java.net.*;
import java.nio.*;
import java.util.*;
import java.lang.management.*;

public class BeanId
{
    private static final Logger log;
    private static final int LOW_ORDER_THREE_BYTES = 16777215;
    private static final char[] HEX_CHARS;
    private static final int MAC;
    private static final short PROC;
    private static final AtomicInteger NEXT_COUNTER;
    private final int timestamp;
    private final int machineIdentifier;
    private final short processIdentifier;
    private final int counter;
    
    static {
        log = LoggerFactory.getLogger((Class)BeanId.class);
        HEX_CHARS = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        NEXT_COUNTER = new AtomicInteger(new SecureRandom().nextInt());
        try {
            MAC = createMachineIdentifier();
            PROC = createProcessIdentifier();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public BeanId() {
        this.timestamp = (int)(System.currentTimeMillis() / 1000L);
        this.machineIdentifier = BeanId.MAC;
        this.processIdentifier = BeanId.PROC;
        this.counter = BeanId.NEXT_COUNTER.getAndIncrement();
    }
    
    public BeanId(final String id) {
        final ByteBuffer buffer = ByteBuffer.wrap(parseHexString(id));
        this.timestamp = makeInt(buffer.get(), buffer.get(), buffer.get(), buffer.get());
        this.machineIdentifier = makeInt((byte)0, buffer.get(), buffer.get(), buffer.get());
        this.processIdentifier = (short)makeInt((byte)0, (byte)0, buffer.get(), buffer.get());
        this.counter = makeInt((byte)0, buffer.get(), buffer.get(), buffer.get());
    }
    
    public void putToByteBuffer(final ByteBuffer buffer) {
        buffer.put(int3(this.timestamp));
        buffer.put(int2(this.timestamp));
        buffer.put(int1(this.timestamp));
        buffer.put(int0(this.timestamp));
        buffer.put(int2(this.machineIdentifier));
        buffer.put(int1(this.machineIdentifier));
        buffer.put(int0(this.machineIdentifier));
        buffer.put(short1(this.processIdentifier));
        buffer.put(short0(this.processIdentifier));
        buffer.put(int2(this.counter));
        buffer.put(int1(this.counter));
        buffer.put(int0(this.counter));
    }
    
    public int getTimestamp() {
        return this.timestamp;
    }
    
    public int getMachineIdentifier() {
        return this.machineIdentifier;
    }
    
    public short getProcessIdentifier() {
        return this.processIdentifier;
    }
    
    public int getCounter() {
        return this.counter;
    }
    
    @Override
    public String toString() {
        return this.toHexString();
    }
    
    public String toHexString() {
        final char[] chars = new char[24];
        int i = 0;
        final ByteBuffer buffer = ByteBuffer.allocate(12);
        this.putToByteBuffer(buffer);
        byte[] array;
        for (int length = (array = buffer.array()).length, j = 0; j < length; ++j) {
            final byte b = array[j];
            chars[i++] = BeanId.HEX_CHARS[b >> 4 & 0xF];
            chars[i++] = BeanId.HEX_CHARS[b & 0xF];
        }
        return new String(chars);
    }
    
    public static String generateUUId() {
        return new BeanId().toString();
    }
    
    private static int createMachineIdentifier() {
        int machinePiece;
        try {
            final StringBuilder sb = new StringBuilder();
            final Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                final NetworkInterface ni = e.nextElement();
                sb.append(ni.toString());
                final byte[] mac = ni.getHardwareAddress();
                if (mac != null) {
                    final ByteBuffer bb = ByteBuffer.wrap(mac);
                    try {
                        sb.append(bb.getChar());
                        sb.append(bb.getChar());
                        sb.append(bb.getChar());
                    }
                    catch (BufferUnderflowException ex) {}
                }
            }
            machinePiece = sb.toString().hashCode();
        }
        catch (Throwable t) {
            machinePiece = new SecureRandom().nextInt();
            BeanId.log.warn("Failed to get machine identifier from network interface, using random number instead", t);
        }
        machinePiece &= 0xFFFFFF;
        return machinePiece;
    }
    
    private static short createProcessIdentifier() {
        short processId;
        try {
            final String processName = ManagementFactory.getRuntimeMXBean().getName();
            if (processName.contains("@")) {
                processId = (short)Integer.parseInt(processName.substring(0, processName.indexOf(64)));
            }
            else {
                processId = (short)ManagementFactory.getRuntimeMXBean().getName().hashCode();
            }
        }
        catch (Throwable t) {
            processId = (short)new SecureRandom().nextInt();
            BeanId.log.warn("Failed to get stub identifier from JMX, using random number instead", t);
        }
        return processId;
    }
    
    private static byte[] parseHexString(final String s) {
        if (!isValid(s)) {
            throw new IllegalArgumentException("invalid hexadecimal representation of an ObjectId: [" + s + "]");
        }
        final byte[] b = new byte[12];
        for (int i = 0; i < b.length; ++i) {
            b[i] = (byte)Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16);
        }
        return b;
    }
    
    public static boolean isValid(final String hexString) {
        if (hexString == null) {
            throw new IllegalArgumentException();
        }
        final int len = hexString.length();
        if (len != 24) {
            return false;
        }
        for (int i = 0; i < len; ++i) {
            final char c = hexString.charAt(i);
            if (c < '0' || c > '9') {
                if (c < 'a' || c > 'f') {
                    if (c < 'A' || c > 'F') {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    private static int makeInt(final byte b3, final byte b2, final byte b1, final byte b0) {
        return b3 << 24 | (b2 & 0xFF) << 16 | (b1 & 0xFF) << 8 | (b0 & 0xFF);
    }
    
    private static byte int3(final int x) {
        return (byte)(x >> 24);
    }
    
    private static byte int2(final int x) {
        return (byte)(x >> 16);
    }
    
    private static byte int1(final int x) {
        return (byte)(x >> 8);
    }
    
    private static byte int0(final int x) {
        return (byte)x;
    }
    
    private static byte short1(final short x) {
        return (byte)(x >> 8);
    }
    
    private static byte short0(final short x) {
        return (byte)x;
    }
    
    public static void main(final String[] args) throws Exception {
        for (int i = 0; i < 100; ++i) {
            final Integer s = timestamp10();
            Thread.sleep(1000L);
        }
    }
    
    public static Integer timestamp10() {
        final Long timestamp = System.currentTimeMillis() / 1000L;
        return timestamp.intValue();
    }
}

package org.globsframework.grpc.writer;

public class AllocatedBuffer {
    private final int length;
    private int position;
    private final byte[] bytes;
    private AllocatedBuffer next;

    public AllocatedBuffer(byte[] bytes, int length) {
        this.bytes = bytes;
        this.length = length;
    }

    public byte[] array() {
        return bytes;
    }

    public int position() {
        return position;
    }

    public AllocatedBuffer position(int position) {
        if (position < 0 || position > length) {
            throw new IllegalArgumentException("Invalid position: " + position);
        }
        this.position = position;
        return this;
    }

    public int limit() {
        // Relative to offset.
        return length;
    }

    public int remaining() {
        return length - position;
    }

    public int read(byte[] b, int off, int len) {
        if (len > remaining()) {
            len = remaining();
        }
        System.arraycopy(bytes, position, b, off, len);
        position += len;
        return len;
    }

    public int read() {
        if (position >= length) {
            return -1;
        }
        return bytes[position++] & 0xFF;
    }

    public void reset() {
        position = 0;
        this.next = null;
    }

    public void setNext(AllocatedBuffer next) {
        this.next = next;
    }

    public AllocatedBuffer getNext() {
        return next;
    }

};
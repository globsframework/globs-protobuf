package org.globsframework.grpc.writer;

public class AllocatedBuffer {
    final int length;
    private int position;
    private byte[] bytes;
    private int offset;

    public AllocatedBuffer(byte[] bytes, int offset, int length) {
        this.bytes = bytes;
        this.offset = offset;
        this.length = length;
    }

    public byte[] array() {
        return bytes;
    }

    public int arrayOffset() {
        return offset;
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
};
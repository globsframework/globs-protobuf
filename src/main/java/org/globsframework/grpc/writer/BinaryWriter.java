// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
//
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file or at
// https://developers.google.com/open-source/licenses/bsd

// updated to match Globs framework need

package org.globsframework.grpc.writer;

import org.globsframework.core.model.Glob;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import static org.globsframework.grpc.writer.WireFormat.*;

/**
 * A protobuf writer that serializes messages in their binary form. Messages are serialized in
 * reverse in order to avoid calculating the serialized size of each nested message. Since the
 * message size is not known in advance, the writer employs a strategy of chunking and buffer
 * chaining. Buffers are allocated as-needed by a provided {@link BufferAllocator}. Once writing is
 * finished, the application can access the buffers in forward-writing order by calling {@link
 * #complete()}.
 *
 * <p>Once {@link #complete()} has been called, the writer can not be reused for additional writes.
 * The {@link #getTotalBytesWritten()} will continue to reflect the total of the write and will not
 * be reset.
 */

public abstract class BinaryWriter implements Writer {
    public static final int DEFAULT_CHUNK_SIZE = 4096;

    private final BufferAllocator alloc;
    private final int chunkSize;
    final GrpcBinWriter grpcBinWriter;

    final ArrayDeque<AllocatedBuffer> buffers = new ArrayDeque<AllocatedBuffer>(4);
    int totalDoneBytes;


    /**
     * Creates a new {@link BinaryWriter} that will allocate heap buffers of {@link
     * #DEFAULT_CHUNK_SIZE} as necessary.
     */
    public static BinaryWriter newHeapInstance(BufferAllocator alloc, GrpcBinWriter grpcBinWriter) {
        return newHeapInstance(alloc, grpcBinWriter, DEFAULT_CHUNK_SIZE);
    }

    /**
     * Creates a new {@link BinaryWriter} that will allocate heap buffers of {@code chunkSize} as
     * necessary.
     */
    public static BinaryWriter newHeapInstance(BufferAllocator alloc, GrpcBinWriter grpcBinWriter, int chunkSize) {
        return new SafeHeapWriter(alloc, grpcBinWriter, chunkSize);
    }

    /**
     * Only allow subclassing for inner classes.
     */
    private BinaryWriter(BufferAllocator alloc, GrpcBinWriter grpcBinWriter, int chunkSize) {
        this.alloc = alloc;
        this.grpcBinWriter = grpcBinWriter;
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("chunkSize must be > 0");
        }
        this.chunkSize = chunkSize;
    }

    public final FieldOrder fieldOrder() {
        return FieldOrder.DESCENDING;
    }

    /**
     * Completes the write operation and returns a queue of {@link AllocatedBuffer} objects in
     * forward-writing order. This method should only be called once.
     *
     * <p>After calling this method, the writer can not be reused. Create a new writer for future
     * writes.
     */
    public final Queue<AllocatedBuffer> complete() {
        finishCurrentBuffer();
        return buffers;
    }

    public final void writeSFixed32(int fieldNumber, int value) throws IOException {
        writeFixed32(fieldNumber, value);
    }

    public final void writeInt64(int fieldNumber, long value) throws IOException {
        writeUInt64(fieldNumber, value);
    }

    public final void writeSFixed64(int fieldNumber, long value) throws IOException {
        writeFixed64(fieldNumber, value);
    }


    public final void writeFloat(int fieldNumber, float value) throws IOException {
        writeFixed32(fieldNumber, Float.floatToRawIntBits(value));
    }

    public final void writeDouble(int fieldNumber, double value) throws IOException {
        writeFixed64(fieldNumber, Double.doubleToRawLongBits(value));
    }

    public final void writeEnum(int fieldNumber, int value) throws IOException {
        writeInt32(fieldNumber, value);
    }

    public final void writeInt32List(int fieldNumber, int[] list, boolean packed)
            throws IOException {
        writeInt32List_Internal(fieldNumber, list, packed);
    }

    private void writeInt32List_Internal(int fieldNumber, int[] list, boolean packed)
            throws IOException {
        if (packed) {
            requireSpace((MAX_VARINT32_SIZE * 2) + (list.length * MAX_VARINT64_SIZE));
            int prevBytes = getTotalBytesWritten();
            for (int i = list.length - 1; i >= 0; --i) {
                writeInt32(list[i]);
            }
            int length = getTotalBytesWritten() - prevBytes;
            writeVarint32(length);
            writeTag(fieldNumber, WIRETYPE_LENGTH_DELIMITED);
        } else {
            for (int i = list.length - 1; i >= 0; --i) {
                writeInt32(fieldNumber, list[i]);
            }
        }
    }

    public final void writeFixed32List(int fieldNumber, int[] list, boolean packed)
            throws IOException {
        writeFixed32List_Internal(fieldNumber, list, packed);
    }

    private void writeFixed32List_Internal(int fieldNumber, int[] list, boolean packed)
            throws IOException {
        if (packed) {
            requireSpace((MAX_VARINT32_SIZE * 2) + (list.length * FIXED32_SIZE));
            int prevBytes = getTotalBytesWritten();
            for (int i = list.length - 1; i >= 0; --i) {
                writeFixed32(list[i]);
            }
            int length = getTotalBytesWritten() - prevBytes;
            writeVarint32(length);
            writeTag(fieldNumber, WIRETYPE_LENGTH_DELIMITED);
        } else {
            for (int i = list.length - 1; i >= 0; --i) {
                writeFixed32(fieldNumber, list[i]);
            }
        }
    }

    public final void writeInt64List(int fieldNumber, long[] list, boolean packed)
            throws IOException {
        writeUInt64List(fieldNumber, list, packed);
    }

    public final void writeUInt64List(int fieldNumber, long[] list, boolean packed)
            throws IOException {
        writeUInt64List_Internal(fieldNumber, list, packed);
    }

    private void writeUInt64List_Internal(int fieldNumber, long[] list, boolean packed)
            throws IOException {
        if (packed) {
            requireSpace((MAX_VARINT32_SIZE * 2) + (list.length * MAX_VARINT64_SIZE));
            int prevBytes = getTotalBytesWritten();
            for (int i = list.length - 1; i >= 0; --i) {
                writeVarint64(list[i]);
            }
            int length = getTotalBytesWritten() - prevBytes;
            writeVarint32(length);
            writeTag(fieldNumber, WIRETYPE_LENGTH_DELIMITED);
        } else {
            for (int i = list.length - 1; i >= 0; --i) {
                writeUInt64(fieldNumber, list[i]);
            }
        }
    }

    public final void writeFixed64List(int fieldNumber, long[] list, boolean packed)
            throws IOException {
        writeFixed64List_Internal(fieldNumber, list, packed);
    }

    private void writeFixed64List_Internal(int fieldNumber, long[] list, boolean packed)
            throws IOException {
        if (packed) {
            requireSpace((MAX_VARINT32_SIZE * 2) + (list.length * FIXED64_SIZE));
            int prevBytes = getTotalBytesWritten();
            for (int i = list.length - 1; i >= 0; --i) {
                writeFixed64(list[i]);
            }
            int length = getTotalBytesWritten() - prevBytes;
            writeVarint32(length);
            writeTag(fieldNumber, WIRETYPE_LENGTH_DELIMITED);
        } else {
            for (int i = list.length - 1; i >= 0; --i) {
                writeFixed64(fieldNumber, list[i]);
            }
        }
    }

    public final void writeFloatList(int fieldNumber, double[] list, boolean packed)
            throws IOException {
        writeFloatList_Internal(fieldNumber, list, packed);
    }

    private void writeFloatList_Internal(int fieldNumber, double[] list, boolean packed)
            throws IOException {
        if (packed) {
            requireSpace((MAX_VARINT32_SIZE * 2) + (list.length * FIXED32_SIZE));
            int prevBytes = getTotalBytesWritten();
            for (int i = list.length - 1; i >= 0; --i) {
                writeFixed32(Float.floatToRawIntBits((float) list[i])); //pas de MathExact?
            }
            int length = getTotalBytesWritten() - prevBytes;
            writeVarint32(length);
            writeTag(fieldNumber, WIRETYPE_LENGTH_DELIMITED);
        } else {
            for (int i = list.length - 1; i >= 0; --i) {
                writeFloat(fieldNumber, (float) list[i]);
            }
        }
    }

    public final void writeDoubleList(int fieldNumber, double[] list, boolean packed)
            throws IOException {
        writeDoubleList_Internal(fieldNumber, list, packed);
    }

    private void writeDoubleList_Internal(int fieldNumber, double[] list, boolean packed)
            throws IOException {
        if (packed) {
            requireSpace((MAX_VARINT32_SIZE * 2) + (list.length * FIXED64_SIZE));
            int prevBytes = getTotalBytesWritten();
            for (int i = list.length - 1; i >= 0; --i) {
                writeFixed64(Double.doubleToRawLongBits(list[i]));
            }
            int length = getTotalBytesWritten() - prevBytes;
            writeVarint32(length);
            writeTag(fieldNumber, WIRETYPE_LENGTH_DELIMITED);
        } else {
            for (int i = list.length - 1; i >= 0; --i) {
                writeDouble(fieldNumber, list[i]);
            }
        }
    }

    public final void writeEnumList(int fieldNumber, int[] list, boolean packed)
            throws IOException {
        writeInt32List(fieldNumber, list, packed);
    }

    public final void writeBoolList(int fieldNumber, boolean[] list, boolean packed)
            throws IOException {
        writeBoolList_Internal(fieldNumber, list, packed);
    }

    private void writeBoolList_Internal(int fieldNumber, boolean[] list, boolean packed)
            throws IOException {
        if (packed) {
            requireSpace((MAX_VARINT32_SIZE * 2) + list.length);
            int prevBytes = getTotalBytesWritten();
            for (int i = list.length - 1; i >= 0; --i) {
                writeBool(list[i]);
            }
            int length = getTotalBytesWritten() - prevBytes;
            writeVarint32(length);
            writeTag(fieldNumber, WIRETYPE_LENGTH_DELIMITED);
        } else {
            for (int i = list.length - 1; i >= 0; --i) {
                writeBool(fieldNumber, list[i]);
            }
        }
    }

    public final void writeStringList(int fieldNumber, String[] list) throws IOException {
        for (int i = list.length - 1; i >= 0; i--) {
            writeString(fieldNumber, list[i]);
        }
    }

    public final void writeBytesList(int fieldNumber, List<byte[]> list) throws IOException {
        for (int i = list.size() - 1; i >= 0; i--) {
            writeBytes(fieldNumber, list.get(i));
        }
    }

    public final void writeUInt32List(int fieldNumber, List<Integer> list, boolean packed)
            throws IOException {
        writeUInt32List_Internal(fieldNumber, list, packed);
    }

    private void writeUInt32List_Internal(int fieldNumber, List<Integer> list, boolean packed)
            throws IOException {
        if (packed) {
            requireSpace((MAX_VARINT32_SIZE * 2) + (list.size() * MAX_VARINT32_SIZE));
            int prevBytes = getTotalBytesWritten();
            for (int i = list.size() - 1; i >= 0; --i) {
                writeVarint32(list.get(i));
            }
            int length = getTotalBytesWritten() - prevBytes;
            writeVarint32(length);
            writeTag(fieldNumber, WIRETYPE_LENGTH_DELIMITED);
        } else {
            for (int i = list.size() - 1; i >= 0; --i) {
                writeUInt32(fieldNumber, list.get(i));
            }
        }
    }


    public final void writeSFixed32List(int fieldNumber, int[] list, boolean packed)
            throws IOException {
        writeFixed32List(fieldNumber, list, packed);
    }


    public final void writeSFixed64List(int fieldNumber, long[] list, boolean packed)
            throws IOException {
        writeFixed64List(fieldNumber, list, packed);
    }


    public final void writeSInt32List(int fieldNumber, int[] list, boolean packed)
            throws IOException {
        writeSInt32List_Internal(fieldNumber, list, packed);
    }

    private void writeSInt32List_Internal(int fieldNumber, int[] list, boolean packed)
            throws IOException {
        if (packed) {
            requireSpace((MAX_VARINT32_SIZE * 2) + (list.length * MAX_VARINT32_SIZE));
            int prevBytes = getTotalBytesWritten();
            for (int i = list.length - 1; i >= 0; --i) {
                writeSInt32(list[i]);
            }
            int length = getTotalBytesWritten() - prevBytes;
            writeVarint32(length);
            writeTag(fieldNumber, WIRETYPE_LENGTH_DELIMITED);
        } else {
            for (int i = list.length - 1; i >= 0; --i) {
                writeSInt32(fieldNumber, list[i]);
            }
        }
    }


    public final void writeSInt64List(int fieldNumber, long[] list, boolean packed)
            throws IOException {
        writeSInt64List_Internal(fieldNumber, list, packed);
    }

    private void writeSInt64List_Internal(int fieldNumber, long[] list, boolean packed)
            throws IOException {
        if (packed) {
            requireSpace((MAX_VARINT32_SIZE * 2) + (list.length * MAX_VARINT64_SIZE));
            int prevBytes = getTotalBytesWritten();
            for (int i = list.length - 1; i >= 0; --i) {
                writeSInt64(list[i]);
            }
            int length = getTotalBytesWritten() - prevBytes;
            writeVarint32(length);
            writeTag(fieldNumber, WIRETYPE_LENGTH_DELIMITED);
        } else {
            for (int i = list.length - 1; i >= 0; --i) {
                writeSInt64(fieldNumber, list[i]);
            }
        }
    }

    public final void writeMessageList(int fieldNumber, Glob[] list) throws IOException {
        for (int i = list.length - 1; i >= 0; i--) {
            writeMessage(fieldNumber, list[i]);
        }
    }

    public final void writeMessageList(int fieldNumber, Glob[] list, ProtoBufGlobSerializer globSerializer) throws IOException {
        for (int i = list.length - 1; i >= 0; i--) {
            writeMessage(fieldNumber, list[i], globSerializer);
        }
    }

    final AllocatedBuffer newHeapBuffer() {
        return alloc.allocateHeapBuffer(chunkSize);
    }

    final AllocatedBuffer newHeapBuffer(int capacity) {
        return alloc.allocateHeapBuffer(Math.max(capacity, chunkSize));
    }

    /**
     * Gets the total number of bytes that have been written. This will not be reset by a call to
     * {@link #complete()}.
     */
    public abstract int getTotalBytesWritten();

    abstract void requireSpace(int size);

    abstract void finishCurrentBuffer();

    abstract void writeTag(int fieldNumber, int wireType);

    abstract void writeVarint32(int value);

    abstract void writeInt32(int value);

    abstract void writeSInt32(int value);

    abstract void writeFixed32(int value);

    abstract void writeVarint64(long value);

    abstract void writeSInt64(long value);

    abstract void writeFixed64(long value);

    abstract void writeBool(boolean value);

    abstract void writeString(String in);

    /**
     * Not using the version in CodedOutputStream due to the fact that benchmarks have shown a
     * performance improvement when returning a byte (rather than an int).
     */
    private static byte computeUInt64SizeNoTag(long value) {
        // handle two popular special cases up front ...
        if ((value & (~0L << 7)) == 0L) {
            // Byte 1
            return 1;
        }
        if (value < 0L) {
            // Byte 10
            return 10;
        }
        // ... leaving us with 8 remaining, which we can divide and conquer
        byte n = 2;
        if ((value & (~0L << 35)) != 0L) {
            // Byte 6-9
            n += 4; // + (value >>> 63);
            value >>>= 28;
        }
        if ((value & (~0L << 21)) != 0L) {
            // Byte 4-5 or 8-9
            n += 2;
            value >>>= 14;
        }
        if ((value & (~0L << 14)) != 0L) {
            // Byte 3 or 7
            n += 1;
        }
        return n;
    }

    /**
     * Writer that uses safe operations on target array.
     */
    private static final class SafeHeapWriter extends BinaryWriter {
        private AllocatedBuffer allocatedBuffer;
        private byte[] buffer;
        private int offset;
        private int limit;
        private int offsetMinusOne;
        private int limitMinusOne;
        private int pos;

        SafeHeapWriter(BufferAllocator alloc, GrpcBinWriter grpcBinWriter, int chunkSize) {
            super(alloc, grpcBinWriter, chunkSize);
            nextBuffer();
        }


        void finishCurrentBuffer() {
            if (allocatedBuffer != null) {
                totalDoneBytes += bytesWrittenToCurrentBuffer();
                allocatedBuffer.position((pos - allocatedBuffer.arrayOffset()) + 1);
                allocatedBuffer = null;
                pos = 0;
                limitMinusOne = 0;
            }
        }

        private void nextBuffer() {
            nextBuffer(newHeapBuffer());
        }

        private void nextBuffer(int capacity) {
            nextBuffer(newHeapBuffer(capacity));
        }

        private void nextBuffer(AllocatedBuffer allocatedBuffer) {

            finishCurrentBuffer();

            buffers.addFirst(allocatedBuffer);

            this.allocatedBuffer = allocatedBuffer;
            this.buffer = allocatedBuffer.array();
            int arrayOffset = allocatedBuffer.arrayOffset();
            this.limit = arrayOffset + allocatedBuffer.limit();
            this.offset = arrayOffset + allocatedBuffer.position();
            this.offsetMinusOne = offset - 1;
            this.limitMinusOne = limit - 1;
            this.pos = limitMinusOne;
        }

        public int getTotalBytesWritten() {
            return totalDoneBytes + bytesWrittenToCurrentBuffer();
        }

        int bytesWrittenToCurrentBuffer() {
            return limitMinusOne - pos;
        }

        int spaceLeft() {
            return pos - offsetMinusOne;
        }


        public void writeUInt32(int fieldNumber, int value) throws IOException {
            requireSpace(MAX_VARINT32_SIZE * 2);
            writeVarint32(value);
            writeTag(fieldNumber, WIRETYPE_VARINT);
        }


        public void writeInt32(int fieldNumber, int value) throws IOException {
            requireSpace(MAX_VARINT32_SIZE + MAX_VARINT64_SIZE);
            writeInt32(value);
            writeTag(fieldNumber, WIRETYPE_VARINT);
        }


        public void writeSInt32(int fieldNumber, int value) throws IOException {
            requireSpace(MAX_VARINT32_SIZE * 2);
            writeSInt32(value);
            writeTag(fieldNumber, WIRETYPE_VARINT);
        }


        public void writeFixed32(int fieldNumber, int value) throws IOException {
            requireSpace(MAX_VARINT32_SIZE + FIXED32_SIZE);
            writeFixed32(value);
            writeTag(fieldNumber, WIRETYPE_FIXED32);
        }


        public void writeUInt64(int fieldNumber, long value) throws IOException {
            requireSpace(MAX_VARINT32_SIZE + MAX_VARINT64_SIZE);
            writeVarint64(value);
            writeTag(fieldNumber, WIRETYPE_VARINT);
        }


        public void writeSInt64(int fieldNumber, long value) throws IOException {
            requireSpace(MAX_VARINT32_SIZE + MAX_VARINT64_SIZE);
            writeSInt64(value);
            writeTag(fieldNumber, WIRETYPE_VARINT);
        }


        public void writeFixed64(int fieldNumber, long value) throws IOException {
            requireSpace(MAX_VARINT32_SIZE + FIXED64_SIZE);
            writeFixed64(value);
            writeTag(fieldNumber, WIRETYPE_FIXED64);
        }


        public void writeBool(int fieldNumber, boolean value) throws IOException {
            requireSpace(MAX_VARINT32_SIZE + 1);
            write((byte) (value ? 1 : 0));
            writeTag(fieldNumber, WIRETYPE_VARINT);
        }


        public void writeString(int fieldNumber, String value) throws IOException {
            int prevBytes = getTotalBytesWritten();
            writeString(value);
            int length = getTotalBytesWritten() - prevBytes;
            requireSpace(2 * MAX_VARINT32_SIZE);
            writeVarint32(length);
            writeTag(fieldNumber, WIRETYPE_LENGTH_DELIMITED);
        }


        public void writeBytes(int fieldNumber, byte[] value) throws IOException {
            write(value, 0, value.length);

            requireSpace(MAX_VARINT32_SIZE * 2);
            writeVarint32(value.length);
            writeTag(fieldNumber, WIRETYPE_LENGTH_DELIMITED);
        }

        public void writeMessage(int fieldNumber, Glob value, ProtoBufGlobSerializer globSerializer) throws IOException {
            int prevBytes = getTotalBytesWritten();
            globSerializer.write(value, this);
            int length = getTotalBytesWritten() - prevBytes;
            requireSpace(MAX_VARINT32_SIZE * 2);
            writeVarint32(length);
            writeTag(fieldNumber, WIRETYPE_LENGTH_DELIMITED);
        }

        public void writeMessage(int fieldNumber, Glob value) throws IOException {
            int prevBytes = getTotalBytesWritten();
            grpcBinWriter.write(value, this);
            int length = getTotalBytesWritten() - prevBytes;
            requireSpace(MAX_VARINT32_SIZE * 2);
            writeVarint32(length);
            writeTag(fieldNumber, WIRETYPE_LENGTH_DELIMITED);
        }

        void writeInt32(int value) {
            if (value >= 0) {
                writeVarint32(value);
            } else {
                writeVarint64(value);
            }
        }


        void writeSInt32(int value) {
            writeVarint32(encodeZigZag32(value));
        }


        void writeSInt64(long value) {
            writeVarint64(encodeZigZag64(value));
        }


        void writeBool(boolean value) {
            write((byte) (value ? 1 : 0));
        }


        void writeTag(int fieldNumber, int wireType) {
            writeVarint32(WireFormat.makeTag(fieldNumber, wireType));
        }


        void writeVarint32(int value) {
            if ((value & (~0 << 7)) == 0) {
                writeVarint32OneByte(value);
            } else if ((value & (~0 << 14)) == 0) {
                writeVarint32TwoBytes(value);
            } else if ((value & (~0 << 21)) == 0) {
                writeVarint32ThreeBytes(value);
            } else if ((value & (~0 << 28)) == 0) {
                writeVarint32FourBytes(value);
            } else {
                writeVarint32FiveBytes(value);
            }
        }

        private void writeVarint32OneByte(int value) {
            buffer[pos--] = (byte) value;
        }

        private void writeVarint32TwoBytes(int value) {
            buffer[pos--] = (byte) (value >>> 7);
            buffer[pos--] = (byte) ((value & 0x7F) | 0x80);
        }

        private void writeVarint32ThreeBytes(int value) {
            buffer[pos--] = (byte) (value >>> 14);
            buffer[pos--] = (byte) (((value >>> 7) & 0x7F) | 0x80);
            buffer[pos--] = (byte) ((value & 0x7F) | 0x80);
        }

        private void writeVarint32FourBytes(int value) {
            buffer[pos--] = (byte) (value >>> 21);
            buffer[pos--] = (byte) (((value >>> 14) & 0x7F) | 0x80);
            buffer[pos--] = (byte) (((value >>> 7) & 0x7F) | 0x80);
            buffer[pos--] = (byte) ((value & 0x7F) | 0x80);
        }

        private void writeVarint32FiveBytes(int value) {
            buffer[pos--] = (byte) (value >>> 28);
            buffer[pos--] = (byte) (((value >>> 21) & 0x7F) | 0x80);
            buffer[pos--] = (byte) (((value >>> 14) & 0x7F) | 0x80);
            buffer[pos--] = (byte) (((value >>> 7) & 0x7F) | 0x80);
            buffer[pos--] = (byte) ((value & 0x7F) | 0x80);
        }


        void writeVarint64(long value) {
            switch (computeUInt64SizeNoTag(value)) {
                case 1:
                    writeVarint64OneByte(value);
                    break;
                case 2:
                    writeVarint64TwoBytes(value);
                    break;
                case 3:
                    writeVarint64ThreeBytes(value);
                    break;
                case 4:
                    writeVarint64FourBytes(value);
                    break;
                case 5:
                    writeVarint64FiveBytes(value);
                    break;
                case 6:
                    writeVarint64SixBytes(value);
                    break;
                case 7:
                    writeVarint64SevenBytes(value);
                    break;
                case 8:
                    writeVarint64EightBytes(value);
                    break;
                case 9:
                    writeVarint64NineBytes(value);
                    break;
                case 10:
                    writeVarint64TenBytes(value);
                    break;
            }
        }

        private void writeVarint64OneByte(long value) {
            buffer[pos--] = (byte) value;
        }

        private void writeVarint64TwoBytes(long value) {
            buffer[pos--] = (byte) (value >>> 7);
            buffer[pos--] = (byte) (((int) value & 0x7F) | 0x80);
        }

        private void writeVarint64ThreeBytes(long value) {
            buffer[pos--] = (byte) (((int) value) >>> 14);
            buffer[pos--] = (byte) (((value >>> 7) & 0x7F) | 0x80);
            buffer[pos--] = (byte) ((value & 0x7F) | 0x80);
        }

        private void writeVarint64FourBytes(long value) {
            buffer[pos--] = (byte) (value >>> 21);
            buffer[pos--] = (byte) (((value >>> 14) & 0x7F) | 0x80);
            buffer[pos--] = (byte) (((value >>> 7) & 0x7F) | 0x80);
            buffer[pos--] = (byte) ((value & 0x7F) | 0x80);
        }

        private void writeVarint64FiveBytes(long value) {
            buffer[pos--] = (byte) (value >>> 28);
            buffer[pos--] = (byte) (((value >>> 21) & 0x7F) | 0x80);
            buffer[pos--] = (byte) (((value >>> 14) & 0x7F) | 0x80);
            buffer[pos--] = (byte) (((value >>> 7) & 0x7F) | 0x80);
            buffer[pos--] = (byte) ((value & 0x7F) | 0x80);
        }

        private void writeVarint64SixBytes(long value) {
            buffer[pos--] = (byte) (value >>> 35);
            buffer[pos--] = (byte) (((value >>> 28) & 0x7F) | 0x80);
            buffer[pos--] = (byte) (((value >>> 21) & 0x7F) | 0x80);
            buffer[pos--] = (byte) (((value >>> 14) & 0x7F) | 0x80);
            buffer[pos--] = (byte) (((value >>> 7) & 0x7F) | 0x80);
            buffer[pos--] = (byte) ((value & 0x7F) | 0x80);
        }

        private void writeVarint64SevenBytes(long value) {
            buffer[pos--] = (byte) (value >>> 42);
            buffer[pos--] = (byte) (((value >>> 35) & 0x7F) | 0x80);
            buffer[pos--] = (byte) (((value >>> 28) & 0x7F) | 0x80);
            buffer[pos--] = (byte) (((value >>> 21) & 0x7F) | 0x80);
            buffer[pos--] = (byte) (((value >>> 14) & 0x7F) | 0x80);
            buffer[pos--] = (byte) (((value >>> 7) & 0x7F) | 0x80);
            buffer[pos--] = (byte) ((value & 0x7F) | 0x80);
        }

        private void writeVarint64EightBytes(long value) {
            buffer[pos--] = (byte) (value >>> 49);
            buffer[pos--] = (byte) (((value >>> 42) & 0x7F) | 0x80);
            buffer[pos--] = (byte) (((value >>> 35) & 0x7F) | 0x80);
            buffer[pos--] = (byte) (((value >>> 28) & 0x7F) | 0x80);
            buffer[pos--] = (byte) (((value >>> 21) & 0x7F) | 0x80);
            buffer[pos--] = (byte) (((value >>> 14) & 0x7F) | 0x80);
            buffer[pos--] = (byte) (((value >>> 7) & 0x7F) | 0x80);
            buffer[pos--] = (byte) ((value & 0x7F) | 0x80);
        }

        private void writeVarint64NineBytes(long value) {
            buffer[pos--] = (byte) (value >>> 56);
            buffer[pos--] = (byte) (((value >>> 49) & 0x7F) | 0x80);
            buffer[pos--] = (byte) (((value >>> 42) & 0x7F) | 0x80);
            buffer[pos--] = (byte) (((value >>> 35) & 0x7F) | 0x80);
            buffer[pos--] = (byte) (((value >>> 28) & 0x7F) | 0x80);
            buffer[pos--] = (byte) (((value >>> 21) & 0x7F) | 0x80);
            buffer[pos--] = (byte) (((value >>> 14) & 0x7F) | 0x80);
            buffer[pos--] = (byte) (((value >>> 7) & 0x7F) | 0x80);
            buffer[pos--] = (byte) ((value & 0x7F) | 0x80);
        }

        private void writeVarint64TenBytes(long value) {
            buffer[pos--] = (byte) (value >>> 63);
            buffer[pos--] = (byte) (((value >>> 56) & 0x7F) | 0x80);
            buffer[pos--] = (byte) (((value >>> 49) & 0x7F) | 0x80);
            buffer[pos--] = (byte) (((value >>> 42) & 0x7F) | 0x80);
            buffer[pos--] = (byte) (((value >>> 35) & 0x7F) | 0x80);
            buffer[pos--] = (byte) (((value >>> 28) & 0x7F) | 0x80);
            buffer[pos--] = (byte) (((value >>> 21) & 0x7F) | 0x80);
            buffer[pos--] = (byte) (((value >>> 14) & 0x7F) | 0x80);
            buffer[pos--] = (byte) (((value >>> 7) & 0x7F) | 0x80);
            buffer[pos--] = (byte) ((value & 0x7F) | 0x80);
        }


        void writeFixed32(int value) {
            buffer[pos--] = (byte) (value >> 24);
            buffer[pos--] = (byte) (value >> 16);
            buffer[pos--] = (byte) (value >> 8);
            buffer[pos--] = (byte) value;
        }


        void writeFixed64(long value) {
            buffer[pos--] = (byte) ((int) (value >> 56));
            buffer[pos--] = (byte) ((int) (value >> 48));
            buffer[pos--] = (byte) ((int) (value >> 40));
            buffer[pos--] = (byte) ((int) (value >> 32));
            buffer[pos--] = (byte) ((int) (value >> 24));
            buffer[pos--] = (byte) ((int) (value >> 16));
            buffer[pos--] = (byte) ((int) (value >> 8));
            buffer[pos--] = (byte) ((int) value);
        }


        void writeString(String in) {
            // Request enough space to write the ASCII string.
            requireSpace(in.length());

            // We know the buffer is big enough...
            int i = in.length() - 1;
            // Set pos to the start of the ASCII string.
            pos -= i;
            // Designed to take advantage of
            // https://wiki.openjdk.java.net/display/HotSpotInternals/RangeCheckElimination
            for (char c; i >= 0 && (c = in.charAt(i)) < 0x80; i--) {
                buffer[pos + i] = (byte) c;
            }
            if (i == -1) {
                // Move pos past the String.
                pos -= 1;
                return;
            }
            pos += i;
            for (char c; i >= 0; i--) {
                c = in.charAt(i);
                if (c < 0x80 && pos > offsetMinusOne) {
                    buffer[pos--] = (byte) c;
                } else if (c < 0x800 && pos > offset) { // 11 bits, two UTF-8 bytes
                    buffer[pos--] = (byte) (0x80 | (0x3F & c));
                    buffer[pos--] = (byte) ((0xF << 6) | (c >>> 6));
                } else if ((c < Character.MIN_SURROGATE || Character.MAX_SURROGATE < c)
                           && pos > (offset + 1)) {
                    // Maximum single-char code point is 0xFFFF, 16 bits, three UTF-8 bytes
                    buffer[pos--] = (byte) (0x80 | (0x3F & c));
                    buffer[pos--] = (byte) (0x80 | (0x3F & (c >>> 6)));
                    buffer[pos--] = (byte) ((0xF << 5) | (c >>> 12));
                } else if (pos > (offset + 2)) {
                    // Minimum code point represented by a surrogate pair is 0x10000, 17 bits,
                    // four UTF-8 bytes
                    char high = 0;
                    if (i == 0 || !Character.isSurrogatePair(high = in.charAt(i - 1), c)) {
                        throw new RuntimeException("UnpairedSurrogateException " + (i - 1) + " " + i);
                    }
                    i--;
                    int codePoint = Character.toCodePoint(high, c);
                    buffer[pos--] = (byte) (0x80 | (0x3F & codePoint));
                    buffer[pos--] = (byte) (0x80 | (0x3F & (codePoint >>> 6)));
                    buffer[pos--] = (byte) (0x80 | (0x3F & (codePoint >>> 12)));
                    buffer[pos--] = (byte) ((0xF << 4) | (codePoint >>> 18));
                } else {
                    // Buffer is full - allocate a new one and revisit the current character.
                    requireSpace(i);
                    i++;
                }
            }
        }

        public void write(byte value) {
            buffer[pos--] = value;
        }

        public void write(byte[] value, int offset, int length) {
            if (spaceLeft() < length) {
                nextBuffer(length);
            }

            pos -= length;
            System.arraycopy(value, offset, buffer, pos + 1, length);
        }


        void requireSpace(int size) {
            if (spaceLeft() < size) {
                nextBuffer(size);
            }
        }
    }

    public static int encodeZigZag32(final int n) {
        return n << 1 ^ n >> 31;
    }

    public static long encodeZigZag64(final long n) {
        return n << 1 ^ n >> 63;
    }

}

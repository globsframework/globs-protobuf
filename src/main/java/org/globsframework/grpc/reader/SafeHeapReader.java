// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
//
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file or at
// https://developers.google.com/open-source/licenses/bsd

// updated to match Globs framework need

package org.globsframework.grpc.reader;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.model.Glob;
import org.globsframework.core.model.GlobInstantiator;
import org.globsframework.core.model.MutableGlob;
import org.globsframework.grpc.writer.WireFormat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.globsframework.grpc.writer.WireFormat.*;

public class SafeHeapReader {

    private static final int FIXED32_MULTIPLE_MASK = WireFormat.FIXED32_SIZE - 1;
    private static final int FIXED64_MULTIPLE_MASK = WireFormat.FIXED64_SIZE - 1;

    private static final int[] emptyInt = new int[0];
    private static final long[] emptyLong = new long[0];
    private static final double[] emptyDouble = new double[0];

    private final boolean bufferIsImmutable;
    private final byte[] buffer;
    private int pos;
    private final int initialPos;
    private int limit;
    private int tag;
    private int endGroupTag;
    private int[] intBuffers = emptyInt; //new int[16];
    private long[] longBuffers = emptyLong; // new long[16];
    private double[] doubleBuffers = emptyDouble; //new double[16];


    public SafeHeapReader(ByteBuffer bytebuf, boolean bufferIsImmutable) {
        this.bufferIsImmutable = bufferIsImmutable;
        buffer = bytebuf.array();
        initialPos = pos = bytebuf.arrayOffset() + bytebuf.position();
        limit = bytebuf.arrayOffset() + bytebuf.limit();
    }

    private boolean isAtEnd() {
        return pos == limit;
    }

    public int getTotalBytesRead() {
        return pos - initialPos;
    }

    public int getFieldNumber() throws IOException {
        if (isAtEnd()) {
            return Integer.MAX_VALUE;
        }
        tag = readVarint32();
        if (tag == endGroupTag) {
            return Integer.MAX_VALUE;
        }
        return WireFormat.getTagFieldNumber(tag);
    }

    public int getTag() {
        return tag;
    }

    public boolean skipField() throws IOException {
        if (isAtEnd() || tag == endGroupTag) {
            return false;
        }

        switch (WireFormat.getTagWireType(tag)) {
            case WIRETYPE_VARINT:
                skipVarint();
                return true;
            case WIRETYPE_FIXED64:
                skipBytes(FIXED64_SIZE);
                return true;
            case WIRETYPE_LENGTH_DELIMITED:
                skipBytes(readVarint32());
                return true;
            case WIRETYPE_FIXED32:
                skipBytes(FIXED32_SIZE);
                return true;
            case WIRETYPE_START_GROUP:
                skipGroup();
                return true;
            default:
                throw new RuntimeException("InvalidProtocolBufferException.invalidWireType");
        }
    }

    public double readDouble() throws IOException {
        requireWireType(WIRETYPE_FIXED64);
        return Double.longBitsToDouble(readLittleEndian64());
    }

    public float readFloat() throws IOException {
        requireWireType(WIRETYPE_FIXED32);
        return Float.intBitsToFloat(readLittleEndian32());
    }

    public long readUInt64() throws IOException {
        requireWireType(WIRETYPE_VARINT);
        return readVarint64();
    }

    public long readInt64() throws IOException {
        requireWireType(WIRETYPE_VARINT);
        return readVarint64();
    }

    public int readInt32() throws IOException {
        requireWireType(WIRETYPE_VARINT);
        return readVarint32();
    }

    public long readFixed64() throws IOException {
        requireWireType(WIRETYPE_FIXED64);
        return readLittleEndian64();
    }

    public int readFixed32() throws IOException {
        requireWireType(WIRETYPE_FIXED32);
        return readLittleEndian32();
    }

    public boolean readBool() throws IOException {
        requireWireType(WIRETYPE_VARINT);
        return readVarint32() != 0;
    }

    public String readString() throws IOException {
        return readStringInternal(false);
    }

    public String readStringRequireUtf8() throws IOException {
        return readStringInternal(true);
    }

    public String readStringInternal(boolean requireUtf8) throws IOException {
        requireWireType(WIRETYPE_LENGTH_DELIMITED);
        final int size = readVarint32();
        if (size == 0) {
            return "";
        }

        requireBytes(size);
        // TODO check
//        if (requireUtf8 && !Utf8.isValidUtf8(buffer, pos, pos + size)) {
//            throw InvalidProtocolBufferException.invalidUtf8();
//        }
        String result = new String(buffer, pos, size, StandardCharsets.UTF_8);
        pos += size;
        return result;
    }


    public Glob readMessage(GlobInstantiator instantiator, GlobType type, ProtoBufGlobDeserializer globDeserializer)
            throws IOException {
        requireWireType(WIRETYPE_LENGTH_DELIMITED);
        return readMessageNoTagCheck(instantiator, type, globDeserializer);
    }

    private MutableGlob readMessageNoTagCheck(GlobInstantiator instantiator, GlobType type, ProtoBufGlobDeserializer globDeserializer) throws IOException {
        int size = readVarint32();
        requireBytes(size);

        // Update the limit.
        int prevLimit = limit;
        int newLimit = pos + size;
        limit = newLimit;

        try {
            final MutableGlob mutableGlob = instantiator.newGlob(type);
            globDeserializer.read(mutableGlob, this);
            if (pos != newLimit) {
                throw InvalidProtocolBufferException.parseFailure();
            }
            return mutableGlob;
        } finally {
            // Restore the limit.
            limit = prevLimit;
        }
    }


    public byte[] readBytes() throws IOException {
        requireWireType(WIRETYPE_LENGTH_DELIMITED);
        int size = readVarint32();
        if (size == 0) {
            return new byte[0];
        }

        requireBytes(size);
        final byte[] ts = Arrays.copyOfRange(buffer, pos, size);
        pos += size;
        return ts;
    }


    public int readUInt32() throws IOException {
        requireWireType(WIRETYPE_VARINT);
        return readVarint32();
    }


    public int readEnum() throws IOException {
        requireWireType(WIRETYPE_VARINT);
        return readVarint32();
    }


    public int readSFixed32() throws IOException {
        requireWireType(WIRETYPE_FIXED32);
        return readLittleEndian32();
    }


    public long readSFixed64() throws IOException {
        requireWireType(WIRETYPE_FIXED64);
        return readLittleEndian64();
    }


    public int readSInt32() throws IOException {
        requireWireType(WIRETYPE_VARINT);
        return decodeZigZag32(readVarint32());
    }

    public static int decodeZigZag32(final int n) {
        return (n >>> 1) ^ -(n & 1);
    }

    public static long decodeZigZag64(final long n) {
        return (n >>> 1) ^ -(n & 1);
    }

    public long readSInt64() throws IOException {
        requireWireType(WIRETYPE_VARINT);
        return decodeZigZag64(readVarint64());
    }

    public double[] readDoubleList() throws IOException {
        switch (WireFormat.getTagWireType(tag)) {
            case WIRETYPE_LENGTH_DELIMITED: {
                final int bytes = readVarint32();
                verifyPackedFixed64Length(bytes);
                if (doubleBuffers.length < bytes) {
                    doubleBuffers = new double[bytes + 16];
                }
                final int fieldEndPos = pos + bytes;
                int i = 0;
                while (pos < fieldEndPos) {
                    doubleBuffers[i] = Double.longBitsToDouble(readLittleEndian64_NoCheck());
                    i++;
                }
                return Arrays.copyOf(doubleBuffers, i);
            }
            case WIRETYPE_FIXED64: {
                int i = 0;
                while (true) {
                    if (doubleBuffers.length == i) {
                        doubleBuffers = Arrays.copyOf(doubleBuffers, doubleBuffers.length + 16);
                    }
                    doubleBuffers[i] = readDouble();
                    i++;

                    if (isAtEnd()) {
                        return Arrays.copyOf(doubleBuffers, i);
                    }
                    int prevPos = pos;
                    int nextTag = readVarint32();
                    if (nextTag != tag) {
                        // We've reached the end of the repeated field. Rewind the buffer position to before
                        // the new tag.
                        pos = prevPos;
                        return Arrays.copyOf(doubleBuffers, i);
                    }
                }
            }
            default:
                throw InvalidProtocolBufferException.invalidWireType();
        }
    }


    public double[] readFloatList() throws IOException {
        switch (WireFormat.getTagWireType(tag)) {
            case WIRETYPE_LENGTH_DELIMITED: {
                final int bytes = readVarint32();
                verifyPackedFixed32Length(bytes);
                if (doubleBuffers.length < bytes) {
                    doubleBuffers = new double[bytes + 16];
                }
                final int fieldEndPos = pos + bytes;
                int i = 0;
                while (pos < fieldEndPos) {
                    doubleBuffers[i] = Float.intBitsToFloat(readLittleEndian32_NoCheck());
                    i++;
                }
                return Arrays.copyOf(doubleBuffers, i);
            }
            case WIRETYPE_FIXED32: {
                int i = 0;
                while (true) {
                    if (doubleBuffers.length == i) {
                        doubleBuffers = Arrays.copyOf(doubleBuffers, doubleBuffers.length + 16);
                    }
                    doubleBuffers[i] = readFloat();
                    i++;

                    if (isAtEnd()) {
                        return Arrays.copyOf(doubleBuffers, i);
                    }
                    int prevPos = pos;
                    int nextTag = readVarint32();
                    if (nextTag != tag) {
                        // We've reached the end of the repeated field. Rewind the buffer position to before
                        // the new tag.
                        pos = prevPos;
                        return Arrays.copyOf(doubleBuffers, i);
                    }
                }
            }
            default:
                throw InvalidProtocolBufferException.invalidWireType();
        }
    }


    public long[] readUInt64List() throws IOException {
        switch (WireFormat.getTagWireType(tag)) {
            case WIRETYPE_LENGTH_DELIMITED: {
                final int bytes = readVarint32();
                if (longBuffers.length < bytes) {
                    longBuffers = new long[bytes + 16];
                }
                final int fieldEndPos = pos + bytes;
                int i = 0;
                while (pos < fieldEndPos) {
                    longBuffers[i] = readVarint64();
                    i++;
                }
                requirePosition(fieldEndPos);
                return Arrays.copyOf(longBuffers, i);
            }
            case WIRETYPE_VARINT: {
                int i = 0;
                while (true) {
                    if (longBuffers.length == i) {
                        longBuffers = Arrays.copyOf(longBuffers, i + 16);
                    }
                    longBuffers[i] = readUInt64();

                    if (isAtEnd()) {
                        return Arrays.copyOf(longBuffers, i);
                    }
                    int prevPos = pos;
                    int nextTag = readVarint32();
                    if (nextTag != tag) {
                        // We've reached the end of the repeated field. Rewind the buffer position to before
                        // the new tag.
                        pos = prevPos;
                        return Arrays.copyOf(longBuffers, i);
                    }
                }
            }
            default:
                throw InvalidProtocolBufferException.invalidWireType();
        }
    }


    public long[] readInt64List() throws IOException {
        switch (WireFormat.getTagWireType(tag)) {
            case WIRETYPE_LENGTH_DELIMITED: {
                final int bytes = readVarint32();
                if (bytes > longBuffers.length) { // worst case
                    longBuffers = new long[bytes];
                }

                final int fieldEndPos = pos + bytes;
                int i = 0;
                while (pos < fieldEndPos) {
                    longBuffers[i] = readVarint64();
                    i++;
                }
                requirePosition(fieldEndPos);
                return Arrays.copyOf(longBuffers, i);
            }
            case WIRETYPE_VARINT: {
                int i = 0;
                while (true) {
                    if (longBuffers.length == i) {
                        longBuffers = Arrays.copyOf(longBuffers, longBuffers.length + 16);
                    }
                    longBuffers[i] = readInt64();
                    i++;

                    if (isAtEnd()) {
                        return Arrays.copyOf(longBuffers, i);
                    }
                    int prevPos = pos;
                    int nextTag = readVarint32();
                    if (nextTag != tag) {
                        // We've reached the end of the repeated field. Rewind the buffer position to before
                        // the new tag.
                        pos = prevPos;
                        return Arrays.copyOf(longBuffers, i);
                    }
                }
            }
            default:
                throw InvalidProtocolBufferException.invalidWireType();
        }
    }

    public int[] readInt32List() throws IOException {
        switch (WireFormat.getTagWireType(tag)) {
            case WIRETYPE_LENGTH_DELIMITED: {
                final int bytes = readVarint32();
                if (bytes > intBuffers.length) { // worst case
                    intBuffers = new int[bytes];
                }
                final int fieldEndPos = pos + bytes;
                int i = 0;
                while (pos < fieldEndPos) {
                    intBuffers[i] = readVarint32();
                    i++;
                }
                requirePosition(fieldEndPos);
                return Arrays.copyOf(intBuffers, i);
            }
            case WIRETYPE_VARINT: {
                int i = 0;
                while (true) {
                    if (intBuffers.length <= i) {
                        intBuffers = Arrays.copyOf(intBuffers, intBuffers.length + 16);
                    }
                    intBuffers[i] = readInt32();
                    i++;
                    if (isAtEnd()) {
                        return Arrays.copyOf(intBuffers, i);
                    }
                    int prevPos = pos;
                    int nextTag = readVarint32();
                    if (nextTag != tag) {
                        // We've reached the end of the repeated field. Rewind the buffer position to before
                        // the new tag.
                        pos = prevPos;
                        return Arrays.copyOf(intBuffers, i);
                    }
                }
            }
            default:
                throw InvalidProtocolBufferException.invalidWireType();
        }
    }

    public long[] readFixed64List() throws IOException {
        switch (WireFormat.getTagWireType(tag)) {
            case WIRETYPE_LENGTH_DELIMITED: {
                final int bytes = readVarint32();
                verifyPackedFixed64Length(bytes);
                if (bytes > this.longBuffers.length) {
                    longBuffers = new long[bytes];
                }

                final int fieldEndPos = pos + bytes;
                int i = 0;
                while (pos < fieldEndPos) {
                    longBuffers[i] = readLittleEndian64_NoCheck();
                    i++;
                }
                return Arrays.copyOf(longBuffers, i);
            }
            case WIRETYPE_FIXED64: {
                int i = 0;
                while (true) {
                    if (longBuffers.length == i) {
                        longBuffers = Arrays.copyOf(longBuffers, longBuffers.length + 16);
                    }
                    longBuffers[i] = readFixed64();
                    i++;

                    if (isAtEnd()) {
                        return Arrays.copyOf(longBuffers, i);
                    }
                    int prevPos = pos;
                    int nextTag = readVarint32();
                    if (nextTag != tag) {
                        // We've reached the end of the repeated field. Rewind the buffer position to before
                        // the new tag.
                        pos = prevPos;
                        return Arrays.copyOf(longBuffers, i);
                    }
                }
            }
            default:
                throw InvalidProtocolBufferException.invalidWireType();
        }
    }


    public int[] readFixed32List() throws IOException {
        switch (WireFormat.getTagWireType(tag)) {
            case WIRETYPE_LENGTH_DELIMITED: {
                final int bytes = readVarint32();
                if (bytes >> 2 > intBuffers.length) {
                    intBuffers = new int[bytes >> 2];
                }
                verifyPackedFixed32Length(bytes);
                final int fieldEndPos = pos + bytes;
                int i = 0;
                while (pos < fieldEndPos) {
                    intBuffers[i] = readLittleEndian32_NoCheck();
                    i++;
                }
                return Arrays.copyOf(intBuffers, i);
            }
            case WIRETYPE_FIXED32: {
                int i = 0;
                while (true) {
                    if (intBuffers.length <= i) {
                        intBuffers = Arrays.copyOf(intBuffers, intBuffers.length + 16);
                    }
                    intBuffers[i] = readFixed32();
                    i++;

                    if (isAtEnd()) {
                        return Arrays.copyOf(intBuffers, i);
                    }
                    int prevPos = pos;
                    int nextTag = readVarint32();
                    if (nextTag != tag) {
                        // We've reached the end of the repeated field. Rewind the buffer position to before
                        // the new tag.
                        pos = prevPos;
                        return Arrays.copyOf(intBuffers, i);
                    }
                }
            }
            default:
                throw InvalidProtocolBufferException.invalidWireType();
        }
    }


    boolean[] booleanBuffers = new boolean[16];

    public boolean[] readBoolList() throws IOException {
        switch (WireFormat.getTagWireType(tag)) {
            case WIRETYPE_LENGTH_DELIMITED: {
                final int bytes = readVarint32();
                if (bytes > booleanBuffers.length) {
                    booleanBuffers = new boolean[bytes + 16];
                }
                final int fieldEndPos = pos + bytes;
                int i = 0;
                while (pos < fieldEndPos) {
                    booleanBuffers[i] = readVarint32() != 0;
                    i++;
                }
                requirePosition(fieldEndPos);
                return Arrays.copyOf(booleanBuffers, i);
            }
            case WIRETYPE_VARINT: {
                int i = 0;
                while (true) {
                    if (booleanBuffers.length == i) {
                        booleanBuffers = Arrays.copyOf(booleanBuffers, booleanBuffers.length + 16);
                    }

                    booleanBuffers[i] = readBool();
                    i++;

                    if (isAtEnd()) {
                        return Arrays.copyOf(booleanBuffers, i);
                    }
                    int prevPos = pos;
                    int nextTag = readVarint32();
                    if (nextTag != tag) {
                        // We've reached the end of the repeated field. Rewind the buffer position to before
                        // the new tag.
                        pos = prevPos;
                        return Arrays.copyOf(booleanBuffers, i);
                    }
                }
            }
            default:
                throw InvalidProtocolBufferException.invalidWireType();
        }
    }


    public String[] readStringList() throws IOException {
        return readStringListInternal(false);
    }


    public String[] readStringListRequireUtf8() throws IOException {
        return readStringListInternal(true);
    }

    String[] strings = new String[16];

    public String[] readStringListInternal(boolean requireUtf8)
            throws IOException {
        if (WireFormat.getTagWireType(tag) != WIRETYPE_LENGTH_DELIMITED) {
            throw InvalidProtocolBufferException.invalidWireType();
        }
        int i = 0;
        while (true) {
            if (strings.length == i) {
                strings = Arrays.copyOf(strings, strings.length + 16);
            }
            strings[i] = readStringInternal(requireUtf8);
            i++;

            if (isAtEnd()) {
                return Arrays.copyOf(strings, i);
            }
            int prevPos = pos;
            int nextTag = readVarint32();
            if (nextTag != tag) {
                // We've reached the end of the repeated field. Rewind the buffer position to before
                // the new tag.
                pos = prevPos;
                return Arrays.copyOf(strings, i);
            }
        }
    }


    public Glob[] readMessageList(GlobInstantiator instantiator, GlobType type, ProtoBufGlobDeserializer globDeserializer)
            throws IOException {
        if (WireFormat.getTagWireType(tag) != WIRETYPE_LENGTH_DELIMITED) {
            throw InvalidProtocolBufferException.invalidWireType();
        }
        List<Glob> globs = new ArrayList<>();
        final int listTag = tag;
        while (true) {
            globs.add(readMessageNoTagCheck(instantiator, type, globDeserializer));

            if (isAtEnd()) {
                return globs.toArray(Glob[]::new);
            }
            int prevPos = pos;
            int nextTag = readVarint32();
            if (nextTag != listTag) {
                // We've reached the end of the repeated field. Rewind the buffer position to before
                // the new tag.
                pos = prevPos;
                return globs.toArray(Glob[]::new);
            }
        }
    }


    public void readBytesList(List<byte[]> target) throws IOException {
        if (WireFormat.getTagWireType(tag) != WIRETYPE_LENGTH_DELIMITED) {
            throw InvalidProtocolBufferException.invalidWireType();
        }

        while (true) {
            target.add(readBytes());

            if (isAtEnd()) {
                return;
            }
            int prevPos = pos;
            int nextTag = readVarint32();
            if (nextTag != tag) {
                // We've reached the end of the repeated field. Rewind the buffer position to before
                // the new tag.
                pos = prevPos;
                return;
            }
        }
    }


    public void readUInt32List(List<Integer> target) throws IOException {
        switch (WireFormat.getTagWireType(tag)) {
            case WIRETYPE_LENGTH_DELIMITED:
                final int bytes = readVarint32();
                final int fieldEndPos = pos + bytes;
                while (pos < fieldEndPos) {
                    target.add(readVarint32());
                }
                break;
            case WIRETYPE_VARINT:
                while (true) {
                    target.add(readUInt32());

                    if (isAtEnd()) {
                        return;
                    }
                    int prevPos = pos;
                    int nextTag = readVarint32();
                    if (nextTag != tag) {
                        // We've reached the end of the repeated field. Rewind the buffer position to before
                        // the new tag.
                        pos = prevPos;
                        return;
                    }
                }
            default:
                throw InvalidProtocolBufferException.invalidWireType();
        }
    }


    public void readEnumList(List<Integer> target) throws IOException {
        switch (WireFormat.getTagWireType(tag)) {
            case WIRETYPE_LENGTH_DELIMITED:
                final int bytes = readVarint32();
                final int fieldEndPos = pos + bytes;
                while (pos < fieldEndPos) {
                    target.add(readVarint32());
                }
                break;
            case WIRETYPE_VARINT:
                while (true) {
                    target.add(readEnum());

                    if (isAtEnd()) {
                        return;
                    }
                    int prevPos = pos;
                    int nextTag = readVarint32();
                    if (nextTag != tag) {
                        // We've reached the end of the repeated field. Rewind the buffer position to before
                        // the new tag.
                        pos = prevPos;
                        return;
                    }
                }
            default:
                throw InvalidProtocolBufferException.invalidWireType();
        }
    }


    public int[] readSFixed32List() throws IOException {
        switch (WireFormat.getTagWireType(tag)) {
            case WIRETYPE_LENGTH_DELIMITED: {
                final int bytes = readVarint32();
                if (bytes >> 2 > intBuffers.length) {
                    intBuffers = new int[bytes >> 2];
                }
                verifyPackedFixed32Length(bytes);
                final int fieldEndPos = pos + bytes;
                int i = 0;
                while (pos < fieldEndPos) {
                    intBuffers[i] = readLittleEndian32_NoCheck();
                    i++;
                }
                return Arrays.copyOf(intBuffers, i);
            }
            case WIRETYPE_FIXED32: {
                int i = 0;
                while (true) {
                    if (intBuffers.length <= i) {
                        intBuffers = Arrays.copyOf(intBuffers, intBuffers.length + 16);
                    }
                    intBuffers[i] = readSFixed32();
                    i++;

                    if (isAtEnd()) {
                        return Arrays.copyOf(intBuffers, i);
                    }
                    int prevPos = pos;
                    int nextTag = readVarint32();
                    if (nextTag != tag) {
                        // We've reached the end of the repeated field. Rewind the buffer position to before
                        // the new tag.
                        pos = prevPos;
                        return Arrays.copyOf(intBuffers, i);
                    }
                }
            }
            default:
                throw InvalidProtocolBufferException.invalidWireType();
        }
    }


    public long[] readSFixed64List() throws IOException {
        switch (WireFormat.getTagWireType(tag)) {
            case WIRETYPE_LENGTH_DELIMITED: {
                final int bytes = readVarint32();
                verifyPackedFixed64Length(bytes);
                if (bytes > this.longBuffers.length) {
                    longBuffers = new long[bytes];
                }

                final int fieldEndPos = pos + bytes;
                int i = 0;
                while (pos < fieldEndPos) {
                    longBuffers[i] = readLittleEndian64_NoCheck();
                    i++;
                }
                return Arrays.copyOf(longBuffers, i);
            }
            case WIRETYPE_FIXED64: {
                int i = 0;
                while (true) {
                    if (longBuffers.length == i) {
                        longBuffers = Arrays.copyOf(longBuffers, longBuffers.length + 16);
                    }
                    longBuffers[i] = readSFixed64();
                    i++;
                    if (isAtEnd()) {
                        return Arrays.copyOf(longBuffers, i);
                    }
                    int prevPos = pos;
                    int nextTag = readVarint32();
                    if (nextTag != tag) {
                        // We've reached the end of the repeated field. Rewind the buffer position to before
                        // the new tag.
                        pos = prevPos;
                        return Arrays.copyOf(longBuffers, i);
                    }
                }
            }
            default:
                throw InvalidProtocolBufferException.invalidWireType();
        }
    }


    public int[] readSInt32List() throws IOException {
        switch (WireFormat.getTagWireType(tag)) {
            case WIRETYPE_LENGTH_DELIMITED: {
                final int bytes = readVarint32();
                if (bytes > this.intBuffers.length) {
                    intBuffers = new int[bytes];
                }
                final int fieldEndPos = pos + bytes;
                int i = 0;
                while (pos < fieldEndPos) {
                    intBuffers[i] = decodeZigZag32(readVarint32());
                    i++;
                }
                return Arrays.copyOf(intBuffers, i);
            }
            case WIRETYPE_VARINT: {
                int i = 0;
                while (true) {
                    if (i == intBuffers.length) {
                        intBuffers = Arrays.copyOf(intBuffers, i + 16);
                    }
                    intBuffers[i] = readSInt32();
                    i++;

                    if (isAtEnd()) {
                        return Arrays.copyOf(intBuffers, i);
                    }
                    int prevPos = pos;
                    int nextTag = readVarint32();
                    if (nextTag != tag) {
                        // We've reached the end of the repeated field. Rewind the buffer position to before
                        // the new tag.
                        pos = prevPos;
                        return Arrays.copyOf(intBuffers, i);
                    }
                }
            }
            default:
                throw InvalidProtocolBufferException.invalidWireType();
        }
    }


    public long[] readSInt64List() throws IOException {
        switch (WireFormat.getTagWireType(tag)) {
            case WIRETYPE_LENGTH_DELIMITED: {
                final int bytes = readVarint32();
                if (bytes > this.intBuffers.length) {
                    longBuffers = new long[bytes];
                }

                final int fieldEndPos = pos + bytes;
                int i = 0;
                while (pos < fieldEndPos) {
                    longBuffers[i] = decodeZigZag64(readVarint64());
                    i++;
                }
                return Arrays.copyOf(longBuffers, i);
            }
            case WIRETYPE_VARINT: {
                int i = 0;
                while (true) {
                    if (i == longBuffers.length) {
                        longBuffers = Arrays.copyOf(longBuffers, i + 16);
                    }

                    longBuffers[i] = readSInt64();
                    i++;

                    if (isAtEnd()) {
                        return Arrays.copyOf(longBuffers, i);
                    }
                    int prevPos = pos;
                    int nextTag = readVarint32();
                    if (nextTag != tag) {
                        // We've reached the end of the repeated field. Rewind the buffer position to before
                        // the new tag.
                        pos = prevPos;
                        return Arrays.copyOf(longBuffers, i);
                    }
                }
            }
            default:
                throw InvalidProtocolBufferException.invalidWireType();
        }
    }

    /**
     * Read a raw Varint from the stream. If larger than 32 bits, discard the upper bits.
     */
    private int readVarint32() throws IOException {
        // See implementation notes for readRawVarint64
        int i = pos;

        if (limit == pos) {
            throw InvalidProtocolBufferException.truncatedMessage();
        }

        int x;
        if ((x = buffer[i++]) >= 0) {
            pos = i;
            return x;
        } else if (limit - i < 9) {
            return (int) readVarint64SlowPath();
        } else if ((x ^= (buffer[i++] << 7)) < 0) {
            x ^= (~0 << 7);
        } else if ((x ^= (buffer[i++] << 14)) >= 0) {
            x ^= (~0 << 7) ^ (~0 << 14);
        } else if ((x ^= (buffer[i++] << 21)) < 0) {
            x ^= (~0 << 7) ^ (~0 << 14) ^ (~0 << 21);
        } else {
            int y = buffer[i++];
            x ^= y << 28;
            x ^= (~0 << 7) ^ (~0 << 14) ^ (~0 << 21) ^ (~0 << 28);
            if (y < 0
                && buffer[i++] < 0
                && buffer[i++] < 0
                && buffer[i++] < 0
                && buffer[i++] < 0
                && buffer[i++] < 0) {
                throw InvalidProtocolBufferException.malformedVarint();
            }
        }
        pos = i;
        return x;
    }

    public long readVarint64() throws IOException {
        // Implementation notes:
        //
        // Optimized for one-byte values, expected to be common.
        // The particular code below was selected from various candidates
        // empirically, by winning VarintBenchmark.
        //
        // Sign extension of (signed) Java bytes is usually a nuisance, but
        // we exploit it here to more easily obtain the sign of bytes read.
        // Instead of cleaning up the sign extension bits by masking eagerly,
        // we delay until we find the final (positive) byte, when we clear all
        // accumulated bits with one xor.  We depend on javac to constant fold.
        int i = pos;

        if (limit == i) {
            throw InvalidProtocolBufferException.truncatedMessage();
        }

        final byte[] buffer = this.buffer;
        long x;
        int y;
        if ((y = buffer[i++]) >= 0) {
            pos = i;
            return y;
        } else if (limit - i < 9) {
            return readVarint64SlowPath();
        } else if ((y ^= (buffer[i++] << 7)) < 0) {
            x = y ^ (~0 << 7);
        } else if ((y ^= (buffer[i++] << 14)) >= 0) {
            x = y ^ ((~0 << 7) ^ (~0 << 14));
        } else if ((y ^= (buffer[i++] << 21)) < 0) {
            x = y ^ ((~0 << 7) ^ (~0 << 14) ^ (~0 << 21));
        } else if ((x = y ^ ((long) buffer[i++] << 28)) >= 0L) {
            x ^= (~0L << 7) ^ (~0L << 14) ^ (~0L << 21) ^ (~0L << 28);
        } else if ((x ^= ((long) buffer[i++] << 35)) < 0L) {
            x ^= (~0L << 7) ^ (~0L << 14) ^ (~0L << 21) ^ (~0L << 28) ^ (~0L << 35);
        } else if ((x ^= ((long) buffer[i++] << 42)) >= 0L) {
            x ^= (~0L << 7) ^ (~0L << 14) ^ (~0L << 21) ^ (~0L << 28) ^ (~0L << 35) ^ (~0L << 42);
        } else if ((x ^= ((long) buffer[i++] << 49)) < 0L) {
            x ^=
                    (~0L << 7)
                    ^ (~0L << 14)
                    ^ (~0L << 21)
                    ^ (~0L << 28)
                    ^ (~0L << 35)
                    ^ (~0L << 42)
                    ^ (~0L << 49);
        } else {
            x ^= ((long) buffer[i++] << 56);
            x ^=
                    (~0L << 7)
                    ^ (~0L << 14)
                    ^ (~0L << 21)
                    ^ (~0L << 28)
                    ^ (~0L << 35)
                    ^ (~0L << 42)
                    ^ (~0L << 49)
                    ^ (~0L << 56);
            if (x < 0L) {
                if (buffer[i++] < 0L) {
                    throw InvalidProtocolBufferException.malformedVarint();
                }
            }
        }
        pos = i;
        return x;
    }

    private long readVarint64SlowPath() throws IOException {
        long result = 0;
        for (int shift = 0; shift < 64; shift += 7) {
            final byte b = readByte();
            result |= (long) (b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                return result;
            }
        }
        throw InvalidProtocolBufferException.malformedVarint();
    }

    private byte readByte() throws IOException {
        if (pos == limit) {
            throw InvalidProtocolBufferException.truncatedMessage();
        }
        return buffer[pos++];
    }

    private int readLittleEndian32() throws IOException {
        requireBytes(FIXED32_SIZE);
        return readLittleEndian32_NoCheck();
    }

    private long readLittleEndian64() throws IOException {
        requireBytes(FIXED64_SIZE);
        return readLittleEndian64_NoCheck();
    }

    private int readLittleEndian32_NoCheck() {
        int p = pos;
        final byte[] buffer = this.buffer;
        pos = p + FIXED32_SIZE;
        return (((buffer[p] & 0xff))
                | ((buffer[p + 1] & 0xff) << 8)
                | ((buffer[p + 2] & 0xff) << 16)
                | ((buffer[p + 3] & 0xff) << 24));
    }

    private long readLittleEndian64_NoCheck() {
        int p = pos;
        final byte[] buffer = this.buffer;
        pos = p + FIXED64_SIZE;
        return (((buffer[p] & 0xffL))
                | ((buffer[p + 1] & 0xffL) << 8)
                | ((buffer[p + 2] & 0xffL) << 16)
                | ((buffer[p + 3] & 0xffL) << 24)
                | ((buffer[p + 4] & 0xffL) << 32)
                | ((buffer[p + 5] & 0xffL) << 40)
                | ((buffer[p + 6] & 0xffL) << 48)
                | ((buffer[p + 7] & 0xffL) << 56));
    }

    private void skipVarint() throws IOException {
        if (limit - pos >= 10) {
            final byte[] buffer = this.buffer;
            int p = pos;
            for (int i = 0; i < 10; i++) {
                if (buffer[p++] >= 0) {
                    pos = p;
                    return;
                }
            }
        }
        skipVarintSlowPath();
    }

    private void skipVarintSlowPath() throws IOException {
        for (int i = 0; i < 10; i++) {
            if (readByte() >= 0) {
                return;
            }
        }
        throw InvalidProtocolBufferException.malformedVarint();
    }

    private void skipBytes(final int size) throws IOException {
        requireBytes(size);

        pos += size;
    }

    private void skipGroup() throws IOException {
        int prevEndGroupTag = endGroupTag;
        endGroupTag = WireFormat.makeTag(WireFormat.getTagFieldNumber(tag), WIRETYPE_END_GROUP);
        while (true) {
            if (getFieldNumber() == Integer.MAX_VALUE || !skipField()) {
                break;
            }
        }
        if (tag != endGroupTag) {
            throw InvalidProtocolBufferException.parseFailure();
        }
        endGroupTag = prevEndGroupTag;
    }

    static class InvalidProtocolBufferException {

        public static IOException parseFailure() {
            return null;
        }

        public static IOException truncatedMessage() {
            return null;
        }

        public static IOException invalidWireType() {
            return null;
        }

        public static IOException malformedVarint() {
            return null;
        }
    }

    private void requireBytes(int size) throws IOException {
        if (size < 0 || size > (limit - pos)) {
            throw InvalidProtocolBufferException.truncatedMessage();
        }
    }

    private void requireWireType(int requiredWireType) throws IOException {
        if (WireFormat.getTagWireType(tag) != requiredWireType) {
            throw InvalidProtocolBufferException.invalidWireType();
        }
    }

    private void verifyPackedFixed64Length(int bytes) throws IOException {
        requireBytes(bytes);
        if ((bytes & FIXED64_MULTIPLE_MASK) != 0) {
            // Require that the number of bytes be a multiple of 8.
            throw InvalidProtocolBufferException.parseFailure();
        }
    }

    private void verifyPackedFixed32Length(int bytes) throws IOException {
        requireBytes(bytes);
        if ((bytes & FIXED32_MULTIPLE_MASK) != 0) {
            // Require that the number of bytes be a multiple of 4.
            throw InvalidProtocolBufferException.parseFailure();
        }
    }

    private void requirePosition(int expectedPosition) throws IOException {
        if (pos != expectedPosition) {
            throw InvalidProtocolBufferException.truncatedMessage();
        }
    }
}
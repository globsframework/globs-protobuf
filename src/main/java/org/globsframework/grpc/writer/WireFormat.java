// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
//
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file or at
// https://developers.google.com/open-source/licenses/bsd

package org.globsframework.grpc.writer;

/**
 * This class is used internally by the Protocol Buffer library and generated message
 * implementations. It is public only because those generated messages do not reside in the {@code
 * protobuf} package. Others should not use this class directly.
 *
 * <p>This class contains constants and helper functions useful for dealing with the Protocol Buffer
 * wire format.
 *
 * @author kenton@google.com Kenton Varda
 */
public final class WireFormat {
    // Do not allow instantiation.
    private WireFormat() {
    }

    public static final int FIXED32_SIZE = 4;
    public static final int FIXED64_SIZE = 8;
    public static final int MAX_VARINT32_SIZE = 5;
    public static final int MAX_VARINT64_SIZE = 10;
    public static final int MAX_VARINT_SIZE = 10;

    public static final int WIRETYPE_VARINT = 0;
    public static final int WIRETYPE_FIXED64 = 1;
    public static final int WIRETYPE_LENGTH_DELIMITED = 2;
    public static final int WIRETYPE_START_GROUP = 3;
    public static final int WIRETYPE_END_GROUP = 4;
    public static final int WIRETYPE_FIXED32 = 5;

    static final int TAG_TYPE_BITS = 3;
    static final int TAG_TYPE_MASK = (1 << TAG_TYPE_BITS) - 1;

    /**
     * Given a tag value, determines the wire type (the lower 3 bits).
     */
    public static int getTagWireType(final int tag) {
        return tag & TAG_TYPE_MASK;
    }

    /**
     * Given a tag value, determines the field number (the upper 29 bits).
     */
    public static int getTagFieldNumber(final int tag) {
        return tag >>> TAG_TYPE_BITS;
    }

    /**
     * Makes a tag value given a field number and wire type.
     */
    public static int makeTag(final int fieldNumber, final int wireType) {
        return (fieldNumber << TAG_TYPE_BITS) | wireType;
    }

    // Field numbers for fields in MessageSet wire format.
    static final int MESSAGE_SET_ITEM = 1;
    static final int MESSAGE_SET_TYPE_ID = 2;
    static final int MESSAGE_SET_MESSAGE = 3;

    // Tag numbers.
    static final int MESSAGE_SET_ITEM_TAG = makeTag(MESSAGE_SET_ITEM, WIRETYPE_START_GROUP);
    static final int MESSAGE_SET_ITEM_END_TAG = makeTag(MESSAGE_SET_ITEM, WIRETYPE_END_GROUP);
    static final int MESSAGE_SET_TYPE_ID_TAG = makeTag(MESSAGE_SET_TYPE_ID, WIRETYPE_VARINT);
    static final int MESSAGE_SET_MESSAGE_TAG =
            makeTag(MESSAGE_SET_MESSAGE, WIRETYPE_LENGTH_DELIMITED);

    /**
     * Validation level for handling incoming string field data which potentially contain non-UTF8
     * bytes.
     */
    enum Utf8Validation {
        /**
         * Eagerly parses to String; silently accepts invalid UTF8 bytes.
         */
        LOOSE,
        /**
         * Eagerly parses to String; throws an IOException on invalid bytes.
         */
        STRICT,
        /**
         * Keep data as ByteString; validation/conversion to String is lazy.
         */
        LAZY;
    }
}

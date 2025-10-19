// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
//
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file or at
// https://developers.google.com/open-source/licenses/bsd

package org.globsframework.grpc.writer;

import org.globsframework.core.model.Glob;

import java.io.IOException;
import java.util.List;

/** A writer that performs serialization of protobuf message fields. */
interface Writer {

    /** The order in which the fields are written by a {@link Writer}. */
  enum FieldOrder {
    /** Fields are written in ascending order by field number. */
    ASCENDING,

    /** Fields are written in descending order by field number. */
    DESCENDING
  }

  /** Indicates the order in which the fields are written by this {@link Writer}. */
  FieldOrder fieldOrder();

  /** Writes a field of type {@link FieldType#SFIXED32}. */
  void writeSFixed32(int fieldNumber, int value) throws IOException;

  /** Writes a field of type {@link FieldType#INT64}. */
  void writeInt64(int fieldNumber, long value) throws IOException;

  /** Writes a field of type {@link FieldType#SFIXED64}. */
  void writeSFixed64(int fieldNumber, long value) throws IOException;

  /** Writes a field of type {@link FieldType#FLOAT}. */
  void writeFloat(int fieldNumber, float value) throws IOException;

  /** Writes a field of type {@link FieldType#DOUBLE}. */
  void writeDouble(int fieldNumber, double value) throws IOException;

  /** Writes a field of type {@link FieldType#ENUM}. */
  void writeEnum(int fieldNumber, int value) throws IOException;

  /** Writes a field of type {@link FieldType#UINT64}. */
  void writeUInt64(int fieldNumber, long value) throws IOException;

  /** Writes a field of type {@link FieldType#INT32}. */
  void writeInt32(int fieldNumber, int value) throws IOException;

  /** Writes a field of type {@link FieldType#FIXED64}. */
  void writeFixed64(int fieldNumber, long value) throws IOException;

  /** Writes a field of type {@link FieldType#FIXED32}. */
  void writeFixed32(int fieldNumber, int value) throws IOException;

  /** Writes a field of type {@link FieldType#BOOL}. */
  void writeBool(int fieldNumber, boolean value) throws IOException;

  /** Writes a field of type {@link FieldType#STRING}. */
  void writeString(int fieldNumber, String value) throws IOException;

  /** Writes a field of type {@link FieldType#BYTES}. */
  void writeBytes(int fieldNumber, byte[] value) throws IOException;

  /** Writes a field of type {@link FieldType#UINT32}. */
  void writeUInt32(int fieldNumber, int value) throws IOException;

  /** Writes a field of type {@link FieldType#SINT32}. */
  void writeSInt32(int fieldNumber, int value) throws IOException;

  /** Writes a field of type {@link FieldType#SINT64}. */
  void writeSInt64(int fieldNumber, long value) throws IOException;

    /** Writes a field of type {@link FieldType#MESSAGE}. */
    void writeMessage(int fieldNumber, Glob value) throws IOException;

    /** Writes a field of type {@link FieldType#MESSAGE}. */
    void writeMessage(int fieldNumber, Glob value, ProtoBufGlobSerializer globSerializer) throws IOException;

    /** Writes a list field of type {@link FieldType#INT32}. */
  void writeInt32List(int fieldNumber, int[] value, boolean packed) throws IOException;

  /** Writes a list field of type {@link FieldType#FIXED32}. */
  void writeFixed32List(int fieldNumber, int[] list, boolean packed) throws IOException;

  /** Writes a list field of type {@link FieldType#INT64}. */
  void writeInt64List(int fieldNumber, long[] value, boolean packed) throws IOException;

  /** Writes a list field of type {@link FieldType#UINT64}. */
  void writeUInt64List(int fieldNumber, long[] value, boolean packed) throws IOException;

  /** Writes a list field of type {@link FieldType#FIXED64}. */
  void writeFixed64List(int fieldNumber, long[] value, boolean packed) throws IOException;

  /** Writes a list field of type {@link FieldType#FLOAT}. */
  void writeFloatList(int fieldNumber, double[] value, boolean packed) throws IOException;

  /** Writes a list field of type {@link FieldType#DOUBLE}. */
  void writeDoubleList(int fieldNumber, double[] list, boolean packed) throws IOException;

  /** Writes a list field of type {@link FieldType#ENUM}. */
  void writeEnumList(int fieldNumber, int[] value, boolean packed) throws IOException;

  /** Writes a list field of type {@link FieldType#BOOL}. */
  void writeBoolList(int fieldNumber, boolean[] value, boolean packed) throws IOException;

  /** Writes a list field of type {@link FieldType#STRING}. */
  void writeStringList(int fieldNumber, String[] value) throws IOException;

  /** Writes a list field of type {@link FieldType#BYTES}. */
  void writeBytesList(int fieldNumber, List<byte[]> value) throws IOException;

  /** Writes a list field of type {@link FieldType#UINT32}. */
  void writeUInt32List(int fieldNumber, List<Integer> value, boolean packed) throws IOException;

  /** Writes a list field of type {@link FieldType#SFIXED32}. */
  void writeSFixed32List(int fieldNumber, int[] value, boolean packed) throws IOException;

  /** Writes a list field of type {@link FieldType#SFIXED64}. */
  void writeSFixed64List(int fieldNumber, long[] value, boolean packed) throws IOException;

  /** Writes a list field of type {@link FieldType#SINT32}. */
  void writeSInt32List(int fieldNumber, int[] value, boolean packed) throws IOException;

  /** Writes a list field of type {@link FieldType#SINT64}. */
  void writeSInt64List(int fieldNumber, long[] value, boolean packed) throws IOException;

  /** Writes a list field of type {@link FieldType#MESSAGE}. */
  void writeMessageList(int fieldNumber, Glob[] value) throws IOException;

  void writeMessageList(int fieldNumber, Glob[] list, ProtoBufGlobSerializer globSerializer) throws IOException;

}

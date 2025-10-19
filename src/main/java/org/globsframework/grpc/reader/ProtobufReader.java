package org.globsframework.grpc.reader;

import org.globsframework.core.metamodel.GlobType;
import org.globsframework.core.model.Glob;

import java.io.IOException;

public interface ProtobufReader {
    Glob read(GlobType type, SafeHeapReader inputStream) throws IOException;
}

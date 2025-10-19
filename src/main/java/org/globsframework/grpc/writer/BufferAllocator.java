package org.globsframework.grpc.writer;

public class BufferAllocator {
    public AllocatedBuffer allocateHeapBuffer(int capacity) {
        return new AllocatedBuffer(new byte[capacity], 0, capacity);
    }
}

package org.globsframework.grpc.writer;

public interface BufferAllocator {
    AllocatedBuffer allocateHeapBuffer(int capacity);

    static BufferAllocator create() {
        return new BasicAllocator();
    }

    class BasicAllocator implements BufferAllocator {
        @Override
        public AllocatedBuffer allocateHeapBuffer(int capacity) {
            return new AllocatedBuffer(new byte[capacity], capacity);
        }
    }
}

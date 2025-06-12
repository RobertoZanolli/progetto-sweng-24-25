package com.google.gwt.sample.notes.shared;

public class NoteIdGenerator implements IdGenerator {
    private static final long EPOCH = 1700000000000L; // Custom start timestamp
    private static final int MACHINE_ID_BITS = 10;
    private static final int SEQUENCE_BITS = 12;

    private static final long MAX_MACHINE_ID = ~(-1L << MACHINE_ID_BITS);
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    private final long machineId;
    private long lastTimestamp = -1L;
    private long sequence = 0L;

    public NoteIdGenerator(long machineId) {
        if (machineId > MAX_MACHINE_ID) {
            throw new IllegalArgumentException("Invalid machineId");
        }
        this.machineId = machineId;
    }

    @Override
    public synchronized long nextId() {
        long timestamp = timeGen();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards");
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }

        lastTimestamp = timestamp;

        return ((timestamp - EPOCH) << (MACHINE_ID_BITS + SEQUENCE_BITS)) |
                (machineId << SEQUENCE_BITS) |
                sequence;
    }

    private long waitNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }
}


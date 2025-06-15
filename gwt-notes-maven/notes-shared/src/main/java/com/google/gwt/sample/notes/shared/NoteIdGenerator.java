package com.google.gwt.sample.notes.shared;

/**
 * Implementazione di IdGenerator che genera ID univoci per le note.
 * Utilizza un algoritmo basato su timestamp e ID macchina per garantire l'unicità.
 */
public class NoteIdGenerator implements IdGenerator {
    private static final long EPOCH = 1700000000000L; // Timestamp di partenza 
    private static final int MACHINE_ID_BITS = 10;
    private static final int SEQUENCE_BITS = 12;

    private static final long MAX_MACHINE_ID = ~(-1L << MACHINE_ID_BITS);
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    private final long machineId;
    private long lastTimestamp = -1L;
    private long sequence = 0L;

    /**
     * Costruttore che inizializza il generatore con un ID macchina specifico
     * @param machineId ID univoco della macchina (deve essere minore di MAX_MACHINE_ID)
     * @throws IllegalArgumentException se l'ID macchina non è valido
     */
    public NoteIdGenerator(long machineId) {
        if (machineId > MAX_MACHINE_ID) {
            throw new IllegalArgumentException("ID macchina non valido");
        }
        this.machineId = machineId;
    }

    @Override
    public synchronized long nextId() {
        long timestamp = timeGen();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Orologio del sistema regredito");
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

    /**
     * Attende il prossimo millisecondo disponibile
     */
    private long waitNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * Ottiene il timestamp corrente in millisecondi
     */
    private long timeGen() {
        return System.currentTimeMillis();
    }
}


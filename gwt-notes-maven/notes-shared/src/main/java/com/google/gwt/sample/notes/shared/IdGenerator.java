package com.google.gwt.sample.notes.shared;

/**
 * Interfaccia per la generazione di identificatori univoci.
 * Implementazioni di questa interfaccia devono garantire la generazione di ID univoci.
 */
public interface IdGenerator {
    /**
     * Genera il prossimo ID disponibile
     * @return un nuovo ID univoco
     */
    long nextId();
}

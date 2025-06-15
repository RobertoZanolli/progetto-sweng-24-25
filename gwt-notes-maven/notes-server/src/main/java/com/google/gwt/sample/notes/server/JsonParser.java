package com.google.gwt.sample.notes.server;

/**
 * Interfaccia che definisce le operazioni base per la conversione JSON.
 * Fornisce metodi per la serializzazione e deserializzazione di oggetti.
 */
public interface JsonParser {
    /**
     * Converte una stringa JSON in un oggetto del tipo specificato.
     * @param json La stringa JSON da convertire
     * @param clazz La classe dell'oggetto da creare
     * @return L'oggetto convertito
     */
    <T> T fromJson(String json, Class<T> clazz);

    /**
     * Converte un oggetto in una stringa JSON.
     * @param obj L'oggetto da convertire
     * @return La rappresentazione JSON dell'oggetto
     */
    String toJson(Object obj);
}

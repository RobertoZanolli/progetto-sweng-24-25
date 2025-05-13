package com.google.gwt.sample.notes.shared;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.google.gwt.user.client.rpc.impl.ReflectionHelper;

@SuppressWarnings("deprecation")
public class StockPrice_Array_Rank_1_FieldSerializer implements com.google.gwt.user.client.rpc.impl.TypeHandler {
  public static void deserialize(SerializationStreamReader streamReader, com.google.gwt.sample.notes.shared.StockPrice[] instance) throws SerializationException {
    com.google.gwt.user.client.rpc.core.java.lang.Object_Array_CustomFieldSerializer.deserialize(streamReader, instance);
  }
  
  public static com.google.gwt.sample.notes.shared.StockPrice[] instantiate(SerializationStreamReader streamReader) throws SerializationException {
    int size = streamReader.readInt();
    return new com.google.gwt.sample.notes.shared.StockPrice[size];
  }
  
  public static void serialize(SerializationStreamWriter streamWriter, com.google.gwt.sample.notes.shared.StockPrice[] instance) throws SerializationException {
    com.google.gwt.user.client.rpc.core.java.lang.Object_Array_CustomFieldSerializer.serialize(streamWriter, instance);
  }
  
  public Object create(SerializationStreamReader reader) throws SerializationException {
    return com.google.gwt.sample.notes.shared.StockPrice_Array_Rank_1_FieldSerializer.instantiate(reader);
  }
  
  public void deserial(SerializationStreamReader reader, Object object) throws SerializationException {
    com.google.gwt.sample.notes.shared.StockPrice_Array_Rank_1_FieldSerializer.deserialize(reader, (com.google.gwt.sample.notes.shared.StockPrice[])object);
  }
  
  public void serial(SerializationStreamWriter writer, Object object) throws SerializationException {
    com.google.gwt.sample.notes.shared.StockPrice_Array_Rank_1_FieldSerializer.serialize(writer, (com.google.gwt.sample.notes.shared.StockPrice[])object);
  }
  
}

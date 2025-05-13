package com.google.gwt.sample.notes.shared;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.google.gwt.user.client.rpc.impl.ReflectionHelper;

@SuppressWarnings("deprecation")
public class DelistedException_FieldSerializer implements com.google.gwt.user.client.rpc.impl.TypeHandler {
  private static native java.lang.String getSymbol(com.google.gwt.sample.notes.shared.DelistedException instance) /*-{
    return instance.@com.google.gwt.sample.notes.shared.DelistedException::symbol;
  }-*/;
  
  private static native void setSymbol(com.google.gwt.sample.notes.shared.DelistedException instance, java.lang.String value) 
  /*-{
    instance.@com.google.gwt.sample.notes.shared.DelistedException::symbol = value;
  }-*/;
  
  public static void deserialize(SerializationStreamReader streamReader, com.google.gwt.sample.notes.shared.DelistedException instance) throws SerializationException {
    setSymbol(instance, streamReader.readString());
    
    com.google.gwt.user.client.rpc.core.java.lang.Exception_FieldSerializer.deserialize(streamReader, instance);
  }
  
  public static com.google.gwt.sample.notes.shared.DelistedException instantiate(SerializationStreamReader streamReader) throws SerializationException {
    return new com.google.gwt.sample.notes.shared.DelistedException();
  }
  
  public static void serialize(SerializationStreamWriter streamWriter, com.google.gwt.sample.notes.shared.DelistedException instance) throws SerializationException {
    streamWriter.writeString(getSymbol(instance));
    
    com.google.gwt.user.client.rpc.core.java.lang.Exception_FieldSerializer.serialize(streamWriter, instance);
  }
  
  public Object create(SerializationStreamReader reader) throws SerializationException {
    return com.google.gwt.sample.notes.shared.DelistedException_FieldSerializer.instantiate(reader);
  }
  
  public void deserial(SerializationStreamReader reader, Object object) throws SerializationException {
    com.google.gwt.sample.notes.shared.DelistedException_FieldSerializer.deserialize(reader, (com.google.gwt.sample.notes.shared.DelistedException)object);
  }
  
  public void serial(SerializationStreamWriter writer, Object object) throws SerializationException {
    com.google.gwt.sample.notes.shared.DelistedException_FieldSerializer.serialize(writer, (com.google.gwt.sample.notes.shared.DelistedException)object);
  }
  
}

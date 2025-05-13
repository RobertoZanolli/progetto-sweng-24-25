package com.google.gwt.sample.notes.shared;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.google.gwt.user.client.rpc.impl.ReflectionHelper;

@SuppressWarnings("deprecation")
public class StockPrice_FieldSerializer implements com.google.gwt.user.client.rpc.impl.TypeHandler {
  private static native double getChange(com.google.gwt.sample.notes.shared.StockPrice instance) /*-{
    return instance.@com.google.gwt.sample.notes.shared.StockPrice::change;
  }-*/;
  
  private static native void setChange(com.google.gwt.sample.notes.shared.StockPrice instance, double value) 
  /*-{
    instance.@com.google.gwt.sample.notes.shared.StockPrice::change = value;
  }-*/;
  
  private static native double getPrice(com.google.gwt.sample.notes.shared.StockPrice instance) /*-{
    return instance.@com.google.gwt.sample.notes.shared.StockPrice::price;
  }-*/;
  
  private static native void setPrice(com.google.gwt.sample.notes.shared.StockPrice instance, double value) 
  /*-{
    instance.@com.google.gwt.sample.notes.shared.StockPrice::price = value;
  }-*/;
  
  private static native java.lang.String getSymbol(com.google.gwt.sample.notes.shared.StockPrice instance) /*-{
    return instance.@com.google.gwt.sample.notes.shared.StockPrice::symbol;
  }-*/;
  
  private static native void setSymbol(com.google.gwt.sample.notes.shared.StockPrice instance, java.lang.String value) 
  /*-{
    instance.@com.google.gwt.sample.notes.shared.StockPrice::symbol = value;
  }-*/;
  
  public static void deserialize(SerializationStreamReader streamReader, com.google.gwt.sample.notes.shared.StockPrice instance) throws SerializationException {
    setChange(instance, streamReader.readDouble());
    setPrice(instance, streamReader.readDouble());
    setSymbol(instance, streamReader.readString());
    
  }
  
  public static com.google.gwt.sample.notes.shared.StockPrice instantiate(SerializationStreamReader streamReader) throws SerializationException {
    return new com.google.gwt.sample.notes.shared.StockPrice();
  }
  
  public static void serialize(SerializationStreamWriter streamWriter, com.google.gwt.sample.notes.shared.StockPrice instance) throws SerializationException {
    streamWriter.writeDouble(getChange(instance));
    streamWriter.writeDouble(getPrice(instance));
    streamWriter.writeString(getSymbol(instance));
    
  }
  
  public Object create(SerializationStreamReader reader) throws SerializationException {
    return com.google.gwt.sample.notes.shared.StockPrice_FieldSerializer.instantiate(reader);
  }
  
  public void deserial(SerializationStreamReader reader, Object object) throws SerializationException {
    com.google.gwt.sample.notes.shared.StockPrice_FieldSerializer.deserialize(reader, (com.google.gwt.sample.notes.shared.StockPrice)object);
  }
  
  public void serial(SerializationStreamWriter writer, Object object) throws SerializationException {
    com.google.gwt.sample.notes.shared.StockPrice_FieldSerializer.serialize(writer, (com.google.gwt.sample.notes.shared.StockPrice)object);
  }
  
}

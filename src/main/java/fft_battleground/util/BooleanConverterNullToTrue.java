package fft_battleground.util;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class BooleanConverterNullToTrue implements AttributeConverter<Boolean, String> {
 
    @Override
    public String convertToDatabaseColumn(Boolean attribute) {
        if (attribute != null) {
            if (attribute) {
                return "Y";
            } else {
                return "N";
            }
                 
        } else {
        	return "Y";
        }
    }
 
    @Override
    public Boolean convertToEntityAttribute(String dbData) {
        if (dbData != null) {
            return dbData.equals("Y");
        } else {
        	return true;
        }
    }
     
}
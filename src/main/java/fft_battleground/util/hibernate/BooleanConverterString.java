package fft_battleground.util.hibernate;

import javax.persistence.AttributeConverter;

public class BooleanConverterString implements AttributeConverter<Boolean, String> {

    @Override
    public String convertToDatabaseColumn(Boolean attribute) {
        if (attribute != null) {
            if (attribute) {
                return "Y";
            } else {
                return "N";
            }
                 
        }
        return null;
    }
 
    @Override
    public Boolean convertToEntityAttribute(String dbData) {
        if (dbData != null) {
            return dbData.equals('Y');
        }
        return null;
    }

}

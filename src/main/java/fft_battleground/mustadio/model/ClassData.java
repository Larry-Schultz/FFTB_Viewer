package fft_battleground.mustadio.model;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import fft_battleground.model.Gender;
import fft_battleground.util.jackson.GenderDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassData {
    public String name;
    
    @JsonDeserialize(using = GenderDeserializer.class)
    public Gender gender;
    public ClassBaseStats baseStats;
    public List<Innate> innates;
}
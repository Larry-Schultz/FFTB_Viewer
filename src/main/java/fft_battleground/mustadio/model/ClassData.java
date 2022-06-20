package fft_battleground.mustadio.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassData {
    public String name;
    public String gender;
    public ClassBaseStats baseStats;
    public List<Innate> innates;
}
package fft_battleground.mustadio.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Status implements Cloneable {
    public String name;
    public String info;
}
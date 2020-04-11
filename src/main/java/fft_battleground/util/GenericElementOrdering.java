package fft_battleground.util;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GenericElementOrdering<T> implements Comparable {
	long id;
	T element;
	
	@SuppressWarnings("unchecked")
	@Override
	public int compareTo(Object arg0) {
		GenericElementOrdering<T> ordering0 = (GenericElementOrdering<T>) arg0;
		return Long.compare(this.id, ordering0.getId());
	}
}

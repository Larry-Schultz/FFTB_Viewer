package fft_battleground.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenericElementOrdering<T> implements Comparable<T> {
	public long id;
	public T element;
	
	public static <T> List<GenericElementOrdering<T>> convertToOrderedList(List<T> list, Comparator<T> comparator) {
		List<T> sortedList = new ArrayList<>();
		sortedList.addAll(list);
		Collections.sort(sortedList, comparator);
		
		List<GenericElementOrdering<T>> sortedGenericElementOrderingList = new ArrayList<>();
		for(int i = 0; i < sortedList.size(); i++) {
			sortedGenericElementOrderingList.add(new GenericElementOrdering<T>(i, sortedList.get(i)));
		}
		
		return sortedGenericElementOrderingList;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public int compareTo(Object arg0) {
		GenericElementOrdering<T> ordering0 = (GenericElementOrdering<T>) arg0;
		return Long.compare(this.id, ordering0.getId());
	}
}

package fft_battleground.util;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenericPairing<U, V> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1667033672393218347L;
	
	private U key;
	private V value;
	
	public static <U,V>  List<GenericPairing<U, V>> convertPairToGenericPair(List<Pair<U,V>> pairList) {
		List<GenericPairing<U,V>> result = pairList.parallelStream().map(pair -> new GenericPairing<U,V>(pair.getLeft(), pair.getRight())).collect(Collectors.toList());
		return result;
	}
	
	public static <U,V> List<GenericPairing<U,V>> convertMapToGenericPairList(Map<U,V> map) {
		List<GenericPairing<U,V>> result = map.keySet().parallelStream().map(key -> new GenericPairing<U,V>(key, map.get(key))).collect(Collectors.toList());
		if(result == null) {
			result = List.of();
		}
		return result;
	}
	
	public static <U,V> Map<U,V> convertGenericPairListToMap(List<GenericPairing<U,V>> genericPairingList) {
		if(genericPairingList == null) {
			return null;
		} else if(genericPairingList.size() == 0) {
			return Collections.emptyMap();
		} else {
			Map<U,V> map = genericPairingList.stream().collect(Collectors.toMap(GenericPairing::getKey, GenericPairing::getValue));
			return map;
		}
	}
	
	@JsonIgnore
	public U getLeft() {
		return this.key;
	}
	
	@JsonIgnore
	public V getRight() {
		return this.value;
	}
}

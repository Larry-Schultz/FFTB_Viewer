package fft_battleground.util;

import java.io.Serializable;

import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class GenericResponse<T> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8564637234667045445L;
	
	public String message;
	public T data;
	
	public GenericResponse(T data) {
		this.data = data;
	}
	
	public static <T> GenericResponse<T> createGenericResponse(T data) {
		return new GenericResponse<T>(data);
	}
	
	public static <T> GenericResponse<T> createGenericResponse(String error, T data) {
		return new GenericResponse<>(error, data);
	}
	
	public static <T> GenericResponse<T> createGenericResponse(T data, String error) {
		return new GenericResponse<>(error, data);
	}
	
	public static <T> ResponseEntity<GenericResponse<T>> createGenericResponseEntity(T data, HttpStatus status) {
		return new ResponseEntity<GenericResponse<T>>(createGenericResponse(data), status);
	}
	
	public static <T> ResponseEntity<GenericResponse<T>> createGenericResponseEntity(String error, T data, HttpStatus status) {
		return new ResponseEntity<GenericResponse<T>>(createGenericResponse(error, data), status);
	}
	
	public static <T> ResponseEntity<GenericResponse<T>> createGenericResponseEntity(T data, String error, HttpStatus status) {
		return new ResponseEntity<GenericResponse<T>>(createGenericResponse(data, error), status);
	}
	
	public static <T> ResponseEntity<GenericResponse<T>> createGenericResponseEntity(T data) {
		return new ResponseEntity<GenericResponse<T>>(createGenericResponse(data), HttpStatus.OK);
	}
	
	public static <T> ResponseEntity<GenericResponse<T>> createGenericResponseEntity(String error, T data) {
		return new ResponseEntity<GenericResponse<T>>(createGenericResponse(error, data), HttpStatus.OK);
	}
	
	public static <T> ResponseEntity<GenericResponse<T>> createGenericResponseEntity(T data, String error) {
		return new ResponseEntity<GenericResponse<T>>(createGenericResponse(data, error), HttpStatus.OK);
	}
}

package fft_battleground.mustadio;

import fft_battleground.exception.MustadioApiException;
import fft_battleground.mustadio.model.MustadioClasses;
import fft_battleground.mustadio.model.MustadioItems;

public interface MustadioRestService {
	MustadioItems fetchMustadioItemsData() throws MustadioApiException;
	MustadioClasses fetchMustadioClassData() throws MustadioApiException;
}

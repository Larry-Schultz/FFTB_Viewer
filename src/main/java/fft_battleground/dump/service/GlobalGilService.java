package fft_battleground.dump.service;

import fft_battleground.dump.model.GlobalGilPageData;
import lombok.SneakyThrows;

public interface GlobalGilService {

	GlobalGilPageData getGlobalGilData();

	Double percentageOfGlobalGil(Integer balance);

}
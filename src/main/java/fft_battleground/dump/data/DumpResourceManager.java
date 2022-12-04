package fft_battleground.dump.data;

import java.io.BufferedReader;

import org.jsoup.nodes.Document;
import org.springframework.core.io.Resource;

import fft_battleground.exception.DumpException;

public interface DumpResourceManager {

	BufferedReader openDumpResource(Resource resource) throws DumpException;

	Document openPlayerList(String url) throws DumpException;

}
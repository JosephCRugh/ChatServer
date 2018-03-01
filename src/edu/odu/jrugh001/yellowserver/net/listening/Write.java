package edu.odu.jrugh001.yellowserver.net.listening;

import java.io.IOException;
import java.io.ObjectOutputStream;

@FunctionalInterface
public interface Write {
	void accept(ObjectOutputStream outStream) throws IOException;
}

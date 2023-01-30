package me.vinceh121.n2ae.script;

import java.io.FilterInputStream;
import java.io.InputStream;

public class CountingInputStream extends FilterInputStream {

	public CountingInputStream(InputStream in) {
		super(in);
	}

}

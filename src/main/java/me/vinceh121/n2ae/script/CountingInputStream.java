package me.vinceh121.n2ae.script;

import java.io.FilterInputStream;
import java.io.InputStream;

public class CountingInputStream extends FilterInputStream {

	public CountingInputStream(final InputStream in) {
		super(in);
	}

}

package com.tblin.market.breakdown;

import java.io.File;

public abstract class BreakDown {

	public BreakDown() {
		File first = new File(DownloadConfig.FIRST_LEVEL_PATH);
		if (!first.exists()) {
			first.mkdir();
		}
		File second = new File(DownloadConfig.SECOND_LEVEL_PATH);
		if (!second.exists()) {
			second.mkdir();
		}
	}

	public abstract int down();

}

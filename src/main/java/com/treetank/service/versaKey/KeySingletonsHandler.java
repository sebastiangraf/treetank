package com.treetank.service.versaKey;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class KeySingletonsHandler {

	private static Map<File, IKeyStorage> singletonInstance = new ConcurrentHashMap<File, IKeyStorage>();

	private KeySingletonsHandler() {
	}

	/**
	 * Singleton getter
	 * 
	 * @param file
	 *            path to the key storage
	 * @return the {@link KeySingletonsHandler}
	 */
	public final static synchronized IKeyStorage getInstance(final File file) {
		if (!singletonInstance.containsKey(file)) {
			final VersaKeyStorage organiser = new VersaKeyStorage(file);
			singletonInstance.put(file, organiser);
		}
		return singletonInstance.get(file);
	}

}

package coding.toast.geotools.utils;

import org.geotools.data.DataStore;

import java.util.Objects;

/**
 * DatsStore Util Class
 */
public class DataStoreUtil {
	public static void closeDataStores(DataStore... stores) {
		for (DataStore dataStore : stores) {
			if (Objects.nonNull(dataStore)) {
				dataStore.dispose();
			}
		}
	}
}

package coding.toast.geotools.utils;

import org.geotools.data.DataStoreFinder;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * ShapeFile Util for Geotools development
 */
public class ShapeFileUtil {
	
	/**
	 * used when any kind of hint is not found to determine shapefile's encoding,
	 * this FORCE_ENCODING value will be used.
	 */
	private static final String FORCE_ENCODING = "UTF-8";
	
	
	public static ShapefileDataStore getShapeFileDataStore(String shapeFilePath, String defaultEncoding) throws IOException {
		URL url = new File(shapeFilePath)
			.toURI().toURL();
		String shapeFileEncoding = readShapeFileEncoding(shapeFilePath, defaultEncoding);
		Map<String, Serializable> shpParamMap = new HashMap<>();
		shpParamMap.put(ShapefileDataStoreFactory.URLP.key, url);
		shpParamMap.put(ShapefileDataStoreFactory.DBFCHARSET.key, shapeFileEncoding);
		return (ShapefileDataStore) DataStoreFinder.getDataStore(shpParamMap);
	}
	
	
	/**
	 * Reads the path of the shapefile, checks for the presence of cpg and cst files,
	 * uses the respective encoding if either file exists, otherwise uses defaultEncoding.
	 * If defaultEncoding is null or an empty string, EUC-KR is used.
	 *
	 * @param shapeFilePath   The path of the shapefile
	 * @param defaultEncoding The default encoding value to use if both cpg and cst files are not present
	 * @throws IOException Thrown when using the Paths API
	 */
	public static String readShapeFileEncoding(String shapeFilePath, String defaultEncoding) throws IOException {
		
		Path path = Paths.get(shapeFilePath);
		String parentPath = path.getParent().toAbsolutePath().toString();
		String pureName = StringUtils.stripFilenameExtension(path.getFileName().toString());
		
		String encoding;
		Path cstFilePath = Paths.get(parentPath, pureName + ".cst");
		Path cpgFilePath = Paths.get(parentPath, pureName + ".cpg");
		
		if (Files.exists(cpgFilePath)) {
			byte[] bytes = Files.readAllBytes(cpgFilePath);
			encoding = new String(bytes);
		}
		else if (Files.exists(cstFilePath)) {
			byte[] bytes = Files.readAllBytes(cstFilePath);
			encoding = new String(bytes);
		}
		else {
			encoding = StringUtils.hasText(defaultEncoding) ? defaultEncoding : FORCE_ENCODING;
		}
		return encoding.trim();
	}
	
}

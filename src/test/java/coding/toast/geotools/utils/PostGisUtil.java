package coding.toast.geotools.utils;

import org.geotools.data.DataAccessFactory;
import org.geotools.data.DataStoreFinder;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.JDBCDataStoreFactory;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;

/**
 * <h2>PostGIS Helper Class working with GeoTools Library</h2>
 * @see <a href="https://docs.geotools.org/maintenance/javadocs/org/geotools/jdbc/JDBCDataStoreFactory.html">JDBCDataStoreFactory Document</a>
 * @see <a href="https://docs.geotools.org/stable/javadocs/org/geotools/data/postgis/PostgisNGDataStoreFactory.html">PostgisNGDataStoreFactory Document</a>
 *
 */
public class PostGisUtil {
	
	/**
	 * get(=create) new GeoTools PostGisDataSource
	 * @param dbtype database server type
	 * @param host database server connection host
	 * @param port database server connection port
	 * @param database database name
	 * @param schema database schema name
	 * @param user database connection user id
	 * @param passwd database connection user password
	 * @return DataStore To Interact with PostGIS Database
	 * @throws IOException occurs when connection fails
	 */
	public static JDBCDataStore getPostGisDataStore(String dbtype, String host, String port,
	                                                 String database, String schema,
	                                                 String user, String passwd) throws IOException {
		return getPostGisDataStore(dbtype, host, port, database, schema, user, passwd, Collections.emptyMap());
	}
	
	/**
	 * get(=create) new GeoTools PostGisDataSource
	 * @param dbtype database server type
	 * @param host database server connection host
	 * @param port database server connection port
	 * @param database database name
	 * @param schema database schema name
	 * @param user database connection user id
	 * @param passwd database connection user password
	 * @param additionalOptions addition options for dataStore creation
	 * @return DataStore To Interact with PostGIS Database
	 * @throws IOException occurs when connection fails
	 */
	public static JDBCDataStore getPostGisDataStore(String dbtype, String host, String port,
	                                      String database, String schema,
	                                      String user, String passwd,
	                                      Map<DataAccessFactory.Param, Object> additionalOptions) throws IOException {
		
		Map<String, Object> optionParams = new HashMap<>();
		
		// Recommendation!
		// It is highly recommended to check JDBCDataStoreFactory.java to see what options are available!
		// There are numerous options, and checking them is crucial as there may be changes
		// with each version of gt-jdbc library
		optionParams.put(JDBCDataStoreFactory.DBTYPE.key, dbtype);
		optionParams.put(JDBCDataStoreFactory.HOST.key, host);
		optionParams.put(JDBCDataStoreFactory.PORT.key, port);
		optionParams.put(JDBCDataStoreFactory.DATABASE.key, database);
		optionParams.put(JDBCDataStoreFactory.USER.key, user);
		optionParams.put(JDBCDataStoreFactory.PASSWD.key, passwd);
		optionParams.put(JDBCDataStoreFactory.SCHEMA.key, schema);
		
		// check additionalOptions is null or empty
		if(!CollectionUtils.isEmpty(additionalOptions)) {
			Set<Map.Entry<DataAccessFactory.Param, Object>> entries = additionalOptions.entrySet();
			for (Map.Entry<DataAccessFactory.Param, Object> entry : entries) {
				optionParams.put(entry.getKey().key, entry.getValue());
			}
		}
		
		// Recommended options:
		// JDBCDataStoreFactory.FETCHSIZE.key - when reading a large table
		// JDBCDataStoreFactory.BATCH_INSERT_SIZE - when writing data to a table. Default value is 1!
		
		// Other useful options:
		// JDBCDataStoreFactory.MAXCONN; // minimum connections (numeric value)
		// JDBCDataStoreFactory.MINCONN; // maximum connections (numeric value)
		// JDBCDataStoreFactory.MAXWAIT; // maximum time to wait for connection attempts, in seconds (numeric value)
		// JDBCDataStoreFactory.VALIDATECONN; // check connection validity before query execution (true/false)
		// JDBCDataStoreFactory.TEST_WHILE_IDLE; // periodically check validity while in use (true/false)
		// JDBCDataStoreFactory.TIME_BETWEEN_EVICTOR_RUNS; // interval for periodic validity checks, in seconds (numeric value)
		
		// If you are using gt-jdbc-postgis, the following options are also available.
		// PostgisNGDataStoreFactory ==> This class extends JDBCDataStoreFactory!
		
		// Note that options written here may vary depending on the version of gt-jdbc,
		// so be sure to check for any changes.
		return (JDBCDataStore) DataStoreFinder.getDataStore(optionParams);
	}
	
	/**
	 * This method is essentially creating the second parameter of DataUtilities.createType.<br>
	 * It creates a one-line String representation of the schema information (=FeatureType) of the ShapeFile<br>
	 * and must be passed as an argument.
	 * @param schema Schema information of the ShapeFile (=FeatureType)
	 * @return String of type spec used in the second parameter of the DataUtilities.createType method
	 */
	public static String getTypeSpecForPostGIS(SimpleFeatureType schema) {
		List<AttributeDescriptor> attributeList = schema.getAttributeDescriptors();
		String geomName = schema.getGeometryDescriptor().getLocalName();
		String geomTypeName = schema.getGeometryDescriptor().getType().getBinding().getSimpleName();
		
		StringBuilder stringBuilder = new StringBuilder();
		
		
		for (AttributeDescriptor attribute : attributeList) {
			String attributeName = attribute.getLocalName();
			
			// Use the frequently used geom type in PostGIS rather than the Geometry Type name used in ShapeFile.
			if(!geomName.equalsIgnoreCase(attributeName)) {
				Class<?> attributeClass = attribute.getType().getBinding();
				stringBuilder
					.append(attributeName).append(":")
					.append(attributeClass == Object.class ? String.class.getName() : attributeClass.getName())
					.append(",");
			}
		}
		
		// Note: The frequently used column name for geometry types in PostGIS is "geom".
		stringBuilder.append("geom")
			.append(":")
			.append(geomTypeName);
		
		return stringBuilder.toString();
	}
	
}

package coding.toast.geotools.postgis;

import coding.toast.geotools.utils.DataStoreUtil;
import coding.toast.geotools.utils.PostGisUtil;
import org.geotools.data.DataUtilities;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.referencing.CRS;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.referencing.FactoryException;

import java.io.IOException;

/**
 * Geotools table creation test class.
 * Warning! Avoid using GeoTools for table creation unless necessary.
 * There are many limitations compared to using pure JDBC libraries.
 */
public class CreateTableUsingGeoToolsTest {
	
	private static JDBCDataStore postGisDataStore;
	
	@BeforeAll
	static void beforeAll() throws IOException {
		postGisDataStore = PostGisUtil.getPostGisDataStore(
			"postgis", // db type
			"localhost", // db server host
			"5432", // db server port
			"postgres", // database 명
			"public", // db 스키마 명
			"postgres", // db connection user id
			"root" // db connection 비번
		);
	}
	
	
	@AfterAll
	static void afterAll() {
		DataStoreUtil.closeDataStores(postGisDataStore);
	}
	
	
	
	// Reading https://docs.geotools.org/stable/userguide/library/main/feature.html
	// and https://stackoverflow.com/questions/52554587/add-new-column-attribute-to-the-shapefile-and-save-it-to-database-using-geotools
	// and https://gis.stackexchange.com/questions/303709/how-to-set-srs-to-epsg4326-in-geotools
	// before this test code will be very helpful!
	@Test
	@DisplayName("Create table via SimpleFeatureTypeBuilder")
	void createTableUsingGeotoolsTest() throws IOException, FactoryException {
		
		SimpleFeatureTypeBuilder featureTypeBuilder = new SimpleFeatureTypeBuilder();
		
		featureTypeBuilder.setName("geotools_create_table"); // table name to create
		
		// setting CRS, if you have multiple geometry columns and want to apply the same
		// CRS, this code will be useful.
		featureTypeBuilder.setCRS(CRS.decode("EPSG:5186"));
		
		// you can see the comment in the middle of the code below.
		// this kind of crs config is useful when you have multiple geometry columns
		// and need to configure different crs.
		featureTypeBuilder.add("geom", Point.class/*, CRS.decode("EPSG:5186")*/);
		
		// the code below is only useful when you have multiple geometry type columns
		featureTypeBuilder.setDefaultGeometry("geom");
		
		// you can create normal attribute info very detail like below
		AttributeTypeBuilder nameAttrBuilder = new AttributeTypeBuilder();
		nameAttrBuilder.setNillable(false);
		nameAttrBuilder.setBinding(String.class);
		nameAttrBuilder.setLength(255);
		AttributeDescriptor nameAttr = nameAttrBuilder.buildDescriptor("name");
		featureTypeBuilder.add(nameAttr);
		// name varchar (15) not null
		
		AttributeTypeBuilder ageAttrBuilder = new AttributeTypeBuilder();
		ageAttrBuilder.setNillable(true);
		ageAttrBuilder.setBinding(Integer.class);
		AttributeDescriptor ageAttr = ageAttrBuilder.buildDescriptor("age");
		featureTypeBuilder.add(ageAttr);
		// ==> age integer
		
		// GeoTools table creation functionality has limitations!
		// You can create a column of numeric type using BigDecimal,
		// but specifying detailed types like numeric(10,2) is not possible.
		// If I'm wrong or if you know a way, please let me know via email.
		
		// Create the table
		postGisDataStore.createSchema(featureTypeBuilder.buildFeatureType());
		
		DataStoreUtil.closeDataStores(postGisDataStore);
	}
	
	@Test
	@DisplayName("Create table via DataUtilities")
	void createTableUsingGeotoolsDataUtilsTest2() throws IOException, FactoryException, SchemaException {
		String createTableName = "geotools_create_table2"; // table name to create
		
		// Specify the schema of the new table.
		// Note (1): typeSpecForPostGIS is a String like "id:java.lang.Long,name:java.lang.String,geom:Point".
		// DataUtilities.createType API documentation provides detailed usage, so please refer to it.
		// Note (2): By default, a spatial index using GIST is created for the geometry column!
		SimpleFeatureType targetSchema
			= DataUtilities.createType(createTableName, "id:java.lang.Long,name:java.lang.String:nillable=false,geom:Point");
		targetSchema = DataUtilities.createSubType(targetSchema, null, CRS.decode("EPSG:5186"));
		
		// Create the table
		postGisDataStore.createSchema(targetSchema);
		
		DataStoreUtil.closeDataStores(postGisDataStore);
	}
}

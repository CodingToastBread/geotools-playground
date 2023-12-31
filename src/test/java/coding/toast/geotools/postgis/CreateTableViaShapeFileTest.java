package coding.toast.geotools.postgis;

import coding.toast.geotools.utils.DataStoreUtil;
import coding.toast.geotools.utils.PostGisUtil;
import coding.toast.geotools.utils.ShapeFileUtil;
import org.geotools.data.DataUtilities;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.SchemaException;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.referencing.CRS;
import org.junit.jupiter.api.Test;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.IOException;
import java.util.Arrays;

/**
 * Test for reading feature attribute information from a shapefile and creating a PostGIS table using that information.
 */
public class CreateTableViaShapeFileTest {
	
	@Test
	void test() throws IOException, FactoryException, SchemaException {
		// Get ShapefileDataStore from the ShapeFileUtil
		ShapefileDataStore shapeFileDataStore = ShapeFileUtil.getShapeFileDataStore(
			"src/test/resources/sample/sample.shp",
			"UTF-8");
		
		// Get the schema and CRS information from the shapefile
		SimpleFeatureType shapeFileSchema = shapeFileDataStore.getSchema();
		CoordinateReferenceSystem shapeFileCrs = shapeFileDataStore.getSchema().getCoordinateReferenceSystem();
		
		// Uncommon, but if there's an error in the prj file of the shapefile,
		// the EPSG code might not be present. It's better to handle it beforehand.
		// If there's no EPSG code, it may cause issues later.
		// So, taking preemptive action is advisable; otherwise, you might regret it later!
		String userDefaultEpsgCodeInput = "5186";
		Integer epsgCode = CRS.lookupEpsgCode(shapeFileCrs, false);
		if (epsgCode == null || epsgCode == 0) {
			shapeFileDataStore.forceSchemaCRS(CRS.decode("EPSG:" + userDefaultEpsgCodeInput));
		}
		
		// Get PostGIS DataStore using PostGisUtil
		JDBCDataStore postGisDataStore = PostGisUtil.getPostGisDataStore(
			"postgis",        // db type
			"localhost",      // db server host
			"5432",           // db server port
			"postgres",       // database name
			"public",         // db schema name
			"postgres",       // db connection user id
			"root"            // db connection password
		);
		
		// Specify the table name to be created
		String tableToCreate = "new_table";
		
		// Check if the table already exists
		boolean isAlreadyExists = Arrays.asList(postGisDataStore.getTypeNames()).contains(tableToCreate);
		if (isAlreadyExists) {
			System.out.println("Already Existing Table!");
			return;
		}
		
		// Read attribute information from the shapefile and create the typeSpec string
		String typeSpecForPostGIS = PostGisUtil.getTypeSpecForPostGIS(shapeFileSchema);
		
		// Create PostGIS Table Schema
		SimpleFeatureType targetSchema
			= DataUtilities.createType(tableToCreate, typeSpecForPostGIS);
		targetSchema = DataUtilities.createSubType(targetSchema, null, shapeFileCrs);
		
		// Create the table
		postGisDataStore.createSchema(targetSchema);
		
		// Close the data stores
		DataStoreUtil.closeDataStores(shapeFileDataStore, postGisDataStore);
	}
}

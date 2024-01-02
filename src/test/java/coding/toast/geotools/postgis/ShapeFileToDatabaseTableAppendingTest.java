package coding.toast.geotools.postgis;

import coding.toast.geotools.utils.PostGisUtil;
import coding.toast.geotools.utils.ShapeFileUtil;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.data.store.ContentFeatureCollection;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.junit.jupiter.api.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

/**
 * Test class for appending shapefile data to a PostGIS table.<br>
 * There are a few important points to note here:<br>
 * <strong>The target table for data injection must have a numeric primary key.</strong>
 */
public class ShapeFileToDatabaseTableAppendingTest {
	
	@Test
	void appendShapeFileDataToTable() throws IOException, FactoryException {
		
		// Create Shapefile DataStore
		ShapefileDataStore shapeFileDataStore = ShapeFileUtil.getShapeFileDataStore(
			"src/test/resources/sample/sample.shp",
			"UTF-8");
		// this shapefile have 3 attributes
		// id(number)
		// name(string)
		// geom(Point, 5186)
		
		// Extract CRS in advance
		CoordinateReferenceSystem shapeFileCrs = shapeFileDataStore.getSchema().getCoordinateReferenceSystem();
		
		// Uncommon, but if there's an error in the prj file of the shapefile,
		// the EPSG code might not be present. It's better to handle it beforehand.
		// If there's no EPSG code, it may cause issues later. So, taking preemptive action is advisable.
		String userDefaultEpsgCodeInput = "5186";
		Integer epsgCode = CRS.lookupEpsgCode(shapeFileCrs, false);
		if (epsgCode == null || epsgCode == 0) {
			shapeFileDataStore.forceSchemaCRS(CRS.decode("EPSG:" + userDefaultEpsgCodeInput));
		}
		
		// Create PostGIS DataStore
		DataStore postGisDataStore = PostGisUtil.getPostGisDataStore(
			"postgis",
			"localhost",
			"5432",
			"postgres",
			"public",
			"postgres",
			"root"
		);
		
		// Target table name for data insert
		String targetTableName = "sample";
		// Caution! There must be a numeric primary key in the table where you want to insert data!!!
        /*
        -- table DDL
		create table public.sample
		(
		    fid  integer not null
						primary key,
		    id   bigint,
		    name varchar,
		    geom geometry(Point, 5186)
		);
		
		create index spatial_new_table_geom
		    on public.sample using gist (geom);
         */
		
		// Check if the table really exists in the database
		if (!Arrays.asList(postGisDataStore.getTypeNames()).contains(targetTableName)) {
			System.err.println("No Table Found!!!!!!");
		}
		
		// Create a new schema based on the existing postGIS schema
		// Retrieve the FeatureType (= schema) of a specific table.
		SimpleFeatureType postGisSchema = postGisDataStore.getSchema(targetTableName);
		
		SimpleFeatureTypeBuilder builderForPostGIS = new SimpleFeatureTypeBuilder();
		
		builderForPostGIS.setName(postGisSchema.getName());
		
		builderForPostGIS.setSuperType((SimpleFeatureType) postGisSchema.getSuper());
		
		builderForPostGIS.addAll(postGisSchema.getAttributeDescriptors());
		// Note1: postGisSchema.getAttributeDescriptors() retrieves only columns excluding the primary key.
		// Note2: If needed, add additional attributes like builderForPostGIS.add("new_attr", String.class);
		// However, this should be a column actually present in the PostGIS table!
		
		// Create a FeatureType containing column information for the postGIS table
		SimpleFeatureType postGISFeatureType = builderForPostGIS.buildFeatureType();
		
		// Declare a transaction; assignment will be done inside the try-catch block.
		Transaction transaction = null;
		
		// Amount of data to be sent to the database at once
		final int BATCH_SIZE = 1000;
		
		// Counting the number of data accumulating in one transaction
		int count = 0;
		
		try {
			
			// Set the transaction to the FeatureStore where we want to perform the write operation.
			transaction = new DefaultTransaction("POSTGIS_DATA_APPENDING");
			
			// Extract the FeatureSource where the actual data will be inserted
			SimpleFeatureSource tableFeatureSource = postGisDataStore.getFeatureSource(targetTableName);
			// typename == table name specified
			// Casting SimpleFeatureSource to SimpleFeatureStore for setting the transaction
			// A crucial caution!
			// If there is no numeric primary key in the table where you want to insert data,
			// an error will occur in the following Class Casting! Make sure to check the presence
			// of a numeric primary key in the table where you want to insert data!
			SimpleFeatureStore tableFeatureStore = (SimpleFeatureStore) tableFeatureSource;
			tableFeatureStore.setTransaction(transaction);
			
			// Create an iterator to read features from the shapefile
			ContentFeatureSource featureSource = shapeFileDataStore.getFeatureSource();
			ContentFeatureCollection featuresCollection = featureSource.getFeatures();
			try (SimpleFeatureIterator features = featuresCollection.features()) {
				
				// Start iterating through the features
				while (features.hasNext()) {
					
					SimpleFeature shapeFileFeature = features.next();
					SimpleFeature transformedFeature = DataUtilities.template(postGISFeatureType);
					
					// Warning! Never set Attribute for a database table numeric primary key!
					// transformedFeature.setAttribute("fid", shapeFileFeature.getAttribute("????")); ==> don't do this!
					
					// transform shapefile data to table data
					transformedFeature.setAttribute("id", shapeFileFeature.getAttribute("id"));
					transformedFeature.setAttribute("name", shapeFileFeature.getAttribute("name"));
					transformedFeature.setDefaultGeometryProperty(shapeFileFeature.getDefaultGeometryProperty());
					tableFeatureStore.addFeatures(DataUtilities.collection(transformedFeature));
					
					count++;
					if (count % BATCH_SIZE == 0) {
						transaction.commit();
						transaction.close();
						tableFeatureStore.setTransaction(null);
						transaction = new DefaultTransaction("POSTGIS_DATA_APPENDING");
						tableFeatureStore.setTransaction(transaction);
					}
				}
				
				// Features added with tableFeatureStore.addFeature might not have been committed yet even
				// after the while loop. Commit them.
				if ((count % BATCH_SIZE) != 0) {
					transaction.commit();
				}
			}
			
		} catch (Exception e) {
			if (Objects.nonNull(transaction)) try {transaction.rollback();} catch (IOException ex) {/* ignore */}
			e.printStackTrace(System.err);
		} finally {
			if (Objects.nonNull(transaction)) try {transaction.close();} catch (IOException ex) {/* ignore */}
			postGisDataStore.dispose();
			shapeFileDataStore.dispose();
		}
	}
}

package coding.toast.geotools.postgis;

import coding.toast.geotools.utils.DataStoreUtil;
import coding.toast.geotools.utils.PostGisUtil;
import org.geotools.feature.type.GeometryDescriptorImpl;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.referencing.CRS;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.referencing.FactoryException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

/**
 * Reading PostGIS Table Meta Info
 */
public class PostGisMetaDataReadTest {
	private static JDBCDataStore postGisDataStore;
	
	@BeforeAll
	static void beforeAll() throws IOException {
		postGisDataStore = PostGisUtil.getPostGisDataStore(
			"postgis", // db type
			"localhost",   // db server host
			"5432",        // db server port
			"postgres",    // database name
			"public",      // db schema name
			"postgres",    // db connection user id
			"root"         // db connection password
		);
	}
	
	@AfterAll
	static void afterAll() {
		DataStoreUtil.closeDataStores(postGisDataStore);
	}
	
	@Test
	void createSimpleGeometryTableTest() throws IOException, FactoryException {
		
		// read All Table inside schema that i config on [PostGisUtil.getPostGisDataSource] method
		String[] tableNames = postGisDataStore.getTypeNames();
		
		if (tableNames.length == 0) {
			System.out.println("No Table Information found in this database schema : "
				+ postGisDataStore.getDatabaseSchema());
			return;
		}
		
		System.out.println("Database Schema : " + postGisDataStore.getDatabaseSchema());
		System.out.println("Table List : " + Arrays.toString(tableNames));
		
		// get Any database Table schema info
		int randomIdx = new Random().nextInt(0, tableNames.length);
		String randomSelectedTable = tableNames[randomIdx];
		SimpleFeatureType tableSchema = postGisDataStore.getSchema(randomSelectedTable);
		
		// read table schema info
		for (AttributeDescriptor attributeDescriptor : tableSchema.getAttributeDescriptors()) {
			System.out.println("\ntable column name : " + attributeDescriptor.getType().getName());
			System.out.println("java attribute Type : " + attributeDescriptor.getType().getBinding().getSimpleName());
			if (attributeDescriptor instanceof GeometryDescriptorImpl geometryDescriptor) {
				System.out.println("geometry column CRS Name : " + geometryDescriptor.getCoordinateReferenceSystem().getName());
				System.out.println("geometry column SRID (=EPSG Code) : " + CRS.lookupEpsgCode(geometryDescriptor.getCoordinateReferenceSystem(), false));
			}
			System.out.println("get meta info : " + attributeDescriptor.getUserData());
		}
	}
}

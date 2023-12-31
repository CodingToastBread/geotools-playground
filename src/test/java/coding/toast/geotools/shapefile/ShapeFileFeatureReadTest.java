package coding.toast.geotools.shapefile;

import coding.toast.geotools.utils.DataStoreUtil;
import coding.toast.geotools.utils.OpenEpsgMapUtil;
import coding.toast.geotools.utils.ShapeFileUtil;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.store.ContentFeatureCollection;
import org.geotools.data.store.ContentFeatureSource;
import org.junit.jupiter.api.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.AttributeDescriptor;

import java.io.IOException;
import java.util.List;

/**
 * Test Class for Reading Each Feature Info inside ShapeFile
 */
public class ShapeFileFeatureReadTest {
	@Test
	void readFeatureTest() throws IOException {
		
		ShapefileDataStore shapeFileDataStore
			= ShapeFileUtil.getShapeFileDataStore(
			"src/test/resources/sample/sample.shp",
			"UTF-8");
		
		
		// (1) Shapefile Iterator Loop
		// Note: For loop iteration, three steps are needed.
		//      1. Extract FeatureSource from dataStore
		//      2. Extract FeatureCollection from FeatureSource
		//      3. Extract Iterator from FeatureCollection
		ContentFeatureSource featureSource = shapeFileDataStore.getFeatureSource();
		ContentFeatureCollection featureCollection = featureSource.getFeatures();
		try (SimpleFeatureIterator shpFileFeatureIterator = featureCollection.features()) {
			
			while (shpFileFeatureIterator.hasNext()) {
				SimpleFeature feature = shpFileFeatureIterator.next();
				int attributeCount = feature.getAttributeCount();
				
				List<AttributeDescriptor> attributeDescriptors
					= feature.getFeatureType().getAttributeDescriptors();
				
				for (int i = 0; i < attributeCount; i++) {
					System.out.println(
						attributeDescriptors.get(i).getLocalName()  // key
							+ " : " + feature.getAttribute(i)       // value
					);
				}
				
				System.out.println("=========================================");
			}
		}
		
		DataStoreUtil.closeDataStores(shapeFileDataStore);
		
		// If you want to see the actual location of the POINT (=the_geom) displayed,
		// uncomment the line below and check!
		// OpenEpsgMapUtil.showMap(5186, 317806.88781702635, 563786.2655425679, 14);
	}
}

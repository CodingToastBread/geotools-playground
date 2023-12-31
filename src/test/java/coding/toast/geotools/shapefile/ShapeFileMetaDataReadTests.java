package coding.toast.geotools.shapefile;

import coding.toast.geotools.utils.ShapeFileUtil;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.type.GeometryDescriptorImpl;
import org.geotools.referencing.CRS;
import org.junit.jupiter.api.Test;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Test Class for Reading ShapeFile MetaData
 */
public class ShapeFileMetaDataReadTests {
	
	@Test
	void readShapeFileMetaData() throws IOException, FactoryException {
		ShapefileDataStore shapeFileDataStore = ShapeFileUtil.getShapeFileDataStore(
			"src/test/resources/sample/sample.shp",
			"UTF-8");
		
		CoordinateReferenceSystem shapeFileCrs = shapeFileDataStore.getSchema().getCoordinateReferenceSystem();
		
		// (1) Extract EPSG CODE
		// It may return null. This could happen if the gt-reference implementation library
		// fails to read CRS correctly or if the shapefile prj is corrupted.
		Integer epsgCode = CRS.lookupEpsgCode(shapeFileCrs, false);
		System.out.println("\n(1) EPSG CODE : " + epsgCode);
		
		// (2) Extract TypeName
		// Retrieve the GeoTools recognized entry (type) name of the shapefile.
		// This typeName is an important value used later when extracting only one
		// entry from various entries in the datastore.
		String typeName = shapeFileDataStore.getTypeNames()[0]; // Since one shapefile is specified, there is always one TypeName.
		System.out.println("\n(2) TypeName Extracted : " + Arrays.toString(shapeFileDataStore.getTypeNames()));
		
		// (3) Query schema information
		// SimpleFeatureType schema = shapeFileDataStore.getSchema();
		SimpleFeatureType schema = shapeFileDataStore.getSchema(typeName);
		System.out.println("\n(3) Schema Information Query: " + schema);
		
		// (4) Extract geometry attribute name
		// Note: There is no attribute for geometry in the dbf file.
		// However, GeoTools provides one attribute named "the_geom" for convenience.
		String geomName = schema.getGeometryDescriptor().getLocalName();
		System.out.println("\n(4) Extracted geometry attribute name: " + geomName);
		
		// (5) Extract geometry type
		String geomTypeName = schema.getGeometryDescriptor().getType().getBinding().getSimpleName();
		System.out.println("\n(5) Extracted geometry type: " + geomTypeName);
		
		// (6) Query attribute information for each Feature.
		System.out.println("\n============================= (6) Querying Each Feature's Attribute Information [START] =============================");
		List<AttributeDescriptor> attributeDescriptors = schema.getAttributeDescriptors();
		for (AttributeDescriptor attributeDescriptor : attributeDescriptors) {
			
			// Let's delve deeper into the geometry type.
			if (attributeDescriptor instanceof GeometryDescriptorImpl geometryDescriptor) {
				System.out.println("\nGeometry Attribute Found!");
				
				// Note: The geometry attribute in the shapefile doesn't have a specified attribute name in the dbf file.
				// Despite that, the output below shows "the_geom." This is an internally designated name by geotools,
				// not the actual content in the shapefile. Keep in mind that!
				System.out.println("Geometry Attr LocalName" + geometryDescriptor.getLocalName());
				System.out.println("Geometry Attr CoordinateReferenceSystem" + geometryDescriptor.getCoordinateReferenceSystem());
				System.out.println("Geometry Attr Type" + geometryDescriptor.getType());
				continue;
			}
			
			// Querying the rest of the AttributeTypeImpl types
			System.out.println("Attribute name : " + attributeDescriptor.getName()
				+ " , Attribute Type : " + attributeDescriptor.getType().getClass()
				+ " , Attribute Binding Data Type : "
				+ attributeDescriptor.getType().getBinding().getSimpleName());
		}
		System.out.println("\n============================= (6) Querying Each Feature's Attribute Information [END] =============================");
		
	}
}

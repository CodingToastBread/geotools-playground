package coding.toast.geotools.reference;

import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

public class TransformTests {
	
	@BeforeAll
	static void beforeAll() {
		// Setting this property is necessary to avoid interpreting X,Y as Y,X, causing a frustrating issue.
		// Be sure to set this property before starting the service! Alternatively, passing it as a VM option globally is also recommended.
		// The reason why we need this configuration is explained in great detail here:
		// https://docs.geotools.org/latest/userguide/library/referencing/order.html.
		System.setProperty("org.geotools.referencing.forceXY", "true");
	}
	
	@Test
	@DisplayName("Transform Deoksugung Palace's Coordinates from EPSG:4326 to EPSG:5179")
	void transFormTest() throws FactoryException, TransformException, IOException {
		
		// Coordinates of Deoksugung Palace 😎
		String coordX = "126.97476625442985";
		String coordY = "37.565611356905336";
		
		CoordinateReferenceSystem sourceCrs = CRS.decode("EPSG:4326");
		CoordinateReferenceSystem targetCrs = CRS.decode("EPSG:5179");
		
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
		Coordinate coordinate = new Coordinate(Double.parseDouble(coordX), Double.parseDouble(coordY));
		Point sourcePoint = geometryFactory.createPoint(coordinate);
		
		MathTransform transform = CRS.findMathTransform(sourceCrs, targetCrs, true);
		Point transformPoint = (Point) JTS.transform(sourcePoint, transform);
		
		System.out.println("Point before transformation (EPSG:4326) = " + sourcePoint);
		System.out.println("Point after transformation (EPSG:5179) = " + transformPoint);
		
		// Uncomment the code below if you want to see it visually!
        /*if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(createEpsgIoURI("4326", sourcePoint, 18));
            Desktop.getDesktop().browse(createEpsgIoURI("5179", transformPoint, 18));
        }*/
		
	}
	
	private URI createEpsgIoURI(String srs, Point point, int zIndex) {
		return URI.create("https://epsg.io/map#srs=%s&x=%s&y=%s&z=%s&layer=streets".formatted(srs, point.getX(), point.getY(), zIndex));
	}
	
}

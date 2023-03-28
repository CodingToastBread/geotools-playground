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
		// 이걸 안 하면 X,Y 를 Y,X 를 반대로 해석하여 굉장히 짜증나는 문제를 발생시킵니다.
		// 꼭 서비스 시작전에 이 프로퍼티를 세팅하시기 바랍니다! 아니면 VM Option 으로 줘서 전역적으로 쓰는 것도 추천합니다.
		// 이러는 이유는 https://docs.geotools.org/latest/userguide/library/referencing/order.html 에 아주 상세히 나옵니다.
		System.setProperty("org.geotools.referencing.forceXY", "true");
	}
	
	@Test
	@DisplayName("덕수궁의 좌표를 4326 -> 5179 로 바꿔보죠!")
	void transFormTest() throws FactoryException, TransformException, IOException {
		
		// 덕수궁의 좌표입니다 😎
		String coordX = "126.97476625442985";
		String coordY = "37.565611356905336";
		
		CoordinateReferenceSystem sourceCrs = CRS.decode("EPSG:4326");
		CoordinateReferenceSystem targetCrs = CRS.decode("EPSG:5179");
		
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
		Coordinate coordinate = new Coordinate(Double.parseDouble(coordX), Double.parseDouble(coordY));
		Point sourcePoint = geometryFactory.createPoint(coordinate);
		
		MathTransform transform = CRS.findMathTransform(sourceCrs, targetCrs, true);
		Point transformPoint = (Point) JTS.transform(sourcePoint, transform);
		
		System.out.println("좌표변경 전(EPSG:4326) Point = " + sourcePoint);
		System.out.println("좌표변경 후(EPSG:5179) Point = " + transformPoint);
		
		// 눈으로 직접 보시고 싶다면 아래 주석을 풀어주세요!
		/*if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			Desktop.getDesktop().browse(createEpsgIoURI("4326", sourcePoint, 18));
			Desktop.getDesktop().browse(createEpsgIoURI("5179", transformPoint, 18));
		}*/
		
	}
	
	private URI createEpsgIoURI(String srs, Point point, int zIndex) {
		return URI.create("https://epsg.io/map#srs=%s&x=%s&y=%s&z=%s&layer=streets".formatted(srs, point.getX(), point.getY(), zIndex));
	}
	
	
}

package coding.toast.geotools.utils;

import org.locationtech.jts.geom.Point;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;


/**
 * This Util Class helps you to see your Point information on <a href="https://epsg.io/map">epsg Map View</a>
 */
public class OpenEpsgMapUtil {
	
	public static void showMap(int epsgCode, Point point, int zIndex) {
		Objects.requireNonNull(point, "point argument is required!");
		showMap(epsgCode, point.getX(), point.getY(), zIndex);
	}
	
	public static void showMap(int epsgCode, double x, double y, int zIndex) {
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			try {
				Desktop.getDesktop().browse(createEpsgIoURI(epsgCode, x, y, 18));
			} catch (IOException e) {
				throw new IllegalStateException("Fail to launch default browser!!");
			}
		}
	}
	
	private static URI createEpsgIoURI(int epsgCode, Point point, int zIndex) {
		return createEpsgIoURI(epsgCode, point.getX(), point.getY(), zIndex);
	}
	
	private static URI createEpsgIoURI(int epsgCode, double x, double y, int zIndex) {
		return URI.create("https://epsg.io/map#srs=%s&x=%s&y=%s&z=%s&layer=streets".formatted(epsgCode, x, y, zIndex));
	}
	
}

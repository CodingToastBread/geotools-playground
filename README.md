# GeoTools Coding Just For Fun 🗺️

Simple GeoTools Module Testing Repository.<br>
> Remark : Please note that all code is written based on JDK 17. Please keep this in mind.<br>
> Remark : I have set the version of GeoTools to 27.2 because it is the minimum version compatible with Java 17.

<br><br>

## 🧭 Coding History

Here is a list of tasks that I have done so far.

- About Reference
  - [How To Transform Point CRS one to another](src/test/java/coding/toast/geotools/reference/TransformTests.java)

<br/>

- About ShapeFile
  - [How To Read ShapeFile MetaInfo](src/test/java/coding/toast/geotools/shapefile/ShapeFileMetaDataReadTests.java)
  - [How To Read ShapeFile Feature Info Using Iterator](src/test/java/coding/toast/geotools/shapefile/ShapeFileFeatureReadTest.java)

<br/>

- About PostGIS
  - [How To Read PostGIS Meta Info](src/test/java/coding/toast/geotools/postgis/PostGisMetaDataReadTest.java)
  - [How To Create PostGIS Table using GeoTools](src/test/java/coding/toast/geotools/postgis/CreateTableUsingGeoToolsTest.java)
  - [How To Create PostGIS Table Via ShapeFile](src/test/java/coding/toast/geotools/postgis/CreateTableViaShapeFileTest.java)
  - [How To import Data from Shapefile To Using PostGIS Table](src/test/java/coding/toast/geotools/postgis/ShapeFileToDatabaseTableAppendingTest.java)

<br/>

- Util Class For GeoTools Development
  - [DataStoreUtil](src/test/java/coding/toast/geotools/utils/DataStoreUtil.java)
  - [OpenEpsgMapUtil](src/test/java/coding/toast/geotools/utils/OpenEpsgMapUtil.java)
  - [ShapeFileUtil](src/test/java/coding/toast/geotools/utils/ShapeFileUtil.java)
  - [PostGisUtil](src/test/java/coding/toast/geotools/utils/PostGisUtil.java)

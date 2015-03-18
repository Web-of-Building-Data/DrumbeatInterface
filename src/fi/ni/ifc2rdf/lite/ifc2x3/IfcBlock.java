package fi.ni.ifc2rdf.lite.ifc2x3;
import fi.ni.ifc2rdf.lite.*;
import fi.ni.ifc2rdf.lite.ifc2x3.interfaces.*;

import java.util.*;

/*
 * IFC Java class
 * @author Jyrki Oraskari
 * @license This work is licensed under a Creative Commons Attribution 3.0 Unported License.
 * http://creativecommons.org/licenses/by/3.0/ 
 */

public class IfcBlock extends IfcCsgPrimitive3D 
{
 // The property attributes
 Double xLength;
 Double yLength;
 Double zLength;


 // Getters and setters of properties

 public Double getXLength() {
   return xLength;
 }
 public void setXLength(String txt){
   Double value = i.toDouble(txt);
   this.xLength=value;

 }

 public Double getYLength() {
   return yLength;
 }
 public void setYLength(String txt){
   Double value = i.toDouble(txt);
   this.yLength=value;

 }

 public Double getZLength() {
   return zLength;
 }
 public void setZLength(String txt){
   Double value = i.toDouble(txt);
   this.zLength=value;

 }

}

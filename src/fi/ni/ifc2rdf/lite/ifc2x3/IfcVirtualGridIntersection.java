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

public class IfcVirtualGridIntersection extends Thing 
{
 // The property attributes
 List<IfcGridAxis> intersectingAxes = new IfcList<IfcGridAxis>();
 List<Double> offsetDistances = new IfcList<Double>();


 // Getters and setters of properties

 public List<IfcGridAxis> getIntersectingAxes() {
   return intersectingAxes;

 }
 public void setIntersectingAxes(IfcGridAxis value){
   this.intersectingAxes.add(value);

 }

 public List<Double> getOffsetDistances() {
   return offsetDistances;
 }
 public void setOffsetDistances(String txt){
   List<Double> value = i.toDoubleList(txt);
   this.offsetDistances=value;

 }

}

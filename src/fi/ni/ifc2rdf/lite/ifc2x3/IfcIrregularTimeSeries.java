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

public class IfcIrregularTimeSeries extends IfcTimeSeries 
{
 // The property attributes
 List<IfcIrregularTimeSeriesValue> values = new IfcList<IfcIrregularTimeSeriesValue>();


 // Getters and setters of properties

 public List<IfcIrregularTimeSeriesValue> getValues() {
   return values;

 }
 public void setValues(IfcIrregularTimeSeriesValue value){
   this.values.add(value);

 }

}

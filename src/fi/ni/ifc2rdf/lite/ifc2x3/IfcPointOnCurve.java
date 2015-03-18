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

public class IfcPointOnCurve extends IfcPoint 
{
 // The property attributes
 IfcCurve   basisCurve;
 Double pointParameter;


 // Getters and setters of properties

 public IfcCurve getBasisCurve() {
   return basisCurve;

 }
 public void setBasisCurve(IfcCurve value){
   this.basisCurve=value;

 }

 public Double getPointParameter() {
   return pointParameter;
 }
 public void setPointParameter(String txt){
   Double value = i.toDouble(txt);
   this.pointParameter=value;

 }

}

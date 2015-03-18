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

public class IfcConnectionCurveGeometry extends IfcConnectionGeometry 
{
 // The property attributes
IfcCurveOrEdgeCurve curveOnRelatingElement;
IfcCurveOrEdgeCurve curveOnRelatedElement;


 // Getters and setters of properties

 public IfcCurveOrEdgeCurve getCurveOnRelatingElement() {
   return curveOnRelatingElement;
 }
 public void setCurveOnRelatingElement(IfcCurveOrEdgeCurve value){
   this.curveOnRelatingElement=value;

 }

 public IfcCurveOrEdgeCurve getCurveOnRelatedElement() {
   return curveOnRelatedElement;
 }
 public void setCurveOnRelatedElement(IfcCurveOrEdgeCurve value){
   this.curveOnRelatedElement=value;

 }

}

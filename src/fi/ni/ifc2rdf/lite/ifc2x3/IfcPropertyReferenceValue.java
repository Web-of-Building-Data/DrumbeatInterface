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

public class IfcPropertyReferenceValue extends IfcSimpleProperty 
{
 // The property attributes
 String usageName;
IfcObjectReferenceSelect propertyReference;


 // Getters and setters of properties

 public String getUsageName() {
   return usageName;
 }
 public void setUsageName(String value){
   this.usageName=value;

 }

 public IfcObjectReferenceSelect getPropertyReference() {
   return propertyReference;
 }
 public void setPropertyReference(IfcObjectReferenceSelect value){
   this.propertyReference=value;

 }

}

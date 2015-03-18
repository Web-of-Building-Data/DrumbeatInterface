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

public class IfcRelDecomposes extends IfcRelationship 
{
 // The property attributes
 IfcObjectDefinition   relatingObject;
 List<IfcObjectDefinition> relatedObjects = new IfcSet<IfcObjectDefinition>();


 // Getters and setters of properties

 public IfcObjectDefinition getRelatingObject() {
   return relatingObject;

 }
 public void setRelatingObject(IfcObjectDefinition value){
   this.relatingObject=value;

 }

 public List<IfcObjectDefinition> getRelatedObjects() {
   return relatedObjects;

 }
 public void setRelatedObjects(IfcObjectDefinition value){
   this.relatedObjects.add(value);

 }

}

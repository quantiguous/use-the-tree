/*
 * Copyright 2014 NH Consulting
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.usethetree;

import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.Iterable;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

public class Reference implements Iterable<Reference>{

  // There are rules and there are (valid) exceptions: It is ACTUALLY REALLY better to not use getters and setters here:
  public String elemName;
  public String value;
  public HashMap<Key, Reference> children = new HashMap<Key, Reference>();
  private HashMap<String, Integer> repeatingElementsIndex = new HashMap<String, Integer>(); 
  public Reference parent = null;
  public Reference firstChild = null;
  public Reference lastChild = null;
  public Reference nextSibling = null;
  public Reference prevSibling = null;
   
 

  public Reference(String elemName) {
    this.elemName = elemName;
  }

  public Reference(String elemName, String value) {
	    this.elemName = elemName;
	    this.value = value;
	  }
  
  public Reference(Reference parent, String elemName, String value) {
	  this.parent = parent;
	  this.elemName = elemName;
	  this.value = value;
	 }
  
  public Reference addChild(String key, String elemName, String value) {
	  Reference child = new Reference(this, elemName, value);
	  
	  int repeatingElemIndex = 0;			// The first (repeating) element with name XYZ
	  Reference tmp = this.firstChild(key);
	  Integer i = null;
	  if (tmp!=null) {
			  i = repeatingElementsIndex.get(key);
		  if (i==null)
			  repeatingElemIndex=1;					// The second
		  else
			  repeatingElemIndex=i+1;				// third, ...
	  }
	  
	  if (!this.children.isEmpty()) {
		  child.prevSibling=this.lastChild;
		  this.lastChild.nextSibling=child;
		  this.lastChild=child;
		  
	  } else {
		  this.firstChild=child;
		  this.lastChild=child;
	  }
		  
	  this.children.put(new Key(key, repeatingElemIndex), child);
	  if (repeatingElemIndex>0)
		  repeatingElementsIndex.put(key, repeatingElemIndex);
	  
      return child;
  }
  
  public Reference addChild(String key, String elemName) {
	  return addChild(key, elemName, null);
  }

  public Reference addChild(String elemName) {
	  return addChild(elemName, elemName, null);
  }
  
  public Reference set(String key, String elemName) {			// move to first child OR CREATE

	  Reference tmp = this.children.get(new Key(key));
	  if (tmp==null)
		  return this.addChild(key, elemName);
	  else
		  return tmp;
	  
  }
  
  public Reference firstChild(String key) {					// move to first child OR NULL

	 return this.children.get(new Key(key));
	  
  }
  
  public Reference child(String elemName, int index) {

	  return this.children.get(new Key(elemName, index));

  }
  
  public Reference firstChild(String elemName, String key, String value) {
	  
	  Reference child = this.firstChild(elemName);
	  if (child!=null) {
		  Reference curChild = child.firstChild(key);
		  while (curChild!=null) {
			  if (curChild.value.equals(value))
				  return child;
			  if (curChild.nextSibling!=null&&curChild.nextSibling.elemName.equals(key))
				  curChild=curChild.nextSibling;
			  else 
				curChild=null;  
		 }
	  }
	
	  return null;
  }
  
public Reference set(String elemName) {
	  
	  Reference tmp =  this.firstChild(elemName);
	  if (tmp==null)
			return this.addChild(elemName);
	  else
		  return tmp;
  }
  
  public Reference set(String key, String elemName, String value) {
	  
	  Reference tmp = this.firstChild(key);
	  if (tmp==null)
			return this.addChild(key, elemName, value);
	  else
		  return tmp;
  }
  
 public Reference add(String elemName, String value) {
	  
	  Reference tmp = this.firstChild(elemName);
	  if (tmp==null)
			return this.addChild(elemName, elemName, value);
	  else {
		   tmp.value = "" + (Integer.parseInt(tmp.value) + Integer.parseInt(value));
		   return tmp;
	  }	   
  }
  
  
  public Reference moveWhere(String elemName, String key, String value) {
	  Reference tmp = firstChild(elemName, key, value);
	  if (tmp!=null)
		  return tmp;
	  else
		  return this.addChild(elemName);
  }  
  
  public void removeFieldFromChildren(String childrenName, String key) {
	  
	  Reference curChild = this.firstChild(childrenName);
	  
	  while (curChild!=null) {
	  
		  Reference tmpReference = curChild.firstChild(key);
		  if (tmpReference!=null)
			  curChild.children.remove(tmpReference);
		  if (curChild.nextSibling!=null&&curChild.nextSibling.elemName.equals(childrenName))
			  curChild=curChild.nextSibling;
		  else 
			  curChild=null;  
		  
	  }  
	 
  }
  
  
  
  public Reference addNextSibling(String elemName) {
	  return this.parent.addChild(elemName);
  }

  
  @Override
  public Iterator<Reference> iterator() {
	  
      return new ChildrenIterator(this);
  }
  
  
  public static Reference createLogicalMsgTreeFromXML(InputStream in) throws XMLStreamException  {
		
	    XMLStreamReader xmlStreamReader = null;
	    int eventType = 0;
	    XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

	    xmlStreamReader = xmlInputFactory.createXMLStreamReader(in);	
			
	    // when XMLStreamReader is created, 
	    // it is positioned at START_DOCUMENT event.
	    eventType = xmlStreamReader.getEventType();

//		int depth = 0;
		
		String curElem = "";
		String curValue = "";
		final Reference inputRoot = new Reference("InputRoot");	
		
		Reference curTree = inputRoot;
		boolean lastTagWasAnOpeningTag = false;
		
	  	
			while(xmlStreamReader.hasNext()) {
			    eventType = xmlStreamReader.next();
			    
			    switch (eventType) {
			        case XMLStreamConstants.START_ELEMENT:				        	
			        	curElem = xmlStreamReader.getLocalName();
			        	curTree = curTree.addChild(curElem);
			        	lastTagWasAnOpeningTag = true;
//			        	depth+=1;
			            break;
			        case XMLStreamConstants.END_ELEMENT:
			        	if (lastTagWasAnOpeningTag)
			        		curTree.value = curValue;
			        	curTree = curTree.parent;     	
			        	lastTagWasAnOpeningTag = false;
//			        	depth-=1;
			            break;
			        case XMLStreamConstants.PROCESSING_INSTRUCTION:
			            break;
			        case XMLStreamConstants.CHARACTERS:
			        	curValue = xmlStreamReader.getText();
			            break;
			        case XMLStreamConstants.COMMENT:
			            break;
			        case XMLStreamConstants.START_DOCUMENT:
			            break;
			        case XMLStreamConstants.ATTRIBUTE:
			        	break;
			        case XMLStreamConstants.END_DOCUMENT:
			            break;
			        case XMLStreamConstants.ENTITY_REFERENCE:
			            break;
			        case XMLStreamConstants.DTD:
			            break;
			        case XMLStreamConstants.CDATA:
			            break;
			        case XMLStreamConstants.SPACE:
			            break;
			        default:   	
			    }
			}
			return inputRoot;

	  }

  public static Reference createLogicalMsgTreeFromEDIEdifact(InputStream in) throws Exception   {
		
	  
		final Reference inputRoot = new Reference("InputRoot");	
		
		Reference curTree = inputRoot.addChild("EDIFACT");
		BufferedReader reader = null;
		
		
		// Prefill with default values (if no UNA segment is given):
		String componentElementSeparator = ":";		//  Component data element separator (separating simple data elements within a composite data element)
		String dataElementSeparator = "+";
		String decimalNotation = ".";
		String releaseCharacter = "?";
		String repetitionSeparator = "*";
		String segmentSeparator = "\'";		// segment terminator
		
		String[] dataElements = null;
		String[] componentElements = null;
		
		reader = new BufferedReader(new InputStreamReader(in));
     
		String line = reader.readLine();
		while(line != null){
			
			if (line.startsWith("UNA")) {     // Service string advice
				
				curTree = curTree.addChild("UNA");

				componentElementSeparator = Character.toString(line.charAt(3));
				dataElementSeparator = Character.toString(line.charAt(4));
				decimalNotation = Character.toString(line.charAt(5));
				releaseCharacter = Character.toString(line.charAt(6));
				repetitionSeparator = Character.toString(line.charAt(7));
				segmentSeparator = Character.toString(line.charAt(8));
				
				curTree.addChild("ComponentElementSeparator").value = componentElementSeparator;
				curTree.addChild("DataElementSeparator").value = dataElementSeparator;
				curTree.addChild("DecimalNotation").value = decimalNotation;
				curTree.addChild("ReleaseCharacter").value = releaseCharacter;
				curTree.addChild("RepetitionSeparator").value = repetitionSeparator;
				curTree.addChild("SegmentSeparator").value = segmentSeparator;
				
				curTree = curTree.parent;
				
			} else {
				
				if (line.endsWith(segmentSeparator))
					line = line.substring(0, line.length()-1);
				else
					throw new Exception("Edifact segments need to end with the Segment Separator and a new line");
				
				
				if (line.startsWith("UNB")) {	// Segment Layout
				
					curTree = curTree.addChild("UNB");
					dataElements = line.split(Pattern.quote(dataElementSeparator));
					
					componentElements = dataElements[1].split(Pattern.quote(componentElementSeparator));
					curTree.addChild("SyntaxIdentifier").value = componentElements[0];
					curTree.addChild("SyntaxVersionNumber").value = componentElements[1];
					
					componentElements = dataElements[2].split(Pattern.quote(componentElementSeparator));
					curTree.addChild("InterchangeSenderIdentification").value = componentElements[0];
					if (componentElements.length>1&&!componentElements[1].equals(""))
							curTree.addChild("IdentificationCodeQualifier").value = componentElements[1];
				
					componentElements = dataElements[3].split(Pattern.quote(componentElementSeparator));
					curTree.addChild("InterchangeRecipientIdentification").value = componentElements[0];
					if (componentElements.length>1&&!componentElements[1].equals(""))
							curTree.addChild("IdentificationCodeQualifier").value = componentElements[1];
					
					componentElements = dataElements[4].split(Pattern.quote(componentElementSeparator));
					curTree.addChild("Date").value = componentElements[0];
					curTree.addChild("Time").value = componentElements[1];
					
					curTree.addChild("InterchangeControlReference").value = dataElements[5];
					
					
				} else if (line.startsWith("LIN")) {	// A line item
					
					curTree = curTree.addChild("LINE_ITEM");
					dataElements = line.split(Pattern.quote(dataElementSeparator));
					if (dataElements.length>1&&!dataElements[1].equals(""))
						curTree.addChild("LINE_ITEM_IDENTIFIER ").value = dataElements[1];
					if (dataElements.length>2&&!dataElements[2].equals(""))
						curTree.addChild("ACTION_REQUEST_DESCRIPTION_CODE").value = dataElements[2];			// Action request/notification description code
					
					if (dataElements.length>3&&!dataElements[3].equals("")) {
						componentElements = dataElements[3].split(Pattern.quote(componentElementSeparator));
						curTree.addChild("ITEM_NUMBER_IDENTIFICATION");
						if (componentElements.length>0&&!componentElements[0].equals(""))
							curTree.lastChild.addChild("ItemIdentifier").value = componentElements[0];
						if (componentElements.length>1&&!componentElements[1].equals(""))
							curTree.lastChild.addChild("ItemTypeIdentificationCode").value = componentElements[1];
						if (componentElements.length>2&&!componentElements[2].equals(""))
							curTree.lastChild.addChild("CodeListIdentificationCode").value = componentElements[2];
						if (componentElements.length>3&&!componentElements[3].equals(""))
							curTree.lastChild.addChild("CodeListResponsibleAgencyCode").value = componentElements[3];
					}
					
					if (dataElements.length>4&&!dataElements[4].equals("")) {
						componentElements = dataElements[4].split(Pattern.quote(componentElementSeparator));
						curTree.addChild("SUB_LINE_INFORMATION");
						if (componentElements.length>0&&!componentElements[0].equals(""))
							curTree.lastChild.addChild("SubLineIndicatorCode").value = componentElements[0];
						if (componentElements.length>1&&!componentElements[1].equals(""))
							curTree.lastChild.addChild("LineItemIdentifier").value = componentElements[1];
					}
					
					if (dataElements.length>5&&!dataElements[5].equals(""))
						curTree.addChild("CONFIGURATION_LEVEL_NUMBER").value = dataElements[5];
					if (dataElements.length>2&&!dataElements[2].equals(""))
						curTree.addChild("CONFIGURATION_OPERATION_CODE").value = dataElements[2];			// Action request/notification description code
	
					curTree = curTree.parent;
					
					
				} else if (line.startsWith("QTY")) {	//  Edifact: QTY QUANTITY DETAILS    http://editipsandtrick.blogspot.de/2012/10/edifact-qty-quantity-details.html
					
					curTree = curTree.addChild("QUANTITY");
					dataElements = line.split(Pattern.quote(dataElementSeparator));
					componentElements = dataElements[1].split(Pattern.quote(componentElementSeparator));
					curTree.addChild("QUANTITY_DETAILS");
					curTree.lastChild.addChild("QuantityTypeCodeQualifier").value = componentElements[0];    // List: http://www.unece.org/trade/untdid/d00a/tred/tred6063.htm
					curTree.lastChild.addChild("Quantity").value = componentElements[1];
					if (componentElements.length>2)
						curTree.lastChild.addChild("MeasurementUnitCode").value = componentElements[2];		// List:http://www.unece.org/fileadmin/DAM/cefact/recommendations/rec20/rec20_rev3_Annex3e.pdf
					curTree = curTree.parent;
					
				} else if (line.startsWith("DTM")) {	// Edifact DTM: DATE/TIME/PERIOD    http://editipsandtrick.blogspot.de/2012/10/edifact-dtm-datetimeperiod.html
					
					curTree = curTree.addChild("DTM");
					dataElements = line.split(Pattern.quote(dataElementSeparator));
					componentElements = dataElements[1].split(Pattern.quote(componentElementSeparator));
					curTree.addChild("FunctionCodeQualifier").value = componentElements[0];							// Date or time or period function code qualifier (List: http://www.unece.org/trade/untdid/d00a/tred/tred2005.htm)
					curTree.addChild("Value").value = componentElements[1];											// Date or time or period value
					if (componentElements.length>2)
						curTree.addChild("FormatCode").value = componentElements[2];	// Date or time or period format code (List: http://www.unece.org/trade/untdid/d00a/tred/tred2379.htm)
					curTree = curTree.parent;
				
				} else if (line.startsWith("BGM")) {	
					
					curTree = curTree.addChild("BEGINNING_OF_MESSAGE");
					dataElements = line.split(Pattern.quote(dataElementSeparator));
					
					if (dataElements.length>1&&!dataElements[1].equals("")) {
						curTree.addChild("DOCUMENT_MESSAGE_NAME");
						componentElements = dataElements[1].split(Pattern.quote(componentElementSeparator));
						if (componentElements.length>0&&!componentElements[0].equals(""))
							curTree.lastChild.addChild("DocumentNameCode").value = dataElements[0];	
						if (componentElements.length>1&&!componentElements[1].equals(""))
							curTree.lastChild.addChild("CodeListIdentificationCode").value = dataElements[1];
						if (componentElements.length>2&&!componentElements[2].equals(""))
							curTree.lastChild.addChild("CodeListResponsibleAgencyCode").value = dataElements[2];
						if (componentElements.length>3&&!componentElements[3].equals(""))
							curTree.lastChild.addChild("DocumentName").value = dataElements[3];
					}
					
					if (dataElements.length>2&&!dataElements[2].equals("")) {
						curTree.addChild("DOCUMENT_IDENTIFICATION");
						componentElements = dataElements[1].split(Pattern.quote(componentElementSeparator));
						if (componentElements.length>0&&!componentElements[0].equals(""))
							curTree.lastChild.addChild("DocumentIdentifier").value = dataElements[0];	
						if (componentElements.length>1&&!componentElements[1].equals(""))
							curTree.lastChild.addChild("VersionIdentifier").value = dataElements[1];
						if (componentElements.length>2&&!componentElements[2].equals(""))
							curTree.lastChild.addChild("RevisionIdentifier").value = dataElements[2];
					}	
					
					if (dataElements.length>3&&!dataElements[3].equals(""))
						curTree.addChild("MESSAGE_FUNCTION_CODE").value = dataElements[3];
					
					if (dataElements.length>4&&!dataElements[4].equals(""))
						curTree.addChild("RESPONSE_TYPE_CODE").value = dataElements[4];

					curTree = curTree.parent;
				
				} else if (line.startsWith("UNH")) {	// Edifact: UNH MESSAGE HEADER    http://editipsandtrick.blogspot.de/2012/10/edifact-unh-message-header.html
					
					curTree = curTree.addChild("UNH");
					dataElements = line.split(Pattern.quote(dataElementSeparator));
					curTree.addChild("MessageReferenceNumber").value = dataElements[1];			// Message reference number, Unique message reference assigned by the sender (List: http://www.unece.org/trade/untdid/d00a/trsd/trsdunh.htm)
					
					componentElements = dataElements[2].split(Pattern.quote(componentElementSeparator));
					curTree.addChild("MessageType").value = componentElements[0];				// Message type. Code identifying a type of message and assigned by its controlling agency (List: http://www.unece.org/trade/untdid/d00a/trsd/trsdunh.htm)
					curTree.addChild("MessageVersionNumber").value = componentElements[1];		// Message version number, Version number of a message type (List: http://www.unece.org/trade/untdid/d00a/trsd/trsdunh.htm)
					curTree.addChild("MessageReleaseNumber").value = componentElements[2];		// Message release number, number within the current message version number (List: http://www.unece.org/trade/untdid/d00a/trsd/trsdunh.htm)
					curTree.addChild("ControllingAgency").value = componentElements[3];			// Controlling agency, coded, Code identifying a controlling agency (List: http://www.unece.org/trade/untdid/d00a/trsd/trsdunh.htm)
					if (componentElements.length>4&&!componentElements[4].equals(""))
						curTree.addChild("AssociationAssignedCode").value = componentElements[4];	
					
					
					curTree = curTree.parent;
					
				} else if (line.startsWith("NAD")) {
					
					curTree = curTree.addChild("NAD");
					dataElements = line.split(Pattern.quote(dataElementSeparator));
					curTree.addChild("PartyFunctionCodeQualifier").value = dataElements[1];		
				
					if (dataElements.length>2&&!dataElements[2].equals("")){
						componentElements = dataElements[2].split(Pattern.quote(componentElementSeparator));
						curTree.addChild("PartyIdentifier").value = componentElements[0];
						if (componentElements.length>1&&!componentElements[1].equals(""))
							curTree.addChild("CodeListIdentificationCode").value = componentElements[1];
						if (componentElements.length>1&&!componentElements[2].equals(""))
							curTree.addChild("CodeListResponsibleAgencyCode").value = componentElements[2];		
					}
					
					if (dataElements.length>3&&!dataElements[3].equals("")){
						componentElements = dataElements[3].split(Pattern.quote(componentElementSeparator));
						curTree.addChild("NameAndAddressDescription").value = componentElements[0];
						if (componentElements.length>1&&!componentElements[1].equals(""))
							curTree.addChild("NameAndAddressDescription").value = componentElements[1];
						if (componentElements.length>2&&!componentElements[2].equals(""))
							curTree.addChild("NameAndAddressDescription").value = componentElements[2];	
						if (componentElements.length>3&&!componentElements[3].equals(""))
							curTree.addChild("NameAndAddressDescription").value = componentElements[3];
						if (componentElements.length>4&&!componentElements[4].equals(""))
							curTree.addChild("NameAndAddressDescription").value = componentElements[4];	
					}
					
					if (dataElements.length>4&&!dataElements[4].equals("")){
						componentElements = dataElements[4].split(Pattern.quote(componentElementSeparator));
						curTree.addChild("PartyName").value = componentElements[0];
						if (componentElements.length>1&&!componentElements[1].equals(""))
							curTree.addChild("PartyName").value = componentElements[1];
						if (componentElements.length>2&&!componentElements[2].equals(""))
							curTree.addChild("PartyName").value = componentElements[2];	
						if (componentElements.length>3&&!componentElements[3].equals(""))
							curTree.addChild("PartyName").value = componentElements[3];
						if (componentElements.length>4&&!componentElements[4].equals(""))
							curTree.addChild("PartyName").value = componentElements[4];
						if (componentElements.length>5&&!componentElements[5].equals(""))
							curTree.addChild("PartyNameFormatCode ").value = componentElements[5];	
					}
					
					if (dataElements.length>5&&!dataElements[5].equals("")){
						componentElements = dataElements[5].split(Pattern.quote(componentElementSeparator));
						curTree.addChild("StreetAndNumber").value = componentElements[0];		// Street and number or post office box identifier  
						if (componentElements.length>1&&!componentElements[1].equals(""))
							curTree.addChild("StreetAndNumber").value = componentElements[1];
						if (componentElements.length>2&&!componentElements[2].equals(""))
							curTree.addChild("StreetAndNumber").value = componentElements[2];	
						if (componentElements.length>3&&!componentElements[3].equals(""))
							curTree.addChild("StreetAndNumber").value = componentElements[3];
					}
					
					if (dataElements.length>6&&!dataElements[6].equals("")){
						curTree.addChild("CityName").value = dataElements[6];
					}
					
					if (dataElements.length>7&&!dataElements[7].equals("")){
						componentElements = dataElements[7].split(Pattern.quote(componentElementSeparator));
						if (componentElements.length>0&&!componentElements[0].equals(""))
							curTree.addChild("CountrySubEntityNameCode").value = componentElements[0];
						if (componentElements.length>1&&!componentElements[1].equals(""))
							curTree.addChild("CodeListIdentificationCode").value = componentElements[1];
						if (componentElements.length>2&&!componentElements[2].equals(""))
							curTree.addChild("CodeListResponsibleAgencyCode").value = componentElements[2];	
						if (componentElements.length>3&&!componentElements[3].equals(""))
							curTree.addChild("CountrySubEntityName ").value = componentElements[3];
					}
					
					if (dataElements.length>8&&!dataElements[8].equals("")){
						curTree.addChild("PostalIdentificationCode").value = dataElements[8];
					}
					
					if (dataElements.length>9&&!dataElements[9].equals("")){
						curTree.addChild("CountryNameCode").value = dataElements[9];
					}
					
					curTree = curTree.parent;
					
				} else if (line.startsWith("FTX")) {

					curTree = curTree.addChild("FTX");
					dataElements = line.split(Pattern.quote(dataElementSeparator));
					curTree.addChild("TextSubjectCodeQualifier").value = dataElements[1];		
					curTree.addChild("FreeTextFunctionCode").value = dataElements[2];
					//curTree.addChild("---").value = dataElements[3];
					curTree.addChild("FreeText").value = dataElements[4];
					
					componentElements = dataElements[2].split(Pattern.quote(componentElementSeparator));
					curTree.addChild("PartyIdentifier").value = componentElements[0];
					//curTree.addChild("---").value = componentElements[1];
					curTree.addChild("CodeListResponsibleAgencyCode").value = componentElements[2];		
					
					curTree = curTree.parent;
					
				} else if (line.startsWith("UNS")) {	
					
					curTree = curTree.addChild("UNS");
					dataElements = line.split(Pattern.quote(dataElementSeparator));
					curTree.addChild("SectionIdentification").value = dataElements[1];		
					
					curTree = curTree.parent;
					
				} else if (line.startsWith("CNT")) {	
					
					curTree = curTree.addChild("CNT");
					dataElements = line.split(Pattern.quote(dataElementSeparator));	
					componentElements = dataElements[1].split(Pattern.quote(componentElementSeparator));
					curTree.addChild("ControlTotalTypeCodeQualifier").value = componentElements[0];
					curTree.addChild("ControlTotalQuantity").value = componentElements[1];
					curTree = curTree.parent;
					
				} else if (line.startsWith("UNT")) {	
					
					curTree = curTree.addChild("UNT");
					dataElements = line.split(Pattern.quote(dataElementSeparator));
					curTree.addChild("NumberOfSegmentsInAMessage").value = dataElements[1];
					curTree.addChild("MessageReferenceNumber").value = dataElements[2];
					curTree = curTree.parent;
					
				} else if (line.startsWith("UNZ")) {	
					
					curTree = curTree.addChild("UNZ");
					dataElements = line.split(Pattern.quote(dataElementSeparator));
					curTree.addChild("InterChangeControlCount").value = dataElements[1];
					curTree.addChild("InterchangeControlReference").value = dataElements[2];
					curTree = curTree.parent;
					
				} else {
				
					// throw new Exception("Unknown Edifact segment \"" + line.substring(0,2) + "\" in line \"" + line + "\""  );
					
				}

			}
			
			line = reader.readLine();
		}

		reader.close();

		return inputRoot;

	  }

  
  public static void writeXMLFromLogicalMsgTree(Reference ref, String filename, HttpServletResponse response) throws XMLStreamException, IOException  {
		

	  	StringBuilder type = new StringBuilder("attachment; filename=");
		type.append(filename);
		
		//response.setContentLength( - not known, since writing out streaming - );
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", type.toString());
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLStreamWriter writer = factory.createXMLStreamWriter(
	               response.getOutputStream() );
		
		writer.writeStartDocument();
		
		boolean backingOut=false;
		boolean doLoop=true;
		while(doLoop) {
			
			if (!backingOut) {
				writer.writeStartElement(ref.elemName);
				if (ref.value!=null) {
					writer.writeCharacters(ref.value);
					writer.writeEndElement();
				}
			}	
			
			if (!backingOut&&!ref.children.isEmpty()) {
				ref = ref.firstChild;
			} else {
				if (ref.nextSibling!=null) {
					ref = ref.nextSibling;
					backingOut=false;
				} else {
					if (ref.parent!=null ) {
						ref = ref.parent;
						writer.writeEndElement();
						backingOut=true;
					} else
						doLoop=false;
				}
			}
			
		}
						
		writer.writeEndDocument();
		writer.flush();
		writer.close();

	}
  
}
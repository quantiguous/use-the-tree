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

import sun.security.util.Length;

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

  public static Reference createLogicalMsgTreeFromEDIEdifact(InputStream in, int verbosity, boolean strict) throws Exception   {
		
	  
		final Reference inputRoot = new Reference("InputRoot");	
		
		Reference curTree = inputRoot.addChild("EDIFACT");
		BufferedReader reader = null;
		
		
		// Default values (if no UNA segment is given):
		String componentDataElementSeparator = ":";
		String dataElementSeparator = "+";
		String decimalNotation = ".";
		String releaseIndicator = "?";
		String repetitionSeparator = "*";
		String segmentTerminator = "\'";
		
		String[] dataElements = null;
		String[] componentElements = null;
		
		reader = new BufferedReader(new InputStreamReader(in));
     
		String line = reader.readLine();
		while(line != null){
			
			if (line.startsWith("UNA")) {
				
				if (verbosity>10)
					curTree = curTree.addChild("UNA_Service_String_Advice");
				else
					curTree = curTree.addChild("UNA");
				
				if (line.length()>3) {
					componentDataElementSeparator = Character.toString(line.charAt(3));
					if (verbosity>5)
						curTree.addChild("COMPONENT_DATA_ELEMENT_SEPARATOR").value = componentDataElementSeparator;
					else
						curTree.addChild("CDES").value = componentDataElementSeparator;
				} else if (strict)
					throw new Exception("COMPONENT DATA ELEMENT SEPARATOR is mandatory but missing");
				
				if (line.length()>4) {
					dataElementSeparator = Character.toString(line.charAt(4));
					if (verbosity>5)
						curTree.addChild("DATA_ELEMENT_SEPARATOR").value = dataElementSeparator;
					else
						curTree.addChild("DES").value = dataElementSeparator;
				} else if (strict)
					throw new Exception("DATA ELEMENT SEPARATOR is mandatory but missing");
				
				if (line.length()>5) {
					decimalNotation = Character.toString(line.charAt(5));
					if (verbosity>5)
						curTree.addChild("DECIMAL_NOTATION").value = decimalNotation;
					else
						curTree.addChild("DN").value = decimalNotation;
				} else if (strict)
					throw new Exception("DECIMAL NOTATION is mandatory but missing");
				
				if (line.length()>6) {
					releaseIndicator = Character.toString(line.charAt(6));
					if (verbosity>5)
						curTree.addChild("RELEASE_INDICATOR").value = releaseIndicator;
					else
						curTree.addChild("RI").value = releaseIndicator;
				} else if (strict)
					throw new Exception("RELEASE INDICATOR is mandatory but missing");
				
				if (line.length()>7) {
					repetitionSeparator = Character.toString(line.charAt(7));
					if (verbosity>5)
						curTree.addChild("REPETITION_SEPARATOR").value = repetitionSeparator;
					else
						curTree.addChild("RS").value = repetitionSeparator;
				} else if (strict)
					throw new Exception("REPETITION SEPARATOR is mandatory but missing");
				
				if (line.length()>8) {
					segmentTerminator = Character.toString(line.charAt(8));
					if (verbosity>5)
						curTree.addChild("SEGMENT_TERMINATOR").value = segmentTerminator;
					else
						curTree.addChild("ST").value = segmentTerminator;
				} else if (strict)
					throw new Exception("SEGMENT TERMINATOR is mandatory but missing");
					
				curTree = curTree.parent;
				
			} else {
				
				if (line.endsWith(segmentTerminator))
					line = line.substring(0, line.length()-1);
				else
					throw new Exception("Edifact segments need to end with the Segment Separator and a new line");
				
				
				if (line.startsWith("UNB")) {
					
					if (verbosity>10)
						curTree = curTree.addChild("UNB_Interchange_Header");
					else
						curTree = curTree.addChild("UNB");
					
					dataElements = line.split(Pattern.quote(dataElementSeparator));
					
					if (dataElements.length>1&&!dataElements[1].replace(componentDataElementSeparator, "").equals("")) {
						if (verbosity>5)
							curTree.addChild("SYNTAX_IDENTIFIER");
						else
							curTree.addChild("SI");
						
						componentElements = dataElements[1].split(Pattern.quote(componentDataElementSeparator));
						
						if (componentElements.length>0&&!componentElements[0].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("SyntaxIdentifier").value = componentElements[0];
							else
								curTree.lastChild.addChild("si").value = componentElements[0];
						else if (strict)
							throw new Exception("Syntax Identifier is mandatory but missing");
						
						if (componentElements.length>1&&!componentElements[1].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("SyntaxVersionNumber").value = componentElements[1];
							else
								curTree.lastChild.addChild("svn").value = componentElements[1];
						else if (strict)
							throw new Exception("Syntax Version Number is mandatory but missing");
						
					} else if (strict)
						throw new Exception("SYNTAX IDENTIFIER is mandatory but missing");
					
					if (dataElements.length>2&&!dataElements[2].replace(componentDataElementSeparator, "").equals("")) {
						if (verbosity>5)
							curTree.addChild("INTERCHANGE_SENDER");
						else
							curTree.addChild("IS");
						
						componentElements = dataElements[2].split(Pattern.quote(componentDataElementSeparator));
						
						if (componentElements.length>0&&!componentElements[0].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("SenderIdentification").value = componentElements[0];
							else
								curTree.lastChild.addChild("si").value = componentElements[0];
						else if (strict)
							throw new Exception("Sender Identification is mandatory but missing");
						
						if (componentElements.length>1&&!componentElements[1].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("PartnerIdentificationCodeQualifier").value = componentElements[1];
							else
								curTree.lastChild.addChild("picq").value = componentElements[1];
					
						if (componentElements.length>2&&!componentElements[2].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("AddressForReverseRouting").value = componentElements[2];
							else
								curTree.lastChild.addChild("afrr").value = componentElements[2];				
					
					} else if (strict)
						throw new Exception("INTERCHANGE SENDER is mandatory but missing");
					
					if (dataElements.length>3&&!dataElements[3].replace(componentDataElementSeparator, "").equals("")) {
						if (verbosity>5)
							curTree.addChild("INTERCHANGE_RECIPIENT");
						else
							curTree.addChild("IR");
						
						componentElements = dataElements[3].split(Pattern.quote(componentDataElementSeparator));
						
						if (componentElements.length>0&&!componentElements[0].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("RecipientIdentification").value = componentElements[0];
							else
								curTree.lastChild.addChild("ri").value = componentElements[0];
						else if (strict)
							throw new Exception("Recipient Identification is mandatory but missing");
						
						if (componentElements.length>1&&!componentElements[1].equals(""))
							if (verbosity>0)
								curTree.addChild("PartnerIdentificationCodeQualifier").value = componentElements[1];
							else
								curTree.addChild("picq").value = componentElements[1];
						
						if (componentElements.length>2&&!componentElements[2].equals(""))
							if (verbosity>0)
								curTree.addChild("RoutingAddress").value = componentElements[2];
							else
								curTree.addChild("ra").value = componentElements[2];
						
					} else if (strict)
						throw new Exception("INTERCHANGE RECIPIENT is mandatory but missing");
						
					if (dataElements.length>4&&!dataElements[4].replace(componentDataElementSeparator, "").equals("")) {
						if (verbosity>5)
							curTree.addChild("DATE_TIME_OF_PREPARATION");
						else
							curTree.addChild("DTOP");
						
						componentElements = dataElements[4].split(Pattern.quote(componentDataElementSeparator));
						
						if (componentElements.length>0&&!componentElements[0].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("Date").value = componentElements[0];
							else
								curTree.lastChild.addChild("date").value = componentElements[0];
						else if (strict)
							throw new Exception("Date is mandatory but missing");
					
						if (componentElements.length>1&&!componentElements[1].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("Time").value = componentElements[1];
							else
								curTree.lastChild.addChild("time").value = componentElements[1];
						else if (strict)
							throw new Exception("Time is mandatory but missing");
					
					} else if (strict)
						throw new Exception("DATE TIME OF PREPARATION is mandatory but missing");
					
					if (dataElements.length>5&&!dataElements[5].replace(componentDataElementSeparator, "").equals("")) {
					
						if (verbosity>5)
							curTree.addChild("INTERCHANGE_CONTROL_REFERENCE").value = dataElements[5];
						else
							curTree.addChild("ICR").value = dataElements[5];
						
					} else if (strict)
						throw new Exception("INTERCHANGE CONTROL REFERENCE is mandatory but missing");
					
					if (dataElements.length>6&&!dataElements[6].replace(componentDataElementSeparator, "").equals("")) {
						if (verbosity>5)
							curTree.addChild("RECIPIENTS_REFERENCE_PASSWORD");
						else
							curTree.addChild("RRP");
						
						componentElements = dataElements[6].split(Pattern.quote(componentDataElementSeparator));
						
						if (componentElements.length>0&&!componentElements[0].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("RecipientsReferencePassword").value = componentElements[0];
							else
								curTree.lastChild.addChild("rrp").value = componentElements[0];
						else if (strict)
							throw new Exception("Recipient's Reference Password is mandatory but missing");
						
						if (componentElements.length>1&&!componentElements[1].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("RecipientsReferencePasswordQualifier").value = componentElements[1];
							else
								curTree.lastChild.addChild("rrpq").value = componentElements[1];
					
					}
					
					if (dataElements.length>7&&!dataElements[7].replace(componentDataElementSeparator, "").equals("")) {
						
						if (verbosity>5)
							curTree.addChild("APPLICATION_REFERENCE").value = dataElements[7];
						else
							curTree.addChild("AR").value = dataElements[7];
						
					}
					
					if (dataElements.length>8&&!dataElements[8].replace(componentDataElementSeparator, "").equals("")) {
						
						if (verbosity>5)
							curTree.addChild("PROCESSING_PRIORITY_CODE").value = dataElements[8];
						else
							curTree.addChild("PPC").value = dataElements[8];
						
					}
					
					if (dataElements.length>9&&!dataElements[9].replace(componentDataElementSeparator, "").equals("")) {
						
						if (verbosity>5)
							curTree.addChild("ACKNOWLEDGEMENT_REQUEST").value = dataElements[9];
						else
							curTree.addChild("AR").value = dataElements[9];
						
					}
					
					if (dataElements.length>10&&!dataElements[10].replace(componentDataElementSeparator, "").equals("")) {
						
						if (verbosity>5)
							curTree.addChild("COMMUNICATIONS_AGREEMENT_ID").value = dataElements[10];
						else
							curTree.addChild("CAI").value = dataElements[10];
						
					}
					
					if (dataElements.length>11&&!dataElements[11].replace(componentDataElementSeparator, "").equals("")) {
						
						if (verbosity>5)
							curTree.addChild("TEST_INDICATOR").value = dataElements[11];
						else
							curTree.addChild("TI").value = dataElements[11];
						
					}
				
					curTree = curTree.parent;
					
				} else if (line.startsWith("UNZ")) {	
					
					if (verbosity>10)
						curTree = curTree.addChild("UNZ_Interchange_Trailer");
					else
						curTree = curTree.addChild("UNZ");
					
					dataElements = line.split(Pattern.quote(dataElementSeparator));
					
					if (dataElements.length>1&&!dataElements[1].replace(componentDataElementSeparator, "").equals(""))
						if (verbosity>5)
							curTree.addChild("INTERCHANGE_CONTROL_COUNT").value = dataElements[1];
						else
							curTree.addChild("ICC").value = dataElements[1];
					else if (strict)
						throw new Exception("INTERCHANGE CONTROL COUNT is mandatory but missing");
						
					if (dataElements.length>2&&!dataElements[2].replace(componentDataElementSeparator, "").equals(""))
						if (verbosity>5)
							curTree.addChild("INTERCHANGE_CONTROL_REFERENCE").value = dataElements[2];
						else
							curTree.addChild("ICR").value = dataElements[2];
					else if (strict)
						throw new Exception("INTERCHANGE CONTROL REFERENCE is mandatory but missing");
					
					curTree = curTree.parent;
					
				} else if (line.startsWith("UNG")) {
					
					if (verbosity>10)
						curTree = curTree.addChild("UNG_Functional_Group_Header");
					else
						curTree = curTree.addChild("UNG");
					
					dataElements = line.split(Pattern.quote(dataElementSeparator));
					
					if (dataElements.length>1&&!dataElements[1].equals(""))
						if (verbosity>5)
							curTree.addChild("FUNCTIONAL_GROUP_IDENTIFICATION").value = dataElements[1];
						else
							curTree.addChild("FGI").value = dataElements[1];
					else if (strict)
						throw new Exception("FUNCTIONAL GROUP IDENTIFICATION is mandatory but missing");
									
					if (dataElements.length>2&&!dataElements[2].replace(componentDataElementSeparator, "").equals("")) {
						if (verbosity>5)
							curTree.addChild("APPLICATION_SENDERS_IDENTIFICATION");
						else
							curTree.addChild("ASI");
						
						componentElements = dataElements[2].split(Pattern.quote(componentDataElementSeparator));
						
						if (componentElements.length>0&&!componentElements[0].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("ApplicationSendersIdentification").value = componentElements[0];
							else
								curTree.lastChild.addChild("asi").value = componentElements[0];
						else if (strict)
							throw new Exception("Application Sender's Identification is mandatory but missing");
						
						if (componentElements.length>1&&!componentElements[1].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("PartnerIdentificationCodeQualifier").value = componentElements[1];
							else
								curTree.lastChild.addChild("picq").value = componentElements[1];
					
					} else if (strict)
						throw new Exception("APPLICATION SENDERS IDENTIFICATION is mandatory but missing");
					
					if (dataElements.length>3&&!dataElements[3].replace(componentDataElementSeparator, "").equals("")) {
						if (verbosity>5)
							curTree.addChild("APPLICATION_RECIPIENTS_IDENTIFICATION");
						else
							curTree.addChild("ARI");
						
						componentElements = dataElements[3].split(Pattern.quote(componentDataElementSeparator));
						
						if (componentElements.length>0&&!componentElements[0].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("RecipientsIdentification").value = componentElements[0];
							else
								curTree.lastChild.addChild("ri").value = componentElements[0];
						else if (strict)
							throw new Exception("Recipient's Identification is mandatory but missing");
						
						if (componentElements.length>1&&!componentElements[1].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("RecipientsIdentificationQualifier").value = componentElements[1];
							else
								curTree.lastChild.addChild("riq").value = componentElements[1];
					
					} else if (strict)
						throw new Exception("APPLICATION RECIPIENTS IDENTIFICATION is mandatory but missing");
					
					if (dataElements.length>4&&!dataElements[4].replace(componentDataElementSeparator, "").equals("")) {
						if (verbosity>5)
							curTree.addChild("DATE_TIME_OF_PREPARATION");
						else
							curTree.addChild("DTOP");
						
						componentElements = dataElements[4].split(Pattern.quote(componentDataElementSeparator));
						
						if (componentElements.length>0&&!componentElements[0].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("Date").value = componentElements[0];
							else
								curTree.lastChild.addChild("date").value = componentElements[0];
						else if (strict)
							throw new Exception("Date is mandatory but missing");
						
						if (componentElements.length>1&&!componentElements[1].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("Time").value = componentElements[1];
							else
								curTree.lastChild.addChild("time").value = componentElements[1];
						else if (strict)
							throw new Exception("Time is mandatory but missing");
					
					} else if (strict)
						throw new Exception("DATE/TIME OF PREPARATION is mandatory but missing");
						
					if (dataElements.length>5&&!dataElements[5].equals("")) {
						if (verbosity>5)
							curTree.addChild("FUNCTIONAL_GROUP_REFERENCE_NUMBER").value = dataElements[5];
						else
							curTree.addChild("FGRN").value = dataElements[5];
					} else if (strict)
						throw new Exception("FUNCTIONAL GROUP REFERENCE NUMBER is mandatory but missing");
					
					if (dataElements.length>6&&!dataElements[6].equals("")) {
						if (verbosity>5)
							curTree.addChild("CONTROLLING_AGENCY").value = dataElements[6];
						else
							curTree.addChild("CA").value = dataElements[6];
					} else if (strict)
						throw new Exception("CONTROLLING AGENCY is mandatory but missing");
					
					if (dataElements.length>7&&!dataElements[7].replace(componentDataElementSeparator, "").equals("")) {
						if (verbosity>5)
							curTree.addChild("MESSAGE_VERSION");
						else
							curTree.addChild("MV");
						
						componentElements = dataElements[7].split(Pattern.quote(componentDataElementSeparator));
						
						if (componentElements.length>0&&!componentElements[0].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("MessageVersionNumber").value = componentElements[0];
							else
								curTree.lastChild.addChild("mvn").value = componentElements[0];
						else if (strict)
							throw new Exception("Message Version Number is mandatory but missing");
						
						if (componentElements.length>1&&!componentElements[1].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("MessageReleaseNumber").value = componentElements[1];
							else
								curTree.lastChild.addChild("mrn").value = componentElements[1];
						else if (strict)
							throw new Exception("Message Release Number is mandatory but missing");
					
						if (componentElements.length>2&&!componentElements[2].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("AssociationAssignedCode").value = componentElements[2];
							else
								curTree.lastChild.addChild("aac").value = componentElements[2];
						
					} else if (strict)
						throw new Exception("MESSAGE VERSION is mandatory but missing");
					
					if (dataElements.length>8&&!dataElements[8].equals(""))
						if (verbosity>5)
							curTree.addChild("APPLICATION_PASSWORD").value = dataElements[8];
						else
							curTree.addChild("AP").value = dataElements[8];

					curTree = curTree.parent;
					
				} else if (line.startsWith("UNE")) {	
					
					if (verbosity>10)
						curTree = curTree.addChild("UNE_Functional_Group_Trailer");
					else
						curTree = curTree.addChild("UNE");
					
					dataElements = line.split(Pattern.quote(dataElementSeparator));
					
					if (dataElements.length>1&&!dataElements[1].equals(""))
						if (verbosity>5)
							curTree.addChild("NUMBER_OF_MESSAGES").value = dataElements[1];
						else
							curTree.addChild("NOM").value = dataElements[1];
					else if (strict)
						throw new Exception("NUMBER OF MESSAGES is mandatory but missing");
					
					if (dataElements.length>2&&!dataElements[2].equals(""))
						if (verbosity>5)
							curTree.addChild("FUNCTIONAL_GROUP_REFERENCE_NUMBER").value = dataElements[2];
						else
							curTree.addChild("FGRN").value = dataElements[2];
					else if (strict)
						throw new Exception("FUNCTIONAL GROUP REFERENCE NUMBER is mandatory but missing");
					
				} else if (line.startsWith("UNH")) {
					
					if (verbosity>10)
						curTree = curTree.addChild("UNH_Message_Header");
					else
						curTree = curTree.addChild("UNH");
					
					dataElements = line.split(Pattern.quote(dataElementSeparator));
					
					if (dataElements.length>1&&!dataElements[1].equals(""))
						if (verbosity>5)
							curTree.addChild("MESSAGE_REFERENCE_NUMBER").value = dataElements[1];
						else
							curTree.addChild("MRN").value = dataElements[1];
					else if (strict)
						throw new Exception("MESSAGE REFERENCE NUMBER is mandatory but missing");
					
					
					if (dataElements.length>2&&!dataElements[2].replace(componentDataElementSeparator, "").equals("")) {
						if (verbosity>5)
							curTree.addChild("MESSAGE_IDENTIFIER");
						else
							curTree.addChild("MI");
						
						componentElements = dataElements[2].split(Pattern.quote(componentDataElementSeparator));
						
						if (componentElements.length>0&&!componentElements[0].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("MessageType").value = componentElements[0];
							else
								curTree.lastChild.addChild("mt").value = componentElements[0];
						else if (strict)
							throw new Exception("Message Type is mandatory but missing");
						
						if (componentElements.length>1&&!componentElements[1].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("MessageVersionNumber").value = componentElements[1];
							else
								curTree.lastChild.addChild("mvn").value = componentElements[1];
						else if (strict)
							throw new Exception("Message Version Number is mandatory but missing");
					
						if (componentElements.length>2&&!componentElements[2].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("MessageReleaseNumber").value = componentElements[2];
							else
								curTree.lastChild.addChild("mrn").value = componentElements[2];
						else if (strict)
							throw new Exception("Message Release Number is mandatory but missing");
					
						if (componentElements.length>3&&!componentElements[3].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("ControllingAgency").value = componentElements[3];
							else
								curTree.lastChild.addChild("ca").value = componentElements[3];
						else if (strict)
							throw new Exception("Controlling Agency is mandatory but missing");
					
						
						if (componentElements.length>4&&!componentElements[4].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("AssociationAssignedCode").value = componentElements[4];
							else
								curTree.lastChild.addChild("AAC").value = componentElements[4];
						
					} else if (strict)
						throw new Exception("MESSAGE IDENTIFIER is mandatory but missing");
					
					if (dataElements.length>3&&!dataElements[3].equals(""))
						if (verbosity>5)
							curTree.addChild("COMMON_ACCESS_REFERENCE").value = dataElements[3];
						else
							curTree.addChild("CAR").value = dataElements[3];
					
					if (dataElements.length>4&&!dataElements[4].replace(componentDataElementSeparator, "").equals("")) {
						if (verbosity>5)
							curTree.addChild("STATUS_OF_THE_TRANSFER");
						else
							curTree.addChild("SOTT");
						
						componentElements = dataElements[4].split(Pattern.quote(componentDataElementSeparator));
						
						if (componentElements.length>0&&!componentElements[0].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("SequenceOfTransfers").value = componentElements[0];
							else
								curTree.lastChild.addChild("sot").value = componentElements[0];
						else if (strict)
							throw new Exception("Sequence Of Transfers is mandatory but missing");
						
						if (componentElements.length>1&&!componentElements[1].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("FirstAndLastTransfer").value = componentElements[1];
							else
								curTree.lastChild.addChild("falt").value = componentElements[1];
					}
					
					curTree = curTree.parent;
					
				} else if (line.startsWith("UNT")) {	
					
					if (verbosity>10)
						curTree = curTree.addChild("UNT_Message_Trailer");
					else
						curTree = curTree.addChild("UNT");
					
					dataElements = line.split(Pattern.quote(dataElementSeparator));
					
					if (dataElements.length>1&&!dataElements[1].equals(""))
						if (verbosity>5)
							curTree.addChild("NUMBER_OF_SEGMENTS_IN_THE_MESSAGE").value = dataElements[1];
						else
							curTree.addChild("NOSITM").value = dataElements[1];
					else if (strict)
						throw new Exception("NUMBER OF SEGMENTS IN THE MESSAGE is mandatory but missing");
					
					if (dataElements.length>2&&!dataElements[2].equals(""))
						if (verbosity>5)
							curTree.addChild("MESSAGE_REFERENCE_NUMBER").value = dataElements[2];
						else
							curTree.addChild("MRN").value = dataElements[2];
					else if (strict)
						throw new Exception("MESSAGE REFERENCE NUMBER is mandatory but missing");
						
					curTree = curTree.parent;
					
					
				} else if (line.startsWith("TXT")) {	
					
					if (verbosity>10)
						curTree = curTree.addChild("TXT_Text");
					else
						curTree = curTree.addChild("TXT");
					
					dataElements = line.split(Pattern.quote(dataElementSeparator));
					
					if (dataElements.length>1&&!dataElements[1].equals(""))
						if (verbosity>5)
							curTree.addChild("TEXT_REFERENCE_CODE").value = dataElements[1];
						else
							curTree.addChild("TRC").value = dataElements[1];
					
					if (dataElements.length>2&&!dataElements[2].equals(""))
						if (verbosity>5)
							curTree.addChild("FREE_FORM_TEXT").value = dataElements[2];
						else
							curTree.addChild("FFT").value = dataElements[2];
					else if (strict)
						throw new Exception("FREE FORM TEXT is mandatory but missing");
						
					curTree = curTree.parent;
					
					
				} else if (line.startsWith("UNS")) {	
					
					if (verbosity>10)
						curTree = curTree.addChild("UNS_Section_Control");
					else
						curTree = curTree.addChild("UNS");
					
					dataElements = line.split(Pattern.quote(dataElementSeparator));
					
					if (dataElements.length>1&&!dataElements[1].equals(""))
						if (verbosity>5)
							curTree.addChild("SECTION_IDENTIFICATION").value = dataElements[1];
						else
							curTree.addChild("SI").value = dataElements[1];
					else if (strict)
						throw new Exception("SECTION IDENTIFICATION is mandatory but missing");
						
					curTree = curTree.parent;
					
				} else if (line.startsWith("LIN")) {
					
					if (verbosity>10)
						curTree = curTree.addChild("LIN_LINE_ITEM");
					else
						curTree = curTree.addChild("LIN");
					
					dataElements = line.split(Pattern.quote(dataElementSeparator));
					
					if (dataElements.length>1&&!dataElements[1].equals(""))
						if (verbosity>5)
							curTree.addChild("LINE_ITEM_IDENTIFIER").value = dataElements[1];
						else
							curTree.addChild("LII").value = dataElements[1];
					
					if (dataElements.length>2&&!dataElements[2].equals(""))
						if (verbosity>5)
							curTree.addChild("ACTION_REQUEST_NOTIFICATION_DESCRIPTION_CODE").value = dataElements[2];
						else
							curTree.addChild("ARNDC").value = dataElements[2];
						
					if (dataElements.length>3&&!dataElements[3].replace(componentDataElementSeparator, "").equals("")) {
						if (verbosity>5)
							curTree.addChild("ITEM_NUMBER_IDENTIFICATION");
						else
							curTree.addChild("INI");
					
						componentElements = dataElements[3].split(Pattern.quote(componentDataElementSeparator));
						
						if (componentElements.length>0&&!componentElements[0].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("ItemIdentifier").value = componentElements[0];
							else 
								curTree.lastChild.addChild("ii").value = componentElements[0];
						
						if (componentElements.length>1&&!componentElements[1].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("ItemTypeIdentificationCode").value = componentElements[1];
							else
								curTree.lastChild.addChild("itic").value = componentElements[1];
						
						if (componentElements.length>2&&!componentElements[2].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("CodeListIdentificationCode").value = componentElements[2];
							else
								curTree.lastChild.addChild("clic").value = componentElements[2];
						
						if (componentElements.length>3&&!componentElements[3].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("CodeListResponsibleAgencyCode").value = componentElements[3];
							else
								curTree.lastChild.addChild("clrac").value = componentElements[3];
					}
					
					if (dataElements.length>4&&!dataElements[4].replace(componentDataElementSeparator, "").equals("")) {
						if (verbosity>5)
							curTree.addChild("SUB_LINE_INFORMATION");
						else
							curTree.addChild("SLI");
						
						componentElements = dataElements[4].split(Pattern.quote(componentDataElementSeparator));
						
						if (componentElements.length>0&&!componentElements[0].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("SubLineIndicatorCode").value = componentElements[0];
							else
								curTree.lastChild.addChild("slic").value = componentElements[0];
						
						if (componentElements.length>1&&!componentElements[1].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("LineItemIdentifier").value = componentElements[1];
							else
								curTree.lastChild.addChild("lii").value = componentElements[1];
					}
					
					if (dataElements.length>5&&!dataElements[5].equals(""))
						if (verbosity>5)
							curTree.addChild("CONFIGURATION_LEVEL_NUMBER").value = dataElements[5];
						else
							curTree.addChild("CLN").value = dataElements[5];
					
					if (dataElements.length>6&&!dataElements[6].equals(""))
						if (verbosity>5)
							curTree.addChild("CONFIGURATION_OPERATION_CODE").value = dataElements[6];
						else
							curTree.addChild("COC").value = dataElements[6];
					
					curTree = curTree.parent;
					
					
				} else if (line.startsWith("QTY")) {

					if (verbosity>10)
						curTree = curTree.addChild("QTY_QUANTITY");
					else
						curTree = curTree.addChild("QTY");
					
					dataElements = line.split(Pattern.quote(dataElementSeparator));
					
					if (dataElements.length>1&&!dataElements[1].equals(""))
						if (verbosity>5)
							curTree.addChild("QUANTITY_DETAILS");
						else
							curTree.addChild("QD");
					
					componentElements = dataElements[1].split(Pattern.quote(componentDataElementSeparator));
					
					if (componentElements.length>0&&!componentElements[0].equals(""))
						if (verbosity>0)
							curTree.lastChild.addChild("QuantityTypeCodeQualifier").value = componentElements[0];
						else
							curTree.lastChild.addChild("qtcq").value = componentElements[0];
					else if (strict)
						throw new Exception("Quantity Type Code Qualifier is mandatory but missing");
					
					if (componentElements.length>1&&!componentElements[1].equals(""))
						if (verbosity>0)
							curTree.lastChild.addChild("Quantity").value = componentElements[1];
						else
							curTree.lastChild.addChild("qty").value = componentElements[1];
					else if (strict)
						throw new Exception("Quantity is mandatory but missing");
					
					if (componentElements.length>2&&!componentElements[2].equals(""))
						if (verbosity>0)
							curTree.lastChild.addChild("MeasurementUnitCode").value = componentElements[2];
						else
							curTree.lastChild.addChild("muc").value = componentElements[2];
						
					curTree = curTree.parent;
					
				} else if (line.startsWith("DTM")) {
					
					if (verbosity>10)
						curTree = curTree.addChild("DTM_DATE_TIME_PERIOD");
					else
						curTree = curTree.addChild("DTM");
					
					dataElements = line.split(Pattern.quote(dataElementSeparator));
					
					if (dataElements.length>1&&!dataElements[1].equals(""))
						if (verbosity>5)
							curTree.addChild("DATE_TIME_PERIOD");
						else
							curTree.addChild("DTP");
					else if (strict)
						throw new Exception("DATE/TIME/PERIOD is mandatory but missing");
					
					componentElements = dataElements[1].split(Pattern.quote(componentDataElementSeparator));
					
					if (componentElements.length>0&&!componentElements[0].equals(""))
						if (verbosity>0)
							curTree.lastChild.addChild("FunctionCodeQualifier").value = componentElements[0];
						else
							curTree.lastChild.addChild("fcq").value = componentElements[0];
					else if (strict)
						throw new Exception("Date or Time or Period Function Code Qualifier is mandatory but missing");
					
					if (componentElements.length>1&&!componentElements[1].equals(""))
						if (verbosity>0)
							curTree.lastChild.addChild("Value").value = componentElements[1];
						else
							curTree.lastChild.addChild("val").value = componentElements[1];

					if (componentElements.length>2&&!componentElements[2].equals(""))
						if (verbosity>0)
							curTree.lastChild.addChild("FormatCode").value = componentElements[2];
						else
							curTree.lastChild.addChild("fc").value = componentElements[2];
					
					curTree = curTree.parent;
				
				} else if (line.startsWith("BGM")) {
					
					if (verbosity>10)
						curTree = curTree.addChild("BGM_BEGINNING_OF_MESSAGE");
					else
						curTree = curTree.addChild("BGM");
					
					dataElements = line.split(Pattern.quote(dataElementSeparator));
					
					if (dataElements.length>1&&!dataElements[1].replace(componentDataElementSeparator, "").equals("")) {

						if (verbosity>5)
							curTree.addChild("DOCUMENT_MESSAGE_NAME");
						else
							curTree.addChild("DMN");
						
						componentElements = dataElements[1].split(Pattern.quote(componentDataElementSeparator));
						
						if (componentElements.length>0&&!componentElements[0].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("DocumentNameCode").value = componentElements[0];
							else
								curTree.lastChild.addChild("dnc").value = componentElements[0];
						
						if (componentElements.length>1&&!componentElements[1].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("CodeListIdentificationCode").value = componentElements[1];
							else
								curTree.lastChild.addChild("clic").value = componentElements[1];
						
						if (componentElements.length>2&&!componentElements[2].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("CodeListResponsibleAgencyCode").value = componentElements[2];
							else
								curTree.lastChild.addChild("clrac").value = componentElements[2];
						
						if (componentElements.length>3&&!componentElements[3].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("DocumentName").value = componentElements[3];
							else
								curTree.lastChild.addChild("dn").value = componentElements[3];
					}
					
					if (dataElements.length>2&&!dataElements[2].replace(componentDataElementSeparator, "").equals("")) {
						if (verbosity>5)
							curTree.addChild("DOCUMENT_MESSAGE_IDENTIFICATION");
						else
							curTree.addChild("DMI");
						
						componentElements = dataElements[2].split(Pattern.quote(componentDataElementSeparator));
						
						if (componentElements.length>0&&!componentElements[0].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("DocumentIdentifier").value = componentElements[0];	
							else
								curTree.lastChild.addChild("di").value = componentElements[0];	
							
						if (componentElements.length>1&&!componentElements[1].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("VersionIdentifier").value = componentElements[1];
							else
								curTree.lastChild.addChild("vi").value = componentElements[1];
						
						if (componentElements.length>2&&!componentElements[2].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("RevisionIdentifier").value = componentElements[2];
							else
								curTree.lastChild.addChild("ri").value = componentElements[2];
					}	
					
					if (dataElements.length>3&&!dataElements[3].equals(""))
						if (verbosity>5)
							curTree.addChild("MESSAGE_FUNCTION_CODE").value = dataElements[3];
						else
							curTree.addChild("MFC").value = dataElements[3];
					
					if (dataElements.length>4&&!dataElements[4].equals(""))
						if (verbosity>5)
							curTree.addChild("RESPONSE_TYPE_CODE").value = dataElements[4];
						else
							curTree.addChild("RTC").value = dataElements[4];

					curTree = curTree.parent;
					
				} else if (line.startsWith("NAD")) {
					
					if (verbosity>10)
						curTree = curTree.addChild("NAD_NAME_AND_ADDRESS");
					else
						curTree = curTree.addChild("NAD");
					
					dataElements = line.split(Pattern.quote(dataElementSeparator));
					
					if (dataElements.length>1&&!dataElements[1].equals(""))
						if (verbosity>5)
							curTree.addChild("PARTY_FUNCTION_CODE_QUALIFIER").value = dataElements[1];
						else
							curTree.addChild("PFCQ").value = dataElements[1];
					else if (strict)
						throw new Exception("PARTY FUNCTION CODE QUALIFIER is mandatory but missing");
					
					if (dataElements.length>2&&!dataElements[2].replace(componentDataElementSeparator, "").equals("")) {
						if (verbosity>5)
							curTree.addChild("PARTY_IDENTIFICATION_DETAILS");
						else
							curTree.addChild("PID");
						
						componentElements = dataElements[2].split(Pattern.quote(componentDataElementSeparator));
						
						if (componentElements.length>0&&!componentElements[0].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("PartyIdentifier").value = componentElements[0];	
							else
								curTree.lastChild.addChild("pi").value = componentElements[0];	
						else if (strict)
							throw new Exception("Party Identifier is mandatory but missing");
						
						if (componentElements.length>1&&!componentElements[1].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("CodeListIdentificationCode").value = componentElements[0];	
							else
								curTree.lastChild.addChild("clic").value = componentElements[0];	
						
						if (componentElements.length>2&&!componentElements[2].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("CodeListResponsibleAgencyCode").value = componentElements[0];	
							else
								curTree.lastChild.addChild("clrac").value = componentElements[0];	
						
					}
					
					if (dataElements.length>3&&!dataElements[3].replace(componentDataElementSeparator, "").equals("")) {
						if (verbosity>5)
							curTree.addChild("NAME_AND_ADDRESS");
						else
							curTree.addChild("NAA");
						
						componentElements = dataElements[3].split(Pattern.quote(componentDataElementSeparator));
						
						if (componentElements.length>0&&!componentElements[0].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("NameAndAddressDescription").value = componentElements[0];	
							else
								curTree.lastChild.addChild("naad").value = componentElements[0];	
						else if (strict)
							throw new Exception("Name And Address Description is mandatory but missing");
						
						if (componentElements.length>1&&!componentElements[1].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("NameAndAddressDescription").value = componentElements[1];	
							else
								curTree.lastChild.addChild("naad").value = componentElements[1];	
						
						if (componentElements.length>2&&!componentElements[2].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("NameAndAddressDescription").value = componentElements[2];	
							else
								curTree.lastChild.addChild("naad").value = componentElements[2];	
						
						if (componentElements.length>3&&!componentElements[3].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("NameAndAddressDescription").value = componentElements[3];	
							else
								curTree.lastChild.addChild("naad").value = componentElements[3];	
						
						if (componentElements.length>4&&!componentElements[4].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("NameAndAddressDescription").value = componentElements[4];	
							else
								curTree.lastChild.addChild("naad").value = componentElements[4];	
						
					}
					
					if (dataElements.length>4&&!dataElements[4].replace(componentDataElementSeparator, "").equals("")) {
						if (verbosity>5)
							curTree.addChild("PARTY_NAME");
						else
							curTree.addChild("PN");
						
						componentElements = dataElements[4].split(Pattern.quote(componentDataElementSeparator));
						
						if (componentElements.length>0&&!componentElements[0].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("PartyName").value = componentElements[0];	
							else
								curTree.lastChild.addChild("pn").value = componentElements[0];
						else if (strict)
							throw new Exception("Party Name is mandatory but missing");
						
						if (componentElements.length>1&&!componentElements[1].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("PartyName").value = componentElements[1];	
							else
								curTree.lastChild.addChild("pn").value = componentElements[1];
						
						if (componentElements.length>2&&!componentElements[2].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("PartyName").value = componentElements[2];	
							else
								curTree.lastChild.addChild("pn").value = componentElements[2];
						
						if (componentElements.length>3&&!componentElements[3].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("PartyName").value = componentElements[3];	
							else
								curTree.lastChild.addChild("pn").value = componentElements[3];
						
						if (componentElements.length>4&&!componentElements[4].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("PartyName").value = componentElements[4];	
							else
								curTree.lastChild.addChild("pn").value = componentElements[4];
					
						if (componentElements.length>5&&!componentElements[5].equals(""))
							if (verbosity>0)
								curTree.addChild("PartyNameFormatCode ").value = componentElements[5];
							else
								curTree.addChild("pnfc ").value = componentElements[5];
					}
					
					
					if (dataElements.length>5&&!dataElements[5].replace(componentDataElementSeparator, "").equals("")) {
						if (verbosity>5)
							curTree.addChild("STREET");
						else
							curTree.addChild("STREET");
						
						componentElements = dataElements[5].split(Pattern.quote(componentDataElementSeparator));
						
						if (componentElements.length>0&&!componentElements[0].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("StreetAndNumber").value = dataElements[0];	
							else
								curTree.lastChild.addChild("san").value = dataElements[0];
						else if (strict)
							throw new Exception("Street and Number or Post Office Box Identifier is mandatory but missing");
						
						if (componentElements.length>1&&!componentElements[1].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("StreetAndNumber").value = dataElements[1];	
							else
								curTree.lastChild.addChild("san").value = dataElements[1];
					
						if (componentElements.length>2&&!componentElements[2].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("StreetAndNumber").value = dataElements[2];	
							else
								curTree.lastChild.addChild("san").value = dataElements[2];					
					
						if (componentElements.length>3&&!componentElements[3].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("StreetAndNumber").value = dataElements[3];	
							else
								curTree.lastChild.addChild("san").value = dataElements[3];	
					}
						
					if (dataElements.length>6&&!dataElements[6].equals(""))
						if (verbosity>5)
							curTree.addChild("CITY_NAME").value = dataElements[6];
						else
							curTree.addChild("CITY").value = dataElements[6];	
						
					if (dataElements.length>7&&!dataElements[7].replace(componentDataElementSeparator, "").equals("")) {
						if (verbosity>5)
							curTree.addChild("COUNTRY_SUB_ENTITY_DETAILS");
						else
							curTree.addChild("CSED");
						
						componentElements = dataElements[7].split(Pattern.quote(componentDataElementSeparator));
						
						if (componentElements.length>0&&!componentElements[0].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("CountrySubEntityNameCode").value = dataElements[0];	
							else
								curTree.lastChild.addChild("csenc").value = dataElements[0];
					
						if (componentElements.length>1&&!componentElements[1].equals(""))
							if (verbosity>0)
								curTree.addChild("CodeListIdentificationCode").value = componentElements[1];
							else
								curTree.addChild("clic").value = componentElements[1];
						if (componentElements.length>2&&!componentElements[2].equals(""))
							if (verbosity>0)
								curTree.addChild("CodeListResponsibleAgencyCode").value = componentElements[2];	
							else
								curTree.addChild("clrac").value = componentElements[2];	
						if (componentElements.length>3&&!componentElements[3].equals(""))
							if (verbosity>0)
								curTree.addChild("CountrySubEntityName ").value = componentElements[3];
							else
								curTree.addChild("csen ").value = componentElements[3];
					}
					
					if (dataElements.length>8&&!dataElements[8].equals("")){
						if (verbosity>5)
							curTree.addChild("POSTAL_IDENTIFICATION_CODE").value = dataElements[8];
						else
							curTree.addChild("PIC").value = dataElements[8];
							
					}
					
					if (dataElements.length>9&&!dataElements[9].equals("")){
						if (verbosity>5)
							curTree.addChild("COUNTRY_NAME_CODE").value = dataElements[9];
						else
							curTree.addChild("CNC").value = dataElements[9];
							
					}
					
					curTree = curTree.parent;
					
				} else if (line.startsWith("FTX")) {

					
					if (verbosity>10)
						curTree = curTree.addChild("FTX_FREE_TEXT");
					else
						curTree = curTree.addChild("FTX");
					
					dataElements = line.split(Pattern.quote(dataElementSeparator));
					
					if (dataElements.length>1&&!dataElements[1].equals(""))
						if (verbosity>5)
							curTree.addChild("TEXT_SUBJECT_CODE_QUALIFIER").value = dataElements[1];	
						else
							curTree.addChild("TSCQ").value = dataElements[1];
					else if (strict)
						throw new Exception("TEXT SUBJECT CODE QUALIFIER is mandatory but missing");
					
					if (dataElements.length>2&&!dataElements[2].equals(""))
						if (verbosity>5)
							curTree.addChild("FREE_TEXT_FUNCTION_CODE").value = dataElements[2];	
						else
							curTree.addChild("FTFC").value = dataElements[2];
					else if (strict)
						throw new Exception("FREE TEXT FUNCTION CODE is mandatory but missing");
					
					if (dataElements.length>3&&!dataElements[3].replace(componentDataElementSeparator, "").equals("")) {
						if (verbosity>5)
							curTree.addChild("TEXT_REFERENCE");
						else
							curTree.addChild("TR");
						
						componentElements = dataElements[3].split(Pattern.quote(componentDataElementSeparator));
						
						if (componentElements.length>0&&!componentElements[0].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("FreeTextValueCode").value = componentElements[0];	
							else
								curTree.lastChild.addChild("ftvc").value = componentElements[0];
						else if (strict)
							throw new Exception("Free Text Value Code is mandatory but missing");
					
						if (componentElements.length>1&&!componentElements[1].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("CodeListIdentificationCode").value = componentElements[1];	
							else
								curTree.lastChild.addChild("clic").value = componentElements[1];
					
						if (componentElements.length>2&&!componentElements[2].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("CodeListResponsibleAgencyCode").value = componentElements[2];	
							else
								curTree.lastChild.addChild("clrac").value = componentElements[2];
					
					}
					
					if (dataElements.length>4&&!dataElements[4].replace(componentDataElementSeparator, "").equals("")) {
						if (verbosity>5)
							curTree.addChild("TEXT_LITERAL");
						else
							curTree.addChild("TL");
						
						componentElements = dataElements[4].split(Pattern.quote(componentDataElementSeparator));
						
						if (componentElements.length>0&&!componentElements[0].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("FreeTextValue").value = componentElements[0];	
							else
								curTree.lastChild.addChild("ftv").value = componentElements[0];
						else if (strict)
							throw new Exception("Free Text Value is mandatory but missing");
					
						if (componentElements.length>1&&!componentElements[1].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("FreeTextValue").value = componentElements[1];	
							else
								curTree.lastChild.addChild("ftv").value = componentElements[1];
						
						if (componentElements.length>2&&!componentElements[2].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("FreeTextValue").value = componentElements[2];	
							else
								curTree.lastChild.addChild("ftv").value = componentElements[2];
						
						if (componentElements.length>3&&!componentElements[3].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("FreeTextValue").value = componentElements[3];	
							else
								curTree.lastChild.addChild("ftv").value = componentElements[3];
						
						if (componentElements.length>4&&!componentElements[4].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("FreeTextValue").value = componentElements[4];	
							else
								curTree.lastChild.addChild("ftv").value = componentElements[4];
					}
					

					if (dataElements.length>5&&!dataElements[5].equals(""))
						if (verbosity>5)
							curTree.addChild("LANGUAGE_NAME_CODE").value = dataElements[5];	
						else
							curTree.addChild("LNC").value = dataElements[5];
					
					if (dataElements.length>6&&!dataElements[6].equals(""))
						if (verbosity>5)
							curTree.addChild("FREE_TEXT_FORMAT_CODE").value = dataElements[6];	
						else
							curTree.addChild("FTFC").value = dataElements[6];
					
					curTree = curTree.parent;
					
				} else if (line.startsWith("UNS")) {	
					
					if (verbosity>10)
						curTree = curTree.addChild("UNS_SECTION_CONTROL");
					else
						curTree = curTree.addChild("UNS");	
					
					dataElements = line.split(Pattern.quote(dataElementSeparator));
					
					if (dataElements.length>1&&!dataElements[1].equals(""))
						if (verbosity>5)
							curTree.addChild("SECTION_IDENTIFICATION").value = dataElements[1];	
						else
							curTree.addChild("SI").value = dataElements[1];
					else if (strict)
						throw new Exception("SECTION IDENTIFICATION is mandatory but missing");
						
					curTree = curTree.parent;
					
				} else if (line.startsWith("CNT")) {
					
					if (verbosity>10)
						curTree = curTree.addChild("CNT_CONTROL_TOTAL");
					else
						curTree = curTree.addChild("CNT");	
					
					dataElements = line.split(Pattern.quote(dataElementSeparator));
					
					if (dataElements.length>1&&!dataElements[1].replace(componentDataElementSeparator, "").equals("")) {
						if (verbosity>5)
							curTree.addChild("CONTROL");
						else
							curTree.addChild("C");
						
						componentElements = dataElements[1].split(Pattern.quote(componentDataElementSeparator));
						
						if (componentElements.length>0&&!componentElements[0].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("ControlTotalTypeCodeQualifier").value = componentElements[0];	
							else
								curTree.lastChild.addChild("cttcq").value = componentElements[0];
						else if (strict)
							throw new Exception("Control Total Type Code Qualifier is mandatory but missing");
					
						if (componentElements.length>1&&!componentElements[1].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("ControlTotalValue").value = componentElements[1];	
							else
								curTree.lastChild.addChild("ctv").value = componentElements[1];
						else if (strict)
							throw new Exception("Control Total Value is mandatory but missing");
					
						if (componentElements.length>2&&!componentElements[2].equals(""))
							if (verbosity>0)
								curTree.lastChild.addChild("MeasurementUnitCode").value = componentElements[2];	
							else
								curTree.lastChild.addChild("muc").value = componentElements[2];	
					}
					
					curTree = curTree.parent;
				
				} else if (strict)			
					throw new Exception("Unknown Edifact segment \"" + line.substring(0,2) + "\" in line \"" + line + "\""  );
					
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
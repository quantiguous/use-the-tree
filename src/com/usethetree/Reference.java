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

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.servlet.http.HttpServletResponse;


public class Reference {

  // I don't care about getters and setters ;-)... I am really sorry!
  public String elemName;
  public String value;

  public LinkedList<Reference> children = new LinkedList<Reference>();

  public Reference parent = null;
  public Reference nextSibling = null;
  public Reference prevSibling = null;
  
  public Reference prevGroupingSibling = null;		//TODO
  

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
  
  public Reference(Reference parent, String elemName) {
	  this.parent = parent;
	  this.elemName = elemName;
	 }
  
  
  public Reference addChild(String elemName, String value) {
	  Reference child = new Reference(this, elemName, value);
	  if (!this.children.isEmpty()) {
		  child.prevSibling=this.children.getLast();
		  this.children.getLast().nextSibling=child;
	  }
	  this.children.add(child);
      return child;
  }

  public Reference addChild(String elemName) {
	  Reference child = new Reference(this, elemName);
	  if (!this.children.isEmpty()) {
		  child.prevSibling=this.children.getLast();
		  this.children.getLast().nextSibling=child;
	  }
	  this.children.add(child);
      return child;
  }
  
  public Reference firstChild(String elemName) {
	  
	  for (Reference child : this.children) {
		  if (child.elemName.equals(elemName))
		  return child;
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
  
  public Reference set(String elemName, String value) {
	  
	  Reference tmp = this.firstChild(elemName);
	  if (tmp==null)
			return this.addChild(elemName, value);
	  else
		  return tmp;
  }
  
  
  public Reference add(String elemName, String value) {
	  
	  Reference tmp = this.firstChild(elemName);
	  if (tmp==null)
			return this.addChild(elemName, value);
	  else {
		   tmp.value = "" + (Integer.parseInt(tmp.value) + Integer.parseInt(value));
		   return tmp;
	  }	   
  }
  
  
  
  public Reference firstChild(String elemName, String key, String value) {
	  
	  for (Reference child : this.children) {
		  if (child.elemName.equals(elemName)) {
			  Reference curChild = child.firstChild(key);
			  if (curChild!=null) {
				  if (curChild.value.equals(value))
					  return child;
			  }
		  }
	  }
	  return null;
  }
  
  public Reference moveWhere(String elemName, String key, String value) {
	  Reference tmp = firstChild(elemName, key, value);
	  if (tmp!=null)
		  return tmp;
	  else
		  return this.addChild(elemName);
  }  
	  
  
  public Reference firstChild() {
	  return this.children.getFirst();
  }
  
  public void removeFieldFromChildren(String childrenName, String field) {
	  
	  Reference curRef = this.firstChild(childrenName);
	  
	  while (curRef!=null) {
	  
		  Reference tmpRef = curRef.firstChild(field);
		  if (tmpRef!=null)
			  curRef.children.remove(tmpRef);
		  curRef = curRef.nextSibling;
		  
	  }  
	 
  }
  
  
  
  public Reference addNextSibling(String elemName) {
	  return this.parent.addChild(elemName);
  }
  

  public static Reference createReferenceFromXML(InputStream in) throws XMLStreamException  {
	
    XMLStreamReader xmlStreamReader = null;
    int eventType = 0;
    XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

    xmlStreamReader = xmlInputFactory.createXMLStreamReader(in);	
		
    // when XMLStreamReader is created, 
    // it is positioned at START_DOCUMENT event.
    eventType = xmlStreamReader.getEventType();

	int depth = 0;
	
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
		        	depth+=1;
		            break;
		        case XMLStreamConstants.END_ELEMENT:
		        	if (lastTagWasAnOpeningTag)
		        		curTree.value = curValue;
		        	curTree = curTree.parent;     	
		        	lastTagWasAnOpeningTag = false;
		        	depth-=1;
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
  
  
  public static void writeXMLFromReference(Reference ref, String filename, HttpServletResponse response) throws XMLStreamException, IOException  {
		

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
				ref = ref.children.getFirst();
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
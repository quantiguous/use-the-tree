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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;


@WebServlet("/ToCSV")
@MultipartConfig
public class ToCSV extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ToCSV() {
        super();
        
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        
        String contentType = request.getContentType();
        
        if ((contentType != null) && (contentType.indexOf("multipart/form-data") >= 0)) {       

            String filename = "csv";
            Part filePart = null;
            try {
                filePart = request.getPart("file");
                filename = getFileName(filePart);
            } catch (IOException e1) {
                returnMsg(request, response, "errorText", e1.getMessage());
            } catch (ServletException e) {
                returnMsg(request, response, "errorText", e.getLocalizedMessage());
            }
            
            if (filename.isEmpty()) {
            	 returnMsg(request, response, "errorText", "No filename found. Did you select a file?");
            }
            
            boolean useShortHeaderNames = request.getParameter("useShortHeaderNames")!=null?true:false;
            boolean writeHeaderLine = request.getParameter("writeHeaderLine")!=null?true:false;
            
            String encoding = request.getParameter("encoding");
            String valueSeparator = request.getParameter("valueSeparator");
            String lineDelimiter = request.getParameter("lineDelimiter");
            
            //String curElem = null;
            LinkedList<String> curElemStack = new LinkedList<String>();
            ArrayList<String> header = new ArrayList<String>();
            ArrayList<String> headerShort = new ArrayList<String>();
            ArrayList<ArrayList<String>> body = new ArrayList<ArrayList<String>>();
            ArrayList<String> curValueTrace = new ArrayList<String>();
            LinkedList<LinkedList<String>> curHeaderTrace = new LinkedList<LinkedList<String>>();
            LinkedList<String> curHeaderTrace2 = new LinkedList<String>();
            
            InputStream in = null;
			try {
				in = filePart.getInputStream();
			} catch (IOException e1) {
				 returnMsg(request, response, "errorText", e1.getMessage());
			}
            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            XMLStreamReader xmlStreamReader = null;
			try {
				xmlStreamReader = xmlInputFactory.createXMLStreamReader(in);
			} catch (XMLStreamException e1) {
				 returnMsg(request, response, "errorText", e1.getMessage());
			}
			
			// when XMLStreamReader is created, 
	        // it is positioned at START_DOCUMENT event.
	        int eventType = xmlStreamReader.getEventType();
	    	
	        boolean backingOut=false;
        	int depth = 0;
        	
        	ArrayList<String> curLineValues = new ArrayList<String>();
        	String curHeader = "";
        	String curElem = "";
        	String curValue = "";
        	
	        try {
	        	
				while(xmlStreamReader.hasNext()) {
				    eventType = xmlStreamReader.next();
				    
				    switch (eventType) {
				        case XMLStreamConstants.START_ELEMENT:	
				        	backingOut = false;
				        	curElem = xmlStreamReader.getLocalName();
				        	curElemStack.add(curElem);		  
				        	depth+=1;
				            break;
				        case XMLStreamConstants.END_ELEMENT:	       
				        	
				        	if (!backingOut) {					// this is a LEAF
				        		
					        	curHeader = concatenateElements(curElemStack);
					        	int position = curHeaderTrace2.indexOf(curHeader);
					        	if (position>-1) {				//Repeating Element !!!
					        		String candidatePosition=null;
					        		if (curLineValues.size()>position) {
					        			candidatePosition = curLineValues.get(position);
					        		}
					        		if (candidatePosition==null||candidatePosition.equals("")) {
					        			addAtPosition(curLineValues,curValue,position);
					        			addAtPosition(curValueTrace, curValue, position);
					        		} else {
						        		body.add(curLineValues);
						        		curLineValues = new ArrayList<String>();
						        		
						        		if (!curValueTrace.isEmpty()) {
						        		
						        			Iterator<String> curHeaderIT = curHeaderTrace2.iterator();
						        			Iterator<String> curValueIT = curValueTrace.iterator();
						        		
						        			boolean endLoop=false;
						        			while (!endLoop&&curValueIT.hasNext()) {
						        				String curValue2 = curValueIT.next();
						        				String curHeader2 = curHeaderIT.next();
						        				if (!curHeader2.equals(curHeader))
						        					curLineValues.add(curValue2);
						        				else
						        					endLoop=true;
						        			}
						        		}
						        		addAtPosition(curLineValues,curValue,position);
						        		addAtPosition(curValueTrace, curValue, position);
						        	
					        		}
					        	} else {
					        		header.add(curHeader);
					        		String headerShortEntry = curElemStack.getLast();
					        		int headerShortEntryPosition = curElemStack.size()-1;
					        		int pos1 = headerShort.indexOf(headerShortEntry);
					        		if (pos1>-1) {
					        			String otherHeaderShortEntry = headerShort.get(pos1);
						        		LinkedList<String> otherElemStack = curHeaderTrace.get(pos1);
						        		int otherHeaderShortEntryPosition = otherElemStack.size()-1;
					        			boolean doLoop=true;
					        			while (doLoop) {  
						        			headerShortEntry=curElemStack.get(headerShortEntryPosition-1) + "." + headerShortEntry;
						        			otherHeaderShortEntry=otherElemStack.get(otherHeaderShortEntryPosition-1) + "." + otherHeaderShortEntry;
						        			doLoop=otherHeaderShortEntry.equals(headerShortEntry);
					        			}
					        			headerShort.set(pos1,otherHeaderShortEntry);
					        		}
					        		headerShort.add(headerShortEntry);
					        		
					        		curLineValues.add(curValue);
					        		curValueTrace.add(curValue);
					        		LinkedList<String> curElemStack2 = new LinkedList<String>();
					        		for ( String s : curElemStack )
					        			curElemStack2.add(s);
					        		curHeaderTrace.add(curElemStack2);
					        		curHeaderTrace2.add(curHeader);
					        		
					        	}
				        	}
				        	backingOut=true;
				        	curElemStack.removeLast();        	
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
			} catch (XMLStreamException e) {
				returnMsg(request, response, "errorText", e.getMessage());
			}
	        if (!curLineValues.isEmpty())
				body.add(curLineValues);
            
	        if (writeHeaderLine)
		        if (useShortHeaderNames)
		        	body.add(0,headerShort);
		        else
		        	body.add(0, header);     
	        
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < body.size(); i++) {
                ArrayList<String> curRow = body.get(i);
                buffer.append(curRow.get(0));
                for (int j = 1; j < curRow.size(); j++) {
                    buffer.append(valueSeparator + curRow.get(j));
                }
                //buffer.append(lineDelimiter);
                buffer.append("\r\n");
            }
            byte[] result = String.valueOf(buffer).getBytes(Charset.forName(encoding));
            
          
            StringBuilder type = new StringBuilder("attachment; filename=");
            type.append(filename + ".csv");
            try {
                response.setContentLength(result.length);
                response.setContentType("application/octet-stream");
                response.setHeader("Content-Disposition", type.toString());
                response.getOutputStream().write( result );
                
            } catch (IOException e) {
                returnMsg(request, response, "errorText", e.getMessage());
            }
         
	        
        }
        
    }

    
    private void returnMsg(HttpServletRequest request, HttpServletResponse response, String attrName, String msg) {

        request.setAttribute(attrName, msg);
        RequestDispatcher requestDispatcher = request.getRequestDispatcher("/index.jsp");
        try {
           requestDispatcher.forward(request, response);
        } catch (IOException e) {
        	ReturnMsg.appendReturnMsg(response, "red", msg + "<br/><br/>" + e.getMessage());
        } catch (ServletException e) {
        	ReturnMsg.appendReturnMsg(response, "red", msg + "<br/><br/>" + e.getMessage());
        }
    }

    private String concatenateElements(LinkedList<String> linkedList) {

    	String result = "";
    	Iterator<String> it = linkedList.iterator();
    	if (it.hasNext()) result = it.next();
    	while (it.hasNext()) {
    	  result += "." + it.next();
    	}
    	return result;

    }

    private void addAtPosition(ArrayList<String> al, String s, int position ) {
    	
    	if (al.size()<=position){
			for (int i = al.size(); i<position; i++  )
				al.add("");
			al.add(s);
		} else
			al.set(position, s);
    }
       
    
    private String getFileName(final Part part) {
        for (String content : part.getHeader("content-disposition").split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }
    

 

}

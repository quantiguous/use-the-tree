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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;


@WebServlet("/ToXML")
@MultipartConfig
public class ToXML extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ToXML() {
        super();
        
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        
        String contentType = request.getContentType();
        
        String errorText = null;			// track and trace an error-variable (old style :-))
        
        if ((contentType != null) && (contentType.indexOf("multipart/form-data") >= 0)) {       

            String filename = "xml";
            Part filePart = null;
            try {
                filePart = request.getPart("file");
                filename = getFileName(filePart);
            } catch (IOException e1) {
            	errorText = e1.getMessage();
            } catch (ServletException e) {
            	errorText = e.getLocalizedMessage();
            }
            
            if (filename.isEmpty()) {
            	errorText = "No filename found. Please select a file";
            }

            String encoding = request.getParameter("encoding");
            String valueSeparator = request.getParameter("valueSeparator");
            String lineDelimiter = request.getParameter("lineDelimiter");

            String[] curElemStack = null;
            String curLine;
            String[] prevValues = null;
            String[] values = null;
            String[] headers = null;
            LinkedList<String[]> headers2 = new LinkedList<String[]>();
            String curValue = null;    
            String[] prevHeader = null;
            String[] curHeader = null;
            int curDepth = 0;
            boolean hasChanged=false;
            
            InputStream in = null;
            
            if (errorText==null) {
	            try {     
	                 in = filePart.getInputStream();
	            } catch (IOException e) {
	                  errorText = e.getMessage();
	            }
            }
            
            Scanner scanner = null;
            
            if (errorText==null) {
	            
            	scanner = new Scanner(in, encoding);
                 
	            //scanner.useDelimiter(lineDelimiter);
	            scanner.useDelimiter("\r\n");
	
	            String header = null;
	            if (scanner.hasNext())
	                header = scanner.next();
	            else
	            	errorText = "No line found. Did you send an empty file?";
	
				headers = header.split(valueSeparator);
				for ( String h : headers ) {
					curElemStack = h.split("\\.");
					headers2.add(curElemStack);	
				}
            }
            
			if (errorText==null) {
				try {
					
					StringBuilder type = new StringBuilder("attachment; filename=");
					type.append(filename + ".xml");
					
					//response.setContentLength( - not known, since writing out streaming - );
					response.setContentType("application/octet-stream");
					response.setHeader("Content-Disposition", type.toString());
					XMLOutputFactory factory = XMLOutputFactory.newInstance();
					XMLStreamWriter writer = factory.createXMLStreamWriter(
				               response.getOutputStream() );
					
					writer.writeStartDocument();
					
					while(scanner.hasNext()) {
						curLine = scanner.next();
						prevValues = values;
						values = curLine.split(valueSeparator);
						curValue=null;
						hasChanged=false;
		
						prevHeader = null;
						for (int i=0; i<values.length; i++) {
							curValue = values[i];
							boolean isEqual = prevValues!=null&&curValue.equals(prevValues[i]);
							if (!isEqual||hasChanged) {
								hasChanged=true;
								if (i>0)
									prevHeader = headers2.get(i-1);
								curHeader = headers2.get(i);
								if (prevHeader!=null) {		
									int i2 = 0;
									boolean doLoop=true;
									while (doLoop&&i2<curHeader.length){
										if (!curHeader[i2].equals(prevHeader[i2]))
											doLoop=false;
										else
											i2++;
									}
									int start = curDepth-1;
									for (int j=start; j>=i2; j--) {
										writer.writeEndElement();
										curDepth-=1;
									}
								}
								
								
								for (int j=curDepth; j<curHeader.length; j++) {
									writer.writeStartElement(curHeader[j]);
									curDepth+=1;
								}
								writer.writeCharacters(curValue);
								writer.writeEndElement();
								curDepth-=1;
							}
							
						}
					}
					
	//				for (int j=curDepth; j>=0; j--)
	//					writer.writeEndElement();
					
					writer.writeEndDocument();
					writer.flush();
					writer.close();
	
				} catch (IOException e) {
					e.printStackTrace();
				} catch (XMLStreamException e1) {
					e1.printStackTrace();
				}
			} else {
	            
		    	request.setAttribute("toXMLErrorText", errorText);
		    	RequestDispatcher requestDispatcher = request.getRequestDispatcher("/index.jsp");
		        try {
		           requestDispatcher.forward(request, response);
		        } catch (IOException | ServletException e) {
		        	e.printStackTrace();
		        }
	        }
			
        }
    
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

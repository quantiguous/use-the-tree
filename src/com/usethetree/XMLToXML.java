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
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.xml.stream.XMLStreamException;


@WebServlet("/XMLToXML")
@MultipartConfig
public class XMLToXML extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public XMLToXML() {
        super();
        
    }

   
	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
	    
		
	    String errorText = null;			// track and trace an error-variable (old style :-))
	    String contentType = null;
	    try {
	    	contentType = request.getContentType();
	    } catch (Exception e) {
	    	errorText=e.getMessage();
	    }
	    
	    if (errorText==null&&contentType!=null&&contentType.indexOf("multipart/form-data")>=0) {       
	
			String filename = "xml";
			Part filePart = null;
			try {
			    filePart = request.getPart("file");
			    filename = getFileName(filePart);
			} catch (IOException e1) {
			    errorText = e1.getMessage();
			} catch (ServletException e) {
				errorText = e.getMessage();
			}
			
			InputStream in = null;
			if (filename.isEmpty()) {
				 errorText = "No filename found. Please select a file";
			} else {
				try {
					in = filePart.getInputStream();
				} catch (IOException e1) {
					errorText = e1.getMessage();
				}
			}
			
			Reference tmp = null;
			if (errorText==null) {
				try {
					tmp = Reference.createReferenceFromXML(in);
				} catch (XMLStreamException e1) {
					errorText = e1.getMessage();
				}
			}
			
			final Reference outputRoot = new Reference("OutRoot");
			final Reference inputRoot = tmp;
			
			if (errorText==null&&filename.startsWith("Identity")) {
			   
			    	outputRoot.firstChild = inputRoot.firstChild;
			    
			} else if (errorText==null&&filename.startsWith("GroupBy1.IN")) {
		        
				Reference rInPosition = inputRoot.firstChild.firstChild;    // message.header
				Reference rOutPos = outputRoot.addChild("msg");
				rOutPos.addChild("timestamp").value = rInPosition.firstChild("date").value + " " + rInPosition.firstChild("time").value;
				rInPosition = inputRoot.firstChild("message").firstChild("orders").firstChild("order");
				rOutPos.addChild("ordNo").value = rInPosition.firstChild("orderNumber").value;
				rInPosition = rInPosition.firstChild("positions").firstChild("position");
				while (rInPosition!=null) {
					
					// use hashmap (for grouping)
					rOutPos = rOutPos.set( rInPosition.firstChild("materialNumber").value, "item" );		
					       			
					rOutPos.set("MATNR").value = rInPosition.firstChild("materialNumber").value;
					rOutPos.add("QTY", rInPosition.firstChild("quantity").value);
					
					rOutPos = rOutPos.parent;
					rInPosition = rInPosition.nextSibling;
				}
			
			} else if (errorText==null&&filename.startsWith("GroupBy2.IN")) {
			    
				Reference rOutOrder = outputRoot.addChild("message").addChild("order");
				Reference rInOrder = inputRoot.firstChild("message").firstChild("order");
				while (rInOrder!=null) {
					Reference rOutPos = rOutOrder;
					Reference rInPos = rInOrder.firstChild("position");
					while(rInPos!=null) {
				  
						// use hashmap (for grouping)
						rOutPos = rOutPos.set( rInPos.firstChild("materialNumber").value, "position" );		
						
						rOutPos.set("materialNumber").value = rInPos.firstChild("materialNumber").value;
						rOutPos.add("quantity", rInPos.firstChild("quantity").value);
						
						Reference rOutSubPos = rOutPos;
						Reference rInSubPos = rInPos.firstChild("subPos");
						while (rInSubPos!=null) {
			
							// use hashmap (for grouping)
							rOutSubPos = rOutSubPos.set(rInSubPos.firstChild("batch").value, "subPos");
							
							rOutSubPos.set("batch").value = rInSubPos.firstChild("batch").value;
							rOutSubPos.add("quantity", rInSubPos.firstChild("quantity").value);
				
							rOutSubPos = rOutSubPos.parent;
				    			rInSubPos = rInSubPos.nextSibling;
						}
						
						rOutPos = rOutPos.parent;
						rInPos = rInPos.nextSibling;
					}
					
					rOutOrder = rOutOrder.parent;
					rInOrder = rInOrder.nextSibling;
				}
			
			} else if (errorText==null&&filename.startsWith("GroupBy3.IN")) {
				
				outputRoot.addChild("message").addChild("date").value = inputRoot.firstChild("message").firstChild("header").firstChild("date").value + " " + inputRoot.firstChild("message").firstChild("header").firstChild("time").value;
				
				Reference rOutOrder = outputRoot;
				Reference rInOrder = inputRoot.firstChild("message").firstChild("orders").firstChild("order");
				while (rInOrder!=null) {
					
					rOutOrder = rOutOrder.firstChild("message").addChild("orders").addChild("order");
					rOutOrder.addChild("orderNumber").value = rInOrder.firstChild("orderNumber").value;
				
					Reference rOutPos = rOutOrder.addChild("positions");;
					Reference rInPos = rInOrder.firstChild("positions").firstChild("position");
					while (rInPos!=null) {
						
						// use hashmap (for *composite* grouping)
						rOutPos = rOutPos.set( rInPos.firstChild("materialNumber").value + "_" + rInPos.firstChild("batch").value, "position" );
						
						rOutPos.set("materialNumber").value = rInPos.firstChild("materialNumber").value;
						rOutPos.set("batch").value = rInPos.firstChild("batch").value;
						rOutPos.add("quantity", rInPos.firstChild("quantity").value);
						
						rOutPos = rOutPos.parent;
						rInPos = rInPos.nextSibling;		
					}
					
					rOutOrder = rOutOrder.parent;
					rInOrder = rInOrder.nextSibling;
				}
				
			
			} else
				if (errorText==null)
					errorText = "Did not find transformation code for " + filename + ". You have to code your transformation IN JAVA first ;-).";
			
			
			if (errorText==null) {
			
				try {
					Reference.writeXMLFromReference(outputRoot.firstChild , "Result_" + filename, response);
				} catch (XMLStreamException | IOException e) {
					e.printStackTrace();
				}  
			
				
			} else {
				
				request.setAttribute("errorText", errorText);
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

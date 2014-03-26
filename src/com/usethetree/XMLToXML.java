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
        
    	
        String errorText = null;			// we could also throw exceptions (instead of using(&tracking) this variable)
        String contentType = null;
        try {
        	contentType = request.getContentType();
        } catch (Exception e) {
        	errorText=e.getMessage();
        }
        
        boolean debugFriendly = request.getParameter("debugFriendly")!=null&&request.getParameter("debugFriendly").equals("true")?true:false;
        boolean utilizeHashmap = request.getParameter("utilizeHashmap")!=null&&request.getParameter("utilizeHashmap").equals("true")?true:false;
        
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

        	Reference inputRoot = null;
        	Ref inRoot = null;
			try {	
				if (!debugFriendly)
					inRoot = Ref.createRefFromXML(in);
				else
					inputRoot = Reference.createReferenceFromXML(in);
			} catch (XMLStreamException e1) {
				errorText = e1.getMessage();
			}
        	final Reference outputRoot = new Reference("OutRoot");
        	final Ref outRoot = new Ref("OutRoot");
        	

	        if (errorText==null&&!debugFriendly&&filename.startsWith("Identity")) {
		   
	        	outRoot.firstChild = inRoot.firstChild;

	        } else if (errorText==null&&filename.startsWith("Identity")) {
	        	
	        	@SuppressWarnings("unused")   // tmp gets used "indirectly" through "outputRoot"
				Reference tmp = outputRoot.addChild(inputRoot.firstChild().elemName);
	        	tmp = inputRoot.firstChild();
	        
	        } else if (errorText==null&&utilizeHashmap&&!debugFriendly&&filename.startsWith("GroupBy1.IN")) {
		        
	        	Ref rInPosition = inRoot.firstChild.firstChild;    // message.header
	        	Ref rOutPos = outRoot.addChild("msg");
	        	rOutPos.addChild("timestamp").value = rInPosition.firstChild("date").value + " " + rInPosition.firstChild("time").value;
	        	rInPosition = inRoot.firstChild("message").firstChild("orders").firstChild("order");
	        	rOutPos.addChild("ordNo").value = rInPosition.firstChild("orderNumber").value;
	        	rInPosition = rInPosition.firstChild("positions").firstChild("position");
	        	while (rInPosition!=null) {
	        		
	        		rOutPos = rOutPos.set( rInPosition.firstChild("materialNumber").value, "item" );			// *utilizes* hashmap (for grouping)
	        		       			
	        		rOutPos.set("MATNR").value = rInPosition.firstChild("materialNumber").value;
	        		rOutPos.add("QTY", rInPosition.firstChild("quantity").value);
	        		
	        		rOutPos = rOutPos.parent;
	        		rInPosition = rInPosition.nextSibling;
	        	}
	        
	        } else if (errorText==null&&utilizeHashmap&&!debugFriendly&&filename.startsWith("GroupBy2.IN")) {
	       	    
	        	Ref rOutOrder = outRoot.addChild("message").addChild("order");
	        	Ref rInOrder = inRoot.firstChild("message").firstChild("order");
	        	while (rInOrder!=null) {
	        		Ref rOutPos = rOutOrder;
	        		Ref rInPos = rInOrder.firstChild("position");
	        		while(rInPos!=null) {
	        	  
	        			rOutPos = rOutPos.set( rInPos.firstChild("materialNumber").value, "position" );		// *utilizes* hashmap (for grouping)
	        			
	        			rOutPos.set("materialNumber").value = rInPos.firstChild("materialNumber").value;
	        			rOutPos.add("quantity", rInPos.firstChild("quantity").value);
	        			
	        			Ref rOutSubPos = rOutPos;
	        			Ref rInSubPos = rInPos.firstChild("subPos");
	        			while (rInSubPos!=null) {
	        			
	        				rOutSubPos = rOutSubPos.set(rInSubPos.firstChild("batch").value, "subPos");		// *utilizes* hashmap (for grouping)
	        				
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
			
	        } else if (errorText==null&&utilizeHashmap&&!debugFriendly&&filename.startsWith("GroupBy3.IN")) {
				
				outRoot.addChild("message").addChild("date").value = inRoot.firstChild("message").firstChild("header").firstChild("date").value + " " + inRoot.firstChild("message").firstChild("header").firstChild("time").value;
				
				Ref rOutOrder = outRoot;
				Ref rInOrder = inRoot.firstChild("message").firstChild("orders").firstChild("order");
				while (rInOrder!=null) {
				
					rOutOrder = rOutOrder.firstChild("message").addChild("orders").addChild("order");
					rOutOrder.addChild("orderNumber").value = rInOrder.firstChild("orderNumber").value;
				
					Ref rOutPos = rOutOrder.addChild("positions");;
					Ref rInPos = rInOrder.firstChild("positions").firstChild("position");
					while (rInPos!=null) {
						
						String groupBy = rInPos.firstChild("materialNumber").value + "_" + rInPos.firstChild("batch").value;
						
						rOutPos = rOutPos.set( groupBy, "position" );		// *utilizes* hashmap (for grouping)
						
	        			rOutPos.set("materialNumber").value = rInPos.firstChild("materialNumber").value;
	        			rOutPos.set("batch").value = rInPos.firstChild("batch").value;
	        			rOutPos.add("quantity", rInPos.firstChild("quantity").value);
						
						rOutPos = rOutPos.parent;
		        		rInPos = rInPos.nextSibling;		
					}
					
					rOutOrder = rOutOrder.parent;
	        		rInOrder = rInOrder.nextSibling;
				}
	        	
	        } else if (errorText==null&&!debugFriendly&&filename.startsWith("GroupBy1.IN")) {
		        
	        	Ref rInPosition = inRoot.firstChild.firstChild;    // message.header
	        	Ref rOutPos = outRoot.addChild("msg");
	        	rOutPos.addChild("timestamp").value = rInPosition.firstChild("date").value + " " + rInPosition.firstChild("time").value;
	        	rInPosition = inRoot.firstChild("message").firstChild("orders").firstChild("order");
	        	rOutPos.addChild("ordNo").value = rInPosition.firstChild("orderNumber").value;
	        	rInPosition = rInPosition.firstChild("positions").firstChild("position");
	        	while (rInPosition!=null) {
	        		rOutPos = rOutPos.moveWhere("items", "MATNR", rInPosition.firstChild("materialNumber").value);   		
	        		
	        		rOutPos.set("MATNR").value = rInPosition.firstChild("materialNumber").value;
	        		rOutPos.add("QTY", rInPosition.firstChild("quantity").value);
	        		
	        		rOutPos = rOutPos.parent;
	        		rInPosition = rInPosition.nextSibling;
	        	}
	        
	        } else if (errorText==null&&!debugFriendly&&filename.startsWith("GroupBy2.IN")) {
	       	    
	        	Ref rOutOrder = outRoot.addChild("message").addChild("order");
	        	Ref rInOrder = inRoot.firstChild("message").firstChild("order");
	        	while (rInOrder!=null) {
	        		Ref rOutPos = rOutOrder;
	        		Ref rInPos = rInOrder.firstChild("position");
	        		while(rInPos!=null) {
	        	  
	        			rOutPos = rOutPos.moveWhere("position", "materialNumber", rInPos.firstChild("materialNumber").value);
	        			
	        			if (rOutPos.firstChild("materialNumber")==null)
	        				rOutPos.addChild("materialNumber").value = rInPos.firstChild("materialNumber").value;
	        			if (rOutPos.firstChild("quantity")==null)
	        				rOutPos.addChild("quantity").value = rInPos.firstChild("quantity").value;
	        			else
		        			rOutPos.firstChild("quantity").value = "" + (Integer.parseInt(rOutPos.firstChild("quantity").value) + Integer.parseInt(rInPos.firstChild("quantity").value));
		        		
	        			Ref rOutSubPos = rOutPos;
	        			Ref rInSubPos = rInPos.firstChild("subPos");
	        			while (rInSubPos!=null) {
	        			
	        				rOutSubPos = rOutSubPos.moveWhere("subPos", "batch", rInSubPos.firstChild("batch").value);
	        				
	        				if (rOutSubPos.firstChild("batch")==null)
		        				rOutSubPos.addChild("batch").value = rInSubPos.firstChild("batch").value;
		        			if (rOutSubPos.firstChild("quantity")==null)
		        				rOutSubPos.addChild("quantity").value = rInSubPos.firstChild("quantity").value;
		        			else
			        			rOutSubPos.firstChild("quantity").value = "" + (Integer.parseInt(rOutSubPos.firstChild("quantity").value) + Integer.parseInt(rInSubPos.firstChild("quantity").value));
			        		
		        			rOutSubPos = rOutSubPos.parent;
			        		rInSubPos = rInSubPos.nextSibling;
	        				
	        			}
	        			
	        			rOutPos = rOutPos.parent;
		        		rInPos = rInPos.nextSibling;
	        			
	        		}
	        		
	        		rOutOrder = rOutOrder.parent;
	        		rInOrder = rInOrder.nextSibling;
	        	}
			
	        } else if (errorText==null&&!debugFriendly&&filename.startsWith("GroupBy3.IN")) {
				
				outRoot.addChild("message").addChild("date").value = inRoot.firstChild("message").firstChild("header").firstChild("date").value + " " + inRoot.firstChild("message").firstChild("header").firstChild("time").value;
				
				Ref rOutOrder = outRoot;
				Ref rInOrder = inRoot.firstChild("message").firstChild("orders").firstChild("order");
				while (rInOrder!=null) {
				
					rOutOrder = rOutOrder.firstChild("message").addChild("orders").addChild("order");
					rOutOrder.addChild("orderNumber").value = rInOrder.firstChild("orderNumber").value;
				
					Ref rOutPos = rOutOrder.addChild("positions");;
					Ref rInPos = rInOrder.firstChild("positions").firstChild("position");
					while (rInPos!=null) {
						
						String groupBy = rInPos.firstChild("materialNumber").value + "_" + rInPos.firstChild("batch").value;
						
						rOutPos = rOutPos.moveWhere("position", "tmp", groupBy);
						
						if (rOutPos.firstChild("tmp")==null)
	        				rOutPos.addChild("tmp").value = groupBy;
						if (rOutPos.firstChild("materialNumber")==null)
	        				rOutPos.addChild("materialNumber").value = rInPos.firstChild("materialNumber").value;
						if (rOutPos.firstChild("batch")==null)
	        				rOutPos.addChild("batch").value = rInPos.firstChild("batch").value;
						if (rOutPos.firstChild("quantity")==null)
	        				rOutPos.addChild("quantity").value = rInPos.firstChild("quantity").value;
						else
		        			rOutPos.firstChild("quantity").value = "" + (Integer.parseInt(rOutPos.firstChild("quantity").value) + Integer.parseInt(rInPos.firstChild("quantity").value));
		        		
						rOutPos = rOutPos.parent;
		        		rInPos = rInPos.nextSibling;
						
					}
					
					rOutPos.removeFieldFromChildren("position", "tmp");
					
					rOutOrder = rOutOrder.parent;
	        		rInOrder = rInOrder.nextSibling;
				}
	        	
	        } else if (errorText==null&&filename.startsWith("GroupBy1.IN")) {
	        
	        	Reference rInPosition = inputRoot.firstChild().firstChild();   // message.header
	        	Reference rOutPos = outputRoot.addChild("msg");
	        	rOutPos.addChild("timestamp").value = rInPosition.firstChild("date").value + " " + rInPosition.firstChild("time").value;
	        	rInPosition = inputRoot.firstChild("message").firstChild("orders").firstChild("order");
	        	rOutPos.addChild("ordNo").value = rInPosition.firstChild("orderNumber").value;
	        	rInPosition = rInPosition.firstChild("positions").firstChild("position");
	        	while (rInPosition!=null) {
	        		rOutPos = rOutPos.moveWhere("items", "MATNR", rInPosition.firstChild("materialNumber").value);   		
	        		
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
	        	  
	        			rOutPos = rOutPos.moveWhere("position", "materialNumber", rInPos.firstChild("materialNumber").value);
	        			
	        			if (rOutPos.firstChild("materialNumber")==null)
	        				rOutPos.addChild("materialNumber").value = rInPos.firstChild("materialNumber").value;
	        			if (rOutPos.firstChild("quantity")==null)
	        				rOutPos.addChild("quantity").value = rInPos.firstChild("quantity").value;
	        			else
		        			rOutPos.firstChild("quantity").value = "" + (Integer.parseInt(rOutPos.firstChild("quantity").value) + Integer.parseInt(rInPos.firstChild("quantity").value));
		        		
	        			Reference rOutSubPos = rOutPos;
	        			Reference rInSubPos = rInPos.firstChild("subPos");
	        			while (rInSubPos!=null) {
	        			
	        				rOutSubPos = rOutSubPos.moveWhere("subPos", "batch", rInSubPos.firstChild("batch").value);
	        				
	        				if (rOutSubPos.firstChild("batch")==null)
		        				rOutSubPos.addChild("batch").value = rInSubPos.firstChild("batch").value;
		        			if (rOutSubPos.firstChild("quantity")==null)
		        				rOutSubPos.addChild("quantity").value = rInSubPos.firstChild("quantity").value;
		        			else
			        			rOutSubPos.firstChild("quantity").value = "" + (Integer.parseInt(rOutSubPos.firstChild("quantity").value) + Integer.parseInt(rInSubPos.firstChild("quantity").value));
			        		
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
						
						String groupBy = rInPos.firstChild("materialNumber").value + "_" + rInPos.firstChild("batch").value;
						
						rOutPos = rOutPos.moveWhere("position", "tmp", groupBy);
						
						if (rOutPos.firstChild("tmp")==null)
	        				rOutPos.addChild("tmp").value = groupBy;
						if (rOutPos.firstChild("materialNumber")==null)
	        				rOutPos.addChild("materialNumber").value = rInPos.firstChild("materialNumber").value;
						if (rOutPos.firstChild("batch")==null)
	        				rOutPos.addChild("batch").value = rInPos.firstChild("batch").value;
						if (rOutPos.firstChild("quantity")==null)
	        				rOutPos.addChild("quantity").value = rInPos.firstChild("quantity").value;
						else
		        			rOutPos.firstChild("quantity").value = "" + (Integer.parseInt(rOutPos.firstChild("quantity").value) + Integer.parseInt(rInPos.firstChild("quantity").value));
		        		
						rOutPos = rOutPos.parent;
		        		rInPos = rInPos.nextSibling;
						
					}
					
					rOutPos.removeFieldFromChildren("position", "tmp");
					
					rOutOrder = rOutOrder.parent;
	        		rInOrder = rInOrder.nextSibling;
				}
				
	        } else
	        	errorText = "Did not recognize file " + filename;
	        
	        
	        if (errorText==null) {
	        
	        	if (!debugFriendly==true) {
	        		
	        		try {
						Ref.writeXMLFromRef(outRoot.firstChild , "Result_" + filename, response);
					} catch (XMLStreamException | IOException e) {
						e.printStackTrace();
					}  
	        		
	        	} else {
	        	
	        		try {
						Reference.writeXMLFromReference(outputRoot.firstChild(), "Result_" + filename, response);
					} catch (XMLStreamException | IOException e) {
						e.printStackTrace();
					}  
		          
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
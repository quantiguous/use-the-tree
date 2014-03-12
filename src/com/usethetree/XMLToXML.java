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
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;



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
        
        String contentType = request.getContentType();
        
        if ((contentType != null) && (contentType.indexOf("multipart/form-data") >= 0)) {       

            String filename = "xml";
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
            
            String groupBy = request.getParameter("groupBy");


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
	    	
        	int depth = 0;
        	
        	String curElem = "";
        	String curValue = "";
        	Tree root = null;
        	Tree curTree = null;
        	boolean lastTagWasAnOpeningTag = false;
        	
	        try {
	        	
				while(xmlStreamReader.hasNext()) {
				    eventType = xmlStreamReader.next();
				    
				    switch (eventType) {
				        case XMLStreamConstants.START_ELEMENT:				        	
				        	curElem = xmlStreamReader.getLocalName();
				        	if (curTree==null) {
				        		root = new Tree(curElem);
				        		curTree = root;
				        	} else {
				        		curTree = curTree.addLeaf(curElem);
				        	}
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
			} catch (XMLStreamException e) {
				returnMsg(request, response, "errorText", e.getMessage());
			}
	        
	        curTree = root;
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
				
				boolean backingOut=false;
				boolean doLoop=true;
				while(doLoop) {
					
					if (!backingOut) {
						writer.writeStartElement(curTree.elemName);
						if (curTree.value!=null) {
							writer.writeCharacters(curTree.value);
							writer.writeEndElement();
						}
					}	
					
					if (!backingOut&&!curTree.leafs.isEmpty()) {
						curTree = curTree.leafs.getFirst();
					} else {
						if (curTree.nextSibling!=null) {
							curTree = curTree.nextSibling;
							backingOut=false;
						} else {
							if (curTree.parent!=null ) {
								curTree = curTree.parent;
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
	
			} catch (IOException e) {
				returnMsg(request, response, "errorText", e.getMessage());
			} catch (XMLStreamException e1) {
				 returnMsg(request, response, "errorText", e1.getMessage());
			}
            
         	        
        }
        
    }

    
    private void returnMsg(HttpServletRequest request, HttpServletResponse response, String attributeName, String message) {

        request.setAttribute(attributeName, message);
        RequestDispatcher requestDispatcher = request.getRequestDispatcher("/index.jsp");
        try {
           requestDispatcher.forward(request, response);
        } catch (IOException e) {
        	ReturnMsg.appendReturnMsg(response, "red", message + "<br/><br/>" + e.getMessage());
        } catch (ServletException e) {
        	ReturnMsg.appendReturnMsg(response, "red", message + "<br/><br/>" + e.getMessage());
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

    private String concatenateElements(String[] strings) {

    	return concatenateElements(strings, strings.length);

    }
    
    private String concatenateElements(String[] strings, int maxCount) {

    	String result = "";
    	if (strings.length>0&&maxCount>0)
    		result = strings[0];
    	int i=1;
    	while (i<strings.length && i<maxCount) {
    		result += "." + strings[i];
    		i++;
    	}
    	return result;

    }
    
    private String concatenateElements(LinkedList<String> linkedList, int maxCount) {

    	String result = "";
    	Iterator<String> it = linkedList.iterator();
    	if (it.hasNext()) 
    		result = it.next();
    	int i = 1;
    	while (it.hasNext()&&i<maxCount) {
    	  result += "." + it.next();
    	  i++;
    	}
    	return result;

    }

}

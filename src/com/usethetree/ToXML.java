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
        
        if ((contentType != null) && (contentType.indexOf("multipart/form-data") >= 0)) {       

        	String filename = "csv";
            Part filePart = null;
            try {
                filePart = request.getPart("file");
                filename = getFileName(filePart);
            } catch (IOException e1) {
                returnMessage(request, response, "errorText", e1.getMessage());
            } catch (ServletException e) {
                returnMessage(request, response, "errorText", e.getLocalizedMessage());
            }
            
            if (filename.isEmpty()) {
            	 returnMessage(request, response, "errorText", "No filename found. Did you select a file?");
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
           
            try {     
                 in = filePart.getInputStream();
            } catch (IOException e) {
                  returnMessage(request, response, "errorText", e.getMessage());
            }
            
            Scanner scanner = new Scanner(in, encoding);
            //scanner.useDelimiter(lineDelimiter);
            scanner.useDelimiter("\r\n");

            String header = null;
            if (scanner.hasNext())
                header = scanner.next();
            else
            	returnMessage(request, response, "errorText", "No line found. Did you send an empty file?" );

			headers = header.split(valueSeparator);
			for ( String h : headers ) {
				curElemStack = h.split("\\.");
				headers2.add(curElemStack);	
			}
	
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
				returnMessage(request, response, "errorText", e.getMessage());
			} catch (XMLStreamException e1) {
				 returnMessage(request, response, "errorText", e1.getMessage());
			}
	        
        }
        
    }

    
    private void returnMessage(HttpServletRequest request, HttpServletResponse response, String attributeName, String message) {

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
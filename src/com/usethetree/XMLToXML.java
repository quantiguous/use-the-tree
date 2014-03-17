package com.usethetree;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

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
            
            String cmds = request.getParameter("cmds");
            
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
        	Tree inRoot = new Tree("InRoot");
        	Tree outRoot = new Tree("OutRoot");
        	Tree curTree = inRoot;
        	boolean lastTagWasAnOpeningTag = false;
        	
	        try {
	        	
				while(xmlStreamReader.hasNext()) {
				    eventType = xmlStreamReader.next();
				    
				    switch (eventType) {
				        case XMLStreamConstants.START_ELEMENT:				        	
				        	curElem = xmlStreamReader.getLocalName();
				        	if (curTree==null) {
				        		inRoot = new Tree(curElem);
				        		curTree = inRoot;
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
    	
            HashMap<String, Tree> references = new HashMap<String, Tree>();
            
            Stack<Integer> blockStructureStackBeginLines = new Stack<Integer>();
//TODO      Stack<Integer> blockStructureStackEndLines = new Stack<Integer>();
            Stack<String> blockStructureStackTypes = new Stack<String>();
            
            String[] commands = cmds.split("\r\n");
            String[] command = null;
            boolean doLoop=false;
            
            String ErrorText = null;
            int curTabCount = 0;
            int prevTabCount = 0;
            
            for (int i=0; i<commands.length; i++) {
            	String c = commands[i];
            	String c2 = c.replace("\t", "");
            	prevTabCount = curTabCount;
            	curTabCount = c.length()-c2.length();
            		
            	command = c2.split(" ");
            	
            	int tabDifference = prevTabCount - curTabCount;
            	doLoop=true;
            	int j = 1;
            	while (j<=tabDifference&&doLoop&&blockStructureStackTypes.size()>=prevTabCount) {
            		String curBlockStructureType = blockStructureStackTypes.pop();
            		if (curBlockStructureType.equals("WHILE")) {
	            		i = blockStructureStackBeginLines.pop();
	            		c = commands[i];
	                	c2 = c.replace("\t", "");
	                	prevTabCount = curTabCount;
	                	curTabCount = c.length()-c2.length();
	                	command = c2.split(" ");
	            		if (command[2].equals("IS")&&command[3].equals("NOT")&&command[4].equals("NULL")) {
	            			Tree tmp = references.containsKey(command[1])?references.get(command[1]):null;
	            			if (tmp!=null) {
	            				blockStructureStackBeginLines.add(i);
	            				blockStructureStackTypes.add("WHILE");
	            				doLoop=false;
	            			}
	            		}
	            		j++;
	            		i++;          		       		
	            		c = commands[i];
	                	c2 = c.replace("\t", "");
	                	prevTabCount = curTabCount;
	                	curTabCount = c.length()-c2.length();
	                	command = c2.split(" ");
            		} else if (curBlockStructureType.equals("IF")) {
            			blockStructureStackBeginLines.pop();
            		}
            	}
            	
            	if (blockStructureStackTypes.size()>=curTabCount) {		// only execute commands that have an "active" WHILE
            	
	            	if (command[0].equals("REF")) {
	            		
	            		String[] keyValue = command[1].split("=");
	            		String key = keyValue[0];
	            		String value = null;
	            		Tree tmp = null;
	            		if (keyValue.length==1)
	            			if (key.startsWith("rIn"))
		            			tmp = inRoot;
		            		else
		            			tmp = outRoot;
	            		else {
	            			value = keyValue[1];
	            		
		            		String[] reference = value.split("\\.");
		       	
		            		int l = 0;
	            			if (references.containsKey(reference[0])) {
	            				tmp = references.get(reference[0]);
	            				l=1;
	            			} else if (key.startsWith("rIn"))
		            			tmp = inRoot;
		            		else
		            			tmp = outRoot;
	            			
	            			String s = null;
		            		for (int k=l;k<reference.length;k++) {
		            			s = reference[k];
		            			Tree tmp2 = tmp.firstChild(s);
		            			if (tmp2==null) {
		            				tmp = tmp.addLeaf(s);
		            			} else {
		            				tmp=tmp2;
		            			}
		            			
		            		}
	            		}
	            		references.put(key, tmp);
	            		
	            	} else if (command[0].equals("MOVE")) {
	            		
	            		if (command[2].equals("NEXT")&&command[3].equals("SIBLING")) {
	            			Tree tmp = references.containsKey(command[1])?references.get(command[1]):null;
	            			if (tmp!=null)
	            				if (command[1].startsWith("rIn"))
	            					tmp = tmp.nextSibling;
			            		else {
			            			Tree tmp2 = tmp.nextSibling;
			            			if (tmp2==null) {
			            				tmp = tmp.addNextSibling(tmp.elemName);
			            			} else {
			            				tmp=tmp2;
			            			}
			            			
			            		}
	            			
	            			references.put(command[1], tmp);
	            		
	            		} else if (command[2].equals("FIRST")&&command[3].equals("CHILD")) {
	            			Tree tmp = references.containsKey(command[1])?references.get(command[1]):null;
	            			if (tmp!=null)
	            				if (command[1].startsWith("in"))
	            					tmp = tmp.firstChild();
			            		else {
			            			Tree tmp2 = tmp.firstChild();
			            			if (tmp2==null) {
			            				tmp = tmp.addLeaf(tmp.elemName);
			            			} else {
			            				tmp=tmp2;
			            			}
			            			
			            		}
	            			
	            			references.put(command[1], tmp);
	            			
	            		} else if (command[2].equals("PARENT")) {
	            			Tree tmp = references.containsKey(command[1])?references.get(command[1]):null;
	            			if (tmp!=null)
	            				tmp = tmp.parent;
	            			
	            			references.put(command[1], tmp);
	            			
	            		} else if (command[2].equals("TO")) {
	            			Tree tmp = references.containsKey(command[1])?references.get(command[1]):null;
	            			if (tmp!=null) {
	            				String[] values=command[3].split("\\.");
	            				
			            		for (String value:values) {
				         			if (references.containsKey(value)) {
				         				tmp = references.get(value);
			            			} else {
			            				if (value.contains("+")) {
				            				value=value.replace("+", "");
				            				tmp=tmp.addLeaf(value);
				            			} else {
					            			tmp = tmp.firstChild(value);
					            			if (tmp==null)
					            				ErrorText = "Exception in Line " + i + ", Element: " + concatenateElements(values) + ", Value: " + value;
				            			}
				            		}			
			            		}
	            			}
	            			references.put(command[1], tmp);
	            		}
	            		
	            		
	            	} else if (command[0].equals("WHILE")) {
	            		if (command[2].equals("IS")&&command[3].equals("NOT")&&command[4].equals("NULL")) {
	            			Tree tmp = references.containsKey(command[1])?references.get(command[1]):null;
	            			if (tmp!=null)
	            				blockStructureStackBeginLines.add(i);
            					blockStructureStackTypes.add("WHILE");
	            		}
	            		
	            	} else if (command[0].equals("IF")) {
	            		if (command[2].equals("IS")&&command[3].equals("NOT")&&command[4].equals("NULL")) {
	            			Tree tmp = references.containsKey(command[1])?references.get(command[1]):null;
	            			if (tmp!=null)
	            				blockStructureStackBeginLines.add(i);
	            				blockStructureStackTypes.add("IF");
	            		}
	            		
	            	} else if (command[0].equals("RETURN")) {
	            		
	            		// finished.
	            		
	            	} else {   // an assignment
            	
	            		String[] keyValue = command[0].split("=");
	            		String[] keys = keyValue[0].split("\\.");
	            		String[] values = keyValue[1].split("\\.");;
	            		
	            		Tree tmpOut = null;
	            		for (String key:keys) {
		         			if (references.containsKey(key)) {
	            				tmpOut = references.get(key);
	            			} else {
	            				Tree tmp2 = tmpOut.firstChild(key);
		            			if (tmp2==null) {
		            				tmpOut = tmpOut.addLeaf(key);
		            			} else {
		            				tmpOut=tmp2;
		            			}
	            			}			
	            		}
	            		
	            		Tree tmpIn = null;
	            		for (String value:values) {
		         			if (references.containsKey(value)) {
		         				tmpIn = references.get(value);
	            			} else {
	            			tmpIn = tmpIn.firstChild(value);
	            			if (tmpIn==null)
	            				ErrorText = "Exception in Line " + i + ", Element: " + concatenateElements(keys) + ", Value: " + value;
	            			}
	            		}
	            		
	            		tmpOut.value = tmpIn.value;
	            		
	            		
	            		
	            	}
            	
            	}
            	
            }
	        
	        
	        
            curTree=outRoot.firstChild();
            		
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
				doLoop=true;
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
							if (curTree.parent!=outRoot ) {
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
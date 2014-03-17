<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
import="com.usethetree.ReturnMsg" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>www.usethetree.com - Fast, scalable and reliable message transformations</title>
</head>
<body>

<h1>Welcome to UseTheTree.com</h1>

This open source project &quot;<a href="https://github.com/mqsiuser/use-the-tree/wiki" target="_blank">use-the-tree</a>&quot; wants to provide fast, scalable and reliable message transformations.<br/>
<br/>
<font color="red">---------- currently under development! ----------</font><br/>
<br/>
Sample File: <a href="/SAP_IDOC.xml" download>SAP_IDOC.xml</a><br/>
<br/>
<h2>Tree -&gt; CSV (Status: Early preview)</h3>
<form action="ToCSV" enctype="multipart/form-data" method="POST" >
    XML-File: <input type="file" name="file" value="" size="10" title="">
    <input type="checkbox" name="writeHeaderLine" checked="checked" value="true"> write header line
    (<input type="checkbox" name="useShortHeaderNames" value="true"> use short names )<br/>
    Encoding: <input type="text" name="encoding" value="UTF-8" size="8" title="e.g. UTF-8, UnicodeLittle"><br/>
    Line Delimiter: <input type="text" name="lineDelimiter" value="\r\n" size="5" title="e.g &quot;\r\n&quot; (Windows), &quot;\n&quot; (Unix)"><br/>
    Value Separator: <input type="text" name="valueSeparator" value="," size="3" title="e.g. &quot;\t&quot; (Tabulator), &quot;,&quot; or &quot;|&quot;"><br/>    
    <br/>
    <input type="submit" name="submit" value="XML -> CSV" title="MS-Excel requires: &quot;UnicodeLittle&quot;, &quot;\r\n&quot; and &quot;\t&quot;"><br/>
</form>

<br/>

<h2>CSV -&gt; tree (Status: Early preview)</h2>
<form action="ToXML" enctype="multipart/form-data" method="POST" >
    CSV-File: <input type="file" name="file" value="" size="10" title="">
    <input type="checkbox" name="writeOutStreaming" checked="checked" value="true"> write out streaming<br/>
    Encoding: <input type="text" name="encoding" value="UTF-8" size="8" title="e.g. UTF-8, UTF-16LE"><br/>
    Line Delimiter: <input type="text" name="lineDelimiter" value="\r\n" size="5" title="e.g &quot;\r\n&quot; (Windows), &quot;\n&quot; (Unix)"><br/>
    Value Separator: <input type="text" name="valueSeparator" value="," size="3" title="e.g. &quot;\t&quot; (Tabulator), &quot;,&quot; or &quot;|&quot;"><br/>
    <br/>
    <input type="submit" name="submit" value="CSV -> XML" title="MS-Excel requires: &quot;UTF-16LE&quot;, &quot;\r\n&quot; and &quot;\t&quot;"><br/>
</form>

<br/>

<h2>Tree -&gt; tree (Status: Early preview)</h2>
<form action="XMLToXML" enctype="multipart/form-data" method="POST" >
    XML-File: <input type="file" name="file" value="" size="10" title=""><br/>
    <textarea name="cmds" cols="100" rows="30">REF rInSalesOrder=IDOC.SalesOrders
REF rOutSalesOrder=
IF rInSalesOrder IS NOT NULL
	MOVE rOutSalesOrder TO +SalesOrder
	MOVE rInSalesOrder TO SalesOrder
	WHILE rInSalesOrder IS NOT NULL
		MOVE rOutSalesOrder TO +SalesOrder
		rOutSalesOrder.ID=rInSalesOrder.CustomerId
		REF rInPosition=rInSalesOrder.Position
		REF rOutPosition=rOutSalesOrder
		WHILE rInPosition IS NOT NULL
			MOVE rOutPosition TO +Position
			rOutPosition.itemNumber=rInPosition.itemNumber
			rOutPosition.posName=rInPosition.positionName
			REF rInSubPosition=rInPosition.SubPosition
			REF rOutSubPosition=rOutPosition
			WHILE rInSubPosition IS NOT NULL
				MOVE rOutSubPosition TO +SubPos
				rOutSubPosition.charge=rInSubPosition.batch
				rOutSubPosition.quantity=rInSubPosition.qty
				MOVE rOutSubPosition PARENT
				MOVE rInSubPosition NEXT SIBLING
			MOVE rOutPosition PARENT
			MOVE rInPosition NEXT SIBLING
		MOVE rOutSalesOrder PARENT
		MOVE rInSalesOrder NEXT SIBLING
RETURN</textarea>
    <br/>
    <input type="submit" name="submit" value="XML -> XML"><br/>

</form>
<br/>
<br/>
<br/>
<br/>
<a href="/impressum.html">Impressum</a>
</body>
</html>





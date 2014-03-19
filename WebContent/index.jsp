<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
import="com.usethetree.ReturnMsg" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>www.use-the-tree.com - Fast, scalable and reliable message transformations</title>
</head>
<body>

<h1>Welcome to use-the-tree.com</h1>

The open source project <a href="https://github.com/mqsiuser/use-the-tree" target="_blank">use-the-tree</a> wants to provide fast, scalable and reliable message transformations.<br/>
<br/>
<font color="red">---------- currently under development! ----------</font><br/>

<h2>Tree -&gt; tree</h2>
<form action="XMLToXML" enctype="multipart/form-data" method="POST" >
Simple: <a href="/SAP_IDOC.xml" download>SAP_IDOC.xml</a>, <a href="/simple.tsl" download>simple.tsl</a><br/>
Simple Group By: <a href="/simpleGroupBy.xml" download>simpleGroupBy.xml</a>, <a href="/simpleGroupBy.tsl" download>simpleGroupBy.tsl</a><br/>
<br/>
    XML-File: <input type="file" name="file" value="" size="10" title=""><br/>
    <textarea name="cmds" cols="100" rows="15">REF rInSubPosition=Position
REF rOutSubPos=
IF rInSubPosition IS NOT NULL
	MOVE rOutSubPos TO +Pos
	SET rOutSubPos.item=rInSubPosition.itemNumber
	MOVE rInSubPosition TO SubPosition
	WHILE rInSubPosition IS NOT NULL
		MOVE rOutSubPos TO SubPos WHERE charge EQUALS rInSubPosition.batch
		SET rOutSubPos.charge=rInSubPosition.batch
		SET rOutSubPos.quantity+=rInSubPosition.qty
		MOVE rOutSubPos PARENT
		MOVE rInSubPosition NEXT SIBLING
RETURN</textarea>
    <br/>
    <input type="submit" name="submit" value="XML -> XML"><br/>

</form>

<% ReturnMsg.printReturnMsg(request, response); %>
<br/>

<h2>Tree -&gt; CSV</h3>
<form action="ToCSV" enctype="multipart/form-data" method="POST" >
    XML-File: <input type="file" name="file" value="" size="10" title="">
    <input type="checkbox" name="writeHeaderLine" checked="checked" value="true"> write header line
    (<input type="checkbox" name="useShortHeaderNames" value="true"> use short names )<br/>
    Encoding: <input type="text" name="encoding" value="UTF-8" size="8" title="e.g. UTF-8, UnicodeLittle">
    Line Delimiter: <input type="text" name="lineDelimiter" value="\r\n" size="5" title="e.g &quot;\r\n&quot; (Windows), &quot;\n&quot; (Unix)">
    Value Separator: <input type="text" name="valueSeparator" value="," size="3" title="e.g. &quot;\t&quot; (Tabulator), &quot;,&quot; or &quot;|&quot;"><br/>    
    <br/>
    <input type="submit" name="submit" value="XML -> CSV" title="MS-Excel requires: &quot;UnicodeLittle&quot;, &quot;\r\n&quot; and &quot;\t&quot;"><br/>
</form>

<br/>

<h2>CSV -&gt; tree</h2>
<form action="ToXML" enctype="multipart/form-data" method="POST" >
    CSV-File: <input type="file" name="file" value="" size="10" title="">
    <input type="checkbox" name="writeOutStreaming" checked="checked" value="true"> write out streaming<br/>
    Encoding: <input type="text" name="encoding" value="UTF-8" size="8" title="e.g. UTF-8, UTF-16LE">
    Line Delimiter: <input type="text" name="lineDelimiter" value="\r\n" size="5" title="e.g &quot;\r\n&quot; (Windows), &quot;\n&quot; (Unix)">
    Value Separator: <input type="text" name="valueSeparator" value="," size="3" title="e.g. &quot;\t&quot; (Tabulator), &quot;,&quot; or &quot;|&quot;"><br/>
    <br/>
    <input type="submit" name="submit" value="CSV -> XML" title="MS-Excel requires: &quot;UTF-16LE&quot;, &quot;\r\n&quot; and &quot;\t&quot;"><br/>
</form>


<br/>
<br/>
<br/>
<br/>
<a href="/impressum.html">Impressum</a>
</body>
</html>





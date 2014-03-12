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

<a href="https://github.com/mqsiuser/use-the-tree/wiki">This project</a> wants to provide fast, scalable and reliable message transformations. <br/>
<br/>
<font color="red">---------- currently under development! ----------</font>
<br/>

<h2>Tree -&gt; flatfile (Status: Early preview)</h3>
<form action="ToCSV" enctype="multipart/form-data" method="POST" >
	Sample File: <a href="/SAP_IDOC.xml" download>SAP_IDOC.xml</a><br/>
    <br/>
    XML-File: <input type="file" name="file" value="" size="10" title="">
    <input type="checkbox" name="writeHeaderLine" checked="checked" value="true"> write header line
    (<input type="checkbox" name="useShortHeaderNames" value="true"> use short names )<br/>
    Encoding: <input type="text" name="encoding" value="UTF-8" size="8" title="encoding"> (e.g. UTF-8, UTF-16LE (Excel!))<br/>
    Line Delimiter: <input type="text" name="lineDelimiter" value="\r\n" size="5" title="line delimitier"> (e.g "\r\n" (Windows (Excel!)), "\n" (Unix))<br/>
    Value Separator: <input type="text" name="valueSeparator" value="," size="3" title="value separator"> (e.g. "\t" (Tabulator (Excel!)), "," or "|")<br/>
    <br/>
    <input type="submit" name="submit" value="XML -> CSV"><br/>
</form>
<br/>
<% ReturnMsg.printReturnMsg(request, response); %>
<br/>

<h2>Flatfile -&gt; tree (Status: Early preview)</h2>

<form action="ToXML" enctype="multipart/form-data" method="POST" >
    <br/>
    File: <input type="file" name="file" value="" size="10" title=""><br/>
    Encoding: <input type="text" name="encoding" value="UTF-8" size="8" title="encoding"> (e.g. UTF-8, UTF-16LE (Excel!))<br/>
    Line Delimiter: <input type="text" name="lineDelimiter" value="\r\n" size="5" title="line delimitier"> (e.g "\r\n" (Windows (Excel!)), "\n" (Unix))<br/>
    Value Separator: <input type="text" name="valueSeparator" value="," size="3" title="value separator"> (e.g. "\t" (Tabulator (Excel!)), "," or "|")<br/>
    <br/>
    <input type="submit" name="submit" value="CSV -> XML"><br/>

</form>
<br/>
<% ReturnMsg.printReturnMsg(request, response); %>
<br/>
<br/>
<h2>Tree -&gt; tree (Status: Very early preview)</h2>
<br/>
<form action="XMLToXML" enctype="multipart/form-data" method="POST" >
    <br/>
    File: <input type="file" name="file" value="" size="10" title=""><br/>
    Group  by: <input type="text" name="groupBy" value="batch" size="8" title="groupBy"> (e.g. batch, itemNumber, orderNumber, customerID)<br/>
    <br/>
    <input type="submit" name="submit" value="XML -> XML"><br/>

</form>
<br/>
<% ReturnMsg.printReturnMsg(request, response); %>
<br/>
<br/>
<br/>
<br/>



<a href="/impressum.html">Impressum</a>

</body>
</html>





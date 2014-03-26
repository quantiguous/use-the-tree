<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>www.use-the-tree.com - Fast, scalable and reliable message transformations</title>
</head>
<body>

<h1>use-the-tree</h1>

An <a href="https://github.com/mqsiuser/use-the-tree" target="_blank" title="github">open source project</a> that wants to provide fast, scalable and reliable message transformations<br/>
<br/>
<font color="red">---------- currently under development ----------</font><br/>

<h2>Tree -&gt; tree</h2>
<form action="XMLToXML" enctype="multipart/form-data" method="POST" >
Simple Group By: <a href="/GroupBy1.IN.xml" download>GroupBy1.IN.xml</a><br/>
Multiple Group By: <a href="/GroupBy2.IN.xml" download>GroupBy2.IN.xml</a><br/>
Composite Group By: <a href="/GroupBy3.IN.xml" download>GroupBy3.IN.xml</a><br/>
<br/>
    XML-File: <input type="file"  name="file" value="" size="10" title="">
    <input type="checkbox" name="utilizeHashmap" checked="checked" value="true"> <i>utilize</i> hashmap
    <input type="checkbox" name="debugFriendly" value="true"> debug friendly<br/>
    <font color="red"><%=request.getAttribute("errorText")!=null?request.getAttribute("errorText")+"<br/>":"" %></font>
    <br/>
    <input type="submit" name="submit" value="XML -> XML"><br/>

</form>
<br/>


<h2>Tree -&gt; CSV</h3>
<form action="ToCSV" enctype="multipart/form-data" method="POST" >
    XML-File: <input type="file" name="file" value="" size="10" title="">
    <input type="checkbox" name="writeHeaderLine" checked="checked" value="true"> write header line
    (<input type="checkbox" name="useShortHeaderNames" value="true"> use short names )<br/>
    Encoding: <input type="text" name="encoding" value="UTF-8" size="8" title="e.g. UTF-8, UnicodeLittle">
    Line Delimiter: <input type="text" name="lineDelimiter" value="\r\n" size="5" title="e.g &quot;\r\n&quot; (Windows), &quot;\n&quot; (Unix)">
    Value Separator: <input type="text" name="valueSeparator" value="," size="3" title="e.g. &quot;\t&quot; (Tabulator), &quot;,&quot; or &quot;|&quot;"><br/>  
    <font color="red"><%=request.getAttribute("ToCSVErrorText")!=null?request.getAttribute("ToCSVErrorText")+"<br/>":"" %></font>  
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
    <font color="red"><%=request.getAttribute("ToXMLErrorText")!=null?request.getAttribute("ToXMLErrorText")+"<br/>":"" %></font>  
    <br/>
    <input type="submit" name="submit" value="CSV -> XML" title="MS-Excel requires: &quot;UTF-16LE&quot;, &quot;\r\n&quot; and &quot;\t&quot;"><br/>
</form>

<br/>
<br/>
<br/>
<a href="https://www.facebook.com/usethetree" target="_blank" title="Facebook">Facebook</a><br/> 
<br/>
<a href="https://twitter.com/mqsiuser" target="_blank" title="Twitter">Twitter</a><br/>
<br/>
<a href="/impressum.html">Impressum</a>
</body>
</html>





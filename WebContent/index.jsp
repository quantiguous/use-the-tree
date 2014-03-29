<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>use-the-tree.com - message transformation in like no time</title>
</head>
<body>

<div id="fb-root"></div>
<script>(function(d, s, id) {
  var js, fjs = d.getElementsByTagName(s)[0];
  if (d.getElementById(id)) return;
  js = d.createElement(s); js.id = id;
  js.src = "//connect.facebook.net/de_DE/all.js#xfbml=1";
  fjs.parentNode.insertBefore(js, fjs);
}(document, 'script', 'facebook-jssdk'));</script>

<div id="container" style="width:100%">

<div id="header" >
	<h1 style="margin-bottom:0;">Message Transformation in like <i>no time</i></h1>
	
</div>

<div id="left" style="float:left;">

		
	<br/>
	<a href="https://github.com/mqsiuser/use-the-tree/wiki" target="_blank" title="Github"><img src="/kraken1.png" width="75px" height="30px"/><img src="/github1.png" width="45px" height="20px"/></a>
	 (<a href="https://github.com/mqsiuser/use-the-tree/tree/master/src/com/usethetree" target="_blank" title="Reference.java, Key.java & ChildrenIterator.java">306 sloc</a>)
	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	<div class="fb-follow" data-href="https://www.facebook.com/usethetree" data-colorscheme="light" data-layout="standard" data-show-faces="false"></div><br/>
	<br/>
	<br/>
	Simple Group By: <a href="/GroupBy1.IN.xml" title="GroupBy1.IN.xml" download>GroupBy1.IN.xml</a> (<a href="https://github.com/mqsiuser/use-the-tree/blob/master/src/com/usethetree/XMLToXML.java#L104-119" target="_blank" title="sloc GroupBy1.IN.xml">12 sloc</a>)<br/>
	Multiple Group By: <a href="/GroupBy2.IN.xml" title="GroupBy2.IN.xml" download>GroupBy2.IN.xml</a> (<a href="https://github.com/mqsiuser/use-the-tree/blob/master/src/com/usethetree/XMLToXML.java#L123-154" target="_blank" title="sloc GroupBy2.IN.xml">21 sloc</a>)<br/>
	Composite Group By: <a href="/GroupBy3.IN.xml" title="GroupBy3.IN.xml" download>GroupBy3.IN.xml</a> (<a href="https://github.com/mqsiuser/use-the-tree/blob/master/src/com/usethetree/XMLToXML.java#L158-185" target="_blank" title="sloc GroupBy3.IN.xml">18 sloc</a>)<br/>
	<h2>tree2tree</h2>
	<form action="XMLToXML" enctype="multipart/form-data" method="POST" >
	    XML-File: <input type="file"  name="file" value="" size="10" title=""><br/>
	    <font color="red"><%=request.getAttribute("errorText")!=null?request.getAttribute("errorText")+"<br/>":"<br/>" %></font>
	    <input type="submit" name="submit" value="xml2xml"><br/>
	</form>
	
	<br/>
	
	<h2>tree2csv</h3>
	<form action="ToCSV" enctype="multipart/form-data" method="POST" >
	    XML-File: <input type="file" name="file" value="" size="10" title="">
	    <input type="checkbox" name="writeHeaderLine" checked="checked" value="true"> write header line
	    (<input type="checkbox" name="useShortHeaderNames" value="true"> use short names )<br/>
	    Encoding: <input type="text" name="encoding" value="UTF-8" size="8" title="e.g. UTF-8, UnicodeLittle">
	    Line Delimiter: <input type="text" name="lineDelimiter" value="\r\n" size="5" title="e.g &quot;\r\n&quot; (Windows), &quot;\n&quot; (Unix)">
	    Value Separator: <input type="text" name="valueSeparator" value="," size="3" title="e.g. &quot;\t&quot; (Tabulator), &quot;,&quot; or &quot;|&quot;"><br/>  
	    <font color="red"><%=request.getAttribute("toCSVErrorText")!=null?request.getAttribute("toCSVErrorText")+"<br/>":"<br/>" %></font>  
	    <input type="submit" name="submit" value="xml2csv" title="MS-Excel requires: &quot;UnicodeLittle&quot;, &quot;\r\n&quot; and &quot;\t&quot;"><br/>
	</form>
	
	<br/>
	
	<h2>csv2tree</h2>
	<form action="ToXML" enctype="multipart/form-data" method="POST" >
	    CSV-File: <input type="file" name="file" value="" size="10" title=""><br/>
	    Encoding: <input type="text" name="encoding" value="UTF-8" size="8" title="e.g. UTF-8, UTF-16LE">
	    Line Delimiter: <input type="text" name="lineDelimiter" value="\r\n" size="5" title="e.g &quot;\r\n&quot; (Windows), &quot;\n&quot; (Unix)">
	    Value Separator: <input type="text" name="valueSeparator" value="," size="3" title="e.g. &quot;\t&quot; (Tabulator), &quot;,&quot; or &quot;|&quot;"><br/>
	    <font color="red"><%=request.getAttribute("toXMLErrorText")!=null?request.getAttribute("toXMLErrorText")+"<br/>":"<br/>" %></font>  
	    <input type="submit" name="submit" value="csv2xml" title="MS-Excel requires: &quot;UTF-16LE&quot;, &quot;\r\n&quot; and &quot;\t&quot;"><br/>
	</form>
	
	<br/><br/><br/>
	

</div>

<div id="right" style="width:500px;float:left;">

	
	
	<a class="twitter-timeline"  href="https://twitter.com/mqsiuser"  data-widget-id="449243263728242688">Tweets by @mqsiuser</a>
    <script>!function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0],p=/^http:/.test(d.location)?'http':'https';if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src=p+"://platform.twitter.com/widgets.js";fjs.parentNode.insertBefore(js,fjs);}}(document,"script","twitter-wjs");</script>
	<br/><br/><br/>
	
</div>


<div id="bottom" style="clear:both;float:left;">
	
	<div class="fb-comments" data-href="http://www.use-the-tree.com/" data-width="1165" data-numposts="10" data-colorscheme="light"></div>
	<br/><br/><br/>

</div>

<div id="footer" style="clear:both;text-align:center;">
	

	<a href="/impressum.html" title="Impressum">Impressum</a>

</div>

</div>


</body>
</html>





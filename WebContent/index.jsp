<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>use-the-tree.com - fast message transformation</title>

	<link rel='stylesheet' href='style.css'></link>

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
	<h1 style="margin-bottom:0;">Fast Message Transformation</h1>
	
</div>

<div id="left" style="float:left;">

<div style="max-width:700px;text-align:justify;">
	<br/>
	<b>use-the-tree</b> provides <i>fast</i> message transformations.
	<br/>
</div>
	<br/>
	&nbsp;&nbsp;&nbsp;&nbsp;Simple Group By: <a href="/GroupBy1.IN.xml" title="GroupBy1.IN.xml" download>GroupBy1.IN.xml</a> (<a href="https://github.com/mqsiuser/use-the-tree/blob/master/src/com/usethetree/XMLToXML.java#L100-116" target="_blank" title="sloc GroupBy1.IN.xml">12 sloc</a>)<br/>
	&nbsp;&nbsp;&nbsp;&nbsp;Multiple Group By: <a href="/GroupBy2.IN.xml" title="GroupBy2.IN.xml" download>GroupBy2.IN.xml</a> (<a href="https://github.com/mqsiuser/use-the-tree/blob/master/src/com/usethetree/XMLToXML.java#L120-153" target="_blank" title="sloc GroupBy2.IN.xml">21 sloc</a>)<br/>
	&nbsp;&nbsp;&nbsp;&nbsp;Composite Group By: <a href="/GroupBy3.IN.xml" title="GroupBy3.IN.xml" download>GroupBy3.IN.xml</a> (<a href="https://github.com/mqsiuser/use-the-tree/blob/master/src/com/usethetree/XMLToXML.java#L157-183" target="_blank" title="sloc GroupBy3.IN.xml">18 sloc</a>)<br/>
	<br/>
	&nbsp;&nbsp;&nbsp;&nbsp;EDI/Edifact: <a href="/EDI_Edifact.txt" title="EDI_Edifact.txt" download>EDI_Edifact.txt</a> (<a href="https://github.com/mqsiuser/use-the-tree/blob/master/src/com/usethetree/Reference.java#L283-622" target="_blank" title="parsing code for EDI_Edifact.txt">parser</a>)<br/>
	<br/><br/>
	You can try out the 4 provided sample files here. The <a href="https://github.com/mqsiuser/use-the-tree/blob/master/src/com/usethetree/XMLToXML.java#L94-183" target="_blank" title="Java transformation code">transformation code</a> will get executed:<br/>
	<div id="rundrum">
		<form action="XMLToXML" enctype="multipart/form-data" method="POST" >
		    XML/TXT-File: <input type="file"  name="file" value="" size="10" title="">
		    (Edifact:&nbsp;&nbsp;&nbsp;&nbsp;<select name="verbosity" value="true">
		      <option>0</option>
		      <option>5</option>
		      <option selected>10</option>
		      <option>15</option>
		    </select> Verbose&nbsp;
		   <input type="checkbox" name="strict" value="true"> Strict)
		    <br/>
		    <font color="red"><%=request.getAttribute("errorText")!=null?request.getAttribute("errorText")+"<br/>":"<br/>" %></font>
		    <input type="submit" name="submit" value="in2out"><br/>
		</form>
	</div>
	<br/><br/>
	You can try out the 3 provided sample XML files here. The tree will be <a href="https://github.com/mqsiuser/use-the-tree/blob/master/src/com/usethetree/ToCSV.java#L120-L234" target="_blank">flattend down to csv</a>:<br/>
	<div id="rundrum">
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
	</div>
	<br/><br/>
	You can try out the result from tree2csv here. The csv will be <a href="https://github.com/mqsiuser/use-the-tree/blob/master/src/com/usethetree/ToXML.java#L123-L203" target="_blank">converted back into XML</a>:<br/>
	<div id="rundrum">
		<form action="ToXML" enctype="multipart/form-data" method="POST" >
		    CSV-File: <input type="file" name="file" value="" size="10" title=""><br/>
		    Encoding: <input type="text" name="encoding" value="UTF-8" size="8" title="e.g. UTF-8, UTF-16LE">
		    Line Delimiter: <input type="text" name="lineDelimiter" value="\r\n" size="5" title="e.g &quot;\r\n&quot; (Windows), &quot;\n&quot; (Unix)">
		    Value Separator: <input type="text" name="valueSeparator" value="," size="3" title="e.g. &quot;\t&quot; (Tabulator), &quot;,&quot; or &quot;|&quot;"><br/>
		    <font color="red"><%=request.getAttribute("toXMLErrorText")!=null?request.getAttribute("toXMLErrorText")+"<br/>":"<br/>" %></font>  
		    <input type="submit" name="submit" value="csv2xml" title="MS-Excel requires: &quot;UTF-16LE&quot;, &quot;\r\n&quot; and &quot;\t&quot;"><br/>
		</form>
	</div>
	<br/><br/><br/>
	
	    <div id="disqus_thread"></div>
    <script type="text/javascript">
        /* * * CONFIGURATION VARIABLES: EDIT BEFORE PASTING INTO YOUR WEBPAGE * * */
        var disqus_shortname = 'use-the-tree'; // required: replace example with your forum shortname

        /* * * DON'T EDIT BELOW THIS LINE * * */
        (function() {
            var dsq = document.createElement('script'); dsq.type = 'text/javascript'; dsq.async = true;
            dsq.src = '//' + disqus_shortname + '.disqus.com/embed.js';
            (document.getElementsByTagName('head')[0] || document.getElementsByTagName('body')[0]).appendChild(dsq);
        })();
    </script>
    <noscript>Please enable JavaScript to view the <a href="http://disqus.com/?ref_noscript">comments powered by Disqus.</a></noscript>
    <a href="http://disqus.com" class="dsq-brlink">comments powered by <span class="logo-disqus">Disqus</span></a>
    
	
</div>

<div id="right" style="width:500px;float:left;padding-left:50px;">

	
	
	<a class="twitter-timeline"  href="https://twitter.com/mqsiuser"  data-widget-id="449243263728242688">Tweets by @mqsiuser</a>
    <script>!function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0],p=/^http:/.test(d.location)?'http':'https';if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src=p+"://platform.twitter.com/widgets.js";fjs.parentNode.insertBefore(js,fjs);}}(document,"script","twitter-wjs");</script>
	<br/><br/><br/>
		
	
	<a href="https://github.com/mqsiuser/use-the-tree/wiki" target="_blank" title="Github"><img src="/github2.png" width="135px" height="60px"/></a>
	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	<a href="http://mqsiuser.tumblr.com/post/84415439481/soa-fell-out-of-favor-but-there-is-a-business-need" target="_blank" title="Motivation for fast message transformation"><img src="/tumblr2.jpeg" width="110px" height="30px"/></a>
	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	<a href="https://news.ycombinator.com/item?id=7666258" target="_blank" title="Announcement on Hacker News"><img src="/hacker-news1.jpg" width="150px" height="30px"/></a><br/>
	
	<br/><br/><br/>	
	<div id="clustrmaps-widget"></div><script type="text/javascript">var _clustrmaps = {'url' : 'http://www.use-the-tree.com', 'user' : 1139472, 'server' : '2', 'id' : 'clustrmaps-widget', 'version' : 1, 'date' : '2014-04-10', 'lang' : 'en', 'corners' : 'square' };(function (){ var s = document.createElement('script'); s.type = 'text/javascript'; s.async = true; s.src = 'http://www2.clustrmaps.com/counter/map.js'; var x = document.getElementsByTagName('script')[0]; x.parentNode.insertBefore(s, x);})();</script><noscript><a href="http://www2.clustrmaps.com/user/243116310"><img src="http://www2.clustrmaps.com/stats/maps-no_clusters/www.use-the-tree.com-thumb.jpg" alt="Locations of visitors to this page" /></a></noscript>

</div>

<!--
<div id="bottom" style="clear:both;float:left;">
	
	  <div class="fb-comments" data-href="http://www.use-the-tree.com/" data-width="800" data-numposts="10" data-colorscheme="light"></div> 
	<br/><br/><br/>

</div>
-->

<div id="bottom2" style="padding-left:50px;float:left;">
	


</div>

<div id="footer" style="clear:both;text-align:center;">
	

	<a href="/impressum.html" title="Impressum">Impressum</a>

</div>

</div>


</body>
</html>






<html>
<body>
<h1>Hello World</h1>
  
<button type="button"
onclick="document.getElementById('demo').innerHTML = Date()">
Click me to display Date and Time.</button>

<p id="demo"></p>

<form action="#" method="post">
Enter a text:<br/>
<input type="text" name="strex" id="strex" size="20" /> <button id="cryptstr">Encrypt</button><br/>
SHA512 hash string:<br/>
<input type="text" name="strcrypt" id="strcrypt" size="33" />
</form>
<script type="text/javascript">
// Here add the code of SHA512 function

// register onclick events for Encrypt button
document.getElementById('cryptstr').onclick = function() {
var txt_string = document.getElementById('strex').value;    // gets data from input text

// encrypts data and adds it in #strcrypt element
document.getElementById('strcrypt').value = SHA512(txt_string);
return false;
}
</script>

</body>
</html> 

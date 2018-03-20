<html>
<head>
    <style>
        <!-- Style for buttons -->
        .button {
            background-color: #3090C7; <!-- light blue -->
            color: white;
            padding: 15px 32px;
            text-align: center;
            text-decoration: none;
            display: inline-block;
            font-size: 16px;
            margin: 4px 2px;
            cursor: pointer;
        }
    </style>
</head>

<body>
Binary number here:
<br>
<input type="text" name="text" id="input" size="65" />
<br>
Number here:
<br>
<input type="text" name="number" id="number" size="65"/>
<br>
<button class ="button" id="Calculate" onclick="calculate()">Calculate</button>  <!-- buttons -->
<button  class ="button" id="Check" onclick="checkIfvalid()">Check</button>
<br>

Result: <br>
<textarea rows="5" cols="40" id="result">
</textarea> <!-- textarea for result -->

<script>

    function Copycat(string) {
        var sum = 0;

        if (string.charAt(0) === "0") {
            sum += 3;
            cheat = true;
        } else {
            sum += 2;
        }

        for (var i = 1; i < string.length; i++) {
            var lastinput = string.charAt(i - 1);

            if (string.charAt(i) === "1") {
                if (lastinput === "0") {
                    sum -= 2;
                } else {
                    sum += 2;
                }
            } else {
                if (lastinput === "0") {
                    sum += 0;
                } else {
                    sum += 3;
                }
            }
        }
        return sum;
    }

    function alwaysCheat(string) {
        var sum = 0;

        for (var i = 0; i < string.length; i++) {
            if (string.charAt(i) === "0") {
                sum += 0;
            } else {
                sum += 2;
            }
        }
        return sum;
    }

    function alwaysCooperate(string) {
        var sum = 0;

        for (var i = 0; i < string.length; i++) {
            if (string.charAt(i) === "0") {
                sum += 3;
            } else {
                sum += 2;
            }
        }
        return sum;
    }

    function grudger(string) {
        var sum = 0;
        var cooperate = true;

        for (var i =0; i < string.length; i++) {
            if (cooperate) { //user cooperates
                if (string.charAt(i) === "0") {  //user cheats
                    sum += 3;
                    cooperate = false;  //grudger starts to cheat
                } else {
                    sum += 2;
                }
            } else {
                if (string.charAt(i) === "0") { //both cheat
                    sum += 0;
                } else {
                    sum -= 2;
                }
            }
        }
        return sum;
    }

    function detective(string) {
        var sum = 0;
        var cheat = false;

        if (string.charAt(0) === "0") {
            sum += 3;
            cheat = true;
        } else {
            sum += 2;
        }

        if (string.charAt(1) === "0") {
            sum += 0;
            cheat = true;
        } else {
            sum -= 1;
        }

        if (string.charAt(2) === "0") {
            sum += 3;
            cheat = true;
        } else {
            sum += 2;
        }

        if (string.charAt(3) === "0") {
            sum += 3;
            cheat = true;
        } else {
            sum += 2;
        }

        for (var i = 4; i < string.length ; i++) {
            var lastinput = string.charAt(i - 1);

            if (cheat) {
                if (string.charAt(i) === "1") {
                    if (lastinput === "0") {
                        sum -= 2;
                    } else {
                        sum += 2;
                    }
                } else {
                    if (lastinput === "0") {
                        sum += 0;
                    } else {
                        sum += 3;
                    }
                }
            } else {
                if (string.charAt(i) === "0") {
                    sum += 0;
                } else {
                    sum += 2;
                }
            }
        }
        return sum;
    }

    function calculate() {
        var inputText = document.getElementById("input").value;
        var output = 0;

        if (inputText.length !== 25) {
            document.getElementById("result").value = "Input invalid";
            return;
        } else {
            output += Copycat(inputText.substring(0, 5));
            output += alwaysCheat(inputText.substring(5, 9));
            output += alwaysCooperate(inputText.substring(9, 13));
            output += grudger(inputText.substring(13, 18));
            output += detective(inputText.substring(18, 25));
            document.getElementById("result").value = output;
            return output;
        }
    }

    function checkIfvalid() {
        var inputNumber = document.getElementById("number").value;
        if (inputNumber === calculate().toString()) {
            document.getElementById("result").value = "Input is valid and solution and number match!";
        } else {
            document.getElementById("result").value = "Solution and number don't match";
        }
    }

</script>

</body>
</html>

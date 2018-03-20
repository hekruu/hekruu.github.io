<html>

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
    /*Copycat*/
    function Copycat(string) {
        var sum = 0;

        if (string.charAt(0) === "0") { //copycat cooperates
            sum += 3;
            cheat = true;
        } else {
            sum += 2;
        }

        for (var i = 1; i < string.length; i++) {  //first user"s binary number = second move for copycat
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
            if (string.charAt(i) === "0") {  //if user cheats, we both get nothing.
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
            if (string.charAt(i) === "0") {  // if user cheats, this girls still wants to be your friend.
                sum += 3;
            } else {
                sum += 2;
            }
        }
        return sum;
    }

    function grudger(string) {  //Guy with a huge yellow hat
        var sum = 0;
        var cooperate = true;  //he likes to cooperate, but if you cheat, he acts like a "always cheater".

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

    function detective(string) {  //The smart guy. First 4 rounds analyzes you, then acts like copycat or always cheater
        var sum = 0;
        var cheat = false;

        if (string.charAt(0) === "0") {  // user cheats, detective cooperates
            sum += 3;
            cheat = true;  //detective remembers this.
        } else {
            sum += 2;
        }

        if (string.charAt(1) === "0") {  // detective cheats and user cheats, they both get nothing.
            sum += 0;
            cheat = true;
        } else {
            sum -= 1;
        }

        if (string.charAt(2) === "0") {  //detective cooperates
            sum += 3;
            cheat = true;
        } else {
            sum += 2;
        }

        if (string.charAt(3) === "0") {  //detective cooperates
            sum += 3;
            cheat = true;
        } else {
            sum += 2;
        }

        for (var i = 4; i < string.length ; i++) {  // now detective starst to act like copycat or always cheater.
            var lastinput = string.charAt(i - 1);

            if (cheat) {  //if user cheated, detective acts like copycat. Code is copy paste from below.
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
            } else {  // if user didnt cheat, detective acts like always cheat.
                if (string.charAt(i) === "0") {
                    sum += 0;
                } else {
                    sum += 2;
                }
            }
        }
        return sum;
    }

    /*
    * Calculates the score for binary value.
    * */
    function calculate() {
        var inputText = document.getElementById("input").value;
        var output = 0;

        if (inputText.length !== 25) {  //binary solution must be 25 digits.
            document.getElementById("result").value = "Input invalid";
            return;
        } else {
            output += Copycat(inputText.substring(0, 5));  // 5 steps
            output += alwaysCheat(inputText.substring(5, 9));  //4 steps
            output += alwaysCooperate(inputText.substring(9, 13)); //4 steps
            output += grudger(inputText.substring(13, 18)); //5 steps
            output += detective(inputText.substring(18, 25)); //7 steps
            document.getElementById("result").value = output;
            return output;
        }
    }
    /*
    *Script for "check" button, calculates the value from binary and checks if the given number and binary value match.
     */
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

package ee.ttu.iti0202.gomoku.strategies;

import ee.ttu.iti0202.gomoku.game.Location;
import ee.ttu.iti0202.gomoku.game.SimpleBoard;
import ee.ttu.iti0202.gomoku.opponent.ComputerStrategy;

import java.util.*;

/*

 Anto Mätas (anto.matas, 180481TAF)
 Helina Kruuk (hekruu, 179546IAIB)

 */
public class Anto_Helina_Strategy implements ComputerStrategy {

    private Map<String, Double> tenByTenBaseValue = new HashMap<>();
    private Map<String, Double> twentyByTwentyBaseValue = new HashMap<>();
    private Map<String, Double> currentBaseValue;

    private boolean timeIsUp, patternIsCorrect, maybeWeHaveWinningMove;

    private final int NR_OF_SEQUENTIAL_ELEMENTS_TO_WIN = 5;
    private final double DIAGONAL_APLIFIER = 1.1;
    private final double B_TEN_TWO_SQUARE_VALUE = 170.0;
    private final double B_TEN_THREE_MARK_VALUE = 30000000.0;
    private final double B_TEN_FOUR_MARK_VALUE = 5100000000000.0;
    private final double B_TEN_WIN_VALUE = 600000000000000000000000000.0;
    private final double FIRST_MOVER_ADVANTAGE = 1.5;
    private final double MAXIMUM_MOVE_TIME_ALLOWED = 1.0;

    private double twoMarkvalue, threeMarkValue, fourMarkValue, winValue;

    private SimpleBoard board;
    private int myPlayer, bestMoveRow, bestMoveCol, winCheckValue,
            tempBestMoveRow, tempBesMoveCol;

    public Anto_Helina_Strategy() {
        createTenByTenBaseTable();
        createTwenyByTwentyBaseTable();
    }

    @Override
    public Location getMove(SimpleBoard board, int player) {
        if (board.getHeight() == 10) {
            currentBaseValue = tenByTenBaseValue;
            twoMarkvalue = B_TEN_TWO_SQUARE_VALUE;
            threeMarkValue = B_TEN_THREE_MARK_VALUE;
            fourMarkValue = B_TEN_FOUR_MARK_VALUE;
            winValue = B_TEN_WIN_VALUE;
        } else {
            twoMarkvalue = B_TEN_TWO_SQUARE_VALUE;
            threeMarkValue = B_TEN_THREE_MARK_VALUE;
            fourMarkValue = B_TEN_FOUR_MARK_VALUE;
            winValue = B_TEN_WIN_VALUE;
            currentBaseValue = twentyByTwentyBaseValue;
        }
        timeIsUp = false;
        //startStopwatch();
        this.myPlayer = player;
        this.board = board;
        //if noone has moved
        if (!board.getLastMove(SimpleBoard.PLAYER_WHITE).isPresent()) {
            if (board.getHeight() == 10) {
                return Location.of(5, 5);
            }
            return Location.of(10, 10);
        }
        minimax(player, 2);
        if(maybeWeHaveWinningMove) {
            tempBesMoveCol = bestMoveCol;
            tempBestMoveRow = bestMoveRow;
            if (minimax(player, 1) == winValue) {
                Location.of(bestMoveRow, bestMoveCol);
            } else {
                return Location.of(tempBestMoveRow, tempBesMoveCol);
            }
        }
        return Location.of(bestMoveRow, bestMoveCol);

    }

    private Double minimax(int player, int depth) {
        // if depth reached: return board score
        if (depth == 0) return evaluate(opponentOf(player));

        // else
        // get all possible moves
        Map<Integer, HashMap<Integer, Double>> emptyMoves = emptyMovesAroundPlayerButtons();

        Double bestScore = Double.MAX_VALUE; // opponent
        if (myPlayer == player) bestScore = Double.NEGATIVE_INFINITY;

        for (Map.Entry<Integer, HashMap<Integer, Double>> rowElement : emptyMoves.entrySet()) {
            for (Map.Entry<Integer, Double> colElement : rowElement.getValue().entrySet()) {

                // make imaginative move
                board.setLocation(rowElement.getKey(), colElement.getKey(), player);

                //check the status now
                Double score = minimax(opponentOf(player), depth - 1);
                //System.out.println(score);
                if (myPlayer == player) {
                    if(score > bestScore) {
                        bestScore = score;
                        bestMoveRow = rowElement.getKey();
                        bestMoveCol = colElement.getKey();
                    }
                } else {
                    bestScore = Math.min(bestScore, score);
                }



                // take the imaginative move back
                board.setLocation(rowElement.getKey(), colElement.getKey(), SimpleBoard.EMPTY);

            }
        }
        //System.out.println("Best: " + bestScore);
        return bestScore;
    }



    private Double evaluate(int player) {
        Map<Integer, HashMap<Integer, Double>> emptyMoves = emptyMovesAroundPlayerButtons();
        Double firstPlayerEvaluation = 0.0, //first is the opponent
                secondPlayerEvalutaion = 0.0; //second is the player

        winCheckValue = evaluateWin();
        if (winCheckValue == 100) {
            return winValue;
        } else if (winCheckValue == -100) {
            maybeWeHaveWinningMove = true;
            return -winValue;
        }


        for (Map.Entry<Integer, HashMap<Integer, Double>> rowElement : emptyMoves.entrySet()) {
            for (Map.Entry<Integer, Double> colElement : rowElement.getValue().entrySet()) {

                //nüüd ma olen ühel suvalisel vabal ruudul
                firstPlayerEvaluation += getScoreValueWhenLookingForFourOpenCombinations(rowElement.getKey(), colElement.getKey(), opponentOf(player));
                secondPlayerEvalutaion += getScoreValueWhenLookingForFourOpenCombinations(rowElement.getKey(), colElement.getKey(), player);

                firstPlayerEvaluation += getScoreValueWhenLookingForFourCombinations(rowElement.getKey(), colElement.getKey(), opponentOf(player));
                secondPlayerEvalutaion += getScoreValueWhenLookingForFourCombinations(rowElement.getKey(), colElement.getKey(), player);


                firstPlayerEvaluation += getScoreValueWhenLookingForThreeCombinations(rowElement.getKey(), colElement.getKey(), opponentOf(player));
                secondPlayerEvalutaion += getScoreValueWhenLookingForThreeCombinations(rowElement.getKey(), colElement.getKey(), player);
                if (firstPlayerEvaluation < fourMarkValue) {
                    firstPlayerEvaluation += getScoreValueWhenLookingForTwoCombinations(rowElement.getKey(), colElement.getKey(), opponentOf(player));
                }
                if (secondPlayerEvalutaion < fourMarkValue) {
                    secondPlayerEvalutaion += getScoreValueWhenLookingForTwoCombinations(rowElement.getKey(), colElement.getKey(), player);
                }

                if (firstPlayerEvaluation < threeMarkValue) {
                    firstPlayerEvaluation += getScoreForSquareWhenLookingOnlyMyNeighour(rowElement.getKey(), colElement.getKey(), opponentOf(player));
                }
                if (secondPlayerEvalutaion < threeMarkValue) {
                    secondPlayerEvalutaion += getScoreForSquareWhenLookingOnlyMyNeighour(rowElement.getKey(), colElement.getKey(), player);
                }



                //System.out.println(rowElement.getKey() + ", " + colElement.getKey() + ": " + colElement.getValue());
            }
        }

        if (firstPlayerEvaluation * FIRST_MOVER_ADVANTAGE >= secondPlayerEvalutaion) {
            if (player == myPlayer) {
                return -1 * firstPlayerEvaluation;
            } else {
                return firstPlayerEvaluation;
            }
        } else {
            if (player == myPlayer) {
                return secondPlayerEvalutaion;
            } else {
                return -1 * secondPlayerEvalutaion;
            }
        }


    }

    private Double getScoreValueWhenLookingForTwoCombinations(int row, int col, int player) {
        Double sumOfPatterns = 0.0;

        sumOfPatterns += getScoreForPattern("OXEXE", twoMarkvalue, row, col, player);
        sumOfPatterns += getScoreForPattern("OXEEX", twoMarkvalue * 0.9, row, col, player);
        sumOfPatterns += getScoreForPattern("XOXEE", twoMarkvalue * 1.5, row, col, player);
        sumOfPatterns += getScoreForPattern("XOEXE", twoMarkvalue * 0.8, row, col, player);
        sumOfPatterns += getScoreForPattern("XOEEX", twoMarkvalue * 0.7, row, col, player);
        sumOfPatterns += getScoreForPattern("EOXEX", twoMarkvalue, row, col, player);
        sumOfPatterns += getScoreForPattern("XXOEE", twoMarkvalue * 2, row, col, player);
        sumOfPatterns += getScoreForPattern("EXOXE", twoMarkvalue * 0.75, row, col, player);
        sumOfPatterns += getScoreForPattern("EXOEX", twoMarkvalue * 0.45, row, col, player);
        sumOfPatterns += getScoreForPattern("EEOXX", twoMarkvalue * 2, row, col, player);

        return sumOfPatterns;
    }

    private Double getScoreValueWhenLookingForThreeCombinations(int row, int col, int player) {
        Double sumOfPatterns = 0.0;
        sumOfPatterns += getScoreForPattern("OXXEX", threeMarkValue * 0.9, row, col, player);
        sumOfPatterns += getScoreForPattern("OXEXX", threeMarkValue * 0.9, row, col, player);
        sumOfPatterns += getScoreForPattern("XOXXE", threeMarkValue * 1.2, row, col, player);
        sumOfPatterns += getScoreForPattern("XOXEX", threeMarkValue * 0.9, row, col, player);
        sumOfPatterns += getScoreForPattern("XOEXX", threeMarkValue * 0.9, row, col, player);
        sumOfPatterns += getScoreForPattern("EOXXX", threeMarkValue * 1.5, row, col, player);
        sumOfPatterns += getScoreForPattern("XXOXE", threeMarkValue * 0.6, row, col, player);
        sumOfPatterns += getScoreForPattern("XXOEX", threeMarkValue * 0.9, row, col, player);
        sumOfPatterns += getScoreForPattern("EXOXX", threeMarkValue * 0.9, row, col, player);

        //exceptions as open-ended
        sumOfPatterns += getScoreForPattern("OXXEE", threeMarkValue * 0.8, row, col, player);
        sumOfPatterns += getScoreForPattern("EOXXE", threeMarkValue * 0.8, row, col, player);

        return sumOfPatterns;
    }

    private Double getScoreValueWhenLookingForFourCombinations(int row, int col, int player) {
        Double sumOfPatterns = 0.0;
        sumOfPatterns += getScoreForPattern("OXXXX", fourMarkValue, row, col, player);
        sumOfPatterns += getScoreForPattern("XOXXX", fourMarkValue, row, col, player);
        sumOfPatterns += getScoreForPattern("XXOXX", fourMarkValue, row, col, player);

        //exception as open-ended
        sumOfPatterns += getScoreForPattern("OXXXE", fourMarkValue * 0.8, row, col, player);

        return sumOfPatterns;
    }

    private Double getScoreValueWhenLookingForFourOpenCombinations(int row, int col, int player) {
        Double sumOfPatterns = 0.0;
        sumOfPatterns += getScoreForPattern("OXXXXE", fourMarkValue * 5 * 50, row, col, player);
        sumOfPatterns += getScoreForPattern("EXOXXXE", fourMarkValue * 1.5 * 50, row, col, player);
        sumOfPatterns += getScoreForPattern("EXXOXXE", fourMarkValue * 50, row, col, player);

        return sumOfPatterns;
    }

    /**
     * @param pattern //EEXOX
     *                E - empty square
     *                X - player button
     *                O - current square I am on
     * @return
     */
    private Double getScoreForPattern(String pattern, Double patternValue, int row, int col, int player) {
        int expectedValue[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int amountOfSquaresLeft = 0;
        int amountOfSquaresRight = 0;
        Double thisPatternValue = 0.0;

        for(int i = 0; i < pattern.length(); i++) {
            if(pattern.charAt(i) == 'X') {
                expectedValue[i] = player;
            }
            if(pattern.charAt(i) == 'O') {
                amountOfSquaresLeft = i;
                amountOfSquaresRight = pattern.length() - 1 - i;
            }
        }

        thisPatternValue += patternValueToRightHorizontal(patternValue, row, col, amountOfSquaresLeft, amountOfSquaresRight, expectedValue);
        thisPatternValue += patternValueToRightDiagonalDown(patternValue, row, col, amountOfSquaresLeft, amountOfSquaresRight, expectedValue);
        thisPatternValue += patternValueToVerticalDown(patternValue, row, col, amountOfSquaresLeft, amountOfSquaresRight, expectedValue);
        thisPatternValue += patternValueToLeftDiagonalDown(patternValue, row, col, amountOfSquaresLeft, amountOfSquaresRight, expectedValue);
        thisPatternValue += patternValueToLeftHorizontal(patternValue, row, col, amountOfSquaresLeft, amountOfSquaresRight, expectedValue);
        thisPatternValue += patternValueToLeftDiagonalUp(patternValue, row, col, amountOfSquaresLeft, amountOfSquaresRight, expectedValue);
        thisPatternValue += patternValueToVerticalUp(patternValue, row, col, amountOfSquaresLeft, amountOfSquaresRight, expectedValue);
        thisPatternValue += patternValueToRightDiagonalUp(patternValue, row, col, amountOfSquaresLeft, amountOfSquaresRight, expectedValue);


        return thisPatternValue;
    }

    private Double patternValueToRightHorizontal(Double patternValue, int row, int col, int amountOfSquaresLeft, int amountOfSquaresRight, int[] expectedValue) {
        if (amIInsideTableIfIMoveLeftHorizontalNrOfSquares(row, col, amountOfSquaresLeft)) {
            if (amIInsideTableIfIMoveRightHorizontalNrOfSquares(row, col, amountOfSquaresRight)) {
                patternIsCorrect = true;
                for(int i = 0; i < amountOfSquaresLeft + amountOfSquaresRight + 1; i++) {
                    if (board.getLocation(row, col - amountOfSquaresLeft + i) != expectedValue[i]){
                        patternIsCorrect = false;
                    }
                }
                if (patternIsCorrect) {
                    return patternValue;
                }
            }
        }
        return 0.0;
    }

    private Double patternValueToRightDiagonalDown(Double patternValue, int row, int col, int amountOfSquaresLeft, int amountOfSquaresRight, int[] expectedValue) {
        if (amIInsideTableIfIMoveLeftDiagUpNrOfSquares(row, col, amountOfSquaresLeft)) {
            if (amIInsideTableIfIMoveRightDiagDownNrOfSquares(row, col, amountOfSquaresRight)) {
                patternIsCorrect = true;
                for(int i = 0; i < amountOfSquaresLeft + amountOfSquaresRight + 1; i++) {
                    if (board.getLocation(row - amountOfSquaresLeft + i, col - amountOfSquaresLeft + i) != expectedValue[i]){
                        patternIsCorrect = false;
                    }
                }
                if (patternIsCorrect) {
                    return patternValue * DIAGONAL_APLIFIER;
                }
            }
        }
        return 0.0;
    }

    private Double patternValueToVerticalDown(Double patternValue, int row, int col, int amountOfSquaresLeft, int amountOfSquaresRight, int[] expectedValue) {
        if (amIInsideTableIfIMoveVerticalUpNrOfSquares(row, col, amountOfSquaresLeft)) {
            if (amIInsideTableIfIMoveVerticalDownNrOfSquares(row, col, amountOfSquaresRight)) {
                patternIsCorrect = true;
                for(int i = 0; i < amountOfSquaresLeft + amountOfSquaresRight + 1; i++) {
                    if (board.getLocation(row - amountOfSquaresLeft + i, col) != expectedValue[i]){
                        patternIsCorrect = false;
                    }
                }
                if (patternIsCorrect) {
                    return patternValue;
                }
            }
        }
        return 0.0;
    }

    private Double patternValueToLeftDiagonalDown(Double patternValue, int row, int col, int amountOfSquaresLeft, int amountOfSquaresRight, int[] expectedValue) {
        if (amIInsideTableIfIMoveRightDiagUpNrOfSquares(row, col, amountOfSquaresLeft)) {
            if (amIInsideTableIfIMoveLeftDiagDownNrOfSquares(row, col, amountOfSquaresRight)) {
                patternIsCorrect = true;
                for(int i = 0; i < amountOfSquaresLeft + amountOfSquaresRight + 1; i++) {
                    if (board.getLocation(row - amountOfSquaresLeft + i, col + amountOfSquaresLeft - i) != expectedValue[i]){
                        patternIsCorrect = false;
                    }
                }
                if (patternIsCorrect) {
                    return patternValue * DIAGONAL_APLIFIER;
                }
            }
        }
        return 0.0;
    }

    private Double patternValueToLeftHorizontal(Double patternValue, int row, int col, int amountOfSquaresLeft, int amountOfSquaresRight, int[] expectedValue) {
        if (amIInsideTableIfIMoveRightHorizontalNrOfSquares(row, col, amountOfSquaresLeft)) {
            if (amIInsideTableIfIMoveLeftHorizontalNrOfSquares(row, col, amountOfSquaresRight)) {
                patternIsCorrect = true;
                for(int i = 0; i < amountOfSquaresLeft + amountOfSquaresRight + 1; i++) {
                    if (board.getLocation(row, col + amountOfSquaresLeft - i) != expectedValue[i]){
                        patternIsCorrect = false;
                    }
                }
                if (patternIsCorrect) {
                    return patternValue;
                }
            }
        }
        return 0.0;
    }

    private Double patternValueToLeftDiagonalUp(Double patternValue, int row, int col, int amountOfSquaresLeft, int amountOfSquaresRight, int[] expectedValue) {
        if (amIInsideTableIfIMoveRightDiagDownNrOfSquares(row, col, amountOfSquaresLeft)) {
            if (amIInsideTableIfIMoveLeftDiagUpNrOfSquares(row, col, amountOfSquaresRight)) {
                patternIsCorrect = true;
                for(int i = 0; i < amountOfSquaresLeft + amountOfSquaresRight + 1; i++) {
                    if (board.getLocation(row + amountOfSquaresLeft - i, col + amountOfSquaresLeft - i) != expectedValue[i]){
                        patternIsCorrect = false;
                    }
                }
                if (patternIsCorrect) {
                    return patternValue * DIAGONAL_APLIFIER;
                }
            }
        }
        return 0.0;
    }

    private Double patternValueToVerticalUp(Double patternValue, int row, int col, int amountOfSquaresLeft, int amountOfSquaresRight, int[] expectedValue) {
        if (amIInsideTableIfIMoveVerticalDownNrOfSquares(row, col, amountOfSquaresLeft)) {
            if (amIInsideTableIfIMoveVerticalUpNrOfSquares(row, col, amountOfSquaresRight)) {
                patternIsCorrect = true;
                for(int i = 0; i < amountOfSquaresLeft + amountOfSquaresRight + 1; i++) {
                    if (board.getLocation(row + amountOfSquaresLeft - i, col) != expectedValue[i]){
                        patternIsCorrect = false;
                    }
                }
                if (patternIsCorrect) {
                    return patternValue;
                }
            }
        }
        return 0.0;
    }

    private Double patternValueToRightDiagonalUp(Double patternValue, int row, int col, int amountOfSquaresLeft, int amountOfSquaresRight, int[] expectedValue) {
        if (amIInsideTableIfIMoveLeftDiagDownNrOfSquares(row, col, amountOfSquaresLeft)) {
            if (amIInsideTableIfIMoveRightDiagUpNrOfSquares(row, col, amountOfSquaresRight)) {
                patternIsCorrect = true;
                for(int i = 0; i < amountOfSquaresLeft + amountOfSquaresRight + 1; i++) {
                    if (board.getLocation(row + amountOfSquaresLeft - i, col - amountOfSquaresLeft + i) != expectedValue[i]){
                        patternIsCorrect = false;
                    }
                }
                if (patternIsCorrect) {
                    return patternValue * DIAGONAL_APLIFIER;
                }
            }
        }
        return 0.0;
    }




    private Double getScoreForSquareWhenLookingOnlyMyNeighour(int row, int col, int player) {
        double squareValue = 0;
        if(amIInsideTableIfIMoveVerticalUpNrOfSquares(row, col, 1)) {
            if(board.getLocation(row - 1, col) == player) {
                squareValue += getCurrentBaseValueFor(row, col);
            }
        }

        if(amIInsideTableIfIMoveRightDiagUpNrOfSquares(row, col, 1)) {
            if(board.getLocation(row - 1, col + 1) == player) {
                squareValue += getCurrentBaseValueFor(row, col) * DIAGONAL_APLIFIER;
            }
        }

        if(amIInsideTableIfIMoveRightHorizontalNrOfSquares(row, col, 1)) {
            if(board.getLocation(row, col + 1) == player) {
                squareValue += getCurrentBaseValueFor(row, col);
            }
        }

        if(amIInsideTableIfIMoveRightDiagDownNrOfSquares(row, col, 1)) {
            if(board.getLocation(row + 1, col + 1) == player) {
                squareValue += getCurrentBaseValueFor(row, col) * DIAGONAL_APLIFIER;
            }
        }

        if(amIInsideTableIfIMoveVerticalDownNrOfSquares(row, col, 1)) {
            if(board.getLocation(row + 1, col) == player) {
                squareValue += getCurrentBaseValueFor(row, col);
            }
        }

        if(amIInsideTableIfIMoveLeftDiagDownNrOfSquares(row, col, 1)) {
            if(board.getLocation(row + 1, col - 1) == player) {
                squareValue += getCurrentBaseValueFor(row, col) * DIAGONAL_APLIFIER;
            }
        }

        if(amIInsideTableIfIMoveLeftHorizontalNrOfSquares(row, col, 1)) {
            if(board.getLocation(row, col - 1) == player) {
                squareValue += getCurrentBaseValueFor(row, col);
            }
        }

        if(amIInsideTableIfIMoveLeftDiagUpNrOfSquares(row, col, 1)) {
            if(board.getLocation(row - 1, col - 1) == player) {
                squareValue += getCurrentBaseValueFor(row, col) * DIAGONAL_APLIFIER;
            }
        }

        return squareValue;
    }




    private boolean amIInsideTableIfIMoveVerticalUpNrOfSquares(int row, int col, int nrOfSquares) {
        if (row >= nrOfSquares) {
            return true;
        }
        return false;
    }

    private boolean amIInsideTableIfIMoveRightDiagUpNrOfSquares(int row, int col, int nrOfSquares) {
        if (amIInsideTableIfIMoveRightHorizontalNrOfSquares(row, col, nrOfSquares)) {
            if (amIInsideTableIfIMoveVerticalUpNrOfSquares(row, col, nrOfSquares)) {
                return true;
            }
        }
        return false;
    }

    private boolean amIInsideTableIfIMoveRightHorizontalNrOfSquares(int row, int col, int nrOfSquares) {
        if (board.getWidth() >= col + 1 + nrOfSquares) {
            return true;
        }
        return false;
    }

    private boolean amIInsideTableIfIMoveRightDiagDownNrOfSquares(int row, int col, int nrOfSquares) {
        if (amIInsideTableIfIMoveRightHorizontalNrOfSquares(row, col, nrOfSquares)) {
            if (amIInsideTableIfIMoveVerticalDownNrOfSquares(row, col, nrOfSquares)) {
                return true;
            }
        }
        return false;
    }

    private boolean amIInsideTableIfIMoveVerticalDownNrOfSquares(int row, int col, int nrOfSquares) {
        if (board.getHeight() >= row + 1 + nrOfSquares) {
            return true;
        }
        return false;
    }

    private boolean amIInsideTableIfIMoveLeftDiagDownNrOfSquares(int row, int col, int nrOfSquares) {
        if (amIInsideTableIfIMoveLeftHorizontalNrOfSquares(row, col, nrOfSquares)) {
            if (amIInsideTableIfIMoveVerticalDownNrOfSquares(row, col, nrOfSquares)) {
                return true;
            }
        }
        return false;
    }

    private boolean amIInsideTableIfIMoveLeftHorizontalNrOfSquares(int row, int col, int nrOfSquares) {
        if (col >= nrOfSquares) {
            return true;
        }
        return false;
    }

    private boolean amIInsideTableIfIMoveLeftDiagUpNrOfSquares(int row, int col, int nrOfSquares) {
        if (amIInsideTableIfIMoveLeftHorizontalNrOfSquares(row, col, nrOfSquares)) {
            if (amIInsideTableIfIMoveVerticalUpNrOfSquares(row, col, nrOfSquares)) {
                return true;
            }
        }
        return false;
    }

    private int evaluateWin() {
        //horisontal
        for (int cRow = 0; cRow < board.getHeight(); cRow++) {
            for (int cCol = 0; cCol < board.getWidth() - NR_OF_SEQUENTIAL_ELEMENTS_TO_WIN + 1; cCol++) {
                int sum = winHorisontalRightCondition(cRow, cCol);
                if (sum == NR_OF_SEQUENTIAL_ELEMENTS_TO_WIN * myPlayer) {
                    //System.out.println("Horisontal - Row: " + cRow + " Col: " + cCol);
                    return 100;
                }
                if (sum == NR_OF_SEQUENTIAL_ELEMENTS_TO_WIN * opponentOf(myPlayer)) {
                    //System.out.println("Horisontal - Row: " + cRow + " Col: " + cCol);
                    return -100;
                }
            }
        }
        //vertical
        for (int cRow = 0; cRow < board.getHeight() - NR_OF_SEQUENTIAL_ELEMENTS_TO_WIN + 1; cRow++) {
            for (int cCol = 0; cCol < board.getWidth(); cCol++) {
                int sum = winVerticalDownCondition(cRow, cCol);
                if (sum == NR_OF_SEQUENTIAL_ELEMENTS_TO_WIN * myPlayer) {
                    //System.out.println("Vertical - Row: " + cRow + " Col: " + cCol);
                    return 100;
                }
                if (sum == NR_OF_SEQUENTIAL_ELEMENTS_TO_WIN * opponentOf(myPlayer)) {
                    //System.out.println("Vertical - Row: " + cRow + " Col: " + cCol);
                    return -100;
                }
            }
        }

        //right-down-diag
        for (int cRow = 0; cRow < board.getHeight() - NR_OF_SEQUENTIAL_ELEMENTS_TO_WIN + 1; cRow++) {
            for (int cCol = 0; cCol < board.getWidth() - NR_OF_SEQUENTIAL_ELEMENTS_TO_WIN + 1; cCol++) {
                int sum = winRightDownDiagCondition(cRow, cCol);
                if (sum == NR_OF_SEQUENTIAL_ELEMENTS_TO_WIN * myPlayer) {
                    //System.out.println("right-down-diag - Row: " + cRow + " Col: " + cCol);
                    return 100;
                }
                if (sum == NR_OF_SEQUENTIAL_ELEMENTS_TO_WIN * opponentOf(myPlayer)) {
                    //System.out.println("right-down-diag - Row: " + cRow + " Col: " + cCol);
                    return -100;
                }
            }
        }

        //left-down-diag
        for (int cRow = 0; cRow < board.getHeight() - NR_OF_SEQUENTIAL_ELEMENTS_TO_WIN + 1; cRow++) {
            for (int cCol = NR_OF_SEQUENTIAL_ELEMENTS_TO_WIN - 1; cCol < board.getWidth(); cCol++) {
                int sum = winLeftDownDiagCondition(cRow, cCol);
                if (sum == NR_OF_SEQUENTIAL_ELEMENTS_TO_WIN * myPlayer) {
                    //System.out.println("left-down-diag - Row: " + cRow + " Col: " + cCol);
                    return 100;
                }
                if (sum == NR_OF_SEQUENTIAL_ELEMENTS_TO_WIN * opponentOf(myPlayer)) {
                    //System.out.println("left-down-diag - Row: " + cRow + " Col: " + cCol);
                    return -100;
                }
            }
        }
        return 0;
    }

    private int winHorisontalRightCondition(int cRow, int cCol) {
        int sum = 0;
        for (int i = 0; i < NR_OF_SEQUENTIAL_ELEMENTS_TO_WIN; i++) {
            sum += board.getLocation(cRow, cCol + i);
        }
        return sum;
    }

    private int winVerticalDownCondition(int cRow, int cCol) {
        int sum = 0;
        for (int i = 0; i < NR_OF_SEQUENTIAL_ELEMENTS_TO_WIN; i++) {
            sum += board.getLocation(cRow + i, cCol);
        }
        return sum;
    }

    private int winRightDownDiagCondition(int cRow, int cCol) {
        int sum = 0;
        for (int i = 0; i < NR_OF_SEQUENTIAL_ELEMENTS_TO_WIN; i++) {
            sum += board.getLocation(cRow + i, cCol + i);
        }
        return sum;
    }

    private int winLeftDownDiagCondition(int cRow, int cCol) {
        int sum = 0;
        for (int i = 0; i < NR_OF_SEQUENTIAL_ELEMENTS_TO_WIN; i++) {
            sum += board.getLocation(cRow + i, cCol - i);
        }
        return sum;
    }


    private int opponentOf(int player) {
        return player * (-1);
    }

    private HashMap<Integer, HashMap<Integer, Double>> makeEmptyMovesList() {
        HashMap<Integer, HashMap<Integer, Double>> emptyMoves = new HashMap<>();
        //System.out.println("ohhoo");
        for (int cRow = 0; cRow <= board.getHeight() - 1; cRow++) {
            HashMap<Integer, Double> rowListElementValues = new HashMap<>();
            for (int cCol = 0; cCol <= board.getHeight() - 1; cCol++) {
                if (board.isEmpty(cRow, cCol)) {
                    rowListElementValues.put(cCol, Double.MIN_VALUE);
                }
            }
            emptyMoves.put(cRow, rowListElementValues);
        }
        return emptyMoves;
    }

    private HashMap<Integer, HashMap<Integer, Double>> emptyMovesAroundPlayerButtons() {

        HashMap<Integer, HashMap<Integer, Double>> emptyMovesAround = new HashMap<>();

        for (int cRow = 0; cRow <= board.getHeight() - 1; cRow++) { //keskmine osa 1-8, 1-8
            HashMap<Integer, Double> rowList = new HashMap<>();
            for (int cCol = 0; cCol <= board.getHeight() - 1; cCol++) {
                if (!board.isEmpty(cRow, cCol)) {//kui on nupp seal, võtab järgmise
                } else if (cRow == 0 && cCol == 0) {  //vasak ülemine nurk
                    if (!board.isEmpty(cRow, cCol + 1) || !board.isEmpty(cRow + 1, cCol + 1)
                            || !board.isEmpty(cRow + 1, cCol)) {  //paremal nupp
                        rowList.put(cCol, Double.MIN_VALUE);
                    }
                } else if (cRow == 0 && cCol == board.getHeight() - 1) {  //parem ülemine nurk
                    if (!board.isEmpty(cRow, cCol - 1) || !board.isEmpty(cRow + 1, cCol - 1)
                            || !board.isEmpty(cRow + 1, cCol) ) {
                        rowList.put(cCol, Double.MIN_VALUE);
                    }
                } else if (cRow == board.getHeight() - 1 && cCol == 0) { //vasak alumine nurk
                    if (!board.isEmpty(cRow - 1, cCol) || !board.isEmpty(cRow, cCol + 1)
                            || !board.isEmpty(cRow - 1, cCol + 1) ) {
                        rowList.put(cCol, Double.MIN_VALUE);
                    }
                } else if (cRow == board.getHeight()- 1 && cCol == board.getHeight() - 1) { //parem alumine nurk
                    if (!board.isEmpty(cRow, cCol - 1) || !board.isEmpty(cRow - 1, cCol - 1)
                            || !board.isEmpty(cRow - 1, cCol) ) {  //vasakul nupp
                        rowList.put(cCol, Double.MIN_VALUE);
                    }
                } else if (cRow == 0) {  //ülemine rida
                    if (!board.isEmpty(cRow, cCol + 1) || !board.isEmpty(cRow, cCol - 1)
                            || !board.isEmpty(cRow + 1, cCol) || !board.isEmpty(cRow + 1, cCol + 1)
                            ||!board.isEmpty(cRow + 1, cCol - 1)) {
                        rowList.put(cCol, Double.MIN_VALUE);
                    }
                } else if (cCol == 0) {  //vasak esimene veerg
                    if (!board.isEmpty(cRow, cCol + 1) || !board.isEmpty(cRow - 1, cCol)
                            || !board.isEmpty(cRow - 1, cCol + 1)
                            || !board.isEmpty(cRow + 1, cCol)
                            || !board.isEmpty(cRow + 1, cCol + 1)) {
                        rowList.put(cCol, Double.MIN_VALUE);
                    }
                } else if (cRow == board.getHeight() - 1) { //alumine rida
                    if (!board.isEmpty(cRow, cCol + 1) || !board.isEmpty(cRow, cCol - 1)
                            || !board.isEmpty(cRow - 1, cCol) || !board.isEmpty(cRow - 1, cCol + 1)
                            || !board.isEmpty(cRow - 1, cCol - 1)) {//vasakul üleval
                        rowList.put(cCol, Double.MIN_VALUE);
                    }
                } else if (cCol == board.getHeight() - 1) { //viimane veerg
                    if (!board.isEmpty(cRow, cCol - 1) || !board.isEmpty(cRow - 1, cCol)
                            || !board.isEmpty(cRow + 1, cCol)
                            || !board.isEmpty(cRow - 1, cCol - 1)
                            || !board.isEmpty(cRow + 1, cCol - 1)) { //vasakul all
                        rowList.put(cCol, Double.MIN_VALUE);
                    }
                } else {  //suvaline punkt laua keskel
                    if (!board.isEmpty(cRow, cCol + 1) || !board.isEmpty(cRow, cCol - 1)
                            || !board.isEmpty(cRow - 1, cCol) || !board.isEmpty(cRow - 1, cCol + 1)
                            || !board.isEmpty(cRow + 1, cCol) || !board.isEmpty(cRow + 1, cCol + 1)
                            || !board.isEmpty(cRow - 1, cCol - 1)
                            || !board.isEmpty(cRow + 1, cCol - 1)) {
                        rowList.put(cCol, Double.MIN_VALUE);
                    }
                }
            }
            if (rowList.size() != 0) {
                emptyMovesAround.put(cRow, rowList);
            }
        }
        return emptyMovesAround;
    }


    private Double getCurrentBaseValueFor(int row, int column) {
        return currentBaseValue.get(Integer.toString(row) + Integer.toString(column));
    }

    private void startStopwatch(){

    }


    private void createTenByTenBaseTable() {
        tenByTenBaseValue.put(Integer.toString(0) + Integer.toString(0), 1.0);
        tenByTenBaseValue.put(Integer.toString(0) + Integer.toString(1), 2.0);
        tenByTenBaseValue.put(Integer.toString(0) + Integer.toString(2), 3.0);
        tenByTenBaseValue.put(Integer.toString(0) + Integer.toString(3), 4.0);
        tenByTenBaseValue.put(Integer.toString(0) + Integer.toString(4), 5.0);
        tenByTenBaseValue.put(Integer.toString(0) + Integer.toString(5), 5.0);
        tenByTenBaseValue.put(Integer.toString(0) + Integer.toString(6), 4.0);
        tenByTenBaseValue.put(Integer.toString(0) + Integer.toString(7), 3.0);
        tenByTenBaseValue.put(Integer.toString(0) + Integer.toString(8), 2.0);
        tenByTenBaseValue.put(Integer.toString(0) + Integer.toString(9), 1.0);

        tenByTenBaseValue.put(Integer.toString(1) + Integer.toString(0), 2.0);
        tenByTenBaseValue.put(Integer.toString(1) + Integer.toString(1), 3.0);
        tenByTenBaseValue.put(Integer.toString(1) + Integer.toString(2), 4.0);
        tenByTenBaseValue.put(Integer.toString(1) + Integer.toString(3), 5.0);
        tenByTenBaseValue.put(Integer.toString(1) + Integer.toString(4), 6.0);
        tenByTenBaseValue.put(Integer.toString(1) + Integer.toString(5), 6.0);
        tenByTenBaseValue.put(Integer.toString(1) + Integer.toString(6), 5.0);
        tenByTenBaseValue.put(Integer.toString(1) + Integer.toString(7), 4.0);
        tenByTenBaseValue.put(Integer.toString(1) + Integer.toString(8), 3.0);
        tenByTenBaseValue.put(Integer.toString(1) + Integer.toString(9), 2.0);

        tenByTenBaseValue.put(Integer.toString(2) + Integer.toString(0), 3.0);
        tenByTenBaseValue.put(Integer.toString(2) + Integer.toString(1), 4.0);
        tenByTenBaseValue.put(Integer.toString(2) + Integer.toString(2), 6.0);
        tenByTenBaseValue.put(Integer.toString(2) + Integer.toString(3), 7.0);
        tenByTenBaseValue.put(Integer.toString(2) + Integer.toString(4), 7.0);
        tenByTenBaseValue.put(Integer.toString(2) + Integer.toString(5), 7.0);
        tenByTenBaseValue.put(Integer.toString(2) + Integer.toString(6), 7.0);
        tenByTenBaseValue.put(Integer.toString(2) + Integer.toString(7), 6.0);
        tenByTenBaseValue.put(Integer.toString(2) + Integer.toString(8), 4.0);
        tenByTenBaseValue.put(Integer.toString(2) + Integer.toString(9), 3.0);

        tenByTenBaseValue.put(Integer.toString(3) + Integer.toString(0), 4.0);
        tenByTenBaseValue.put(Integer.toString(3) + Integer.toString(1), 5.0);
        tenByTenBaseValue.put(Integer.toString(3) + Integer.toString(2), 7.0);
        tenByTenBaseValue.put(Integer.toString(3) + Integer.toString(3), 8.0);
        tenByTenBaseValue.put(Integer.toString(3) + Integer.toString(4), 9.0);
        tenByTenBaseValue.put(Integer.toString(3) + Integer.toString(5), 9.0);
        tenByTenBaseValue.put(Integer.toString(3) + Integer.toString(6), 8.0);
        tenByTenBaseValue.put(Integer.toString(3) + Integer.toString(7), 7.0);
        tenByTenBaseValue.put(Integer.toString(3) + Integer.toString(8), 5.0);
        tenByTenBaseValue.put(Integer.toString(3) + Integer.toString(9), 4.0);

        tenByTenBaseValue.put(Integer.toString(4) + Integer.toString(0), 5.0);
        tenByTenBaseValue.put(Integer.toString(4) + Integer.toString(1), 6.0);
        tenByTenBaseValue.put(Integer.toString(4) + Integer.toString(2), 7.0);
        tenByTenBaseValue.put(Integer.toString(4) + Integer.toString(3), 9.0);
        tenByTenBaseValue.put(Integer.toString(4) + Integer.toString(4), 10.0);
        tenByTenBaseValue.put(Integer.toString(4) + Integer.toString(5), 10.0);
        tenByTenBaseValue.put(Integer.toString(4) + Integer.toString(6), 9.0);
        tenByTenBaseValue.put(Integer.toString(4) + Integer.toString(7), 7.0);
        tenByTenBaseValue.put(Integer.toString(4) + Integer.toString(8), 6.0);
        tenByTenBaseValue.put(Integer.toString(4) + Integer.toString(9), 5.0);

        tenByTenBaseValue.put(Integer.toString(5) + Integer.toString(0), 5.0);
        tenByTenBaseValue.put(Integer.toString(5) + Integer.toString(1), 6.0);
        tenByTenBaseValue.put(Integer.toString(5) + Integer.toString(2), 7.0);
        tenByTenBaseValue.put(Integer.toString(5) + Integer.toString(3), 9.0);
        tenByTenBaseValue.put(Integer.toString(5) + Integer.toString(4), 10.0);
        tenByTenBaseValue.put(Integer.toString(5) + Integer.toString(5), 10.0);
        tenByTenBaseValue.put(Integer.toString(5) + Integer.toString(6), 9.0);
        tenByTenBaseValue.put(Integer.toString(5) + Integer.toString(7), 7.0);
        tenByTenBaseValue.put(Integer.toString(5) + Integer.toString(8), 6.0);
        tenByTenBaseValue.put(Integer.toString(5) + Integer.toString(9), 5.0);

        tenByTenBaseValue.put(Integer.toString(6) + Integer.toString(0), 4.0);
        tenByTenBaseValue.put(Integer.toString(6) + Integer.toString(1), 5.0);
        tenByTenBaseValue.put(Integer.toString(6) + Integer.toString(2), 7.0);
        tenByTenBaseValue.put(Integer.toString(6) + Integer.toString(3), 8.0);
        tenByTenBaseValue.put(Integer.toString(6) + Integer.toString(4), 9.0);
        tenByTenBaseValue.put(Integer.toString(6) + Integer.toString(5), 9.0);
        tenByTenBaseValue.put(Integer.toString(6) + Integer.toString(6), 8.0);
        tenByTenBaseValue.put(Integer.toString(6) + Integer.toString(7), 7.0);
        tenByTenBaseValue.put(Integer.toString(6) + Integer.toString(8), 5.0);
        tenByTenBaseValue.put(Integer.toString(6) + Integer.toString(9), 4.0);

        tenByTenBaseValue.put(Integer.toString(7) + Integer.toString(0), 3.0);
        tenByTenBaseValue.put(Integer.toString(7) + Integer.toString(1), 4.0);
        tenByTenBaseValue.put(Integer.toString(7) + Integer.toString(2), 6.0);
        tenByTenBaseValue.put(Integer.toString(7) + Integer.toString(3), 7.0);
        tenByTenBaseValue.put(Integer.toString(7) + Integer.toString(4), 7.0);
        tenByTenBaseValue.put(Integer.toString(7) + Integer.toString(5), 7.0);
        tenByTenBaseValue.put(Integer.toString(7) + Integer.toString(6), 7.0);
        tenByTenBaseValue.put(Integer.toString(7) + Integer.toString(7), 6.0);
        tenByTenBaseValue.put(Integer.toString(7) + Integer.toString(8), 4.0);
        tenByTenBaseValue.put(Integer.toString(7) + Integer.toString(9), 3.0);

        tenByTenBaseValue.put(Integer.toString(8) + Integer.toString(0), 2.0);
        tenByTenBaseValue.put(Integer.toString(8) + Integer.toString(1), 3.0);
        tenByTenBaseValue.put(Integer.toString(8) + Integer.toString(2), 4.0);
        tenByTenBaseValue.put(Integer.toString(8) + Integer.toString(3), 5.0);
        tenByTenBaseValue.put(Integer.toString(8) + Integer.toString(4), 6.0);
        tenByTenBaseValue.put(Integer.toString(8) + Integer.toString(5), 6.0);
        tenByTenBaseValue.put(Integer.toString(8) + Integer.toString(6), 5.0);
        tenByTenBaseValue.put(Integer.toString(8) + Integer.toString(7), 4.0);
        tenByTenBaseValue.put(Integer.toString(8) + Integer.toString(8), 3.0);
        tenByTenBaseValue.put(Integer.toString(8) + Integer.toString(9), 2.0);

        tenByTenBaseValue.put(Integer.toString(9) + Integer.toString(0), 1.0);
        tenByTenBaseValue.put(Integer.toString(9) + Integer.toString(1), 2.0);
        tenByTenBaseValue.put(Integer.toString(9) + Integer.toString(2), 3.0);
        tenByTenBaseValue.put(Integer.toString(9) + Integer.toString(3), 4.0);
        tenByTenBaseValue.put(Integer.toString(9) + Integer.toString(4), 5.0);
        tenByTenBaseValue.put(Integer.toString(9) + Integer.toString(5), 5.0);
        tenByTenBaseValue.put(Integer.toString(9) + Integer.toString(6), 4.0);
        tenByTenBaseValue.put(Integer.toString(9) + Integer.toString(7), 3.0);
        tenByTenBaseValue.put(Integer.toString(9) + Integer.toString(8), 2.0);
        tenByTenBaseValue.put(Integer.toString(9) + Integer.toString(9), 1.0);
    }

    private void createTwenyByTwentyBaseTable() {
        twentyByTwentyBaseValue.put(Integer.toString(0) + Integer.toString(0), 1.0);
        twentyByTwentyBaseValue.put(Integer.toString(0) + Integer.toString(1), 1.0);
        twentyByTwentyBaseValue.put(Integer.toString(0) + Integer.toString(2), 2.0);
        twentyByTwentyBaseValue.put(Integer.toString(0) + Integer.toString(3), 3.0);
        twentyByTwentyBaseValue.put(Integer.toString(0) + Integer.toString(4), 3.0);
        twentyByTwentyBaseValue.put(Integer.toString(0) + Integer.toString(5), 4.0);
        twentyByTwentyBaseValue.put(Integer.toString(0) + Integer.toString(6), 4.0);
        twentyByTwentyBaseValue.put(Integer.toString(0) + Integer.toString(7), 4.0);
        twentyByTwentyBaseValue.put(Integer.toString(0) + Integer.toString(8), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(0) + Integer.toString(9), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(0) + Integer.toString(10), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(0) + Integer.toString(11), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(0) + Integer.toString(12), 4.0);
        twentyByTwentyBaseValue.put(Integer.toString(0) + Integer.toString(13), 4.0);
        twentyByTwentyBaseValue.put(Integer.toString(0) + Integer.toString(14), 4.0);
        twentyByTwentyBaseValue.put(Integer.toString(0) + Integer.toString(15), 3.0);
        twentyByTwentyBaseValue.put(Integer.toString(0) + Integer.toString(16), 3.0);
        twentyByTwentyBaseValue.put(Integer.toString(0) + Integer.toString(17), 2.0);
        twentyByTwentyBaseValue.put(Integer.toString(0) + Integer.toString(18), 1.0);
        twentyByTwentyBaseValue.put(Integer.toString(0) + Integer.toString(19), 1.0);

        twentyByTwentyBaseValue.put(Integer.toString(1) + Integer.toString(0), 1.0);
        twentyByTwentyBaseValue.put(Integer.toString(1) + Integer.toString(1), 2.0);
        twentyByTwentyBaseValue.put(Integer.toString(1) + Integer.toString(2), 3.0);
        twentyByTwentyBaseValue.put(Integer.toString(1) + Integer.toString(3), 4.0);
        twentyByTwentyBaseValue.put(Integer.toString(1) + Integer.toString(4), 4.0);
        twentyByTwentyBaseValue.put(Integer.toString(1) + Integer.toString(5), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(1) + Integer.toString(6), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(1) + Integer.toString(7), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(1) + Integer.toString(8), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(1) + Integer.toString(9), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(1) + Integer.toString(10), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(1) + Integer.toString(11), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(1) + Integer.toString(12), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(1) + Integer.toString(13), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(1) + Integer.toString(14), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(1) + Integer.toString(15), 4.0);
        twentyByTwentyBaseValue.put(Integer.toString(1) + Integer.toString(16), 4.0);
        twentyByTwentyBaseValue.put(Integer.toString(1) + Integer.toString(17), 3.0);
        twentyByTwentyBaseValue.put(Integer.toString(1) + Integer.toString(18), 2.0);
        twentyByTwentyBaseValue.put(Integer.toString(1) + Integer.toString(19), 1.0);

        twentyByTwentyBaseValue.put(Integer.toString(2) + Integer.toString(0), 2.0);
        twentyByTwentyBaseValue.put(Integer.toString(2) + Integer.toString(1), 3.0);
        twentyByTwentyBaseValue.put(Integer.toString(2) + Integer.toString(2), 4.0);
        twentyByTwentyBaseValue.put(Integer.toString(2) + Integer.toString(3), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(2) + Integer.toString(4), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(2) + Integer.toString(5), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(2) + Integer.toString(6), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(2) + Integer.toString(7), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(2) + Integer.toString(8), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(2) + Integer.toString(9), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(2) + Integer.toString(10), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(2) + Integer.toString(11), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(2) + Integer.toString(12), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(2) + Integer.toString(13), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(2) + Integer.toString(14), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(2) + Integer.toString(15), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(2) + Integer.toString(16), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(2) + Integer.toString(17), 4.0);
        twentyByTwentyBaseValue.put(Integer.toString(2) + Integer.toString(18), 3.0);
        twentyByTwentyBaseValue.put(Integer.toString(2) + Integer.toString(19), 2.0);

        twentyByTwentyBaseValue.put(Integer.toString(3) + Integer.toString(0), 3.0);
        twentyByTwentyBaseValue.put(Integer.toString(3) + Integer.toString(1), 4.0);
        twentyByTwentyBaseValue.put(Integer.toString(3) + Integer.toString(2), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(3) + Integer.toString(3), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(3) + Integer.toString(4), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(3) + Integer.toString(5), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(3) + Integer.toString(6), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(3) + Integer.toString(7), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(3) + Integer.toString(8), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(3) + Integer.toString(9), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(3) + Integer.toString(10), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(3) + Integer.toString(11), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(3) + Integer.toString(12), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(3) + Integer.toString(13), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(3) + Integer.toString(14), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(3) + Integer.toString(15), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(3) + Integer.toString(16), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(3) + Integer.toString(17), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(3) + Integer.toString(18), 4.0);
        twentyByTwentyBaseValue.put(Integer.toString(3) + Integer.toString(19), 3.0);

        twentyByTwentyBaseValue.put(Integer.toString(4) + Integer.toString(0), 3.0);
        twentyByTwentyBaseValue.put(Integer.toString(4) + Integer.toString(1), 4.0);
        twentyByTwentyBaseValue.put(Integer.toString(4) + Integer.toString(2), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(4) + Integer.toString(3), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(4) + Integer.toString(4), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(4) + Integer.toString(5), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(4) + Integer.toString(6), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(4) + Integer.toString(7), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(4) + Integer.toString(8), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(4) + Integer.toString(9), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(4) + Integer.toString(10), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(4) + Integer.toString(11), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(4) + Integer.toString(12), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(4) + Integer.toString(13), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(4) + Integer.toString(14), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(4) + Integer.toString(15), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(4) + Integer.toString(16), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(4) + Integer.toString(17), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(4) + Integer.toString(18), 4.0);
        twentyByTwentyBaseValue.put(Integer.toString(4) + Integer.toString(19), 3.0);

        twentyByTwentyBaseValue.put(Integer.toString(5) + Integer.toString(0), 4.0);
        twentyByTwentyBaseValue.put(Integer.toString(5) + Integer.toString(1), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(5) + Integer.toString(2), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(5) + Integer.toString(3), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(5) + Integer.toString(4), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(5) + Integer.toString(5), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(5) + Integer.toString(6), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(5) + Integer.toString(7), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(5) + Integer.toString(8), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(5) + Integer.toString(9), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(5) + Integer.toString(10), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(5) + Integer.toString(11), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(5) + Integer.toString(12), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(5) + Integer.toString(13), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(5) + Integer.toString(14), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(5) + Integer.toString(15), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(5) + Integer.toString(16), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(5) + Integer.toString(17), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(5) + Integer.toString(18), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(5) + Integer.toString(19), 4.0);

        twentyByTwentyBaseValue.put(Integer.toString(6) + Integer.toString(0), 4.0);
        twentyByTwentyBaseValue.put(Integer.toString(6) + Integer.toString(1), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(6) + Integer.toString(2), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(6) + Integer.toString(3), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(6) + Integer.toString(4), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(6) + Integer.toString(5), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(6) + Integer.toString(6), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(6) + Integer.toString(7), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(6) + Integer.toString(8), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(6) + Integer.toString(9), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(6) + Integer.toString(10), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(6) + Integer.toString(11), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(6) + Integer.toString(12), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(6) + Integer.toString(13), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(6) + Integer.toString(14), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(6) + Integer.toString(15), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(6) + Integer.toString(16), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(6) + Integer.toString(17), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(6) + Integer.toString(18), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(6) + Integer.toString(19), 4.0);

        twentyByTwentyBaseValue.put(Integer.toString(7) + Integer.toString(0), 4.0);
        twentyByTwentyBaseValue.put(Integer.toString(7) + Integer.toString(1), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(7) + Integer.toString(2), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(7) + Integer.toString(3), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(7) + Integer.toString(4), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(7) + Integer.toString(5), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(7) + Integer.toString(6), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(7) + Integer.toString(7), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(7) + Integer.toString(8), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(7) + Integer.toString(9), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(7) + Integer.toString(10), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(7) + Integer.toString(11), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(7) + Integer.toString(12), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(7) + Integer.toString(13), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(7) + Integer.toString(14), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(7) + Integer.toString(15), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(7) + Integer.toString(16), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(7) + Integer.toString(17), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(7) + Integer.toString(18), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(7) + Integer.toString(19), 4.0);

        twentyByTwentyBaseValue.put(Integer.toString(8) + Integer.toString(0), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(8) + Integer.toString(1), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(8) + Integer.toString(2), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(8) + Integer.toString(3), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(8) + Integer.toString(4), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(8) + Integer.toString(5), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(8) + Integer.toString(6), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(8) + Integer.toString(7), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(8) + Integer.toString(8), 10.0);
        twentyByTwentyBaseValue.put(Integer.toString(8) + Integer.toString(9), 10.0);
        twentyByTwentyBaseValue.put(Integer.toString(8) + Integer.toString(10), 10.0);
        twentyByTwentyBaseValue.put(Integer.toString(8) + Integer.toString(11), 10.0);
        twentyByTwentyBaseValue.put(Integer.toString(8) + Integer.toString(12), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(8) + Integer.toString(13), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(8) + Integer.toString(14), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(8) + Integer.toString(15), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(8) + Integer.toString(16), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(8) + Integer.toString(17), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(8) + Integer.toString(18), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(8) + Integer.toString(19), 5.0);

        twentyByTwentyBaseValue.put(Integer.toString(9) + Integer.toString(0), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(9) + Integer.toString(1), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(9) + Integer.toString(2), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(9) + Integer.toString(3), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(9) + Integer.toString(4), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(9) + Integer.toString(5), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(9) + Integer.toString(6), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(9) + Integer.toString(7), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(9) + Integer.toString(8), 10.0);
        twentyByTwentyBaseValue.put(Integer.toString(9) + Integer.toString(9), 10.0);
        twentyByTwentyBaseValue.put(Integer.toString(9) + Integer.toString(10), 10.0);
        twentyByTwentyBaseValue.put(Integer.toString(9) + Integer.toString(11), 10.0);
        twentyByTwentyBaseValue.put(Integer.toString(9) + Integer.toString(12), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(9) + Integer.toString(13), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(9) + Integer.toString(14), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(9) + Integer.toString(15), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(9) + Integer.toString(16), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(9) + Integer.toString(17), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(9) + Integer.toString(18), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(9) + Integer.toString(19), 5.0);

        twentyByTwentyBaseValue.put(Integer.toString(10) + Integer.toString(0), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(10) + Integer.toString(1), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(10) + Integer.toString(2), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(10) + Integer.toString(3), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(10) + Integer.toString(4), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(10) + Integer.toString(5), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(10) + Integer.toString(6), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(10) + Integer.toString(7), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(10) + Integer.toString(8), 10.0);
        twentyByTwentyBaseValue.put(Integer.toString(10) + Integer.toString(9), 10.0);
        twentyByTwentyBaseValue.put(Integer.toString(10) + Integer.toString(10), 10.0);
        twentyByTwentyBaseValue.put(Integer.toString(10) + Integer.toString(11), 10.0);
        twentyByTwentyBaseValue.put(Integer.toString(10) + Integer.toString(12), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(10) + Integer.toString(13), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(10) + Integer.toString(14), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(10) + Integer.toString(15), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(10) + Integer.toString(16), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(10) + Integer.toString(17), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(10) + Integer.toString(18), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(10) + Integer.toString(19), 5.0);

        twentyByTwentyBaseValue.put(Integer.toString(11) + Integer.toString(0), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(11) + Integer.toString(1), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(11) + Integer.toString(2), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(11) + Integer.toString(3), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(11) + Integer.toString(4), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(11) + Integer.toString(5), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(11) + Integer.toString(6), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(11) + Integer.toString(7), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(11) + Integer.toString(8), 10.0);
        twentyByTwentyBaseValue.put(Integer.toString(11) + Integer.toString(9), 10.0);
        twentyByTwentyBaseValue.put(Integer.toString(11) + Integer.toString(10), 10.0);
        twentyByTwentyBaseValue.put(Integer.toString(11) + Integer.toString(11), 10.0);
        twentyByTwentyBaseValue.put(Integer.toString(11) + Integer.toString(12), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(11) + Integer.toString(13), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(11) + Integer.toString(14), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(11) + Integer.toString(15), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(11) + Integer.toString(16), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(11) + Integer.toString(17), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(11) + Integer.toString(18), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(11) + Integer.toString(19), 5.0);


        twentyByTwentyBaseValue.put(Integer.toString(12) + Integer.toString(0), 4.0);
        twentyByTwentyBaseValue.put(Integer.toString(12) + Integer.toString(1), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(12) + Integer.toString(2), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(12) + Integer.toString(3), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(12) + Integer.toString(4), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(12) + Integer.toString(5), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(12) + Integer.toString(6), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(12) + Integer.toString(7), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(12) + Integer.toString(8), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(12) + Integer.toString(9), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(12) + Integer.toString(10), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(12) + Integer.toString(11), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(12) + Integer.toString(12), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(12) + Integer.toString(13), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(12) + Integer.toString(14), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(12) + Integer.toString(15), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(12) + Integer.toString(16), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(12) + Integer.toString(17), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(12) + Integer.toString(18), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(12) + Integer.toString(19), 4.0);

        twentyByTwentyBaseValue.put(Integer.toString(13) + Integer.toString(0), 4.0);
        twentyByTwentyBaseValue.put(Integer.toString(13) + Integer.toString(1), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(13) + Integer.toString(2), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(13) + Integer.toString(3), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(13) + Integer.toString(4), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(13) + Integer.toString(5), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(13) + Integer.toString(6), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(13) + Integer.toString(7), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(13) + Integer.toString(8), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(13) + Integer.toString(9), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(13) + Integer.toString(10), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(13) + Integer.toString(11), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(13) + Integer.toString(12), 9.0);
        twentyByTwentyBaseValue.put(Integer.toString(13) + Integer.toString(13), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(13) + Integer.toString(14), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(13) + Integer.toString(15), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(13) + Integer.toString(16), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(13) + Integer.toString(17), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(13) + Integer.toString(18), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(13) + Integer.toString(19), 4.0);

        twentyByTwentyBaseValue.put(Integer.toString(14) + Integer.toString(0), 4.0);
        twentyByTwentyBaseValue.put(Integer.toString(14) + Integer.toString(1), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(14) + Integer.toString(2), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(14) + Integer.toString(3), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(14) + Integer.toString(4), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(14) + Integer.toString(5), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(14) + Integer.toString(6), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(14) + Integer.toString(7), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(14) + Integer.toString(8), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(14) + Integer.toString(9), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(14) + Integer.toString(10), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(14) + Integer.toString(11), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(14) + Integer.toString(12), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(14) + Integer.toString(13), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(14) + Integer.toString(14), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(14) + Integer.toString(15), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(14) + Integer.toString(16), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(14) + Integer.toString(17), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(14) + Integer.toString(18), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(14) + Integer.toString(19), 4.0);

        twentyByTwentyBaseValue.put(Integer.toString(15) + Integer.toString(0), 3.0);
        twentyByTwentyBaseValue.put(Integer.toString(15) + Integer.toString(1), 4.0);
        twentyByTwentyBaseValue.put(Integer.toString(15) + Integer.toString(2), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(15) + Integer.toString(3), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(15) + Integer.toString(4), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(15) + Integer.toString(5), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(15) + Integer.toString(6), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(15) + Integer.toString(7), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(15) + Integer.toString(8), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(15) + Integer.toString(9), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(15) + Integer.toString(10), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(15) + Integer.toString(11), 8.0);
        twentyByTwentyBaseValue.put(Integer.toString(15) + Integer.toString(12), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(15) + Integer.toString(13), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(15) + Integer.toString(14), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(15) + Integer.toString(15), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(15) + Integer.toString(16), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(15) + Integer.toString(17), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(15) + Integer.toString(18), 4.0);
        twentyByTwentyBaseValue.put(Integer.toString(15) + Integer.toString(19), 3.0);

        twentyByTwentyBaseValue.put(Integer.toString(16) + Integer.toString(0), 3.0);
        twentyByTwentyBaseValue.put(Integer.toString(16) + Integer.toString(1), 4.0);
        twentyByTwentyBaseValue.put(Integer.toString(16) + Integer.toString(2), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(16) + Integer.toString(3), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(16) + Integer.toString(4), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(16) + Integer.toString(5), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(16) + Integer.toString(6), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(16) + Integer.toString(7), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(16) + Integer.toString(8), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(16) + Integer.toString(9), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(16) + Integer.toString(10), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(16) + Integer.toString(11), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(16) + Integer.toString(12), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(16) + Integer.toString(13), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(16) + Integer.toString(14), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(16) + Integer.toString(15), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(16) + Integer.toString(16), 7.0);
        twentyByTwentyBaseValue.put(Integer.toString(16) + Integer.toString(17), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(16) + Integer.toString(18), 4.0);
        twentyByTwentyBaseValue.put(Integer.toString(16) + Integer.toString(19), 3.0);

        twentyByTwentyBaseValue.put(Integer.toString(17) + Integer.toString(0), 2.0);
        twentyByTwentyBaseValue.put(Integer.toString(17) + Integer.toString(1), 3.0);
        twentyByTwentyBaseValue.put(Integer.toString(17) + Integer.toString(2), 4.0);
        twentyByTwentyBaseValue.put(Integer.toString(17) + Integer.toString(3), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(17) + Integer.toString(4), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(17) + Integer.toString(5), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(17) + Integer.toString(6), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(17) + Integer.toString(7), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(17) + Integer.toString(8), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(17) + Integer.toString(9), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(17) + Integer.toString(10), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(17) + Integer.toString(11), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(17) + Integer.toString(12), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(17) + Integer.toString(13), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(17) + Integer.toString(14), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(17) + Integer.toString(15), 6.0);
        twentyByTwentyBaseValue.put(Integer.toString(17) + Integer.toString(16), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(17) + Integer.toString(17), 4.0);
        twentyByTwentyBaseValue.put(Integer.toString(17) + Integer.toString(18), 3.0);
        twentyByTwentyBaseValue.put(Integer.toString(17) + Integer.toString(19), 2.0);

        twentyByTwentyBaseValue.put(Integer.toString(18) + Integer.toString(0), 1.0);
        twentyByTwentyBaseValue.put(Integer.toString(18) + Integer.toString(1), 2.0);
        twentyByTwentyBaseValue.put(Integer.toString(18) + Integer.toString(2), 3.0);
        twentyByTwentyBaseValue.put(Integer.toString(18) + Integer.toString(3), 4.0);
        twentyByTwentyBaseValue.put(Integer.toString(18) + Integer.toString(4), 4.0);
        twentyByTwentyBaseValue.put(Integer.toString(18) + Integer.toString(5), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(18) + Integer.toString(6), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(18) + Integer.toString(7), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(18) + Integer.toString(8), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(18) + Integer.toString(9), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(18) + Integer.toString(10), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(18) + Integer.toString(11), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(18) + Integer.toString(12), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(18) + Integer.toString(13), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(18) + Integer.toString(14), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(18) + Integer.toString(15), 4.0);
        twentyByTwentyBaseValue.put(Integer.toString(18) + Integer.toString(16), 4.0);
        twentyByTwentyBaseValue.put(Integer.toString(18) + Integer.toString(17), 3.0);
        twentyByTwentyBaseValue.put(Integer.toString(18) + Integer.toString(18), 2.0);
        twentyByTwentyBaseValue.put(Integer.toString(18) + Integer.toString(19), 1.0);

        twentyByTwentyBaseValue.put(Integer.toString(19) + Integer.toString(0), 1.0);
        twentyByTwentyBaseValue.put(Integer.toString(19) + Integer.toString(1), 1.0);
        twentyByTwentyBaseValue.put(Integer.toString(19) + Integer.toString(2), 2.0);
        twentyByTwentyBaseValue.put(Integer.toString(19) + Integer.toString(3), 3.0);
        twentyByTwentyBaseValue.put(Integer.toString(19) + Integer.toString(4), 3.0);
        twentyByTwentyBaseValue.put(Integer.toString(19) + Integer.toString(5), 4.0);
        twentyByTwentyBaseValue.put(Integer.toString(19) + Integer.toString(6), 4.0);
        twentyByTwentyBaseValue.put(Integer.toString(19) + Integer.toString(7), 4.0);
        twentyByTwentyBaseValue.put(Integer.toString(19) + Integer.toString(8), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(19) + Integer.toString(9), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(19) + Integer.toString(10), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(19) + Integer.toString(11), 5.0);
        twentyByTwentyBaseValue.put(Integer.toString(19) + Integer.toString(12), 4.0);
        twentyByTwentyBaseValue.put(Integer.toString(19) + Integer.toString(13), 4.0);
        twentyByTwentyBaseValue.put(Integer.toString(19) + Integer.toString(14), 4.0);
        twentyByTwentyBaseValue.put(Integer.toString(19) + Integer.toString(15), 3.0);
        twentyByTwentyBaseValue.put(Integer.toString(19) + Integer.toString(16), 3.0);
        twentyByTwentyBaseValue.put(Integer.toString(19) + Integer.toString(17), 2.0);
        twentyByTwentyBaseValue.put(Integer.toString(19) + Integer.toString(18), 1.0);
        twentyByTwentyBaseValue.put(Integer.toString(10) + Integer.toString(19), 1.0);
    }

    @Override
    public String getName() {
        return "Anto-Helina";
    }

}

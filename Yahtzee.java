/*
 * File: Yahtzee.java
 * ------------------
 * This program will eventually play the Yahtzee game.
 */

import java.util.LinkedList;

import acm.io.*;
import acm.program.*;
import acm.util.*;

public class Yahtzee extends GraphicsProgram implements YahtzeeConstants {
	
	/** Private instance variables */
	private YahtzeeDisplay display;
	private RandomGenerator rgen;
	private int nPlayers; // Number of players
	private int[] diceValues = new int[N_DICE]; // stores the most recently rolled dice values
	private boolean[][] selectedCategory = new boolean[MAX_PLAYERS][N_CATEGORIES]; // tells which categories has been selected by which players, true if selected
	private int[][] scoreBoard = new int[MAX_PLAYERS][N_CATEGORIES]; // stores score for each player based on categories
	private String[] playerNames; // Stores names of players
	
	public static void main(String[] args) {
		new Yahtzee().start(args);
	}
	
	public void run() {
		IODialog dialog = getDialog();
		
		/* Allow user to re-enter number of players if necessary */
		while (true) {
			nPlayers = dialog.readInt("Enter number of players. This must be less than 4.");
			if (nPlayers >= 1 && nPlayers <= MAX_PLAYERS) break;
		}
		
		/* Asks & stores names of players */
		playerNames = new String[nPlayers];
		for (int j = 0; j < nPlayers; j++) 
			playerNames[j] = dialog.readLine("Enter name for player " + j);
		
		display = new YahtzeeDisplay(getGCanvas(), playerNames);
		
		/* Start game */
		for (int i = 1; i <= N_SCORING_CATEGORIES; i++)
			for (int j = 1; j <= nPlayers; j++) {
				display.printMessage("It's " + playerNames[j - 1] + "\' turn. Click \"Roll Dice\" button to roll the dice." );
				firstRoll(j);
				secondAndThirdRoll(j);
				display.printMessage("Select a category for this roll.");
				int categoryNum = display.waitForPlayerToSelectCategory();
				boolean categoryMatch = checkCategoryMatch(j, categoryNum);
				updateScoreBoard(j, categoryNum, categoryMatch);
				calculateAndDisplayTotalScore(j);
			}
		
		/* Calculate upper score, lower score, and upper bonus for each players */
		for (int j = 0; j < nPlayers; j++) {
			
			calculateAndDisplayUpperScoreAndUpperBonus(j);
			display.printMessage("aaaa");
			calculateAndDisplayLowerScore(j);
		}
		chooseWinnerAndEnd(); // Calculate total score for each player, and choose the winner
	}

	/**
	 * Choose dice values for the first roll
	 * @param player The player who is rolling the dice
	 */
        private void firstRoll(int player) {
        	rgen = new RandomGenerator();
	        display.waitForPlayerToClickRoll(player);
	        for (int i = 0; i < N_DICE; i++) {
	        	int randomNumber = rgen.nextInt(1, 6);
	        	diceValues[i] = randomNumber;
	        }
	        display.displayDice(diceValues);
        }
	
	/**
	 * Update dice values for second and third roll
	 * @param player The player who is rolling the dice
	 */
        private void secondAndThirdRoll(int player) {
        	for (int i = 1; i <= 2; i++) {
        		rgen = new RandomGenerator();
        		display.printMessage("Select the dice you wish to re-roll and click \"Roll Again\"!");
        		display.waitForPlayerToSelectDice();
        		for (int j = 0; j < N_DICE; j++) {
        			if (display.isDieSelected(j)) {
        				int randomNumber = rgen.nextInt(1, 6);
        				diceValues[j] = randomNumber;
        			}
        			display.displayDice(diceValues);
        		}
        	}
        }
        
	/**
	 * Check if the chosen category matches the dice configurations
	 * @param player The player who is player
	 * @param categoryNum The chosen category 
	 * @return boolean True if the chosen category matches the dice configurations
	 */
        private boolean checkCategoryMatch(int player, int categoryNum) {
        	// Chosen category has been selected before
        	if (selectedCategory[player - 1][categoryNum - 1]) 
        		return false;
        	
        	selectedCategory[player - 1][categoryNum - 1] = true;
        	
        	// Categories from ONES to SIXES and CHANCE always match with any dice configurations
        	if (categoryNum >= ONES && categoryNum <= SIXES || categoryNum == CHANCE) 
        		return true;
        		
        	// For other categories
        	LinkedList<Integer> ones = new LinkedList<Integer>(); 
        	LinkedList<Integer> twos = new LinkedList<Integer>(); 
        	LinkedList<Integer> threes = new LinkedList<Integer>(); 
        	LinkedList<Integer> fours = new LinkedList<Integer>(); 
        	LinkedList<Integer> fives = new LinkedList<Integer>(); 
        	LinkedList<Integer> sixes = new LinkedList<Integer>(); 
        	
        	for (int i = 0; i < N_DICE; i++) {
        		switch (diceValues[i]) {
        			case 1:
        				ones.add(1);
        				break;
        			case 2:
        				twos.add(2);
        				break;
        			case 3:
        				threes.add(3);
        				break;
        			case 4:
        				fours.add(4);
        				break;
        			case 5:
        				fives.add(5);
        				break;
        			case 6:
        				sixes.add(6);
        				break;
        		}
        	}
        	
        	if (categoryNum == THREE_OF_A_KIND)
        		if (ones.size() == 3 || twos.size() == 3 || threes.size() == 3 || fours.size() == 3
        				|| fives.size() == 3 || sixes.size() == 3) 
        			return true;
        		
        	
        	if (categoryNum == FOUR_OF_A_KIND)
        		if (ones.size() == 4 || twos.size() == 4 || threes.size() == 4 || fours.size() == 4
        				|| fives.size() == 4 || sixes.size() == 4) 
        			return true;
        		
        	
        	if (categoryNum == FULL_HOUSE)
        		if ( (ones.size() == 3 || twos.size() == 3 || threes.size() == 3 || fours.size() == 3 || fives.size() == 3 || sixes.size() == 3) 
        				&& (ones.size() == 2 || twos.size() == 2 || threes.size() == 2 || fours.size() == 2 || fives.size() == 2 || sixes.size() == 2) ){
        			selectedCategory[player - 1][categoryNum] = true;
        			return true;
        		}
        	
        	if (categoryNum == SMALL_STRAIGHT)
        		if ( (ones.size() >= 1 && twos.size() >= 1 && threes.size() >= 1 && fours.size() >= 1) 
        				|| (twos.size() >= 1 && threes.size() >= 1 && fours.size() >= 1 && fives.size() >= 1) 
        				|| (threes.size() >= 1 && fours.size() >= 1 && fives.size() >= 1 && sixes.size() >= 1) ) 
        			return true;
        		
        	
        	if (categoryNum == LARGE_STRAIGHT)
        		if ( (ones.size() >= 1 && twos.size() >= 1 && threes.size() >= 1 && fours.size() >= 1 && fives.size() >= 1) 
        				|| (twos.size() >= 1 && threes.size() >= 1 && fours.size() >= 1&& fives.size() >= 1 && sixes.size() >= 1) ) 
        			return true;
        		
        	
        	if (categoryNum == YAHTZEE)
        		if (ones.size() == 5 || twos.size() == 5 || threes.size() == 5 || fours.size() == 5
        				|| fives.size() == 5 || sixes.size() == 5) 
        			return true;
     
        	return false;
        }
        
        /**
         * Update score board
	 * @param player The player who is playing
	 * @param categoryNum The number of the chosen category
	 * @param categoryMatch Whether the chosen category matches the dice configurations
	 */
        private void updateScoreBoard(int player, int categoryNum, boolean categoryMatch) {
        	if (!categoryMatch) {
        		display.updateScorecard(categoryNum, player, 0);
        	} else {
        		int score = 0;
        		if (categoryNum >= ONES && categoryNum <= SIXES)
        			for (int i = 0; i < N_DICE; i++) 
        				if (diceValues[i] == categoryNum)
        					score += diceValues[i];
        		
        		if (categoryNum == CHANCE || categoryNum == THREE_OF_A_KIND || categoryNum == FOUR_OF_A_KIND) 
        			for (int i = 0; i < N_DICE; i++) 
        				score += diceValues[i];
        		
        		if (categoryNum == FULL_HOUSE) 
        			score = 25;
        		
        		if (categoryNum == SMALL_STRAIGHT)
        			score = 30;
        		
        		if (categoryNum == LARGE_STRAIGHT)
        			score = 40;
        		
        		if (categoryNum == YAHTZEE)
        			score = 50;
        		
        		scoreBoard[player - 1][categoryNum - 1] = score;
        		display.updateScorecard(categoryNum, player, score);
        	}
        }
        
	/**
	 * Calculate and display total score
	 * @param player The player who is playing
	 */
        private void calculateAndDisplayTotalScore(int player) {
        	int totalScore = 0;
        	for (int i = 0; i < N_CATEGORIES; i++) 
        		totalScore += scoreBoard[player - 1][i];
        	display.updateScorecard(TOTAL, player, totalScore);
        }
        
	/**
	 * Calculate and display upper score & upper bonus 
	 * @param player The player being considered
	 */
        private void calculateAndDisplayUpperScoreAndUpperBonus(int player) {
        	int upperScore = 0;
	        for (int i = 0; i < SIXES; i++)
	        	upperScore += scoreBoard[player][i];
	        display.updateScorecard(UPPER_SCORE, player + 1, upperScore);
	        if (upperScore > 63)
	        	display.updateScorecard(UPPER_BONUS, player + 1, 35);
	        else
	        	display.updateScorecard(UPPER_BONUS, player + 1, 0);
        }
        
	/**
	 * Calculate and display lower score
	 * @param player The player being considered
	 */
        private void calculateAndDisplayLowerScore(int player) {
        	int lowerScore = 0;
	        for (int i = THREE_OF_A_KIND - 1; i < CHANCE; i++)
	        	lowerScore += scoreBoard[player][i];
	        display.updateScorecard(LOWER_SCORE, player + 1, lowerScore);
        }
        
	/**
	 * Calculate the total scores for the last time
	 * determine who the winner is
	 * and print the congratulations message
	 */
        private void chooseWinnerAndEnd() {
        	int finalTotalScore = 0;
        	int largestScore = 0;
        	String nameOfWinner = "";
	        // Calculate the total score for the last time for each player
        	for (int i = 0; i < nPlayers; i++) {
        		for (int j = 0; j < LOWER_SCORE; j++) 
        			finalTotalScore += scoreBoard[i][j];
        		display.updateScorecard(TOTAL, i + 1, finalTotalScore);
        		if (finalTotalScore > largestScore) {
        			largestScore = finalTotalScore;
        			nameOfWinner = playerNames[i];
        		}
        	}
        	display.printMessage("Congratulations, " + nameOfWinner + ", you're the winner with a total score of " + largestScore + " !");
        }
}
		
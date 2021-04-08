/* Programmer: Brandon Cordova
// Program: ConnectFourServer.java
// Purpose: to illustrate the implementation of a server-based
// console web-game based on real life game "Connect Four".
// Objective: store four R's or four B's consecutively without interruption from
// the opponents mark. If four in a row are achieved, there is a winner. Either player 'R'
// or player 'B'.
// Player R represents 'Red chip', and Player B represent 'Blue Chip'. In real world, round chips denoted by a color are used
// to play the game. Hence why we use R and B to denote the color.
// Once there is a winner, the program will prompt the users to exit the server, while the server will continue
// to wait for more clients. There is max 2 clients per game, by game design. If other clients join, they will simply sit in a queue
// until the current game is finished.
*/

import java.util.*;
import java.net.*;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectFourServer{

	public static void main(String [] args)throws IOException{
		ServerSocket server = new ServerSocket(5000); //listen on port(5000)
			try{
				ExecutorService fixedPool = Executors.newFixedThreadPool(2); //max 2 clients in game. if other clients join they will sit in queue and wait for in-game clients to finish.
				while(true){
					Game game = new Game(); //create new game.
					fixedPool.execute(game.new Player(server.accept(), 'R')); //create new PLayer inner class, with value R, representing Red chip
					fixedPool.execute(game.new Player(server.accept(), 'B')); //create new inner class Player, wth value B, representing Blue Chip.
				}
			}catch(Exception e){System.out.println(e);}
		}
	}

class Game{

	private char [][]table = new char[6][7];
	Player currentPlayer;

	//initalize table, assign all squares '-'
	public void initialTable(char[][] table){
	for(int rows = 0; rows < 6;  rows++){
			for(int cols = 0; cols < 7; cols++){
				table[rows][cols] = '-';
			}
		}
	}

//display to all players
public static void display1(char[][] table, PrintWriter output, Player player){

		output.println("");
		player.opponent.output.println("");
		for(int c = 1; c <= 7; c++){
			output.print("  ");
			player.opponent.output.print("  ");
			output.print(c + " ");
			player.opponent.output.print(c + " ");
		}
		output.println("");
		player.opponent.output.println("");
		for(int rows = 0; rows < 6; rows++){
			output.print("| ");
			player.opponent.output.print("| ");
			for(int cols = 0; cols < 7; cols++){
				output.print(table[rows][cols] + " | ");
				player.opponent.output.print(table[rows][cols] + " | ");
			};
			output.println("");
			player.opponent.output.println("");
		}
			output.print("-----------------------------");
			player.opponent.output.println("-----------------------------");

	}

	//display table to server
	public static void display(char[][] table){

		System.out.println("");
		for(int c = 1; c <= 7; c++){
			System.out.print("  ");
			System.out.print(c + " ");
		}
		System.out.println("");
		for(int rows = 0; rows < 6; rows++){
			System.out.print("| ");
			for(int cols = 0; cols < 7; cols++){
				System.out.print(table[rows][cols] + " | ");
			};
			System.out.println("");
		}
			System.out.println("-----------------------------");

	}

	public boolean validate(int column, char[][] table, PrintWriter output, Player player){

		if(player != currentPlayer){
			output.println("Wait your turn...");
			throw new IllegalStateException("Wait your turn...");
		}
		if(player.opponent == null){
			System.out.println("");
			output.println("You don't have an opponent yet...");
			throw new IllegalStateException("No player has joined the game yet.");		
		}

		if(column < 0 || column >= table[0].length){
			System.out.println("Invalid input:\nPlease enter a column between 1 and 7.");
			output.println("Invalid Input: Please enter a column between 1 and 7.");
			return false;
		}

		if(table[0][column] != '-'){
			System.out.println("Invalid Input: column already full.\nPlease choose another column between 1 and 7.");
			output.println("Invalid Input: column already full.\n Please choose another column between 1 and 7.");
			return false;
		}

			return true;
		}

		public static boolean isWinner(char player, char[][] table){
		//check for 4 across
		for(int row = 0; row<table.length; row++){
			for (int col = 0;col < table[0].length-3;col++){
				if (table[row][col] == player   && 
					table[row][col+1] == player &&
					table[row][col+2] == player &&
					table[row][col+3] == player){
					return true;
				}
			}			
		}
		//check for 4 up and down
		for(int row = 0; row < table.length - 3; row++){
			for(int col = 0; col < table[0].length; col++){
				if (table[row][col] == player   && 
					table[row+1][col] == player &&
					table[row+2][col] == player &&
					table[row+3][col] == player){
					return true;
				}
			}
		}
		//check upward diagonal
		for(int row = 3; row < table.length; row++){
			for(int col = 0; col < table[0].length-3; col++){
				if (table[row][col] == player   && 
					table[row-1][col+1] == player &&
					table[row-2][col+2] == player &&
					table[row-3][col+3] == player){
					return true;
				}
			}
		}
		//check downward diagonal
		for(int row = 0; row < table.length-3; row++){
			for(int col = 0; col < table[0].length-3; col++){
				if (table[row][col] == player   && 
					table[row+1][col+1] == player &&
					table[row+2][col+2] == player &&
					table[row+3][col+3] == player){
					return true;
				}
			}
		}
		return false;
	}

	public synchronized void move(int spot,char mark, Player player){
		for(int row = table.length-1; row >=0; row--){
				if(table[row][spot] == '-'){
					table[row][spot] = mark;
					break;
				}
		}

		currentPlayer = currentPlayer.opponent; //switch between player1 and player2
	}

	class Player implements Runnable{
		char mark;
		Player opponent;
		Socket socket;
		Scanner input;
		PrintWriter output;
		boolean validPlay;
		boolean winner = false;

		public Player(Socket socket, char mark){
			this.socket=socket;
			this.mark=mark;

		}

		@Override
		public void run(){
			try{
				initialTable(table); // setup initial table for clients.

				while(true){
					System.out.println("Player " + mark + " has joined game.");
					setup();
					receiveInput();
				}

				}catch(Exception e){System.out.println(e);}
				if(opponent != null && opponent.output != null){
					opponent.output.println("Other player left game...");
					opponent.output.println("Enter 'Exit' to leave game....");
				}
				try{
					this.socket.close();
					this.input.close();
					this.output.close();
				}catch(IOException e){System.out.println(e);}
		}

		private void setup() throws IOException{
			input = new Scanner(socket.getInputStream());
			output = new PrintWriter(socket.getOutputStream(), true);
			output.println("Welcome Player " + mark);
			output.println("You can enter 'Exit' at any time to quit the game.");
			if(mark == 'R'){
				currentPlayer = this;

				output.println("Waiting for another player....");
			}else{
				opponent = currentPlayer;
				opponent.opponent=this;
				opponent.output.println("Your move: Enter a column between 1 and 7 to begin game...");
				output.println("Wait for other player to make their move...");
			}
			if(opponent != null){ //wait for an opponent to display the table and start game.
				display1(table, output, this);
				display(table);
				output.println("");
			}
		}

		private void receiveInput(){
			while(input.hasNextLine()){
				var command = input.nextLine();
				if(command.equals("Exit")){
					clientExit(command);
				}
				else{
				int intCommand = Integer.parseInt(command);
				intCommand = intCommand-1;
				processInput(intCommand, mark);
					}
			}
		}

		private void processInput(int spot, char mark){
			try{

				validPlay = validate(spot,table, output, this); //validate the move by the player. determine if client went too soon, or if input was invalid.
				while(validPlay == false){ // while validPlay doesnt return true, we will keep prompting currentPlayer for input.
					receiveInput();
				}
				output.println("Valid Move");
				move(spot,mark, this); //put players mark in the given column, switch turns between clients.
				display1(table, output, this); //display updated table to both players.
				output.println("");
				display(table); //display table to console.

				// check for a winner!
				winner = (isWinner(mark, table));
				if(winner){
					System.out.println("Player " + mark + " Won");
					output.println("Player " + mark + " won.");
					opponent.output.println("Player " + mark + " won.");
					output.println("Enter 'Exit' to leave game....");
					opponent.output.println("Enter 'Exit' to leave game....");
				}
				else{
				opponent.output.println("Your turn.");
				}
			}catch(Exception e){System.out.println(e);}
		}

		private void clientExit(String checkCommand){
				try{
						System.out.println("Player " + mark + " left game.");
						System.out.println("Goodbye player " + mark);
						this.socket.close();
						this.input.close();
						this.output.close();
					}catch(IOException e){System.out.println(e);}
		}
	}
}

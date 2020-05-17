package MKAgent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import MKAgent.Protocol.MoveTurn;

/**
 * The main application class. It also provides methods for communication
 * with the game engine.
 */
public class Random {
	//class variables
	public static Board board;
	public static String latestMsg;
	//Create Log for debugging
	public static BufferedWriter log;
	public static Side player; //enum of SOUTH and NORTH
	
    /**
     * Input from the game engine.
     */
    private static Reader input = new BufferedReader(new InputStreamReader(System.in));

    /**
     * Sends a message to the game engine.
     * @param msg The message.
     */
    public static void sendMsg (String msg)
    {
    	System.out.print(msg);
    	System.out.flush();
    }

    /**
     * Receives a message from the game engine. Messages are terminated by
     * a '\n' character.
     * @return The message.
     * @throws IOException if there has been an I/O error.
     */
    public static String recvMsg() throws IOException
    {
    	StringBuilder message = new StringBuilder();
    	int newCharacter;

    	do
    	{
    		newCharacter = input.read();
    		if (newCharacter == -1)
    			throw new EOFException("Input ended unexpectedly.");
    		message.append((char)newCharacter);
    	} while((char)newCharacter != '\n');

		return message.toString();
    }

	/**
	 * The main method, invoked when the program is started.
	 * @param args Command line arguments.
	 */
	public static void main(String[] args)	{
		
		try {
			//setup log
			log = new BufferedWriter(new FileWriter("random.txt"));
			
			//initialize Game state (board/sides)
			interpretStartMsg(); //find out which player we are
			board = new Board(7,7);
			
			
			if (player == Side.SOUTH) {
				//we are first
				makeRandomMove();//make random move
			} 
			
			
			
			//Make random moves that are valid
			int i = 0;
			while (i++ < 1000) { //loop 1000 times to not end too early
				latestMsg = Main.recvMsg();
				MoveTurn turn = Protocol.interpretStateMsg(latestMsg, board);
				log.write(board.toString());
				log.flush();
				if (turn.again) {
					makeRandomMove();
				}
			}
				
			log.flush();
			//close log
			log.close();
		} catch (IOException e) {
			System.err.println((e.getMessage()));
		}
		
		//Board board = new Board();
		catch (InvalidMessageException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
		}
	}
	
	//checks and records the first msg received and finds if we are South or North
	public static void interpretStartMsg() throws IOException {
		log.write("DEBUG");
		log.flush();
		latestMsg = Main.recvMsg(); //receive first message
		log.write(latestMsg); //log it
		log.flush();
		
		try {
			// check msg is type start
			if (MsgType.START != Protocol.getMessageType(latestMsg)) {
				//MsgType is not START something is wrong!
				log.write("Expected msg type to be START -- something is wrong!!\n");
				System.exit(0);
			} else {
				//Everything is as expected interpret start msg
				if (Protocol.interpretStartMsg(latestMsg)) {
					player = Side.SOUTH; //Set player as SOUTH
					log.write("We are the SOUTH player so we go first\n");
				} else {
					player = Side.NORTH; //Set player as NORTH
					log.write("We are the NORTH player so we go second\n");
				}
				
			}
			log.flush();
			
		} catch (InvalidMessageException e) {
			log.write(e.getMessage());
		}
		
	}
	
	public static void makeRandomMove() throws IOException {
		boolean finished = false;
		while (!finished) {
			int nextMove = (int) ((Math.random() * ((7 - 1) + 1)) + 1); // pick random number from 1 to 7
			
			Kalah kalah = new Kalah(board);
			
			//check move is legal
			if (kalah.isLegalMove(new Move(player,nextMove))) {
				log.write("Making Move " + nextMove + " \n");
				Main.sendMsg(Protocol.createMoveMsg(nextMove)); // send move msg
				kalah.makeMove(new Move(player,nextMove));
				finished = true;
				log.flush();
			}
		}		
	}
	
		
}

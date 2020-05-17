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
 * This class is use for testing our bot
 * It will perform the move you provide as arguments
 */
public class TestBot {
  	//class variables
  	public static Board board;
  	public static String latestMsg;
  	//Create Log for debugging
  	public static BufferedWriter log;
    public static BufferedWriter log1;
  	public static Side player; //enum of SOUTH and NORTH

    /**
     * Input from the game engine.
     */
    private static Reader input = new BufferedReader(new InputStreamReader(System.in));

    /**
     * Sends a message to the game engine.
     * @param msg The message.
     */
    public static void sendMsg (String msg) throws IOException
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

    //checks and records the first msg received and finds if we are South or North
  	public static void interpretStartMsg() throws IOException , InvalidMessageException{
  		latestMsg = Main.recvMsg(); //receive first message
  		log.write(latestMsg); //log it

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

  		} catch (InvalidMessageException e) {
  			log.write(e.getMessage());
  		}
  	}


    public static int[] createMoveArray(String[] args) throws IOException{

        StringBuilder buildStr = new StringBuilder("");
        char[] charArray = null;
        String argStr= null;
          for(int i = 0 ; i < args.length; i++){
            buildStr = buildStr.append(args[i]);
          }
          argStr = buildStr.toString();
          argStr = argStr.replaceAll(" ", "");
          charArray = argStr.toCharArray();
          int[] moveArray = new int[charArray.length];
          for(int i = 0; i < charArray.length; i++){
            moveArray[i] = Character.getNumericValue(charArray[i]);
          }

          return moveArray;
    }


    public static void makePlannedMove(int nextMove) throws IOException , InvalidMessageException{

        Kalah kalah = new Kalah(board);

        //check move is legal
        if (kalah.isLegalMove(new Move(player,nextMove))) {
          log.write("Making Move " + nextMove + " \n");
          Main.sendMsg(Protocol.createMoveMsg(nextMove)); // send move msg
          kalah.makeMove(new Move(player,nextMove));
          log.flush();
        }else{
          System.exit(1);
        }

    }


    public static void playPlanned(int[] moveArray) throws IOException, InvalidMessageException{

      log = new BufferedWriter(new FileWriter("log.txt"));

      //initialize Game state (board/sides)
      interpretStartMsg(); //find out which player we are
      board = new Board(7,7);

      int nextMoveArrayElement = 0;
      //int nextMove = hardCodedMoveArray[nextMoveArrayElement];
      int nextMove = moveArray[nextMoveArrayElement];
      if (player == Side.SOUTH) {
        makePlannedMove(nextMove);
        nextMoveArrayElement++;
      }

      int i = 0;
      while (i++ < 1000) { //loop 1000 times to not end too early
        latestMsg = Main.recvMsg();
        MoveTurn turn = Protocol.interpretStateMsg(latestMsg, board);
        log.write(board.toString());
        log.flush();
        if (turn.again) {
          //nextMove = hardCodedMoveArray[nextMoveArrayElement];
          nextMove = moveArray[nextMoveArrayElement];
          makePlannedMove(nextMove);
          nextMoveArrayElement++;
        }
      }

    }


	/**
	 * The main method, invoked when the program is started.
	 * @param args Command line arguments.
	 */
	public static void main(String[] args)	{

    		try {
          //SOUTH: 1 3 4 1 2 3 1 4 2 6 7 7 6 5 6 4 3 6 5 2 6 5 3 2 4 5 6 1 4
          //NORTH: 1 4 3 5 3 1 2 6 7 1 2 7 6 7 5 6 7 5 7 3 7 5 7 2 1 7 2 6 7 5 (6/3)
          //MKAgent perform illegal move when we are north
          int[] moveArray = createMoveArray(args);
          playPlanned(moveArray);

          //close log
          log.close();
    		} catch (IOException e) {
    			System.err.println((e.getMessage()));
    		} catch (InvalidMessageException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}

	}//main


}

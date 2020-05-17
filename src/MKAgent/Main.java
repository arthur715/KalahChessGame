package MKAgent;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import MKAgent.Protocol.MoveTurn;


class MiniMaxThread implements Runnable {

	private Node 	child;
	private int 	depth;
	private float alpha;
	private float beta;

	// Store result to be returned
	private float result;

	// Flag to kill thread once thread stores result in float[] arr
	private final AtomicBoolean running = new AtomicBoolean(false);

	public MiniMaxThread(Node child, int depth, float alpha,
											float beta, Side player, int i)	{
		this.child 	= child;
		this.depth 	= depth;
		this.alpha 	= alpha;
		this.beta 	= beta;
	}

	@Override
	public void run() {
		running.set(true);

		while (running.get()) {
			try {
				result = Main.minimax(child, depth, alpha, beta, child.getPlayerToMove());
    	}
			catch (Exception e){
        System.err.println(e.getMessage());
      }
			// kill thread after result obtained
			running.set(false);
		}
	}

	public float getResult() {
		return result;
	}
}
/*
int len = root.getChildren().size();

Thread[] threads = new Thread[len];
MiniMaxThread[] miniMaxThreads = new MiniMaxThread[len];

int i = 0;
for (Node child : root.getChildren()) {
	miniMaxThreads[i] = new MiniMaxThread(child, DEPTH,
											Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY,
													player, i);
	threads[i] = new Thread(miniMaxThreads[i]);
	threads[i].start();
	i++;
}

// Wait for threads to end
for (int j = 0; j < threads.length; j++) {
	threads[j].join();
}
*/


/**
 * The main application class. It also provides methods for communication
 * with the game engine.
 */
public class Main {
	//class variables
	//public static Board board;
	public static String latestMsg;
	//Create Log for debugging
	//public static BufferedWriter log;
	public static Side player; //enum of SOUTH and NORTH
	public static final int DEPTH = 7;

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
			//log = new BufferedWriter(new FileWriter("log.txt"));

			//initialize Game state (board/sides)
			interpretStartMsg(); //find out which player we are
			Board board = new Board(7,7);

			/*
			if (player == Side.SOUTH) {
				//we are first
				makeRandomMove();//make random move
			} */

			Node root = new Node();
			addChildrenFirstTurn(root); // find all valid children
			/*for (Node child : root.getChildren()) {
				log.write(Byte.toString(child.getMove()) + "\n");
				log.write(child.getPlayerToMove().toString() + "\n");
				log.flush();
				for (Node child2 : child.getChildren()) {
					log.write("\t" + Byte.toString(child2.getMove()) + "\n");
					log.write("\t" + child2.getPlayerToMove().toString() + "\n");
					log.write("\t" + child2.getBoard().toString() + "\n");
					log.flush();
				}
			}*/

			Node bestChild = new Node();
			float currentBest =  Float.NEGATIVE_INFINITY;
			//float temp = 0;

			// MAKE FIRST MOVE IF SOUTH
			if (player == Side.SOUTH) {

				int len = root.getChildren().size();
				int i = 0;

				Thread[] threads = new Thread[len];
				MiniMaxThread[] miniMaxThreads = new MiniMaxThread[len];


				//find best from children
				for (Node child : root.getChildren()) {
					miniMaxThreads[i] = new MiniMaxThread(child, DEPTH,
					Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY,
					player, i);
					threads[i] = new Thread(miniMaxThreads[i]);
					threads[i].start();
					i++;
				}

				// Wait for threads to end
				for (int j = 0; j < threads.length; j++) {
					threads[j].join();
				}

				i = 0;
				for (Node child : root.getChildren()) {
					//log.write("move " + child.getMove() + " " + miniMaxThreads[i].getResult() + ", ");
					if (player == Side.NORTH) {
						if (miniMaxThreads[i].getResult() <= currentBest) {
							currentBest = miniMaxThreads[i].getResult();
							bestChild = child;
						//	log.write("best option is: move" + bestChild.getMove() + ",");
						}
					}
					else {
						if (miniMaxThreads[i].getResult() >= currentBest) {
							currentBest = miniMaxThreads[i].getResult();
							bestChild = child;
						//	log.write("best option is: move" + bestChild.getMove() + ",");
						}
					}
					i++;
				}
				//log.write("\n");
				//log.flush();
				//send move
				//log.write("Making MOVE: " + bestChild.getMove() + "\n");
				Main.sendMsg(Protocol.createMoveMsg(bestChild.getMove())); // send move msg

			}

/*			//Make random moves that are valid
			int i = 0;
			while (i++ < 1000) { //loop 1000 times to not end too early
				latestMsg = Main.recvMsg();
				MoveTurn turn = Protocol.interpretStateMsg(latestMsg, board);
				log.write(board.toString());
				log.flush();
				if (turn.again) {
					makeRandomMove();
				}
			}*/

			//make moves whenever its our turn
			int i = 0;
			while (i++ < 1000) {
				//wait for next message
				latestMsg = Main.recvMsg();
				MoveTurn turn = Protocol.interpretStateMsg(latestMsg, board);
				if (turn.move == -1) {
					//player = player.opposite();
				//	log.write("OPPONENT SWAPPED!\n");
					for (Node child : root.getChildren()) {
						if (child.getMove() == 8) {
							root = child;
						}
					}
				} else {
				/*	log.write("ROOT BOARD\n");
					log.flush();
					log.write(root.getBoard().toString() + "\n");
					log.flush();*/
					for (Node child : root.getChildren()) {
						if (child.getMove() == turn.move) {
							root = child;
						}
					}

				}
				if (root.getChildren().isEmpty()) {
					addChildren(root);
				}

			/*	log.write("NEW ROOT BOARD\n");
				log.flush();
				log.write(root.getBoard().toString() + "\n");
				log.flush();

				log.write("CURRENT BOARD\n");
				log.flush();

				log.write(board.toString());
				log.flush();*/

				/*log.write(Byte.toString(root.getMove()) + " " + minimax(root, 7, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, root.getPlayerToMove()) + " " + "\n");
				log.write(root.getPlayerToMove().toString() + "\n");
				log.flush();*/

				/*for (Node child : root.getChildren()) {
					log.write(Byte.toString(child.getMove()) + " " + minimax(child, 6, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, child.getPlayerToMove()) + " " + "\n");
					log.write(child.getPlayerToMove().toString() + "\n");
					log.flush();
				}*/

				bestChild = new Node();
				//temp = 0;
				if (player == Side.NORTH) {
					currentBest = Float.POSITIVE_INFINITY;
				} else {
					currentBest = Float.NEGATIVE_INFINITY;
				}
				if (turn.again) {
					//find best from children
					Collections.sort((List<Node>) root.getChildren());
					int len = root.getChildren().size();
					i = 0;

					Thread[] threads = new Thread[len];
					MiniMaxThread[] miniMaxThreads = new MiniMaxThread[len];


					//find best from children
					for (Node child : root.getChildren()) {
						miniMaxThreads[i] = new MiniMaxThread(child, DEPTH,
						Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY,
						player, i);
						threads[i] = new Thread(miniMaxThreads[i]);
						threads[i].start();
						i++;
					}

					// Wait for threads to end
					for (int j = 0; j < threads.length; j++) {
						threads[j].join();
					}

					i = 0;
					for (Node child : root.getChildren()) {
					//	log.write("move " + child.getMove() + " " + miniMaxThreads[i].getResult() + ", ");
						if (player == Side.NORTH) {
							if (miniMaxThreads[i].getResult() <= currentBest) {
								currentBest = miniMaxThreads[i].getResult();
								bestChild = child;
						//		log.write("best option is: move" + bestChild.getMove() + ",");
							}
						}
						else {
							if (miniMaxThreads[i].getResult() >= currentBest) {
								currentBest = miniMaxThreads[i].getResult();
								bestChild = child;
							//	log.write("best option is: move" + bestChild.getMove() + ",");
							}
						}
						i++;
					}
					/*log.write("\n");
					log.flush();*/
					//send move
					if (bestChild.getMove() == 0) {
					/*	log.write("NO MOVE FOUND!!!");
						log.write(bestChild.getBoard().toString());
						log.write("temp: " + temp + ", currentBest: " + currentBest);
						log.flush();*/
					}
				/*	log.write("Making MOVE: " + bestChild.getMove() + "\n");
					log.flush();*/
					if (bestChild.getMove() == 8) {
						Main.sendMsg(Protocol.createSwapMsg());
						//update root after swapping
						for (Node child : root.getChildren()) {
							if (child.getMove() == 8) {
								root = child;
							}
						}
					} else {
						Main.sendMsg(Protocol.createMoveMsg(bestChild.getMove())); // send move msg
					}

				}
			}

			/*log.flush();
			//close log
			log.close();*/
		} catch (Exception e) {
			System.err.println((e.getMessage()));
		}
/*
		//Board board = new Board();
		catch (InvalidMessageException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
		}*/
	}

	//checks and records the first msg received and finds if we are South or North
	public static void interpretStartMsg() throws IOException {
		latestMsg = Main.recvMsg(); //receive first message
		/*log.write(latestMsg); //log it
		log.flush();*/

		try {
			// check msg is type start
			if (MsgType.START != Protocol.getMessageType(latestMsg)) {
				//MsgType is not START something is wrong!
				//log.write("Expected msg type to be START -- something is wrong!!\n");
				System.exit(0);
			} else {
				//Everything is as expected interpret start msg
				if (Protocol.interpretStartMsg(latestMsg)) {
					player = Side.SOUTH; //Set player as SOUTH
				//	log.write("We are the SOUTH player so we go first\n");
				} else {
					player = Side.NORTH; //Set player as NORTH
				//	log.write("We are the NORTH player so we go second\n");
				}

			}
			//log.flush();

		} catch (InvalidMessageException e) {
		//	log.write(e.getMessage());
		}

	}

/*	public static void makeRandomMove() throws IOException {
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
	}*/

	public static float minimax(Node position,int depth, float alpha, float beta, Side player) throws IOException {
	    if (depth == 0 || Kalah.gameOver(position.getBoard())) {
	    	//return evaluateBoard(position.getBoard());
	    	return evaluateBoard2(position);
	    }

	    //Player is maximizingPlayer
	    if (player == Side.SOUTH) {
	    	float maxEval = Float.NEGATIVE_INFINITY;

	    	//if no children of given position find all valid children
	    	if (position.getChildren().isEmpty()) {
	    		addChildren(position);
	    	}
	    	//for each child of position
	    	Collections.sort((List<Node>) position.getChildren());
	    	for (Node child : position.getChildren()) {
	    		float eval = minimax(child, depth - 1, alpha, beta, child.getPlayerToMove());
	    		maxEval = Math.max(maxEval, eval);
	    		alpha = Math.max(alpha, eval);
	    		if (beta <= alpha) {
	    			break; //dont need to check other children
	    		}
	    	}
	    	return maxEval;

	    	//player is minimizing player
	    } else {
	    	float minEval = Float.POSITIVE_INFINITY;

	    	//if no children of given position find all valid children
	    	if (position.getChildren().isEmpty()) {
	    		addChildren(position);
	    	}

	    	// for each child of position
	    	for (Node child : position.getChildren()) {
	    		float eval = minimax(child, depth - 1, alpha, beta, child.getPlayerToMove());
	    		minEval = Math.min(minEval, eval);
	    		beta = Math.min(beta, eval);
	    		if (beta <= alpha) {
	    			break; //dont need to check other children
	    		}
	    	}
	    	return minEval;
	    }
	}


	//return a score for the state of the given board
	public static float evaluateBoard(Board board) {

		return board.getSeedsInStore(Side.SOUTH) - board.getSeedsInStore(Side.NORTH);

	}

	public static float evaluateBoard2(Node node) {
		Board nodeBoard = node.getBoard();
		Side side = node.getPlayerToMove();
		//check if SOUTH has already won or lost
		//comment this out if you went to evaluate by how much score we win by
		/*if (nodeBoard.getSeedsInStore(Side.SOUTH) >= 50) {
			return Float.POSITIVE_INFINITY; // we won so dont care anymore
		} else if (nodeBoard.getSeedsInStore(Side.NORTH) >= 50) {
			return Float.NEGATIVE_INFINITY; //we lost so dont care anymore
		}*/

		float score = nodeBoard.getSeedsInStore(Side.SOUTH) - nodeBoard.getSeedsInStore(Side.NORTH);
		float extraTurnScore;
		float extraTurnScore2;

		if (side == Side.SOUTH) {
			extraTurnScore = Float.NEGATIVE_INFINITY;
			extraTurnScore2 = Float.NEGATIVE_INFINITY;
		} else {
			extraTurnScore = Float.POSITIVE_INFINITY;
			extraTurnScore2 = Float.POSITIVE_INFINITY;
		}

		if (side == Side.SOUTH) {
			score += 3; //add 3 if its our turn
			// deal with seeds less than 7
			byte extraTurn = canGetExtraTurn1(nodeBoard, side);
			if (extraTurn != 0) {
				for (Node child : node.getChildren()) {
					if (child.getMove() == extraTurn) {
						 extraTurnScore = evaluateBoard2(child);
					}
				}
			}
			//deal with seeds larger than 15
			extraTurn = canGetExtraTurn2(nodeBoard, side);
			if (extraTurn != 0) {
				for (Node child : node.getChildren()) {
					if (child.getMove() == extraTurn) {
						extraTurnScore2 = evaluateBoard2(child);
						if(extraTurnScore2 > extraTurnScore)
						{
							extraTurnScore = extraTurnScore2;
						}
					}
				}
			}

			score +=  (float) (holeCapture1(nodeBoard, side));
		}
		else {
			score -= 3;
			// deal with seeds less than 7
			byte extraTurn = canGetExtraTurn1(nodeBoard, side);
			if (extraTurn != 0) {
				for (Node child : node.getChildren()) {
					if (child.getMove() == extraTurn) {
						extraTurnScore = evaluateBoard2(child);
					}
				}
			}
			//deal with seeds larger than 15
			extraTurn = canGetExtraTurn2(nodeBoard, side);
			if (extraTurn != 0) {
				for (Node child : node.getChildren()) {
					if (child.getMove() == extraTurn) {
						extraTurnScore2 = evaluateBoard2(child);
						if(extraTurnScore2 < extraTurnScore)
						{
							extraTurnScore = extraTurnScore2;
						}
					}
				}
			}

			score -=  (float) (holeCapture1(nodeBoard, side));

		}


		if((nodeBoard.getSeedsInStore(side) + nodeBoard.getSeedsInStore(side.opposite())) > 60){
			for (int i = 1; i < 8; i++) {
				score += ( 0.25 * nodeBoard.getSeeds(Side.SOUTH, i));
				score -= ( 0.25 * nodeBoard.getSeeds(Side.NORTH, i));
			}
		} else if((nodeBoard.getSeedsInStore(side) + nodeBoard.getSeedsInStore(side.opposite())) > 40) {
			for (int i = 1; i < 8; i++) {
				score += ( 0.2 * nodeBoard.getSeeds(Side.SOUTH, i));
				score -= ( 0.2 * nodeBoard.getSeeds(Side.NORTH, i));
			}
		} else {
			for (int i = 1; i < 8; i++) {
				score += ( 0.1 * nodeBoard.getSeeds(Side.SOUTH, i));
				score -= ( 0.1 * nodeBoard.getSeeds(Side.NORTH, i));
			}

		}
		if(side == Side.SOUTH){
			return Math.max(score, extraTurnScore);
		}
		else
		{
			return Math.min(score, extraTurnScore);
		}
	}

	public static float holeCapture1(final Board board, final Side side) {
		float n = 0;
		float bestCapture = 0;
		for (int i = 1; i < 8; ++i) {
			if (board.getSeeds(side, i) == 15 )
				n = board.getSeedsOp(side, i);
				if(n > bestCapture){
					bestCapture = n+2;
				}
				//n	+= board.getSeedsOp(side, i) + 2;
			if (board.getSeeds(side, i) == 0 && canPutLastSeedHere1(board, side, i)) {
				n = board.getSeedsOp(side, i);
				if(n > bestCapture){
					bestCapture = n+1;
				}
				//n += board.getSeedsOp(side, i) + 1;
			}
			if (board.getSeeds(side, i) == 0 && canPutLastSeedHere2(board, side, i)) {
				n = board.getSeedsOp(side, i);
				if(n > bestCapture){
					bestCapture = n+2;
				}
				//n += board.getSeedsOp(side, i) + 2;
			}
		}
		return bestCapture;
	}

	public static boolean canPutLastSeedHere1(final Board board, final Side side, final int n) {
		for (int i = 1; i < n; i++){
			if( board.getSeeds(side, i) == (n - i) )
			return true;
		}
		return false;
	}

	public static boolean canPutLastSeedHere2(final Board board, final Side side, final int n) {
		for (int i = n+1; i < 8; i++){
			if( board.getSeeds(side, i) == (15 - i + n) )
			return true;
		}
		return false;
	}

	public static byte canGetExtraTurn1(final Board board, final Side side) {
		for (byte i = 7; i >= 1; i--) {
			if ( board.getSeeds(side, i) == (8 - i) ) {
				return i;
			}
		}
		return 0;
	}

	public static byte canGetExtraTurn2(final Board board, final Side side) {
		for (byte i = 7; i >= 1; i--) {
			if ( board.getSeeds(side, i) == (23 - i) ) {
				return i;
			}
		}
		return 0;
	}

/////////////////////////// evaluate board 4 ////////////////////////////////
/*	public static float evaluateBoard4(Node node) {
		final Board nodeBoard = node.getBoard();
		final Side side = node.getPlayerToMove();
		float n =  (float) (holeCapture1(nodeBoard, side) * 0.5);

		//if it is our turn
		if (side == player) {
			n += 2;
			// deal with seeds less than 7
			byte extraTurn = canGetExtraTurn1(nodeBoard, side);
			if (extraTurn != 0) {
				Kalah.makeMove(board, move)
			}
			//deal with seeds larger than 15
			if (canGetExtraTurn2(nodeBoard, side)) {
				n += 1;
			}
		}
		else{
			n -= 2;
			// deal with seeds less than 7
			if (canGetExtraTurn1(nodeBoard, side)) {
				n -= 4;
			}
			//deal with seeds larger than 15
			if (canGetExtraTurn2(nodeBoard, side)) {
				n -= 1;
			}
		}

		for (int i = 1; i < 8; i++) {
			n += (0.1 * nodeBoard.getSeeds(Side.SOUTH, i));
			n -= (0.1 * nodeBoard.getSeeds(Side.NORTH, i));
		}

		return (float) (n + (float)(nodeBoard.getSeedsInStore(Side.SOUTH) - nodeBoard.getSeedsInStore(Side.NORTH)) * 0.5);


	}
*/

	/*public static float evaluateBoard5(Node node) {
		Board nodeBoard = node.getBoard();
		float score = nodeBoard.getSeedsInStore(Side.SOUTH) - nodeBoard.getSeedsInStore(Side.NORTH);
		Side side = node.getPlayerToMove();
		float bestCapture = 0;

		if (side == Side.SOUTH) {
			score += 2;
		}
		else{
			score -= 2;
		}

		for (int i = 1; i < 8; i ++)
		{
			if (nodeBoard.getSeeds(side, i) == 0)
			{
				for (int j = 1; j < 8; j ++)
				{
					if(nodeBoard.getSeeds(side, j) == i - j)
					{
						if(side == Side.SOUTH)
						{
							bestCapture = nodeBoard.getSeedsOp(side, i);
							score += bestCapture;
						}
						else
						{
							bestCapture = nodeBoard.getSeedsOp(side, i);
							score -= bestCapture;
						}
					}
				}
			}
		}

		for (int i = 1; i < 8; i ++) 
		{
			if (nodeBoard.getSeeds(side, i) == 8 - i)
			{
				if(side == Side.SOUTH)
					score += 2;
				else
					score -= 2;
			}
		}

		for (int i = 1; i < 8; i++) {
			score += (0.2 * nodeBoard.getSeeds(Side.SOUTH, i));
			score -= (0.2 * nodeBoard.getSeeds(Side.NORTH, i));
		}
		return score;

	}*/
	
	/*public static float evaluateBoard6(Node node) {
		Board nodeBoard = node.getBoard();
		float score = (float)0.0;
		float scoreSouth = nodeBoard.getSeedsInStore(Side.SOUTH);
		float scoreNorth = nodeBoard.getSeedsInStore(Side.NORTH);
		int southSeedsInLeftMostPit = nodeBoard.getSeeds(Side.SOUTH, 1);
		int northSeedsInLeftMostPit = nodeBoard.getSeeds(Side.NORTH, 1);
		int southSeedsInAllPits = 0;
		int northSeedsInAllPits = 0;
		for(int i = 1; i < 8; i ++)
		{
			southSeedsInAllPits += nodeBoard.getSeeds(Side.SOUTH, i);
			northSeedsInAllPits -= nodeBoard.getSeeds(Side.NORTH, i);
		} // for
		int southNumberOfUnEmptyPits = 0;
		int northNumberOfUnEmptyPits = 0;
		for(int i = 1; i < 8; i ++)
		{
			if(nodeBoard.getSeeds(Side.SOUTH, i) != 0)
			{
				southNumberOfUnEmptyPits ++;
			}
			if(nodeBoard.getSeeds(Side.NORTH, i) != 0)
			{
				northNumberOfUnEmptyPits --;
			}
		} // for
		score += (float)((float)scoreSouth * 0.57) + (float)((float)southSeedsInLeftMostPit * 0.19) + (float)((float)southSeedsInAllPits * 0.19)+ (float)((float)southNumberOfUnEmptyPits * 0.37);
		score -= (float)((float)scoreNorth * 0.57) - (float)((float)northSeedsInLeftMostPit * 0.19) - (float)((float)northSeedsInAllPits * 0.19) - (float)((float)northNumberOfUnEmptyPits * 0.37);
		return score;
	}*/

///////////////////////////////////////////////////////////////////////////////

	// finds all valid children for a given node
	public static void addChildren(Node parent) {
		for (byte i = 1; i < 8; i++) {
			//if move is valid create child
			if (Kalah.isLegalMove(parent.getBoard(), new Move(parent.getPlayerToMove(),i))) {
				parent.addChild(i, rankMove(parent, i));
			}
		}
	}

	//method to fix bad Kalah class code and add swap
	public static void addChildrenFirstTurn(Node parent) throws IOException {
		for (byte i = 1; i < 8; i++) {
			//if move is valid create child
			parent.addChildNorth(i, rankMove(parent, i));
		}
		for (Node child : parent.getChildren()) {
			addChildren(child);
/*				log.write("test");
			log.flush();*/
			child.addSwappableChild();
/*				for (Node child2 : child.getChildren().values()) {
				log.write(child2.getBoard().toString());
				log.flush();
			}*/
		}
	}

	//returns a value that is an estimate of how good a move is
		public static byte rankMove(Node node, byte move) {
			int seeds = node.getBoard().getSeeds(node.getPlayerToMove(), move);

			//can we make a move that steals from opponents hole?


/*			//if seeds loop around and land in starting hole to steal opponents hole
			if ((seeds + move) == 15) {
				return 3;
			}

			//can we make a move that gives another turn?
			if ((seeds + move) == 8) {
				return 2;
			}

			//if the seeds stay on our side
			if ((seeds+move) <=7) {
				return 1;
			}

			//if the seeds go to opponents side and dont loop back
			if ((seeds+move) <= 14) {
				return 0;
			}*/
			//can we make a move that gives another turn?
			if ((seeds + move) == 8) {
				return 0;
			}

			//if seeds loop around and land in starting hole to steal opponents hole
			if (seeds == 15) {
				return 1;
			}

			if ((seeds + move) > 15) {
				return 2;
			}




			//if the seeds stay on our side
			if ((seeds+move) <=7) {
				return 3;
			}

			//if the seeds go to opponents side and dont loop back
			if ((seeds+move) <= 14) {
				return 4;
			}

			return 5;
		}



}

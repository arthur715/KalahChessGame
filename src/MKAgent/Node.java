package MKAgent;

import java.util.ArrayList;

public class Node implements Comparable<Node>{

    private byte move = 0; // move = 0 = root
    private float weight; //record monte carlo weight/score/rating
	//private Node parent;
    private ArrayList<Node> children;
    private Board board; //board state at this node
    private Side playerToMove;
    private byte rank;

    //Root node constructor
    public Node() {
    	this.children = new ArrayList<Node>(); //initialize children map
    	playerToMove = Side.SOUTH; //South is always first to move
    	board = new Board(7,7); // create initial board state
    }

    //create Root node not at initial board state
    public Node(Board board, Side playerToMove) {
    	this.children = new ArrayList<Node>(); //initialize children map
    	this.playerToMove = playerToMove;
    	this.board = board;
    }

    //Normal node constructor
    public Node(byte move) {
    	this.move = move;
    	//this.parent = parent;
    	this.children = new ArrayList<Node>();
    }

	public float getWeight() {
		return weight;
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}

/*	public Node getParent() {
		return parent;
	}*/

  public ArrayList<Node> getChildren() {
		return children;
	}

	public void setChildren(ArrayList<Node> children) {
		this.children = children;
	}

	public byte getMove() {
		return move;
	}

	public void setMove(byte move) {
		this.move = move;
	}

	public Board getBoard() {
		return board;
	}

	public void setBoard(Board board) {
		this.board = board;
	}

	public Side getPlayerToMove() {
		return playerToMove;
	}

	public void setPlayerToMove(Side playerToMove) {
		this.playerToMove = playerToMove;
	}

	public void addChild(byte move, byte rank) {
		Node child = new Node((byte)move); // create child node and set parent
		children.add(child); //add child node to map of children

		try {
			//copy board to create new one with move made on it
			child.board = board.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		// make the move so the child has correct board and set playerToMove
		child.playerToMove = Kalah.makeMove(child.board, new Move(this.playerToMove, move));
	}

	public void addChildNorth(byte move, byte rank) {
		Node child = new Node((byte)move); // create child node and set parent
		children.add(child); //add child node to map of children

		try {
			//copy board to create new one with move made on it
			child.board = board.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		Kalah.makeMove(child.board, new Move(this.playerToMove, move));
		child.playerToMove = Side.NORTH;
	}

	public void addSwappableChild() {
		Node child = new Node((byte)8);
		children.add(child);
		try {
			//copy board to create new one with move made on it
			child.board = board.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		//swap board to keep us as south on SWAP
		int[] south = new int[8];
		int[] north = new int[8];
		for (int i = 1; i < 8; i++) {
			south[i] = child.board.getSeeds(Side.SOUTH, i);
			north[i] = child.board.getSeeds(Side.NORTH, i);
		}

		for (int i = 1; i < 8; i++) {
			child.board.setSeeds(Side.SOUTH, i, north[i]);
			child.board.setSeeds(Side.NORTH, i, south[i]);
		}

		int southStore = child.board.getSeedsInStore(Side.SOUTH);
		int northStore = child.board.getSeedsInStore(Side.NORTH);
		child.board.setSeedsInStore(Side.SOUTH, northStore);
		child.board.setSeedsInStore(Side.NORTH, southStore);
		child.setPlayerToMove(Side.SOUTH);
		child.rank = -1;
	}

	@Override
	public String toString() {
		StringBuilder tree = new StringBuilder();
		tree.append("move " + move + "\n");

		for(Node child : children) {
			tree.append(child + " ");
		}

		return tree.toString();
	}

	@Override
	public int compareTo(Node node) {
		return rank - node.rank;
	}


}

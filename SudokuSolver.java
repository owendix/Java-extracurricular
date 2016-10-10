public class SudokuSolver
{
	private static final int PLACEHOLDER=0;
	private static SudokuGame theGame;
	
	public static void main(String[] args)
	{
		boolean isGoodInitial, isSolutionPrinted=true;
		//Initialize sudoku game: make SudokuGame class
		final int MAXSOLUTIONS=100;
		
		//Read in user input string (row by row)
		//for initial fixed values: set initialEntries
		//0th row set to -1; 0th column set to -1
		
		
		//Initialize sudoku game
		if (args.length!=0)
			theGame = new SudokuGame(args[0],PLACEHOLDER);
		else
			theGame = new SudokuGame("read",PLACEHOLDER);
		System.out.println("Initial Setup: ");
		System.out.print(theGame);
		
		isGoodInitial=theGame.isValidInitial();
		
		//Solve sudoku game
		if (isGoodInitial){
			theGame.solveGame(MAXSOLUTIONS, isSolutionPrinted);	//true: print solution to screen
			System.out.println("All done!");
		}else{
			System.out.println("Invalid initial configuration");
		}
	}
}

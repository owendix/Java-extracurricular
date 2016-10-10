import java.util.Scanner;
import java.util.Random;

public class SudokuGame
{
	//Instance variables
	private int[][] game = new int[10][10];
	private final int PLACEHOLDER;
	//Have nothing stored in game[0][col] or game[row][0] for intuitive use
	private boolean[][] fixedEntry = new boolean[10][10];
	
	//Constructors
	public SudokuGame(String inputMethod, int aPLACEHOLDER)
	{
		//pass fixedEntry array references
		PLACEHOLDER=aPLACEHOLDER;
						
		if (inputMethod.indexOf("default")!=-1)
			setInitialGameDefault();
		else if (inputMethod.indexOf("generate")!=-1 
				|| inputMethod.indexOf("create")!=-1
				|| inputMethod.indexOf("random")!=-1)
			setInitialGameGenerate();
		else
			setInitialGameRead();
		
		assert isValidInitial();
		
	}
	public SudokuGame(int[][] theInitialEntries, int aPLACEHOLDER){
		//pass fixedEntry array references
		int row, col;
		PLACEHOLDER=aPLACEHOLDER;
		
		for (row=1; row<=9; row++){
			for (col=1; col<=9; col++){
				fixedEntry[row][col]=false;	//in case called outside constructor
				setEntryAt(row,col,theInitialEntries[row][col]);
				if (isValidEntryAt(row,col))
					fixedEntry[row][col]=true;
			}
			
		}
		assert isValidInitial();
	}
	
	
	//Mutator Methods
	public void setInitialGameRead()
	{
		Scanner stdin = new Scanner(System.in);
		int row, col;
		
		//Prompt for a line at a time
		fixedEntry[0][0]=false;
		setEntryAt(0,0,-1);
		System.out.print("Input rows of initial ");
		System.out.print("game with "+PLACEHOLDER+" as a placeholder ");
		System.out.print("and numbers separated by spaces.\n");
		for (row=1; row<=9; row++){
			fixedEntry[row][0]=false;
			setEntryAt(row,0,-1);
			fixedEntry[0][row]=false;
			setEntryAt(0,row,-1);
			System.out.println("Row "+row+": ");			
			//Read in a row at a time
			for (col=1; col<=9; col++){
				fixedEntry[row][col]=false;		//In case this is called outside
												//of constructor
				setEntryAt(row,col,stdin.nextInt());
				if (isValidEntryAt(row,col))
					fixedEntry[row][col]=true;
			}
		}
		stdin.close();
		
		assert isValidInitial();
		
	}
	public void setInitialGameDefault()
	{
		//For tinkering: specified this way so easy to visualize
		int[] row0={-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
		int[] row1={-1,0,8,0,0,0,0,0,5,0};
		int[] row2={-1,0,0,9,2,0,8,3,0,0};
		int[] row3={-1,0,4,0,0,0,0,0,2,0};
		int[] row4={-1,0,1,5,0,3,0,4,9,0};
		int[] row5={-1,0,0,8,0,2,0,6,0,0};
		int[] row6={-1,4,0,0,0,0,0,0,0,7};
		int[] row7={-1,0,0,0,7,0,3,0,0,0};
		int[] row8={-1,3,0,0,5,9,4,0,0,2};
		int[] row9={-1,0,0,0,0,1,0,0,0,0};
		int row, col;
		
		for (row=0;row<=9;row++)
			for (col=0;col<=9;col++)
				fixedEntry[row][col]=false;	//in case called outside constructor
		
		for (col=0;col<=9;col++){
			game[0][col]=setEntryAt(0,col,row0[col]);
			game[1][col]=setEntryAt(1,col,row1[col]);
			game[2][col]=setEntryAt(2,col,row2[col]);
			game[3][col]=setEntryAt(3,col,row3[col]);
			game[4][col]=setEntryAt(4,col,row4[col]);
			game[5][col]=setEntryAt(5,col,row5[col]);
			game[6][col]=setEntryAt(6,col,row6[col]);
			game[7][col]=setEntryAt(7,col,row7[col]);
			game[8][col]=setEntryAt(8,col,row8[col]);
			game[9][col]=setEntryAt(9,col,row9[col]);
			if (col>0){
				for (row=1;row<=9;row++){
					if (isValidEntryAt(row,col))
						fixedEntry[row][col]=true;
				}	
			}
		}
		
		assert isValidInitial();
	
	}
	public void setInitialGameGenerate()
	{
		//Declare an initial game to setup
		//Maintain a list
		//
		//max # of ALLOWED solutions
		final int MAXINSERT=8, MAXSOLUTIONS=1, MAXATTEMPTS=2000;
		int[] listPosition = new int[MAXINSERT];
		int nSolutions, nAttempts=0, i, iDecrement=MAXINSERT, row, col;
		boolean isSequential=false, solFound;
		
		setBlankInitialGame();
		//set multiple random entries at once and store
		//their positions (implied sequential ordering of game) in 
		//listPosition for later possible removal		
		do{
			nAttempts++;
			nSolutions=0;
			solFound=false;		//=-1 if failed, =1 if succeeded, =0 if searching
			//Add a group of random entries to the game board
			if (!isSequential)
				for (i=0; i<MAXINSERT; i++)
					listPosition[i]=setRandomValidEntry();
			
			//test the board for solutions
			if (isValidInitial()){
				nSolutions = solveGame(MAXSOLUTIONS+1,false);//Don't print
			}else{
				System.out.println("setInitialGameGenerate() failed. Default:");
				//don't want an error: set to default
				setInitialGameDefault();
				return;
			}
			
			//what to do based on nSolutions
			//sequential mode and non-sequential mode (bracket solution)
			if (!isSequential){	//bracket solution mode
				if (nSolutions==0){//Found upper limit
					isSequential=true;
					//remove from game but not list
					//Just set most recent entries to UNfixed so 
					//They'll be removed
					iDecrement=MAXINSERT;
					iDecrement--;
					row=1+(listPosition[iDecrement]-1)/9;
					col=1+(listPosition[iDecrement]-1)%9;
					fixedEntry[row][col]=false;
				}else if (nSolutions<=MAXSOLUTIONS){
					solFound=true;
				}//else: nSolutions>MAXSOLUTIONS keep increasing by MAXINSERT
							
			}else{//Sequential mode
				if (nSolutions>MAXSOLUTIONS){
					//Its gone from nSolutions==0 to too many
					//Clear the board and try again
					setBlankInitialGame();
					//reset important variables
					iDecrement=MAXINSERT;
					isSequential=false;
				}else if (nSolutions>0){
					solFound=true;
				}else{
					//nSolutions==0: still too constrained
					iDecrement--;
					row=1+(listPosition[iDecrement]-1)/9;
					col=1+(listPosition[iDecrement]-1)%9;
					fixedEntry[row][col]=false;				
				}
			}
			//Return game board to unsolved state
			for (row=1;row<=9;row++)
				for(col=1;col<=9;col++)
					if (!isFixedEntryAt(row,col))
						setEntryAt(row,col,PLACEHOLDER);
		
		}while(!solFound && nAttempts<MAXATTEMPTS);
				
		if (solFound){
			assert isValidInitial();
		}else{
			System.out.println("setInitialGameGenerate() failed. Default:");
			//don't want an error: set to default
			setInitialGameDefault();	
		}
	}
	private void setBlankInitialGame()
	{
		int row, col;
		//Initialize board
		for (row=0; row<=9; row++){
			for (col=0; col<=9; col++){
				fixedEntry[row][col]=false;
				if (row==0 || col==0)
					setEntryAt(row,col,-1);
				else
					setEntryAt(row,col,0);
			}
		}
	}
	public int setEntryAt(int row, int col, int entryValue)
	{
		/* Precondition: sudokuGame initialized
		 * Postcondition: valid entries set, invalid entries set to -1
		 * returns true if any entry is set, false otherwise
		 * */
		//Check if final/fixed
		//Checking row and column ensures the target index is valid
		assert (isValidEntry(row) && isValidEntry(col));
		if (!isFixedEntryAt(row,col)){
			if (!isValidEntry(entryValue))
				entryValue=PLACEHOLDER;		//placeholder entry is inserted
			game[row][col]=entryValue;
			return game[row][col];	//Return new entry, including
		}else{
			return -1;	//Entry is fixed
		}
		//NEED to be able to insert PLACEHOLDER to insert entries!!!!!!
	}
	private int setRandomValidEntry()
	{
		//
		//game[randRow][randCol] is set to a random entry
		//and fixedEntry[randRow][randCol] is set as well
		//Return the position (sequential ordering or row,col index)
		//of the random entry that was set
		Random rand = new Random();
		boolean isValidRandEntry;
		int randEntry, randRow, randCol;
		do{
			//Pick a random place and value, stick it there,
			//keep trying if its not a valid choice
			isValidRandEntry=true;
			randEntry = 1+rand.nextInt(9);	//integer between 1 and 9
			randRow = 1+rand.nextInt(9);
			randCol = 1+rand.nextInt(9);
			//Add a random entry: 1-9 somewhere on the board
			//If something's already there, try again
			if (!isFixedEntryAt(randRow,randCol)){
				setEntryAt(randRow,randCol,randEntry);
				isValidRandEntry=isValidRandEntry 
					&& isRowValid(randRow);
				isValidRandEntry=isValidRandEntry 
					&& isColValid(randCol);
				isValidRandEntry=isValidRandEntry 
					&& isBoxValid(randRow,randCol);
				if (isValidRandEntry)
					fixedEntry[randRow][randCol]=true;
				else
					setEntryAt(randRow,randCol,PLACEHOLDER);
			}else{//nothing set
				isValidRandEntry=false;
			}			
		}while(!isValidRandEntry);
		
		return (randRow-1)*9+randCol;	//return position placed within game
	}
	public int incrementEntryAt(int row, int col)
	{
		/* Returns NEW value of game[row][col],
		 * If entry is already at max value=9, sets to PLACEHOLDER and returns this val,
		 * Returns -1 if entry is fixed, OR if (row,col) index is invalid
		 * If entry is already a placeholder, increments and returns 1
		 * Precondition: 
		 * Postcondition: 
		 * */
		//Check if final/fixed
		//Checking row and column ensure the target index is valid
		assert (isValidEntry(row) && isValidEntry(col));
		if (!isFixedEntryAt(row,col)){
			if (game[row][col] == 9)
				game[row][col]=PLACEHOLDER;
			else
				game[row][col]++;
			return game[row][col];	//RETURN NEW VALUE OF game[row][col]
		}else{
			return -1;	//if entry is fixed
		}
	}
	
	//Accessor Methods
	public int solveGame(int MAXSOLUTIONS, boolean isSolutionPrinted)
	{
		int position=1, row, col, newEntry, nextStep=1;
		int nSolutions=0, count=0;
		final int MAXITERATIONS=81000;
		//Suggest: final int MAXSOLUTIONS=10;
		
		//position ranges from 1 to 81 inclusively
		//nextStep = 1 to go forward a step, -1 to go back a step
		do{
			count++;
			//position = 1:81 while adjusting gameBoard
			row = 1+(position-1)/9;
			col = 1+(position-1)%9;
			//Solve sudoku: print when solution found (do not store solution)
			//returns -1 if fixed entry
			newEntry=incrementEntryAt(row, col);
			if (newEntry!=-1){
				//Entry is not fixed
				if (newEntry!=PLACEHOLDER){
					if (isRowValid(row) && isColValid(col) && isBoxValid(row,col)){
						//new entry fits rules
						nextStep=1;		//Go forward
						position+=nextStep;
					}
					//else: entry breaks the rules, increment same position
				}else{
					//entry was at 9, incremented/cycled to PLACEHOLDER
					//No valid entries with base configuration of entries
					//Need to go back and change base entries
					nextStep=-1;	//Go backward
					position+=nextStep;
				}
			}else{//== -1 if entry is fixed (from initial gameBoard conditions)
				//If going forward (nextStep=1), continue forward
				//If going backward (nextStep=-1), continue backward
				position+=nextStep;
			}
			
			//Check for a solution
			if (position>=82){
				//Check that it is indeed solved
				assert isGameSolved();
				nSolutions++;
				count=0;	//reset count so puzzles with more solutions
							//are not more likely to time out for this
				if (isSolutionPrinted){
					System.out.println("Solution "+nSolutions+": ");
					//Print solution
					System.out.print(toString());	//uses .toString() automatically
				}
				nextStep=-1;
				position+=nextStep;
			}
									
		}while(position >= 1 && nSolutions<MAXSOLUTIONS && 
			count<MAXITERATIONS);
		//If this is not true, no more solutions
		if (count>=MAXITERATIONS){
			nSolutions=MAXITERATIONS;	//Predicting more solutions allowed
			if (isSolutionPrinted)
				System.out.println("solveGame(): hit max # of iterations!!!");
		}else if (nSolutions>=MAXSOLUTIONS && isSolutionPrinted){
			System.out.println("solveGame(): hit max # of solutions");
		}
		return nSolutions;		
	}
	
	public int[][] getGameCopy()
	{
		//Returns copy of game
		int[][] theGameCopy = new int[10][10];
		int row, col;
		for (row=0;row<theGameCopy.length; row++)
			for (col=0;col<theGameCopy[row].length; col++)
				theGameCopy[row][col]=game[row][col];
		
		return theGameCopy;
	}
	public int getEntryAt(int row, int col)
	{
		return game[row][col];
		//Skip checking for valid index for speed.
	}
	public boolean isFixedEntryAt(int row, int col)
	{
		/* Check if fixed Entry is true or false, set during construction
		 */
		return fixedEntry[row][col];
		
	}
	public int getNumberOfFixedEntries()
	{
		int row, col, nFixed=0;
		for (row=1;row<=9;row++)
			for (col=1; col<=9; col++)
				if (isFixedEntryAt(row,col))
					nFixed++;
		return nFixed;
	}
	
	public boolean isRowValid(int row)
	{
		// Check if there are no duplicate numbers
		// Note that only valid entry values (1-9) should be checked
		// Others assumed to be placeholders
		// Able to do partial, full column, randomly initialized or during solution
		/* Precondition: 
		 * Postcondition: no entries have changed. returns true if row is valid SO FAR
		 */
		int theCheckEntry, checkCol, seekCol;
		final int LASTCOL=9;
		
		for (checkCol=1; checkCol<=LASTCOL-1; checkCol++){
			if (isValidEntryAt(row,checkCol)){
				theCheckEntry=game[row][checkCol];
				for (seekCol=checkCol+1; seekCol<=LASTCOL; seekCol++){
					if (game[row][seekCol]==theCheckEntry){
						return false;
					}
				}
			}
		}
		return true;
	}
	
	public boolean isColValid(int col)
	{
		// Mirrored after isRowValid
		// Check if there are no duplicate numbers
		// Note that only valid entry values (1-9) should be checked
		// Others assumed to be placeholders
		// Able to do partial, full column, randomly initialized or during solution
		/* Precondition: 
		 * Postcondition: no entries have changed. returns true if column is 
		 * valid SO FAR
		 */
		int theCheckEntry, checkRow, seekRow;
		final int LASTROW=9;
				
		for (checkRow=1; checkRow<=LASTROW-1; checkRow++){
			if (isValidEntryAt(checkRow,col)){
				theCheckEntry=game[checkRow][col];
				for (seekRow=checkRow+1; seekRow<=LASTROW; seekRow++){
					if (game[seekRow][col]==theCheckEntry){
						return false;
					}
				}
			}
		}
		return true;
	}
	
	public boolean isBoxValid(int row, int col)
	{
		// Check if there are no duplicate numbers in box
		// Note that only valid entry values (1-9) should be checked
		// Others assumed to be placeholders
		// Able to do partial, full column, randomly initialized or during solution
		/* Precondition: 
		 * */
		
		int firstRow, firstCol, lastRow, lastCol, iRow, iCol, checkEntry;
		int jRow, jCol, startJCol;
		
		//Find start/end row and column of box
		firstRow = startBoxIndexAlong(row);
		lastRow = firstRow+2;
		firstCol = startBoxIndexAlong(col);
		lastCol = firstCol+2;
		
		//Go through a row, then move down to next
		//Cannot use isRowValid or isColValid: need to check throughout Box
		for (iRow=firstRow; iRow<=lastRow; iRow++){
			for (iCol=firstCol; iCol <=lastCol; iCol++){//Just to get checkEntry
				if (isValidEntryAt(iRow,iCol)){
					checkEntry = game[iRow][iCol];
					for (jRow = iRow; jRow<=lastRow; jRow++){
						//Where to start inner seek Entry
						if (jRow == iRow)
							startJCol = iCol+1;
						else
							startJCol = firstCol;
						for (jCol = startJCol; jCol<=lastCol; jCol++){
							if (game[jRow][jCol] == checkEntry){
								return false;
							}
						}
					}
				}//If not a valid entry, assume placeholder and move on

			}
		}
		return true;
	}
	public boolean isRowComplete(int row)
	{
		for (int iCol = 1; iCol <= 9; iCol++)
			if (!isValidEntryAt(row, iCol))
				return false;
		
		return true;
		
	}
	public boolean isColComplete(int col)
	{
		for (int iRow = 1; iRow <= 9; iRow++)
			if (!isValidEntryAt(iRow, col))
				return false;
		
		return true;
	}
	public boolean isBoxComplete(int row, int col)
	{
		int firstRow, lastRow, firstCol, lastCol, iRow, iCol;
		firstRow = startBoxIndexAlong(row);
		lastRow = firstRow+2;
		firstCol = startBoxIndexAlong(col);
		lastCol = firstCol+2;
		for (iRow=firstRow; iRow<=lastRow; iRow++){
			for (iCol=firstCol; iCol<=lastCol; iCol++){
				if (!isValidEntryAt(iRow,iCol))
					return false;
			}
		}
		return true;
	}
	public int startBoxIndexAlong(int index)
	{
		/* Precondition: index is between 1 and 9 inclusively
		 * Postcondition: returned value is start index 
		 * of 3x3 box along same dimension
		 * */
		return index - (index-1)%3;
	}
	public boolean isValidEntryAt(int row, int col)
	{
		/* Uses the fact that indices range from 1 to 9 (skip 0)
		 * Skip this for speed.*/
		if (isValidEntry(game[row][col]))
			return true;
		else
			return false;
	}
	public boolean isValidEntry(int entryValue)
	{
		/* Checks if entryValue (the number to be inserted into an
		 * index) is between 1 and 9.
		 * Skip this for speed.*/
		if (entryValue>=1 && entryValue<=9)
			return true;
		else
			return false;
	}
	public boolean isValidInitial()
	{
		//Check for no repeats, and at least
		int row, col, nFixedEntries=0, nPlaceHolders=0;
		final int MINENTRIES=1;
		boolean isAllValid=true;
		//fixed entries will automatically be valid
		//but if fixed not set, may have a valid entry
		for (row=1; row<=9; row++){
			for (col=1; col<=9; col++){
				if (isFixedEntryAt(row,col)){
					nFixedEntries++;
				}else if (game[row][col]==PLACEHOLDER){
					nPlaceHolders++;
				}
				if (row == startBoxIndexAlong(row) && col==startBoxIndexAlong(col)){
					isAllValid = isAllValid && isBoxValid(row,col);
				}
				if (row==9)
					isAllValid = isAllValid && isColValid(col);
			}
			isAllValid = isAllValid && isRowValid(row);
		}
		isAllValid = isAllValid && (nFixedEntries>=MINENTRIES);
		return isAllValid && ((nFixedEntries+nPlaceHolders)==81);
		
	}
	public boolean isGameSolved()
	{
		int row, col;
		boolean isDone=true, isValid=true;
		
		for (row=1; row<=9; row++){
			isDone = isDone && isRowComplete(row);
			for (col=1; col<=9; col++){
				if (row == startBoxIndexAlong(row) && col == startBoxIndexAlong(col))
					isValid = isValid && isBoxValid(row,col);
				if (row==9)
					isValid = isValid && isColValid(col);
			}
			isValid = isValid && isRowValid(row);
			if (!isValid || !isDone)
				break;
		}
		
		return (isDone && isValid);
	}
	public String toString()
	{
		String line, lineBreak;
		lineBreak="-------------------------\n";
		line=lineBreak;
		for (int row=1; row<=9; row++){
			for (int col=1; col<=9; col++){
				if (col%3==1)
					line=line+"| ";
				if(game[row][col]!=PLACEHOLDER)
					line=line+game[row][col]+" ";
				else if (isFixedEntryAt(row,col))
					line=line+"* ";		//should not be fixed and PLACEHOLDER
				else
					line=line+"  ";
			}
			line=line+"|\n";
			if (row%3==0)
				line=line+lineBreak;
		}
		return line;
	}
	public String toStringFixed()
	{
		String line, lineBreak;
		
		lineBreak="-------------------------\n";
		line=toString()+lineBreak;
		for (int row=1; row<=9; row++){
			for (int col=1; col<=9; col++){
				if (col%3==1)
					line=line+"| ";
				if (isFixedEntryAt(row,col))
					line=line+"* ";
				else
					line=line+"  ";
			}
			line=line+"|\n";
			if (row%3==0)
				line=line+lineBreak;
		}
		
		return line;
	}
}

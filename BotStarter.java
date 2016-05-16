// // Copyright 2015 theaigames.com (developers@theaigames.com)

//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at

//        http://www.apache.org/licenses/LICENSE-2.0

//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
// 
//    For the full copyright and license information, please view the LICENSE
//    file that was distributed with this source code.

package bot;

import java.util.*;
/**
 * BotStarter class
 * 
 * This is the abstract BotStarter class.
 *    We will be creating multiple types of bots
 *    (ones that play the game differently)
 * 
 * Some things to note when interacting with this API.
 *    Lines of four are represented by 2D arrays of Integer objects:
 *    The matrix has dimensions of 4 x 2: i.e. 4 rows of 2 columns.
 *       The first column is the column of the locations in the line
 *       The second column is the row of the locations in the line
 *    Thus, the way someone would find a location in the line would be:
 *       line[i][0], line[i][1]:
 *          where line[i][0] represents the column, and
 *                line[i][1] represents the row
 *       It is kept column first and row second because it makes implementation
 *          easier.
 * 
 * @author Jim van Eeden <jim@starapple.nl>, Joost de Meij <joost@starapple.nl>
 * @author Ryan Pachauri
 * @version 5/12/16
 */

public abstract class BotStarter {
   
   /*
    * 2D int array that keeps track of the location of the discs and who they
    *    belong to:
    *       0: belongs to no one
    *       1: belongs to the player with the id of 1
    *       2: "                                  " 2
    */
   private int[][] field;
   public Map<Integer,Integer> availableMoves;
   public int myID;
   
   public BotStarter(int rows, int columns) {
      this.field = new int[rows][columns];
      this.availableMoves = new HashMap<Integer,Integer>();
   }
   
   /**
    * 
    * @return  int the column to place a disc
    */
   public abstract int makeTurn();
   
   /**
    * @Postcondition:
    *    1. Resets the field based on the data received
    *    2. Resets the availableMoves because there are new possible moves
    * @param s
    */
   public void parse(String s) {
      String[] rows = s.split(";");
      for (int row = 0; row < rows.length; row++) {
         String[] columns = rows[row].split(",");
         for (int col = 0; col < columns.length; col++) {
            this.field[row][col] = Integer.parseInt(columns[col]);
         }
      }
      this.setAvailableMoves();
   }
   
   /**
    * @Precondition: There is at least one open space in the field; otherwise,
    *                   prints out to console that there are no more spaces
    *                availableMoves might be already full
    * @return  A Map of Integers to Integers where
    *             K: the column
    *             V: the row
    */
   private void setAvailableMoves() {
      this.availableMoves.clear();
      for (int i = 0; i < this.field[0].length; i++) {//traverses the first row
         for (int j = 0; j < this.field.length; j++) {//traverses the columns
            if (this.field[j][i] == 0) {//j is row; i is col
               this.availableMoves.put((Integer)i, (Integer) j);
            }
         }
      }
   }
   
   /**
    * 
    * @param row  the location we are looking at: one row above this is the
    *                location we want to know about
    * @param column
    * @return  true if the locatoin above this column is in the position to win
    *             (for either player); otherwise, false
    */
   public boolean belowAPositionToWin(int row, int column, int id) {
      int aboveRow = row - 1;
      return (this.validLocation(aboveRow, column) &&
               this.positionToWin(aboveRow, column, id));
                
   }
   
   /**
    * @Precondition: location given is a valid location; otherwise,
    *                   throws an IllegalArgumentException
    * @param row
    * @param column
    * @param id
    * @return  true, if the given location is a valid location in the
    *             position to win for the player with the given id; otherwise,
    *          false
    */
   public boolean positionToWin(int row, int column, int id) {
      if (!this.validLocation(row, column)) {
         throw new IllegalArgumentException();
      }
      Set<Integer[][]> wins =
            this.getPossibleWins(row, column, id);
      for (Integer[][] line : wins) {
         //number of tokens belonging to player of id equals 3
         //i.e. 3 out of 4 tokens in this line are already in place
         //     the 4th being the available location we are looking at
         if (this.numTokensInLine(line, id) == 3) {
            return true;
         }
      }
      return false;
   }
   
   /**
    * Note: This format is different from others
    *    The indices of the array represent rows while
    *    the list of integers within each index represent columns for that row
    * 
    * This method is used to find all free locations:
    *    i.e.  All locations that have not been occupied by either player
    * 
    * @return  an array of List of columns
    */
   public List<Integer>[] getFreeLocations() {
      @SuppressWarnings("unchecked")
      List<Integer>[] rowsOfFreeLocs = new List[this.field.length];
      for (int row = 0; row < this.field.length; row++) {
         List<Integer> columns = new ArrayList<Integer>();
         for (int col = 0; col < this.field[0].length; col++) {
            if (this.field[row][col] == 0) {
               columns.add(col);
            }
         }
         rowsOfFreeLocs[row] = columns;
      }
      return rowsOfFreeLocs;
   }
   
   /**
    * This method returns a Map (the details of it are laid out below)
    *    The purpose of this Map is to get all possible moves of a player
    *    This includes all moves the player has made in addition to the
    *       available moves for the next turn.
    * If given a 1 or 2,
    *    finds the past moves of that player
    * If given a 0,
    *    finds all free locations
    * 
    * @param id   the id of a player
    * @return  A Map where
    *          K: Integer that represents a column in the field
    *          V: List<Integer> that represents rows in that column
    */
   public Map<Integer, List<Integer>> getLocations(int id) {
      Map<Integer, List<Integer>> moves = new HashMap<Integer, List<Integer>>();
      for (int i = 0; i < this.field[0].length; i++) {//traverses the first row
         List<Integer> rows = new ArrayList<Integer>();
         for (int j = 0; j < this.field.length; j++) {//traverses each column
            if (this.field[j][i] == id) {//[j][i] because j represents rows
               rows.add(j);//gets all moves player has made already
            }
         }
         if (rows.size() > 0) {
            moves.put(i, rows);
         }
      }
      return moves;
   }
   
   
   /**
    * Looks through the line for an available location that is not loc
    *    We are only looking for one available location.
    *    
    * @param line a free location
    * @return  if there is only one available location other than loc,
    *             returns an Integer[] representation of loc
    *          if there are no other available locations or there are more than
    *             one, OR
    *             if there are any free locations that are not available
    *             locations or the given loc,
    *                returns null
    *          
    */
   public Integer[] findAvailableLocation(Integer[][] line, int row, int col) {
      List<Integer> indices = new ArrayList<Integer>();
      for (int i = 0; i < line.length; i++) {
         int lineCol = line[i][0];
         int lineRow = line[i][1];
         if (lineRow != row || lineCol != col ) {
            if (this.availableMoves.containsKey(lineCol) &&
                  this.availableMoves.get(lineCol) == lineRow) {
               indices.add(i);
            } else if (this.field[lineRow][lineCol] == 0) {
               //absolutely no free locations other than available locations
               return null;
            }
         }
      }
      if (indices.size() == 1) {
         return line[indices.get(0)];
      }
      return null;
   }
   
   /**
    * Given a line and the id we are looking at, 
    * 
    * If given a 1 or 2,
    *    finds the number of locations belonging to the player with that id
    * If given a 0,
    *    finds the number of locations belonging to neither player
    * 
    * @param line the line of locations on the board we are looking at
    * @param id   the id of the player we are looking at
    * @return  an int from 0 to 4, depending on the number of tokens
    */
   public int numTokensInLine(Integer[][] line, int id) {
      int sum = 0;
      for (int i = 0; i < line.length; i++) {
         int column = line[i][0];
         int row = line[i][1];
         if (this.field[row][column] == id) {
            sum++;
         }
      }
      return sum;
   }
   
   /**
    * A win is defined as a line of 4 discs from the same player
    *    This line can be:
    *       1. horizontal
    *       2. vertical
    *       3. left diagonal
    *       4. right diagonal
    * 
    * For a location, we only want the wins for a certain player
    * 
    * @Precondition: row and col are in the field and id is a 1 or 2;
    *                otherwise,
    *                   throws an IllegalArgumentException
    * 
    * @param row  the row of the disc we are looking at
    * @param col  the column of the disc we are looking at
    * @param id   the id of a player (must be 1 or 2)
    * @return  a Set of lines belonging to the player with the given id at the
    *          
    */
   public Set<Integer[][]> getPossibleWins(int row, int col, int id) {
      if (!this.validLocation(row, col) || (id != 1 && id != 2)) {
         throw new IllegalArgumentException();
      }
      Set<Integer[][]> wins = this.getPossibleWins(row, col);
      for (Iterator<Integer[][]> i = wins.iterator(); i.hasNext();) {
         Integer[][] line = i.next();
         if (!this.lineBelongsTo(line, id)) {
            i.remove();
         }
      }
      return wins;
   }
   
   /**
    * @Precondition: the line must contain
    *                   1. tokens from only one player or
    *                   2. no tokens at all
    * @param line a line of locations in our field
    * @param id   the id of a player (must be a 1 or 2)
    * @return  if there is a token that belongs to a different player, false
    *          if the line contains only tokens belong to the player with the
    *             given id OR
    *             contains only locations that belong to neither player OR
    *             some combination of the two,
    *             true
    */
   private boolean lineBelongsTo(Integer[][] line, int id) {
      for (int i = 0; i < line.length; i++) {
         int column = line[i][0];
         int row = line[i][1];
         //there is a token in this line that belongs to a different player
         if (this.field[row][column] != id && this.field[row][column] != 0) {
            return false;
         }
      }
      return true;
   }
   
   /**
    * A win is defined as a line of 4 discs from the same player
    *    This line can be:
    *       1. horizontal
    *       2. vertical
    *       3. left diagonal
    *       4. right diagonal
    *    We don't need the id of what we are looking for because the id
    *       is already defined at the location we are looking at.
    *       If it is a 1 or 2, the location belongs to a player.
    *          We include lines of the id or 0
    *       If it is a 0, the location belongs to no one.
    *          We includes lines of only 0 because we are only looking at how
    *          playing at an unplayed location helps either player.
    * 
    * @param row  the row of the disc we are looking at
    * @param col  the column of the disc we are looking at
    * @return A Set of 2D arrays that contain the locations.
    * 
    * The 2D Integer array is explained here:
    *    The rows separate the locations.
    *    The first column is the column of the locations
    *    The second column is the row of the locations
    *       e.g.  01
    *             02
    *             03
    *             04
    */
   public Set<Integer[][]> getPossibleWins(int row, int col) {
      int[][] lines = this.findWinningLines(row, col);
      Set<Integer[][]> wins = new HashSet<Integer[][]>();
      //compares the columns of the horizontal line
      int horizontalLineLength = lines[1][1] - lines[0][1];
      if (horizontalLineLength >= 3) {
         this.addPossibleWins(wins, lines[0][0], lines[0][1], 0, 1,
               horizontalLineLength - 2);
      }
      //compares the rows of the vertical line
      int verticalLineLengeth = lines[2][0] - lines[3][0];
      if (verticalLineLengeth >= 3) {
         this.addPossibleWins(wins, lines[3][0], lines[3][1], 1, 0,
               verticalLineLengeth - 2);
      }
      //compares the columns of the left diagonal line
      int leftDiagonalLine = lines[5][1] - lines[4][1];
      if (leftDiagonalLine >= 3) {
         this.addPossibleWins(wins, lines[4][0], lines[4][1], 1, 1,
               leftDiagonalLine - 2);
      }
      //compares the columns of the right diagonal line
      int rightDiagonalLine = lines[7][1] - lines[6][1];
      if (rightDiagonalLine >= 3) {
         this.addPossibleWins(wins, lines[6][0], lines[6][1], -1, 1,
               rightDiagonalLine - 2);
      }
      return wins;
   }
   
   /**
    * Adds all possible wins in this line to wins
    * @Precondition: wins is already declared
    * 
    * @param wins A Set of 2D Integer arrays containing possible wins
    * @param row  the row of the location we start the line at
    * @param col  the column of the location we start the line at
    * @param rowDiff the direction the row moves in
    * @param colDiff the direction the column moves in
    * @param increment  the number of possible wins there are in this line
    */
   private void addPossibleWins(Set<Integer[][]> wins, int row, int col,
         int rowDiff, int colDiff, int increment) {
      for (int i = 0; i < increment; i++) {
         Integer[][] four = new Integer[4][2];//four in a row
         for (int j = 0; j < 4; j++) {
            four[j][0] = col + colDiff * (i + j);
            four[j][1] = row + rowDiff * (i + j);
         }
         wins.add(four);
      }
   }
   
   /**
    * @Precondition: The location given is a valid location in this field
    * 
    * This denotes the 8 points surrounding the given location so that
    *    we can figure out the lines that make it
    * The set of 8 points are divided into 4 sets of lines
    * The order of the points are as follows:
    *    0. left side of horizontal
    *    1. right side of horizontal
    *    2. top of vertical
    *    3. bottom of vertical
    *    4. top left of left diagonal
    *    5. bottom right of left diagonl
    *    6. bottom left of right diagonal
    *    7. top right of right diagonal
    *    
    * @param row  the row of the location we are considering
    * @param col  the column of the location we are considering
    * @return  int[][] of 
    */
   private int[][] findWinningLines(int row, int col) {
      int[][] lines = new int[8][2];
      lines[0] = this.findMaxDistance(0, -1, row, col);
      lines[1] = this.findMaxDistance(0, 1, row, col);
      lines[2] = this.findMaxDistance(1, 0, row, col);
      lines[3] = this.findMaxDistance(-1, 0, row, col);
      lines[4] = this.findMaxDistance(-1, -1, row, col);
      lines[5] = this.findMaxDistance(1, 1, row, col);
      lines[6] = this.findMaxDistance(1, -1, row, col);
      lines[7] = this.findMaxDistance(-1, 1, row, col);
      return lines;
   }
   
   /**
    * Finds the maximum distance away from a given location that
    *    could make a line of connected discs.
    * We keep incrementing while either:
    *    1. the location contains a disc of the same id as the initial location
    *    2. the location contains no disc
    * We stop incrementing and do not include the location of:
    *    1. a location that is not in the bounds of the field
    *    2. a location containing a disc of a different id as the starting
    *          location
    * 
    * @param rowDiff    how much to increment the row by
    * @param colDiff    "                       " column by
    * @param row        the starting row
    * @param col        the starting column
    * @return  an int[] of size two where:
    *             the first element represents a row
    *             the second element represents a column
    */
   private int[] findMaxDistance(int rowDiff, int colDiff, int row, int col) {
      int counter = 0;
      int[] location = {row,col};
      int id = this.field[row][col];
      while (counter < 4 && this.matchingLocation(row + rowDiff * counter,
            col + colDiff * counter, id)) {
         location[0] = row + rowDiff * counter;
         location[1] = col + colDiff * counter;
         counter++;
      }
      //if we started off with a free location (which would have an id of 0),
      //then we would want to keep going to include the max distance of the
      //other id
      if (id == 0 && this.validLocation(row + rowDiff * counter,
            col + colDiff * counter)) {
         id = this.field[row + rowDiff * counter][col + colDiff * counter];
         while (counter < 4 && this.matchingLocation(row + rowDiff * counter,
               col + colDiff * counter, id)) {
            location[0] = row + rowDiff * counter;
            location[1] = col + colDiff * counter;
            counter++;
         }
      }
      return location;
   }
   
   /**
    * Given an id, return true if this location either contains a disc of the
    *    same id or no disc at all
    *    
    * @param row     row of the location
    * @param column  column of the location
    * @param id   1 or 2, which is a disc that belongs to a player
    *             0       which is a location that belongs to neither player
    * @return  true if the disc at this location matches id or
    *               the location belongs to no one; otherwise,
    *          false
    *               this would indicate that the location is out of bounds or
    *               belongs to a different player
    *          
    */
   private boolean matchingLocation(int row, int column, int id) {
      if (!this.validLocation(row, column)) {
         return false;
      }
      return this.field[row][column] == id || this.field[row][column] == 0;
   }
   
   /**
    * 
    * @param row
    * @param column
    * @return  true if the given location is one that is in our field;
    *          otherwise, false
    * 
    */
   public boolean validLocation(int row, int column) {
      return (row >= 0 && column >= 0 && row < this.field.length &&
            column < this.field[0].length);
   }
   
   /**
    * @Precondition: nums1 and nums2 have the same dimensions
    * @param nums1
    * @param nums2
    * @return  true if the element at each location in nums1 equals
    *                the element at the same location in nums2
    */
   public boolean equal2DArrays(Integer[][] nums1, Integer[][] nums2) {
      for (int i = 0; i < nums1.length; i++) {
         for (int j = 0; j < nums1[0].length; j++) {
            if (nums1[i][j] != (nums2[i][j])) {
               return false;
            }
         }
      }
      return true;
   }
}
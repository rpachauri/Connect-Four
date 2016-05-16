package bot;

import java.util.*;
/**
 * This bot is obsessed with traps.
 *    If this bot can win, it tries to.
 *    If the opponent would win, it tries to prevent it.
 *    If it can set up a trap, it attempts to.
 *    If the opponent can set up a trap, it attempts to block it.
 *    In all other cases, it chooses the column with the best chances of
 *       winning the game.
 * 
 * @author RyanPachauri
 * @version 5/12/16
 */
public class TrapBot extends BotStarter {

   public TrapBot(int rows, int columns) {
      super(rows, columns);
   }

   @Override
   public int makeTurn() {
      int oppID = 3 - this.myID;
      int winColumn = this.scrapeToWin(this.myID);//attempts to win immediately
      if (winColumn != 7) {
         return winColumn;
      }
      winColumn = this.scrapeToWin(oppID);
      if (winColumn != 7) {
         return winColumn;
      }
      this.scrapeAgainstAlmostWins(oppID);
      this.scrapeThreats(oppID);
      this.scrapeMiddleTraps(this.myID);
      this.scrapeMiddleTraps(oppID);
      this.scrapeStackTraps(this.myID);
      this.scrapeStackTraps(oppID);
      return bestAvailableLocation();
   }
   
   /**
    * Looking at the id, finds an available location(s) that could win the game
    *    for the player with that id
    *    i.e.  playing in this column wins the game for the player
    *         
    */
   private int scrapeToWin(int id) {
      for (Integer column : this.availableMoves.keySet()) {
         if (super.positionToWin(this.availableMoves.get(column), column, id)) {
            return column;
         }
      }
      return 7;
   }
   
   /**
    * A player would not want to play in a location that would lead to:
    *    the other player winning OR
    *    the other player preventing the player's win
    * We want to scrape for locations that would avoid this
    *    If there are none, then it does not scrape any locations.
    *    If all available locations are in this situation, then it does not
    *       scrape any locations
    */
   private void scrapeAgainstAlmostWins(int id) {
      Set<Integer> almostWinningColumns = new HashSet<Integer>();
      for (Integer column : this.availableMoves.keySet()) {
         int row = this.availableMoves.get(column);
         if (!super.belowAPositionToWin(row, column, id)) {
            almostWinningColumns.add(column);
         }
      }
      this.scrapeLocations(almostWinningColumns);
   }
   
   /**
    * Finds any middle traps that the player with the given id could set.
    *    A middle trap is where the player's tokens are in the middle of two
    *    options. The player has an option on either side to win.
    * 
    * @param id   the id of a player
    * @return  Set of columns where the player could set stack traps
    */
   private void scrapeMiddleTraps(int id) {
      Set<Integer> middleTrapColumns = new HashSet<Integer>();
      for (Integer column : this.availableMoves.keySet()) {
         //list of other availablelocations that are in possible wins for the
         //available location we are considering
         List<Integer> availableLocs =
               this.getAvailableLocs(this.availableMoves.get(column), column, id);
         //we can only set a middle trap if
         //there are more than 1 other available locs
         if (availableLocs.size() > 1) {
            middleTrapColumns.add(column);
         }
      }
      this.scrapeLocations(middleTrapColumns);
   }
   
   private List<Integer> getAvailableLocs(int row, int column, int id) {
      List<Integer> availableLocs = new ArrayList<Integer>();
      Set<Integer[][]> possibleWins =
            super.getPossibleWins(row, column, id);
      for (Integer[][] line : possibleWins) {
         //finds the available location in the line
         Integer[] freeLoc = super.findAvailableLocation(line, row, column);
         //checks that there is indeed an available location in the line
         //(other than the possible available location given to us)
         if (freeLoc != null) {
            availableLocs.add(freeLoc[0]);
         }
      }
      return availableLocs;
   }
   
   /**
    * Finds any stack traps that the player with the given id could set.
    *    A stack trap is where the player could play in one column and win OR
    *       the opponent plays in that column and the player stacks another
    *       token on top to win anyway.
    *    i.e. The opponent has one of two options:
    *          1. Prevent the player's win in the bottom row
    *             -> This would allow the player to win in the "stacked" row
    *          2. Allow the player's win in the bottom row
    */
   private void scrapeStackTraps(int id) {
      List<Integer>[] freeLocations = super.getFreeLocations();
      for (int row = freeLocations.length - 1; row > 0; row--) {
         Set<Integer> availableColumns = new HashSet<Integer>();
         for (Integer column : freeLocations[row]) {
            if (freeLocations[row - 1].contains(column)) {
               List<Integer> bRowLocs = 
                     this.getAvailableLocs(row, column, id);
               List<Integer> tRowLocs =
                     this.getAvailableLocs(row - 1, column, id);
               if (super.positionToWin(row, column, id)) {
                  for (Integer col : tRowLocs) {
                     availableColumns.add(col);
                  }
               } else if (super.positionToWin(row - 1, column, id)) {
                  for (Integer col : bRowLocs) {
                     availableColumns.add(col);
                  }
               } else {
                  for (Integer bCol : bRowLocs) {
                     for (Integer tCol : tRowLocs) {
                        if (bCol == tCol) {
                           availableColumns.add(bCol);
                        }
                     }
                  }
               }
            }
            this.scrapeLocations(availableColumns);
         }
      }
   }
   
   /**
    * There are some points in the game when it is possible to create threats.
    *    A threat is a possible win of a free location
    *    (that is not an available location).
    *       We will not reach this point if there is an available location that
    *          has a possible win because we will have already returned it at
    *          that point.
    * Threats are stored in a Map using the same format used for available
    *    columns because there can only be one type of threat per player per
    *    column.
    * 
    */
   private void scrapeThreats(int oppID) {
      Map<Integer, Integer> myOddThreats = this.getOddThreats(this.myID);
      Map<Integer, Integer> myEvenThreats = this.getEvenThreats(this.myID);
      Map<Integer, Integer> oppOddThreats = this.getOddThreats(oppID);
      Map<Integer, Integer> oppEvenThreats = this.getEvenThreats(oppID);
      boolean scrapeOkay = true;
      //neither player can have no threats
      if (myOddThreats.isEmpty() && myEvenThreats.isEmpty() &&
            oppOddThreats.isEmpty() && oppEvenThreats.isEmpty()) {
         if (this.myID == 1) {
            this.scrapeToMakeOddThreat(myID);//TODO make this more efficient
            this.scrapeToMakeEvenThreat(myID);
            this.scrapeToMakeOddThreat(oppID);
            this.scrapeToMakeEvenThreat(oppID);
         } else {
            this.scrapeToMakeOddThreat(oppID);
            this.scrapeToMakeEvenThreat(oppID);
            this.scrapeToMakeOddThreat(myID);//TODO make this more efficient
            this.scrapeToMakeEvenThreat(myID);
         }
         scrapeOkay = false;
      }//each player can have at most one type of threat
      else if (!myOddThreats.isEmpty() && !myEvenThreats.isEmpty()) {
         scrapeOkay = false;
      } else if (!oppOddThreats.isEmpty() && !oppEvenThreats.isEmpty()) {
         scrapeOkay = false;
      }
      if (scrapeOkay) {
         if (oppOddThreats.isEmpty() && oppEvenThreats.isEmpty()) {
            this.scrapeForOtherThreat(myOddThreats, myEvenThreats, myID);
         } else if (myOddThreats.isEmpty() && myEvenThreats.isEmpty()) {
            this.scrapeForOtherThreat(oppOddThreats, oppEvenThreats, oppID);
         } else {
            if (this.myID == 1) {
               if (oppOddThreats.size() > 0) {
                  if (myEvenThreats.isEmpty()) {
                     this.scrapeToMakeEvenThreat(myID);
                  } else {
                     this.scrapeAgainstAlmostWins(myID);
                  }
               } else if (oppEvenThreats.size() > 0) {
                  if (myOddThreats.isEmpty()) {
                     this.scrapeToMakeOddThreat(myID);
                  } else {
                     this.scrapeAgainstAlmostWins(myID);
                  }
               }
            } else if (this.myID == 2) {
               if (oppOddThreats.size() > 0) {
                  if (myOddThreats.isEmpty()) {
                     this.scrapeToMakeOddThreat(myID);
                  } else {
                     this.scrapeAgainstAlmostWins(myID);
                  }
               } else if (oppEvenThreats.size() > 0) {
                  if (myEvenThreats.isEmpty()) {
                     this.scrapeToMakeEvenThreat(myID);
                  } else {
                     //this is the one different case
                     this.scrapeAgainstAlmostWins(oppID);
                  }
               }
            }
         }
      }
   }
   
   private Map<Integer, Integer> getOddThreats(int id) {
      List<Integer>[] freeLocs = super.getFreeLocations();
      Map<Integer, Integer> oddThreats = new HashMap<Integer, Integer>();
      for (int row = 1; row < freeLocs.length; row = row + 2) {
         for (Integer column : freeLocs[row]) {
            if (super.positionToWin(row, column, id)) {
               oddThreats.put(column, row);
            }
         }
      }
      return oddThreats;
   }
   
   private Map<Integer, Integer> getEvenThreats(int id) {
      List<Integer>[] freeLocs = super.getFreeLocations();
      Map<Integer, Integer> evenThreats = new HashMap<Integer, Integer>();
      for (int row = 0; row < freeLocs.length; row = row + 2) {
         for (Integer column : freeLocs[row]) {
            if (super.positionToWin(row, column, id)) {
               evenThreats.put(column, row);
            }
         }
      }
      return evenThreats;
   }
   
   /**
    * @Precondition: only one of oddThreats or evenThreats can be empty;
    *                if oddThreats & evenThreats are empty or neither are,
    *                   throws an IllegalArgumentException
    * @param oddThreats
    * @param evenThreats
    * @param id
    */
   private void scrapeForOtherThreat(Map<Integer, Integer> oddThreats,
         Map<Integer, Integer> evenThreats, int id) {
      if ((oddThreats.isEmpty() && evenThreats.isEmpty()) ||
          (!oddThreats.isEmpty() && !evenThreats.isEmpty())) {
         throw new IllegalArgumentException();
      }
      if (oddThreats.isEmpty()) {
         this.scrapeToMakeEvenThreat(id);
      } else if (evenThreats.isEmpty()) {
         this.scrapeToMakeOddThreat(id);
      }
   }
   
   private void scrapeToMakeOddThreat(int id) {
      List<Integer>[] freeLocs = super.getFreeLocations();
      for (int row = freeLocs.length - 1; row >= 0; row = row - 2) {
         Set<Integer> columns = new HashSet<Integer>();
         for (Integer column : freeLocs[row]) {
            List<Integer> availableLocs = this.getAvailableLocs(row, column, id);
            for (Integer availableCol : availableLocs) {
               columns.add(availableCol);
            }
         }
         this.scrapeLocations(columns);
      }
   }
   
   private void scrapeToMakeEvenThreat(int id) {
      List<Integer>[] freeLocs = super.getFreeLocations();
      for (int row = freeLocs.length - 2; row >= 0; row = row - 2) {
         Set<Integer> columns = new HashSet<Integer>();
         for (Integer column : freeLocs[row]) {
            List<Integer> availableLocs = this.getAvailableLocs(row, column, id);
            for (Integer availableCol : availableLocs) {
               columns.add(availableCol);
            }
         }
         this.scrapeLocations(columns);
      }
   }
   
   /**
    * Looks at the available locations and picks the one with the most possible
    *    wins for the given id
    * 
    * @param id   the id of the player we are considering
    * @return  the column of the best location
    */
   private int bestAvailableLocation() {
      int maxWins = -1;
      int maxColumn = -1;
      for (Integer column : this.availableMoves.keySet()) {
         int wins = this.getPossibleWins(this.availableMoves.get(column),
               column).size();
         if (wins > maxWins) {
            maxWins = wins;
            maxColumn = column;
         }
      }
      return maxColumn;
   }
   
   /**
    * @Precondition: columns not null; otherwise,
    *                   throws IllegalArgumentException
    *                
    * @Postcondition:   availableLocations field keyset only contains the given
    *                      columns
    *                   if size of columns is 0, does not change
    *                      availableLocations
    * @param columns the columns we want to keep
    */
   private void scrapeLocations(Set<Integer> columns) {
      if (columns == null) {
         throw new IllegalArgumentException();
      }
      if (columns.size() > 0) {
         for (Iterator<Integer> i = this.availableMoves.keySet().iterator();
               i.hasNext();) {
            Integer column = i.next();
            if (!columns.contains(column)) {
               i.remove();;
            }
         }
      }
   }
}

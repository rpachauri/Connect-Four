# Connect-Four
I have created an artificial intelligence that will play connect four using the engine provided by theaigames.com.
  For more information on the platform, the game, and how the interaction works, please refer to:
    http://theaigames.com/competitions/four-in-a-row

This is the first artificial intelligence I have created and I wanted to start off with Connect Four as it was the easiest game offered
  to code.

Unlike some other artificial intelligences, mine does not look at all future moves in the game and decide the best route to take.
  Instead, it looks at the available moves you could make (a number that could range from 1 to 7)
  and chooses the best one out of them.

It starts off with the simple rules:

  1.  If you can win, win.

  2.  Else if your opponent can win, prevent that win.

From there, it employs a variety of techniques such as the creation of traps and threats to try to pin the opponent.

BotStarter is an abstract class because (in creating TrapBot) I had created multiple types of Bots extending from BotStarter that
  would all implement different strategies. I decided to stick with TrapBot because it seemed to be doing the best.

I will define language used in the comment section here to aid your understanding:

  The private instance variable field is the board. It is a given that any Connect Four game will have 6 rows and 7 columns.

    The field is a 2D array of ints:

      1 represents the location of a token that belongs to player 1

      2 "                                                       " 2

      0 represents a location that belongs to neither player

  The game is won when a player makes a line of four using his or her own tokens. This line can be horizontal, vertical, or diagonal.
    I have defined a "left diagonal" as one that goes from the top left to the bottom right and
                   a "right diagonal" as one that goes from the bottom left to the top right.

  "Free locations" are all locations that have not been chosen by either player
    i.e. the field keeps a 0 in their locations.

  An available location is defined as a free location that a player can make their move on.
    i.e. anywhere you can put your token in immediately

  The private instance variable availableLocations is a Map of Integers to Integers:

    K:  Integer column
    V:  Integer row
    We implement it this way because each column can have exactly 1 or 0 available locations
      We do not include columns that have 0 available locations
      Thus, unless the game is over, available locations will have between 1 and 7 (inclusive) columns to choose from.

  The difference between a trap and a threat:

    A threat is a free location that has the possibility of winning.
    A trap is where two free locations have the possibility of winning.
      Even if the opponent attempts to block one way of winning, the player will win on the next turn using the other way.

This program has received some inspiration from http://www.informatik.uni-trier.de/~fernau/DSL0607/Masterthesis-Viergewinnt.pdf.

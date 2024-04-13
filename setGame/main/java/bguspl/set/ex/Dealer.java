package bguspl.set.ex;

import bguspl.set.Env;
import bguspl.set.ThreadLogger;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.Random;

/**
 * This class manages the dealer's threads and data
 */
public class Dealer implements Runnable {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Game entities.
     */
    private final Table table;
    private final Player[] players;

    /**
     * The list of card ids that are left in the dealer's deck.
     */
    private final List<Integer> deck;

    /**
     * True iff game should be terminated.
     */
    private volatile boolean terminate;

    /**
     * The time when the dealer needs to reshuffle the deck due to turn timeout.
     */
    private long reshuffleTime = Long.MAX_VALUE;
    /**
     * indicates if the set is correct
     */
    private volatile boolean isSet;

    /**
     * queue of players that have a set to check
     */
    protected volatile ArrayBlockingQueue<Integer> playersWithSet;
    /**
     * id of player that have a set to check
     */
    private Integer hasSetToCheck;

    public Dealer(Env env, Table table, Player[] players) {
        this.env = env;
        this.table = table;
        this.players = players;
        deck = IntStream.range(0, env.config.deckSize).boxed().collect(Collectors.toList());
        terminate = false;
        isSet = false;
        playersWithSet = new ArrayBlockingQueue<Integer>( players.length);
    }

    /**
     * The dealer thread starts here (main loop for the dealer thread).
     */
    @Override
    public void run() {
        env.logger.info("thread " + Thread.currentThread().getName() + " starting.");
        placeCardsOnTable();
        for (Player player : players) {
            Thread playerThread = new ThreadLogger(player, Integer.valueOf(player.id).toString(), env.logger);
            playerThread.start();
        }
        while (!shouldFinish()) {
            placeCardsOnTable();
            updateTimerDisplay(true);
            timerLoop();
            removeAllCardsFromTable();
            updateTimerDisplay(true);
        }
        announceWinners();
        env.logger.info("thread " + Thread.currentThread().getName() + " terminated.");
    }

    /**
     * The inner loop of the dealer thread that runs as long as the countdown did
     * not time out.
     */
    private void timerLoop() {
        while (!terminate && System.currentTimeMillis() < reshuffleTime) {
            sleepUntilWokenOrTimeout();
            if (System.currentTimeMillis() < reshuffleTime) {
                removeCardsFromTable();
                placeCardsOnTable();
                updateTimerDisplay(isSet);
                isSet = false;
            }
        }
        if(hasSetToCheck != null){
            try{
                players[hasSetToCheck].dealerWakeup.put(2);
            } catch(InterruptedException ignored){}
            hasSetToCheck = null;
        }
    }

    /**
     * Called when the game should be terminated.
     */
    public void terminate() {
        terminate = true;
        for (int i = players.length - 1; i >= 0; i--) { // terminating each plyer threaad
            players[i].terminate();
        }
    }

    /**
     * Check if the game should be terminated or the game end conditions are met.
     *
     * @return true iff the game should be finished.
     */
    private boolean shouldFinish() { //conditions to finish the game
        List<Integer> cardsOnTable = Arrays.stream(table.slotToCard).filter(Objects::nonNull)
                .collect(Collectors.toList());
        return terminate || (env.util.findSets(deck, 1).size() == 0 && env.util.findSets(cardsOnTable, 1).size() == 0);
    }

    /**
     * Checks cards should be removed from the table and removes them.
     */
    private void removeCardsFromTable() {
        if (hasSetToCheck != null) {
            if (table.playersTokens[hasSetToCheck].size() == env.config.featureSize) { // still has a full set to check
                int[] setToCheck = new int[env.config.featureSize]; // array of slots
                int index = 0;
                for (int slot : table.playersTokens[hasSetToCheck]) {
                    System.out.println("size "+ table.playersTokens[hasSetToCheck].size()+ " "+ index);
                    if (table.slotToCard[slot] == null||index>=env.config.featureSize) {
                        try {
                            players[hasSetToCheck].dealerWakeup.put(2);
                        } catch (InterruptedException ignored) {
                        }
                        return;
                    }
                   
                        setToCheck[index] = table.slotToCard[slot];
                        index++;
                }
                isSet = env.util.testSet(setToCheck);

                if (isSet) {
                        for (int i = 0; i < env.config.featureSize; i++) {
                            for (int j = 0; j < players.length; j++) {
                                if (table.playersTokens[j].contains(table.cardToSlot[setToCheck[i]])) {
                                    players[j].removeTokenFromActionsQueue(table.cardToSlot[setToCheck[i]]); // remove action from player
                                }
                            }
                            if(table.cardToSlot[setToCheck[i]]!=null){
                            table.removeCard(table.cardToSlot[setToCheck[i]]); // remove card from table
                            }
                        }
                }
                if (isSet) {
                    try {
                        players[hasSetToCheck].dealerWakeup.put(1); //is set
                    } catch (InterruptedException ignored) {
                    }
                } else {
                    try {
                        players[hasSetToCheck].dealerWakeup.put(0); //is not a correct set
                    } catch (InterruptedException ignored) {
                    }
                }
            } else {
                    try {
                        players[hasSetToCheck].dealerWakeup.put(2); //is not a full set
                    } catch (InterruptedException ignored) {
                    }
            }
        }
    }

    /**
     * Check if any cards can be removed from the deck and placed on the table.
     */
    private void placeCardsOnTable() {
        if (table.countCards() < env.config.tableSize && deck.size() != 0) {
           synchronized (table) {
                for (int i = 0; i < table.slotToCard.length; i++) {
                    if (table.slotToCard[i] == null && deck.size()>0) {
                        Random rand = new Random();
                        int card = deck.get(rand.nextInt(deck.size()));
                        table.placeCard(card, i);
                        deck.remove(deck.indexOf(card));
                    }
                }
                table.canPutCards=true;
                table.hints();
            }
        }
    }

    /**
     * Sleep for a fixed amount of time or until the thread is awakened for some
     * purpose.
     */
    private void sleepUntilWokenOrTimeout() {
        try {
            if (reshuffleTime - System.currentTimeMillis() <= env.config.turnTimeoutWarningMillis) { // quick timerupdate- in the last 5 seconds
                hasSetToCheck = playersWithSet.poll(0, TimeUnit.MILLISECONDS);
            } else {
                hasSetToCheck = playersWithSet.poll(1000, TimeUnit.MILLISECONDS); //regular timer update
            }
        } catch (InterruptedException interrupt) {
        }
    }

    /**
     * Reset and/or update the countdown and the countdown display.
     */
    private void updateTimerDisplay(boolean reset) {
        if (!reset) { // updating timer
            if (reshuffleTime - System.currentTimeMillis() <= env.config.turnTimeoutWarningMillis) { // show warning in the last 5  seconds
                env.ui.setCountdown(Math.max(reshuffleTime -1- System.currentTimeMillis(), 0), true);
            } else {
                env.ui.setCountdown(Math.max(reshuffleTime -10- System.currentTimeMillis(),0), false); // show regular update timer
            }
        } else { // reset timer
            env.ui.setCountdown(env.config.turnTimeoutMillis - 1000, false);
            updateReshuffleTime();
        }
    }

    /**
     * Returns all the cards from the table to the deck.
     */
    private void removeAllCardsFromTable() {
        if (!terminate) {
             synchronized (table) {
                table.canPutCards=false;
                table.removeAllTokensFromTable();
                for (int i = 0; i < table.slotToCard.length; i++) {
                    if (table.slotToCard[i] != null) {
                        deck.add(table.slotToCard[i]);
                        table.removeCard(i);
                    }
                }
                removeActionsFromPlayers();
            }
        }
    }

    /**
     * Check who is/are the winner/s and displays them.
     */
    private void announceWinners() {
        terminate(); // finish the game
        removeAllCardsFromTable();
        List<Integer> winList = new LinkedList<Integer>();
        int maxScore = -1;
        for (int i = 0; i < players.length; i++) { // finds max score
            if (players[i].score() > maxScore) {
                maxScore = players[i].score();
            }
        }
        for (int i = 0; i < players.length; i++) { // looking for players with max score
            if (players[i].score() == maxScore) {
                winList.add(players[i].id);
            }
        }
        int[] winners = new int[winList.size()]; // creating an array of winners
        int place = 0;
        for (int id : winList) {
            winners[place] = id;
            place++;
        }
        env.ui.announceWinner(winners);
    }

    public void removeActionsFromPlayers() {
        for (Player player : players) {
            player.removeAllActions();
        }
        for (Integer id : playersWithSet) {
            synchronized (players[id].playerThread) {
                try {
                    players[id].dealerWakeup.put(2);
                } catch (InterruptedException ignored) {
                }
            }
        }
        playersWithSet.clear();
    }

    private void updateReshuffleTime() {
        reshuffleTime = System.currentTimeMillis() + env.config.turnTimeoutMillis;
    }
}
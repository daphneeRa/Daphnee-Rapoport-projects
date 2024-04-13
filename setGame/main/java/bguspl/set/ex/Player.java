package bguspl.set.ex;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.Random;
import bguspl.set.Env;

/**
 * This class manages the players' threads and data
 *
 * @inv id >= 0
 * @inv score >= 0
 */
public class Player implements Runnable {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Game entities.
     */
    private final Table table;

    /**
     * The id of the player (starting from 0).
     */
    public final int id;

    /**
     * The thread representing the current player.
     */
    protected Thread playerThread;

    /**
     * The thread of the AI (computer) player (an additional thread used to generate
     * key presses).
     */
    private Thread aiThread;

    /**
     * True iff the player is human (not a computer player).
     */
    private final boolean human;

    /**
     * True iff game should be terminated.
     */
    private volatile boolean terminate;

    /**
     * The current score of the player.
     */
    private int score;
    /**
     * The Dealer that controls the game
     */
    private Dealer dealer;
    /**
     * player's actions
     */
    protected ArrayBlockingQueue<Integer> playersActions;
    /**
     * results of set's check
     */
    protected LinkedBlockingQueue<Integer> dealerWakeup;
    /**
     * indicates whether the player is freezing
     */
    protected volatile boolean isFreezed;
    /**
     * indicates whether the player got point or penatly (-1:default, 0: penatly, 1: point, 2: isn't full set anymore)
     */
    protected volatile int isPointOrPenatly;

    /**
     * The class constructor.
     *
     * @param env              - the environment object.
     * @param dealer           - the dealer object.
     * @param table            - the table object.
     * @param id               - the id of the player.
     * @param human            - true iff the player is a human player (i.e. input is provided manually, via the keyboard).
     * @param playersActions   - actions that the player does.
     * @param isfreezed        - the player freezed or not.
     * @param isPointOrPenatly - the player got point or penatly.
     * 
     */
    public Player(Env env, Dealer dealer, Table table, int id, boolean human) {
        this.env = env;
        this.table = table;
        this.id = id;
        this.human = human;
        this.dealer = dealer;
        this.playersActions = new ArrayBlockingQueue<Integer>(env.config.featureSize);
        this.dealerWakeup = new LinkedBlockingQueue<Integer>();
        this.isFreezed = false;
        this.isPointOrPenatly = -1;
    }

    /**
     * The main player thread of each player starts here (main loop for the player
     * thread).
     */
    @Override
    public void run() {
        playerThread = Thread.currentThread();
        env.logger.info("thread " + Thread.currentThread().getName() + " starting.");
        if (!human)
            createArtificialIntelligence();
        while (!terminate) {
                 try {
                Integer currAction = (playersActions.take());
                if (!table.removeToken(id, currAction)) {
                    if (table.playersTokens[id].size() < env.config.featureSize)
                        table.placeToken(id, currAction);
                }
                if (table.playersTokens[id].size() == env.config.featureSize) {
                    try {
                        dealer.playersWithSet.put(id);
                    } catch (InterruptedException ignored) {
                    }
                    isFreezed = true;
                    
                    try {
                        isPointOrPenatly = dealerWakeup.take(); //waiting for dealer to check
                    } catch (InterruptedException e) {
                    }
                    if (isPointOrPenatly == 1) { // set is correct
                        point();
                    } else if (isPointOrPenatly == 0) { // set isn't correct
                        penalty();
                    } else { // doesn't have a full set anymore
                        isFreezed = false;
                        isPointOrPenatly = -1;
                    }
                }
            } catch (InterruptedException ignored) {
            }
        }
        if (!human)
            try {
                aiThread.join();
            } catch (InterruptedException ignored) {
            }
        env.logger.info("thread " + Thread.currentThread().getName() + " terminated.");
    }

    /**
     * Creates an additional thread for an AI (computer) player. The main loop of
     * this thread repeatedly generates
     * key presses. If the queue of key presses is full, the thread waits until it
     * is not full.
     */
    private void createArtificialIntelligence() {
        // note: this is a very, very smart AI (!)
        aiThread = new Thread(() -> {
            Random rand = new Random();
            env.logger.info("thread " + Thread.currentThread().getName() + " starting.");
            while (!terminate) {
                try {
                    playersActions.offer(rand.nextInt(env.config.tableSize), 10, TimeUnit.MILLISECONDS);
                } catch (InterruptedException ignored) {
                }
            }
            env.logger.info("thread " + Thread.currentThread().getName() + " terminated.");
        }, "computer-" + id);
        aiThread.start();
    }

    /**
     * Called when the game should be terminated.
     */
    public void terminate() {
        terminate = true;
        if (!human) {
            aiThread.interrupt();
        }
        env.logger.info("try to terminate");
        isPointOrPenatly = 2;
        playerThread.interrupt();
        try {
            playerThread.join();
        } catch (InterruptedException e) {
        }
    }

    /**
     * This method is called when a key is pressed.
     *
     * @param slot - the slot corresponding to the key pressed.
     */
    public void keyPressed(int slot) {
        if (!terminate) {
            if (!isFreezed && table.slotToCard[slot] != null) {
                playersActions.offer(slot);
                }
        }
    }

    /**
     * Award a point to a player and perform other related actions.
     *
     * @post - the player's score is increased by 1.
     * @post - the player's score is updated in the ui.
     */
    public void point() {
        if (!terminate) {
            isFreezed = true;
            int ignored = table.countCards(); // this part is just for demonstration in the unit tests
            env.ui.setScore(id, ++score);
            score = score++;
            try {
                for (long i = env.config.pointFreezeMillis; !terminate && i > 0; i = i - 1000) {
                    env.ui.setFreeze(id, i);
                    if (i < 1000) {
                        Thread.sleep(i);
                    } else
                        Thread.sleep(1000);
                }
            } catch (InterruptedException interrupt) {
            }
            env.ui.setFreeze(id, 0);
            isFreezed = false;
            isPointOrPenatly = -1;
        }
    }

    /**
     * Penalize a player and perform other related actions.
     */
    public void penalty() {
        if (!terminate) {
            isFreezed = true;
            try {
                for (long i = env.config.penaltyFreezeMillis; !terminate && i > 0; i = i - 1000) {
                    env.ui.setFreeze(id, i);
                    if (i < 1000) {
                        Thread.sleep(i);
                    } else
                        Thread.sleep(1000);
                }

            } catch (InterruptedException interrupt) {
            }
            env.ui.setFreeze(id, 0);
            isFreezed = false;
            isPointOrPenatly = -1;
        }
    }

    public int score() {
        return score;
    }

    public void removeTokenFromActionsQueue(int slot) { // the method will remove the token from the player's actions queue if it presents
        if (playersActions.contains(slot)) {
            playersActions.remove(slot);
        }
    }

    public void removeAllActions() {
        playersActions.clear();
    }

}

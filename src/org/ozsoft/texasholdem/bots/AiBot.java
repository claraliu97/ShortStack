// This file is part of the 'texasholdem' project, an open source
// Texas Hold'em poker application written in Java.
//
// Copyright 2009 Oscar Stigter
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.ozsoft.texasholdem.bots;

import java.util.List;
import java.util.Set;

import org.ozsoft.texasholdem.Card;
import org.ozsoft.texasholdem.Player;
import org.ozsoft.texasholdem.TableType;
import org.ozsoft.texasholdem.AiUtil;
import org.ozsoft.texasholdem.actions.Action;
import org.ozsoft.texasholdem.actions.BetAction;
import org.ozsoft.texasholdem.actions.RaiseAction;
import org.ozsoft.texasholdem.util.PokerUtils;

/**
 * Basic Texas Hold'em poker bot. <br />
 * <br />
 * 
 * The current implementation acts purely on the bot's hole cards, based on the
 * Chen formula, combined with a configurable level of tightness (when to play
 * or fold a hand ) and aggression (how much to bet or raise in case of good
 * cards or when bluffing). <br />
 * <br />
 * 
 * TODO:
 * <ul>
 * <li>Improve basic bot AI</li>
 * <li>bluffing</li>
 * <li>consider board cards</li>
 * <li>consider current bet</li>
 * <li>consider pot</li>
 * </ul>
 * 
 * @author Oscar Stigter
 */
public class AiBot extends Bot {
    
    /** Tightness (0 = loose, 100 = tight). */
    private final int tightness;
    
    /** Betting aggression (0 = safe, 100 = aggressive). */
    private final int aggression;
    
    /** Table type. */
    private TableType tableType;
    
    /** The hole cards. */
    private Card[] cards;
    
    /**
     * Constructor.
     * 
     * @param tightness
     *            The bot's tightness (0 = loose, 100 = tight).
     * @param aggression
     *            The bot's aggressiveness in betting (0 = careful, 100 =
     *            aggressive).
     */
    public AiBot(int tightness, int aggression) {
        if (tightness < 0 || tightness > 100) {
            throw new IllegalArgumentException("Invalid tightness setting");
        }
        if (aggression < 0 || aggression > 100) {
            throw new IllegalArgumentException("Invalid aggression setting");
        }
        this.tightness = tightness;
        this.aggression = aggression;
    }

    /** {@inheritDoc} */
    @Override
    public void joinedTable(TableType type, int bigBlind, List<Player> players) {
        this.tableType = type;
    }

    /** {@inheritDoc} */
    @Override
    public void messageReceived(String message) {
        // Not implemented.
    }

    /** {@inheritDoc} */
    @Override
    public void handStarted(Player dealer) {
        cards = null;
    }

    /** {@inheritDoc} */
    @Override
    public void actorRotated(Player actor) {
        // Not implemented.
    }

    /** {@inheritDoc} */
    @Override
    public void boardUpdated(List<Card> cards, int bet, int pot) {
        // Not implemented.
    }

    /** {@inheritDoc} */
    @Override
    public void playerUpdated(Player player) {
        if (player.getCards().length == NO_OF_HOLE_CARDS) {
            this.cards = player.getCards();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void playerActed(Player player) {
        // Not implemented.
    }

    /** {@inheritDoc} */
    @Override
    public Action act(int minBet, int currentBet, Set<Action> allowedActions, Card[] commuCards) {
        Action action = null;
        if (allowedActions.size() == 1) {
            // No choice, must check.
            action = Action.CHECK;
        } else {
        	int len = 0;
        	for (Card c: commuCards) {
        		if (c!=null) {len += 1;}
        	}
        	
            double callScore = AiUtil.cutoff(len)[0];
            double betScore = AiUtil.cutoff(len)[1];
            
            Card[] allCards = new Card[len+2];
            allCards[0] = cards[0];
            allCards[1] = cards[1];
            for(int i=2; i<len+2; i++) {
            	allCards[i] = commuCards[i-2];
            }
            double Score = AiUtil.eval(allCards);
            System.out.printf(AiUtil.printCards(allCards)+":%.4f\n",Score);
            
            if ((Score<callScore)) {
                if (allowedActions.contains(Action.CHECK)) {
                    // Always check for free if possible.
                    action = Action.CHECK;
                } else {
                    // Bad hole cards; play tight.
                    action = Action.FOLD;
                }
            } else {
                // Good enough hole cards, play hand.
                if (Score>=betScore) {
                    // Very good hole cards; bet or raise!
                    if (aggression == 0) {
                        // Never bet.
                        if (allowedActions.contains(Action.CALL)) {
                            action = Action.CALL;
                        } else {
                            action = Action.CHECK;
                        }
                    } else if (aggression == 100) {
                        // Always go all-in!
                        //FIXME: Check and bet/raise player's remaining cash.
                        int amount = (tableType == TableType.FIXED_LIMIT) ? minBet : 100 * minBet;
                        if (allowedActions.contains(Action.BET)) {
                            action = new BetAction(amount);
                        } else if (allowedActions.contains(Action.RAISE)) {
                            action = new RaiseAction(amount);
                        } else if (allowedActions.contains(Action.CALL)) {
                            action = Action.CALL;
                        } else {
                            action = Action.CHECK;
                        }
                    } else {
                        int amount = minBet;
                        if (tableType == TableType.NO_LIMIT) {
                            int betLevel = aggression / 20;
                            for (int i = 0; i < betLevel; i++) {
                                amount *= 2;
                            }
                        }
                        if (currentBet < amount) {
                            if (allowedActions.contains(Action.BET)) {
                                action = new BetAction(amount);
                            } else if (allowedActions.contains(Action.RAISE)) {
                                action = new RaiseAction(amount);
                            } else if (allowedActions.contains(Action.CALL)) {
                                action = Action.CALL;
                            } else {
                                action = Action.CHECK;
                            }
                        } else {
                            if (allowedActions.contains(Action.CALL)) {
                                action = Action.CALL;
                            } else {
                                action = Action.CHECK;
                            }
                        }
                    }
                } else {
                    // Decent hole cards; check or call.
                    if (allowedActions.contains(Action.CHECK)) {
                        action = Action.CHECK;
                    } else {
                        action = Action.CALL;
                    }
                }
            }
        }
        return action;
    }
    
}

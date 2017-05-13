package org.ozsoft.texasholdem;

import org.ozsoft.texasholdem.util.PokerUtils;

public class AiUtil {
	
	//Percentage of the role that unknown cards play in evaluating a hand
	private static final double flopRate = 0.5; 
	private static final double riverRate = 0.25; 
	
	public static String printCards(Card[] cards) {
		String s = "";
		for (Card c: cards) {
			s += c.toString()+" ";
		}
		return s;
	}
	
	/**Input: two hole cards and three flop cards
	 * Output: the expectation of the final value of the current hand
	 */
	public static double flopEval(Card[] flopCards) {
		Hand knownHand = new Hand(flopCards);
		HandEvaluator knownEval = new HandEvaluator(knownHand);
		int knownScore = knownEval.getType().getValue();
		
		Card[] cards = new Card[7];
		for (int i=0;i<5;i++) {
			cards[i] = flopCards[i];
		}
		double futureScore = 0;
		int count = 0;
		for (int s1=0; s1<4; s1++) {
			for (int r1=0; r1<13; r1++){
				Card new1 = new Card(r1,s1);
				if (repeat(cards,new1)) {}
				else {
					cards[5] = new1;
					for (int s2=0; s2<4; s2++) {
						for (int r2=0; r2<13; r2++){
							Card new2 = new Card(r2,s2);
							if (repeat(cards,new2)) {}
							else {
								cards[6] = new2;
								Hand testHand = new Hand(cards);
								HandEvaluator eval = new HandEvaluator(testHand);
								futureScore += eval.getType().getValue();
								count += 1;
							}
						}
					}
				}
			}
		}
		double score = (1-flopRate)*knownScore + flopRate*futureScore/count;
		return score;
	}
	
	/**Input: two hole cards, three flop cards and one turn card
	 * Output: the expectation of the final value of the current hand
	 */
	public static double turnEval(Card[] flopCards) {
		Hand knownHand = new Hand(flopCards);
		HandEvaluator knownEval = new HandEvaluator(knownHand);
		int knownScore = knownEval.getType().getValue();
		
		Card[] cards = new Card[7];
		for (int i=0;i<6;i++) {
			cards[i] = flopCards[i];
		}
		double futureScore = 0;
		int count = 0;
		for (int s2=0; s2<4; s2++) {
			for (int r2=0; r2<13; r2++){
				Card new2 = new Card(r2,s2);
				if (repeat(cards,new2)) {}
				else {
					cards[6] = new2;
					Hand testHand = new Hand(cards);
					HandEvaluator eval = new HandEvaluator(testHand);
					futureScore += eval.getType().getValue();
					count += 1;
				}
			}
		}	
		double score = (1-riverRate)*knownScore + riverRate*futureScore/count;
		return score;
	}
	
	//Check if Card c is in Card[] cards
	public static boolean repeat(Card[] cards, Card c) {
		int suit = c.getSuit();
		int rank = c.getRank();
		for (Card card:cards) {
			if ((card!=null)&&card.getSuit()==suit && card.getRank()==rank)
				return true;
		}
		return false;
	}
	
	/**The overall eval function for a hand at pre-flop, flop, turn and river
	 * @param cards: community cards + hole cards
	 * @return the evaluated value of the hand
	 */
	public static double eval(Card[] cards) {
		int len = cards.length;
		if(len==2) {
			return PokerUtils.getChenScore(cards);
		} else if(len==5) {
			return flopEval(cards);
		} else if(len==6) {
			return turnEval(cards);
		} else if(len==7) {
			Hand testHand = new Hand(cards);
			HandEvaluator eval = new HandEvaluator(testHand);
			return eval.getType().getValue();
		} else {
			return 0;
		}
	}
	
	/**Returns the two evaluation cutoffs to tell how the ai should act
	 * @param num: the number of community cards
	 * @return [cut1,cut2]: cut1 is the callScore (higher than this can call)
	 * cut 2 is the betScore (high than this can bet)
	 */
	public static int[] cutoff(int num) {
		int[][] cutoffs = new int[6][2];
		cutoffs[0][0] = 4;
		cutoffs[0][1] = 7;
		cutoffs[3][0] = 2;
		cutoffs[3][1] = 3;
		cutoffs[4][0] = 2;
		cutoffs[4][1] = 3;
		cutoffs[5][0] = 2;
		cutoffs[5][1] = 3;
		return cutoffs[num];
	}
	
	public static void main(String args[]) {
		Card[] cards = new Card [5];
		cards[1] = new Card("As");
		cards[2] = new Card("Ks");
		cards[3] = new Card("Qs");
		cards[4] = new Card("Js");
		cards[0] = new Card("Th");
		
		System.out.printf("flop score: %.4f\n",flopEval(cards));
		
		Card[] cards2 = new Card [6];
		for (int i=0; i<5; i++) {
			cards2[i] = cards[i];
		}
		cards2[5] = new Card("2s");
		
		System.out.printf("turn score: %.4f\n",turnEval(cards2));
		
	}

}
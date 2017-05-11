package org.ozsoft.texasholdem;


public class Test {
	
	//Percentage of the role that unknown cards play in evaluating a hand
	private static final double flopRate = 0.5; 
	private static final double riverRate = 0.25; 
	
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
	
	/**Input: two hole cards, three flop cards and one river card
	 * Output: the expectation of the final value of the current hand
	 */
	public static double riverEval(Card[] flopCards) {
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
	
	
	public static boolean repeat(Card[] cards, Card c) {
		int suit = c.getSuit();
		int rank = c.getRank();
		for (Card card:cards) {
			if ((card!=null)&&card.getSuit()==suit && card.getRank()==rank)
				return true;
		}
		return false;
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
		
		System.out.printf("river score: %.4f\n",riverEval(cards2));
		
	}

}
package org.ozsoft.texasholdem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.ozsoft.texasholdem.util.PokerUtils;

public class AiUtil {
	
	//Percentage of the role that unknown cards play in evaluating a hand
	private static final double flopRate = 0.1; 
	private static final double riverRate = 0.05; 
	
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
		double knownScore = getValue(knownEval);
		
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
								if (getValue(eval)>knownScore) {
									futureScore += getValue(eval);
									count += 1;
								}
							}
						}
					}
				}
			}
		}
		double score = count==0?knownScore:((1-flopRate)*knownScore + flopRate*futureScore/count);
		return score;
	}
	
	/**Input: two hole cards, three flop cards and one turn card
	 * Output: the expectation of the final value of the current hand
	 */
	public static double turnEval(Card[] flopCards) {
		Hand knownHand = new Hand(flopCards);
		HandEvaluator knownEval = new HandEvaluator(knownHand);
		double knownScore = getValue(knownEval);
		
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
					if (getValue(eval)>knownScore) {
						futureScore += getValue(eval);
						count += 1;
					}
				}
			}
		}	
		double score = count==0?knownScore:((1-riverRate)*knownScore + riverRate*futureScore/count);
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
			return getValue(eval);
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
		int[][] cutoffs =  {{4,7},
				  			{0,0},
							{0,0},
							{2,3},
							{2,3},
							{2,3}};
		return cutoffs[num];
	}
	
	/** Returns how good a hand is according to the machine learning result
	 * 0 = bad, 1 = decent, 2 = good
	 * @param cards
	 * @return
	 */
	public static int level(Card[] cards) {
		double[] w0 = new double[] {0,0,0};
		double[] w1 = new double[] {0,0,0};
		Perceptron p1 = new Perceptron(w0[0],new double[] {w0[1],w0[2]});
		Perceptron p2 = new Perceptron(w1[0],new double[] {w1[1],w1[2]});
		double whole = eval(cards);
		double hole = eval(new Card[] {cards[0],cards[1]});
		if (p1.Output(new double[] {hole,whole})==0) {
			return 0;
		} else if(p2.Output(new double[] {hole,whole})==1) {
			return 2;
		} else {
			return 1;
		}
	}
	
	public static Perceptron cutoffLearning (String filename) {
		Perceptron p = new Perceptron(0,new double[] {0,0});
		try(BufferedReader br = new BufferedReader(new FileReader(filename))) {
		    String line = br.readLine();
		    int size = Integer.parseInt(line);
		    int[] outputs = new int[size];
		    double[][] inputs = new double[size][2];
		    
		    for (int i=0; i<size; i++) {
		    	line = br.readLine();
		    	String[] thisline = line.split(" ");
		    	int len = thisline.length;
		    	int y = Integer.parseInt(thisline[len-1]);
		    	Card[] cards = new Card[len-1];
		    	for (int j=0; j<len-1; j++) {
		    		cards[j] = new Card(thisline[j]);
		    	}
		    	
		    	double whole = eval(cards);
		    	double hole = eval(new Card[] {cards[0],cards[1]});
		    	
		    	outputs[i] = y;
		    	inputs[i] = new double[] {hole,whole};
		    	
		    	System.out.printf("%.0f %.2f %d "+printCards(cards)+"\n",hole,whole,y);
		    }

			p.Train(inputs, outputs, 0.5, 2000000);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return p;
	}
	
	public static double getValue(HandEvaluator eval) {
		return eval.getType().getValue()*eval.containsTopP;
	}
	
	public static void main(String args[]) {
		Card[] cards = new Card [5];
		cards[0] = new Card("Ts");
		cards[1] = new Card("Tc");
		cards[2] = new Card("As");
		cards[3] = new Card("Ac");
		cards[4] = new Card("4d");
		
		System.out.printf("flop score: %.4f\n",flopEval(cards));
		Hand testHand = new Hand(cards);
		HandEvaluator eval = new HandEvaluator(testHand);
		System.out.printf("TP=%d\n",eval.containsTopP);
		
		Card[] cards2 = new Card [6];
		for (int i=0; i<5; i++) {
			cards2[i] = cards[i];
		}
		cards2[5] = new Card("2s");
		
		//System.out.printf("turn score: %.4f\n",turnEval(cards2));
		Perceptron haha = cutoffLearning("ShortStack_data12.txt");
		//System.out.println(haha.Output(new double[] {0,0}));
	}

}
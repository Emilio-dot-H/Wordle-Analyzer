import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class WordleAnalyze {
	private char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray();
	
	protected Scanner scAnswers;
	protected Scanner scGuesses;
	
	protected ArrayList<String> answers;
	protected ArrayList<String> guesses;

	protected ArrayList<Double> guessScore;
	//words ranked by score in order
	ArrayList<WordScore> wordRanked;
	ArrayList<ArrayList<FqAnalysis>> zipfRank;
	
	//Strength of word stats
	protected double inGreen;
	protected double inYellow;
	
	//sort letters by positions and by frequency in position
	protected ArrayList<HashMap<Character, Integer>> letterPosition;
	public HashMap<Character, Integer> firstPosition;
	public HashMap<Character, Integer> secondPosition;
	public HashMap<Character, Integer> thirdPosition;
	public HashMap<Character, Integer> fourthPosition;
	public HashMap<Character, Integer> fifthPosition;
	
	//overall data
	public HashMap<Character, Integer> overallFrequency;
	public HashMap<Character, Double> overallScore;
	
	//attribute a score based of frequency in a position
	protected ArrayList<HashMap<Character, Double>> letterScore = new ArrayList<HashMap<Character, Double>>();
	public HashMap<Character, Double> firstScore = new HashMap<Character, Double>();
	public HashMap<Character, Double> secondScore = new HashMap<Character, Double>();
	public HashMap<Character, Double> thirdScore = new HashMap<Character, Double>();
	public HashMap<Character, Double> fourthScore = new HashMap<Character, Double>();
	public HashMap<Character, Double> fifthScore = new HashMap<Character, Double>();
	
	static class Main{
		public static void main(String[] args) throws FileNotFoundException {
			//1) create instance of the class
			WordleAnalyze e = new WordleAnalyze();
			System.out.println(e.letterScore);
			System.out.println(e.overallFrequency);
			System.out.println(e.overallScore);
//			e.greyLetter("soadclint");
//			e.greenLetter('e', 5);
//			e.greenLetter('i', 2);
//			e.greenLetter('l', 4);
//			e.greenLetter('y', 5);
			e.getZipfRank(5);
//			e.printBreakdown("skiff");
			e.getRank(50);
			
			
		}
	}
	public WordleAnalyze() throws FileNotFoundException{
		fileTransfer("wordle-answers-alphabetical.txt"
		,"wordle-allowed-guesses.txt");
		
		this.guessScore = new ArrayList<Double>();
		this.zipfRank = new ArrayList<ArrayList<FqAnalysis>>();
		
		this.letterPosition = new ArrayList<HashMap<Character, Integer>>();
		this.firstPosition = new HashMap<Character, Integer>();
		this.secondPosition = new HashMap<Character, Integer>();
		this.thirdPosition = new HashMap<Character, Integer>();
		this.fourthPosition = new HashMap<Character, Integer>();
		this.fifthPosition = new HashMap<Character, Integer>();
		
		letterPosition.add(firstPosition);
		letterPosition.add(secondPosition);
		letterPosition.add(thirdPosition);
		letterPosition.add(fourthPosition);
		letterPosition.add(fifthPosition);
		
		this.overallFrequency = new HashMap<Character, Integer>();
		this.overallScore = new HashMap<Character, Double>();
		
		while(scAnswers.hasNext()) {
			//System.out.println(sc.next());
			answers.add(scAnswers.next());
		}
		while(scGuesses.hasNext()) {
			guesses.add(scGuesses.next());
		}
		guesses.addAll(answers);
		
		//WORDLE ANALYSIS
		//1) count frequency of letters in each position
		countLetters();
		//2) attach a score to each letter based off frequency in position
		setScore();
		//3) Rank words by summing up the total score of their letters based at each position
		rankByPowerOfFreqOfPos();
		
	}
	public void fileTransfer(String answersFile, String guessesFile) throws FileNotFoundException {
		this.scAnswers = new Scanner(new File(answersFile));
		this.answers = new ArrayList<String>();
		this.scGuesses = new Scanner(new File(guessesFile));
		this.guesses = new ArrayList<String>();
	}
	public ArrayList<String> getAnswers(){
		return answers;
	}
	public ArrayList<String> getGuesses() {
		return guesses;
	}
	//counts every letters occurrence at a given location.
	public void countLetters() {
		//filling up overall counter with zeros
		for(int i = 0; i<alphabet.length; i++) {
			overallFrequency.put(alphabet[i], 0);
		}
		countLetters(0);
		countLetters(1);
		countLetters(2);
		countLetters(3);
		countLetters(4);
	}
	/*
	 * takes the letterPosition list that contains a hashmap for each position in
	 * the 5 letter word. Given a location args, fetches the hashmap for that 
	 * location and counts the every letters occurrence at that position.*/
	public void countLetters(int location) {
		HashMap<Character, Integer> position = letterPosition.get(location);
		for(int i = 0; i < answers.size(); i++) {
			String word = answers.get(i);
			char letter = word.charAt(location);
			if(!position.containsKey(letter)) {
				position.put(letter, 1);
				overallFrequency.put(letter, overallFrequency.get(letter) + 1);
			}
			else if(position.containsKey(letter)) {
				position.put(letter, position.get(letter) + 1);
				overallFrequency.put(letter, overallFrequency.get(letter) + 1);
			}
		}
	}

	//calculates the max, and assigns a score rank from highest to lowest fq
	public void scoreMax(HashMap<Character, Integer> position, HashMap<Character, Double> scoreMap) {
		ArrayList<FqAnalysis> zipf = new ArrayList<FqAnalysis>();
		Object[] fq = position.values().toArray();
		double totalFq = 0;
		for(int i = 0; i<fq.length;i++) {
			totalFq += new Double(fq[i].toString());
		}
		double rank = 1.0;
		double score = 0.0;
		char letter = ' ';
		int max = 0;
		int count = 0;
		
		while(count < position.size()) {
			for(int i = 0; i < alphabet.length; i++) {
				//if the letter is in the position map and the frq is greater than max
				//AND it's not added to the score map yet.
				if(position.containsKey(alphabet[i]) && 
						position.get(alphabet[i]) > max &&
						!scoreMap.containsKey(alphabet[i])) {
					letter = alphabet[i];
					max = position.get(alphabet[i]);	
				}
			}
			double relativeFreq = (max)/totalFq;
			double zipfScore = 1/(rank + relativeFreq);
			score += zipfScore;
			scoreMap.put(letter, score);
			zipf.add(new FqAnalysis(Character.toString(letter), zipfScore, relativeFreq, score));
			//reset
			count++;
			rank++;
			max = 0;
			letter = ' ';
		}
		zipfRank.add(zipf);
	}
	public void setScore() {
		positionalScore();
		scoreMax(overallFrequency, overallScore);
		
	}
	public void positionalScore() {
		letterScore.add(firstScore);
		letterScore.add(secondScore);
		letterScore.add(thirdScore);
		letterScore.add(fourthScore);
		letterScore.add(fifthScore);
		for(int pos = 0; pos < letterPosition.size(); pos++) {
			//double totalLetters = answers.size();
			HashMap<Character, Integer> position = letterPosition.get(pos);
			HashMap<Character, Double> scoreMap = letterScore.get(pos);
			scoreMax(position, scoreMap);
		}
	}
	public void rankByPowerOfFreqOfPos() {
		this.wordRanked = new ArrayList<WordScore>();
		for(int i = 0; i < guesses.size(); i++) {
			double guessSc = 0;
			String fullWord = guesses.get(i);
			char[] word = fullWord.toCharArray();
			for(int letter = 0; letter < word.length; letter++) {
				//search for dups
				for(int j = word.length -1; j > letter; j--) {
					//5 point demerit for duplicate values
					if(word[letter] == word[j]) {
						guessSc += 5;
					}
				}
				//green: correct letter in correct spot
				if(letterScore.get(letter).containsKey(word[letter])) {
					guessSc += letterScore.get(letter).get(word[letter]);
				}
				//overall occurence of the letter throughout the library
				if(overallScore.containsKey(word[letter])) {
					guessSc += overallScore.get(word[letter]);
				}
				//filter for grey letters
				if(!letterScore.get(letter).containsKey(word[letter])) {
					guessSc += 100;
				}
				//filter for green letters
				if(letterScore.get(letter) == null) {
					guessSc += 100;
				}
				
			}
			wordRanked.add(new WordScore(fullWord, guessSc));
			guessScore.add(guessSc);
		}
		Collections.sort(wordRanked);
	}
	public void greyLetter(String word) {
		for(int i = 0; i < word.length(); i++) {
			overallScore.remove(word.charAt(i));
			for(int j = 0; j<letterScore.size(); j++) {
				if(letterScore.get(j).containsKey(word.charAt(i))) {
					letterScore.get(j).remove(word.charAt(i));
				}
			}
		}rankByPowerOfFreqOfPos();
	}
	public void greenLetter(char letter, int position) {
		position -= 1;
		for(int i = 0; i < alphabet.length; i++) {
			if(alphabet[i] != letter) {
				letterScore.get(position).remove(alphabet[i]);
			}
		}
		rankByPowerOfFreqOfPos();
	}
	public void getRank() {
		int rank = 1;
		for (WordScore entity : wordRanked) {
	        System.out.println(rank+") "+entity.word + " => " +"Score: "+ String.format("%.2f", entity.score) +
	        		" | greenYield: "+ String.format("%.2f",strengthOfWord(entity.word)[0]) + "% | yellowYield: "+
	        		String.format("%.2f",strengthOfWord(entity.word)[1])+"%");
			rank++;
		}
	}
	public void getRank(int limit) {
		int rank = 1;
		for (WordScore entity : wordRanked) {
			if(rank > limit) break;
	        System.out.println(rank+") "+entity.word + " => " +"Score: "+ String.format("%.2f", entity.score) +
	        		" | greenYield: "+ String.format("%.2f",strengthOfWord(entity.word)[0]) + "% | yellowYield: "+
	        		String.format("%.2f",strengthOfWord(entity.word)[1])+"%");
			rank++;
		}
		
	}
	public void getZipfRank(int x) {
		int rank = 1;
		for (FqAnalysis entity : zipfRank.get(x)) {
			
	        System.out.println(rank+") "+ entity.word + " => " +"score: " + String.format("%.3f", entity.overallScore) + 
	        		", zipf: "+ String.format("%.3f",entity.zipf) + ", relFrq: " + String.format("%.3f",entity.relFq));
			rank++;
		}
	}
	public double[] strengthOfWord(String w) {
		this.inGreen = 0;
		this.inYellow = 0;
		char[] word = w.toCharArray();
		for(int letter = 0; letter<word.length; letter++) {
			for(int ans = 0; ans<answers.size(); ans++) {
				if(answers.get(ans).charAt(letter) == word[letter]){
					inGreen++;
				}
				if(!(answers.get(ans).charAt(letter) == word[letter]) &&
					answers.get(ans).contains(Character.toString(word[letter]))) {
					char[] answer = answers.get(ans).toCharArray();
					for(char c:answer) {
						if(c == word[letter]) {
							inYellow++;
						}
					}
				}
			}
		}
		double[] colors = new double[2];
		colors[0] = (inGreen/(answers.size()*5))*100;
		colors[1] = (inYellow/(answers.size()*5))*100;
		return colors;
	}
	public String[] breakdown(String w) {
		String [] result = new String[21];
		double greenPr = strengthOfWord(w)[0]/100;
		double yellowPr = strengthOfWord(w)[1]/100;
		//grey only
		double onlyGreyP = Math.pow((1-greenPr), 5)*Math.pow((1-yellowPr), 5);
		//solo green
		double oneGreenP = (greenPr)*Math.pow((1-greenPr), 4)*5*Math.pow((1-yellowPr), 5);
		double twoGreenP = Math.pow(greenPr, 2)*Math.pow((1-greenPr), 3)*10*Math.pow((1-yellowPr), 5);
		double threeGreenP = Math.pow(greenPr, 3)*Math.pow((1-greenPr), 2)*10*Math.pow((1-yellowPr), 5);
		double fourGreenP = Math.pow(greenPr, 4)*Math.pow((1-greenPr), 1)*5*Math.pow((1-yellowPr), 5);
		double allGreenP = Math.pow(greenPr, 5)*5*Math.pow((1-yellowPr), 5);
		//solo yellow
		double oneYellowP = (yellowPr)*Math.pow((1-yellowPr), 4)*5*Math.pow((1-greenPr), 5);
		double twoYellowP = Math.pow(yellowPr, 2)*Math.pow((1-yellowPr), 3)*10*Math.pow((1-greenPr), 5);
		double threeYellowP = Math.pow(yellowPr, 3)*Math.pow((1-yellowPr), 2)*10*Math.pow((1-greenPr), 5);
		double fourYellowP = Math.pow(yellowPr, 4)*Math.pow((1-yellowPr), 1)*5*Math.pow((1-greenPr), 5);
		double allYellowP = Math.pow(yellowPr, 5)*5*Math.pow((1-yellowPr), 5);
		//combo
		double oneGreenOneYellowP = (greenPr)*Math.pow((1-greenPr), 4)*(yellowPr)*Math.pow((1-yellowPr), 4)*10;
		double oneGreenTwoYellowP = (greenPr)*Math.pow((1-greenPr), 4)*Math.pow(yellowPr, 2)*Math.pow((1-yellowPr), 3)*10;
		double oneGreenThreeYellowP = (greenPr)*Math.pow((1-greenPr), 4)*Math.pow(yellowPr, 3)*Math.pow((1-yellowPr), 2)*5;
		double oneGreenFourYellowP = (greenPr)*Math.pow((1-greenPr), 4)*Math.pow(yellowPr, 4)*Math.pow((1-yellowPr), 1)*5;
		double twoGreenOneYellowP = Math.pow(greenPr, 2)*Math.pow((1-greenPr), 3)*Math.pow(yellowPr, 1)*Math.pow((1-yellowPr), 4)*10;
		double twoGreenTwoYellowP = Math.pow(greenPr, 2)*Math.pow((1-greenPr), 3)*Math.pow(yellowPr, 2)*Math.pow((1-yellowPr), 3)*10;
		double twoGreenThreeYellowP = Math.pow(greenPr, 2)*Math.pow((1-greenPr), 3)*Math.pow(yellowPr, 3)*Math.pow((1-yellowPr), 2)*10;
		double threeGreenOneYellowP = Math.pow(greenPr, 3)*Math.pow((1-greenPr), 2)*Math.pow(yellowPr, 1)*Math.pow((1-yellowPr), 4)*10;
		double threeGreenTwoYellowP = Math.pow(greenPr, 3)*Math.pow((1-greenPr), 2)*Math.pow(yellowPr, 2)*Math.pow((1-yellowPr), 3)*10;
		double fourGreenOneYellowP = Math.pow(greenPr, 4)*Math.pow((1-greenPr), 1)*Math.pow(yellowPr, 1)*Math.pow((1-yellowPr), 4)*5;

		



		
		String allGrey = "⬜" + "⬜" + "⬜" + "⬜" + "⬜ : "+  String.format("%.2f", onlyGreyP*100)+"%";
		String oneGreen = "🟩" + "⬜" + "⬜" + "⬜" + "⬜ : "+ String.format("%.2f", oneGreenP*100)+"%";
		String twoGreen = "🟩" + "🟩" + "⬜" + "⬜" + "⬜ : "+ String.format("%.2f", twoGreenP*100)+"%";
		String threeGreen = "🟩" + "🟩" + "🟩" + "⬜" + "⬜ : "+ String.format("%.2f", threeGreenP*100)+"%";
		String fourGreen = "🟩" + "🟩" + "🟩" + "🟩" + "⬜ : "+ String.format("%.2f", fourGreenP*100)+"%";
		String allGreen = "🟩" + "🟩" + "🟩" + "🟩" + "🟩 : "+ String.format("%.4f", allGreenP*100)+"%";
		
		String oneYellow = "🟨" + "⬜" + "⬜" + "⬜" + "⬜ : "+ String.format("%.2f", oneYellowP*100)+"%";
		String twoYellow = "🟨" + "🟨" + "⬜" + "⬜" + "⬜ : "+ String.format("%.2f", twoYellowP*100);
		String threeYellow = "🟨" + "🟨" + "🟨" + "⬜" + "⬜ : "+ String.format("%.2f", threeYellowP*100)+"%";
		String fourYellow = "🟨" + "🟨" + "🟨" + "🟨" + "⬜ : "+ String.format("%.2f", fourYellowP*100)+"%";
		String allYellow = "🟨" + "🟨" + "🟨" + "🟨" + "🟨 : "+ String.format("%.2f", allYellowP*100)+"%";
		
		String oneGreenOneYellow = "🟩" + "🟨" + "⬜" + "⬜" + "⬜ : "+ String.format("%.2f", oneGreenOneYellowP*100)+"%";
		String oneGreenTwoYellow = "🟩" + "🟨" + "🟨" + "⬜" + "⬜ : "+ String.format("%.2f", oneGreenTwoYellowP*100)+"%";
		String oneGreenThreeYellow = "🟩" + "🟨" + "🟨" + "🟨" + "⬜ : "+ String.format("%.2f", oneGreenThreeYellowP*100)+"%";
		String oneGreenFourYellow = "🟩" + "🟨" + "🟨" + "🟨" + "🟨 : "+ String.format("%.2f", oneGreenFourYellowP*100)+"%";
		String twoGreenOneYellow = "🟩" + "🟩" + "🟨" + "⬜" + "⬜ : "+ String.format("%.2f", twoGreenOneYellowP*100)+"%";
		String twoGreenTwoYellow = "🟩" + "🟩" + "🟨" + "🟨" + "⬜ : "+ String.format("%.2f", twoGreenTwoYellowP*100)+"%";
		String twoGreenThreeYellow = "🟩" + "🟩" + "🟨" + "🟨" + "🟨 : "+ String.format("%.2f", twoGreenThreeYellowP*100)+"%";
		String threeGreenOneYellow = "🟩" + "🟩" + "🟩" + "🟨" + "⬜ : "+ String.format("%.2f", threeGreenOneYellowP*100)+"%";
		String threeGreenTwoYellow = "🟩" + "🟩" + "🟩" + "🟨" + "🟨 : "+ String.format("%.2f", threeGreenTwoYellowP*100)+"%";
		String fourGreenOneYellow = "🟩" + "🟩" + "🟩" + "🟩" + "🟨 : "+ String.format("%.3f", fourGreenOneYellowP*100)+"%";



		
		result[0] = allGrey;
		result[1] = oneGreen;
		result[2] = twoGreen;
		result[3] = threeGreen;
		result[4] = fourGreen;
		result[5] = allGreen;
		
		result[6] = oneYellow;
		result[7] = twoYellow;
		result[8] = threeYellow;
		result[9] = fourYellow;
		result[10] = allYellow;
		
		result[11] = oneGreenOneYellow;
		result[12] = oneGreenTwoYellow;
		result[13] = oneGreenThreeYellow;
		result[14] = oneGreenFourYellow;
		result[15] = twoGreenOneYellow;
		result[16] = twoGreenTwoYellow;
		result[17] = twoGreenThreeYellow;
		result[18] = threeGreenOneYellow;
		result[19] = threeGreenTwoYellow;
		result[20] = fourGreenOneYellow;
		
		return result;
		
	}
	public void printBreakdown(String w) {
		String [] br = breakdown(w);
		for(int i = 0; i < br.length; i++) {
			System.out.println(br[i]);
		}
	}

}

class WordScore implements Comparable<WordScore> {
    String word;
    double score;
    WordScore(String word, double score) {
        this.word = word;
        this.score = score;
    }
    @Override
    public int compareTo(WordScore o) {
        if (this.score > o.score)
            return 1;
        else if (this.score < o.score)
            return -1;
        return 0;
    }
}
class FqAnalysis implements Comparable<FqAnalysis> {
    String word;
    double zipf;
    double relFq;
    double overallScore;
    
    FqAnalysis(String word, double zipf, double relFq, double overallScore) {
        this.word = word;
        this.zipf = zipf;
        this.relFq = relFq;
        this.overallScore= overallScore;
    }
    @Override
    public int compareTo(FqAnalysis o) {
        if (this.overallScore > o.overallScore)
            return 1;
        else if (this.overallScore < o.overallScore)
            return -1;
        return 0;
    }
}



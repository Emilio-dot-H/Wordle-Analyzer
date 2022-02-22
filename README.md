### Table of Contents

 1. [Introduction](#introduction) 
 2. [Letter Frequency](#letter-frq)
 3. [Power Law & Zipf’s Law](#power)
 4. [Data Initialization](#data-init) 
 5. [The Algorithm](#alg)
    1. [Counting the frequency of letters](#count-fq)
    2. [Attaching a score to each letter](#attch-score)
    3. [Ranking words by summing up their overall letter scores](#rank-words)
4. [Observations](#observations)
    1. [Strength of word - Green & Yellow Yields](#strength-words)
    2. [Overall score, Zipf ranking and relative frequency of letters](#overall-score)
    3. [Score, Zipf ranking and relative frequency of letters in each position](#position-score)
5. [Top 5 Words](#top5)
6. [Works Cited](#biblio)



---


## Introduction <a name="introduction"></a>

This document will cover a computer program in Java that is designed specifically with Wordle in mind. The algorithm will dissect, observe, and rank letter frequencies in the realm of Wordle. Wordle is a web-based word game entailing 30 empty tiles into one big box. The objective of the game is to guess the five-letter word ‘password’ in six tries or less with allotted feedback after every guess. The hint based feedback is in the form of colored tiles, a green tile represents the correct letter in the correct position. A yellow tile represents the correct letter in the incorrect position. Wordle has a single daily solution, with all players attempting to guess the same word. Thus, it has started being referred to by many experts as a gimmick that has attracted the public eye, and much like many great gimmicks; niches, subcultures, and sub-genres are quick to follow suit. From it stems an interesting point of debate and analysis. Can such a format be statistically broken down and does it adhere to the standard power laws that much larger systems confound too via their nature. 

---

## Letter Frequency <a name="letter-frq"></a>

Before diving into the algorithm, it’s important to distinguish the data. Letter frequency analysis is an important tool to consider when you are trying to model the behaviour of letters among words. While many rankings already exist such as entries in the Concise Oxford dictionairy: _“eariotnslcudpmhgbfywkvxzjq”_, or a list which cites Robert Lewand’s Cryptological Mathematics: _“etaoinshrdlcumwfgypbvkjxqz”_.

There are important distinguishments to be made, letter rankings are relative to their pool. While some letters would hold a higher value given that you are analysing biblical texts they might not hold up given the analysis of modern english texts. This same philosophy applies to the length of words, frequency of letters will vary depending on whether you are observing three letter words, four letter words, five letter words, etc.. In the case of the Wordle word pool, we are dealing with a curated list of five letter words in a relatively limited bank of up to 12972 words (more or less with NYTimes’ reductions), some of which are not technically considered English words in the traditional sense. Thus, in order to break down the letter patterns in Wordle it’s important to acknowledge how words in general operate in terms of letter frequency distribution.


---


## Power Law & Zipf’s Law <a name="power"></a>

In statistics, a power law is a functional relationship between two quantities, where a relative change in one quantity results in a proportional relative change in the other quantity, independent of the initial size of those quantities: one quantity varies as a power of another. Think of a kingdom as an analogy, the power law dynamics that exist between the ruler of a kingdom versus a townsperson is proportional to the said relationship, thus a relative change to the rulership will have a much higher impact on the townsman's livelihood then if the situation was reversed. 

This is where Zipf’s law comes in, an empirical law formulated using mathematical statistics that refers to the fact that for many types of data studied in the physical and social sciences, the rank-frequency distribution is an inverse relation. 

The law may also be written: 
![Zipf Equation](https://imgur.com/lVBsAWM)



In its simplest form Zipf’s law is a function of 1/f. The core concept behind the Zipfian distribution is that the frequency of any word is inversely proportional to its rank. Thus the most frequent word will occur approximately twice as often as the second most frequent word, three times as often as the third most frequent word, etc. This natural observation occurs in many statistical situations, including the frequency of letters. Zipf will be the basis of our ranking system.

---

## Data Initialization <a name="data-init"></a>


```
// Character array of the alphabet for reference.
private char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray();
// List of valid Wordle answers and guesses.
protected ArrayList<String> answers;
protected ArrayList<String> guesses;

//Words ranked by score in order
ArrayList<WordScore> wordRanked;
ArrayList<ArrayList<FqAnalysis>> zipfRank;

//Strength of word statistics to measure strength
protected double inGreen;
protected double inYellow;
	
//Sort letters by positions and by frequency in position
protected ArrayList<HashMap<Character, Integer>> letterPosition;
public HashMap<Character, Integer> firstPosition;
public HashMap<Character, Integer> secondPosition;
public HashMap<Character, Integer> thirdPosition;
public HashMap<Character, Integer> fourthPosition;
public HashMap<Character, Integer> fifthPosition;
	
//Overall letter data and frequency statistics
public HashMap<Character, Integer> overallFrequency;
public HashMap<Character, Double> overallScore;
	
//Attribute a score based on frequency and zipf law in a given position
protected ArrayList<HashMap<Character, Double>> letterScore = new ArrayList<HashMap<Character, Double>>();
public HashMap<Character, Double> firstScore = new HashMap<Character, Double>();
public HashMap<Character, Double> secondScore = new HashMap<Character, Double>();
public HashMap<Character, Double> thirdScore = new HashMap<Character, Double>();
public HashMap<Character, Double> fourthScore = new HashMap<Character, Double>();
public HashMap<Character, Double> fifthScore = new HashMap<Character, Double>();
```

---

## The Algorithm <a name="alg"></a>

The Wordle analysis can be divided into various subsections:



* Counting the frequency of letters in each position and overall.
* Attaching a score to each letter based on its relative frequency and a Zipfian metric.
* Ranking words by summing up their overall letter scores.


### Counting the frequency of letters <a name="count-fq"></a>

Given a list of hashmaps for each letter position in the 5 letter word. Count the frequency of each letter at a given position and track the frequency as a value associated with the key letter in a hashmap. This will allow us easy access to the frequency count of any letter at any position.
```
public void countLetters(int location) { 
	HashMap&lt;Character, Integer> position = letterPosition.get(location); 
		for(int i = 0; i &lt; answers.size(); i++) { 
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
```


### Attaching a score to each letter <a name="attch-score"></a>

Perhaps the most crucial function in the program. The following sifts through every hashmap that contains a key letter and frequency value for all five positions as well as the overall frequency of a letter and attaches a score ranking that increments as we go from more frequent to less frequent. The score calculation is influenced by Zipf’s law but uses the relative frequency of a letter to shift the ranking.
![Formula - Zipf+RelFq](https://imgur.com/SEx3w5q), where r is the rank and beta is the relative frequency of of the letter.
```
public void scoreMax(HashMap&lt;Character, Integer> position, HashMap&lt;Character, Double> scoreMap) { 
	ArrayList&lt;FqAnalysis> zipf = new ArrayList&lt;FqAnalysis>(); 
	Object[] fq = position.values().toArray(); 
	double totalFq = 0; 
	for(int i = 0; i&lt;fq.length;i++) { 
		totalFq += new Double(fq[i].toString()); 
	} 
	double rank = 1.0; 
	double score = 0.0; 
	char letter = ' '; 
	int max = 0; 
	int count = 0; 
		 \
	while(count &lt; position.size()) { 
		for(int i = 0; i &lt; alphabet.length; i++) { 
//If the letter is in the position map & the fq is greater than max
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
		zipf.add(new FqAnalysis(Character.toString(letter), 
                     zipfScore, relativeFreq, score)); 
		<em>//reset</em> 
		count++; 
		rank++; 
		max = 0; 
		letter = ' '; 
	} 
	zipfRank.add(zipf); 
}
```



### Ranking words by summing up their overall letter scores <a name="rank-words"></a>

The last step of the process is going through every possible answer in the Wordle list of answers and tallying up the score of each letter based on it’s positional score and overall score. The optimal words are expected to have a lower score than the less optimal words. This algorithm also adds a penalty to repeat letters as it is more ideal to cover a larger range of unique letters. Double letters might skew the system if there is a high yield letter that is getting repeated.

```
public void rankByPowerOfFreqOfPos() { 
	this.wordRanked = new ArrayList&lt;WordScore>(); 
	for(int i = 0; i &lt; guesses.size(); i++) { 
		double guessSc = 0; 
		String fullWord = guesses.get(i); 
		char[] word = fullWord.toCharArray(); 
		for(int letter = 0; letter &lt; word.length; letter++) { 
			<em>//search for dups</em> 
			for(int j = word.length -1; j > letter; j--) { 
				//5 point demerit for duplicate values
				if(word[letter] == word[j]) { 
					guessSc += 5; 
				} 
			} 
			//green: correct letter in correct spot\
			if(letterScore.get(letter).containsKey(word[letter])) { 
					guessSc +=letterScore.get(letter).get(word[letter]); 
				} 
			//overall occurence of the letter throughout the library
			if(overallScore.containsKey(word[letter])) { 
				guessSc += overallScore.get(word[letter]); 
			} 
			//filter for grey letters
			if(!letterScore.get(letter).containsKey(word[letter])) { 
				guessSc += 100; 
			} \
			//filter for green letters
			if(letterScore.get(letter) == null) { 
				guessSc += 100; 
			} 
			 
		} 
		wordRanked.add(new WordScore(fullWord, guessSc)); \
		guessScore.add(guessSc); 
	} 
	Collections.sort(wordRanked); 
}
```

---

## Observations <a name="observations"></a>

The top 30 best starter words in Wordle based on letter frequency analysis. It’s important to note that the green and yellow yields are the likelihood that a green or yellow tile is drawn at any given position of the word. The ranking doesn't solely rely on those yields and does a good job weighing positional impact and overall frequency.

**The top 30 best starter words in Wordle based on letter frequency analysis.** 

1) soare  →	Score: 15.04 | greenYield: 13.20% | yellowYield: 24.41%

2) saine  →	Score: 15.33 | greenYield: 13.32% | yellowYield: 21.06%

3) crane  →	Score: 15.59 | greenYield: 11.90% | yellowYield: 22.69%

4) caret  →	Score: 15.96 | greenYield: 10.68% | yellowYield: 24.86%

5) slane  →	Score: 16.11 | greenYield: 12.79% | yellowYield: 22.07%

6) salet  →	Score: 16.15 | greenYield: 11.69% | yellowYield: 24.01%

7) saner  →	Score: 16.20 | greenYield: 11.57% | yellowYield: 24.34%

8) coate  →	Score: 16.28 | greenYield: 11.64% | yellowYield: 22.66%

9) crate  →	Score: 16.36 | greenYield: 11.53% | yellowYield: 24.24%

10) carse  →	Score: 16.41 | greenYield: 10.89% | yellowYield: 24.05%

11) stane  →	Score: 16.42 | greenYield: 11.71% | yellowYield: 23.21%

12) raine  →	Score: 16.44 | greenYield: 11.07% | yellowYield: 25.23%

13) brane  →	Score: 16.48 | greenYield: 11.69% | yellowYield: 21.39%

14) saice  →	Score: 16.50 | greenYield: 13.06% | yellowYield: 20.42%

15) taler  →	Score: 16.57 | greenYield: 9.46% | yellowYield: 27.93%

16) cater  →	Score: 16.58 | greenYield: 9.87% | yellowYield: 25.53%

17) carle  →	Score: 16.59 | greenYield: 10.81% | yellowYield: 24.47%

18) stare  →	Score: 16.64 | greenYield: 11.46% | yellowYield: 26.22%

19) raise  →	Score: 16.65 | greenYield: 10.97% | yellowYield: 25.85%

20) trace  →	Score: 16.69 | greenYield: 11.22% | yellowYield: 24.59%

21) arose  →	Score: 16.69 | greenYield: 10.77% | yellowYield: 26.39%

22) roate  →	Score: 16.70 | greenYield: 10.83% | yellowYield: 27.13%

23) stale  →	Score: 16.82 | greenYield: 11.54% | yellowYield: 24.25%

24) raile  →	Score: 16.83 | greenYield: 10.89% | yellowYield: 26.26%

25) arise  →	Score: 16.88 | greenYield: 10.96% | yellowYield: 25.94%

26) slate  →	Score: 16.88 | greenYield: 12.41% | yellowYield: 23.63%

27) trone  →	Score: 16.89 | greenYield: 10.94% | yellowYield: 23.52%

28) caner  →	Score: 16.89 | greenYield: 10.12% | yellowYield: 24.03%

29) sore  →	Score: 16.92 | greenYield: 11.08% | yellowYield: 24.03%

30) crone  →	Score: 16.96 | greenYield: 11.36% | yellowYield: 20.98%


### Strength of word - Green & Yellow Yields {#strength-of-word-green-&-yellow-yields} <a name="strength-words"></a>

A function designed to compare a certain word’s letters to every other list of words. It tracks the frequency of its letters in a given position as well as the overall frequency compared to all words in the list and draws a yield metric. 


```
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
```



### Overall score, zipf ranking and relative frequency of letters <a name="overall-score"></a>

1) e → score: 0.904, zipf: 0.904, relFrq: 0.107	2) a → score: 1.383, zipf: 0.480, relFrq: 0.085

3) r → score: 1.708, zipf: 0.325, relFrq: 0.078	4) o → score: 1.954, zipf: 0.246, relFrq: 0.065

5) t → score: 2.152, zipf: 0.198, relFrq: 0.063	6) l → score: 2.317, zipf: 0.165, relFrq: 0.062

7) i → score: 2.459, zipf: 0.142, relFrq: 0.058	8) s → score: 2.583, zipf: 0.124, relFrq: 0.058

9) n → score: 2.693, zipf: 0.111, relFrq: 0.050	10) c → score: 2.793, zipf: 0.100, relFrq: 0.041

11) u → score: 2.883, zipf: 0.091, relFrq: 0.040	12) y → score: 2.966, zipf: 0.083, relFrq: 0.037

13) d → score: 3.043, zipf: 0.077, relFrq: 0.034	14) h → score: 3.114, zipf: 0.071, relFrq: 0.034

15) p → score: 3.181, zipf: 0.067, relFrq: 0.032	16) m → score: 3.243, zipf: 0.062, relFrq: 0.027

17) g → score: 3.302, zipf: 0.059, relFrq: 0.027	18) b → score: 3.357, zipf: 0.055, relFrq: 0.024

19) f → score: 3.410, zipf: 0.053, relFrq: 0.020	20) k → score: 3.460, zipf: 0.050, relFrq: 0.018

21) w => score: 3.508, zipf: 0.048, relFrq: 0.017	22) v → score: 3.553, zipf: 0.045, relFrq: 0.013

23) z → score: 3.596, zipf: 0.043, relFrq: 0.003	24) x → score: 3.638, zipf: 0.042, relFrq: 0.003

25) q → score: 3.678, zipf: 0.040, relFrq: 0.003	26) j → score: 3.717, zipf: 0.038, relFrq: 0.00


### Score, zipf ranking and relative frequency of letters in each position <a name="position-score"></a>


<table>
  <tr>
   <td><strong>Score, zipf, and relative frequency of letters in 1st position</strong>
   </td>
   <td><strong>Score, zipf, and relative frequency of letters in 2nd position</strong>
   </td>
   <td><strong>Score, zipf, and relative frequency of letters in 3rd position</strong>
   </td>
   <td><strong>Score, zipf, and relative frequency of letters in 4th position</strong>
   </td>
   <td><strong>Score, zipf, and relative frequency of letters in 5th position</strong>
   </td>
  </tr>
  <tr>
   <td>1) s → score: 0.863, zipf: 0.863, relFrq: 0.158
<p>
2) c→ score: 1.343, zipf: 0.479, relFrq: 0.086
<p>
3) b → score: 1.668, zipf: 0.325, relFrq: 0.075
<p>
4) t → score: 1.914, zipf: 0.246, relFrq: 0.064
<p>
5) p → score: 2.112, zipf: 0.198, relFrq: 0.061
<p>
6) a → score: 2.277, zipf: 0.165, relFrq: 0.061
<p>
7) f → score: 2.418, zipf: 0.142, relFrq: 0.059
<p>
8) g → score: 2.543, zipf: 0.124, relFrq: 0.050
<p>
9) d → score: 2.653, zipf: 0.111, relFrq: 0.048
<p>
10) m→ score: 2.753, zipf: 0.100, relFrq: 0.046
<p>
11) r → score: 2.843, zipf: 0.091, relFrq: 0.045
<p>
12) l → score: 2.926, zipf: 0.083, relFrq: 0.038
<p>
13) w → score: 3.003, zipf: 0.077, relFrq: 0.036
<p>
14) e → score: 3.074, zipf: 0.071, relFrq: 0.031
<p>
15) h → score: 3.141, zipf: 0.067, relFrq: 0.030
<p>
16) v → score: 3.203, zipf: 0.062, relFrq: 0.019
<p>
17) o → score: 3.262, zipf: 0.059, relFrq: 0.018
<p>
18) n → score: 3.318, zipf: 0.056, relFrq: 0.016
<p>
19) i → score: 3.370, zipf: 0.053, relFrq: 0.015
<p>
20) u → score: 3.420, zipf: 0.050, relFrq: 0.014
<p>
21) q → score: 3.468, zipf: 0.048, relFrq: 0.010
<p>
22) j → score: 3.513, zipf: 0.045, relFrq: 0.009
<p>
23) k → score: 3.557, zipf: 0.043, relFrq: 0.009
<p>
24) y → score: 3.598, zipf: 0.042, relFrq: 0.003
<p>
25) z → score: 3.638, zipf: 0.040, relFrq: 0.001
   </td>
   <td>1) a → score: 0.884, zipf: 0.884, relFrq: 0.131
<p>
2) o → score: 1.356, zipf: 0.472, relFrq: 0.121
<p>
3) r → score: 1.677, zipf: 0.321, relFrq: 0.115
<p>
4) e → score: 1.920, zipf: 0.244, relFrq: 0.105
<p>
5) i → score: 2.117, zipf: 0.197, relFrq: 0.087
<p>
6) l → score: 2.281, zipf: 0.164, relFrq: 0.087
<p>
7) u → score: 2.422, zipf: 0.141, relFrq: 0.080
<p>
8) h → score: 2.546, zipf: 0.124, relFrq: 0.062
<p>
9) n → score: 2.657, zipf: 0.111, relFrq: 0.038
<p>
10) t → score: 2.757, zipf: 0.100, relFrq: 0.033
<p>
11) p → score: 2.847, zipf: 0.091, relFrq: 0.026
<p>
12) w → score: 2.930, zipf: 0.083, relFrq: 0.019
<p>
13) c → score: 3.007, zipf: 0.077, relFrq: 0.017
<p>
14) m → score: 3.079, zipf: 0.071, relFrq: 0.016
<p>
15) y → score: 3.145, zipf: 0.067, relFrq: 0.010
<p>
16) d → score: 3.208, zipf: 0.062, relFrq: 0.009
<p>
17) b → score: 3.267, zipf: 0.059, relFrq: 0.007
<p>
18) s → score: 3.322, zipf: 0.056, relFrq: 0.007
<p>
19) v → score: 3.375, zipf: 0.053, relFrq: 0.006
<p>
20) x → score: 3.425, zipf: 0.050, relFrq: 0.006
<p>
21) g → score: 3.472, zipf: 0.048, relFrq: 0.005
<p>
22) k → score: 3.518, zipf: 0.045, relFrq: 0.004
<p>
23) f → score: 3.561, zipf: 0.043, relFrq: 0.003
<p>
24) q → score: 3.603, zipf: 0.042, relFrq: 0.002
<p>
25) j → score: 3.643, zipf: 0.040, relFrq: 0.001
<p>
26) z → score: 3.681, zipf: 0.038, relFrq: 0.001
   </td>
   <td>1) a → score: 0.883, zipf: 0.883, relFrq: 0.133
<p>
2) i → score: 1.356, zipf: 0.473, relFrq: 0.115
<p>
3) o → score: 1.678, zipf: 0.322, relFrq: 0.105
<p>
4) e → score: 1.923, zipf: 0.245, relFrq: 0.076
<p>
5) u → score: 2.120, zipf: 0.197, relFrq: 0.071
<p>
6) r → score: 2.285, zipf: 0.165, relFrq: 0.070
<p>
7) n → score: 2.427, zipf: 0.142, relFrq: 0.060
<p>
8) l → score: 2.551, zipf: 0.124, relFrq: 0.048
<p>
9) t → score: 2.661, zipf: 0.111, relFrq: 0.048
<p>
10) s → score: 2.761, zipf: 0.100, relFrq: 0.035
<p>
11) d → score: 2.852, zipf: 0.091, relFrq: 0.032
<p>
12) g→ score: 2.935, zipf: 0.083, relFrq: 0.029
<p>
13) m → score: 3.012, zipf: 0.077, relFrq: 0.026
<p>
14) p → score: 3.083, zipf: 0.071, relFrq: 0.025
<p>
15) b → score: 3.149, zipf: 0.067, relFrq: 0.025
<p>
16) c → score: 3.212, zipf: 0.062, relFrq: 0.024
<p>
17) v → score: 3.271, zipf: 0.059, relFrq: 0.021
<p>
18) y → score: 3.326, zipf: 0.056, relFrq: 0.013
<p>
19) w → score: 3.379, zipf: 0.053, relFrq: 0.011
<p>
20) f → score: 3.429, zipf: 0.050, relFrq: 0.011
<p>
21) k → score: 3.476, zipf: 0.048, relFrq: 0.005
<p>
22) x → score: 3.522, zipf: 0.045, relFrq: 0.005
<p>
23) z → score: 3.565, zipf: 0.043, relFrq: 0.005
<p>
24) h → score: 3.607, zipf: 0.042, relFrq: 0.004
<p>
25) j → score: 3.647, zipf: 0.040, relFrq: 0.001
<p>
26) q → score: 3.685, zipf: 0.038, relFrq: 0.000
   </td>
   <td>1) e → score: 0.879, zipf: 0.879, relFrq: 0.137
<p>
2) n → score: 1.360, zipf: 0.481, relFrq: 0.079
<p>
3) s → score: 1.686, zipf: 0.325, relFrq: 0.074
<p>
4) a → score: 1.931, zipf: 0.246, relFrq: 0.070
<p>
5) l → score: 2.129, zipf: 0.197, relFrq: 0.070
<p>
6) i → score: 2.293, zipf: 0.165, relFrq: 0.068
<p>
7) c → score: 2.435, zipf: 0.142, relFrq: 0.066
<p>
8) r → score: 2.559, zipf: 0.124, relFrq: 0.066
<p>
9) t → score: 2.669, zipf: 0.110, relFrq: 0.060
<p>
10) o → score: 2.769, zipf: 0.099, relFrq: 0.057
<p>
11) u → score: 2.859, zipf: 0.091, relFrq: 0.035
<p>
12) g → score: 2.942, zipf: 0.083, relFrq: 0.033
<p>
13) d → score: 3.019, zipf: 0.077, relFrq: 0.030
<p>
14) m → score: 3.090, zipf: 0.071, relFrq: 0.029
<p>
15) k → score: 3.157, zipf: 0.067, relFrq: 0.024
<p>
16) p → score: 3.219, zipf: 0.062, relFrq: 0.022
<p>
17) v → score: 3.278, zipf: 0.059, relFrq: 0.020
<p>
18) f → score: 3.334, zipf: 0.056, relFrq: 0.015
<p>
19) h → score: 3.386, zipf: 0.053, relFrq: 0.012
<p>
20) w → score: 3.436, zipf: 0.050, relFrq: 0.011
<p>
21) b → score: 3.484, zipf: 0.048, relFrq: 0.010
<p>
22) z → score: 3.529, zipf: 0.045, relFrq: 0.009
<p>
23) x → score: 3.573, zipf: 0.043, relFrq: 0.001
<p>
24) y → score: 3.614, zipf: 0.042, relFrq: 0.001
<p>
25) j→ score: 3.654, zipf: 0.040, relFrq: 0.001
   </td>
   <td>1) e → score: 0.845, zipf: 0.845, relFrq: 0.183
<p>
2) y → score: 1.309, zipf: 0.464, relFrq: 0.157
<p>
3) t → score: 1.630, zipf: 0.322, relFrq: 0.109
<p>
4) r → score: 1.875, zipf: 0.244, relFrq: 0.092
<p>
5) l → score: 2.072, zipf: 0.197, relFrq: 0.067
<p>
6) h → score: 2.237, zipf: 0.165, relFrq: 0.060
<p>
7) n → score: 2.379, zipf: 0.142, relFrq: 0.056
<p>
8) d → score: 2.503, zipf: 0.124, relFrq: 0.051
<p>
9) k → score: 2.614, zipf: 0.111, relFrq: 0.049
<p>
10) a → score: 2.713, zipf: 0.100, relFrq: 0.028
<p>
11) o → score: 2.804, zipf: 0.091, relFrq: 0.025
<p>
12) p → score: 2.887, zipf: 0.083, relFrq: 0.024
<p>
13) m → score: 2.964, zipf: 0.077, relFrq: 0.018
<p>
14) g → score: 3.035, zipf: 0.071, relFrq: 0.018
<p>
15) s → score: 3.102, zipf: 0.067, relFrq: 0.016
<p>
16) c → score: 3.164, zipf: 0.062, relFrq: 0.013
<p>
17) f → score: 3.223, zipf: 0.059, relFrq: 0.011
<p>
18) w → score: 3.279, zipf: 0.056, relFrq: 0.007
<p>
19) b → score: 3.331, zipf: 0.053, relFrq: 0.005
<p>
20) i → score: 3.381, zipf: 0.050, relFrq: 0.005
<p>
21) x → score: 3.429, zipf: 0.048, relFrq: 0.003
<p>
22) z → score: 3.474, zipf: 0.045, relFrq: 0.002
<p>
23) u → score: 3.518, zipf: 0.043, relFrq: 0.000
   </td>
  </tr>
</table>

---

## Top 5 Words <a name="top5"></a>

A brief dissection of the 5 most optimal first word guesses that you can use in Wordle determined by the Wordle program analysis. Soare, saine, crane, caret, slane, and notable mentions for salet and saner. These words differ by a score of no more than 1.07 and are all exceptionally efficient and interchangeable. It comes down to preference as certain words will yield more yellow than green. Greens in general are low yields but high reward tiles so it comes down to what your preferences are. 

The following is a breakdown of some of the top words. It is important to note that the green and yellow tiles in these depictions are not ordered, the positioning can vary.


<table>
  <tr>
   <td><strong>SOARE</strong>
   </td>
   <td><strong>SAINE</strong>
   </td>
   <td><strong>CRANE</strong>
   </td>
   <td><strong>CARET</strong>
   </td>
  </tr>
  <tr>
   <td>
<p>
⬜⬜⬜⬜⬜ : 12.16%
<p>
🟩⬜⬜⬜⬜ : 9.24%
<p>
🟩🟩⬜⬜⬜ : 2.81%
<p>
🟩🟩🟩⬜⬜ : 0.43%
<p>
🟩🟩🟩🟩⬜ : 0.03%
<p>
🟩🟩🟩🟩🟩 : 0.0049%
<p>
🟨⬜⬜⬜⬜ : 19.63%
<p>
🟨🟨⬜⬜⬜ : 12.68
<p>
🟨🟨🟨⬜⬜ : 4.10%
<p>
🟨🟨🟨🟨⬜ : 0.66%
<p>
🟨🟨🟨🟨🟨 : 0.11%
<p>
🟩🟨⬜⬜⬜ : 5.97%
<p>
🟩🟨🟨⬜⬜ : 1.93%
<p>
🟩🟨🟨🟨⬜ : 0.31%
<p>
🟩🟨🟨🟨🟨 : 0.10%
<p>
🟩🟩🟨⬜⬜ : 0.91%
<p>
🟩🟩🟨🟨⬜ : 0.29%
<p>
🟩🟩🟨🟨🟨 : 0.09%
<p>
🟩🟩🟩🟨⬜ : 0.14%
<p>
🟩🟩🟩🟨🟨 : 0.04%
<p>
🟩🟩🟩🟩🟨 : 0.01%
   </td>
   <td>
<p>
⬜⬜⬜⬜⬜ : 15.00%
<p>
🟩⬜⬜⬜⬜ : 11.52%
<p>
🟩🟩⬜⬜⬜ : 3.54%
<p>
🟩🟩🟩⬜⬜ : 0.54%
<p>
🟩🟩🟩🟩⬜ : 0.04%
<p>
🟩🟩🟩🟩🟩 : 0.0064%
<p>
🟨⬜⬜⬜⬜ : 20.01%
<p>
🟨🟨⬜⬜⬜ : 10.68
<p>
🟨🟨🟨⬜⬜ : 2.85%
<p>
🟨🟨🟨🟨⬜ : 0.38%
<p>
🟨🟨🟨🟨🟨 : 0.06%
<p>
🟩🟨⬜⬜⬜ : 6.15%
<p>
🟩🟨🟨⬜⬜ : 1.64%
<p>
🟩🟨🟨🟨⬜ : 0.22%
<p>
🟩🟨🟨🟨🟨 : 0.06%
<p>
🟩🟩🟨⬜⬜ : 0.95%
<p>
🟩🟩🟨🟨⬜ : 0.25%
<p>
🟩🟩🟨🟨🟨 : 0.07%
<p>
🟩🟩🟩🟨⬜ : 0.15%
<p>
🟩🟩🟩🟨🟨 : 0.04%
<p>
🟩🟩🟩🟩🟨 : 0.01%
   </td>
   <td>
<p>     
⬜⬜⬜⬜⬜ : 14.66%
<p>
🟩⬜⬜⬜⬜ : 9.90%
<p>
🟩🟩⬜⬜⬜ : 2.68%
<p>
🟩🟩🟩⬜⬜ : 0.36%
<p>
🟩🟩🟩🟩⬜ : 0.02%
<p>
🟩🟩🟩🟩🟩 : 0.0033%
<p>
🟨⬜⬜⬜⬜ : 21.50%
<p>
🟨🟨⬜⬜⬜ : 12.62
<p>
🟨🟨🟨⬜⬜ : 3.70%
<p>
🟨🟨🟨🟨⬜ : 0.54%
<p>
🟨🟨🟨🟨🟨 : 0.08%
<p>
🟩🟨⬜⬜⬜ : 5.81%
<p>
🟩🟨🟨⬜⬜ : 1.71%
<p>
🟩🟨🟨🟨⬜ : 0.25%
<p>
🟩🟨🟨🟨🟨 : 0.07%
<p>
🟩🟩🟨⬜⬜ : 0.79%
<p>
🟩🟩🟨🟨⬜ : 0.23%
<p>
🟩🟩🟨🟨🟨 : 0.07%
<p>
🟩🟩🟩🟨⬜ : 0.11%
<p>
🟩🟩🟩🟨🟨 : 0.03%
<p>
🟩🟩🟩🟩🟨 : 0.01%
   </td>
   <td>
<p>
⬜⬜⬜⬜⬜ : 13.62%
<p>
🟩⬜⬜⬜⬜ : 8.14%
<p>
🟩🟩⬜⬜⬜ : 1.95%
<p>
🟩🟩🟩⬜⬜ : 0.23%
<p>
🟩🟩🟩🟩⬜ : 0.01%
<p>
🟩🟩🟩🟩🟩 : 0.0017%
<p>
🟨⬜⬜⬜⬜ : 22.53%
<p>
🟨🟨⬜⬜⬜ : 14.91
<p>
🟨🟨🟨⬜⬜ : 4.93%
<p>
🟨🟨🟨🟨⬜ : 0.82%
<p>
🟨🟨🟨🟨🟨 : 0.11%
<p>
🟩🟨⬜⬜⬜ : 5.39%
<p>
🟩🟨🟨⬜⬜ : 1.78%
<p>
🟩🟨🟨🟨⬜ : 0.29%
<p>
🟩🟨🟨🟨🟨 : 0.10%
<p>
🟩🟩🟨⬜⬜ : 0.64%
<p>
🟩🟩🟨🟨⬜ : 0.21%
<p>
🟩🟩🟨🟨🟨 : 0.07%
<p>
🟩🟩🟩🟨⬜ : 0.08%
<p>
🟩🟩🟩🟨🟨 : 0.03%
<p>
🟩🟩🟩🟩🟨 : 0.005%
   </td>
  </tr>
</table>



<table>
  <tr>
   <td><strong>SLANE</strong>
   </td>
   <td><strong>SANER</strong>
   </td>
   <td><strong>COATE</strong>
   </td>
   <td><strong>CRATE</strong>
   </td>
  </tr>
  <tr>
   <td>
<p>     
⬜⬜⬜⬜⬜ : 14.50%
<p>
🟩⬜⬜⬜⬜ : 10.63%
<p>
🟩🟩⬜⬜⬜ : 3.12%
<p>
🟩🟩🟩⬜⬜ : 0.46%
<p>
🟩🟩🟩🟩⬜ : 0.03%
<p>
🟩🟩🟩🟩🟩 : 0.0049%
<p>
🟨⬜⬜⬜⬜ : 20.54%
<p>
🟨🟨⬜⬜⬜ : 11.63
<p>
🟨🟨🟨⬜⬜ : 3.30%
<p>
🟨🟨🟨🟨⬜ : 0.47%
<p>
🟨🟨🟨🟨🟨 : 0.08%
<p>
🟩🟨⬜⬜⬜ : 6.02%
<p>
🟩🟨🟨⬜⬜ : 1.71%
<p>
🟩🟨🟨🟨⬜ : 0.24%
<p>
🟩🟨🟨🟨🟨 : 0.07%
<p>
🟩🟩🟨⬜⬜ : 0.88%
<p>
🟩🟩🟨🟨⬜ : 0.25%
<p>
🟩🟩🟨🟨🟨 : 0.07%
<p>
🟩🟩🟩🟨⬜ : 0.13%
<p>
🟩🟩🟩🟨🟨 : 0.04%
<p>
🟩🟩🟩🟩🟨 : 0.009%
   </td>
   <td>
<p>     
⬜⬜⬜⬜⬜ : 13.41%
<p>
🟩⬜⬜⬜⬜ : 8.77%
<p>
🟩🟩⬜⬜⬜ : 2.29%
<p>
🟩🟩🟩⬜⬜ : 0.30%
<p>
🟩🟩🟩🟩⬜ : 0.02%
<p>
🟩🟩🟩🟩🟩 : 0.0026%
<p>
🟨⬜⬜⬜⬜ : 21.57%
<p>
🟨🟨⬜⬜⬜ : 13.87
<p>
🟨🟨🟨⬜⬜ : 4.46%
<p>
🟨🟨🟨🟨⬜ : 0.72%
<p>
🟨🟨🟨🟨🟨 : 0.11%
<p>
🟩🟨⬜⬜⬜ : 5.64%
<p>
🟩🟨🟨⬜⬜ : 1.82%
<p>
🟩🟨🟨🟨⬜ : 0.29%
<p>
🟩🟨🟨🟨🟨 : 0.09%
<p>
🟩🟩🟨⬜⬜ : 0.74%
<p>
🟩🟩🟨🟨⬜ : 0.24%
<p>
🟩🟩🟨🟨🟨 : 0.08%
<p>
🟩🟩🟩🟨⬜ : 0.10%
<p>
🟩🟩🟩🟨🟨 : 0.03%
<p>
🟩🟩🟩🟩🟨 : 0.006%
   </td>
   <td>
<p>     
⬜⬜⬜⬜⬜ : 14.91%
<p>
🟩⬜⬜⬜⬜ : 9.82%
<p>
🟩🟩⬜⬜⬜ : 2.59%
<p>
🟩🟩🟩⬜⬜ : 0.34%
<p>
🟩🟩🟩🟩⬜ : 0.02%
<p>
🟩🟩🟩🟩🟩 : 0.0030%
<p>
🟨⬜⬜⬜⬜ : 21.84%
<p>
🟨🟨⬜⬜⬜ : 12.80
<p>
🟨🟨🟨⬜⬜ : 3.75%
<p>
🟨🟨🟨🟨⬜ : 0.55%
<p>
🟨🟨🟨🟨🟨 : 0.08%
<p>
🟩🟨⬜⬜⬜ : 5.75%
<p>
🟩🟨🟨⬜⬜ : 1.69%
<p>
🟩🟨🟨🟨⬜ : 0.25%
<p>
🟩🟨🟨🟨🟨 : 0.07%
<p>
🟩🟩🟨⬜⬜ : 0.76%
<p>
🟩🟩🟨🟨⬜ : 0.22%
<p>
🟩🟩🟨🟨🟨 : 0.07%
<p>
🟩🟩🟩🟨⬜ : 0.10%
<p>
🟩🟩🟩🟨🟨 : 0.03%
<p>
🟩🟩🟩🟩🟨 : 0.007%
   </td>
   <td>
<p>     
⬜⬜⬜⬜⬜ : 13.52%
<p>
🟩⬜⬜⬜⬜ : 8.81%
<p>
🟩🟩⬜⬜⬜ : 2.30%
<p>
🟩🟩🟩⬜⬜ : 0.30%
<p>
🟩🟩🟩🟩⬜ : 0.02%
<p>
🟩🟩🟩🟩🟩 : 0.0025%
<p>
🟨⬜⬜⬜⬜ : 21.63%
<p>
🟨🟨⬜⬜⬜ : 13.85
<p>
🟨🟨🟨⬜⬜ : 4.43%
<p>
🟨🟨🟨🟨⬜ : 0.71%
<p>
🟨🟨🟨🟨🟨 : 0.10%
<p>
🟩🟨⬜⬜⬜ : 5.64%
<p>
🟩🟨🟨⬜⬜ : 1.81%
<p>
🟩🟨🟨🟨⬜ : 0.29%
<p>
🟩🟨🟨🟨🟨 : 0.09%
<p>
🟩🟩🟨⬜⬜ : 0.74%
<p>
🟩🟩🟨🟨⬜ : 0.24%
<p>
🟩🟩🟨🟨🟨 : 0.08%
<p>
🟩🟩🟩🟨⬜ : 0.10%
<p>
🟩🟩🟩🟨🟨 : 0.03%
<p>
🟩🟩🟩🟩🟨 : 0.006%
   </td>
  </tr>
</table>

---

## Works Cited <a name="biblio"></a>


    _Word Pro - crazy crazy_, https://mathweb.ucsd.edu/~crypto/Projects/MarshaMoreno/TimeComparisonFrequency.pdf. Accessed 21 February 2022.


    Astle, David. “What is Wordle and how to solve it.” _Sydney Morning Herald_, 31 December 2021, https://www.smh.com.au/culture/books/tips-from-an-expert-how-to-solve-everyone-s-favourite-game-wordle-20211230-p59kwl.html. Accessed 21 February 2022.


    Norvig, Peter. “Letter frequency.” _Wikipedia_, https://en.wikipedia.org/wiki/Letter_frequency#Relative_frequencies_of_letters_in_the_English_language. Accessed 21 February 2022.


    Piantadosi, Steven T. “Zipf's word frequency law in natural language: A critical review and future directions.” _NCBI_, https://www.ncbi.nlm.nih.gov/pmc/articles/PMC4176592/. Accessed 21 February 2022.


    “Power law.” _Wikipedia_, https://en.wikipedia.org/wiki/Power_law. Accessed 21 February 2022.


    “Zipf's law.” _Wikipedia_, https://en.wikipedia.org/wiki/Zipf%27s_law. Accessed 21 February 2022.

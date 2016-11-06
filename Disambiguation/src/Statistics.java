import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class Statistics {
	
	public static HashMap<String, HashMap<String, Float>> statBefore;
	public static HashMap<String, HashMap<String, Float>> statAfter;
	public static List<String> partsOfSpeech;

	public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, ParserConfigurationException, SAXException, IOException {
		// TODO Auto-generated method stub
		partsOfSpeech = VectorizingPOS.collectPartsOfSpeech();
		List<Trigram> data = RusCorpora.loadData();
		List<Trigram> trainData = data.subList(0, 400000);
		statBefore = getStat(trainData, true);
		statAfter = getStat(trainData, false);
		System.out.println("statBefore.size " + statBefore.size());
		System.out.println("statAfter.size " + statAfter.size());
		List<Trigram> testData = data.subList(400000, 500000);
		test(testData, "D:/Morph/statErrors.txt");
		writeResults(testData, "D:/Morph/statResults.txt");
	}
	
	public static void test(List<Trigram> data, String pathToWrite) throws IOException {
		PrintWriter ps = new PrintWriter(new BufferedWriter(new FileWriter(pathToWrite)));
		int right = 0;
		int withStat = 0;
		int ambig = 0;
		int rightAmbig = 0;
		for (Trigram trigram : data) {

			String ourPos = choosePartOfSpeech(trigram);
			if (ourPos != null) {
				withStat++;
				if (RusCorpora.getAllPossiblePos(trigram.targetWord)!=null && RusCorpora.getAllPossiblePos(trigram.targetWord).size()>1) {
					ambig++;
				}
			}
			if (ourPos!=null && ourPos.equals(trigram.targetPartOfSpeech)) {
				right++;
				if (RusCorpora.getAllPossiblePos(trigram.targetWord)!=null && RusCorpora.getAllPossiblePos(trigram.targetWord).size()>1) {
					rightAmbig++;
				}
			}
			else {
				ps.println("target: " + trigram.targetPartOfSpeech + "; our: " + ourPos);
				ps.println(trigram.wordBefore + " [" + trigram.targetWord + "] " + trigram.wordAfter);
				ps.println();
			}
		}
		ps.close();
		double precision = (right*1.0) / (withStat*1.0);
		double disambig = (rightAmbig*1.0) / (ambig*1.0);
		System.out.println("Precision: " + precision);
		System.out.println("Disambiguation: " + disambig);
		System.out.println("withStat: " + withStat);		
	}
	
	public static void writeResults(List<Trigram> data, String pathToWrite) throws IOException {
		PrintWriter ps = new PrintWriter(new BufferedWriter(new FileWriter(pathToWrite)));
		for (Trigram trigram : data) {
			String ourPos = choosePartOfSpeech(trigram);
			if (ourPos == null) {
				ps.println("0:0");
			}
			else {
				ps.println(partsOfSpeech.indexOf(ourPos)+":"+1);
			}
		}
		ps.close();
	}
	
	public static String choosePartOfSpeech(Trigram trigram) {
		HashMap<String, Float> probs = new HashMap<>();
		//String flexBefore = RusCorpora.getFlex(trigram.targetWord)+"-"+RusCorpora.getFlex(trigram.wordAfter);
		String flexBefore = VectorizingSubstitutions.hashSetToString(trigram.targetSubst)+"-"+VectorizingSubstitutions.hashSetToString(trigram.substBefore);
		fillProbs(probs, flexBefore, true, 1);
		//String flexAfter = RusCorpora.getFlex(trigram.targetWord)+"-"+RusCorpora.getFlex(trigram.wordAfter);
		String flexAfter = VectorizingSubstitutions.hashSetToString(trigram.targetSubst)+"-"+VectorizingSubstitutions.hashSetToString(trigram.substAfter);
		fillProbs(probs, flexAfter, false, 1);
		if (probs != null) {
		float max = 0;
		for (Float value : probs.values()) {
			if (value > max) {
				max = value;
			}
		}
		for (String pos : probs.keySet()) {
			if (probs.get(pos).equals(max)) {
				return pos;
			}
		}
		}
		return null;
	}
	
	public static void fillProbs(HashMap<String, Float> probs, String flex, boolean previousWord, float coeff) {
		HashMap<String, Float> hm = new HashMap<>();
		if (previousWord) {
			hm = statBefore.get(flex);
		}
		else {
			hm = statAfter.get(flex);
		}
		if (hm == null) {
			return;
		}
		for (String pos : hm.keySet()) {
			if (probs.containsKey(pos)) {
				float value = probs.get(pos);
				value += hm.get(pos);
				probs.put(pos, value*coeff);
			}
			else {
				probs.put(pos, hm.get(pos)*coeff);
			}
		}
	}
	
	public static HashMap<String, HashMap<String, Float>> getStat(List<Trigram> data, boolean previousWord) {
		HashMap<String, HashMap<String, Float>> stat = new HashMap<>();
		int num = 0;
		for (Trigram trigram : data) {
			num++;
			System.out.println("getStat " + num);
			String flex = new String();
			if (previousWord) {
				//flex = RusCorpora.getFlex(trigram.targetWord)+"-"+RusCorpora.getFlex(trigram.wordBefore);
				flex = VectorizingSubstitutions.hashSetToString(trigram.targetSubst)+"-"+VectorizingSubstitutions.hashSetToString(trigram.substBefore);
			}
			else {
				//flex = RusCorpora.getFlex(trigram.targetWord)+"-"+RusCorpora.getFlex(trigram.wordAfter);
				flex = VectorizingSubstitutions.hashSetToString(trigram.targetSubst)+"-"+VectorizingSubstitutions.hashSetToString(trigram.substAfter);
			}
			addToHashMap(stat, flex, trigram.targetPartOfSpeech);
		}
		return stat;
	}
	
	public static void countProbs(HashMap<String, HashMap<String, Float>> stat) {
		int num = 0;
		for (String flex : stat.keySet()) {
			num++;
			System.out.println("count probs " + num);
			HashMap<String, Float> amount = stat.get(flex);
			float sum = 0;
			for (Float value : amount.values()) {
				sum += value;
			}
			for (String pos : amount.keySet()) {
				float prob = amount.get(pos)/sum;
				amount.put(pos, prob);
			}
			stat.put(flex, amount);
		}
	}
	
	public static void addToHashMap(HashMap<String, HashMap<String, Float>> stat, String flex, String pos) {
		if (stat.containsKey(flex)) {
			HashMap<String, Float> amount = stat.get(flex);
			if (amount.containsKey(pos)) {
				float value = amount.get(pos);
				value += 1;
				amount.put(pos, value);
			}
			else {
				float value = 1;
				amount.put(pos, value);
			}
			stat.put(flex, amount);
		}
		else {
			HashMap<String, Float> amount = new HashMap<>();
			float value = 1;
			amount.put(pos, value);
			stat.put(flex, amount);
		}
	}

}

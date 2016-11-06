import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class PostProcessing {
	public static List<String> partsOfSpeech;
	public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, ParserConfigurationException, SAXException, IOException {
		// TODO Auto-generated method stub
		partsOfSpeech = VectorizingPOS.collectPartsOfSpeech();
		List<Trigram> corpus = RusCorpora.loadData();
		List<Trigram> testData = corpus.subList(400000, 500000);
		//List<Trigram> testData = OpenCorpora.loadOpenCorpora();
		HashMap<Integer, HashMap<Integer, Float>> results = loadResults("D:/Morph/StatCombinerWordVectors/results.txt");
		addResults(results, "D:/Morph/StatCombinerEmbedding/results.txt", 1);
		addResults(results, "D:/Morph/StatCombinerPOS/results.txt", 3);
		addResults(results, "D:/Morph/StatCombinerEmbedding/Random/results.txt", 1);
		addResults(results, "D:/Morph/StatCombinerSubstitution/results.txt", 1);
		addResults(results, "D:/Morph/StatCombinerSubstitution/Random/results.txt", 1);
		addResults(results, "D:/Morph/statResults.txt", 1);
		addResults(results, "D:/Morph/StatCombinerWordVectors/Average/results.txt", 1);
		processResults(testData, results, "D:/Morph/allErrors.txt", false);
		//processOpenCorpora(testData, results, "D:/Morph/StatCombinerSubstitution/OpenCorpora/errors.txt", false);
		
//		HashMap<Integer, HashMap<Integer, Float>> substitutionRandom = loadResults("D:\\Morph\\StatCombinerSubstitution\\Random\\results.txt");
//		writeResultsAfterPostProcessing(testData, substitutionRandom, "D:\\Morph\\StatCombinerSubstitution\\Random\\resultsPP.txt");
//		System.out.println(1);
//		HashMap<Integer, HashMap<Integer, Float>> embedding = loadResults("D:\\Morph\\StatCombinerEmbedding\\results.txt");
//		writeResultsAfterPostProcessing(testData, embedding, "D:\\Morph\\StatCombinerEmbedding\\resultsPP.txt");
//		System.out.println(2);
//		HashMap<Integer, HashMap<Integer, Float>> embeddingRandom = loadResults("D:\\Morph\\StatCombinerEmbedding\\Random\\results.txt");
//		writeResultsAfterPostProcessing(testData, embeddingRandom, "D:\\Morph\\StatCombinerEmbedding\\Random\\resultsPP.txt");
//		System.out.println(3);
//		HashMap<Integer, HashMap<Integer, Float>> wordVectors = loadResults("D:\\Morph\\StatCombinerWordVectors\\results.txt");
//		writeResultsAfterPostProcessing(testData, wordVectors, "D:\\Morph\\StatCombinerWordVectors\\resultsPP.txt");
//		System.out.println(4);
//		HashMap<Integer, HashMap<Integer, Float>> wordVectorsAverage = loadResults("D:\\Morph\\StatCombinerWordVectors\\Average\\results.txt");
//		writeResultsAfterPostProcessing(testData, wordVectorsAverage, "D:\\Morph\\StatCombinerWordVectors\\Average\\resultsPP.txt");
//		System.out.println(5);
		
	}
	
	public static HashMap<Integer, HashMap<Integer, Float>> loadResults(String path) throws IOException {
		HashMap<Integer, HashMap<Integer, Float>> results = new HashMap<>();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
		String line = null;
		int num = 0;
		while((line = br.readLine())!=null) {
			HashMap<Integer, Float> probs = new HashMap<>();
			String[] res = line.split(",");
			for (String r : res) {
				String[] parts = r.split(":");
				probs.put(Integer.parseInt(parts[0]), Float.parseFloat(parts[1]));
			}
			results.put(num, probs);
			num++;
		}
		return results;
	}
	
	public static void addResults(HashMap<Integer, HashMap<Integer, Float>> results, String path, float coeff) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
		String line = null;
		int num = 0;
		while ((line = br.readLine())!=null) {
			HashMap<Integer, Float> probs = results.get(num);
			String[] res = line.split(",");
			for (String r : res) {
				String[] parts = r.split(":");
				if (probs.containsKey(Integer.parseInt(parts[0]))) {
					float value = probs.get(Integer.parseInt(parts[0]));
					value += Float.parseFloat(parts[1]);
					probs.put(Integer.parseInt(parts[0]), value*coeff);
				}
				else {
					probs.put(Integer.parseInt(parts[0]), Float.parseFloat(parts[1])*coeff);
				}
			}
			results.put(num, probs);
			num++;
		}
	}
	
	public static void processResults(List<Trigram> testData, HashMap<Integer, HashMap<Integer, Float>> results, String pathToWrite, boolean postProcessing) throws IOException {
		PrintWriter ps = new PrintWriter(new BufferedWriter(new FileWriter(pathToWrite)));
		int right = 0;
		int ambig = 0;
		int rightAmbig = 0;
		for (Trigram trigram : testData) {
			int ourPos = -1;
			HashMap<Integer, Float> probs = results.get(testData.indexOf(trigram));
			HashSet<String> allPossiblePOS = trigram.allPossiblePos;
			
			if (allPossiblePOS!=null && allPossiblePOS.size()>1) {
				ambig++;
				if (postProcessing) {
			for (String pos : allPossiblePOS) {
				if (probs.keySet().contains(partsOfSpeech.indexOf(pos))) {
					ourPos = maxWithWordNet(allPossiblePOS, probs);
				}
			}
			}
			}
			
			if (ourPos == -1) {
			ourPos = max(probs);
			}
			if (partsOfSpeech.indexOf(trigram.targetPartOfSpeech)==ourPos) {
				right++;
				if (allPossiblePOS!=null && allPossiblePOS.size()>1) {
					rightAmbig++;
				}
			}
			else {
				String error = trigram.wordBefore+" ["+trigram.targetWord+"] "+trigram.wordAfter;
				ps.write("target: " + trigram.targetPartOfSpeech + "; our: " + partsOfSpeech.get(ourPos));
				ps.println();
				ps.write(error);
				ps.println();
				ps.println();
			}
		}
		ps.close();
		double precision = (right*1.0) / (testData.size()*1.0);
		double ambigPrecision = (rightAmbig*1.0) / (ambig*1.0);
		System.out.println("precision: " + precision);
		System.out.println("ambig precision: " + ambigPrecision);
		System.out.println("ambig = " + ambig);
	}
	
	public static void processOpenCorpora(List<Trigram> testData, HashMap<Integer, HashMap<Integer, Float>> results, String pathToWrite, boolean postProcessing) throws IOException {
		PrintWriter ps = new PrintWriter(new BufferedWriter(new FileWriter(pathToWrite)));
		int right = 0;
		int num = 0;
		int ambig = 0;
		int rightAmbig = 0;
		for (Trigram trigram : testData) {
			int ourPos = -1;
			System.out.println("num " + num);
			if (partsOfSpeech.indexOf(trigram.targetPartOfSpeech)==-1) {
				continue;
			}
			if (trigram.allPossiblePos!=null && trigram.allPossiblePos.size()>1) {
				ambig++;
			}
			HashMap<Integer, Float> probs = results.get(num);
			HashSet<String> allPossiblePOS = trigram.allPossiblePos;
			if (allPossiblePOS!=null) {
				if (postProcessing) {
			for (String pos : allPossiblePOS) {
			if (probs.keySet().contains(partsOfSpeech.indexOf(pos))) {
				ourPos = maxWithWordNet(allPossiblePOS, probs);
			}
		}
			}
			}
			if (ourPos == -1) {
			ourPos = max(probs);
			}
			if (partsOfSpeech.indexOf(trigram.targetPartOfSpeech)==(ourPos)) {
				right++;
				if (trigram.allPossiblePos!=null && trigram.allPossiblePos.size()>1) {
					rightAmbig++;
				}
			}
			else {
				String error = trigram.wordBefore+" ["+trigram.targetWord+"] "+trigram.wordAfter;
				ps.write("# " + num + " target: " + trigram.targetPartOfSpeech+"; our: " + partsOfSpeech.get(ourPos));
				ps.println();
				ps.write(error);
				ps.println();
				ps.println();
			}
			num++;
		}
		ps.close();
		double precision = (right*1.0) / (testData.size()*1.0);
		double ambigPrecision = (rightAmbig*1.0) / (ambig*1.0);
		System.out.println("right: " + right);
		System.out.println("precision: " + precision);
		System.out.println("ambig precision: " + ambigPrecision);
		System.out.println("ambig = " + ambig);
	}
	
	public static void writeResultsAfterPostProcessing(List<Trigram> testData, HashMap<Integer, HashMap<Integer, Float>> results, String pathToWrite) throws IOException {
		PrintWriter ps = new PrintWriter(new BufferedWriter(new FileWriter(pathToWrite)));
		int num = 0;
		for (Integer target : results.keySet()) {
			boolean written = false;
			Trigram idealTrigram = testData.get(num);
			HashMap<Integer, Float> probs = results.get(target);
			if (idealTrigram.allPossiblePos!=null) {
			for (String pos : idealTrigram.allPossiblePos) {
				if (probs.containsKey(partsOfSpeech.indexOf(pos))) {
					int choosenPos = partsOfSpeech.indexOf(pos);
					float prob = probs.get(partsOfSpeech.indexOf(pos));
					ps.write(choosenPos+":"+prob+",");
					written = true;
				}
			}
			}
			if (written == false) {
				ps.write(0+":"+0);
			}
			ps.println();
			num++;
		}
		ps.close();
	}
	
	public static int maxWithWordNet(HashSet<String> possiblePOS, HashMap<Integer, Float> probs) {
		HashMap<Integer, Float> probsWN = new HashMap<>();
		for (String pos : possiblePOS) {
			if (probs.containsKey(partsOfSpeech.indexOf(pos))) {
				int key = partsOfSpeech.indexOf(pos);
				float value = probs.get(partsOfSpeech.indexOf(pos));
				probsWN.put(key, value);
			}
		}
		return max(probsWN);
	}
	
	public static int max(HashMap<Integer, Float> probs) {
		float max = 0;
		for (Float value : probs.values()) {
			if (value > max) {
				max = value;
			}
		}
		int pos = 0;
		for (Integer p : probs.keySet()) {
			if (probs.get(p).equals(max)) {
				pos = p;
			}
		}
		return pos;
	}

}

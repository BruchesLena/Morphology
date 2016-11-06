import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class VectorizingEmbedding {
	public static List<String> dictionary;
	public static List<String> partsOfSpeech;
	public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, ParserConfigurationException, SAXException, IOException {
		// TODO Auto-generated method stub
		//List<Trigram> corpus = RusCorpora.loadData();
		List<Trigram> corpForDict = RusCorpora.loadData();
		List<Trigram> openCorpora = OpenCorpora.loadOpenCorpora();
		dictionary = getDictionary(corpForDict, true);
		partsOfSpeech = VectorizingPOS.collectPartsOfSpeech();
		System.out.println("dictionary size: " + dictionary.size());
		try {
			print(openCorpora,"learnSet");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println("dictionary size: " + dictionary.size() + " DON'T FORGET +1");
	}
	
	private static void print(List<Trigram> dataSet, String ln) throws IOException {
		int num = 0;
		int count = 0;
		PrintWriter ps = new PrintWriter(new BufferedWriter(new FileWriter("D:/Morph/StatCombinerEmbedding/Random/OpenCorpora/" + ln + count)));
		for (Trigram e : dataSet) {
			printEntry(ps, e, num);
			num++;
			System.out.println("print " + num);
			if (num % 20000 == 0) {
				ps.close();
				count++;

				ps = new PrintWriter(new BufferedWriter(new FileWriter("D:/Morph/StatCombinerEmbedding/Random/OpenCorpora/" + ln + count)));
				if (num == 500000) {
					break;
				}
			}
		}
		ps.close();
		System.out.println("Printed:" + num);
	}
	
	private static void printEntry(PrintWriter ps, Trigram trigram, int num) {
		double[] mm = new double[3];
		initInputsWithRandom(trigram, mm);
		if (partsOfSpeech.indexOf(trigram.targetPartOfSpeech) == -1) {
			return;
		}
		ps.print(partsOfSpeech.indexOf(trigram.targetPartOfSpeech) + ";");
		for (int i = 0; i < mm.length; i++) {
			ps.print(mm[i]);
			if (i != mm.length - 1) {
				ps.print(" ,");
			}
		}
		ps.println();
	}
	
	private static void initInputs(Trigram trigram, double[] mm) {
		if (dictionary.indexOf(trigram.wordBefore)==-1) {
			mm[0] = dictionary.size()+1;
		}
		else {
		mm[0] = dictionary.indexOf(trigram.wordBefore);
		}
		if (dictionary.indexOf(trigram.targetWord)==-1) {
			mm[1] = dictionary.size()+1;
		}
		else {
		mm[1] = dictionary.indexOf(trigram.targetWord);
		}
		if (dictionary.indexOf(trigram.wordAfter)==-1) {
			mm[2] = dictionary.size()+1;
		}
		else {
		mm[2] = dictionary.indexOf(trigram.wordAfter);
		}
	}
	
	private static void initInputsWithRandom(Trigram trigram, double[] mm) {
		Random random = new Random(5);
		if (dictionary.indexOf(trigram.wordBefore)==-1) {
			mm[0] = random.nextInt(104878);
		}
		else {
			mm[0] = dictionary.indexOf(trigram.wordBefore);
		}
		if (dictionary.indexOf(trigram.targetWord)==-1) {
			mm[1] = random.nextInt(104878);
		}
		else {
			mm[1] = dictionary.indexOf(trigram.targetWord);
		}
		if (dictionary.indexOf(trigram.wordAfter)==-1) {
			mm[2] = dictionary.indexOf(104878);
		}
		else {
			mm[2] = dictionary.indexOf(104878);
		}
	}
	
	public static List<String> getDictionary(List<Trigram> data, boolean needShuffle) {
		if (needShuffle == true) {
			Collections.shuffle(data, new Random(5));
		}
		HashSet<String> dict = new HashSet<>();
		for (Trigram trigram : data.subList(0, 400000)) {
			dict.add(trigram.wordBefore);
			dict.add(trigram.wordAfter);
			dict.add(trigram.targetWord);
		}
		List<String> dictionary = new ArrayList<>();
		dictionary.addAll(dict);
		return dictionary;		
	}

}

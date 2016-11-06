import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class VectorizingSubstitutions {
	public static List<String> partsOfSpeech;
	public static List<String> substitutions;
	public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, ParserConfigurationException, SAXException, IOException {
		// TODO Auto-generated method stub
		List<Trigram> corpForDict = RusCorpora.loadData();
		//List<Trigram> corpus = RusCorpora.loadData();
		List<Trigram> corpus = OpenCorpora.loadOpenCorpora();
		substitutions = new ArrayList<>();
		substitutions.addAll(RusCorpora.collectSub(corpForDict));
		partsOfSpeech = VectorizingPOS.collectPartsOfSpeech();
		try {
			print(corpus,"learnSet");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("substitution size " + substitutions.size());
	}
	
	private static void print(List<Trigram> dataSet, String ln) throws IOException {
		int num = 0;
		int count = 0;
		PrintWriter ps = new PrintWriter(new BufferedWriter(new FileWriter("D:/Morph/StatCombinerSubstitution/Random/OpenCorpora/" + ln + count)));
		for (Trigram e : dataSet) {
			printEntry(ps, e, num);
			num++;
			System.out.println("print " + num);
			if (num % 20000 == 0) {
				ps.close();
				count++;

				ps = new PrintWriter(new BufferedWriter(new FileWriter("D:/Morph/StatCombinerSubstitution/Random/OpenCorpora/" + ln + count)));
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
		initInputs(trigram, mm);
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
		fillArrayWithRandom(mm, 0, hashSetToString(trigram.substBefore));
		fillArrayWithRandom(mm, 1, hashSetToString(trigram.targetSubst));
		fillArrayWithRandom(mm, 2, hashSetToString(trigram.substAfter));
	}
	
	private static void fillArray(double[] mm, int pos, String sub) {
		if (substitutions.contains(sub)) {
			mm[pos] = substitutions.indexOf(sub);
		}
		else {
			mm[pos] = substitutions.size()+1;
		}
	}
	
	private static void fillArrayWithRandom(double[] mm, int pos, String sub) {
		Random random = new Random();
		if (substitutions.contains(sub)) {
			mm[pos] = substitutions.indexOf(sub);
		}
		else {
			mm[pos] = random.nextInt(substitutions.size());
		}
	}
	
	public static String hashSetToString(HashSet<String> subs) {
		String s = new String();
		for (String str : subs) {
			s += str+"/";
		}
		return s;
	}

}

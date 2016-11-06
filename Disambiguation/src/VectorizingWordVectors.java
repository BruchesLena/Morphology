import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.wordnet.MorphologicalRelation;
import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.semantic.wordnet.WordNetProvider;

public class VectorizingWordVectors {
	private static WordVectors wordVectorsSmall;
	public static List<String> partsOfSpeech;
	public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, ParserConfigurationException, SAXException, IOException {
		// TODO Auto-generated method stub
		wordVectorsSmall = new WordVectors();
		try {
			wordVectorsSmall.load(new BufferedReader(new InputStreamReader(new FileInputStream("C:/Avito/wordVectorsSmall.txt"), "UTF-8")));
		} catch (IOException e) {
			e.printStackTrace();
		}
		partsOfSpeech = VectorizingPOS.collectPartsOfSpeech();
		//List<Trigram> corpus = RusCorpora.loadData();
		List<Trigram> corpus = OpenCorpora.loadOpenCorpora();
		try {
			print(corpus,"learnSet");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void print(List<Trigram> dataSet, String ln) throws IOException {
		int num = 0;
		int count = 0;
		PrintWriter ps = new PrintWriter(new BufferedWriter(new FileWriter("D:/Morph/StatCombinerWordVectors/Average/OpenCorpora/" + ln + count)));
		for (Trigram e : dataSet) {
			printEntry(ps, e, num);
			num++;
			System.out.println("print " + num);
			if (num % 20000 == 0) {
				ps.close();
				count++;

				ps = new PrintWriter(new BufferedWriter(new FileWriter("D:/Morph/StatCombinerWordVectors/Average/OpenCorpora/" + ln + count)));
				if (num == 500000) {
					break;
				}
			}
		}
		ps.close();
		System.out.println("Printed:" + num);
	}
	
	private static void printEntry(PrintWriter ps, Trigram trigram, int num) {
		double[] mm = new double[900];
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
		double[] vecBefore = sumVectors(trigram.wordBefore);
		for (int i=0; i < 300; i++) {
			mm[i] = vecBefore[i];
		}
		double[] targetVec = sumVectors(trigram.targetWord);
		for (int i =0; i < 300; i++) {
			mm[300+i] = targetVec[i];
		}
		double[] vecAfter = sumVectors(trigram.wordAfter);
		for (int i = 0; i < 300; i++) {
			mm[600+i] = vecAfter[i];
		}
	}
	
	private static double[] sumVectors(String wordForm) {
		double[] mm = new double[300];
		HashSet<String> basicForms = RusCorpora.getBasicForms(wordForm);
		if (basicForms==null) {
			return mm;
		}
		if (basicForms.size()==1) {
			List<String> bf = new ArrayList<>();
			bf.addAll(basicForms);
			fillArray(mm, bf.get(0));
			return mm;
		}
		if (basicForms.size()>1) {
			for (String form : basicForms) {
				addToArray(mm, form);
			}
			return mm;
		}
		return mm;
	}
	
	private static void fillArray(double[] mm, String word) {
		HashSet<String> partsOfSpeech = RusCorpora.getAllPossiblePos(word);
		if (partsOfSpeech.contains("VERB")) {
			word = getInfinitive(word);
		}
		if (wordVectorsSmall.vectors.containsKey(word)) {
			float[] vec = wordVectorsSmall.vectors.get(word);
			if (vec != null) {
				for (int i = 0; i < 300; i++) {
					mm[i] = vec[i];
				}
			}
		}
	}
	
	private static void addToArray(double[] mm, String word) {
		HashSet<String> pos = RusCorpora.getAllPossiblePos(word);
		if (pos.contains("VERB")) {
			word = getInfinitive(word);
		}
		if (wordVectorsSmall.vectors.containsKey(word)) {
			float[] vec = wordVectorsSmall.vectors.get(word);
			if (vec!= null) {
				for (int i = 0; i < 300; i++) {
					double el = mm[i];
					el += vec[i];
					double average = el/2;
					mm[i] = average;
				}
			}
		}
	}
	
	private static String getInfinitive(String wordForm) {
		TextElement wordElement = WordNetProvider.getInstance().getWordElement(wordForm);
		for (MeaningElement m : wordElement.getConcepts()) {
			for (MorphologicalRelation r : m.getMorphologicalRelations()) {
				return r.getWord().getParentTextElement().getBasicForm();
			}
		}
		return wordForm;
	}

}

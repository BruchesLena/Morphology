import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.onpositive.semantic.wordnet.Grammem;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;

public class VectorizingPOS {
	public static List<String> partsOfSpeech;

	public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, ParserConfigurationException, SAXException, IOException {
		// TODO Auto-generated method stub
		partsOfSpeech = collectPartsOfSpeech();
		//List<Trigram> ruscorpora = RusCorpora.loadData();
		List<Trigram> openCorpora = OpenCorpora.loadOpenCorpora();
		try {
			print(openCorpora,"learnSet");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void print(List<Trigram> dataSet, String ln) throws IOException {
		int num = 0;
		int count = 0;
		PrintWriter ps = new PrintWriter(new BufferedWriter(new FileWriter("D:/Morph/StatCombinerPOS/OpenCorpora/" + ln + count)));
		for (Trigram e : dataSet) {
			printEntry(ps, e, num);
			num++;
			System.out.println("print " + num);
			if (num % 20000 == 0) {
				ps.close();
				count++;

				ps = new PrintWriter(new BufferedWriter(new FileWriter("D:/Morph/StatCombinerPOS/OpenCorpora/" + ln + count)));
				if (num == 500000) {
					break;
				}
			}
		}
		ps.close();
		System.out.println("Printed:" + num);
	}
	
	private static void printEntry(PrintWriter ps, Trigram trigram, int num) {
		double[] mm = new double[51];
		initInputs(trigram, mm);
		if (partsOfSpeech.indexOf(trigram.targetPartOfSpeech)==-1) {
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
	
//	private static void initInputs(Trigram trigram, double[] mm) {
//		int posBefore = partsOfSpeech.indexOf(trigram.partOfSpeechBefore);
//		if (posBefore!=-1) {
//		mm[posBefore] = 1;
//		}
//		if (trigram.allPossiblePos != null) {
//			for (String pos : trigram.allPossiblePos) {
//				int index = partsOfSpeech.indexOf(pos);
//				mm[17+index] = 1;
//			}
//		}
//		int posAfter = partsOfSpeech.indexOf(trigram.partOfSpeechAfter);
//		if (posAfter!=-1){
//		mm[17*2+posAfter] = 1;
//		}
//	}
	
	private static void initInputs(Trigram trigram, double[] mm) {
		setVector(trigram.wordBefore, trigram.partOfSpeechBefore, 0, mm);
		setVector(trigram.targetWord, trigram.targetPartOfSpeech, 1, mm);
		setVector(trigram.wordAfter, trigram.partOfSpeechAfter, 2, mm);
	}
	
	private static void setVector(String wordForm, String pos, int i, double[] mm) {
		HashSet<String> posBefore = OpenCorpora.getAllPossiblePos(wordForm);
		if (posBefore != null) {
			for (String p : posBefore) {
				int index = partsOfSpeech.indexOf(p);
				mm[17*i+index] = 1;
			}
		}
		else {
			int index = partsOfSpeech.indexOf(pos);
			if (index!=-1) {
			mm[17*i+index] = 1;
			}
		}		
	}
	
	public static List<String> collectPartsOfSpeech() {
		List<String> partsOfSpeech = new ArrayList<>();
		HashSet<PartOfSpeech> pos = Grammem.PartOfSpeech.all;
		for (PartOfSpeech partOfSpeech : pos) {
			partsOfSpeech.add(partOfSpeech.id);
		}
		return partsOfSpeech;
	}

}

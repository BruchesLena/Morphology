import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.onpositive.semantic.wordnet.AbstractWordNet;
import com.onpositive.semantic.wordnet.GrammarRelation;
import com.onpositive.semantic.wordnet.MeaningElement;
import com.onpositive.semantic.wordnet.TextElement;
import com.onpositive.semantic.wordnet.WordNetProvider;
import com.onpositive.semantic.wordnet.Grammem.PartOfSpeech;

public class RusCorpora {
	public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, ParserConfigurationException, SAXException, IOException {
		// TODO Auto-generated method stub
		List<Trigram> data = loadData();
		HashSet<String> substitutions = collectSub(data);
		for (String s : substitutions) {
			System.out.println(s);
		}
		System.out.println("Substitution size: " + substitutions.size());
		System.out.println("data size: " + data.size());
	}
	
	public static List<Trigram> loadData() throws ParserConfigurationException, UnsupportedEncodingException, FileNotFoundException, SAXException, IOException {
		List<Trigram> data = new ArrayList<>();
		//File file  = new File("D:/corpora_1M/texts/7.xhtml");
		File folder = new File("D:/corpora in UTF/texts");
		List<File> folderFiles = getXmlFiles(folder);
		for (int j = 0; j < folderFiles.size(); j++) {
			System.out.println(j);
		DocumentBuilderFactory f  = DocumentBuilderFactory.newInstance();
		f.setValidating(false);
		DocumentBuilder builder = f.newDocumentBuilder();
		Document doc = builder.parse(new InputSource(new InputStreamReader(new FileInputStream(folderFiles.get(j)),"UTF-8")));
		//subCorpora.add(getSubCorpus(doc));
		if (isWrittenText(doc) == false) {
			continue;
		}
		NodeList nodeList = doc.getElementsByTagName("w");
		for (int i = 1; i < nodeList.getLength(); i++) {
			System.out.println(i);
			if (i == 1648) {
				System.out.println("stop");
			}
			if (i == nodeList.getLength()-1) {
				break;
			}
			Node node = nodeList.item(i);
			String wordForm = setWordForm(node);
			String pos = getPartOfSpeech(getGrammems(node));
			if (getPartOfSpeech(getGrammems(node))==null) {
				continue;
			}
			Trigram trigram = new Trigram();
			trigram.targetPartOfSpeech = pos;
			trigram.targetWord = wordForm;
			trigram.wordBefore = setWordForm(nodeList.item(i-1));
			trigram.wordAfter = setWordForm(nodeList.item(i+1));
			trigram.partOfSpeechBefore = getPartOfSpeech(getGrammems(nodeList.item(i-1)));
			trigram.partOfSpeechAfter = getPartOfSpeech(getGrammems(nodeList.item(i+1)));
			trigram.allPossiblePos = getAllPossiblePos(wordForm);
			trigram.substBefore = setSubstitution(nodeList.item(i-1));
			trigram.targetSubst = setSubstitution(node);
			trigram.substAfter = setSubstitution(nodeList.item(i+1));
			data.add(trigram);
		}
		}
		return data;
	}
	
	public static HashSet<String> collectSubstitutions(List<Trigram> data) {
		HashSet<String> allSub = new HashSet<>();
		Collections.shuffle(data, new Random(5));
		for (Trigram trigram : data) {
			allSub.addAll(trigram.substBefore);
			allSub.addAll(trigram.targetSubst);
			allSub.addAll(trigram.substAfter);
		}
		return allSub;
	}
	
	public static HashSet<String> collectSub(List<Trigram> data) {
		HashSet<String> allSub = new HashSet<>();
		Collections.shuffle(data, new Random(5));
		for (Trigram trigram : data.subList(0, 400000)) {
			addToHashSet(trigram.substBefore, allSub);
			addToHashSet(trigram.targetSubst, allSub);
			addToHashSet(trigram.substAfter, allSub);
		}
		return allSub;
	}
	
	public static void addToHashSet(HashSet<String> sub, HashSet<String> subst) {
		String s = new String();
		for (String str : sub) {
			s += str+"/";
		}
		subst.add(s);
	}
	
	public static HashSet<String> setSubstitution(Node node) {
		HashSet<String> substitutions = new HashSet<>();
		String wordForm = setWordForm(node);
		HashSet<String> basicForms = getBasicForms(wordForm);
		if (basicForms == null) {
			substitutions.add(getFlex(wordForm));
		}
		else {
		for (String basicForm : basicForms) {
			String sub = getFlex(wordForm)+"-"+getFlex(basicForm);
			substitutions.add(sub);
		}
		}
		return substitutions;
	}
	
	public static HashSet<String> getBasicForms(String wordForm) {
		HashSet<String> basicForms = new HashSet<>();
		AbstractWordNet instance = WordNetProvider.getInstance();
		GrammarRelation[] possibleGrammarForms = instance.getPossibleGrammarForms(wordForm);
		if (possibleGrammarForms == null) {
			return null;
		}
		for (GrammarRelation g : possibleGrammarForms) {
			TextElement textElement = g.getWord();
			String basicForm = textElement.getBasicForm();
			basicForms.add(basicForm);
		}
		return basicForms;
	}
	
	public static HashSet<String> getAllPossiblePos(String wordForm) {
		HashSet<String> pos = new HashSet<>();
		AbstractWordNet instance = WordNetProvider.getInstance();
		GrammarRelation[] possibleGrammarForms = instance.getPossibleGrammarForms(wordForm);
		if (possibleGrammarForms == null) {
			return null;
		}
		for (GrammarRelation gr : possibleGrammarForms) {
			TextElement textElement = gr.getWord();
			MeaningElement[] concepts = textElement.getConcepts();
			for (MeaningElement m : concepts) {
				PartOfSpeech partOfSpeech = m.getPartOfSpeech();
				if (partOfSpeech != null) {
					pos.add(partOfSpeech.id);
				}
			}
		}
		return pos;
	}
	
	public static List<String> getDictionary(List<Trigram> data) {
		HashSet<String> dict = new HashSet<>();
		List<String> d = new ArrayList<>();
		for (Trigram trigram : data) {
			dict.add(trigram.wordBefore);
			dict.add(trigram.targetWord);
			dict.add(trigram.wordAfter);
		}
		d.addAll(dict);
		return d;
	}
	
	public static String setWordForm(Node node) {
		String word = node.getTextContent().toLowerCase();
		if (word.contains("`")) {
			word = removeAccent(word);
		}
		return word;
	}
	
	public static String removeAccent(String wordForm) {
		String[] parts = wordForm.split("`");
		return parts[0]+parts[1];
	}
	
	public static String getPartOfSpeech(String gr) {
		String[] grammems = gr.split(",");
		if (grammems[0].equals("A")) {
			for (String grammem : grammems) {
				if (grammem.equals("brev")) {
					return "ADJS";
				}
				if (grammem.equals("plen")) {
					return "ADJF";
				}
			}
		}
		if (grammems[0].equals("V")) {
			for (String grammem : grammems) {
				if (grammem.contains("=partcp")) {
					for (String g : grammems) {
						if (g.equals("brev")) {
							return "PRTS";
						}
						if (g.equals("plen")) {
							return "PRTF";
						}
					}
				}
				if (grammem.contains("=inf")) {
					return "INFN";
				}
				if (grammem.contains("=ger")) {
					return "GRND";
				}
			}
		}
		if (grammems[0].equals("S")) {
			return "NOUN";
		}
		if (grammems[0].equalsIgnoreCase("num")) {
			return "NUMR";
		}
		if (grammems[0].equalsIgnoreCase("a-num")) {
			return "ADJF";
		}
		if (grammems[0].equalsIgnoreCase("adv")) {
			return "ADVB";
		}
		if (grammems[0].equalsIgnoreCase("praedic")) {
			return "PRED";
		}
		if (grammems[0].equalsIgnoreCase("parenth")) {
			return "CONJ";
		}
		if (grammems[0].equalsIgnoreCase("s-pro")) {
			return "NPRO";
		}
		if (grammems[0].equalsIgnoreCase("a-pro")) {
			return "ADJF";
		}
		if (grammems[0].equalsIgnoreCase("adv-pro")) {
			return "ADVB";
		}
		if (grammems[0].equalsIgnoreCase("praedic-pro")) {
			return "NPRO";
		}
		if (grammems[0].equalsIgnoreCase("pr")) {
			return "PREP";
		}
		if (grammems[0].equalsIgnoreCase("conj")) {
			return "CONJ";
		}
		if (grammems[0].equalsIgnoreCase("part")) {
			return "PRCL";
		}
		if (grammems[0].equalsIgnoreCase("intj")) {
			return "INTJ";
		}
		return null;
	}
	
	public static String getGrammems(Node node) {
		if (node.ELEMENT_NODE == node.getNodeType()) {
			Element element = (Element) node;
			Element ana = (Element) element.getFirstChild();
			return ana.getAttribute("gr");
		}
		return null;
	}
	
	public static String getLemma(Node node) {
		if (node.ELEMENT_NODE == node.getNodeType()) {
			Element element = (Element) node;
			Element ana = (Element) element.getFirstChild();
			return ana.getAttribute("lex").toLowerCase();
		}
		return null;
	}
	
	public static boolean isWrittenText(Document doc) {
		NodeList title = doc.getElementsByTagName("meta");
		for (int i = 0; i < title.getLength(); i++) {
			Node node = title.item(i);
			if (getContent(node).equals("ПК письменных текстов")) {
				return true;
			}					
		}
		return false;		
	}
	
	public static String getContent(Node node) {
		if (node.ELEMENT_NODE == node.getNodeType()) {
			Element element = (Element) node;
			return element.getAttribute("content");
		}
		return null;
	}
	
	public static String getFlex(String word) {
		if (word.length()<4) {
			return word;
		}
		else {
			return word.substring(word.length()-3);
		}
	}
	
	private static List<File> getXmlFiles(File folder) {
		List<File> folderFiles = new ArrayList<File>();
		File[] files = folder.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				boolean isTxt = name.endsWith(".xhtml");
				return isTxt;
			}
		});
		if (files != null) {
			folderFiles.addAll(Arrays.asList(files));
		}

		File[] folders = folder.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				boolean isFolder = dir.isDirectory();
				return isFolder;
			}

		});
		if (folders != null) {
			for (int f = 0; f < folders.length; f++) {
				File newFile = folders[f];
				List<File> filesList = getXmlFiles(newFile);
				folderFiles.addAll(filesList);
			}
		}
		return folderFiles;
	}
}

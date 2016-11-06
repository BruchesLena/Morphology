import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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

public class OpenCorpora {
	public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, ParserConfigurationException, SAXException, IOException {
		// TODO Auto-generated method stub
		List<Trigram> data = loadOpenCorpora();
		System.out.println("data size: " + data.size());
	}
	
	public static List<Trigram> loadOpenCorpora() throws ParserConfigurationException, UnsupportedEncodingException, FileNotFoundException, SAXException, IOException {
		List<Trigram> data = new ArrayList<>();
		File file  = new File("D:/annot.opcorpora.no_ambig.xml/annot.opcorpora.no_ambig.xml");
		DocumentBuilderFactory f  = DocumentBuilderFactory.newInstance();
		f.setValidating(false);
		DocumentBuilder builder = f.newDocumentBuilder();
		Document doc = builder.parse(new InputSource(new InputStreamReader(new FileInputStream(file),"UTF-8")));
		NodeList nodeList = doc.getElementsByTagName("token");
		for (int i = 0; i < nodeList.getLength(); i++) {
			System.out.println(i);
			Node node = nodeList.item(i);
			//String wordForm = getTextFromToken(node);
			if (i > 0 && i < nodeList.getLength()-1) {
				if (isPunctuationMark(getLemmaFromToken(nodeList.item(i-1))) || isNumber(getLemmaFromToken(nodeList.item(i-1)))||
						isPunctuationMark(getLemmaFromToken(nodeList.item(i+1))) || isNumber(getLemmaFromToken(nodeList.item(i+1))) ||
						isPunctuationMark(getLemmaFromToken(node)) || isNumber(getLemmaFromToken(node))){
							continue;
						}
				Trigram trigram = new Trigram();
				trigram.targetPartOfSpeech = getPOSFromToken(node);
				trigram.wordBefore = getTextFromToken(nodeList.item(i-1));
				trigram.wordAfter = getTextFromToken(nodeList.item(i+1));
				trigram.targetWord = getTextFromToken(node);
				trigram.partOfSpeechBefore = getTextFromToken(nodeList.item(i-1));
				trigram.partOfSpeechAfter = getTextFromToken(nodeList.item(i+1));
				trigram.allPossiblePos = getAllPossiblePos(getTextFromToken(node));
				trigram.substBefore = RusCorpora.setSubstitution(nodeList.item(i-1));
				trigram.targetSubst = RusCorpora.setSubstitution(node);
				trigram.substAfter = RusCorpora.setSubstitution(nodeList.item(i+1));
				data.add(trigram);
			}
		}
		//}
		return data;
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
	
	public static String getPOSFromToken(Node node) {
		if (node.ELEMENT_NODE == node.getNodeType()) {
			Element element = (Element) node;
			Element tfr = (Element) element.getFirstChild();
			Element v = (Element) tfr.getFirstChild();
			Element l = (Element) v.getFirstChild();
			Element g = (Element) l.getFirstChild();
			return g.getAttribute("v");
		}
		return null;
	}
	
	public static String getTextFromToken(Node node) {
		if (node.ELEMENT_NODE == node.getNodeType()) {
			Element element = (Element) node;
			return element.getAttribute("text").toLowerCase();
		}
		return null;
	}
	
	public static boolean isPunctuationMark(String word) {
		String[] marks = {".", ",", "?", "!", "\"", ":", ";", "-", "(", ")"};
		for (String mark : marks) {
			if (mark.equalsIgnoreCase(word)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isNumber(String word) {
		String[] numbers = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
		for (String number : numbers) {
			if (word.contains(number)) {
				return true;
			}
		}
		return false;
	}
	
	public static String getLemmaFromToken(Node node) {
		if (node.ELEMENT_NODE == node.getNodeType()) {
			Element element = (Element) node;
			Element tfr = (Element) element.getFirstChild();
			Element v = (Element) tfr.getFirstChild();
			Element l = (Element) v.getFirstChild();			
			return l.getAttribute("t");
		}
		return null;
	}
}

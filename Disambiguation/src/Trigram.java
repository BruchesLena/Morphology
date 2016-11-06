import java.util.HashSet;

public class Trigram {
	public String wordBefore;
	public String targetWord;
	public String wordAfter;
	public String targetPartOfSpeech;
	public String partOfSpeechBefore;
	public String partOfSpeechAfter;
	public HashSet<String> allPossiblePos;
	public HashSet<String> substBefore;
	public HashSet<String> targetSubst;
	public HashSet<String> substAfter;
}

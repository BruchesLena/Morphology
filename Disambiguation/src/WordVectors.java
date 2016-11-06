import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class WordVectors {
public HashMap<String, float[]>vectors=new HashMap<>();
	
	protected  int vector_size=200;
	
	public void load(BufferedReader rs) throws IOException{
		rs.readLine();
		while (true){
			try {
				String line=rs.readLine();
				if (line==null){
					break;
				}
				String[] split = line.split(" ");
				float[]dim=new float[split.length-1];
				for (int i=1;i<split.length;i++){
					dim[i-1]=Float.parseFloat(split[i]);
				}
				String s=split[0];
				if (s.indexOf('_')!=-1){
					s=s.substring(0, s.indexOf('_'));
				}
				vectors.put(s, dim);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
	}
	
	public static void main(String[] args) {
		WordVectors wordVectors = new WordVectors();
		try {
			wordVectors.load(new BufferedReader(new FileReader("C:/Avito/tt2")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void load() {
		try {
			this.load(new BufferedReader(new FileReader("C:/Avito/tt2")));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void load(String s) {
		try {
			this.load(new BufferedReader(new FileReader(s)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}


public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int[] test = {1,2,3,4,5,6};
		for (int i = 0; i < test.length; i++) {
			int buffer = test[i];
			test[i] = test[test.length-i-1];
			test[test.length-i-1] = buffer;
			if (i > test.length/2) {
				break;
			}
		}
		for (int i = 0; i < test.length; i++) {
			System.out.println(test[i]);
		}
	}

}


public class ArrayHelp {

	public static <A> boolean arrayContains(A[] arr, A obj) {
		for (A a : arr)
			if (a == obj)
				return true;
		return false;
	}

	public static boolean arrayContains(char[] arr, char obj) {
		for (char a : arr)
			if (a == obj)
				return true;
		return false;
	}
	
}

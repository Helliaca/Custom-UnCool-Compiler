
public interface names {
	public default boolean isTnames() {
		return false;
	}

	public default names[] getProduction(names n) {
		System.out.println("This is not supposed to run.");
		return null;
	}

	public default tnames[] First(names[] prod) {
		System.out.println("This is not supposed to run.");
		return null;
	}

}

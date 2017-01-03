
public interface names {
	public boolean isTnames();

	public names[] getProduction(tnames first, tnames follow);

	public tnames[] First(names[] prod);

}

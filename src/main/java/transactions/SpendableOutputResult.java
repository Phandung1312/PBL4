package transactions;

import java.util.List;
import java.util.Map;

public class SpendableOutputResult {
	private double accumulated;
	private Map<String,int[]> unSpendOuts;
	public SpendableOutputResult(double accumulated, Map<String,int[]> unSpendOuts) {
		this.accumulated = accumulated;
		this.unSpendOuts = unSpendOuts;
	}
	public double getAccumulated() {
		return accumulated;
	}
	public void setAccumulated(double accumulated) {
		this.accumulated = accumulated;
	}
	public Map<String, int[]> getUnSpendOuts() {
		return unSpendOuts;
	}
	public void setUnSpendOuts(Map<String, int[]> unSpendOuts) {
		this.unSpendOuts = unSpendOuts;
	}
	
}

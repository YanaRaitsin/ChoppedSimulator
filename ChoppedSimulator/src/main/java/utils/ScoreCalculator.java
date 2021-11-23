package utils;

public class ScoreCalculator {
	
	//saves the data of the inserted ingredients into different kitchen tools in the simulation to calculate the score after the user serve the dish.
	
	private String name;
	
	private int amount;
	
	public ScoreCalculator(String name, int amount) {
		this.name = name;
		this.amount = amount;
	}
	
	public ScoreCalculator(ScoreCalculator scoreCalculator) {
		this(scoreCalculator.getName(),scoreCalculator.getAmount());
	}

	public String getName() {
		return name;
	}

	public int getAmount() {
		return amount;
	}

	@Override
	public String toString() {
		return "name=" + name + ", amount=" + amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

}

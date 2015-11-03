package db;

public class Report {
	private int id;
	private String contents;
	private String opinion;

	public Report(int id, String contents, String opinion) {
		super();
		this.id = id;
		this.contents = contents;
		this.opinion = opinion;
	}

	public int getId() {
		return id;
	}

	public String getContents() {
		return contents;
	}

	public String getOpinion() {
		return opinion;
	}
}

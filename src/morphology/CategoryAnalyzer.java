package morphology;

/**
 * Last update 2015-10-30
 * 
 * ������ �Ұ� db ����, dbmanager �����
 * 
 */
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import java.util.List;
import java.util.StringTokenizer;

import db.Word;

public class CategoryAnalyzer {
	/**
	 * db���� ��Ҹ� �ҷ��� KomoranHelper �� ���¼ҷ� ���� ������ ��Һ� ī��Ʈ�� �Ѵ�
	 * 
	 */
	private List factorDic = new ArrayList<Factor>(); // �ڻ��غ� factor�� ���� �� ���

	private List<Factor> ambiguityDic = new ArrayList<>();
	private static int dicSize; // ���� ����

	private ArrayList<TextMsg> testMsg = new ArrayList<>();

	private db.DbManager dbmanager = db.DbManager.getInstance();

	private static String dbLoc = "D://IDE workspace//workspace//morphology//lib//models-light"; // ���¼�
																									// �м�
																									// ����
																									// ���

	private static String dicFileLoc = ".//lib/dic.txt";
	/**
	 * factor ���� ��� ��� // �����
	 */
	// txt���� ���
	// String text_file_loc="D:/morpholory_dir/sms.txt"; //sms ���� ���� ���� ���

	private KomoranHelper khelper;
	private Factor[] factors; // �迭�� factor����
	private StringTokenizer token;
	private WordClass[] sentense;

	private int totlaFactors;

	class TextMsg{
		
		protected String testMsg;
		protected boolean EC_parameter;
		
		public void TextMsg(){
			this.testMsg="";
			this.EC_parameter=false;
		}
		public void setTextMsg(String tmp_Str,boolean ec){
			this.testMsg=tmp_Str;
			this.EC_parameter=ec;
		}
	}
	class Factor {
		/**
		 * Factor class �� ���κ� ������ ����ִ�. factorName, factorCnt,list(�� ��Һ�
		 * ����(context)��)
		 */

		protected String factorName;
		protected int factorCnt;
		protected String[] exceptionlist;
		protected String[] wordlist;
		protected int EXsize;
		protected int WDsize;

		public Factor() {
			this.factorName = "";
			this.factorCnt = 0;
			this.EXsize = 0;
			this.WDsize=0;

		}

		public void setFactor(String str, int cnt,int wdsize,String[] wdlist_tmp, int exsize, String[] exlist_tmp) {
			/**
			 * �ϳ��� ���ο� ���� ä���, str,���� �̸� cnt,�󵵼� list, ���ο� ����Ǵ� ����(context)��(�̰�
			 * CategoryAnalyzer���� �޾ƿ´�)
			 */
			this.factorName = str;
			this.factorCnt = cnt;
			this.EXsize = exsize;
			this.WDsize = wdsize;
			this.exceptionlist = exlist_tmp;
			this.wordlist=wdlist_tmp;

		}// setFactor
	}// Factor class

	public String getWord(int index) {

		return "word from db";
	}

	String UninflectedWord = "/N";// ü�� NN, NP, NR (1)
	String Predicate = "/V"; // ��� VV, VA, VX, VC (2)
	String Susigeon = "/M";// ���ľ� (4)
	String Dongnibeon = "/IC"; // ������ (8)
	String RelateWord = "/J"; // ����� (16)
	String[] MorphemeDependency = { "/E", "/X" }; // �������¼� (���, ���λ�, ���̻�, ���)
													// (32,64)
	String Symbol = "/S"; // ��ȣ (128)

	class WordClass {
		/**
		 * WordClass �� ���¼ҵ��� ���Ͽ� ǰ��(���� ����)���� �з��Ѵ� ���� ������� �״� �ʿ� ����
		 */
		protected String str;
		protected int type;
		protected int component;

		public WordClass() {
			str = "";
			type = -1; // ü��(0), ���(1),���ľ�(2), ������(3), ����� (4), �������¼�(5), ��ȣ(6)
			component = 0;
		}

	}// WordClass class

	public CategoryAnalyzer() {

		khelper = new KomoranHelper();
		// factors = new Factor[]; // �ӽŷ��׿� �ѱ� factor ���� ���
		this.khelper.setKomoranDir(dbLoc);
		this.initKomoran();

	}

	public void initKomoran() {

		khelper.setKomoranDir(dbLoc);

	}

	public void getDBFactor() { // factor�� db���� �о�´�

		ArrayList<String> category_tmp = dbmanager.queryAllCategory();
		this.totlaFactors = category_tmp.size(); // db���� category table�� row ����

		this.factors = new Factor[this.totlaFactors];

		for (int i = 0; i < this.totlaFactors; i++) {

			factors[i] = new Factor();
			// tmp_wordlist_by_categoryID�� categoryID�� ������ wordlist�� �޾�
			// �´�ArrayList<Word>
			ArrayList<Word> tmp_wordlist_by_categoryID = dbmanager.queryWordList(i+1);
			
			// tmp_word_morphogy_before�� ���¼� �м� ��ģ ��� ����.
			//List<String> tmp_word_morphogy_before[] = new List[tmp_wordlist_by_categoryID.size()];
			// tmp_word_morphogy_�� tmp_word_morphlogy�� ���� ���� ��ȯ
			String tmp_word_morphogy[] = new String[tmp_wordlist_by_categoryID.size()];

			for (int j = 0; j < tmp_wordlist_by_categoryID.size(); j++) {
				tmp_word_morphogy[j] = new String();
				tmp_word_morphogy[j] = convertStrFormat(tmp_wordlist_by_categoryID.get(j).getWord(), false);
				// tmp_word_morphogy[j] =
				// khelper.getter().analyze(tmp_wordlist_by_categoryID.get(j).getWord());

				// tmp_word_morphogy[j]=convertStrFormat(tmp_str);
			}
			
			ArrayList<Word> tmp_exlist_by_categoryID = dbmanager.queryExceptionalWordList(i+1);
			String tmp_exword_morphogy[] = new String[tmp_exlist_by_categoryID.size()];
			
			for (int j = 0; j < tmp_exlist_by_categoryID.size(); j++) {
				tmp_exword_morphogy[j] = new String();
				tmp_exword_morphogy[j] = convertStrFormat(tmp_exlist_by_categoryID.get(j).getWord(), false);
			}
			factors[i].setFactor(category_tmp.get(i).toString(), 0, dbmanager.queryCountOfWordList(i+1),
					tmp_word_morphogy,dbmanager.queryCountOfExcepWordList(i+1),tmp_exword_morphogy);

			
		}//for
		
		/*
		 * for(int i=0;i<factors[0].list.size();i++)
		 * System.out.println(factors[0].list.get(i).getWord());
		 */
	}

	public void getExceptionalWord() {

		// dbmanager.
		// for(int i=0;i<tmp_wordlist.size();i++){
		// tmp_factor.list=
		// }

	}

	public String convertStrFormat(String bf, boolean blank) {// ���¼� �м� ����� ����
																// ���ϰ� ��ȯ
		StringTokenizer token;
		if (blank == true)
			token = new StringTokenizer(bf, "[=, ");
		else
			token = new StringTokenizer(bf, "[=,");

		String str = "";

		while (token.hasMoreTokens())
			str += token.nextToken();

		str = str.replace("first", "");
		str = str.replace("Pair", "");

		str = str.replace("second", "/");
		str = str.replace("]", "+");
		// System.out.println("conver_StrFormat:" + str);

		return str;

	}

	/*
	 * public void initFactor(Factor[] fact) { // Morpholoy class���� ����� factor
	 * �ʱ�ȭ
	 * 
	 * int len = fact.length; System.out.println("len:" + len); for (int i = 0;
	 * i < len; i++) { fact[i] = new Factor(); fact[i].factor_cnt = 0; }
	 * 
	 * fact[0].factor_name = "�ڻ� �غ� ���"; fact[0].negative = true;
	 * 
	 * fact[1].factor_name = "��ġ��"; fact[1].negative = true;
	 * 
	 * }
	 */
	public String readData(String str) {//

		/*
		 * ��� sns, sms ������?? �ܺ�txt���Ϸ� ���� �д��� �ƴϸ� �ǽð����� �������� �ְڳ�
		 * 
		 * �� �κ��� �� ����� ����� ����ϰ� �ϴ� ���� �ܰ質 ���� �ܰ迡�� �����ֱ� ���ؼ��� ���Ƿ�...
		 */
		// return str;
		return str;
	}

	public void compStr_wd(String cmp) {// ���� ���� �ܾ� �˻�
		System.out.println("CMPwd:" + cmp);
		int tmp_cnt = 0;
		int blank_count;
		for (int i = 0; i <this.totlaFactors; i++) { // ��ü factor�鿡 ���� �˻�
			blank_count = 1;
			for (int j = 0; j < factors[i].WDsize; j++) {// i�� ° factor�� ���� �˻�
				// System.out.println(factors[i].list[j]);
				
				
					for (int k = 0; k < factors[i].wordlist[j].length(); k++) {
						if (factors[i].wordlist[j].charAt(k) == ' ')
							blank_count++;
					} // for-k

				StringTokenizer token = new StringTokenizer(factors[i].wordlist[j], " ");
				
				while (token.hasMoreTokens()) {
					String str = token.nextToken();
					
					if (cmp.contains(str) == true) {
						System.out.print(str);
						System.out.print("-");
						System.out.println("Match!!"+i+j);
						tmp_cnt++;
						
					}else break;
				}
				if ((tmp_cnt != 0) && (blank_count == tmp_cnt)) {

					this.factors[i].factorCnt++;

					//System.out.println("Wordlist ���"+this.factors[i].factorName + " �� " + this.factors[i].factorCnt + "�� ����"+ "("+blank_count +":"+ tmp_cnt+")");
					
							
					break;
				}
				//System.out.println("" + blank_count + "/" + tmp_cnt);
				tmp_cnt = 0;
			} // for-j
		} // for-i

	}
	public boolean compStr_ex(String cmp) {// ���� ���� �ܾ� �˻�
		System.out.println("CMPex:" + cmp);
		int tmp_cnt = 0;
		boolean set=false;
		int blank_count;
		for (int i = 0; i <this.totlaFactors; i++) { // ��ü factor�鿡 ���� �˻�
			blank_count = 1;
			for (int j = 0; j < factors[i].EXsize; j++) {// i�� ° factor�� ���� �˻�
				// System.out.println(factors[i].list[j]);
				
				
					for (int k = 0; k < factors[i].exceptionlist[j].length(); k++) {
						if (factors[i].exceptionlist[j].charAt(k) == ' ')
							blank_count++;
					} // for-k

				StringTokenizer token = new StringTokenizer(factors[i].exceptionlist[j], " ");
				
				while (token.hasMoreTokens()) {
					String str = token.nextToken();
					
					if (cmp.contains(str) == true) {
						System.out.print(str);
						System.out.println("Match!!"+i+j);
						tmp_cnt++;
						
					}else break;
				}
				if ((tmp_cnt != 0) && (blank_count == tmp_cnt)) {

					this.factors[i].factorCnt++;

					System.out.println("exceptionWord ���:"+this.factors[i].factorName + " �� " + this.factors[i].factorCnt + "�� ����"+ "("+blank_count +":"+ tmp_cnt+")");
					
							
					set=true;
				}
				//System.out.println("" + blank_count + "/" + tmp_cnt);
				tmp_cnt = 0;
			} // for-j
		} // for-i
		return set;
	}

	public boolean checkExceptional(List<TextMsg> tm) { // '�ڻ� �غ�' ��� factor�� ���� �˻� ���� ��ü
		String str = "";
		System.out.println("ex���� ������:" + tm.size());
		boolean result=false;
		
		for (int j = 0; j < tm.size(); j++) { // �� factor�� word�� ����
			
			str+=tm.get(j).testMsg;
			if(tm.get(j).EC_parameter==true){
				
				result=compStr_ex(str);
				str="";
			}//if
		}//for
		if(str.equals("")) return result;
		else compStr_ex(str);
		return result;

	}
	public void checkWord(List<TextMsg> tm) { // '�ڻ� �غ�' ��� factor�� ���� �˻� ���� ��ü
		String str = "";
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println("wd���� ������:" + tm.size());
		for (int j = 0; j < tm.size(); j++) { // �� factor�� word�� ����

			 compStr_wd(tm.get(j).testMsg);// ����� factor�� �˻�

		}

	}

	// ���� �Ⱦ��� �Լ�
	public int defineType(String tmp) { // ���� ���⼭
		int val = 0;
		/*
		 * 1.�־�-ü�� 1 2.������- ����(VV), ���˻�(VA), (ü��+������(EC)) ->�� 3.����-
		 * (ü��+�ְ�����(JKS) 4.������ - ü��+ ����������(JKO) 5.�λ�� - (ü��+ �λ������(JKB)), �λ�??
		 * 6.������ - ������(MM), (ü��+ ����������(JKG))
		 */
		if (tmp.contains(UninflectedWord) == true) { // ü���̴ϱ�
			if (tmp.contains("/EC")) {
				System.out.println("ü��+������=������");
			} else if (tmp.contains("/JKS")) {
				System.out.println("ü��+�ְ�����=����");
			} else if (tmp.contains("/JKB")) {
				System.out.println("ü��+�λ������=�λ��");
			} else if (tmp.contains("/JKG")) {
				System.out.println("ü��+����������=������");
			}
		}
		if (tmp.contains(Predicate) == true)
			val += 2;// 2 ���
		if (tmp.contains(Susigeon) == true)
			val += 4;// 4 ���ľ�
		if (tmp.contains(Dongnibeon) == true)
			val += 8;// 8 ������
		if (tmp.contains(RelateWord) == true)
			val += 16;// 16 �����
		if (tmp.contains(MorphemeDependency[0]) == true)
			val += 32;// 32 ������1
		if (tmp.contains(MorphemeDependency[1]) == true)
			val += 64;// 64 ������2
		if (tmp.contains(Symbol) == true)
			val += 128;// 128 ��ȣ
		System.out.println("Test:" + val);

		this.checkType(val);

		return val;
	}

	// ���� �Ⱦ��� �Լ�
	public int checkType(int type) {// ���⼭
		/*
		 * String tmp =Integer.toBinaryString(type); //tmp=(BitSet)type;
		 * System.out.println("byte:"+tmp+" "+tmp.length()); int bit_level=0;
		 */
		/* ����� �ݴ�� */

		/*
		 * for(int i=tmp.length()-1;i>=0;i--){
		 * //System.out.println(tmp.charAt(i)); if(tmp.charAt(1)==1){//ü��
		 * if(tmp.charAt(5)==1){ System.out.println("ü��+������=������"); return 2; }
		 * 
		 * } bit_level++; }//for
		 */

		// if( ) return 1; //�־�
		return 1;
	}

	/**
	 * KomoranHeler�� ���� ������ �ְ� ���¼� �м��� ���� ����� �޾ƿ´�.
	 */
	public List morpology(String tmp) {

		return this.khelper.getter().analyze(tmp);
	}

	public void analyzingList(List tm) {

		/**
		 * ���¼� �м������ �ٷ�� ������ ���� ��ȯ�Ѵ�.
		 */
		TextMsg tmp;
		for (int i = 0; i < tm.size(); i++) {
			tmp=new TextMsg();
			String str=this.convertStrFormat(tm.get(i).toString(),true);
			
			tmp.setTextMsg(str,str.contains("/EC"));
			//System.out.print("**"+str+str.contains("/EC"));
			testMsg.add(tmp);
			System.out.println("*"+testMsg.get(i).testMsg+testMsg.get(i).EC_parameter);
		}
		/*
		  ǰ�� ���� �κ� ���� �ƴ� for (int i = 0; i < tm.size(); i++) {
		  this.sentense[i].str = this.convertStrFormat(tm.get(i).toString());
		  System.out.println(this.sentense[i].str); this.sentense[i].type =
		  this.defineType(this.sentense[i].str);
		  
		  this.checkType(this.sentense[i].type); 
		  }
		 */

	}

	// ���� �Ⱦ��� �Լ�
	/**
	 * ǰ�縦 ����� WordClass�� �ʱ�ȭ �ϴ� �Լ� ���� �Ƚᵵ �� ��
	 */
	/*
	 * public void initWord(int size) {
	 * 
	 * // �켱 inner class�� �迭�� ���� �Ҷ� �̷��� �ϰ�
	 * 
	 * sentense[0] = this.new WordClass(); // // �ʱ�ȭ�� ������ ���� Outer class�� ��ü��
	 * �������� �����!! this.sentense = new CategoryAnalyzer.WordClass[size]; for (int
	 * i = 0; i < size; i++) { this.sentense[i] = this.new WordClass(); } }
	 */


	public void addWordWithMP(String tmp) {

		List tmp_list = khelper.getter().analyze(tmp);
		String returnStr = "";
		for (int i = 0; i < tmp_list.size(); i++) {

			StringTokenizer token = new StringTokenizer(convertStrFormat(tmp_list.get(i).toString(), true), "+");

			while (token.hasMoreTokens()) {
				String str = token.nextToken();

				// ��� �κ� üũ
				if ((str.contains("/NNG") == true) || (str.contains("/NNP") == true) || (str.contains("/NP") == true)) {
					returnStr += str;
				} // ��� �κ� üũ
				else if ((str.contains("/VV") == true) || (str.contains("/VA") == true)) {
					returnStr += str;
				} // ��Ÿ(���) üũ
				else if ((str.contains("/XR") == true)) {
					returnStr += str;
				}
			} // while
		} // for
		System.out.println("word list�� ���� ��:" + returnStr);
		// 1 ��ſ� category ID ��������
		dbmanager.insertWord(returnStr, 1);
	}
	public void check(List<TextMsg> tm ){
		
		if(this.checkExceptional(tm)==false)
			this.checkWord(tm);
		System.out.println("�˻� ��� ����");
	}
	public void initTestMsg(){
		this.testMsg=new ArrayList<>();
	}
	public static void main(String[] args) {

		CategoryAnalyzer m = new CategoryAnalyzer();

		// m.addWordWithMP("�̿���");
		m.getDBFactor();// factor���� txt���� �� �о��

		/*
		for(int i=0;i<m.totlaFactors;i++){
			System.out.println(m.factors[i].EXsize);
			System.out.println(m.factors[i].WDsize);
			for(int j=0;j<m.factors[i].exceptionlist.length;j++)
				System.out.println(m.factors[i].exceptionlist[j]);
		}
		*/
		
		/* 
		 * ���� ���� ������ ���� 
		 * �װ�;�
		 * �����
		 * 
		 * 
		 */
		
		List<TextMsg> tm = m.morpology(m.readData("���� ���� ������ ���� ����� �װ�;�"));
		m.analyzingList(tm);

		m.check(m.testMsg);
/*		
		m.initTestMsg();
		m.analyzingList(m.morpology(m.readData("�װ�;�")));
		
		m.check(m.testMsg);
		
		m.initTestMsg();
		m.analyzingList(m.morpology(m.readData("�����")));
		
		m.check(m.testMsg);
		
		*/
		
		for(int i=0;i<m.totlaFactors;i++){
				if(m.factors[i].factorCnt>0)
					System.out.println(m.factors[i].factorName+" ��(��) "+m.factors[i].factorCnt+"�� "+"���ԵǾ� ����");
			
		}
		
		/*
		 * for (int i = 0; i < m.totlaFactors; i++) if (m.factors[i].factorCnt >
		 * 0) { System.out.println(m.factors[i].factorName + " �� " +
		 * m.factors[i].factorCnt + "�� ����"); }
		 */
	}
}// class

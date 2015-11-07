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
	private ArrayList<TextMsg> testMsg = new ArrayList<>();

	private db.DbManager dbmanager = db.DbManager.getInstance();

	private static String dbLoc = ".//lib//models-light"; // ���¼�

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

	class TextMsg {

		protected String testMsg;

		protected boolean checked;
		protected int start;
		protected int end;
		protected int ex_number;
		protected int wd_number;

		protected int ing;

		public void TextMsg() {
			this.testMsg = "";

			this.checked = false;
			this.start = this.ing = this.end = this.ex_number = this.wd_number = -1;
		}

		public void setTextMsg(String tmp_Str) {
			this.testMsg = tmp_Str;

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
			this.WDsize = 0;

		}

		public void setFactor(String str, int cnt, int wdsize, String[] wdlist_tmp, int exsize, String[] exlist_tmp) {
			/**
			 * �ϳ��� ���ο� ���� ä���, str,���� �̸� cnt,�󵵼� list, ���ο� ����Ǵ� ����(context)��(�̰�
			 * CategoryAnalyzer���� �޾ƿ´�)
			 */
			this.factorName = str;
			this.factorCnt = cnt;
			this.EXsize = exsize;
			this.WDsize = wdsize;
			this.exceptionlist = exlist_tmp;
			this.wordlist = wdlist_tmp;

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
			ArrayList<Word> tmp_wordlist_by_categoryID = dbmanager.queryWordList(i + 1);

			// tmp_word_morphogy_before�� ���¼� �м� ��ģ ��� ����.
			// List<String> tmp_word_morphogy_before[] = new
			// List[tmp_wordlist_by_categoryID.size()];
			// tmp_word_morphogy_�� tmp_word_morphlogy�� ���� ���� ��ȯ
			String tmp_word_morphogy[] = new String[tmp_wordlist_by_categoryID.size()];

			for (int j = 0; j < tmp_wordlist_by_categoryID.size(); j++) {
				tmp_word_morphogy[j] = new String();
				tmp_word_morphogy[j] = convertStrFormat(tmp_wordlist_by_categoryID.get(j).getWord(), false);
				// tmp_word_morphogy[j] =
				// khelper.getter().analyze(tmp_wordlist_by_categoryID.get(j).getWord());

				// tmp_word_morphogy[j]=convertStrFormat(tmp_str);
			}

			ArrayList<Word> tmp_exlist_by_categoryID = dbmanager.queryExceptionalWordList(i + 1);
			String tmp_exword_morphogy[] = new String[tmp_exlist_by_categoryID.size()];

			for (int j = 0; j < tmp_exlist_by_categoryID.size(); j++) {
				tmp_exword_morphogy[j] = new String();
				tmp_exword_morphogy[j] = convertStrFormat(tmp_exlist_by_categoryID.get(j).getWord(), false);
			}
			factors[i].setFactor(category_tmp.get(i).toString(), 0, dbmanager.queryCountOfWordList(i + 1),
					tmp_word_morphogy, dbmanager.queryCountOfExcepWordList(i + 1), tmp_exword_morphogy);

		} // for

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
		/**
		 * ��������� �ڸ���! ������ �޾ƿͼ� word list �˻��Ѵ�.
		 * 
		 */
		System.out.println("CMPwd:" + cmp);
		int tmp_cnt = 0;
		int blank_count;
		for (int i = 0; i < this.totlaFactors; i++) { // ��ü factor�鿡 ���� �˻�
			blank_count = 1;
			for (int j = 0; j < factors[i].WDsize; j++) {// i�� ° factor�� ���� �˻�
				// System.out.println(factors[i].list[j]);

				StringTokenizer token = new StringTokenizer(factors[i].wordlist[j], " ");
				blank_count = token.countTokens();
				while (token.hasMoreTokens()) {
					String str = token.nextToken();

					if (cmp.contains(str) == true) {
						System.out.print(str);
						System.out.print("-");
						tmp_cnt++;
						System.out.println("Match!!" + i + j + "blank:" + blank_count + "/" + tmp_cnt);

					}
				}
				if ((tmp_cnt != 0) && (blank_count == tmp_cnt)) {

					this.factors[i].factorCnt++;

//					System.out.println("Wordlist" + this.factors[i].factorName + " �� " + this.factors[i].factorCnt
//							+ "�� ����" + "(" + blank_count + ":" + tmp_cnt + ")");

					break;
				}
		
				tmp_cnt = 0;
			} // for-j
		} // for-i

	}

	public int compStr_ex(int factorIndex) {
		// TODO compStr_Ex
		/**
		 * ����(EC)������ �ڸ���! ������ �޾ƿͼ� exception word list �˻��Ѵ�.
		 * 
		 */

		int tmp_cnt = 0;
		boolean set = false;
		int blank_count;

		int result = 0;
		int start = -1;
		int end = -1;
		// List<String> factorString = new ArrayList<String>();

		for (int i = 0; i < this.factors[factorIndex].EXsize; i++) {
			/* �� factor�� ����ִ� �� ���ڸ�ŭ ���Ѵ�. */

			String[] factorStrList = this.factors[factorIndex].exceptionlist;

			StringTokenizer token = new StringTokenizer(factorStrList[i], " ");
			blank_count = token.countTokens();
			List<String> strList = new ArrayList<>();

			while (token.hasMoreTokens()) {
				strList.add(token.nextToken());
			}

			for (int j = start + 1; j < this.testMsg.size(); j++) {
//				System.out.println("tmp_cnt:" + tmp_cnt + "strList:" + strList.size() + strList.get(0));
				for (int k = 0; k < blank_count; k++) {
					set = this.testMsg.get(j).testMsg.contains(strList.get(k));

					if (set == true) {
						tmp_cnt++;
						if (start == -1)
							start = j;

						System.out.println(set + this.testMsg.get(j).testMsg + tmp_cnt + "/" + blank_count + "/" + j);
					}

					if (tmp_cnt == blank_count) {
						end = j;
						System.out.println("fully matched" + (factorIndex + 1) + "   from" + start + "to" + j);
						System.out.println();
						result++;
						tmp_cnt = 0;

						for (int l = start; l <= end; l++) {
							this.testMsg.get(l).checked = true;
							this.testMsg.get(l).ex_number = (factorIndex + 1);
						}
						start = -1;
						end = -1;
						break;
					}
				}
			} // for-j
		} // while

		if (result > 0)
			System.out.println("result" + result + "about" + (factorIndex + 1));
		return result;
	}

	/**
	 * @param tm
	 * @return
	 */
	public void checkExceptional() {

		// TODO checkException �κ�
		String str = new String();
		System.out.println("ex���� ������:" + this.testMsg.size());

		for (int i = 0; i < this.totlaFactors; i++) {// �� factor �� ������ ���� �˻� �ϰ�
														// check ǥ�ø� �Ѵ�.
			this.factors[i].factorCnt += compStr_ex(i);

		}

	}

	public void checkWord() { // '�ڻ� �غ�' ��� factor�� ���� �˻� ���� ��ü
		/**
		 * ���� ��ü�� �޾Ƽ� exception word list �˻縦 ���� ���¼� �м��Ⱑ �ڸ� ���� ������ ������ �ڸ��ϴ�.
		 * ��/NP��/JK + ��/VV+��/EC (2 ���)
		 * 
		 * ���������� �ڸ��� ������ ������ ó�� �ʿ���
		 */
		String str = "";
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println("wd���� ������:" + this.testMsg.size());
		for (int j = 0; j < this.testMsg.size(); j++) { // �� factor�� word�� ����

			if (this.testMsg.get(j).checked == false) {
				System.out.print(this.testMsg.get(j).checked);
				compStr_wd(this.testMsg.get(j).testMsg);// ����� factor�� �˻�
			}
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
			tmp = new TextMsg();
			String str = this.convertStrFormat(tm.get(i).toString(), true);

			tmp.setTextMsg(str);
			// System.out.print("**"+str+str.contains("/EC"));
			testMsg.add(tmp);
			System.out.println("*" + testMsg.get(i).testMsg);
		}
		/*
		 * ǰ�� ���� �κ� ���� �ƴ� for (int i = 0; i < tm.size(); i++) {
		 * this.sentense[i].str = this.convertStrFormat(tm.get(i).toString());
		 * System.out.println(this.sentense[i].str); this.sentense[i].type =
		 * this.defineType(this.sentense[i].str);
		 * 
		 * this.checkType(this.sentense[i].type); }
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

	public void addWordWithMP(String tmp, int index) {

		List tmp_list = khelper.getter().analyze(tmp);
		String returnStr = "";
		// System.out.println(tmp_list.get(0));
		for (int i = 0; i < tmp_list.size(); i++) {

			StringTokenizer token = new StringTokenizer(convertStrFormat(tmp_list.get(i).toString(), true), "+");

			while (token.hasMoreTokens()) {
				String str = token.nextToken();

				// ��� �κ� üũ
				if ((str.contains("/NNG") == true) || (str.contains("/NNP") == true) || (str.contains("/NP") == true)) {
					returnStr += str;
					returnStr += " ";
				} // ��� �κ� üũ
				else if ((str.contains("/VV") == true) || (str.contains("/VA") == true)
						|| (str.contains("/VX") == true)) {
					returnStr += str;
					returnStr += " ";
				} // ��Ÿ(���) üũ
				else if ((str.contains("/XR") == true)) {
					returnStr += str;
					returnStr += " ";
				} // �λ�? <��> ��ڴ�
				else if ((str.contains("/MAG") == true)) {
					returnStr += str;
					returnStr += " ";
				}
			} // while
		} // for
		System.out.println("word list�� ���� ��:" + returnStr);
		// 1 ��ſ� category ID ��������
		dbmanager.insertWord(returnStr, index);
	}

	/**
	 * ec�κ��� �߶� �ִ´�. 1. ���� �κ��� ���ܵд�. �� ���簡 ��� ���Ŀ� ���� �ǹ̰� �޶����� exception���� ����
	 * ex)������ ���� �ȴ�.->'����'�� �ǹ̿� �߿��ϱ⿡ exception����
	 * 
	 * 2. ù ������ �ٸ� ī�װ��� �ٽ� ������ ���� ��ġ�� ��� (�ڻ������� �ູ~~=exceptional, �ູ��
	 * �ູ=wordlist)
	 * 
	 * @param tmp
	 *            exception wordlist�� ���� String
	 * @param index
	 *            category_id
	 */
	public void addExceptionWithMP(String tmp, int index) {

		List tmp_list = khelper.getter().analyze(tmp);
		String returnStr = "";
		// System.out.println(tmp_list.get(0));
		for (int i = 0; i < tmp_list.size(); i++) {

			StringTokenizer token = new StringTokenizer(convertStrFormat(tmp_list.get(i).toString(), true), "");

			while (token.hasMoreTokens()) {

				String str = token.nextToken();

				str = this.stringCut(str, "/EC");
				str = this.stringCut(str, "/ETM");
				returnStr += str;

			} // while
			returnStr += " ";
		} // for
		System.out.println("exception list�� ���� ��:" + returnStr);
		// 1 ��ſ� category ID ��������
		dbmanager.insertExceptionalWord(returnStr, index);
	}

	public String stringCut(String origin, String cut) {
		if (origin.contains(cut)) {
			String cutter = "";
			int ec_index = origin.lastIndexOf(cut);
			int start = -1;

			for (int j = ec_index; j >= 0; j--)
				if (origin.charAt(j) == '+') {
					start = j;
					break;
				} // if
			for (int j = 0; j < start; j++)
				cutter += origin.charAt(j);

			origin = cutter;
		}

		return origin;
	}

	public void check() {

		this.checkExceptional();
		this.checkWord();

		System.out.println("�˻� ��� ����");
	}

	public void initTestMsg() {
		this.testMsg = new ArrayList<>();
	}

	public ArrayList<Integer> categoryAnalyzer(String tmp) {

		this.getDBFactor();
		List<TextMsg> tm = this.morpology(tmp);
		this.analyzingList(tm);

		this.check();
		ArrayList<Integer> result = new ArrayList<>();

		for (int i = 0; i < this.totlaFactors; i++) {
			result.add(this.factors[i].factorCnt);

			if (this.factors[i].factorCnt > 0) {
				System.out
						.println(this.factors[i].factorName + " ��(��) " + this.factors[i].factorCnt + "�� " + "���ԵǾ� ����");

			}
		}

		return result;
	}

	public void listSetting() {

		this.addExceptionWithMP("�����Կ�", 1);
		this.addExceptionWithMP("�ڻ� �Ͻ÷���", 1);
		this.addExceptionWithMP("�״� ����", 1);
		this.addExceptionWithMP("������� �Դϴ�", 1);

		this.addExceptionWithMP("�ڻ��Դϴ�", 1);
		this.addExceptionWithMP("���� �����ϴ�", 1);

		this.addExceptionWithMP("������ ��ó", 2);
		this.addExceptionWithMP("����ϴ�", 2);
		this.addExceptionWithMP("�ູ���� �ʳ�", 2);
		this.addExceptionWithMP("������ ������", 2);
		this.addExceptionWithMP("����� ����", 2);
		this.addExceptionWithMP("�������� ��", 2);
		this.addExceptionWithMP("�� ���� ����� ������ ���ϰ�", 2);
		this.addExceptionWithMP("������ ���� ���ſ�", 2);
		this.addExceptionWithMP("���� ���뵵 ��Ƹ� �� ����", 2);
		this.addExceptionWithMP("������ ���� �ȴ�.", 2);

		this.addExceptionWithMP("�ٸ� ���� ���� �;�", 3);

		this.addExceptionWithMP("�ڽ��� ��Ű�� �ʹ�", 4);
		this.addExceptionWithMP("���ư� �� ������", 4);

		this.addExceptionWithMP("�λ��� �����ϰ�", 6);
		this.addExceptionWithMP("���� ��", 6);
		this.addExceptionWithMP("���� �� ������", 6);
		this.addExceptionWithMP("���� ��Ҿ�", 6);
		this.addExceptionWithMP("���� �༮�� ������", 6);
		this.addExceptionWithMP("���� ���̾Ͼ�", 6);

		this.addExceptionWithMP("���� �������� ������", 7);
		this.addExceptionWithMP("�������� ���� ������", 7);
		this.addExceptionWithMP("��ó�� �༭ �̾��ϴ�", 7);

		this.addExceptionWithMP("�뼭���� �����ž�", 9);
		this.addExceptionWithMP("�뼭�� �� ����", 9);

		this.addExceptionWithMP("�ູ�ϰ� �缼��", 10);
		this.addExceptionWithMP("�̿� ������", 10);
		this.addExceptionWithMP("�뼭�ϼ���", 10);
		this.addExceptionWithMP("�ҽ��߾��", 10);
		this.addExceptionWithMP("������ ������", 10);
		this.addExceptionWithMP("�������� ����", 10);
		this.addExceptionWithMP("�̾������� ����", 10);
		this.addExceptionWithMP("�������� ����", 10);

		this.addExceptionWithMP("�Ϸ縦 ����", 11);

		this.addExceptionWithMP("���� ȭ���� �ؼ�", 12);
		this.addExceptionWithMP("���� ���� ��", 12);
		this.addExceptionWithMP("ȭ�����ּ���", 12);

		this.addExceptionWithMP("������ �ҿ�", 14);

		this.addExceptionWithMP("��Ƶ� ��°� �ƴϴ�", 15);

		this.addExceptionWithMP("�̷��� ���� ��", 20);

		this.addExceptionWithMP("�� �־� �־���", 24);

		this.addExceptionWithMP("�� ��", 26);

		this.addExceptionWithMP("���� ����", 27);

		this.addExceptionWithMP("������ ����", 28);

		this.addExceptionWithMP("����� ����", 34);

		this.addExceptionWithMP("�� ������", 36);
		this.addExceptionWithMP("�ʸ� �ƴϸ�", 36);
		this.addExceptionWithMP("�ʷ� ����", 36);
		this.addExceptionWithMP("�ʸ� �����ٸ�", 36);

		this.addWordWithMP("���� �ھ�", 1); // ����/NNG �ھ�/NNG
		this.addWordWithMP("���� ����", 1); // ����/NNG ����/VV
		this.addWordWithMP("��� ����", 1);
		this.addWordWithMP("�� ��ڴ�", 1);

		this.addWordWithMP("��ư� ��ġ�� �� ������", 2);
		this.addWordWithMP("��Ⱑ �Ⱦ�", 2);
		this.addWordWithMP("�� �ڽ��� ���", 2);
		this.addWordWithMP("��°� ���ܿ�", 2);
		this.addWordWithMP("��ó�� �޾Ƽ�", 2);
		this.addWordWithMP("��ư� ������ ����", 2);
		this.addWordWithMP("�ϴ��� ��������", 2);
		this.addWordWithMP("�������� ������", 2);
		this.addWordWithMP("��Ƽ�� �����", 2);
		this.addWordWithMP("��Ⱑ ���뽺����", 2);
		this.addWordWithMP("������", 2);
		this.addWordWithMP("���� �ɴ޷�", 2);
		this.addWordWithMP("���Ӵ�", 2);
		this.addWordWithMP("�����ϴ�", 2);
		this.addWordWithMP("�ƹ��͵� �� �� ����", 2);
		this.addWordWithMP("�������� �� ����", 2);
		this.addWordWithMP("��Ⱑ �����", 2);
		this.addWordWithMP("������ �ʾ�����", 2);

		this.addWordWithMP("�װ� �ʹٴ� ����", 3);
		this.addWordWithMP("������ ������", 3);
		this.addWordWithMP("������ ��������", 3);
		this.addWordWithMP("���� ����", 3);
		this.addWordWithMP("�浿�� �ڻ�", 3);
		this.addWordWithMP("������ �����ϱ⵵", 3);
		this.addWordWithMP("�λ� ��", 3);
		this.addWordWithMP("�׾�� ��", 3);
		this.addWordWithMP("������ ���ߴ���", 3);
		this.addWordWithMP("�׾�� ��", 3);
		this.addWordWithMP("���� ��", 3);
		this.addWordWithMP("�ٽ� �¾��", 3);
		this.addWordWithMP("���̰� �ϰ�", 3);
		this.addWordWithMP("�װ� ����", 3);
		this.addWordWithMP("�װŵ�", 3);
		this.addWordWithMP("ȯ���Ѵٸ�", 3);
		this.addWordWithMP("���� �����", 3);
		this.addWordWithMP("���� �׾������", 3);
		this.addWordWithMP("����", 3);
		this.addWordWithMP("���� �� ����", 3);
		this.addWordWithMP("�ڻ� ����", 3);
		this.addWordWithMP("�����ڻ�", 3);
		this.addWordWithMP("���� ����", 3);
		this.addWordWithMP("���� ����", 3);
		this.addWordWithMP("�ױ�� ����", 3);
		this.addWordWithMP("�ױ� ���� ���", 3);
		this.addWordWithMP("�ڻ�", 3);
		this.addWordWithMP("�־��� ����", 3);
		this.addWordWithMP("���� ����", 3);

		this.addWordWithMP("�̴�� �ױ⿣", 4);
		this.addWordWithMP("�ۿ� �� ��", 4);
		this.addWordWithMP("�㹫��", 4);
		this.addWordWithMP("������ �ƽ�����", 4);
		this.addWordWithMP("�̷�", 4);
		this.addWordWithMP("��� �ʹ�", 4);
		this.addWordWithMP("�̷�", 4);
		this.addWordWithMP("������ ���ư���", 4);

		this.addWordWithMP("�ױ�� �����Ѱ�", 5);
		this.addWordWithMP("������ ���", 5);

		this.addWordWithMP("����� ����������", 6);
		this.addWordWithMP("�����Ҵ�", 6);
		this.addWordWithMP("���� �ȴ�", 6);
		this.addWordWithMP("���� �Ӵ�", 6);
		this.addWordWithMP("���θ� ���Ѵ�", 6);
		this.addWordWithMP("�����ϴ�", 6);
		this.addWordWithMP("�� �׾�� �� �ΰ�", 6);
		this.addWordWithMP("�ڽ��� �̿���", 6);
		this.addWordWithMP("�� ����", 6);
		this.addWordWithMP("��� �����ϸ鼭", 6);
		this.addWordWithMP("��ȸ�ȴ�", 6);
		this.addWordWithMP("���� �߸�", 6);
		this.addWordWithMP("���� ���� ��", 6);
		this.addWordWithMP("�� �߸�", 6);

		this.addWordWithMP("�˼��ؿ�", 7);
		this.addWordWithMP("�̾���", 7);
		this.addWordWithMP("����帳�ϴ�", 7);
		this.addWordWithMP("���˵帳�ϴ�", 7);
		this.addWordWithMP("�ż��� ����", 7);

		this.addWordWithMP("�Ⱦ����", 8);
		this.addWordWithMP("�̿���", 8);
		this.addWordWithMP("����", 8);
		this.addWordWithMP("�ƹ��� ���� ���ϰھ�", 8);

		this.addWordWithMP("����", 9);
		this.addWordWithMP("����", 9);
		this.addWordWithMP("�׿�������", 9);

		this.addWordWithMP("���� ���󿡼�", 11);
		this.addWordWithMP("õ��", 11);
		this.addWordWithMP("ü��", 11);
		this.addWordWithMP("������", 11);

		this.addWordWithMP("�� ��ʽ�", 12);
		this.addWordWithMP("������ ��", 12);
		this.addWordWithMP("ȭ���� ��Ź", 12);
		this.addWordWithMP("�� ��ʽ�", 12);
		this.addWordWithMP("������", 12);
		this.addWordWithMP("3����", 12);
		this.addWordWithMP("����", 12);
		this.addWordWithMP("�������", 12);
		this.addWordWithMP("��ü�� ����", 12);
		this.addWordWithMP("�ýű���", 12);
		this.addWordWithMP("���� ����", 12);

		this.addWordWithMP("�� �� ����", 13);

		this.addWordWithMP("�� ������", 14);
		this.addWordWithMP("�ؾ��ֱ�", 14);
		this.addWordWithMP("���� ����", 14);
		this.addWordWithMP("�� ����� ���", 14);
		this.addWordWithMP("�ȶ����� �����ּ���", 14);
		this.addWordWithMP("�����ּ���", 14);
		this.addWordWithMP("��Ź�帳�ϴ�", 14);
		this.addWordWithMP("�����ּ���", 14);
		this.addWordWithMP("ó���� �ֱ�", 14);
		this.addWordWithMP("�ذ��� �ּ���", 14);
		this.addWordWithMP("�˸��� ���ƶ�", 14);

		this.addWordWithMP("����", 15);
		this.addWordWithMP("���� ��", 15);
		this.addWordWithMP("�ߵ�� ���� ���", 15);
		this.addWordWithMP("���ο� �ð�", 15);
		this.addWordWithMP("������", 15);
		this.addWordWithMP("������ �귯", 15);
		this.addWordWithMP("�����", 15);

		this.addWordWithMP("������", 16);
		this.addWordWithMP("���� ����", 16);
		this.addWordWithMP("���ڿ� �ߵ�", 16);

		this.addWordWithMP("ġ��", 17);

		this.addWordWithMP("�����", 18);
		this.addWordWithMP("�����", 18);
		this.addWordWithMP("���", 18);

		this.addWordWithMP("�ܷӴ�", 19);
		this.addWordWithMP("ȥ�ڴ�", 19);
		this.addWordWithMP("����", 19);
		this.addWordWithMP("ȥ�ڴ�", 19);
		this.addWordWithMP("�����ϰ� ����", 19);
		this.addWordWithMP("���� �ܸ�", 19);

		this.addWordWithMP("�Ҿ�", 20);
		this.addWordWithMP("����", 20);
		this.addWordWithMP("����", 20);
		this.addWordWithMP("�ٽ�", 20);

		this.addWordWithMP("��ȿ�ڽ�", 21);
		this.addWordWithMP("���� �Ǽ�", 21);
		this.addWordWithMP("��", 21);
		this.addWordWithMP("�˰�", 21);

		this.addWordWithMP("���", 22);
		this.addWordWithMP("�ǻ�", 22);
		this.addWordWithMP("�Ϳ���", 22);
		this.addWordWithMP("�Ⱦ��ִ�", 22);
		this.addWordWithMP("������", 22);
		this.addWordWithMP("����", 22);
		this.addWordWithMP("����", 22);
		this.addWordWithMP("����", 22);
		this.addWordWithMP("�̻�", 22);
		this.addWordWithMP("�Ƹ��ٿ�", 22);

		this.addWordWithMP("��� ����", 23);
		this.addWordWithMP("�����", 23);
		this.addWordWithMP("���� �ǹ�", 23);
		this.addWordWithMP("����", 23);
		this.addWordWithMP("�ູ��", 23);
		this.addWordWithMP("�ҹ���", 23);
		this.addWordWithMP("����", 23);
		this.addWordWithMP("����", 23);
		this.addWordWithMP("������", 23);
		this.addWordWithMP("������", 23);
		this.addWordWithMP("���", 23);

		this.addWordWithMP("�����", 24);
		this.addWordWithMP("��մ�", 24);
		this.addWordWithMP("ȯ��", 24);
		this.addWordWithMP("�ູ", 24);
		this.addWordWithMP("���", 24);
		this.addWordWithMP("�¸�", 24);
		this.addWordWithMP("��ڴ�", 24);
		this.addWordWithMP("����", 24);
		this.addWordWithMP("��Ȱ��", 24);
		this.addWordWithMP("��մ�", 24);
		this.addWordWithMP("���� ����", 24);
		this.addWordWithMP("�غ��ϴ�", 24);
		this.addWordWithMP("�̰��", 24);
		this.addWordWithMP("��", 24);

		this.addWordWithMP("�������̺�", 25);
		this.addWordWithMP("�ÿ��ÿ���", 25);
		this.addWordWithMP("������", 25);
		this.addWordWithMP("������", 25);
		this.addWordWithMP("������", 25);
		this.addWordWithMP("�ÿ���", 25);
		this.addWordWithMP("�ķ���", 25);
		this.addWordWithMP("�� �ո���", 25);

		this.addWordWithMP("�ڶ�������", 26);
		this.addWordWithMP("�ҽ��ִ�", 26);
		this.addWordWithMP("����", 26);
		this.addWordWithMP("�����", 26);
		this.addWordWithMP("�س´�", 26);
		this.addWordWithMP("������", 26);
		this.addWordWithMP("�ڽ��ִ�", 26);
		this.addWordWithMP("����������", 26);
		this.addWordWithMP("�ѵ���", 26);
		this.addWordWithMP("�Ǳ�����", 26);

		this.addWordWithMP("�����ο�", 27);
		this.addWordWithMP("������", 27);
		this.addWordWithMP("��ȭ�ο�", 27);
		this.addWordWithMP("������", 27);
		this.addWordWithMP("ħ����", 27);
		this.addWordWithMP("�ȵ�������", 27);
		this.addWordWithMP("�Ƚ�", 27);
		this.addWordWithMP("������ ���̴�", 27);
		this.addWordWithMP("������", 27);

		this.addWordWithMP("����", 28);
		this.addWordWithMP("���", 28);
		this.addWordWithMP("��Ŭ�ϴ�", 28);
		this.addWordWithMP("���", 28);
		this.addWordWithMP("����������", 28);
		this.addWordWithMP("������ ����", 28);
		this.addWordWithMP("����", 28);
		this.addWordWithMP("������ �����̴�", 28);

		this.addWordWithMP("���� ����", 29);
		this.addWordWithMP("¥����", 29);
		this.addWordWithMP("����", 29);
		this.addWordWithMP("��еǴ�", 29);
		this.addWordWithMP("�ų���", 29);
		this.addWordWithMP("������ �ڴ�", 29);
		this.addWordWithMP("����߶�", 29);
		this.addWordWithMP("�߶���", 29);
		this.addWordWithMP("����ռ���", 29);
		this.addWordWithMP("��������", 29);
		this.addWordWithMP("������ ��ġ��", 29);

		this.addWordWithMP("����", 30);
		this.addWordWithMP("���", 30);
		this.addWordWithMP("��¦", 30);
		this.addWordWithMP("������", 30);
		this.addWordWithMP("����", 30);

		this.addWordWithMP("�������� ����", 31);
		this.addWordWithMP("������", 31);
		this.addWordWithMP("�׸���", 31);
		this.addWordWithMP("����ʹ�", 31);
		this.addWordWithMP("��ƶ�ϴ�", 31);
		this.addWordWithMP("�Ʒ��ϴ�", 31);
		this.addWordWithMP("�����ϴ�", 31);

		this.addWordWithMP("����", 32);
		this.addWordWithMP("�ú��� ����", 32);
		this.addWordWithMP("�ñ��ϴ�", 32);
		this.addWordWithMP("�û��ϴ�", 32);
		this.addWordWithMP("������", 32);

		this.addWordWithMP("������", 33);
		this.addWordWithMP("����", 33);
		this.addWordWithMP("����", 33);
		this.addWordWithMP("����", 33);
		this.addWordWithMP("�η���", 33);
		this.addWordWithMP("������", 33);
		this.addWordWithMP("������", 33);
		this.addWordWithMP("����", 33);
		this.addWordWithMP("����", 33);
		this.addWordWithMP("�Ҹ�", 33);

		this.addWordWithMP("¥��", 34);
		this.addWordWithMP("ȭ�� ����", 34);
		this.addWordWithMP("�г�", 34);
		this.addWordWithMP("�μ��� �ʹ�", 34);
		this.addWordWithMP("���", 34);
		this.addWordWithMP("����", 34);
		this.addWordWithMP("���", 34);

		this.addWordWithMP("¡�׷���", 35);
		this.addWordWithMP("������", 35);
		this.addWordWithMP("����", 35);
		this.addWordWithMP("�� ���´�", 35);
		this.addWordWithMP("���", 35);
		this.addWordWithMP("���ܿ�", 35);

		this.addWordWithMP("¡�׷���", 36);
		System.out.println("list setting complete");
	}

	public static void main(String[] args) {

		CategoryAnalyzer m = new CategoryAnalyzer();
		// m.listSetting();

		// String testStr = "�ʸ� �ƴϸ�";
		// List test = m.khelper.getter().analyze(testStr);
		//
		// for (int i = 0; i < test.size(); i++) {
		// System.out.println(m.convertStrFormat(test.get(i).toString(), true));
		//
		// }
		// m.addExceptionWithMP(testStr, 0);
		// m.addWordWithMP(testStr, 0);

		// m.addExceptionWithMP(" ", 0);
		// m.addWordWithMP(testStr, 0);

		// TODO �˻� �޼����� ���⼭
		m.categoryAnalyzer("�ʹ� ������ ��ó �̷��� ���´� �㹫�� ���� ���� �����ΰ�");

	}// main

}// class

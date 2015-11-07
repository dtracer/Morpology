package morphology;

/**
 * Last update 2015-10-30
 * 
 * 앞으로 할것 db 연결, dbmanager 만들기
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
	 * db에서 요소를 불러와 KomoranHelper 가 형태소로 나눈 문장을 요소별 카운트를 한다
	 * 
	 */
	private ArrayList<TextMsg> testMsg = new ArrayList<>();

	private db.DbManager dbmanager = db.DbManager.getInstance();

	private static String dbLoc = ".//lib//models-light"; // 형태소

	/**
	 * factor 파일 상대 경로 // 저장된
	 */
	// txt파일 경로
	// String text_file_loc="D:/morpholory_dir/sms.txt"; //sms 내용 담은 파일 경로

	private KomoranHelper khelper;
	private Factor[] factors; // 배열로 factor관리
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
		 * Factor class 각 요인별 정보가 들어있다. factorName, factorCnt,list(각 요소별
		 * 문맥(context)들)
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
			 * 하나의 요인에 값을 채운다, str,요인 이름 cnt,빈도수 list, 요인에 적용되는 문구(context)들(이건
			 * CategoryAnalyzer에서 받아온다)
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

	String UninflectedWord = "/N";// 체언 NN, NP, NR (1)
	String Predicate = "/V"; // 용언 VV, VA, VX, VC (2)
	String Susigeon = "/M";// 수식언 (4)
	String Dongnibeon = "/IC"; // 독립언 (8)
	String RelateWord = "/J"; // 관계언 (16)
	String[] MorphemeDependency = { "/E", "/X" }; // 의존형태소 (어미, 접두사, 접미사, 어근)
													// (32,64)
	String Symbol = "/S"; // 기호 (128)

	class WordClass {
		/**
		 * WordClass 각 형태소들을 합하여 품사(문장 성분)으로 분류한다 현재 방법에는 그닥 필요 없음
		 */
		protected String str;
		protected int type;
		protected int component;

		public WordClass() {
			str = "";
			type = -1; // 체언(0), 용언(1),수식언(2), 독립언(3), 관계언 (4), 의존형태소(5), 기호(6)
			component = 0;
		}

	}// WordClass class

	public CategoryAnalyzer() {

		khelper = new KomoranHelper();
		// factors = new Factor[]; // 머신러닝에 넘길 factor 정보 기록
		this.khelper.setKomoranDir(dbLoc);
		this.initKomoran();

	}

	public void initKomoran() {

		khelper.setKomoranDir(dbLoc);

	}

	public void getDBFactor() { // factor들 db에서 읽어온다

		ArrayList<String> category_tmp = dbmanager.queryAllCategory();
		this.totlaFactors = category_tmp.size(); // db에서 category table의 row 숫자

		this.factors = new Factor[this.totlaFactors];

		for (int i = 0; i < this.totlaFactors; i++) {

			factors[i] = new Factor();
			// tmp_wordlist_by_categoryID는 categoryID가 동일한 wordlist를 받아
			// 온다ArrayList<Word>
			ArrayList<Word> tmp_wordlist_by_categoryID = dbmanager.queryWordList(i + 1);

			// tmp_word_morphogy_before는 형태소 분석 거친 결과 저장.
			// List<String> tmp_word_morphogy_before[] = new
			// List[tmp_wordlist_by_categoryID.size()];
			// tmp_word_morphogy_는 tmp_word_morphlogy를 보기 쉽게 변환
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

	public String convertStrFormat(String bf, boolean blank) {// 형태소 분석 결과를 보기
																// 편하게 변환
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
	 * public void initFactor(Factor[] fact) { // Morpholoy class에서 사용할 factor
	 * 초기화
	 * 
	 * int len = fact.length; System.out.println("len:" + len); for (int i = 0;
	 * i < len; i++) { fact[i] = new Factor(); fact[i].factor_cnt = 0; }
	 * 
	 * fact[0].factor_name = "자살 준비 언급"; fact[0].negative = true;
	 * 
	 * fact[1].factor_name = "수치심"; fact[1].negative = true;
	 * 
	 * }
	 */
	public String readData(String str) {//

		/*
		 * 어떻게 sns, sms 읽을까?? 외부txt파일로 만들어서 읽던가 아니면 실시간으로 읽을수도 있겠네
		 * 
		 * 이 부분은 앱 만드는 사람이 고려하고 일단 설계 단계나 이후 단계에서 보여주기 위해서는 임의로...
		 */
		// return str;
		return str;
	}

	public void compStr_wd(String cmp) {// 죽음 관련 단어 검사
		/**
		 * 문장단위로 자른것! 문장을 받아와서 word list 검사한다.
		 * 
		 */
		System.out.println("CMPwd:" + cmp);
		int tmp_cnt = 0;
		int blank_count;
		for (int i = 0; i < this.totlaFactors; i++) { // 전체 factor들에 대해 검사
			blank_count = 1;
			for (int j = 0; j < factors[i].WDsize; j++) {// i번 째 factor에 대해 검사
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

//					System.out.println("Wordlist" + this.factors[i].factorName + " 이 " + this.factors[i].factorCnt
//							+ "개 검출" + "(" + blank_count + ":" + tmp_cnt + ")");

					break;
				}
		
				tmp_cnt = 0;
			} // for-j
		} // for-i

	}

	public int compStr_ex(int factorIndex) {
		// TODO compStr_Ex
		/**
		 * 문장(EC)단위로 자른것! 문장을 받아와서 exception word list 검사한다.
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
			/* 한 factor에 들어있는 것 숫자만큼 비교한다. */

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

		// TODO checkException 부분
		String str = new String();
		System.out.println("ex문장 사이즈:" + this.testMsg.size());

		for (int i = 0; i < this.totlaFactors; i++) {// 각 factor 당 문장을 돌며 검사 하고
														// check 표시를 한다.
			this.factors[i].factorCnt += compStr_ex(i);

		}

	}

	public void checkWord() { // '자살 준비' 라는 factor에 대해 검사 과정 전체
		/**
		 * 문장 전체를 받아서 exception word list 검사를 위해 형태소 분석기가 자른 어절 단위로 문장을 자릅니다.
		 * 내/NP가/JK + 가/VV+다/EC (2 덩어리)
		 * 
		 * 어절단위로 자르기 때문에 부정어 처리 필요함
		 */
		String str = "";
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println("wd문장 사이즈:" + this.testMsg.size());
		for (int j = 0; j < this.testMsg.size(); j++) { // 각 factor의 word에 대해

			if (this.testMsg.get(j).checked == false) {
				System.out.print(this.testMsg.get(j).checked);
				compStr_wd(this.testMsg.get(j).testMsg);// 결과를 factor와 검사
			}
		}

	}

	// 아직 안쓰는 함수
	public int defineType(String tmp) { // 차라리 여기서
		int val = 0;
		/*
		 * 1.주어-체언 1 2.서술어- 동사(VV), 형옹사(VA), (체언+연결어미(EC)) ->ㅍ 3.보어-
		 * (체언+주격조사(JKS) 4.목적어 - 체언+ 목적격조사(JKO) 5.부사어 - (체언+ 부사격조사(JKB)), 부사??
		 * 6.관형어 - 관형사(MM), (체언+ 관형격조사(JKG))
		 */
		if (tmp.contains(UninflectedWord) == true) { // 체언이니까
			if (tmp.contains("/EC")) {
				System.out.println("체언+연결어미=서술어");
			} else if (tmp.contains("/JKS")) {
				System.out.println("체언+주격조사=보어");
			} else if (tmp.contains("/JKB")) {
				System.out.println("체언+부사격조사=부사어");
			} else if (tmp.contains("/JKG")) {
				System.out.println("체언+관형격조사=관형어");
			}
		}
		if (tmp.contains(Predicate) == true)
			val += 2;// 2 용언
		if (tmp.contains(Susigeon) == true)
			val += 4;// 4 수식언
		if (tmp.contains(Dongnibeon) == true)
			val += 8;// 8 독립언
		if (tmp.contains(RelateWord) == true)
			val += 16;// 16 관계언
		if (tmp.contains(MorphemeDependency[0]) == true)
			val += 32;// 32 의존사1
		if (tmp.contains(MorphemeDependency[1]) == true)
			val += 64;// 64 의존사2
		if (tmp.contains(Symbol) == true)
			val += 128;// 128 기호
		System.out.println("Test:" + val);

		this.checkType(val);

		return val;
	}

	// 아직 안쓰는 함수
	public int checkType(int type) {// 여기서
		/*
		 * String tmp =Integer.toBinaryString(type); //tmp=(BitSet)type;
		 * System.out.println("byte:"+tmp+" "+tmp.length()); int bit_level=0;
		 */
		/* 엔디안 반대로 */

		/*
		 * for(int i=tmp.length()-1;i>=0;i--){
		 * //System.out.println(tmp.charAt(i)); if(tmp.charAt(1)==1){//체언
		 * if(tmp.charAt(5)==1){ System.out.println("체언+연결어미=서술어"); return 2; }
		 * 
		 * } bit_level++; }//for
		 */

		// if( ) return 1; //주어
		return 1;
	}

	/**
	 * KomoranHeler를 통해 문장을 주고 형태소 분석을 시켜 결과를 받아온다.
	 */
	public List morpology(String tmp) {

		return this.khelper.getter().analyze(tmp);
	}

	public void analyzingList(List tm) {

		/**
		 * 형태소 분석결과를 다루기 쉽도록 형태 변환한다.
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
		 * 품사 쓰는 부분 아직 아님 for (int i = 0; i < tm.size(); i++) {
		 * this.sentense[i].str = this.convertStrFormat(tm.get(i).toString());
		 * System.out.println(this.sentense[i].str); this.sentense[i].type =
		 * this.defineType(this.sentense[i].str);
		 * 
		 * this.checkType(this.sentense[i].type); }
		 */

	}

	// 아직 안쓰는 함수
	/**
	 * 품사를 사용할 WordClass를 초기화 하는 함수 아직 안써도 됨 ㅋ
	 */
	/*
	 * public void initWord(int size) {
	 * 
	 * // 우선 inner class의 배열을 생성 할때 이렇게 하고
	 * 
	 * sentense[0] = this.new WordClass(); // // 초기화도 기존에 만든 Outer class의 객체를
	 * 기준으로 만든다!! this.sentense = new CategoryAnalyzer.WordClass[size]; for (int
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

				// 명사 부분 체크
				if ((str.contains("/NNG") == true) || (str.contains("/NNP") == true) || (str.contains("/NP") == true)) {
					returnStr += str;
					returnStr += " ";
				} // 용언 부분 체크
				else if ((str.contains("/VV") == true) || (str.contains("/VA") == true)
						|| (str.contains("/VX") == true)) {
					returnStr += str;
					returnStr += " ";
				} // 기타(어근) 체크
				else if ((str.contains("/XR") == true)) {
					returnStr += str;
					returnStr += " ";
				} // 부사? <못> 살겠다
				else if ((str.contains("/MAG") == true)) {
					returnStr += str;
					returnStr += " ";
				}
			} // while
		} // for
		System.out.println("word list에 넣을 것:" + returnStr);
		// 1 대신에 category ID 넿으세요
		dbmanager.insertWord(returnStr, index);
	}

	/**
	 * ec부분은 잘라서 넣는다. 1. 조사 부분은 남겨둔다. 즉 조사가 어떤게 들어가냐에 따라 의미가 달라지면 exception으로 포함
	 * ex)남에게 짐이 된다.->'에게'가 의미에 중요하기에 exception으로
	 * 
	 * 2. 첫 어절이 다른 카테고리의 핵심 어절과 많이 겹치는 경우 (자살이유의 행복~~=exceptional, 행복의
	 * 행복=wordlist)
	 * 
	 * @param tmp
	 *            exception wordlist에 넣을 String
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
		System.out.println("exception list에 넣을 것:" + returnStr);
		// 1 대신에 category ID 넿으세요
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

		System.out.println("검사 결과 종료");
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
						.println(this.factors[i].factorName + " 이(가) " + this.factors[i].factorCnt + "개 " + "포함되어 있음");

			}
		}

		return result;
	}

	public void listSetting() {

		this.addExceptionWithMP("죽을게요", 1);
		this.addExceptionWithMP("자살 하시려구", 1);
		this.addExceptionWithMP("죽는 이유", 1);
		this.addExceptionWithMP("여기까지 입니다", 1);

		this.addExceptionWithMP("자살입니다", 1);
		this.addExceptionWithMP("저는 떠납니다", 1);

		this.addExceptionWithMP("뼈아픈 상처", 2);
		this.addExceptionWithMP("억울하다", 2);
		this.addExceptionWithMP("행복하지 않네", 2);
		this.addExceptionWithMP("순간이 끔찍해", 2);
		this.addExceptionWithMP("희망이 없다", 2);
		this.addExceptionWithMP("버림받은 나", 2);
		this.addExceptionWithMP("할 일을 제대로 하지도 못하고", 2);
		this.addExceptionWithMP("짊어질 짐이 무거워", 2);
		this.addExceptionWithMP("받을 고통도 헤아릴 수 없다", 2);
		this.addExceptionWithMP("남에게 짐이 된다.", 2);

		this.addExceptionWithMP("다른 세상에 가고 싶어", 3);

		this.addExceptionWithMP("자신을 지키고 싶다", 4);
		this.addExceptionWithMP("돌아갈 수 있을까", 4);

		this.addExceptionWithMP("인생을 리셋하고", 6);
		this.addExceptionWithMP("약한 놈", 6);
		this.addExceptionWithMP("못난 나 때문에", 6);
		this.addExceptionWithMP("오래 살았어", 6);
		this.addExceptionWithMP("나란 녀석을 쓰레기", 6);
		this.addExceptionWithMP("나로 말미암아", 6);

		this.addExceptionWithMP("저를 이해하지 마세요", 7);
		this.addExceptionWithMP("저때문에 마음 아프고", 7);
		this.addExceptionWithMP("상처를 줘서 미안하다", 7);

		this.addExceptionWithMP("용서하지 않을거야", 9);
		this.addExceptionWithMP("용서할 수 없어", 9);

		this.addExceptionWithMP("행복하게 사세요", 10);
		this.addExceptionWithMP("미워 마세요", 10);
		this.addExceptionWithMP("용서하세요", 10);
		this.addExceptionWithMP("불쌍했어요", 10);
		this.addExceptionWithMP("마음이 아프다", 10);
		this.addExceptionWithMP("슬퍼하지 마라", 10);
		this.addExceptionWithMP("미안해하지 마라", 10);
		this.addExceptionWithMP("원망하지 마라", 10);

		this.addExceptionWithMP("하루를 정리", 11);

		this.addExceptionWithMP("나를 화장을 해서", 12);
		this.addExceptionWithMP("나를 묻을 때", 12);
		this.addExceptionWithMP("화장해주세요", 12);

		this.addExceptionWithMP("마지막 소원", 14);

		this.addExceptionWithMP("살아도 사는게 아니다", 15);

		this.addExceptionWithMP("미래가 없는 삶", 20);

		this.addExceptionWithMP("잘 있어 주었다", 24);

		this.addExceptionWithMP("내 편", 26);

		this.addExceptionWithMP("걱정 없이", 27);

		this.addExceptionWithMP("가슴이 벅찬", 28);

		this.addExceptionWithMP("배려가 없다", 34);

		this.addExceptionWithMP("너 때문에", 36);
		this.addExceptionWithMP("너만 아니면", 36);
		this.addExceptionWithMP("너로 인해", 36);
		this.addExceptionWithMP("너만 없었다면", 36);

		this.addWordWithMP("무덤 코앞", 1); // 무덤/NNG 코앞/NNG
		this.addWordWithMP("강물 빠지", 1); // 강물/NNG 빠지/VV
		this.addWordWithMP("깨어나 않을", 1);
		this.addWordWithMP("못 살겠다", 1);

		this.addWordWithMP("살아갈 가치를 못 느끼고", 2);
		this.addWordWithMP("살기가 싫어", 2);
		this.addWordWithMP("살 자신이 없어서", 2);
		this.addWordWithMP("사는게 지겨워", 2);
		this.addWordWithMP("상처를 받아서", 2);
		this.addWordWithMP("살아갈 이유가 없다", 2);
		this.addWordWithMP("하늘이 무너지는", 2);
		this.addWordWithMP("뼈저리게 느끼는", 2);
		this.addWordWithMP("버티기 힘들다", 2);
		this.addWordWithMP("살기가 고통스러워", 2);
		this.addWordWithMP("빚독촉", 2);
		this.addWordWithMP("빚에 쪼달려", 2);
		this.addWordWithMP("괴롭다", 2);
		this.addWordWithMP("분통하다", 2);
		this.addWordWithMP("아무것도 할 수 없다", 2);
		this.addWordWithMP("짊어져야 할 일이", 2);
		this.addWordWithMP("살기가 힘들다", 2);
		this.addWordWithMP("죽지도 않았을까", 2);

		this.addWordWithMP("죽고 싶다는 생각", 3);
		this.addWordWithMP("죽으면 끝날까", 3);
		this.addWordWithMP("죽으면 편해질까", 3);
		this.addWordWithMP("생을 마감", 3);
		this.addWordWithMP("충동적 자살", 3);
		this.addWordWithMP("죽음을 생각하기도", 3);
		this.addWordWithMP("인생 끝", 3);
		this.addWordWithMP("죽어야 돼", 3);
		this.addWordWithMP("죽음을 택했는지", 3);
		this.addWordWithMP("죽어야 돼", 3);
		this.addWordWithMP("죽을 때", 3);
		this.addWordWithMP("다시 태어날게", 3);
		this.addWordWithMP("죽이게 하고", 3);
		this.addWordWithMP("죽고 싶은", 3);
		this.addWordWithMP("죽거든", 3);
		this.addWordWithMP("환생한다면", 3);
		this.addWordWithMP("내가 사라져", 3);
		this.addWordWithMP("내가 죽었더라면", 3);
		this.addWordWithMP("죽음", 3);
		this.addWordWithMP("죽은 것 같아", 3);
		this.addWordWithMP("자살 생각", 3);
		this.addWordWithMP("동반자살", 3);
		this.addWordWithMP("죽을 각오", 3);
		this.addWordWithMP("삶을 포기", 3);
		this.addWordWithMP("죽기로 작정", 3);
		this.addWordWithMP("죽기 위한 방법", 3);
		this.addWordWithMP("자살", 3);
		this.addWordWithMP("최악의 선택", 3);
		this.addWordWithMP("사후 세계", 3);

		this.addWordWithMP("이대로 죽기엔", 4);
		this.addWordWithMP("밖에 못 산", 4);
		this.addWordWithMP("허무해", 4);
		this.addWordWithMP("떠나기 아쉽지만", 4);
		this.addWordWithMP("미련", 4);
		this.addWordWithMP("살고 싶다", 4);
		this.addWordWithMP("미련", 4);
		this.addWordWithMP("옛날로 돌아가고", 4);

		this.addWordWithMP("죽기로 결정한건", 5);
		this.addWordWithMP("죽음을 결심", 5);

		this.addWordWithMP("기억을 날려버리고", 6);
		this.addWordWithMP("구제불능", 6);
		this.addWordWithMP("내가 싫다", 6);
		this.addWordWithMP("내가 밉다", 6);
		this.addWordWithMP("공부를 못한다", 6);
		this.addWordWithMP("비참하다", 6);
		this.addWordWithMP("난 죽어야 될 인간", 6);
		this.addWordWithMP("자신이 미워요", 6);
		this.addWordWithMP("저 못난", 6);
		this.addWordWithMP("평생 사죄하면서", 6);
		this.addWordWithMP("후회된다", 6);
		this.addWordWithMP("나의 잘못", 6);
		this.addWordWithMP("내가 못난 꼴", 6);
		this.addWordWithMP("내 잘못", 6);

		this.addWordWithMP("죄송해요", 7);
		this.addWordWithMP("미안해", 7);
		this.addWordWithMP("사과드립니다", 7);
		this.addWordWithMP("사죄드립니다", 7);
		this.addWordWithMP("신세를 졌다", 7);

		this.addWordWithMP("싫었어요", 8);
		this.addWordWithMP("미워요", 8);
		this.addWordWithMP("원망", 8);
		this.addWordWithMP("아무도 믿지 못하겠어", 8);

		this.addWordWithMP("저주", 9);
		this.addWordWithMP("복수", 9);
		this.addWordWithMP("죽여버리고", 9);

		this.addWordWithMP("다음 세상에서", 11);
		this.addWordWithMP("천국", 11);
		this.addWordWithMP("체념", 11);
		this.addWordWithMP("마지막", 11);

		this.addWordWithMP("내 장례식", 12);
		this.addWordWithMP("한줌의 재", 12);
		this.addWordWithMP("화장을 부탁", 12);
		this.addWordWithMP("내 장례식", 12);
		this.addWordWithMP("삼일장", 12);
		this.addWordWithMP("3일장", 12);
		this.addWordWithMP("묘지", 12);
		this.addWordWithMP("묻어줘라", 12);
		this.addWordWithMP("신체를 기증", 12);
		this.addWordWithMP("시신기증", 12);
		this.addWordWithMP("비석을 남겨", 12);

		this.addWordWithMP("그 돈 가져", 13);

		this.addWordWithMP("나 잊지마", 14);
		this.addWordWithMP("잊어주길", 14);
		this.addWordWithMP("비석을 남겨", 14);
		this.addWordWithMP("내 몫까지 살아", 14);
		this.addWordWithMP("안락하지 말아주세요", 14);
		this.addWordWithMP("갚아주세요", 14);
		this.addWordWithMP("부탁드립니다", 14);
		this.addWordWithMP("잘해주세요", 14);
		this.addWordWithMP("처리해 주길", 14);
		this.addWordWithMP("해결해 주세요", 14);
		this.addWordWithMP("알리지 말아라", 14);

		this.addWordWithMP("힘들어서", 15);
		this.addWordWithMP("힘들 때", 15);
		this.addWordWithMP("견디기 힘이 들어", 15);
		this.addWordWithMP("괴로운 시간", 15);
		this.addWordWithMP("슬펐어요", 15);
		this.addWordWithMP("눈물만 흘러", 15);
		this.addWordWithMP("우울증", 15);

		this.addWordWithMP("술주정", 16);
		this.addWordWithMP("술에 찌들어", 16);
		this.addWordWithMP("알코올 중독", 16);

		this.addWordWithMP("치매", 17);

		this.addWordWithMP("장애인", 18);
		this.addWordWithMP("장애자", 18);
		this.addWordWithMP("장애", 18);

		this.addWordWithMP("외롭다", 19);
		this.addWordWithMP("혼자다", 19);
		this.addWordWithMP("섭섭", 19);
		this.addWordWithMP("혼자다", 19);
		this.addWordWithMP("의자하고 싶은", 19);
		this.addWordWithMP("나를 외면", 19);

		this.addWordWithMP("불안", 20);
		this.addWordWithMP("절박", 20);
		this.addWordWithMP("걱정", 20);
		this.addWordWithMP("근심", 20);

		this.addWordWithMP("불효자식", 21);
		this.addWordWithMP("같은 실수", 21);
		this.addWordWithMP("죄", 21);
		this.addWordWithMP("죄값", 21);

		this.addWordWithMP("사랑", 22);
		this.addWordWithMP("뽀뽀", 22);
		this.addWordWithMP("귀여움", 22);
		this.addWordWithMP("안아주다", 22);
		this.addWordWithMP("소중한", 22);
		this.addWordWithMP("응원", 22);
		this.addWordWithMP("위로", 22);
		this.addWordWithMP("예쁜", 22);
		this.addWordWithMP("이쁜", 22);
		this.addWordWithMP("아름다움", 22);

		this.addWordWithMP("기분 좋은", 23);
		this.addWordWithMP("웃기다", 23);
		this.addWordWithMP("삶의 의미", 23);
		this.addWordWithMP("즐겼다", 23);
		this.addWordWithMP("행복한", 23);
		this.addWordWithMP("소박함", 23);
		this.addWordWithMP("보람", 23);
		this.addWordWithMP("수다", 23);
		this.addWordWithMP("달콤한", 23);
		this.addWordWithMP("빛나는", 23);
		this.addWordWithMP("희망", 23);

		this.addWordWithMP("웃기다", 24);
		this.addWordWithMP("재밌다", 24);
		this.addWordWithMP("환희", 24);
		this.addWordWithMP("축복", 24);
		this.addWordWithMP("우승", 24);
		this.addWordWithMP("승리", 24);
		this.addWordWithMP("기쁘다", 24);
		this.addWordWithMP("좋다", 24);
		this.addWordWithMP("쾌활한", 24);
		this.addWordWithMP("재밌다", 24);
		this.addWordWithMP("힘을 내자", 24);
		this.addWordWithMP("극복하다", 24);
		this.addWordWithMP("이겼다", 24);
		this.addWordWithMP("희열", 24);

		this.addWordWithMP("하이파이브", 25);
		this.addWordWithMP("시원시원한", 25);
		this.addWordWithMP("통쾌한", 25);
		this.addWordWithMP("유쾌한", 25);
		this.addWordWithMP("상쾌한", 25);
		this.addWordWithMP("시원한", 25);
		this.addWordWithMP("후련한", 25);
		this.addWordWithMP("뻥 뚫리는", 25);

		this.addWordWithMP("자랑스러운", 26);
		this.addWordWithMP("소신있는", 26);
		this.addWordWithMP("긍지", 26);
		this.addWordWithMP("대견한", 26);
		this.addWordWithMP("해냈다", 26);
		this.addWordWithMP("성공한", 26);
		this.addWordWithMP("자신있는", 26);
		this.addWordWithMP("만족스러운", 26);
		this.addWordWithMP("뿌듯한", 26);
		this.addWordWithMP("의기양양한", 26);

		this.addWordWithMP("여유로움", 27);
		this.addWordWithMP("따뜻함", 27);
		this.addWordWithMP("평화로운", 27);
		this.addWordWithMP("포근한", 27);
		this.addWordWithMP("침착한", 27);
		this.addWordWithMP("안도스러운", 27);
		this.addWordWithMP("안심", 27);
		this.addWordWithMP("마음이 놓이는", 27);
		this.addWordWithMP("차분한", 27);

		this.addWordWithMP("고마움", 28);
		this.addWordWithMP("배려", 28);
		this.addWordWithMP("뭉클하다", 28);
		this.addWordWithMP("배려", 28);
		this.addWordWithMP("감동스러운", 28);
		this.addWordWithMP("가슴이 벅찬", 28);
		this.addWordWithMP("은혜", 28);
		this.addWordWithMP("마음을 움직이는", 28);

		this.addWordWithMP("깨어 있자", 29);
		this.addWordWithMP("짜릿한", 29);
		this.addWordWithMP("설렘", 29);
		this.addWordWithMP("흥분되는", 29);
		this.addWordWithMP("신나는", 29);
		this.addWordWithMP("심장이 뛴다", 29);
		this.addWordWithMP("생기발랄", 29);
		this.addWordWithMP("발랄한", 29);
		this.addWordWithMP("원기왕성한", 29);
		this.addWordWithMP("열정적인", 29);
		this.addWordWithMP("에너지 넘치는", 29);

		this.addWordWithMP("벌써", 30);
		this.addWordWithMP("놀라", 30);
		this.addWordWithMP("깜짝", 30);
		this.addWordWithMP("어이쿠", 30);
		this.addWordWithMP("심쿵", 30);

		this.addWordWithMP("끝나버린 느낌", 31);
		this.addWordWithMP("감성적", 31);
		this.addWordWithMP("그립다", 31);
		this.addWordWithMP("보고싶다", 31);
		this.addWordWithMP("애틋하다", 31);
		this.addWordWithMP("아련하다", 31);
		this.addWordWithMP("애잔하다", 31);

		this.addWordWithMP("질투", 32);
		this.addWordWithMP("꼴보기 싫은", 32);
		this.addWordWithMP("시기하는", 32);
		this.addWordWithMP("시샘하는", 32);
		this.addWordWithMP("샘나는", 32);

		this.addWordWithMP("낯설다", 33);
		this.addWordWithMP("공포", 33);
		this.addWordWithMP("절박", 33);
		this.addWordWithMP("기피", 33);
		this.addWordWithMP("두려움", 33);
		this.addWordWithMP("떨린다", 33);
		this.addWordWithMP("무섭다", 33);
		this.addWordWithMP("좌절", 33);
		this.addWordWithMP("절망", 33);
		this.addWordWithMP("소름", 33);

		this.addWordWithMP("짜증", 34);
		this.addWordWithMP("화가 난다", 34);
		this.addWordWithMP("분노", 34);
		this.addWordWithMP("부수고 싶다", 34);
		this.addWordWithMP("억울", 34);
		this.addWordWithMP("절규", 34);
		this.addWordWithMP("배신", 34);

		this.addWordWithMP("징그럽다", 35);
		this.addWordWithMP("더럽다", 35);
		this.addWordWithMP("끔찍", 35);
		this.addWordWithMP("토 나온다", 35);
		this.addWordWithMP("멸시", 35);
		this.addWordWithMP("역겨워", 35);

		this.addWordWithMP("징그럽다", 36);
		System.out.println("list setting complete");
	}

	public static void main(String[] args) {

		CategoryAnalyzer m = new CategoryAnalyzer();
		// m.listSetting();

		// String testStr = "너만 아니면";
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

		// TODO 검사 메세지는 여기서
		m.categoryAnalyzer("너무 뼈아픈 상처 미련이 남는다 허무해 죽을 때는 언제인가");

	}// main

}// class

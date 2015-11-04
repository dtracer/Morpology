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
			this.WDsize=0;

		}

		public void setFactor(String str, int cnt,int wdsize,String[] wdlist_tmp, int exsize, String[] exlist_tmp) {
			/**
			 * 하나의 요인에 값을 채운다, str,요인 이름 cnt,빈도수 list, 요인에 적용되는 문구(context)들(이건
			 * CategoryAnalyzer에서 받아온다)
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
			ArrayList<Word> tmp_wordlist_by_categoryID = dbmanager.queryWordList(i+1);
			
			// tmp_word_morphogy_before는 형태소 분석 거친 결과 저장.
			//List<String> tmp_word_morphogy_before[] = new List[tmp_wordlist_by_categoryID.size()];
			// tmp_word_morphogy_는 tmp_word_morphlogy를 보기 쉽게 변환
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
		System.out.println("CMPwd:" + cmp);
		int tmp_cnt = 0;
		int blank_count;
		for (int i = 0; i <this.totlaFactors; i++) { // 전체 factor들에 대해 검사
			blank_count = 1;
			for (int j = 0; j < factors[i].WDsize; j++) {// i번 째 factor에 대해 검사
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

					//System.out.println("Wordlist 결과"+this.factors[i].factorName + " 이 " + this.factors[i].factorCnt + "개 검출"+ "("+blank_count +":"+ tmp_cnt+")");
					
							
					break;
				}
				//System.out.println("" + blank_count + "/" + tmp_cnt);
				tmp_cnt = 0;
			} // for-j
		} // for-i

	}
	public boolean compStr_ex(String cmp) {// 죽음 관련 단어 검사
		System.out.println("CMPex:" + cmp);
		int tmp_cnt = 0;
		boolean set=false;
		int blank_count;
		for (int i = 0; i <this.totlaFactors; i++) { // 전체 factor들에 대해 검사
			blank_count = 1;
			for (int j = 0; j < factors[i].EXsize; j++) {// i번 째 factor에 대해 검사
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

					System.out.println("exceptionWord 결과:"+this.factors[i].factorName + " 이 " + this.factors[i].factorCnt + "개 검출"+ "("+blank_count +":"+ tmp_cnt+")");
					
							
					set=true;
				}
				//System.out.println("" + blank_count + "/" + tmp_cnt);
				tmp_cnt = 0;
			} // for-j
		} // for-i
		return set;
	}

	public boolean checkExceptional(List<TextMsg> tm) { // '자살 준비' 라는 factor에 대해 검사 과정 전체
		String str = "";
		System.out.println("ex문장 사이즈:" + tm.size());
		boolean result=false;
		
		for (int j = 0; j < tm.size(); j++) { // 각 factor의 word에 대해
			
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
	public void checkWord(List<TextMsg> tm) { // '자살 준비' 라는 factor에 대해 검사 과정 전체
		String str = "";
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println("wd문장 사이즈:" + tm.size());
		for (int j = 0; j < tm.size(); j++) { // 각 factor의 word에 대해

			 compStr_wd(tm.get(j).testMsg);// 결과를 factor와 검사

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
			tmp=new TextMsg();
			String str=this.convertStrFormat(tm.get(i).toString(),true);
			
			tmp.setTextMsg(str,str.contains("/EC"));
			//System.out.print("**"+str+str.contains("/EC"));
			testMsg.add(tmp);
			System.out.println("*"+testMsg.get(i).testMsg+testMsg.get(i).EC_parameter);
		}
		/*
		  품사 쓰는 부분 아직 아님 for (int i = 0; i < tm.size(); i++) {
		  this.sentense[i].str = this.convertStrFormat(tm.get(i).toString());
		  System.out.println(this.sentense[i].str); this.sentense[i].type =
		  this.defineType(this.sentense[i].str);
		  
		  this.checkType(this.sentense[i].type); 
		  }
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


	public void addWordWithMP(String tmp) {

		List tmp_list = khelper.getter().analyze(tmp);
		String returnStr = "";
		for (int i = 0; i < tmp_list.size(); i++) {

			StringTokenizer token = new StringTokenizer(convertStrFormat(tmp_list.get(i).toString(), true), "+");

			while (token.hasMoreTokens()) {
				String str = token.nextToken();

				// 명사 부분 체크
				if ((str.contains("/NNG") == true) || (str.contains("/NNP") == true) || (str.contains("/NP") == true)) {
					returnStr += str;
				} // 용언 부분 체크
				else if ((str.contains("/VV") == true) || (str.contains("/VA") == true)) {
					returnStr += str;
				} // 기타(어근) 체크
				else if ((str.contains("/XR") == true)) {
					returnStr += str;
				}
			} // while
		} // for
		System.out.println("word list에 넣을 것:" + returnStr);
		// 1 대신에 category ID 넿으세요
		dbmanager.insertWord(returnStr, 1);
	}
	public void check(List<TextMsg> tm ){
		
		if(this.checkExceptional(tm)==false)
			this.checkWord(tm);
		System.out.println("검사 결과 종료");
	}
	public void initTestMsg(){
		this.testMsg=new ArrayList<>();
	}
	public ArrayList<Integer> categoryAnalyzer(String tmp){
		
		this.getDBFactor();
		List<TextMsg> tm = this.morpology(tmp);
		this.analyzingList(tm);

		this.check(this.testMsg);
		ArrayList<Integer> result=new ArrayList<>();

		for(int i=0;i<this.totlaFactors;i++){
			result.add(this.factors[i].factorCnt);
		
				if(this.factors[i].factorCnt>0){
					System.out.println(this.factors[i].factorName+" 이(가) "+this.factors[i].factorCnt+"개 "+"포함되어 있음");
					
				}
		}
		
		return result;
	}
	public static void main(String[] args) {

		CategoryAnalyzer m = new CategoryAnalyzer();

		// m.addWordWithMP("미워요");
		

		/*
		for(int i=0;i<m.totlaFactors;i++){
			System.out.println(m.factors[i].EXsize);
			System.out.println(m.factors[i].WDsize);
			for(int j=0;j<m.factors[i].exceptionlist.length;j++)
				System.out.println(m.factors[i].exceptionlist[j]);
		}
		*/
		
		ArrayList<Integer> result=	m.categoryAnalyzer("사랑해 내가 세상에 없을것 같아 죽고싶어");
		
		for(int i=0;i<m.totlaFactors;i++)
			System.out.println(result.get(i));
		/* 메인 시퀀스
		 m.getDBFactor();// factor들을 txt에서 다 읽어옴
		List<TextMsg> tm = m.morpology(m.readData("내가 세상에 없을것 같아 사랑해 죽고싶어"));
		m.analyzingList(tm);

		m.check(m.testMsg);
		
		*/
/*		
		m.initTestMsg();
		m.analyzingList(m.morpology(m.readData("죽고싶어")));
		
		m.check(m.testMsg);
		
		m.initTestMsg();
		m.analyzingList(m.morpology(m.readData("사랑해")));
		
		m.check(m.testMsg);
		
		*/
		
		/*
		 * for (int i = 0; i < m.totlaFactors; i++) if (m.factors[i].factorCnt >
		 * 0) { System.out.println(m.factors[i].factorName + " 이 " +
		 * m.factors[i].factorCnt + "개 검출"); }
		 */
	}
}// class

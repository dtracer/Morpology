package morphology;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import java.util.List;
import java.util.StringTokenizer;


public class CategoryAnalyzer {

	List  dic_factor =new ArrayList<Factor>(); // 자살준비 factor에 어휘 들어갈 목록
	List dic_negative =new ArrayList<Factor>();
	List dic_ambiguity =new ArrayList<Factor>();
	static int dic_size; // 어휘 수량

	static String db_loc = "D://IDE workspace//workspace//morphology//lib//models-light"; // 형태소 분석 파일 경로
	
	static String dic_file_loc = "D://IDE workspace//workspace//morphology//lib/dic.txt"; // factor가 저장된
																// txt파일 경로
	// String text_file_loc="D:/morpholory_dir/sms.txt"; //sms 내용 담은 파일 경로
	
	KomoranHelper khelper;
	Factor factors[]; // 배열로 factor관리
	StringTokenizer token;
	CategoryAnalyzer.WordClass[] sentense;
	
	static int total_factor_num = 5; // 총 factor들 수량

	 class Factor { // 지정할 factors 이름, 수량 적을 클래스
		
		String factor_name;
		int factor_cnt;
		boolean negative; // 긍정, 부정 factor 판별

		public Factor(){
			this.factor_name="";
			this.factor_cnt=0;
			this.negative=true;
		}
		public void setFactor(String str,int cnt,boolean neg){
			this.factor_name=str;
			this.factor_cnt=cnt;
			this.negative=neg;
		}
	}
	
	String UninflectedWord="/N";//체언 NN, NP, NR (1)
	String Predicate="/V"; //용언 VV, VA, VX, VC (2)
	String Susigeon="/M";//수식언 (4)
	String Dongnibeon="/IC"; //독립언 (8)
	String RelateWord="/J"; //관계언 (16)
	String[] MorphemeDependency={"/E","/X"}; //의존형태소 (어미, 접두사, 접미사, 어근) (32,64)
 	String Symbol="/S"; //기호 (128)

 	
 	class WordClass{ //문장 품사 클래스
		String str;
		int type;
		int component;
		public WordClass(){
			str="";
			type=-1; //체언(0), 용언(1),수식언(2), 독립언(3), 관계언 (4), 의존형태소(5), 기호(6)
			component=0;
		}
		public void setWord(String str_tmp,int type_tmp,int component_type){
		
			this.str=str_tmp;
			this.type=type_tmp;
			this.component=component_type;
		}
		
 	}
	public CategoryAnalyzer() {

		khelper =new KomoranHelper();
		factors = new Factor[total_factor_num]; // 머신러닝에 넘길 factor 정보 기록
		this.khelper.setKomoranDir(db_loc);
		this.initKomoran();
		
	}
	public void initKomoran(){
		
		khelper.setKomoranDir(db_loc);
		
	}
	public void getDBFactor() { //factor들 db에서 읽어온다
		BufferedReader rd;

		String tmp = "1";
		try {
			rd = new BufferedReader(new FileReader(dic_file_loc));
			while (tmp != null) {
				tmp = rd.readLine();

				// System.out.println(tmp);

				dic_factor.add(tmp);
			}
			dic_factor.remove(dic_factor.size() - 1);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dic_size = dic_factor.size();
		
	}
	public void getNegativeDic(){
		
	}
	public void getAmbiguityDic(){
		
	}
	public String convert_StrFormat(String bf) {//형태소 분석 결과를 보기 편하게 변환

		StringTokenizer token = new StringTokenizer(bf, "[=, ");

		String str = "";

		while (token.hasMoreTokens())
			str += token.nextToken();

		str = str.replace("first", "");
		str = str.replace("Pair", "");

		str = str.replace("second", "/");
		str = str.replace("]", "+");
		System.out.println("conver_StrFormat:" + str);
		return str;

	}
/*
	public void initFactor(Factor[] fact) { // Morpholoy class에서 사용할 factor 초기화

		int len = fact.length;
		System.out.println("len:" + len);
		for (int i = 0; i < len; i++) {
			fact[i] = new Factor();
			fact[i].factor_cnt = 0;
		}

		fact[0].factor_name = "자살 준비 언급";
		fact[0].negative = true;

		fact[1].factor_name = "수치심";
		fact[1].negative = true;

	}
*/
	public String read_data() {// 
		
		/* 어떻게 sns, sms 읽을까?? 외부txt파일로 만들어서 읽던가 아니면 실시간으로 읽을수도 있겠네
		 * 
		 * 이 부분은 앱 만드는 사람이 고려하고 일단 설계 단계나 이후 단계에서 보여주기 위해서는 임의로...
		 */
		
		return "행복하다";
	}

	public int comp_str(String cmp) {// 죽음 관련 단어 검사
		int tmp_cnt = 0;
		for (int i = 0; i < dic_size; i++) {

			if (cmp.contains(dic_factor.get(i).toString()) == true) {
				System.out.println(dic_factor.get(i).toString());
				tmp_cnt++;
			}//if
		}//for

		return tmp_cnt;
	}
	
	public int check_factor(List tm){ // '자살 준비' 라는 factor에 대해 검사 과정 전체
		String str="";
		int cnt=0;
		for (int i = 0; i < tm.size(); i++) {

			str = this.convert_StrFormat(tm.get(i).toString());// 형태소 분석 결과를 판단하기
															// 쉽게 변환
			cnt += this.comp_str(str);// 결과를 factor와 검사

		}
		return cnt;
	}
	public int define_type(String tmp){ //차라리 여기서
		int val=0;
		/* 1.주어-체언  1
		 * 2.서술어- 동사(VV), 형옹사(VA), (체언+연결어미(EC))  ->ㅍ
		 * 3.보어- (체언+주격조사(JKS)
		 * 4.목적어 - 체언+ 목적격조사(JKO)
		 * 5.부사어 - (체언+ 부사격조사(JKB)), 부사??
		 * 6.관형어 - 관형사(MM), (체언+ 관형격조사(JKG)) 
		 */
		if(tmp.contains(UninflectedWord)==true){ //체언이니까
			if(tmp.contains("/EC")){
				System.out.println("체언+연결어미=서술어");
			}
			else if (tmp.contains("/JKS")){
				System.out.println("체언+주격조사=보어");
			}
			else if (tmp.contains("/JKB")){
				System.out.println("체언+부사격조사=부사어");
			}
			else if (tmp.contains("/JKG")){
				System.out.println("체언+관형격조사=관형어");
			}
		}
		if(tmp.contains(Predicate)==true)val+=2;//2 용언
		if(tmp.contains(Susigeon)==true) val+=4;//4 수식언
		if(tmp.contains(Dongnibeon)==true) val+=8;//8 독립언
		if(tmp.contains(RelateWord)==true) val+=16;//16 관계언
		if(tmp.contains(MorphemeDependency[0])==true) val+=32;//32 의존사1
		if(tmp.contains(MorphemeDependency[1])==true)val+=64;//64 의존사2
		if(tmp.contains(Symbol)==true) val+=128;//128 기호
		System.out.println("Test:"+val);
		
		this.check_type(val);
		
		return val;
	} 
	public int check_type(int type){//여기서
		/*
		String tmp =Integer.toBinaryString(type);
		//tmp=(BitSet)type;
		System.out.println("byte:"+tmp+" "+tmp.length());
		int bit_level=0;
		*/
		/* 엔디안 반대로 */
		
		/*
		for(int i=tmp.length()-1;i>=0;i--){
			//System.out.println(tmp.charAt(i));
			if(tmp.charAt(1)==1){//체언   
				if(tmp.charAt(5)==1){
					System.out.println("체언+연결어미=서술어");
					return 2;
				}
				
			}
			bit_level++;
		}//for
		*/
		
		
	//if( )	return 1; //주어
		return 1;
	}
	public List morpology(String tmp){
		
		return this.khelper.komoran.analyze(tmp);
	}
	public void analyze(List tm){
		for(int i=0;i<tm.size();i++){
			this.sentense[i].str = this.convert_StrFormat(tm.get(i).toString());
			System.out.println(this.sentense[i].str);
			this.sentense[i].type=this.define_type(this.sentense[i].str);
			
			this.check_type(this.sentense[i].type);
			
			}
	}
	public void initWord(int size){
	//우선 inner class의 배열을 생성 할때 이렇게 하고	
			
		//	sentense[0] = m.new WordClass(); //
		//초기화도 기존에 만든 Outer class의 객체를 기준으로 만든다!!
		this.sentense=new CategoryAnalyzer.WordClass[size];
		for(int i=0;i<size;i++){
			this.sentense[i]=this.new WordClass ();
		}
	}
	public void check_Negative(){
		
	}
	public void check_Ambiguity(){
		
	}
	public static void main(String[] args) {

		CategoryAnalyzer m = new CategoryAnalyzer();

		// int nbest = 1;
		// List<List<List<Pair<String,String>>>> analyzeNbestResult =
		// komoran.analyze(in,nbest);

		m.getDBFactor();// factor들을 txt에서 다 읽어옴
		
		List tm = m.morpology(m.read_data());
		
		// 현재 문장에 대해 형태소 분석 결과가 list에 

		// System.out.println(tm.get(1).toString().contains("MM")); //태그 확인

		// System.out.println(tm.size());//3개의 띄어쓰기로 나뉨

		m.initWord(tm.size());
		
		m.analyze(tm);
		
		
		//System.out.println(dic_factor.get(dic_factor.size() - 1));
		// 분석한 문장 몇개로 나뉘었나 출력 
		
	
		//System.out.println(m.convert_StrFormat(tm.get(1).toString()));
	/*	
		m.factor[0].factor_cnt=m.check_factor(tm);
		//0번 factor에 대해 검사 결과

		System.out.println("fact1_Cnt:" + m.factor[0].factor_cnt);
*/
	}
}// class

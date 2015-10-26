package morphology;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import java.util.List;
import java.util.StringTokenizer;


public class CategoryAnalyzer {

	List  dic_factor =new ArrayList<Factor>(); // �ڻ��غ� factor�� ���� �� ���
	List dic_negative =new ArrayList<Factor>();
	List dic_ambiguity =new ArrayList<Factor>();
	static int dic_size; // ���� ����

	static String db_loc = "D://IDE workspace//workspace//morphology//lib//models-light"; // ���¼� �м� ���� ���
	
	static String dic_file_loc = "D://IDE workspace//workspace//morphology//lib/dic.txt"; // factor�� �����
																// txt���� ���
	// String text_file_loc="D:/morpholory_dir/sms.txt"; //sms ���� ���� ���� ���
	
	KomoranHelper khelper;
	Factor factors[]; // �迭�� factor����
	StringTokenizer token;
	CategoryAnalyzer.WordClass[] sentense;
	
	static int total_factor_num = 5; // �� factor�� ����

	 class Factor { // ������ factors �̸�, ���� ���� Ŭ����
		
		String factor_name;
		int factor_cnt;
		boolean negative; // ����, ���� factor �Ǻ�

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
	
	String UninflectedWord="/N";//ü�� NN, NP, NR (1)
	String Predicate="/V"; //��� VV, VA, VX, VC (2)
	String Susigeon="/M";//���ľ� (4)
	String Dongnibeon="/IC"; //������ (8)
	String RelateWord="/J"; //����� (16)
	String[] MorphemeDependency={"/E","/X"}; //�������¼� (���, ���λ�, ���̻�, ���) (32,64)
 	String Symbol="/S"; //��ȣ (128)

 	
 	class WordClass{ //���� ǰ�� Ŭ����
		String str;
		int type;
		int component;
		public WordClass(){
			str="";
			type=-1; //ü��(0), ���(1),���ľ�(2), ������(3), ����� (4), �������¼�(5), ��ȣ(6)
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
		factors = new Factor[total_factor_num]; // �ӽŷ��׿� �ѱ� factor ���� ���
		this.khelper.setKomoranDir(db_loc);
		this.initKomoran();
		
	}
	public void initKomoran(){
		
		khelper.setKomoranDir(db_loc);
		
	}
	public void getDBFactor() { //factor�� db���� �о�´�
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
	public String convert_StrFormat(String bf) {//���¼� �м� ����� ���� ���ϰ� ��ȯ

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
	public void initFactor(Factor[] fact) { // Morpholoy class���� ����� factor �ʱ�ȭ

		int len = fact.length;
		System.out.println("len:" + len);
		for (int i = 0; i < len; i++) {
			fact[i] = new Factor();
			fact[i].factor_cnt = 0;
		}

		fact[0].factor_name = "�ڻ� �غ� ���";
		fact[0].negative = true;

		fact[1].factor_name = "��ġ��";
		fact[1].negative = true;

	}
*/
	public String read_data() {// 
		
		/* ��� sns, sms ������?? �ܺ�txt���Ϸ� ���� �д��� �ƴϸ� �ǽð����� �������� �ְڳ�
		 * 
		 * �� �κ��� �� ����� ����� ����ϰ� �ϴ� ���� �ܰ質 ���� �ܰ迡�� �����ֱ� ���ؼ��� ���Ƿ�...
		 */
		
		return "�ູ�ϴ�";
	}

	public int comp_str(String cmp) {// ���� ���� �ܾ� �˻�
		int tmp_cnt = 0;
		for (int i = 0; i < dic_size; i++) {

			if (cmp.contains(dic_factor.get(i).toString()) == true) {
				System.out.println(dic_factor.get(i).toString());
				tmp_cnt++;
			}//if
		}//for

		return tmp_cnt;
	}
	
	public int check_factor(List tm){ // '�ڻ� �غ�' ��� factor�� ���� �˻� ���� ��ü
		String str="";
		int cnt=0;
		for (int i = 0; i < tm.size(); i++) {

			str = this.convert_StrFormat(tm.get(i).toString());// ���¼� �м� ����� �Ǵ��ϱ�
															// ���� ��ȯ
			cnt += this.comp_str(str);// ����� factor�� �˻�

		}
		return cnt;
	}
	public int define_type(String tmp){ //���� ���⼭
		int val=0;
		/* 1.�־�-ü��  1
		 * 2.������- ����(VV), ���˻�(VA), (ü��+������(EC))  ->��
		 * 3.����- (ü��+�ְ�����(JKS)
		 * 4.������ - ü��+ ����������(JKO)
		 * 5.�λ�� - (ü��+ �λ������(JKB)), �λ�??
		 * 6.������ - ������(MM), (ü��+ ����������(JKG)) 
		 */
		if(tmp.contains(UninflectedWord)==true){ //ü���̴ϱ�
			if(tmp.contains("/EC")){
				System.out.println("ü��+������=������");
			}
			else if (tmp.contains("/JKS")){
				System.out.println("ü��+�ְ�����=����");
			}
			else if (tmp.contains("/JKB")){
				System.out.println("ü��+�λ������=�λ��");
			}
			else if (tmp.contains("/JKG")){
				System.out.println("ü��+����������=������");
			}
		}
		if(tmp.contains(Predicate)==true)val+=2;//2 ���
		if(tmp.contains(Susigeon)==true) val+=4;//4 ���ľ�
		if(tmp.contains(Dongnibeon)==true) val+=8;//8 ������
		if(tmp.contains(RelateWord)==true) val+=16;//16 �����
		if(tmp.contains(MorphemeDependency[0])==true) val+=32;//32 ������1
		if(tmp.contains(MorphemeDependency[1])==true)val+=64;//64 ������2
		if(tmp.contains(Symbol)==true) val+=128;//128 ��ȣ
		System.out.println("Test:"+val);
		
		this.check_type(val);
		
		return val;
	} 
	public int check_type(int type){//���⼭
		/*
		String tmp =Integer.toBinaryString(type);
		//tmp=(BitSet)type;
		System.out.println("byte:"+tmp+" "+tmp.length());
		int bit_level=0;
		*/
		/* ����� �ݴ�� */
		
		/*
		for(int i=tmp.length()-1;i>=0;i--){
			//System.out.println(tmp.charAt(i));
			if(tmp.charAt(1)==1){//ü��   
				if(tmp.charAt(5)==1){
					System.out.println("ü��+������=������");
					return 2;
				}
				
			}
			bit_level++;
		}//for
		*/
		
		
	//if( )	return 1; //�־�
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
	//�켱 inner class�� �迭�� ���� �Ҷ� �̷��� �ϰ�	
			
		//	sentense[0] = m.new WordClass(); //
		//�ʱ�ȭ�� ������ ���� Outer class�� ��ü�� �������� �����!!
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

		m.getDBFactor();// factor���� txt���� �� �о��
		
		List tm = m.morpology(m.read_data());
		
		// ���� ���忡 ���� ���¼� �м� ����� list�� 

		// System.out.println(tm.get(1).toString().contains("MM")); //�±� Ȯ��

		// System.out.println(tm.size());//3���� ����� ����

		m.initWord(tm.size());
		
		m.analyze(tm);
		
		
		//System.out.println(dic_factor.get(dic_factor.size() - 1));
		// �м��� ���� ��� �������� ��� 
		
	
		//System.out.println(m.convert_StrFormat(tm.get(1).toString()));
	/*	
		m.factor[0].factor_cnt=m.check_factor(tm);
		//0�� factor�� ���� �˻� ���

		System.out.println("fact1_Cnt:" + m.factor[0].factor_cnt);
*/
	}
}// class

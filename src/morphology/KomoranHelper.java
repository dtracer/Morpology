package morphology;

import kr.co.shineware.util.common.model.Pair;

import java.util.List;

import kr.co.shineware.nlp.komoran.core.analyzer.Komoran;

public class KomoranHelper {
	
	private Komoran komoran;
	
	public Komoran getter(){
		
		return this.komoran;
	}
	
	public void setKomoranDir(String db_loc){
		
		komoran = new Komoran(db_loc); // 형태소 분석 위한 경로 지정
	}
	
} 

package morphology;

import kr.co.shineware.util.common.model.Pair;

import java.util.List;

import kr.co.shineware.nlp.komoran.core.analyzer.Komoran;

public class KomoranHelper {
	
	Komoran komoran;
	
	public void setKomoranDir(String db_loc){
		
		komoran = new Komoran(db_loc); // ���¼� �м� ���� ��� ����
	}
}

package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * 모든 데이터베이스 관련 동작을 담당하는 녀석.<br><br>
 * 
 * 로컬에서 테스트할 시 각자에 맞게 입력해야 하는 부분이 있음을 유의.
 * 
 * <p>최종 수정: 2015-11-03</p>
 * 
 * @author Moon
 *
 */
public class DbManager {
	////////////////////////////////////////////////////////////////////////////
	// 각자에 맞게 입력해야 하는 부분
	////////////////////////////////////////////////////////////////////////////
	/**
	 * 테스트하는 각자 로컬호스트의 데이터베이스 스키마 이름을 입력해야함. 
	 */
	private static final String databaseName = "lifelink";

	/**
	 * mysql 로컬호스트 user name 입력해야함.
	 */
	private static final String user = "root";

	/**
	 * mysql 로컬호스트 password 입력해야함.
	 */
	private static final String password = "moon";
	////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////

	@SuppressWarnings("unused")
	private static final String TABLE_CATEGORY_LIST = "category_list";

	@SuppressWarnings("unused")
	private static final String TABLE_WORD_LIST = "word_list";

	@SuppressWarnings("unused")
	private static final String TABLE_EXCEPTIONAL_WORD_LIST = "exceptional_word_list";

	@SuppressWarnings("unused")
	private static final String TABLE_REPORTED_LIST = "reported_list";

	@SuppressWarnings("unused")
	private static final String TABLE_TRAINING_DATA = "training_data";

	private static final String url = "jdbc:mysql://localhost/" + databaseName;

	private static volatile DbManager instance; 

	/**
	 * DbManager 클래스를 사용하고 싶으면 이 메소드를 통해서 객체를 생성할 수 있다.<br><br>
	 * 
	 * 이 클래스는 싱글턴 패턴이 적용되어 <b>{@code new}</b> 를 통한 객체 할당이 안되도록 막아져있다.
	 * 이 메소드를 부르면 객체가 있을경우 그 객체를 받아오며 객체가 없는 경우
	 * 객체를 생성한다.
	 * 
	 * @return DbManager instance.
	 */
	public static DbManager getInstance() {
		if(instance == null) {
			synchronized(DbManager.class) {
				if(instance == null)
					instance = new DbManager();
			}
		}
		return instance;
	}

	private DbManager() {
	}

	/**
	 * local host database로의 연결을 해줌.
	 * 
	 * @return Connection object.
	 */
	private Connection getConnection() {
		Connection con = null;
		try {
			con = DriverManager.getConnection(url, user, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return con;
	}

	/**
	 * 테이블의 tuple 개수를 리턴한다.
	 * 
	 * @param tableName 테이블 이름이 들어감<br>
	 * {@link #TABLE_CATEGORY_LIST}<br>
	 * {@link #TABLE_WORD_LIST}<br>
	 * {@link #TABLE_EXCEPTIONAL_WORD_LIST}<br>
	 * {@link #TABLE_REPORTED_LIST}<br>
	 * {@link #TABLE_TRAINING_DATA}<br>
	 * 중에 하나가 들어감
	 * 
	 * @return integer value of 테이블 tuple의 개수
	 */
	public int queryCountOfTable(String tableName) {
		Connection connection = getConnection();
		int result = -1;
		String query = "select count(*) from ?";

		try {
			PreparedStatement ps = connection.prepareStatement(query);
			ps.setString(1, tableName);
			ResultSet rs = ps.executeQuery();
			if(rs.next())
				result = rs.getInt(1);

			rs.close();
			ps.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 데이터베이스에 카테고리를 추가한다.
	 * 
	 * @param categoryName 추가할 카테고리 이름.
	 */
	public void insertCategory(String categoryName) {
		try {
			Connection connection = getConnection();
			String sql = "insert into category_list(name) values(?)";
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, categoryName);
			preparedStatement.execute();
			preparedStatement.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 카테고리를 수정한다.
	 * 
	 * @param id 수정할 카테고리의 아이디
	 * @param categoryName 새로운 이름
	 */
	public void modifyCategory(int id, String newCategoryName) {
		try {
			Connection connection = getConnection();
			String sql = "update category_list set category=? where id=" + id;
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, newCategoryName);
			preparedStatement.execute();
			preparedStatement.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 모든 카테고리 이름들을 리턴한다.
	 * 
	 * @return {@code ArrayList<String>} names of all categories
	 */
	public ArrayList<String> queryAllCategory() {
		ArrayList<String> resultList = new ArrayList<>();
		try {
			Connection connection = getConnection();
			Statement statement = null;
			ResultSet resultSet = null;

			statement = connection.createStatement();
			resultSet = statement.executeQuery("select * from category_list");

			while(resultSet.next()) {
				resultList.add(resultSet.getString(2));
			}
			statement.close();
			resultSet.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return resultList;
	}

	/**
	 * 카테고리 아이디를 받아온다.
	 * 
	 * @param category 아이디를 알고싶은 카테고리 이름
	 * @return integer value of category id
	 */
	public int queryCategoryId(String category) {
		Connection connection = getConnection();
		PreparedStatement ps = null;
		ResultSet resultSet = null;
		int result = -1;

		try {
			String sql = "select id from category_list where category=?";
			ps = connection.prepareStatement(sql);
			ps.setString(1, category);
			resultSet = ps.executeQuery();

			if(resultSet.next())
				result = resultSet.getInt(1);

			resultSet.close();
			ps.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 카테고리 삭제
	 * 
	 * @param categoryId 삭제할 카테고리의 아이디
	 */
	public void deleteCategory(int categoryId) {
		Connection connection = getConnection();
		String sql = "delete from category_list where id=?";
		try {
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, categoryId);
			ps.execute();

			ps.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 단어를 추가한다.
	 * 
	 * @param word 추가할 단어
	 * @param categoryId 이 단어가 속해있는 카테고리의 아이디
	 */
	public void insertWord(String word, int categoryId) {
		try {
			Connection connection = getConnection();
			String sql = "insert into word_list(word, category_id) values(?, ?)";

			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setString(1, word);
			ps.setInt(2, categoryId);

			ps.execute();
			ps.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 단어를 추가한다.
	 * 
	 * @param word 추가할 단어
	 * @param categoryId 이 단어가 속해있는 카테고리 이름
	 */
	public void insertWord(String word, String category) {
		int categoryId = queryCategoryId(category);
		insertWord(word, categoryId);
	}

	/**
	 * {@link #TABLE_WORD_LIST} 테이블에서 모든 단어들을 리턴한다.
	 * 
	 * @return {@code ArrayList<Word>} All of the words in the word list table.
	 */
	public ArrayList<Word> queryAllWord() {
		Connection connection = getConnection();
		ArrayList<Word> wordList = new ArrayList<>();
		try {
			String sql = "select * from word_list";
			PreparedStatement ps;
			ps = connection.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();

			while(rs.next())
				wordList.add(new Word(rs.getString(2), rs.getInt(3)));

			rs.close();
			ps.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return wordList;
	}

	/**
	 * 특정 카테고리에 속한 단어들 개수를 리턴
	 * 
	 * @param categoryId 단어가 몇개인지 알고싶은 카테고리의 아이디
	 * @return 단어의 개수
	 */
	public int queryCountOfWordList(int categoryId) {
		Connection connection = getConnection();
		int result = -1;
		String query = "select count(*) from word_list where category_id=?";

		try {
			PreparedStatement ps = connection.prepareStatement(query);
			ps.setInt(1, categoryId);
			ResultSet rs = ps.executeQuery();
			if(rs.next())
				result = rs.getInt(1);

			rs.close();
			ps.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 특정 카테고리에 속하는 단어 리스트를 리턴
	 * 
	 * @param categoryId 리턴할 단어들의 카테고리 아이디 
	 * @return {@code ArrayList<Word>} 단어 리스트
	 */
	public ArrayList<Word> queryWordList(int categoryId) {
		Connection connection = getConnection(); 
		ArrayList<Word> wordList = new ArrayList<>();
		try {
			String sql = "select * from word_list where category_id=?";
			PreparedStatement ps;
			ps = connection.prepareStatement(sql);
			ps.setInt(1, categoryId);
			ResultSet rs = ps.executeQuery();

			while(rs.next())
				wordList.add(new Word(rs.getString(2), rs.getInt(3)));

			rs.close();
			ps.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return wordList;
	}

	/**
	 * {@link #TABLE_WORD_LIST}의 단어를 수정한다.
	 * 
	 * @param wordId 수정할 단어의 아이디
	 * @param newWord 새로운 단어
	 * @param newCategoryId 새로운 카테고리 아이디
	 */
	public void modifyWord(int wordId, String newWord, int newCategoryId) {
		Connection connection = getConnection();
		String sql = "update word_list set word=?, category_id=? where id=?";
		try {
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setString(1, newWord);
			ps.setInt(2, newCategoryId);
			ps.setInt(3, wordId);
			ps.execute();

			ps.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * {@link #TABLE_WORD_LIST}에서 단어를 삭제한다.
	 * 
	 * @param wordId 삭제할 단어의 아이디
	 */
	public void deleteWord(int wordId) {
		Connection connection = getConnection();
		String sql = "delete from word_list where id=?";

		try {
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, wordId);
			ps.execute();

			ps.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 예외 단어를 추가한다.
	 * 
	 * @param word 추가할 예외 단어
	 * @param categoryId 이 예외 단어가 속해있는 카테고리의 아이디
	 */
	public void insertExceptionalWord(String word, int categoryId) {
		try {
			Connection connection = getConnection();
			String sql = "insert into exceptional_word_list(word, category_id) values(?, ?)";

			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setString(1, word);
			ps.setInt(2, categoryId);

			ps.execute();
			ps.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 예외 단어를 추가한다.
	 * 
	 * @param word 추가할 예외 단어
	 * @param categoryId 이 예외 단어가 속해있는 카테고리 이름
	 */
	public void insertExceptionalWord(String word, String category) {
		int categoryId = queryCategoryId(category);
		insertExceptionalWord(word, categoryId);
	}

	/**
	 * {@link TABLE_EXCEPTIONAL_WORD_LIST} 테이블에서 모든 예외 단어들을 리턴한다.
	 * 
	 * @return {@code ArrayList<Word>} All of the exceptional words in the exceptional word list table.
	 */
	public ArrayList<Word> queryAllExceptionalWord() {
		Connection connection = getConnection();
		ArrayList<Word> wordList = new ArrayList<>();
		try {
			String sql = "select * from exceptional_word_list";
			PreparedStatement ps;
			ps = connection.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();

			while(rs.next())
				wordList.add(new Word(rs.getString(2), rs.getInt(3)));

			rs.close();
			ps.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return wordList;
	}

	/**
	 * 특정 카테고리에 속한 예외 단어들 개수를 리턴
	 * 
	 * @param categoryId 예외 단어가 몇개인지 알고싶은 카테고리의 아이디
	 * @return 예외 단어의 개수
	 */
	public int queryCountOfExcepWordList(int categoryId) {
		Connection connection = getConnection();
		int result = -1;
		String query = "select count(*) from exceptional_word_list where category_id=?";

		try {
			PreparedStatement ps = connection.prepareStatement(query);
			ps.setInt(1, categoryId);
			ResultSet rs = ps.executeQuery();
			if(rs.next())
				result = rs.getInt(1);

			rs.close();
			ps.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 특정 카테고리에 속하는 예외 단어 리스트를 리턴
	 * 
	 * @param categoryId 리턴할 예외 단어들의 카테고리 아이디 
	 * @return {@code ArrayList<Word>} 예외 단어 리스트
	 */
	public ArrayList<Word> queryExceptionalWordList(int categoryId) {
		Connection connection = getConnection(); 
		ArrayList<Word> wordList = new ArrayList<>();
		try {
			String sql = "select * from exceptional_word_list where category_id=?";
			PreparedStatement ps;
			ps = connection.prepareStatement(sql);
			ps.setInt(1, categoryId);
			ResultSet rs = ps.executeQuery();

			while(rs.next())
				wordList.add(new Word(rs.getString(2), rs.getInt(3)));

			rs.close();
			ps.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return wordList;
	}

	/**
	 * {@link #TABLE_EXCEPTIONAL_WORD_LIST}의 예외 단어를 수정한다.
	 * 
	 * @param wordId 수정할 예외 단어의 아이디
	 * @param newWord 새로운 예외 단어
	 * @param newCategoryId 새로운 카테고리 아이디
	 */
	public void modifyExceptionalWord(int wordId, String newWord, int newCategoryId) {
		Connection connection = getConnection();
		String sql = "update exceptional_word_list set word=?, category_id=? where id=?";
		try {
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setString(1, newWord);
			ps.setInt(2, newCategoryId);
			ps.setInt(3, wordId);
			ps.execute();

			ps.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * {@link #TABLE_EXCEPTIONAL_WORD_LIST}에서 예외 단어를 삭제한다.
	 * 
	 * @param wordId 삭제할 예외 단어의 아이디
	 */
	public void deleteExceptionalWord(int wordId) {
		Connection connection = getConnection();
		String sql = "delete from exceptional_word_list where id=?";

		try {
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.setInt(1, wordId);
			ps.execute();

			ps.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**JUNE*******
	 * Report table을 받아온다.
	 * 
	 * @param id = {@code integer} report id
	 * @param contents = {@code String} 제목
	 * @param opinion = {@code String} 내용
	 */
	public ArrayList<Report> queryAllReport() {
		ArrayList<Report> resultList = new ArrayList<>();
		try {
			Connection connection = getConnection();
			Statement statement = null;
			ResultSet resultSet = null;

			statement = connection.createStatement();
			resultSet = statement.executeQuery("select * from reported_list");

			while(resultSet.next()) {
				resultList.add(new Report(resultSet.getInt("id"), resultSet.getString("Contents"), resultSet.getString("Opinion")));
			}
			statement.close();
			resultSet.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return resultList;
	}
}
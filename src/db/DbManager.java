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
 * <p>최종 수정: 2015-11-02</p>
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
	 * 데이터베이스에 카테고리를 추가한다.
	 * 
	 * @param categoryName 추가할 카테고리 이름.
	 */
	public void insertCategory(String categoryName) {
		try {
			Connection connection = getConnection();
			String query = "insert into category_list(name) values(?)";
			PreparedStatement preparedStatement = connection.prepareStatement(query);
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
			String query = "update category_list set category=? where id=" + id;
			PreparedStatement preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, newCategoryName);
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
	public void modifyWord(int id, String newCategoryName) {
		try {
			Connection connection = getConnection();
			String query = "update category_list set category=? where id=" + id;
			PreparedStatement preparedStatement = connection.prepareStatement(query);
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
			String query = "select id from category_list where category=?";
			ps = connection.prepareStatement(query);
			ps.setString(1, category);
			resultSet = ps.executeQuery();
			
			if(resultSet.next())
				result = resultSet.getInt(1);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
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
			String query = "insert into word_list(word, category_id) values(?, ?)";

			PreparedStatement ps = connection.prepareStatement(query);
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
}
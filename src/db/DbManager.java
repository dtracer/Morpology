package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * ��� �����ͺ��̽� ���� ������ ����ϴ� �༮.<br><br>
 * 
 * ���ÿ��� �׽�Ʈ�� �� ���ڿ� �°� �Է��ؾ� �ϴ� �κ��� ������ ����.
 * 
 * <p>���� ����: 2015-11-02</p>
 * 
 * @author Moon
 *
 */
public class DbManager {
	////////////////////////////////////////////////////////////////////////////
	// ���ڿ� �°� �Է��ؾ� �ϴ� �κ�
	////////////////////////////////////////////////////////////////////////////
	/**
	 * �׽�Ʈ�ϴ� ���� ����ȣ��Ʈ�� �����ͺ��̽� ��Ű�� �̸��� �Է��ؾ���. 
	 */
	private static final String databaseName = "lifelink";

	/**
	 * mysql ����ȣ��Ʈ user name �Է��ؾ���.
	 */
	private static final String user = "root";

	/**
	 * mysql ����ȣ��Ʈ password �Է��ؾ���.
	 */
	private static final String password = "moon";
	////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////

	private static final String url = "jdbc:mysql://localhost/" + databaseName;

	private static volatile DbManager instance; 

	/**
	 * DbManager Ŭ������ ����ϰ� ������ �� �޼ҵ带 ���ؼ� ��ü�� ������ �� �ִ�.<br><br>
	 * 
	 * �� Ŭ������ �̱��� ������ ����Ǿ� <b>{@code new}</b> �� ���� ��ü �Ҵ��� �ȵǵ��� �������ִ�.
	 * �� �޼ҵ带 �θ��� ��ü�� ������� �� ��ü�� �޾ƿ��� ��ü�� ���� ���
	 * ��ü�� �����Ѵ�.
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
	 * local host database���� ������ ����.
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
	 * �����ͺ��̽��� ī�װ��� �߰��Ѵ�.
	 * 
	 * @param categoryName �߰��� ī�װ� �̸�.
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
	 * ī�װ��� �����Ѵ�.
	 * 
	 * @param id ������ ī�װ��� ���̵�
	 * @param categoryName ���ο� �̸�
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
	 * ī�װ��� �����Ѵ�.
	 * 
	 * @param id ������ ī�װ��� ���̵�
	 * @param categoryName ���ο� �̸�
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
	 * ��� ī�װ� �̸����� �����Ѵ�.
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
	 * ī�װ� ���̵� �޾ƿ´�.
	 * 
	 * @param category ���̵� �˰���� ī�װ� �̸�
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
	 * �ܾ �߰��Ѵ�.
	 * 
	 * @param word �߰��� �ܾ�
	 * @param categoryId �� �ܾ �����ִ� ī�װ��� ���̵�
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
	 * �ܾ �߰��Ѵ�.
	 * 
	 * @param word �߰��� �ܾ�
	 * @param categoryId �� �ܾ �����ִ� ī�װ� �̸�
	 */
	public void insertWord(String word, String category) {
		int categoryId = queryCategoryId(category);
		insertWord(word, categoryId);
	}
}
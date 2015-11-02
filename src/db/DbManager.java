package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DbManager {
	
	/**
	 * �׽�Ʈ�ϴ� ���� ����ȣ��Ʈ�� �����ͺ��̽� �̸��� �Է��ؾ���. 
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

	private static final String url = "jdbc:mysql://localhost/" + databaseName;
	
	public DbManager() {
	}

	private Connection getConnection() {
		Connection con = null;
		try {
			con = DriverManager.getConnection(url, user, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return con;
	}

	public void insertCategory(String categoryName) {
		Connection connection = getConnection();
		try {
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

	public ArrayList<String> queryAllCategory() {
		Connection connection = getConnection();
		ArrayList<String> resultList = new ArrayList<>();
		try {
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
}

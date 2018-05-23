package Config;

import java.awt.Toolkit;
import java.io.FileReader;
import java.util.LinkedHashMap;

import javax.swing.JOptionPane;

public class config {
	public static final config config = new config();
	
	private String username = null;
	private String password = null;
	private String database = null;
	private int database_state = 0;
	private String dburl = "jdbc:mysql://127.0.0.1:3306/";
	private LinkedHashMap<String, Integer> websites = new LinkedHashMap<String, Integer>();
	private int framew = 0;
	private int frameh = 0;
	private int screenwidth = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
	private int screenwheight = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
	
	private config()
	{
		websites.put("website.DL_79xs", 8);
		websites.put("website.DL_biquge", 8);
		websites.put("website.DL_bookbao8", 3);
		websites.put("website.DL_shushu8", 8);
		
		OrderProperty pro = new OrderProperty();
		try {
			pro.load(new FileReader("./config.properity"));
			username = pro.getProperty("username");
			password = pro.getProperty("password");
			database = pro.getProperty("database");
			database_state = Integer.parseInt(pro.getProperty("database_state", "0"));
			
			framew = Integer.parseInt(pro.getProperty("width", "480"));
			frameh = Integer.parseInt(pro.getProperty("height", "600"));
			framew = framew <= 300 ? 300 : framew;  
			frameh = frameh <= 200 ? 200 : frameh;
			framew = framew >= screenwidth? screenwidth : framew;
			frameh = frameh >= screenwheight? screenwheight : frameh;
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "读取配置文件config.properity失败，请将该文件放置在当前java程序的同一级目录。",
											"错误说明", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getDatabase() {
		return database;
	}

	public int getDatabase_state() {
		return database_state;
	}

	public String getDburl() {
		return dburl;
	}

	public LinkedHashMap<String, Integer> getWebsites() {
		return websites;
	}

	public int getFramew() {
		return framew;
	}

	public int getFrameh() {
		return frameh;
	}

	public int getScreenwidth() {
		return screenwidth;
	}

	public int getScreenwheight() {
		return screenwheight;
	}

	public void setDatabase_state(int database_state) {
		this.database_state = database_state;
	}
}

package TEST_3;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.zip.GZIPInputStream;
import javax.net.ssl.HttpsURLConnection;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.util.*;


public class HFWeather {
	static String driver="com.mysql.cj.jdbc.Driver";
	static String URL="jdbc:mysql://localhost:3306/HEWeather_db?serverTimezone=UTC&useUnicode=true&characterEncoding=utf-8";
	static Connection conn=null;
	public static void main(String [] args) throws UnsupportedEncodingException, SQLException{
		conn=DriverManager.getConnection(URL,"root","66666666");
		//createTable();
		Scanner sc=new Scanner(System.in);
		String input_city="";
		while(!input_city.equals("exit")) {
			System.out.println("输入要添加的城市");
			input_city=sc.next();
			if(!input_city.equals("none")) {
				String text=getHtmlContent(input_city);
				JSONObject js =  JSON.parseObject(text);
				JSONObject details=js.getJSONArray("location").getJSONObject(0);
				String name=details.getString("name");
				String id=details.getString("id");
				String lat=details.getString("lat");
				String lon=details.getString("lon");
				add(URL,name,id,lat,lon);
				String text_2=getHtmlContent2(id);
				JSONObject js_2 =  JSON.parseObject(text_2);
				for(int j=0;j<3;j++) {
					JSONObject details_2=js_2.getJSONArray("daily").getJSONObject(j);
					String fxDate=details_2.getString("fxDate");
					String tempMax=details_2.getString("tempMax");
					String tempMin=details_2.getString("tempMin");
					String textDay=details_2.getString("textDay");
					updata(URL,fxDate,tempMax,tempMin,textDay,j+1,id);
				}
			}
			System.out.println("你要查询的城市");
			String q_city=sc.next();
			query(URL,q_city);	
		}
		sc.close();
	}
	
	public static String getHtmlContent(String city) throws UnsupportedEncodingException {
		String httpUrl = "https://geoapi.qweather.com/v2/city/lookup?key=48482c0cf87044ab81d81276da64ee92&location="+URLEncoder.encode(city,"UTF-8");
		String res=new String();
	    try {
	        URL url = new URL(httpUrl);
	        //打开网罗连接
	        HttpsURLConnection connection =  (HttpsURLConnection) url.openConnection();
	        //GET方式访问
	        connection.setRequestMethod("GET");
	        connection.connect();
	        //输入流
	        InputStream is = connection.getInputStream();
	        //GZIP处理
	        GZIPInputStream gzipInputStream =new GZIPInputStream(is);
			String line;
			BufferedReader br = new BufferedReader(new
			InputStreamReader(gzipInputStream, StandardCharsets.UTF_8));
			while ((line = br.readLine()) != null) {
			    res=res+line;
			}
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return res;
	}
	
	public static String getHtmlContent2(String id){
	    String httpUrl = "https://devapi.qweather.com/v7/weather/3d?key=48482c0cf87044ab81d81276da64ee92&location="+id;
		String res=new String();
	    try {
	        URL url = new URL(httpUrl);
	        //打开网罗连接
	        HttpsURLConnection connection =  (HttpsURLConnection) url.openConnection();
	        //GET方式访问
	        connection.setRequestMethod("GET");
	        connection.connect();
	        //输入流
	        InputStream is = connection.getInputStream();
	        //GZIP处理
	        GZIPInputStream gzipInputStream =new GZIPInputStream(is);
			String line;
			BufferedReader br = new BufferedReader(new
			InputStreamReader(gzipInputStream, StandardCharsets.UTF_8));
			while ((line = br.readLine()) != null) {
			    res+=line;
			}
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return res;
	}

	public static void createTable() throws SQLException {
		String sql = "CREATE TABLE IF NOT EXISTS `HFWeather`("
			     +"`id` INT UNSIGNED AUTO_INCREMENT ,"
			  	 +" `city_name` VARCHAR(100),"
			  	 +" `lat` VARCHAR(100) ,"
			  	 +" `lon` VARCHAR(100) ,"
			  	 +" `D1` VARCHAR(100) ,"
			  	 +" `D2` VARCHAR(100) ,"
			  	 +" `D3` VARCHAR(100) ,"
			  	 +"PRIMARY KEY ( `id` )"
			  	 +")ENGINE=InnoDB DEFAULT CHARSET=utf8;";
		conn.setAutoCommit(false);
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		preparedStatement.executeUpdate();
		conn.commit();
		conn.close();
	}

	public static void add(String URL,String name,String id,String lat,String lon) throws SQLException{
		String sql = "INSERT INTO HFWeather (id,city_name,lat,lon) VALUES('"+id+"','"+name+"',"+lat+","+lon+")";
		Connection conn =DriverManager.getConnection(URL,"root","66666666");
		conn.setAutoCommit(false);
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		preparedStatement.executeUpdate();
		conn.commit();
		conn.close();
	}

	public static void updata(String URL,String fxDate,String tempMax,String tempMin,String textDay,int index,String id) throws SQLException{
		String sql = "UPDATE HFWeather SET HFWeather.D"+index+" = 'fxDate:"+fxDate+"tempMax:"+tempMax+"tempMin:"+tempMin+"textDay:"+textDay+"' where id = "+id+"";
		Connection conn =DriverManager.getConnection(URL,"root","66666666");
		conn.setAutoCommit(false);
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		preparedStatement.executeUpdate();
		conn.commit();
		conn.close();
	}

	public static void query(String URL,String q_city) throws SQLException{
		String sql = "SELECT * FROM HFWeather";
		Connection conn=DriverManager.getConnection(URL,"root","66666666");
		conn.setAutoCommit(false);
		PreparedStatement preparedStatement = conn.prepareStatement(sql);
		ResultSet resultSet = preparedStatement.executeQuery();
		boolean check=false;
		while (resultSet.next()) {
			String city_name = resultSet.getString("city_name");
			if((q_city.compareTo(city_name))==0) {
				check=true;
				int id = resultSet.getInt("id");
				String lat = resultSet.getString("lat");
				String lon = resultSet.getString("lon");
				String D1 = resultSet.getString("D1");
				String D2 = resultSet.getString("D2");
				String D3 = resultSet.getString("D3");
				System.out.println("city_name: "+city_name +" id: "+id + " lat: "+lat+" lon: "+lon+"\n"+D1+" \n"+D2+" \n"+D3);
			}
		}
		conn.commit();
		if(!check) System.out.println("数据库中没有该城市信息qwq");
		conn.close();
	}
}

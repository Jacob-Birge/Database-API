package edu.uky.cs405g.sample.database;

// Used with permission from Dr. Bumgardner

import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Calendar;


public class DBEngine {
    private DataSource ds;
    public boolean isInit = false;
    public DBEngine(String host, String database, String login, 
		String password) {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            String dbConnectionString = null;
            if(database == null) {
                dbConnectionString ="jdbc:mysql://" + host + "?" 
					+"user=" + login  +"&password=" + password 
					+"&useUnicode=true&useJDBCCompliantTimezoneShift=true"
					+"&useLegacyDatetimeCode=false&serverTimezone=UTC"; 
			} else {
                dbConnectionString ="jdbc:mysql://" + host + "/" + database
				+ "?" + "user=" + login  +"&password=" + password 
				+ "&useUnicode=true&useJDBCCompliantTimezoneShift=true"
				+ "&useLegacyDatetimeCode=false&serverTimezone=UTC";
            }
            ds = setupDataSource(dbConnectionString);
            isInit = true;
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    } // DBEngine()

    public static DataSource setupDataSource(String connectURI) {
        //
        // First, we'll create a ConnectionFactory that the
        // pool will use to create Connections.
        // We'll use the DriverManagerConnectionFactory,
        // using the connect string passed in the command line
        // arguments.
        //
        ConnectionFactory connectionFactory = null;
            connectionFactory = 
				new DriverManagerConnectionFactory(connectURI, null);
        //
        // Next we'll create the PoolableConnectionFactory, which wraps
        // the "real" Connections created by the ConnectionFactory with
        // the classes that implement the pooling functionality.
        //
        PoolableConnectionFactory poolableConnectionFactory =
                new PoolableConnectionFactory(connectionFactory, null);

        //
        // Now we'll need a ObjectPool that serves as the
        // actual pool of connections.
        //
        // We'll use a GenericObjectPool instance, although
        // any ObjectPool implementation will suffice.
        //
        ObjectPool<PoolableConnection> connectionPool =
                new GenericObjectPool<>(poolableConnectionFactory);

        // Set the factory's pool property to the owning pool
        poolableConnectionFactory.setPool(connectionPool);

        //
        // Finally, we create the PoolingDriver itself,
        // passing in the object pool we created.
        //
        PoolingDataSource<PoolableConnection> dataSource =
                new PoolingDataSource<>(connectionPool);

        return dataSource;
    } // setupDataSource()

    public Map<String,String> getUsers() {
        Map<String,String> userIdMap = new HashMap<>();

        PreparedStatement stmt = null;
        try
        {
            Connection conn = ds.getConnection();
            String queryString = null;
            queryString = "SELECT * FROM Identity";
            stmt = conn.prepareStatement(queryString);
			// No parameters, so no binding needed.
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String userId = Integer.toString(rs.getInt("idnum"));
                String userName = rs.getString("handle");
                userIdMap.put(userId, userName);
            }
            rs.close();
            stmt.close();
            conn.close();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return userIdMap;
    } // getUsers()

    public Map<String,String> getBDATE(String idnum) {
        Map<String,String> userIdMap = new HashMap<>();

        PreparedStatement stmt = null;
        Integer id = Integer.parseInt(idnum);
        try
        {
            Connection conn = ds.getConnection();
            String queryString = null;
// Here is a statement, but we want a prepared statement.
//            queryString = "SELECT bdate FROM Identity WHERE idnum = "+id;
//            
            queryString = "SELECT bdate FROM Identity WHERE idnum = ?";
// ? is a parameter placeholder
            stmt = conn.prepareStatement(queryString);
			stmt.setInt(1,id);
// 1 here is to denote the first parameter.
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String bdate = rs.getString("bdate");
                userIdMap.put("bdate", bdate);
            }
            rs.close();
            stmt.close();
            conn.close();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return userIdMap;
    } // getBDATE()


    public Integer getStoryCount() {
        PreparedStatement stmt = null;
        try {
            Connection connect = ds.getConnection();
            try {
                String queryString = null;
                queryString = "SELECT COUNT(*) as num_stories FROM Story";
                ResultSet rs = stmt.executeQuery();
                Integer num_stories = rs.getInt("num_stories");
                return num_stories;
            } catch (SQLException throwables) {
                stmt.close();
                connect.close();
                return -10;
            } catch (Exception throwables) {
                throwables.printStackTrace();
            } finally {
                stmt.close();
                connect.close();
            }
        } catch (Exception throwables){
            throwables.printStackTrace();
        }
        return 1;
    } //getStoryCount

    public Integer createStory(Map<String,String> storyInfo) {
        PreparedStatement stmt = null;
        //create timestamp
        Calendar calendar = Calendar.getInstance();
        java.sql.Timestamp timestampObject = new java.sql.Timestamp(calendar.getTime().getTime());
        try {
            Connection connect = ds.getConnection();
            try {
                String queryString = null;
                queryString = "INSERT INTO Story (sidnum, idnum, chapter, url, expires, tstamp) VALUES (?, ?, ?, ?, ?, ?)";
                stmt = connect.prepareStatement(queryString);
                stmt.setString(1, storyInfo.get("sidnum"));
                stmt.setString(2, storyInfo.get("idnum")); //idnum
                stmt.setString(3, storyInfo.get("chapter")); //chapter
                stmt.setString(4, storyInfo.get("url")); //url
                stmt.setString(5, storyInfo.get("expires")); //expires
                stmt.setTimestamp(6, timestampObject); //tstamp

                int count = stmt.executeUpdate();
            } catch (SQLException throwables) {
                stmt.close();
                connect.close();
                return 0;
            } catch (Exception throwables) {
                throwables.printStackTrace();
            } finally {
                stmt.close();
                connect.close();
            }
        } catch (Exception throwables){
            throwables.printStackTrace();
        }
        return 1;
    } //createStory

    public Integer getReprintCount() {
        PreparedStatement stmt = null;
        Integer num_reprints = 1;
        try {
            Connection connect = ds.getConnection();
            try {
                String queryString = null;
                queryString = "SELECT COUNT(*) as num_reprints FROM Reprint";
                stmt = connect.prepareStatement(queryString);
                ResultSet rs = stmt.executeQuery();
                num_reprints = rs.getInt("num_reprints");
            } catch (SQLException throwables) {
                stmt.close();
                connect.close();
                return -10;
            } catch (Exception throwables) {
                throwables.printStackTrace();
            } finally {
                stmt.close();
                connect.close();
            }
        } catch (Exception throwables){
            throwables.printStackTrace();
        }
        return num_reprints;
    } //getReprintCount

    public Integer isBlocked(Map<String,String> reprintInfo) throws SQLException {
        Connection connect = ds.getConnection();
        try {
            Statement blocked_stmt = connect.createStatement();
            Statement select_stmt = connect.createStatement();
            String query = "SELECT idnum FROM Story WHERE sidnum = " + reprintInfo.get("sidnum") + ";";
            ResultSet select_result = select_stmt.executeQuery(query);
            String poster_id = select_result.getString("idnum");

            String blockQuery = "SELECT idnum, blocked from Block WHERE blocked =" + reprintInfo.get("idnum") + " AND idnum = " + poster_id + ";";
            ResultSet block_result = blocked_stmt.executeQuery(blockQuery);
            if (!block_result.isBeforeFirst()) {
                return 0;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return -1;
        } catch (Exception throwables) {
            throwables.printStackTrace();
            return -1;
        } finally {
            connect.close();
        }
        return 1;
    }

    public Integer reprint(Map<String,String> reprintInfo) {
        PreparedStatement stmt = null;
        String returnString = null;
        //create timestamp
        Calendar calendar = Calendar.getInstance();
        java.sql.Timestamp timestampObject = new java.sql.Timestamp(calendar.getTime().getTime());
        try {
            Connection connect = ds.getConnection();
            try {
                Integer like_boolean = 0;
                if (reprintInfo.get("likeit") == "true"){
                    like_boolean = 1;
                }
                String queryString = null;
                queryString = "INSERT INTO Reprint (sidnum, idnum, likeit, tstamp, rpnum) VALUES (?, ?, ?, ?, ?)";
                stmt = connect.prepareCall("begin" + " booleanFunc (par_bool => (CASE ? WHEN 1 THEN TRUE ELSE FALSE END)); " + "end;");
                stmt = connect.prepareStatement(queryString);
                stmt.setString(1, reprintInfo.get("sidnum"));
                stmt.setString(2, reprintInfo.get("idnum")); //idnum
                stmt.setInt(3, like_boolean); //likeit
                stmt.setTimestamp(4, timestampObject); //tstamp
                stmt.setString(5, reprintInfo.get("rpnum")); //rpnum

                int count = stmt.executeUpdate();
            } catch (SQLException throwables) {

                stmt.close();
                connect.close();
                return throwables.getErrorCode();
            } catch (Exception throwables) {
                throwables.printStackTrace();
                return 0;
            } finally {
                stmt.close();
                connect.close();
            }
        } catch (Exception throwables){
            throwables.printStackTrace();
            return 0;
        }
        return 1;
    } //reprint

    public Map<String,String> validateUser(String handleVal, String passVal) {
        Map<String,String> userIdMap = new HashMap<>();
        PreparedStatement stmt = null;
        try
        {
            Connection conn = ds.getConnection();
            String queryString = null;

            queryString = "SELECT idnum FROM Identity WHERE handle = ? and pass = ?";
            stmt = conn.prepareStatement(queryString);
            stmt.setString(1,handleVal);
            stmt.setString(2,passVal);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String idnum = rs.getString("idnum");
                userIdMap.put("idnum", idnum);
            }
            rs.close();
            stmt.close();
            conn.close();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return userIdMap;
    } // validateUser()

} // class DBEngine

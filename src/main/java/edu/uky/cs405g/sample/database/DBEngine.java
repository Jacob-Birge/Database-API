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

    public Map<String,String> validateUser(Map<String, String> userInfo) {
        Map<String,String> userIdMap = new HashMap<>();
        PreparedStatement stmt = null;
        try
        {
            Connection conn = ds.getConnection();
            String queryString = null;

            queryString = "SELECT idnum FROM Identity WHERE handle = ? and pass = ?";
            stmt = conn.prepareStatement(queryString);
            stmt.setString(1,userInfo.get("handle"));
            stmt.setString(2,userInfo.get("password"));

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

    public int createUser(String handle, String pass, String fullname, String location, String email, String bdate) {
        PreparedStatement stmt = null;
        try {
            Connection conn = ds.getConnection();
            String queryString = null;
            try {
                queryString = "INSERT INTO Identity (handle, pass, fullname, location, email, bdate) VALUES (?, ?, ?, ?, ?, ?)";
                stmt = conn.prepareStatement(queryString);
                stmt.setString(1, handle);
                stmt.setString(2, pass);
                stmt.setString(3, fullname);
                stmt.setString(4, location);
                stmt.setString(5, email);
                stmt.setString(6, bdate);

                int count = stmt.executeUpdate();
            }
            catch(SQLException ex){
                stmt.close();
                conn.close();
                return 0;
            }
            catch(Exception ex){
                ex.printStackTrace();
            }
            finally{
                stmt.close();
                conn.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 1;
    }// createUser()

    public Map<String,String> seeUser(String idnum) {
        Map<String,String> userIdMap = new HashMap<>();
        PreparedStatement stmt = null;
        try
        {
            Connection conn = ds.getConnection();
            String queryString = null;

            queryString = "SELECT * FROM Identity WHERE idnum = ?";
            stmt = conn.prepareStatement(queryString);
            stmt.setString(1, idnum);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String handle = rs.getString("handle");
                userIdMap.put("handle", handle);
                String fullname = rs.getString("fullname");
                userIdMap.put("fullname", fullname);
                String location = rs.getString("location");
                userIdMap.put("location", location);
                String email = rs.getString("email");
                userIdMap.put("email", email);
                String bdate = rs.getString("bdate");
                userIdMap.put("bdate", bdate);
                String joined = rs.getString("joined");
                userIdMap.put("joined", joined);
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
    } // seeUser()

    public Map<String, String> getSuggestions(Map<String,String> userInfo) {
        Map<String,String> result = new HashMap<String, String>();
        PreparedStatement stmt = null;
        try
        {
            Connection conn = ds.getConnection();
            String queryString = null;
            
            queryString = "select distinct ff.followed, s.handle from Identity as u join Follows as uf on u.idnum = uf.follower join Follows as ff on uf.followed = ff.follower join Identity as s on s.idnum = ff.followed where u.handle = ? and u.pass = ? and  ff.followed != u.idnum and ff.followed not in (select uf.followed from Identity as u join Follows as uf on u.idnum = uf.follower where u.handle = ? and u.pass = ?) limit 4";
            stmt = conn.prepareStatement(queryString);
            stmt.setString(1, userInfo.get("handle"));
            stmt.setString(2, userInfo.get("password"));
            stmt.setString(3, userInfo.get("handle"));
            stmt.setString(4, userInfo.get("password"));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String idnum = rs.getString("ff.followed");
                String handle = rs.getString("s.handle");
                result.put(idnum, handle);
            }
            rs.close();
            stmt.close();
            conn.close();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return result;
    } // getSuggestions()

    public Integer blockUser(Map<String,String> userInfo){
        PreparedStatement stmt = null;
        try
        {
            Connection conn = ds.getConnection();
            String queryString = null;
            try {
                queryString = "insert into Block (idnum, blocked) values (?,?)";
                stmt = conn.prepareStatement(queryString);
                stmt.setString(1, userInfo.get("idnum"));
                stmt.setString(2, userInfo.get("blockIdnum"));

                int count = stmt.executeUpdate();
            }
            catch(SQLException ex){
                stmt.close();
                conn.close();
                return 0;
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
            finally {
                stmt.close();
                conn.close();
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return 1;
    } // blockUser()

    public int followUser(String userRequestingFollow, String userToBeFollowed) {
        int StatusCode = 1;
        try {
            Connection connection = ds.getConnection();
            Statement selectStmt = connection.createStatement();
            Statement blockedStmt = connection.createStatement();
            Statement insertStmt = connection.createStatement();
            String Query = "SELECT idnum FROM Identity WHERE HANDLE =\"" + userRequestingFollow + "\";";
            ResultSet result = selectStmt.executeQuery(Query);

            if (result.next()) {
                String idnum = result.getString("idnum");
                Query = "SELECT blocked FROM Block WHERE idnum ="+userToBeFollowed+";";
                ResultSet resultBlock = blockedStmt.executeQuery(Query);
                if (resultBlock.next()){
                    StatusCode = 0;
                }
                else {
                    Query = "INSERT INTO Follows (follower, followed) VALUES (" + idnum + "," + userToBeFollowed + ");";
                    int updateStatus = insertStmt.executeUpdate(Query);
                    StatusCode = 1;
                }
            }
            else {
                StatusCode = -1;
            }

            result.close();
            selectStmt.close();
            blockedStmt.close();
            insertStmt.close();
            connection.close();
        }
        catch(Exception ex) {
            StatusCode = -1;
            ex.printStackTrace();
        }
        return StatusCode;
    } // followUser()

    public int unfollowUser(String userRequestingUnfollow, String userToBeUnfollowed) {
        int StatusCode = 1;
        try {
            Connection connection = ds.getConnection();
            Statement selectStmt = connection.createStatement();
            Statement followingStmt = connection.createStatement();
            Statement deleteStmt = connection.createStatement();
            String Query = "SELECT idnum FROM Identity WHERE HANDLE =\"" + userRequestingUnfollow + "\";";
            ResultSet result = selectStmt.executeQuery(Query);

            if (result.next()) {
                String idnum = result.getString("idnum");
                Query = "SELECT followed FROM Follows WHERE follower ="+idnum+" AND followed ="+userToBeUnfollowed+";";
                ResultSet resultFollow = followingStmt.executeQuery(Query);
                if (!resultFollow.next()){
                    StatusCode = 0;
                }
                else {
                    Query = "DELETE FROM Follows WHERE follower ="+idnum+" AND followed ="+userToBeUnfollowed+";";
                    int deleteStatus = deleteStmt.executeUpdate(Query);
                    StatusCode = 1;
                }
            }
            else {
                StatusCode = -1;
            }

            result.close();
            selectStmt.close();
            followingStmt.close();
            deleteStmt.close();
            connection.close();
        }
        catch(Exception ex) {
            StatusCode = -1;
            ex.printStackTrace();
        }
        return StatusCode;
    } // unfollowUser()

    public List<Map<String, String>> getTimeline(String idnum, String newest, String oldest){
        List<Map<String, String>> timeline = new ArrayList<>();
        PreparedStatement stmt = null;
        try
        {
            Connection conn = ds.getConnection();
            String queryString = null;

            queryString = "SELECT * FROM Story WHERE idnum IN (SELECT followed FROM Follows WHERE follower = ?) "
            + "AND idnum NOT IN (SELECT idnum FROM Block WHERE blocked = ?) "
            + "AND tstamp >= ? AND tstamp <= ? AND (CURRENT_TIMESTAMP < expires OR expires IS NULL) "


            + "UNION "
            + "";
            stmt = conn.prepareStatement(queryString);
            stmt.setString(1, idnum);
            stmt.setString(2, idnum);
            stmt.setString(3, oldest);
            stmt.setString(4, newest);
            stmt.setString(5, idnum);
            stmt.setString(6, idnum);
            stmt.setString(7, oldest);
            stmt.setString(8, newest);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {

            }
            rs.close();
            stmt.close();
            conn.close();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return timeline;
    } // getTimeline()
} // class DBEngine

package edu.uky.cs405g.sample.database;

// Used with permission from Dr. Bumgardner

//Authors:  Kyle Hume
//          Kelsey Cole
//          Sam Armstrong
//          Jacob Birge

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

    public Integer createStory(Map<String,String> storyInfo) {
        PreparedStatement stmt = null;
        try {
            //connect to the database
            Connection connect = ds.getConnection();
            try {
                //build the prepared statement
                String queryString = null;
                queryString = "INSERT INTO Story (idnum, chapter, url, expires) VALUES (?, ?, ?, ?)";
                stmt = connect.prepareStatement(queryString);
                stmt.setString(1, storyInfo.get("idnum")); //idnum
                stmt.setString(2, storyInfo.get("chapter")); //chapter
                stmt.setString(3, storyInfo.get("url")); //url
                stmt.setString(4, storyInfo.get("expires")); //expires
                //execute the prepared statement
                int count = stmt.executeUpdate();
            }
            //catch SQL exceptions
            catch (SQLIntegrityConstraintViolationException throwables) {
                //close everything and return error code
                stmt.close();
                connect.close();
                return throwables.getErrorCode();
            }
            //catch all other exceptions
            catch (Exception throwables) {
                throwables.printStackTrace();
                return 0;
            }
            finally {
                //close everything
                stmt.close();
                connect.close();
            }
        }
        //catch any exceptions that may occur
        catch (Exception throwables){
            throwables.printStackTrace();
        }
        //return that story was posted
        return 1;
    } //createStory

    public Integer isBlocked(Map<String,String> usersInfo){
        Integer status = 0;
        PreparedStatement stmt;
        try
        {
            //connect to database
            Connection conn = ds.getConnection();
            String queryString = null;
            //build the prepared statement
            queryString = "select if (? in (select b.blocked from Block as b where b.idnum = ?),1,0) as blocked, if (? in (select i.idnum from Identity as i),1,0) as userExists";
            stmt = conn.prepareStatement(queryString);
            stmt.setString(1,usersInfo.get("userId"));
            stmt.setString(2,usersInfo.get("otherUserId"));
            stmt.setString(3,usersInfo.get("otherUserId"));
            //execute the prepared statement
            ResultSet rs = stmt.executeQuery();
            //get the returned status
            while (rs.next()) {
                if (rs.getInt("userExists") == 0){
                    status = -1;
                }
                else{
                    status = rs.getInt("blocked");
                }
            }
            //close everything
            rs.close();
            stmt.close();
            conn.close();
        }
        //catch exceptions
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        //return 1 if user is blocked by other user and 0 if not
        return status;
    }

    public Integer isBlockedReprint(Map<String,String> reprintInfo) throws SQLException {
        Connection connect = ds.getConnection();
        try {
            PreparedStatement stmt = null;
            Statement blocked_stmt = connect.createStatement();
            Statement select_stmt = connect.createStatement();
            String query = "SELECT idnum FROM Story WHERE sidnum = " + reprintInfo.get("sidnum") + ";";
            ResultSet select_result = select_stmt.executeQuery(query);
            String poster_id = null;
            while (select_result.next()) {
                poster_id = select_result.getString("idnum");
            }

            String blockQuery = "SELECT idnum, blocked from Block WHERE blocked =" + reprintInfo.get("idnum") + " AND idnum = " + poster_id + ";";
            ResultSet block_result = blocked_stmt.executeQuery(blockQuery);
            int count = 0;
            while (block_result.next()) {
                count += 1;
            }
            if (count > 0){
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
        try {
            Connection connect = ds.getConnection();
            try {
                Integer like_boolean = 0;
                if (reprintInfo.get("likeit") == "true"){
                    like_boolean = 1;
                }
                String queryString = null;
                queryString = "INSERT INTO Reprint (sidnum, idnum, likeit) VALUES (?, ?, ?)";
                stmt = connect.prepareStatement(queryString);
                stmt.setString(1, reprintInfo.get("sidnum"));
                stmt.setString(2, reprintInfo.get("idnum")); //idnum
                stmt.setInt(3, like_boolean); //likeit

                int count = stmt.executeUpdate();
            } catch (SQLIntegrityConstraintViolationException throwables) {
                stmt.close();
                connect.close();
                return -2;
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

    public Map<String,String> validateUser(Map<String, String> userInfo) {
        Map<String,String> userIdMap = new HashMap<>();
        PreparedStatement stmt;
        try
        {
            //connect to database
            Connection conn = ds.getConnection();
            String queryString = null;
            //build the prepared statement
            queryString = "SELECT idnum FROM Identity WHERE handle = ? and pass = ?";
            stmt = conn.prepareStatement(queryString);
            stmt.setString(1,userInfo.get("handle"));
            stmt.setString(2,userInfo.get("password"));
            //execute the prepared statement
            ResultSet rs = stmt.executeQuery();
            //get the returned idnum
            while (rs.next()) {
                String idnum = rs.getString("idnum");
                userIdMap.put("idnum", idnum);
            }
            //close everything
            rs.close();
            stmt.close();
            conn.close();
        }
        //catch exceptions
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        //return the map containing the found idnum (or empty if user not found)
        return userIdMap;
    } // validateUser()

    public int createUser(Map<String, String> myMap) {
        try {
            //initialize the query and prepared statement
            String queryString = null;
            PreparedStatement stmt = null;
            //connect to database
            Connection conn = ds.getConnection();
            try {
                //build the prepared statement
                queryString = "INSERT INTO Identity (handle, pass, fullname, location, email, bdate) VALUES (?, ?, ?, ?, ?, ?)";
                stmt = conn.prepareStatement(queryString);
                stmt.setString(1, myMap.get("handle"));
                stmt.setString(2, myMap.get("password"));
                stmt.setString(3, myMap.get("fullname"));
                stmt.setString(4, myMap.get("location"));
                stmt.setString(5, myMap.get("xmail"));
                stmt.setString(6, myMap.get("bdate"));
                //execute the statement
                int count = stmt.executeUpdate();
            }
            //catch any exceptions from the database
            catch(SQLIntegrityConstraintViolationException ex){
                stmt.close();
                conn.close();
                return 0;
            }
            //catch all other exceptions
            catch(Exception ex){
                ex.printStackTrace();
            }
            finally{
                //make sure to close everything
                stmt.close();
                conn.close();
            }
        //catch unexpected exceptions that may occur
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //created user successfully
        return 1;
    }// createUser()

    public Map<String,String> seeUser(String idnum) {
        Map<String,String> userIdMap = new HashMap<>();
        PreparedStatement stmt = null;
        try
        {
            //connect to database
            Connection conn = ds.getConnection();
            //build prepared statement
            String queryString = null;
            queryString = "SELECT * FROM Identity WHERE idnum = ?";
            stmt = conn.prepareStatement(queryString);
            stmt.setString(1, idnum);
            //put all of the found info in a map
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                userIdMap.put("handle", rs.getString("handle"));
                userIdMap.put("fullname", rs.getString("fullname"));
                userIdMap.put("location", rs.getString("location"));
                userIdMap.put("email", rs.getString("email"));
                userIdMap.put("bdate", rs.getString("bdate"));
                userIdMap.put("joined", rs.getString("joined"));
            }
            //close everything
            rs.close();
            stmt.close();
            conn.close();
        }
        //catch any exceptions that may occur
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        //return the map with the person's
        return userIdMap;
    } // seeUser()

    public Map<String, String> getSuggestions(Map<String,String> userInfo) {
        Map<String,String> result = new HashMap<String, String>();
        PreparedStatement stmt = null;
        try
        {
            //connect to database
            Connection conn = ds.getConnection();
            //build prepared statement
            String queryString = null;
            queryString = "select distinct ff.followed, s.handle from Identity as u join Follows as uf " +
                    "on u.idnum = uf.follower join Follows as ff on uf.followed = ff.follower join Identity as s " +
                    "on s.idnum = ff.followed where u.handle = ? and u.pass = ? and  " +
                    "ff.followed != u.idnum and ff.followed not in (select uf.followed from Identity as u " +
                    "join Follows as uf on u.idnum = uf.follower where u.handle = ? and u.pass = ?) limit 4";
            stmt = conn.prepareStatement(queryString);
            stmt.setString(1, userInfo.get("handle"));
            stmt.setString(2, userInfo.get("password"));
            stmt.setString(3, userInfo.get("handle"));
            stmt.setString(4, userInfo.get("password"));
            //execute prepared statement
            ResultSet rs = stmt.executeQuery();
            //put returned suggestions into a map
            while (rs.next()) {
                String idnum = rs.getString("ff.followed");
                String handle = rs.getString("s.handle");
                result.put(idnum, handle);
            }
            //close everything
            rs.close();
            stmt.close();
            conn.close();
        }
        //catch all exceptions that may occur
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        //return map of suggested users
        return result;
    } // getSuggestions()

    public Integer blockUser(Map<String,String> userInfo){
        PreparedStatement stmt = null;
        try
        {
            //connect to database
            Connection conn = ds.getConnection();
            String queryString = null;
            try {
                //build prepared statement
                queryString = "insert into Block (idnum, blocked) values (?,?)";
                stmt = conn.prepareStatement(queryString);
                stmt.setString(1, userInfo.get("idnum"));
                stmt.setString(2, userInfo.get("blockIdnum"));
                //execute prepared statement
                int count = stmt.executeUpdate();
            }
            catch(SQLIntegrityConstraintViolationException ex){
                stmt.close();
                conn.close();
                return 0;
            }
            //catch all other exceptions
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
            finally {
                //close everything
                stmt.close();
                conn.close();
            }
        }
        //catch any other exceptions that may occur
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return 1;
    } // blockUser()

    public int followUser(String userRequestingFollow, String userToBeFollowed) {
        int StatusCode = 1;
        try {
            PreparedStatement stmt = null;
            Connection connection = ds.getConnection();
            try {
                //build prepared statement
                String Query = "INSERT INTO Follows (follower, followed) VALUES (?,?);";
                stmt = connection.prepareStatement(Query);
                stmt.setString(1, userRequestingFollow);
                stmt.setString(2, userToBeFollowed);
                //execute prepared statement
                int updateStatus = stmt.executeUpdate();
                //close everything
                stmt.close();
                connection.close();
            }
            //catch SQL constraint exceptions
            catch(SQLIntegrityConstraintViolationException ex){
                //close everything
                stmt.close();
                connection.close();
                return -2;
            }
        }
        //catch all other exceptions
        catch(Exception ex) {
            StatusCode = -1;
            ex.printStackTrace();
        }
        return StatusCode;
    } // followUser()

    public int unfollowUser(String userRequestingUnfollow, String userToBeUnfollowed) {
        int StatusCode;
        try {
            //connect to database
            Connection connection = ds.getConnection();
            PreparedStatement stmt = null;
            try {
                //build prepared statement
                String Query = "DELETE FROM Follows WHERE follower =? AND followed =?;";
                stmt = connection.prepareStatement(Query);
                stmt.setString(1, userRequestingUnfollow);
                stmt.setString(2, userToBeUnfollowed);
                //execute prepared statement
                int deleteStatus = stmt.executeUpdate();
                //nothing was deleted (didn't already follow)
                if (deleteStatus == 0){
                    StatusCode = 0;
                }
                //lines were deleted (success)
                else{
                    StatusCode = 1;
                }
                // close everything
                stmt.close();
                connection.close();
            }
            // catch SQL constraint exceptions
            catch(SQLIntegrityConstraintViolationException ex){
                stmt.close();
                connection.close();
                return -2;
            }
        }
        // catch any other exceptions that may occur
        catch(Exception ex) {
            StatusCode = -1;
            ex.printStackTrace();
        }
        // return statuscode (1=success, 0=person not followed, -2=SQL constraint exception)
        return StatusCode;
    } // unfollowUser()

    public List<Map<String, String>> getTimeline(String idnum, String newest, String oldest){
        List<Map<String, String>> timeline = new ArrayList<>();
        PreparedStatement stmt = null;
        int size = 0;
        try
        {
            Connection conn = ds.getConnection();
            String queryString = null;

            queryString = "SELECT 'story' as type, i.handle as handle, s.sidnum as sidnum, s.chapter as chapter, s.tstamp as tstamp "
                    + "FROM Story as s join Identity as i on i.idnum = s.idnum WHERE s.idnum IN (SELECT followed FROM Follows WHERE follower = ?) "
                    + "AND s.idnum NOT IN (SELECT idnum FROM Block WHERE blocked = ?) "
                    + "AND s.tstamp >= ? AND s.tstamp <= ? AND (CURRENT_TIMESTAMP < s.expires OR s.expires IS NULL)"
                    + "UNION SELECT 'reprint' as type, i.handle as handle, s.sidnum as sidnum, s.chapter as chapter, s.tstamp as tstamp "
                    + "from Reprint as r join Story as s on r.sidnum = s.sidnum join Identity as i on s.idnum = i.idnum"
                    + "where r.idnum IN (select followed from Follows where follower = ?) "
                    + "AND s.idnum NOT IN (select idnum from Block where blocked = ?) "
                    + "AND s.tstamp >= ? AND s.tstamp <= ? "
                    + "AND (CURRENT_TIMESTAMP < s.expires OR s.expires IS NULL)";


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
                Map<String,String> thisPost = new HashMap<String, String>();
                String type = rs.getString("type");
                thisPost.put("type", type);
                String author = rs.getString("handle");
                thisPost.put("author", author);
                String sidnum = rs.getString("sidnum");
                thisPost.put("sidnum", sidnum);
                String chapter = rs.getString("chapter");
                thisPost.put("chapter", chapter);
                String posted = rs.getString("tstamp");
                thisPost.put("posted", posted);
                timeline.add(thisPost);
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

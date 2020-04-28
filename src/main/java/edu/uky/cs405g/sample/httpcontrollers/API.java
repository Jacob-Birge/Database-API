package edu.uky.cs405g.sample.httpcontrollers;
//
// Sample code used with permission from Dr. Bumgardner
//
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.uky.cs405g.sample.Launcher;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

// test comment

@Path("/api")
public class API {

    private Type mapType;
    private Gson gson;

    public API() {
        mapType = new TypeToken<Map<String, String>>() {
        }.getType();
        gson = new Gson();
    }

    //curl http://localhost:9990/api/status
    //{"status_code":1}
    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response healthcheck() {
        String responseString = "{\"status_code\":0}";
        try {
            //Here is where you would put your system test, 
			//but this is not required.
            //We just want to make sure your API is up and active/
            //status_code = 0 , API is offline
            //status_code = 1 , API is online
            responseString = "{\"status_code\":1}";
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString)
				.header("Access-Control-Allow-Origin", "*").build();
    } // healthcheck()

    //curl http://localhost:9998/api/listusers
    //{"1":"@paul","2":"@chuck"}
    @GET
    @Path("/listusers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listUsers() {
        String responseString = "{}";
        try {
            Map<String, String> teamMap = Launcher.dbEngine.getUsers();
            responseString = Launcher.gson.toJson(teamMap);
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    } // listUsers()


    //curl -d '{"foo":"silly1","bar":"silly2"}' \
	//     -H "Content-Type: application/json" \
    //     -X POST  http://localhost:9990/api/exampleJSON
	//
    //{"status_code":1, "foo":silly1, "bar":silly2}
    @POST
    @Path("/exampleJSON")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response exampleJSON(InputStream inputData) {
        String responseString = "{\"status_code\":0}";
        StringBuilder crunchifyBuilder = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(inputData));
            String line = null;
            while ((line=in.readLine()) != null) {
                crunchifyBuilder.append(line);
            }
            String jsonString = crunchifyBuilder.toString();

            Map<String, String> myMap = gson.fromJson(jsonString, mapType);
            String fooval = myMap.get("foo");
            String barval = myMap.get("bar");
            //Here is where you would put your system test,
            //but this is not required.
            //We just want to make sure your API is up and active/
            //status_code = 0 , API is offline
            //status_code = 1 , API is online
            responseString = "{\"status_code\":1, "
					+"\"foo\":\""+fooval+"\", "
					+"\"bar\":\""+barval+"\"}";
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    } // exampleJSON()

    //curl http://localhost:9990/api/exampleGETBDATE/2
    //{"bdate":"1968-01-26"}
    @GET
    @Path("/exampleGETBDATE/{idnum}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response exampleBDATE(@PathParam("idnum") String idnum) {
        String responseString = "{\"status_code\":0}";
        StringBuilder crunchifyBuilder = new StringBuilder();
        try {
            Map<String,String> teamMap = Launcher.dbEngine.getBDATE(idnum);
            responseString = Launcher.gson.toJson(teamMap);
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    } // exampleBDATE

    //curl -d '{"handle":"@cooldude42", "password":"mysecret!", "fullname":"Angus Mize", "location":"Kentucky", "xmail":"none@nowhere.com", "bdate":"1970-07-01"}' \
    //     -H "Content-Type: application/json" \
    //     -X POST  http://localhost:9990/api/createuser
    //
    // Output: {"status":"4"} // positive number is the Identity.idnum created.
    // Output: {"status":"-2", "error":"SQL Constraint Exception"}. [EDIT 04/14]
    //Kyle
    @POST
    @Path("/createuser")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createUser(InputStream inputData) {
        String responseString = "{\"status_code\":0}";
        StringBuilder crunchifyBuilder = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(inputData));
            String line = null;
            while ((line=in.readLine()) != null) {
                crunchifyBuilder.append(line);
            }
            String jsonString = crunchifyBuilder.toString();

            Map<String, String> myMap = gson.fromJson(jsonString, mapType);
            String handleVal = myMap.get("handle");
            String passVal = myMap.get("pass");
            String fullnameVal = myMap.get("fullname");
            String locationVal = myMap.get("location");
            String xmailVal = myMap.get("xmail");
            String bdateVal = myMap.get("bdate");

            int result = Launcher.dbEngine.createUser(handleVal, passVal, fullnameVal, locationVal, xmailVal, bdateVal);

            // check if query seemed to have executed correctly
            if (result == 0){
                responseString = "{\"status_code\":-2, "
                        +"\"error\":\"SQL Constraint Exception\"}\n";
            }
            else{
                // check if query inserted correctly
                Map<String, String> userInfo = new HashMap<>();
                userInfo.put("handle", handleVal);
                userInfo.put("password", passVal);
                Map<String,String> teamMap = Launcher.dbEngine.validateUser(userInfo);
                if (teamMap.isEmpty()){
                    responseString = "{\"status_code\":-2, "
                            +"\"error\":\"SQL Constraint Exception\"}\n";
                }
                else {
                    //status_code = 0 , API is offline
                    //status_code = 1 , API is online
                    responseString = "{\"status_code\":" + teamMap.get("idnum") + "}";
                }
            }

        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    } // createUser()

    //curl -d '{"handle":"@cooldude42", "password":"mysecret!"}' \
    //     -H "Content-Type: application/json" \
    //     -X POST  http://localhost:9990/api/seeuser/2
    // 2 = Identity.idnum
    // Output: {"status":"1", "handle":"@carlos", "fullname":"Carlos Mize", "location":"Kentucky", "email":carlos@notgmail.com", "bdate":"1970-01-26","joined":"2020-04-01"}
    // Output: {}. // no match found, could be blocked, user doesn't know.
    //Kyle
    @POST
    @Path("/seeuser/{idnum}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response seeUser(InputStream inputData, @PathParam("idnum") String idnum) {
        String responseString = "{\"status_code\":0}";
        StringBuilder crunchifyBuilder = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(inputData));
            String line = null;
            while ((line=in.readLine()) != null) {
                crunchifyBuilder.append(line);
            }
            String jsonString = crunchifyBuilder.toString();

            Map<String, String> myMap = gson.fromJson(jsonString, mapType);
            Map<String,String> teamMap = Launcher.dbEngine.validateUser(myMap);
            if (teamMap.isEmpty()){
                responseString = "{\"status_code\":-10, "
                        +"\"error\":\"invalid credentials\"}";
            }
            else {
                //status_code = 0 , API is offline
                //status_code = 1 , API is online
                Map<String,String> userMap = Launcher.dbEngine.seeUser(idnum);
                if (userMap.isEmpty()){
                    responseString = "{}";
                }
                else {
                    String handle = userMap.get("handle");
                    String fullname = userMap.get("fullname");
                    String location = userMap.get("location");
                    String email = userMap.get("email");
                    String bdate = userMap.get("bdate");
                    String joined = userMap.get("joined");
                    responseString = "{\"status_code\":1, "
                            + "\"handle\":\"" + handle + "\", "
                            + "\"fullname\":\"" + fullname + "\", "
                            + "\"location\":\"" + location + "\", "
                            + "\"email\":\"" + email + "\", "
                            + "\"bdate\":\"" + bdate + "\", "
                            + "\"joined\":\"" + joined + "\"}";
                }
            }
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    } // seeUser()

    //curl -d '{"handle":"@cooldude42", "password":"mysecret!"}' \
    //     -H "Content-Type: application/json" \
    //     -X POST  http://localhost:9990/api/suggestions
    //
    // Output, status > 0 is the number of suggested people returned
    // Output: {"status":"3", "idnums":"1,2,4", "handles":"@paul,@carlos","@fake"}
    // Output: {"status":"0", "error":"no suggestions"}
    // Jacob
    @POST
    @Path("/suggestions")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response suggestions(InputStream inputData) {
        String responseString = "{\"status_code\":0}";
        StringBuilder crunchifyBuilder = new StringBuilder();
        try {
            //get info passed in json
            BufferedReader in = new BufferedReader(new InputStreamReader(inputData));
            String line = null;
            while ((line=in.readLine()) != null) {
                crunchifyBuilder.append(line);
            }
            String jsonString = crunchifyBuilder.toString();
            Map<String, String> userInfo = gson.fromJson(jsonString, mapType);
            //validate the user
            Map<String,String> teamMap = Launcher.dbEngine.validateUser(userInfo);
            if (teamMap.isEmpty()){
                responseString = "{\"status\":\"-10\", "
                        +"\"error\":\"invalid credentials\"}";
            }
            else {
                //get follow suggestions
                Map<String, String> suggestions = Launcher.dbEngine.getSuggestions(userInfo);
                //count how many suggestions were found
                int sugLen = suggestions.size();
                //build status code portion
                responseString = "{\"status\":\"" + Integer.toString(sugLen) + "\", \"";
                //if no suggestions were found
                if (sugLen == 0){
                    responseString = responseString + "error\":\"no suggestions\"}";
                }
                //if suggestions were found
                else {
                    //get list of idnums and handles for suggestions
                    String idnums = "";
                    String handles = "";
                    for (Map.Entry<String, String> entry : suggestions.entrySet()){
                        idnums += entry.getKey() + ",";
                        handles += entry.getValue() + ",";
                    }
                    //remove extra comma at end of list
                    idnums = idnums.substring(0, idnums.length() - 1);
                    handles = handles.substring(0, handles.length() - 1);
                    //add lists to json response
                    responseString = responseString + "idnums\":\"" + idnums + "\",";
                    responseString = responseString + "\"handles\":\"" + handles + "\"}";
                }
            }
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    } // suggestions()

    //curl -d '{"handle":"@cooldude42", "password":"mysecret!", "chapter":"I ate at Mario's!", "url":"http://imagesite.dne/marios.jpg"}' \
    //     -H "Content-Type: application/json" \
    //     -X POST  http://localhost:9990/api/poststory
    //
    // Output: {"status":"1"}
    // Output: {"status":"0", "error":"invalid expires date"}
    // Output: {"status":"0", "error":"expire date in past"}
    // Output: {"status":"0", "error":"missing chapter"}
    // Kelsey
    @POST
    @Path("/poststory")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postStory(InputStream inputData) {
        String responseString = "{\"status_code\":0}";
        StringBuilder crunchifyBuilder = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(inputData));
            String line = null;
            while ((line=in.readLine()) != null) {
                crunchifyBuilder.append(line);
            }
            String jsonString = crunchifyBuilder.toString();

            Map<String, String> myMap = gson.fromJson(jsonString, mapType);
            String fooval = myMap.get("foo");
            String barval = myMap.get("bar");
            //Here is where you would put your system test,
            //but this is not required.
            //We just want to make sure your API is up and active/
            //status_code = 0 , API is offline
            //status_code = 1 , API is online
            responseString = "{\"status_code\":1, "
                    +"\"foo\":\""+fooval+"\", "
                    +"\"bar\":\""+barval+"\"}";
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    } // postStory()

    //curl -d '{"handle":"@cooldude42", "password":"mysecret!", "likeit":true}' \
    //     -H "Content-Type: application/json" \
    //     -X POST  http://localhost:9990/api/reprint/45
    // 45 = Story.sidnum
    // Output: {"status":"1"}
    // Output: {"status":"0", "error":"blocked"}
    // Output: {"status":"0", "error":"story not found"}
    // Kelsey
    @POST
    @Path("/reprint")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response reprint(InputStream inputData) {
        String responseString = "{\"status_code\":0}";
        StringBuilder crunchifyBuilder = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(inputData));
            String line = null;
            while ((line=in.readLine()) != null) {
                crunchifyBuilder.append(line);
            }
            String jsonString = crunchifyBuilder.toString();

            Map<String, String> myMap = gson.fromJson(jsonString, mapType);
            String fooval = myMap.get("foo");
            String barval = myMap.get("bar");
            //Here is where you would put your system test,
            //but this is not required.
            //We just want to make sure your API is up and active/
            //status_code = 0 , API is offline
            //status_code = 1 , API is online
            responseString = "{\"status_code\":1, "
                    +"\"foo\":\""+fooval+"\", "
                    +"\"bar\":\""+barval+"\"}";
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    } // reprint()

    //curl -d '{"handle":"@cooldude42", "password":"mysecret!"}' \
    //     -H "Content-Type: application/json" \
    //     -X POST  http://localhost:9990/api/follow/2
    // 2 = Identity.idnum
    // Output: {"status":"1"}
    // Output: {"status":"0", "error":"blocked"}
    // DNE
    //Sam
    @POST
    @Path("/follow/{idnum}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response follow(@PathParam("idnum") String idnum, InputStream inputData) {
        String responseString = "{\"status_code\":0}\n";
        StringBuilder crunchifyBuilder = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(inputData));
            String line = null;
            while ((line=in.readLine()) != null) {
                crunchifyBuilder.append(line);
            }
            String jsonString = crunchifyBuilder.toString();

            Map<String, String> myMap = gson.fromJson(jsonString, mapType);
            String handle = myMap.get("handle");

            // Validating User
            Map<String,String> teamMap = Launcher.dbEngine.validateUser(myMap);
            if (teamMap.isEmpty()){
                responseString = "{\"status_code\":-1, "
                        +"\"error\":\"invalid credentials\"}\n";
                return Response.ok(responseString)
                        .header("Access-Control-Allow-Origin", "*").build();
            }

            // Following the user
            int ResponseStatus = Launcher.dbEngine.followUser(handle, idnum);
            if (ResponseStatus == 1){
                responseString = "{\"status_code\":1}\n";
                return Response.ok(responseString)
                        .header("Access-Control-Allow-Origin", "*").build();
            }
            else if (ResponseStatus == 0){
                responseString = "{\"status_code\":0, "
                        +"\"error\":\"blocked\"}\n";
                return Response.ok(responseString)
                        .header("Access-Control-Allow-Origin", "*").build();
            }
            else if (ResponseStatus == -1){
                responseString = "{\"status_code\":-1, "
                        +"\"error\":\"User to be followed does not exist\"}\n";
                return Response.ok(responseString)
                        .header("Access-Control-Allow-Origin", "*").build();
            }

        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    } // follow()

    //curl -d '{"handle":"@cooldude42", "password":"mysecret!"}' \
    //     -H "Content-Type: application/json" \
    //     -X POST  http://localhost:9990/api/unfollow/2
    // 2 = Identity.idnum
    //
    // Output: {"status":"1"}
    // Output: {"status":"0", "error":"not currently followed"}
    //Sam
    @POST
    @Path("/unfollow/{idnum}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response unfollow(@PathParam("idnum") String idnum, InputStream inputData) {
        String responseString = "{\"status_code\":0}\n";
        StringBuilder crunchifyBuilder = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(inputData));
            String line = null;
            while ((line=in.readLine()) != null) {
                crunchifyBuilder.append(line);
            }
            String jsonString = crunchifyBuilder.toString();
            Map<String, String> myMap = gson.fromJson(jsonString, mapType);
            String handle = myMap.get("handle");

            // Validating User
            Map<String,String> teamMap = Launcher.dbEngine.validateUser(myMap);
            if (teamMap.isEmpty()){
                responseString = "{\"status_code\":-1, "
                        +"\"error\":\"invalid credentials\"}\n";
                return Response.ok(responseString)
                        .header("Access-Control-Allow-Origin", "*").build();
            }

            // Unfollowing the user
            int ResponseStatus = Launcher.dbEngine.unfollowUser(handle, idnum);
            if (ResponseStatus == 1){
                responseString = "{\"status_code\":1}\n";
                return Response.ok(responseString)
                        .header("Access-Control-Allow-Origin", "*").build();
            }
            else if (ResponseStatus == 0){
                responseString = "{\"status_code\":0, "
                        +"\"error\":\"not currently followed\"}\n";
                return Response.ok(responseString)
                        .header("Access-Control-Allow-Origin", "*").build();
            }
            else if (ResponseStatus == -1){
                responseString = "{\"status_code\":-1, "
                        +"\"error\":\"User to be unfollowed does not exist\"}\n";
                return Response.ok(responseString)
                        .header("Access-Control-Allow-Origin", "*").build();
            }

        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    } // unfollow()

    //curl -d '{"handle":"@cooldude42", "password":"mysecret!"}' \
    //     -H "Content-Type: application/json" \
    //     -X POST  http://localhost:9990/api/block/2
    // 2 = Identity.idnum
    //
    // Output: {"status":"1"}
    // Output: {"status":"0", "error":"DNE"}
    // Jacob
    @POST
    @Path("/block/{idnum}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response block(InputStream inputData, @PathParam("idnum") String idnum) {
        String responseString;
        StringBuilder crunchifyBuilder = new StringBuilder();
        try {
            //get info passed in json
            BufferedReader in = new BufferedReader(new InputStreamReader(inputData));
            String line = null;
            while ((line=in.readLine()) != null) {
                crunchifyBuilder.append(line);
            }
            String jsonString = crunchifyBuilder.toString();
            Map<String, String> userInfo = gson.fromJson(jsonString, mapType);

            //validate the user
            Map<String,String> teamMap = Launcher.dbEngine.validateUser(userInfo);
            if (teamMap.isEmpty()){
                responseString = "{\"status\":\"-10\", "
                        +"\"error\":\"invalid credentials\"}";
            }
            else{
                userInfo.put("idnum", teamMap.get("idnum"));
                userInfo.put("blockIdnum", idnum);
                Integer blocked = Launcher.dbEngine.blockUser(userInfo);
                responseString = "{\"status\":\"" + blocked.toString() + "\"";
                if (blocked == 0){
                    responseString += ",\"error\":\"DNE\"";
                }
                responseString += "}";
            }
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    } // block()

    //curl -d '{"handle":"@cooldude42", "password":"mysecret!", "newest":"2020-04-02 15:33:59", "oldest":"2020-03-29 00:00:01"}' \
    //     -H "Content-Type: application/json" \
    //     -X POST  http://localhost:9990/api/timeline
    //
    // Output: {"0":"{\"type\":\"story\",\"author\":\"@cooldude44\",\"sidnum\":\"14\",\"chapter\":\"Just some set math, SQL is super fun!\",\"posted\":\"2020-04-16 15:37:48\"}","1":"{\"type\":\"reprint\",\"author\":\"@cooldude44\",\"sidnum\":\"15\",\"chapter\":\"JSON objects are fun and useful!\",\"posted\":\"2020-04-15 10:37:44\"}","status":"2"}
    // Output: {"status":"0"}
    @POST
    @Path("/timeline")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response timeline(InputStream inputData) {
        String responseString = "{\"status_code\":0}";
        StringBuilder crunchifyBuilder = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(inputData));
            String line = null;
            while ((line=in.readLine()) != null) {
                crunchifyBuilder.append(line);
            }
            String jsonString = crunchifyBuilder.toString();

            Map<String, String> userInfo = gson.fromJson(jsonString, mapType);
            Map<String,String> validation = Launcher.dbEngine.validateUser(userInfo);
            if (validation.isEmpty()){
                responseString = "{\"status\":\"-10\", "
                        +"\"error\":\"invalid credentials\"}";
            }
            else {
                List<Map<String, String>> stories = Launcher.dbEngine.getTimeline(userInfo);
                responseString = "{";
                for (int i=0; i<stories.size(); i++){
                    Map<String, String> currStory = stories.get(i);
                    responseString += "\"" + i + "\":\"{";
                    responseString += "\"type\":\"" + currStory.get("type") + "\"";
                    responseString += ",\"author\":\"" + currStory.get("author") + "\"";
                    responseString += ",\"sidnum\":\"" + currStory.get("sidnum") + "\"";
                    responseString += ",\"chapter\":\"" + currStory.get("chapter") + "\"";
                    responseString += ",\"posted\":\"" + currStory.get("posted") + "\"}\"";
                }
                responseString += "\"status\":\"" + stories.size() + "\"}";
            }
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    } // timeline()

} // API.java

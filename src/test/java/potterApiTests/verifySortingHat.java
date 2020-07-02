package potterApiTests;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.Array;
import java.util.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.*;

public class verifySortingHat {
    String _id;
    String api="$2a$10$acmYxGYfRQw9of/8V4o/1eILm6HcCu885NvftB81W6Qd/AkMhHdkq";
    List<Object> listidsofGryffindor;

    @BeforeClass
    public void setUp(){
        RestAssured.baseURI="https://www.potterapi.com/v1";
    }
    @Test
    public void test1(){
        List<String>  houses = new ArrayList<>();
        houses.add("\"Gryffindor\"");
        houses.add("\"Ravenclaw\"");
        houses.add("\"Slytherin\"");
        houses.add("\"Hufflepuff\"");

        Response response = given().accept(ContentType.JSON)
                .when().get("sortingHat");

        String s = response.body().asString();
        System.out.println("s = " + s);
        for (String house : houses) {
            System.out.println("\""+house+"\"");
            if(s.contains(house)){
                System.out.println("The body has "+s +" that is the same as " + house);
            }

        }

    }
    @Test
    public void verifyBadKey(){

        Response response = given().accept(ContentType.JSON)
                .and().queryParam("key", api+"1")
                .when().get("/characters");
        assertEquals(response.statusCode(),401);
        assertEquals(response.contentType(),"application/json; charset=utf-8");
        assertTrue(response.statusLine().contains("Unauthorized"));

        JsonPath jsonPath =response.jsonPath();

        String actualMessage =jsonPath.prettyPrint();
        String expectedmessage="\"error\": \"API Key Not Found\"";
        assertTrue(actualMessage.contains(expectedmessage));
    }

    @Test
    public void verifyNoKey(){
        Response response = given().accept(ContentType.JSON)
                .when().get("/characters");

        assertEquals(response.statusCode(),409);
        assertEquals(response.contentType(),"application/json; charset=utf-8");
        assertTrue(response.statusLine().contains("Conflict"));

        JsonPath jsonPath =response.jsonPath();

        String actualMessage=response.jsonPath().prettyPrint();
        String expectedMessage="\"error\": \"Must pass API key for request\"";
        assertTrue(actualMessage.contains(expectedMessage));

    }
    @Test
    public void VerifyNumberOfCharacters(){

        Response response = given().accept(ContentType.JSON)
                .queryParam("key", api)
                .when().get("/characters");

        assertEquals(response.statusCode(),200);
        assertEquals(response.contentType(),"application/json; charset=utf-8");

        JsonPath jsonPath = response.jsonPath();
        List<Map<String,Object>> mapList =response.body().as(List.class);
        assertEquals(mapList.size(),195);

    }
    @Test
    public void verifyNumberOfCharacterIdAndHouse(){

        Response response = given().accept(ContentType.JSON)
                .queryParam("key", api)
                .when().get("/characters");

        assertEquals(response.statusCode(),200);
        assertEquals(response.contentType(),"application/json; charset=utf-8");

        List<Map<String,Object>> lists =response.body().as(List.class);
        for (Map<String, Object> list : lists) {
            String idNumber = (String) list.get("_id");
            assertTrue(!idNumber.isEmpty());
            boolean booleanType= (boolean) list.get("dumbledoresArmy");

            if(list.get("house")!=null){
                String houseName= (String) list.get("house");
                if(houseName.equals("Gryffindor") || houseName.equals("Ravenclaw")
                        || houseName.equals("Slytherin") || houseName.equals("Hufflepuff")){
                }else{
                    System.out.println(houseName+" is not in the list");
                }
            }
        }

    }
    @Test
    public void verifiedAllCharacterInformation(){
        Response response = given().accept(ContentType.JSON)
                .queryParam("key",api)
                .when().get("/characters");
        assertEquals(response.statusCode(),200);
        assertEquals(response.contentType(),"application/json; charset=utf-8");

        // put the all characters into a list of maps.
        List<Map<String,Object>> allCharacterInfo=response.body().as(List.class);

        //select a random number to pick a random character info.
        Random rn = new Random();
        int number =rn.nextInt(195);
        System.out.println("Random number: "+number);
        // put the selected cha into a map
        Map<String,Object> randomChaInfo=allCharacterInfo.get(number);
        System.out.println("randomChaInfo.get(\"name\") = " + randomChaInfo.get("name"));

        //retrieve the selected random character with information.
        String name = (String) allCharacterInfo.get(number).get("name");
        Response response1 = given().accept(ContentType.JSON)
                .queryParam("key",api)
                .and().queryParam("name",name)
                .when().get("/characters");

        List<Map<String,Object>> singleCh=response1.body().as(List.class);
        //put the the selected cha into a map in order to assert the fields.
        Map<String,Object> actualChInfo = singleCh.get(0);
        System.out.println("actualChInfo.get(\"name\") = " + actualChInfo.get("name"));

        // assert the fields of the character.
        assertEquals(actualChInfo,randomChaInfo);
    }
    @Test
    public void verifyNameSearch(){
        Response response = given().accept(ContentType.JSON)
                .queryParam("key", api)
                .queryParam("name", "Harry Potter")
                .when().get("/characters");
       assertEquals(response.statusCode(),200);
       assertEquals(response.contentType(),"application/json; charset=utf-8");

       //using HamCrestMatcher class, verify status code,content type and the name.
        given().accept(ContentType.JSON)
                .queryParam("key", api)
                .queryParam("name", "Harry Potter")
                .when().get("/characters")
                .then().assertThat().statusCode(200)
                .and().contentType("application/json; charset=utf-8")
                .assertThat().body("name[0]",equalTo("Harry Potter"));

       //send a request with query param with name of "Marry Potter" with the HamCrestMatcher.
       // verify the body is empty.
        given().accept(ContentType.JSON)
                .queryParam("key", api)
                .queryParam("name", "Marry Potter")
                .when().get("/characters")
                .then().assertThat().statusCode(200)
                .and().contentType("application/json; charset=utf-8")
                .body("", Matchers.empty());

    }
    @Test
    public void verifyHouseMembers(){
        Response response = given().accept(ContentType.JSON)
                .queryParam("key", api)
                .when().get("/houses");
        assertEquals(response.statusCode(),200);
        assertEquals(response.contentType(),"application/json; charset=utf-8");

        List<Map<String,Object>> allHouseList=response.body().as(List.class);

        // find out the id of the gryffindor house and put it into a list.
        //name of the list is listidsofGryffindor which declared globally.

        //buy using a foreach loop to get the house name one by one and find out "Gryffindor"
        //when get the "Gryffindor" retrieve the id of the house.
        // place the ids of the Gryffindor into a list in order to assert.
        for (Map<String, Object> stringObjectMap : allHouseList) {
            String GryffindorHouse= (String) stringObjectMap.get("name");
            System.out.println(GryffindorHouse);
            if (GryffindorHouse.equals("Gryffindor")){
                _id = (String) stringObjectMap.get("_id");
                System.out.println(_id);
                listidsofGryffindor = (List<Object>) stringObjectMap.get("members");
                break;
            }
        }
        Response response1 = given().accept(ContentType.JSON)
                .queryParam("key", api)
                .pathParam("houseId", _id)
                .when().get("/houses/{houseId}");
        // create a jsonPath object to reach the ids.
        JsonPath jsonPath = response1.jsonPath();

        // put the ids of gryffindor into a list
        List<Object> actualListofIdsOfGryFfindor = jsonPath.getList("members[0]._id");
        System.out.println(actualListofIdsOfGryFfindor.size());

        // assert the ids of Gryffindor from the both sources.
        assertEquals(actualListofIdsOfGryFfindor,listidsofGryffindor);
    }
    @Test
    public void verifyHouseMembersAgain(){

        Response response = given().accept(ContentType.JSON)
                .queryParam("key", api)
                .pathParam("houseId", "5a05e2b252f721a3cf2ea33f")
                .when().get("/houses/{houseId}");
        // put the whole ids into a list.
        List<String> idsFromHouses=response.path("members[0]._id");
        System.out.println(idsFromHouses);
        System.out.println("idsFromHouses.size() = " + idsFromHouses.size());

        Response response1 = given().accept(ContentType.JSON)
                .queryParam("key", api)
                .queryParam("house", "Gryffindor")
                .when().get("/characters");

        JsonPath jsonPath = response1.jsonPath();
        List<String> idsFromCharacters = jsonPath.getList("_id");
        System.out.println(idsFromCharacters);
        System.out.println("idsFromCharacters.size() = " + idsFromCharacters.size());

        assertEquals(idsFromCharacters,idsFromHouses);
        //there is one id extra in the characters list.
    }
    @Test
    public void verifyHouseWithMostMembers(){
        Response response = given().accept(ContentType.JSON)
                .queryParam("key", api)
                .when().get("/houses");

        assertEquals(response.statusCode(),200);
        assertEquals(response.contentType(),"application/json; charset=utf-8");
        // put the houses in a list of map
        List<Map<String,Object>> allHouseList=response.body().as(List.class);
            int max=0;
            String houseName="";
        for (Map<String, Object> eachHouse : allHouseList) {
            //make a list for each the members of each houses.
            List<String> numberOfMembers= (List<String>) eachHouse.get("members");

            System.out.println("Number of members in "+eachHouse.get("name")+": " + numberOfMembers.size());
            // compare the number of members in each house.
            if(numberOfMembers.size()>max){
                houseName= (String) eachHouse.get("name");
                max=numberOfMembers.size();
            }
        }
        //print the house name and number.
        System.out.println(houseName+" has most member with  "+max+".");
    }
}

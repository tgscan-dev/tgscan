package xyz.tgscan.dto;

import com.fasterxml.jackson.annotation.JsonProperty;// import com.fasterxml.jackson.databind.ObjectMapper; // version 2.11.1

import java.util.ArrayList;
// import com.fasterxml.jackson.annotation.JsonProperty; // version 2.11.1

/* ObjectMapper om = new ObjectMapper();
Root root = om.readValue(myJsonString, Root.class); */
public class AS{
    @JsonProperty("Query") 
    public String query;
    @JsonProperty("FullResults") 
    public int fullResults;
    @JsonProperty("Results") 
    public ArrayList<Result> results;
}


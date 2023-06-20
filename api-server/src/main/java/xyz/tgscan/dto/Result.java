package xyz.tgscan.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class Result {
  @JsonProperty("Type")
  public String type;

  @JsonProperty("Suggests")
  public ArrayList<Suggest> suggests;
}

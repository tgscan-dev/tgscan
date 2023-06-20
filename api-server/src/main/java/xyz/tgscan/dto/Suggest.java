package xyz.tgscan.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Suggest {
  @JsonProperty("Txt")
  public String txt;

  @JsonProperty("Type")
  public String type;

  @JsonProperty("Sk")
  public String sk;

  @JsonProperty("HCS")
  public int hCS;
}

package xyz.tgscan.dto;

import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class QueryDTO {
  private String kw;
  private String correct;
  private Map<String, Float> termWeight;
  private String tokens;
}

package xyz.tgscan.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MessageDocHighlightingDTO {
  private String title;
  private String desc;
}

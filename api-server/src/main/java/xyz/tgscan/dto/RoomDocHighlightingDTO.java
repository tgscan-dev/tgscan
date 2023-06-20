package xyz.tgscan.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RoomDocHighlightingDTO {
  private String name;
  private String jhiDesc;
}

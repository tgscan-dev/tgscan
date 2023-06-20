package xyz.tgscan.dto;

import jakarta.persistence.Id;
import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.elasticsearch.annotations.Document;
import xyz.tgscan.enums.IdxConstant;

@Data
@Accessors(chain = true)
@Document(indexName = IdxConstant.ROOM_IDX, createIndex = false)
public class RoomDocDTO {

  @Id private Long id;

  private String link;

  private Integer memberCnt;

  private String type;

  private String status;
  private RoomDocHighlightingDTO highlighting;

  public static RoomDocDTO fromTgRoomDoc(Map tgRoomDoc, String name, String jhiDesc) {
    RoomDocDTO roomDocDTO = new RoomDocDTO();
    roomDocDTO.setId(Long.valueOf(tgRoomDoc.get("id").toString()));
    roomDocDTO.setLink((String) tgRoomDoc.get("link"));
    roomDocDTO.setMemberCnt((Integer) tgRoomDoc.get("memberCnt"));
    roomDocDTO.setType((String) tgRoomDoc.get("type"));
    roomDocDTO.setStatus((String) tgRoomDoc.get("status"));
    roomDocDTO.setHighlighting(new RoomDocHighlightingDTO().setName(name).setJhiDesc(jhiDesc));
    return roomDocDTO;
  }
}

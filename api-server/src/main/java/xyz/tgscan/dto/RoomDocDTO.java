package xyz.tgscan.dto;

import jakarta.persistence.Id;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.elasticsearch.annotations.Document;
import xyz.tgscan.enums.IdxConstant;

@Data
@Accessors(chain = true)
@Document(indexName = IdxConstant.ROOM_IDX, createIndex = false)
public class RoomDocDTO {

  @Id private String id;

  private String link;

  private Integer memberCnt;

  private String type;
  private String lang;

  private List<String> tags;
  private String category;

  private String status;
  private RoomDocHighlightingDTO highlighting;

  public static RoomDocDTO fromTgRoomDoc(Map tgRoomDoc, String name, String jhiDesc) {
    RoomDocDTO roomDocDTO = new RoomDocDTO();
    roomDocDTO.setId(tgRoomDoc.get("id").toString());
    roomDocDTO.setLink((String) tgRoomDoc.get("link"));
    roomDocDTO.setMemberCnt((Integer) tgRoomDoc.get("memberCnt"));
    roomDocDTO.setType((String) tgRoomDoc.get("type"));
    roomDocDTO.setLang(StringUtils.join((List<String>) tgRoomDoc.get("lang"), ","));
    var tags0 = (List<String>) tgRoomDoc.get("tags");
    var realTags = tags0.stream().filter(StringUtils::isNotEmpty).toList();
    roomDocDTO.setTags(realTags);
    roomDocDTO.setCategory(StringUtils.join((List<String>) tgRoomDoc.get("category"), ","));
    roomDocDTO.setStatus((String) tgRoomDoc.get("status"));
    roomDocDTO.setHighlighting(new RoomDocHighlightingDTO().setName(name).setJhiDesc(jhiDesc));
    return roomDocDTO;
  }
}

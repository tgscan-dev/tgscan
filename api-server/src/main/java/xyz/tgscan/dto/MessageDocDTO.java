package xyz.tgscan.dto;

import jakarta.persistence.Id;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.annotations.Document;
import xyz.tgscan.enums.IdxConstant;

@Data
@Slf4j
@Accessors(chain = true)
@Document(indexName = IdxConstant.ROOM_IDX, createIndex = false)
public class MessageDocDTO {

  @Id private Long id;

  //  private String title;
  //  private String content;
  private long offset;

  private List<String> tags;
  private String type;

  private Integer chatId;
  private Date sendTime;
  private MessageDocHighlightingDTO highlighting;

  public static MessageDocDTO fromTgMessageDoc(Map tgRoomDoc, String name, String jhiDesc) {
    MessageDocDTO tgRoomDocDTO = new MessageDocDTO();
    tgRoomDocDTO.setId(Long.valueOf(tgRoomDoc.get("id").toString()));
    //    tgRoomDocDTO.setTitle((String) tgRoomDoc.get("title"));
    tgRoomDocDTO.setOffset((Integer) tgRoomDoc.get("offset"));
    tgRoomDocDTO.setType((String) tgRoomDoc.get("type"));
    tgRoomDocDTO.setTags((List<String>) tgRoomDoc.get("tags"));
    tgRoomDocDTO.setChatId((Integer) tgRoomDoc.get("chatId"));
    var create = (Long) tgRoomDoc.get("sendTime");
    tgRoomDocDTO.setSendTime(create == null ? null : new Date(create));

    tgRoomDocDTO.setHighlighting(new MessageDocHighlightingDTO().setTitle(name).setDesc(jhiDesc));
    return tgRoomDocDTO;
  }
}

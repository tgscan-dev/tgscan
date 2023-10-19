package xyz.tgscan.domain;

import jakarta.persistence.*;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Accessors(chain = true)
@Entity
public class Message {
  @Basic
  @Column(name = "offset")
  private long offset;

  @Basic
  @Column(name = "username")
  private String username;

  @Basic
  @Column(name = "sender_id")
  private long senderId;

  @Basic
  @Column(name = "content")
  private String content;

  @Basic
  @Column(name = "send_time")
  private Timestamp sendTime;

  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  @Basic
  @Column(name = "id")
  private Long id;
}

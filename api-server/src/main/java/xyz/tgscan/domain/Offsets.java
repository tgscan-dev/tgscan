package xyz.tgscan.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Accessors(chain = true)
@Entity
@Table(name = "spy_offsets")
public class Offsets {
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  @Column(name = "username")
  private String username;

  @Basic
  @Column(name = "last_offset")
  private long lastOffset;

  @Basic
  @Column(name = "crawl_link")
  private Boolean crawlLink;

  @Basic
  @Column(name = "room_name")
  private String roomName;

  @Basic
  @Column(name = "link")
  private String link;


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Offsets offsets = (Offsets) o;
    return username == offsets.username
        && lastOffset == offsets.lastOffset
        && Objects.equals(crawlLink, offsets.crawlLink)
        && Objects.equals(roomName, offsets.roomName)
        && Objects.equals(link, offsets.link);
  }

  @Override
  public int hashCode() {
    return Objects.hash(username, lastOffset, crawlLink, roomName, link);
  }
}

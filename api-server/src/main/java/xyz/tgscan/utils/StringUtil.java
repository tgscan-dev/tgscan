package xyz.tgscan.utils;

public class StringUtil {

  public static void main(String[] args) {
    var s =
        "telegram中文导航/中文频道/中文搜索 中文（中文包） 中文 中文包 » 中文 中文包 \uE072» 中文 中文包 ■ 中文     \uD83E\uDEF5 中文包";
    var s1 = removeSpecial(s);
    System.out.println(s1);
  }

  public static String removeSpecial(String x) {
    return x.replaceAll(" ", "")
        .replaceAll("【", " ")
        .replaceAll("】", " ")
        .replaceAll("\\[", " ")
        .replaceAll("]", " ")
        .replaceAll(",", " ")
        .replaceAll("\\.", " ")
        .replaceAll("。", " ")
        .replaceAll("#", " ")
        .replaceAll("＃", " ")
        .replaceAll("丨", " ")
        .replaceAll("!", "")
        .replaceAll("《", " ")
        .replaceAll("[^\\p{IsHan}\\p{Alpha}]+", " ")
        .replaceAll("_", " ")
        .replaceAll("\\+", " ")
        .replaceAll("-", " ")
        .replaceAll("》", " ")
        .replaceAll("<", " ")
        .replaceAll(">", " ")
        .replaceAll("？", " ")
        .replaceAll("[\\x{0001f300}-\\x{0001f64f}]|[\\x{0001f680}-\\x{0001f6ff}]", " ")
        .replaceAll("\"", " ")
        .replaceAll("'", " ")
        .replaceAll("\\?", " ")
        .replaceAll("\\s+", " ")
        .trim();
  }
}

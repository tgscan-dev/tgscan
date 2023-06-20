package xyz.tgscan;

import com.github.houbb.pinyin.constant.enums.PinyinStyleEnum;
import com.github.houbb.pinyin.util.PinyinHelper;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

public class ParseTest {
  private static ArrayList<String> parseTags(String input) {
    Pattern pattern = Pattern.compile("#\\S+");
    Matcher matcher = pattern.matcher(input);
    var strings = new ArrayList<String>();
    while (matcher.find()) {
      String tag = matcher.group();
      strings.add(tag);
    }
    return strings;
  }

  @Test
  public void testParse() {
    String pinyin2 = PinyinHelper.toPinyin("分词也很重要", PinyinStyleEnum.FIRST_LETTER,"");
    System.out.println(pinyin2);
    String pinyin3 = PinyinHelper.toPinyin("分词也很重要", PinyinStyleEnum.NORMAL,"");
    System.out.println(pinyin3);
  }
}

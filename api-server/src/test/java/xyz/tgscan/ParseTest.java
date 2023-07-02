package xyz.tgscan;

import com.github.houbb.pinyin.constant.enums.PinyinStyleEnum;
import com.github.houbb.pinyin.util.PinyinHelper;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

public class ParseTest {
  @Test
  void t1() {
    String text = "我的世界";

    // 使用 StandardTokenizer 进行分词
    var segment = HanLP.newSegment();
    Segment standardSegment = segment.enableAllNamedEntityRecognize(true);
    System.out.println("StandardTokenizer:");
    for (Term term : standardSegment.seg(text)) {
      System.out.print(term.word + " ");
    }

    // 使用 NLPTokenizer 进行分词
    Segment nlpSegment = HanLP.newSegment().enableIndexMode(true).enableCustomDictionary(false);
    System.out.println("\nNLPTokenizer:");
    for (Term term : nlpSegment.seg(text)) {
      System.out.print(term.word + " ");
    }
  }

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

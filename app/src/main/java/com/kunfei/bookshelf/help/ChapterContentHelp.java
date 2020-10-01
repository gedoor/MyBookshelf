package com.kunfei.bookshelf.help;

import android.text.TextUtils;

import com.kunfei.bookshelf.bean.ReplaceRuleBean;
import com.kunfei.bookshelf.model.ReplaceRuleManager;
import com.luhuiguo.chinese.ChineseUtils;

public class ChapterContentHelp {
    private static ChapterContentHelp instance;

    public static synchronized ChapterContentHelp getInstance() {
        if (instance == null)
            instance = new ChapterContentHelp();
        return instance;
    }

    /**
     * 转繁体
     */
    private String toTraditional(String content) {
        int convertCTS = ReadBookControl.getInstance().getTextConvert();
        switch (convertCTS) {
            case 0:
                break;
            case 1:
                content = ChineseUtils.toSimplified(content);
                break;
            case 2:
                content = ChineseUtils.toTraditional(content);
                break;
        }
        return content;
    }

    /**
     * 替换净化
     */
    public String replaceContent(String bookName, String bookTag, String content, Boolean replaceEnable) {
        if (!replaceEnable) return toTraditional(content);
        if (ReplaceRuleManager.getEnabled().size() == 0) return toTraditional(content);
        //替换
        for (ReplaceRuleBean replaceRule : ReplaceRuleManager.getEnabled()) {
            if (isUseTo(replaceRule.getUseTo(), bookTag, bookName)) {
                try {
                    content = content.replaceAll(replaceRule.getFixedRegex(), replaceRule.getReplacement());
                } catch (Exception ignored) {
                }
            }
        }
        return toTraditional(content);
    }

    /**
     * 轻小说分段模式，强制按照短句子分段渲染
     */
    public static String LightNovelParagraph(String content,String chapterName) {
        if (ReadBookControl.getInstance().getLightNovelParagraph()) {
            String[] p = content.split("\n(\\s*)");
//          章节的文本格式为章节标题-空行-首段，所以处理段落时需要掠过第一行文本
            StringBuffer buffer = new StringBuffer();
            if(!chapterName.trim().equals(p[0]))
                buffer.append(p[0]);

//            如果存在分段错误，需要把段落重新黏合
            for (int i = 1; i < p.length; i++) {
                if (isFullSentences(p[i - 1]))
                    buffer.append("\n");
                buffer.append(p[i].replaceAll("\\s+([\"”“])\\s*","$1"));
            }
            p = buffer.toString().split("\n");

            buffer = new StringBuffer();

            for (String s : p) {
                buffer.append("\n");
                buffer.append(seekQuotation(s));
            }

            content=chapterName
                    +SPACE_BEFORE_PARAGRAPH
                    +buffer.toString()
                    .replaceFirst("^\\s+","")
//                    强制修正相邻两个引号的问题。由于此规则会掩盖算法的缺陷，故调试时需要先注释掉
                    .replaceAll("\\s*[“”]{2,3}\\s*","”\n“")
                    .replaceAll("\n”","“")
                    .replaceAll("\n(\\s*)", SPACE_BEFORE_PARAGRAPH);
        }
        return content;
    }

    /* 搜寻引号并进行分段。
    参照百科词条[引号#应用示例](https://baike.baidu.com/item/%E5%BC%95%E5%8F%B7/998963?#5)对引号内容进行矫正并分句。
    一、完整引用说话内容，在反引号内侧有断句标点。例如：
            1) 丫姑折断几枝扔下来，边叫我的小名儿边说：“先喂饱你！”
            2）“哎呀，真是美极了！”皇帝说，“我十分满意！”
            3）“怕什么！海的美就在这里！”我说道。
    二、部分引用，在反引号外侧有断句标点：
            4）适当地改善自己的生活，岂但“你管得着吗”，而且是顺乎天理，合乎人情的。
            5）现代画家徐悲鸿笔下的马，正如有的评论家所说的那样，“形神兼备，充满生机”。
            6）唐朝的张嘉贞说它“制造奇特，人不知其所为”。
    三、一段接着一段地直接引用时，中间段落只在段首用起引号，该段段尾却不用引回号。但是正统文学不在考虑范围内。
    四、引号里面又要用引号时，外面一层用双引号，里面一层用单引号。暂时不需要考虑
    五、反语和强调，周围没有断句符号。
*/
    private static String seekQuotation(String s) {
        if (s.length() < 2)
            return "";
//        标记引号是否已经关闭（成对）
        boolean closed = true;
//        标记是否是 xxxx说：“yyyy” ，处于yyy的区间。当此处引号关闭时，需要插入换行。
        boolean saying = false;
        int sentens_counter = (int) (Math.random() * 4 + 1);
        StringBuffer buffer = new StringBuffer(s.charAt(0));
        for (int i = 1; i < s.length(); i++) {
            char c = s.charAt(i);

            if (MARK_QUOTATION.indexOf(c) != -1) {
                if (buffer.length() > 1) {
                    //          先使用前1字符对引号状态进行矫正，再写入新的引号
                    char b = buffer.charAt(buffer.length() - 1);
                    if (MARK_SENTENCES_MID.indexOf(b) != -1) {
                        // 匹配到了引号.此时如果前一个标点为句中标点，则不进行分段。
                        closed = true;
                        if (b == ':') {
                            buffer.setCharAt(buffer.length() - 1, '：');
                        } else if (b == ',') {
                            buffer.setCharAt(buffer.length() - 1, '，');
                        }
                    } else if (MARK_QUOTATION.indexOf(b) != -1) {
                        closed = true;
                        if (buffer.charAt(buffer.length() - 2) == '\n') {
                            buffer.setCharAt(buffer.length() - 2, '”');
                            buffer.deleteCharAt(buffer.length() - 1);
//                            buffer.setCharAt(buffer.length() - 1, '\n');
                        } else {
                            buffer.setCharAt(buffer.length() - 1, '”');
                        }
                    }
                }

                if (closed) {
                    closed = false;
                    sentens_counter = (int) (Math.random() * 4 + 4);
                    if (MARK_SENTENCES_MID.indexOf(s.charAt(i - 1)) == -1) {
                        buffer.append("\n");
                    }
                    buffer.append("“");
                } else {
                    if (buffer.charAt(buffer.length() - 1) == '\n' && (c == '“' || c == '"')) {
                        buffer.deleteCharAt(buffer.length() - 1);
                        buffer.append("”\n“");
                        sentens_counter = (int) (Math.random() * 4 + 4);
                    } else{
                        closed = true;
                        if(saying){
                            saying=false;
                            buffer.append("”\n");
                        }else
                            buffer.append("”");
                        sentens_counter = (int) (Math.random() * 4 + 1);
                    }

                }
            } else {
                if (buffer.length() > 1  && (MARK_SENTENCES_MID.indexOf(c) != -1 ||MARK_SENTENCES.indexOf(c) != -1)) {
                    //          先使用前1字符对引号状态进行矫正，再写入新的引号
                    char b = buffer.charAt(buffer.length() - 1);
                    if (MARK_SENTENCES_SAY.indexOf(b) != -1) {
                        // 如果内容为 /“xxxx”xxx说。，：/，那么引号需要强制关闭。
                        closed = true;
                        if(MARK_SENTENCES_MID.indexOf(c) != -1 )
                            saying=true;
                        int index_quotation = buffer.lastIndexOf("“");
                        int index_nextline = buffer.lastIndexOf("\n");
                        if (index_nextline < index_quotation) {
                            buffer.setCharAt(index_quotation, '”');
                            if (index_quotation - index_nextline == 1)
                                buffer.deleteCharAt(index_nextline);

                            int index_q=buffer.lastIndexOf("”",index_quotation-1);
                            int index_n=buffer.lastIndexOf("\n",index_nextline-1);
                            int index_p=buffer.lastIndexOf("“",index_quotation-1);
                            if (index_n < index_q &index_p<index_q) {
                                buffer.setCharAt(index_q, '“');
                                if(MARK_SENTENCES.indexOf(c) != -1 && index_q-index_n>1){
//                             在调整引号之后，需要在第一个引号前插入换行。例： /xxxx。”yyyy“zz说。/
                                    buffer.insert(index_q,'\n');
                                }
                            }
                        }

                    } else if (MARK_QUOTATION.indexOf(b) != -1 && c == ' ') {
//                            引号前后无需空格
                        continue;
                    }
                }

                if (MARK_SENTENCES.indexOf(c) != -1) {

                    sentens_counter--;
                    if (sentens_counter < 1) {
//                            如果内容在引号外，随机几个句子产生一个段落。考虑到引号缺失造成的异常，即使在引号内的内容，也会被随机分段
                        buffer.append(c);
                        if (closed)
                            sentens_counter = (int) (Math.random() * 4 + 1);
                        else
                            sentens_counter = (int) (Math.random() * 4 + 4);
                        buffer.append("\n");
                    } else {
                        buffer.append(c);
                    }
                } /*else  if (c == ' ') {
//                  断句后也无需空格
                    continue;
                }*/else
                    buffer.append(c);
            }
        }
        return buffer.toString();
    }

    //  段落换行符
    private static String SPACE_BEFORE_PARAGRAPH = "\n    ";
    //  句子末位的标点
    private static String MARK_SENTENCES = "？。！?!~”\"";
    //  句中标点，由于某些网站常把“，”写为"."，故英文句点按照句中标点判断
    private static String MARK_SENTENCES_MID = ".，、：,/:”\"";
    private static String MARK_SENTENCES_F = "啊嘛吧吗噢哦了呢呐";
    private static String MARK_SENTENCES_SAY = "问说喊唱叫骂道着答";
    //  引号
    private static String MARK_QUOTATION = "\"“”";

    private static boolean isFullSentences(String s) {
        if (s.length() < 2)
            return false;
        char c = s.charAt(s.length() - 1);
        return MARK_SENTENCES.indexOf(c) != -1;
    }

    private boolean isUseTo(String useTo, String bookTag, String bookName) {
        return TextUtils.isEmpty(useTo)
                || useTo.contains(bookTag)
                || useTo.contains(bookName);
    }

}

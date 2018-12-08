# 书源制作教程
- https://www.hostfans.cn/make-course

# 从2.18.120813开始增加了新的规则写法
- 支持XPath语法,以@XPath:开头,语法见 http://www.w3school.com.cn/xpath/index.asp
- XPath语法测试 http://www.bejson.com/testtools/xpath/ 写书源时可用
- 支持JSonPath语法,以@JSon:开头,语法见 https://blog.csdn.net/koflance/article/details/63262484
- JSonPath语法测试 http://jsonpath.herokuapp.com 写书源时可用
- 支持用js处理结果,以@js:开头,结果变量为result 如 "@JSon:$.link@js:"http://chapterup.zhuishushenqi.com/chapter/" + encodeURIComponent(result)"
- **注意** JSon的值如果不是String,用js处理时需自己进行类型转换
- **注意** #替换规则在新语法下无法使用,新的语法用js处理结果,原有的规则不变,见下方

# 书源规则说明
- 书源规则基于HTML标记,如class,id,tag等
- 想要写规则先要打开网页源代码,在里面找到想要获取内容对应的标签,
- Chrome可以在网页上右击点击检查可以方便的查看标签

## 基本写法
- @为分隔符,用来分隔获取规则
- 每段规则可分为3段
- 第一段是类型,如class,id,tag等, children获取所有子标签,不需要第二段和第三段
- 第二段是名称,
- 第三段是位置,class,tag会获取到多个,所以要加位置,id类型不要加
- 如不加位置会获取所有
- 位置正数从0开始,0是第一个
- 如为负数则是取倒数的值,-1为最倒数第一个,-2为倒数第二个
- !是排除,有些位置不符合需要排除用!,后面的序号用:隔开0是第1个,负数为倒数序号,-1最后一个,-2倒数第2个,依次
- @的最后一段为获取内容,如text,textNodes,href,src,html等
- 如果有不同网页的规则可以用 | 或 & 分隔 或 %
- |会以第一个取到值的为准,
- & 会合并所有规则取到的值,
- % 会依次取数,如三个列表,先取列表1的第一个,再取列表2的第一个,再取列表3的第一个,再取列表1的第2个.......
- 如需要正则替换在最后加上 #正则表达式#替换为
- 例:class.odd.0@tag.a.0@text|tag.dd.0@tag.h1@text#全文阅读
- 例:class.odd.0@tag.a.0@text&tag.dd.0@tag.h1@text#全文阅读

### BookSourceUrl 书源网址
### BookSourceName 书源名称
### BookSourceGroup 书源分组
### RuleSearchUrl 搜索网址
- 例:http://www.gxwztv.com/search.htm?keyword=searchKey&pn=searchPage-1
- ?为get @为post
- searchKey为关键字标识,运行时会替换为搜索关键字,
- searchPage,searchPage-1为搜索页数,从0开始的用searchPage-1,
- page规则还可以写成{index（第一页）, indexSecond（第二页）, indexThird（第三页）, index-searchPage+1 或 index-searchPage-1 或 index-searchPage}
- 要添加转码编码在最后加 |char=gbk
- |char=escape 会模拟js escape方法进行编码

#### ruleFindUrl 发现规则
- 发现规则分为两段,名称和url用::分开,如
- 起点风云榜::https://www.qidian.com/rank/yuepiao?page=searchPage
- url规则和搜索规则一样,多个规则用&&或换行分开,如
- 起点风云榜::https://www.qidian.com/rank/yuepiao?page=searchPage&&原创风云榜::https://www.qidian.com/rank/yuepiao?style=1&page=searchPage
- 也可以每行写一个,域名可以省略,如省略会调用书源域名

起点风云榜::/rank/yuepiao?page=searchPage

原创风云榜::/rank/yuepiao?style=1&page=searchPage

#### RuleSearchList 搜索列表
- 例:class.list-group-item!0:%

#### RuleSearchAuthor 搜索里的作者
- 例:class.col-xs-2.0@text

#### RuleSearchKind 搜索里的类型
- 例:class.col-xs-1.0@text

#### RuleSearchLastChapter 搜索里的最新章节
- 例:class.col-xs-4.0@tag.a.0@text

#### RuleSearchName 获取搜索里的书名
- 例:class.col-xs-3.0@tag.a.0@text

#### RuleSearchNoteUrl 搜索里的书链接
- 例:class.col-xs-3.0@tag.a.0@href

#### RuleSearchCoverUrl 搜索里的书封面
- 例:tag.img.0@src

#### RuleBookUrlPattern 书籍页面里的URL正则
- 例:https?://www.piaotian.com/bookinfo/.*"

#### RuleBookName 书籍页面里的书名称
- 例:class.active.0@text

#### RuleBookAuthor 书籍页面里的作者
- 例:class.col-xs-12.0@tag.small.0@text

#### RuleIntroduce 书籍页面里的简介
- 例:class.panel panel-default mt20.0@id.shot@text

#### RuleCoverUrl 书籍页面里的封面
- 例:class.panel-body.0@class.img-thumbnail.0@src

#### RuleBookKind 书籍页面里的分类
- 例:id.centerm@tag.td.3@text#.*：

#### RuleBookLastChapter 书籍页面里的最新章节
- 例class.grid.0@tag.td.0@tag.a@text

#### RuleChapterUrl 书籍页面里的目录地址
- 如果目录地址和书籍地址一样则留空
- 例:class.list-group-item tac.0@tag.a.0@href

#### RuleChapterUrlNext 目录页面里的下一页目录地址
- 如果没有下一页则留空
- 例:class.page.0@text.下一页.0@href
**也可以获取所有目录页url的列表,会依次获取**
- 例:class.chapter.0@tag.a@href  (class.chapter.0)里包含第1页第2页第三页等

#### RuleChapterList 目录页面的目录列表
- 前面加 - 号倒序排列
- 例:id.chapters-list@tag.a
- 例:-id.chapters-list@tag.a

#### RuleChapterName 目录列表的章节名称
- 例:text

#### RuleContentUrl 目录列表的章节链接
- 例:href

#### RuleBookContent 章节内容
- 例:id.txtContent@textNodes
- 如需获取AJAX动态内容前面加$
- 例:$id.txtContent@textNodes

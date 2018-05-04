# 书源规则说明
#### 书源规则基于HTML标记,如class,id,tag等
想要写规则先要打开网页原代码,在里面找到想要获取内容对应的标签,
Chrome可以在网页上右击点击检查可以方便的查看标签
## 基本写法
#### @为分隔符,用来分隔获取规则
#### 每段规则可分为3段
第一段是类型,如class,id,tag等

第二段是名称,

第三段是位置,class,tag会获取到多个,所以要加位置,id类型不要加

如不加位置会获取所有

##### !是排除,有些位置不符合需要排除用!,后面的序号用:隔开,%为最后一个
@的最后一段为获取内容,如text,textNodes,href,src等
##### 如果有不同网页的规则可以用 | 分隔,多个规则会以第一个取到值的为准
##### 如需要正则替换在最后加上 #正则表达式
##### 例:
class.odd.0@tag.a.0@text|tag.dd.0@tag.h1@text#全文阅读

### BookSourceUrl 书源网址
### BookSourceName 书源名称
### BookSourceGroup 书源分组
### RuleSearchUrl 搜索网址
例:http://www.gxwztv.com/search.htm?keyword=searchKey&pn=searchPage-1

searchKey为关键字标识,运行时会替换为搜索关键字,

searchPage,searchPage-1为搜索页数,从0开始的用searchPage-1,

page规则还可以写成{index,index2,index3}

要添加转码编码在最后加 |char=gbk

#### ruleFindUrl 发现规则
发现规则分为两段,名称和url用::分开,如

起点风云榜::https://www.qidian.com/rank/yuepiao?page=searchPage

url规则和搜索规则一样,多个规则用&&分开,如

起点风云榜::https://www.qidian.com/rank/yuepiao?page=searchPage&&原创风云榜::https://www.qidian.com/rank/yuepiao?style=1&page=searchPage

#### RuleSearchList 搜索列表
例:class.list-group-item!0:%
#### RuleSearchAuthor 搜索里的作者
例:class.col-xs-2.0@text
#### RuleSearchKind 搜索里的类型
例:class.col-xs-1.0@text
#### RuleSearchLastChapter 搜索里的最新章节
例:class.col-xs-4.0@tag.a.0@text
#### RuleSearchName 获取搜索里的书名
例:class.col-xs-3.0@tag.a.0@text
#### RuleSearchNoteUrl 搜索里的书链接
例:class.col-xs-3.0@tag.a.0@href
#### RuleSearchCoverUrl 搜索里的书封面
例:tag.img.0@src
#### RuleBookName 书籍页面里的书名称
例:class.active.0@text
#### RuleBookAuthor 书籍页面里的作者
例:class.col-xs-12.0@tag.small.0@text
#### RuleIntroduce 书籍页面里的简介
例:class.panel panel-default mt20.0@id.shot@text
#### RuleCoverUrl 书籍页面里的封面
例:class.panel-body.0@class.img-thumbnail.0@src
#### RuleChapterUrl 书籍页面里的目录地址
例:class.list-group-item tac.0@tag.a.0@href
#### RuleChapterList 目录页面的目录列表,前面加 - 号倒序排列
例:id.chapters-list@tag.a
#### RuleChapterName 目录列表的章节名称
例:text
#### RuleContentUrl 目录列表的章节链接
例:href
#### RuleBookContent 章节内容
例:id.txtContent@textNodes
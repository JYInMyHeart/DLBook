# 功能说明
用于搜索小说，并多线程下载小说。如果配置了mysql数据库（没有数据库不影响下载小说功能本身），还能够将下载的小说按章节缓存到数据库中。 
下载策略为：  
1. 在指定的网站（可以通过实现一个抽象方法来动态添加网站，方法见后文）搜索小说，返回结果列表；
2. 搜索数据库，如果在数据库中能找到相关数据，则从数据库中读取数据；
3. 从网络中读取目录，根据目录章节数与数据库中取得的章节数对比。如果章节数少于数据库中的数据，则不缓存网络章节。如果多于数据库章节数，则缓存多余的部分；
4. 将新增的章节填入数据库（如果有的话），将数据库获取到的数据和网络中获取到的数据存入txt中。  
PS：DLBookLog为运行日志，如果出现软件运行结果与预测不符合，可以查看日志。

# 配置文件说明
相关配置文件说明如下：
<table>
	<th>配置</th>
	<th>说明</th>
	<tr>
		<td>width</td>
		<td>软件宽度</td>
	</tr>
	<tr>
		<td>height</td>
		<td>软件高度</td>
	</tr>
	<tr>
		<td>username</td>
		<td>数据库帐号</td>
	</tr>
	<tr>
		<td>password</td>
		<td>数据库密码</td>
	</tr>
	<tr>
		<td>database</td>
		<td>数据库名</td>
	</tr>
	<tr>
		<td>server_ip</td>
		<td>数据库ip，本机默认127.0.0.1</td>
	</tr>
	<tr>
		<td>port</td>
		<td>数据库端口，默认3306</td>
	</tr>
	<tr>
		<td>database_state</td>
		<td>0:需要重新配置数据库 <br>1:不需要重新配置数据库</td>
	</tr>
	<tr>
		<td>search_switch</td>
		<td>控制搜索范围，请在UI界面中设置</td>
	</tr>
</table>


# 添加网站的方法
## 实现DLBook中的抽象方法
总共考虑了2种下载小说的方式：  
1、能够获取所有目录信息，则使用多线程同时对多个目录下载  
2、无法获取目录信息，则需要根据起始地址依次逐章节下载  
如果条件允许，推荐第一种，速度更快。两种的区别在下面的函数中会有介绍。

```
//根据搜索关键字返回一个搜索结果列表
protected abstract ArrayList<BookBasicInfo> getBookInfoByKey(String key);

//根据小说的网址返回小说的目录信息
能够获取全部目录则直接依次填入全部章节地址，每个地址都为String类型，填入ArrayList<String>中返回
无法获取全部目录则需要按顺序依次填入3项内容，总章节数（获取不到就填max），起始链接地址，结束链接地址（可以为null，但必须填入）
protected abstract ArrayList<String> getCatalog(String Url);

//根据小说章节地址返回小说的章节内容
如果无法获取全部目录，需要在返回的Chapter中填入nexturl的内容，即填入下一个章节的链接
protected abstract Chapter getChapters(String Url);
```
注意：  
1. getBookInfoByKey返回的BookBasicInfo中的BookUrl将用于getCatalog的输入，getCatalog返回的Url用于getChapters的输入；
2. 尽量对getChapters中的网页内容做前期处理(比如一些广告什么的)，这会使得输出的格式更加合乎阅读要求。

## 在config类中增加以上新增的类路径  
```
websites.put("website.DL_79xs", new websiteinfo(8, "79小说"));
websites.put("website.DL_biquge", new websiteinfo(8, "笔趣阁"));
websites.put("website.DL_bookbao8", new websiteinfo(3, "书包网"));
websites.put("website.DL_shushu8", new websiteinfo(8, "书书吧"));
websites.put("website.DL_hunhun520", new websiteinfo(8, "混混小说"));
```
类似上面那样增加类路径和对该网站下载时使用的多线程数（不得超过16线程，不能低于1线程，否则会被强制为8线程）。  
如果一些网站下载的时候出现一大片因为连接超时导致的下载失败，可以尝试降低线程数。  
PS:使用System.out.println()将直接输出到DLBookLog运行日志中。 

## 可能用到的工具函数
```
//说明，以下这些方法并不强制需要实现或调用，而是公用方法来增加爬取效率

//根据网址和编码集获取网页内容，get方式获取
protected String getHtmlInfo(String Urladdress, String charset);

//根据网址和编码集获取网页内容，post方式获取,要求输入网址，表单集，填写表单的字符编码和最终获取网页的字符编码
protected String postHtmlInfo(String Urladdress, LinkedHashMap<String,String> values, String inputcharset, String outputcharset)

/*一些网站返回的搜索结果中只包含小说的欢迎页面地址，需要进一步进入这些地址才能怕取到我们想要的内容。
这两个方法用于多线程爬取这些页面，加快搜索速度*/
//需要在子类中覆写，根据传入的网页内容返回书籍信息
protected BookBasicInfo getbookinfoByhtmlinfo(String htmlinfo);
//直接调用即可，传入的参数bookurls为待爬取的url集，bookinfos中会自动填入爬取的结果，charset设置编码集
protected void getbookinfos(ArrayList<String> bookurls, ArrayList<BookBasicInfo> bookinfos,String charset);
```
# 配置mysql数据库

如果要实现将数据入数据库的功能，要对数据库做一些配置：  
1.下载并安装mysql数据库，启动mysql数据库 ，将mysql数据库字符集设置为utf-8编码，确保mysql中包含一个名为mysql的数据库（为mysql自带默认数据库）。

```
友情提示，要修改默认字符串，需要在C:\Program Files\MySQL\MySQL Server xx内的my.ini中修改，而不是网上很多教程中的C:\ProgramData\MySQL\MySQL Server xx内修改; 
```

2.在config.properity配置mysql数据库帐号，密码，数据库名，服务器IP，服务器端口，并确保database_state设置为0。  
PS：如果配置的为远程mysql服务器，请确保远程服务器以下设置OK  
1）防火墙中允许mysqld进程或者关闭防火墙  
2）相关用户在user表中的host为%。 
 
```
以root用户为例，在可以登录mysql的机器上查询
select host from mysql.user where user="root"
如果上述查询结果为localhost，执行以下语句修改
update mysql.user set host="%" where user="root"
然后重启mysql服务
```

3.如果配置后发生如下类似的错误The server time zone value '�й���׼ʱ��' is unrecognized or represents more than one time zone，可以按如下设置

```
show variables like "%time_zone%";
如果结果为SYSTEM，执行如下命令
set global time_zone='+8:00';（和地区有关，总之和UTC时间对齐）
```
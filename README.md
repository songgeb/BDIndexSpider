# BDIndexSpider

百度指数抓取工具

提供关键词、起始和结束日期，软件能够抓取多个关键词每天的百度指数

### Feature

- 支持按照**省份、城市**查询
- 实测兼容**Mac**、**Windows**系统，理论上也支持**Linux**，但没测过

### Requirement

- 源码基于Java1.8
- 使用Maven管理

### 使用说明

- 运行前，需要在左上角初始化中**配置账户密码**和导入要抓取的**关键词**
- 需要安装**Chrome**浏览器，版本在**64-66**之间
- 源码中的`a.txt`为输入文件格式要求，可以**输入多行**
- 如果不想运行源码，可以到**executable**目录下直接下载可执行`jar`文件
- 关于抓取频率限制问题可以看[这里](https://github.com/songgeb/BDIndexSpider/issues/2)

### 问题反馈

关于工具的问题反馈和建议，推荐大家在github上开[issue](https://github.com/songgeb/BDIndexSpider/issues)进行详细说明

### ChangeLog

- 2018年05月11日
	适配新的百度指数页面
- 2018年04月22日
	加入按照地区查询功能
- 2018年04月13日
	不再使用tesseract进行ocr，自己写了个ocr实现

- 2018年04月08日
	提高精确模式抓取效率

- 2018年04月05日
	添加可执行jar文件，添加用户可配置账户密码功能

- 2018年04月01日
	目前已经修复了**精确模式**，可以正常运行

- 2018年03月27日
	本代码写于2016年，首次开源，目前由于Webdriver驱动的问题，无法直接运行。后面会抽时间修复一下

### 打赏

<div align=center>
<div style="display:inline-block;">
<img src="https://songgeb.github.io/images/wechat.jpg">
<p>微信</p>
</div>

<div style="display:inline-block;">
<img src="https://songgeb.github.io/images/alipay.jpg">
    <p>支付宝</p>
</div>
</div>

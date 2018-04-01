# BDIndexSpider
百度指数抓取工具

### 持续更新

|时间|更新|备注|
|:-:|:-:|:-:|
|2018年04月01日|目前已经修复了**精确模式**，可以正常运行||
|2018年03月27日|本代码写于2016年，首次开源，目前由于Webdriver驱动的问题，无法直接运行。后面会抽时间修复一下||

### Requirement

- 基于Java1.8, Maven管理
- 运行前，需要在**BDIndexAction.java**文件配置百度账号、密码
- 本程序用到了图像识别组件**tesseract**，需要提前在机器上安装，[安装指南](https://github.com/tesseract-ocr/tesseract)
- 需要安装**Chrome**浏览器

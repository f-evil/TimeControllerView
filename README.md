## TimeControllerView项目介绍

### 0. 系列文章
- [1. 自定义View入门学习-OneChart](http://www.nooocat.com/index.php/2019/11/04/283/)
- [2. 自定义View入门学习-WheelView](http://www.nooocat.com/index.php/2019/11/15/296/)
- [3. 自定义View入门学习-TimeControllerlView](http://www.nooocat.com/index.php/2019/11/15/296/)

### 1. 原始效果图
![原始效果图](https://s2.ax1x.com/2020/01/13/l7Izz4.png)

### 2. Demo效果图
![Demo效果图](https://s2.ax1x.com/2020/01/13/l7opQJ.png)

### 3. 解决问题
- 刻度尺对应控制器

### 4. 由来
- 组内学习自定义View的基础Demo

### 5. 包含内容
- 基础 Paint 的使用
- 基础 Canvas 的 drawText，drawLine ，drawPath 等 的使用
- 基础 Canvas 的 translate，sava，restore 的使用
- 基础 Path 的使用
- 等

### 6. 使用

#### 6.1 属性介绍
- 无

#### 6.2 使用办法

``` xml
<com.easy.timecontrollerview.TimeControllerView
            android:id="@+id/stv_chart"
            android:layout_width="match_parent"
            android:layout_height="400dp"/>
```
``` java
ScaleTableView stv_chart = findViewById(R.id.stv_chart);
stv_chart.setMinMaxValue(0, 11);
```


### 7. 项目解析
- 暂无

### 8. 博客地址
[OneChart](http://www.nooocat.com/index.php/2019/11/04/283/)
# 打车模拟

----------

一个简单的打车模拟软件，使用百度地图SDK作定位处理，用户约车，司机接单的过程。

## 环境

服务器源码：IntelliJ IDEA  
客户端源码：Android Studio 2.2.3

## 源码目录

服务器源码：TakeTaxiServer  
用户端源码：TakeTaxiUser  
司机端源码：TakeTaxiDriver

程序大致说明：  
(1).服务器使用Mina框架，绑定端口运行后，在Handler中处理客户端连接及数据的交互。数据格式统一为JSON。  
(2).用户端和司机端连接成功后，发送用户名和手机号到服务器。  
(3).用户端的打车信息、取消信息，司机端的接单信息，在服务器里处理发送。  
(4).程序只是一个简单的模拟过程，对用户、订单更细节的处理（比如用户的存储、订单的完成等）未作实现。


## 软件截图
**用户端：**
![](http://i.imgur.com/t9KXh62.jpg)
![](http://i.imgur.com/Bw7mEdP.png)

**司机端：**
![](http://i.imgur.com/qPcEaqF.png)

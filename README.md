# 前言
虽说RPC的原理实际不难，现在成熟度的框架也很多。但是使用现成的框架可能没有去关注里面的原理，自己便想自己实现一个简单的RPC框架。虽然RCP的原理不是很难
但是自己在实现的过程中自己也遇到了很多问题。过这个简易的轮子，可以学到RPC的底层原理和原理以及各种Java编码实践的运用。

# 简介
ysera-rpc是基于Netty+Zookeeper实现的RPC框架

### 简单说一下设计一个最基本的 RPC 框架的思路：
1.**注册中心** ：注册中心首先是要有的，推荐使用 Zookeeper。注册中心负责服务地址的注册与查找，相当于目录服务。服务端启动的时候将服务名称及其对应的
地址(ip+port)注册到注册中心，服务消费端根据服务名称找到对应的服务地址。有了服务地址之后，服务消费端就可以通过网络请求服务端了。    
2.**网络传输** ：既然要调用远程的方法就要发请求，请求中至少要包含你调用的类名、方法名以及相关参数吧！推荐基于NIO的Netty框架。    
3.**序列化** ：既然涉及到网络传输就一定涉及到序列化，你不可能直接使用 JDK 自带的序列化吧！JDK 自带的序列化效率低并且有安全漏洞。所以，你还要考虑使用哪种序列化协议，比较常用的有hession2、kyro、protostuff。        
4.**动态代理** ： 另外，动态代理也是需要的。因为 RPC 的主要目的就是让我们调用远程方法像调用本地方法一样简单，使用动态代理可以屏蔽远程方法调用的细节比如网络传输。
也就是说当你调用远程方法的时候，实际会通过代理对象来传输网络请求，不然的话，怎么可能直接就调用到远程方法呢？    
5.**负载均衡** ：负载均衡也是需要的。为啥？举个例子我们的系统中的某个服务的访问量特别大，我们将这个服务部署在了多台服务器上，当客户端发起请求的时候，    
多台服务器都可以处理这个请求。那么，如何正确选择处理该请求的服务器就很关键。假如，你就要一台服务器来处理该服务的请求，那该服务部署在多台服务器的意义
就不复存在了。负载均衡就是为了避免单个服务器响应同一请求，容易造成服务器宕机、崩溃等问题，我们从负载均衡的这四个字就能明显感受到它的意义。    
......
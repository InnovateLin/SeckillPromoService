### 功能效果展示

#### 注册

<img src="https://github.com/InnovateLin/SeckillPromoService/blob/master/picture/register.png" style="width:300px;height:auto" align="middle">


#### 验证码验证页面
<img src="https://github.com/InnovateLin/SeckillPromoService/blob/master/picture/verification.png" style="width:300px;height:auto" align="middle">



#### 用户登陆界面

<img src="https://github.com/InnovateLin/SeckillPromoService/blob/master/picture/login.png" style="width:300px;height:auto" align="middle">



#### 下单页面

<img src="https://github.com/InnovateLin/SeckillPromoService/blob/master/picture/createOrder.png" style="width:300px;height:auto" align="middle">



### 项目结构图

分为Data Object/Model/View Object

DAO层用于实现数据库的映射

Model层用于集合DAO层的数据

VO层用于前端展示给用户看的信息

<img src="https://github.com/InnovateLin/SeckillPromoService/blob/master/picture/system structure.png" >



#### 基本模块实现思路

##### 非业务模块实现思路

1.前端不能显示如密码类的敏感信息，敏感信息需要拆分出来单独做一张表

一般将用户的敏感信息从用户表从分离出来，比如密码单独作为一张表。这样，就需要两个DAO来对应同一个用户Model，分别是UserDAO和UserPasswordDAO，这就是Data Object，从数据库直接映射出来的Object。

在Service层操作的时候，又需要将两个Data Object对象合在一起操作，所以就把两个Data Object封装成一个Model对象，包含了用户的所有信息。

在Controller层，不需要将UserModel的“密码”、“注册信息”等无关信息暴露给前端。这就需要Model转换View Object将不需要暴露给用户的敏感字段从Model中剔除掉。



2.定义一个通用的返回对象

一般要使用一个统一的类，来返回后端处理的对象，作为HTTP的Response。不然默认给前端是对象的toString()方法，不易阅读，而且，不能包含是处理成功还是失败的信息。这个类就是response.CommonReturnType。



3.规范化Spring的报错信息

当程序内部出错后，Spring Boot会显示默认的出错页面，这些页面不能很好的告知用户错误是什么。需要将错误封装起来，通过自定义的CommonReturnType返回给用户，告诉用户哪里出错了，比如“密码输入错误”、“服务器内部错误”等等。

这些内容，封装到了error包下面的三个类里面。一个是CommonError接口，一个是枚举异常类EmBusinessError，一个是异常处理类BusinessException。

CommonError接口提供三个方法，一个获得错误码的方法getErrCode()，一个获得错误信息的方法getErrMsg()，一个设置错误信息的方法setErrMsg(String errMsg)。

错误类型枚举类EmBusinessError含有两个属性，一个是错误码errCode一个是错误信息errMsg。通过CommonError接口的方法，获得相应错误码和错误信息。

BusinessException继承Exception类实现CommonError接口，用于在程序出错时，throw异常。

这样，在程序中可以抛出自定义的异常了。

throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);

在BaseController里面创建异常拦截器处理自定义异常，BusinessException被抛给了Tomcat，而Tomcat不知道如何处理BusinessException。所以，需要一个拦截器，拦截抛出的BusinessException。

在controller.BaseController中新建一个handlerException()方法， 添加@ExceptionHandler和@ResponseStatus注解。这样，抛出的异常，就会先进入这个方法进行处理，如果是BusinessException，那么创建一个Map来封装错误码和错误信息，返回给前端。



处理404，405Not Found问题

定义AOP增强类进行处理

新建一个controller.GlobalExceptionHandler的类，整个类加上@ControllerAdvice接口，表示这是一个AOP增强类。然后根据异常的情况来进行处理，404/405错误会触发NoHandlerFoundException，然后被GlobalExceptionHandler捕获到。

最后在Spring配置文件中添加：

\#处理404/405

spring.mvc.throw-exception-if-no-handler-found=true

spring.resources.add-mappings=false



4.处理跨域问题 前后端分离的问题

由于浏览器的安全机制，JS只能访问与所在页面同一个域（相同协议、域名、端口）的内容， 但是我们这里，需要通过Ajax请求，去请求后端接口并返回数据，这时候就会受到浏览器的安全限制，产生跨域问题（如果只是通过Ajax向后端服务器发送请求而不要求返回数据，是不受跨域限制的）。

所以，前端的HTML页面，在Ajax请求体里面，需要设置contentType:"application/x-www-form-urlencoded"，并添加一个额外的字段xhrFields:{withCredentials: true}。

后端的Controller类需要添加@CrossOrigin(allowCredentials = "true",allowedHeaders = "*")注解。Controller的每一个API的@RequestMapping注解，也要添上consumes = {"application/x-www-form-urlencoded"}字段。



浏览器设置处理

<img src="https://github.com/InnovateLin/SeckillPromoService/blob/master/picture/salafrai cookie.png">



6.校验规则进行优化

使用注解+validator对属性进行维护。本项目使用了org.hibernate.validator包来进行校验。

比如像UserModel类，直接可以对字段添加注解，实现校验规则。

@NotBlank(message = "用户名不能为空")

private String name;

@NotNull(message = "年龄必须填写")

@Min(value = 0,message = "年龄必须大于0")

@Max(value = 120,message = "年龄必须小于120")

private Integer age;

封装校验结果

当然，上面只是定义了校验规则，我们还需要校验结果，所以创建一个validator.ValidationResult类，来封装校验结果。

类包含两个属性 

1.hasErrors 判断校验结果是否有错

2.errorMsgMap 封装校验结果和相关的出错信息

校验器的使用方法

新建一个validator.ValidatorImpl类，实现InitializingBean接口，为了能在这个Bean初始化的时候，初始化其中的javax.validation.Validator对象validator。

自定义一个校验方法validate，这个方法实际上就是调用validator对象的validate(Object obj)方法。该方法会根据校验规则，校验规则按照注解的形式设置，返回一个Set，如果传入的Object出现校验错误，就会把错误加入到Set中。最后遍历这个Set，将错误封装到ValidationResult即可。最后在进行参数校验时使用validate对封装的用户model进行校验



#### 用户注册模块

1.短信发送业务

注册之前，输入手机号，请求后端getOtp接口。接口生成验证码后，发送到用户手机，并且用Map将验证码和手机绑定起来。将Map放到redis服务器的Session里面。



2.用户注册模块

注册请求后端UserController.register接口，先进行短信验证，然后将注册信息封装到UserModel，调用UserServiceImpl.register()，先对注册信息进行入参校验，再将UserModel转成UserDO、UserPasswordDO存入到数据库。UserServiceImpl.register()方法，设计到了数据库写操作，需要加上@Transactional注解，以事务的方式进行处理，保证用户信息的完整性。



3.用户登陆模块

登录请求后端UserController.login接口，前端传过来手机号和密码。调用UserServiceImpl.validateLogin方法，这个方法先通过手机号查询user_info表，看是否存在该用户，返回UserDO对象，再根据UserDO.id去user_password表中查询密码。如果密码匹配，则返回UserModel对象给login方法，最后login方法将UserModel对象存放到Session里面，即完成了登录。



#### 商品业务模块

商品添加业务

请求后端ItemController.create接口，传入商品创建的各种信息，封装到ItemModel对象，调用，ItemServiceImpl.createItem方法，进行入参校验，然后将ItemModel转换成ItemDO和ItemStockDO对象，分别写入数据库。



获取商品业务

请求后端ItemController.get接口，传入一个Id，通过ItemServiceImpl.getItemById先查询出ItemDO对象，再根据这个对象查出ItemStockDO对象，最后两个对象封装成一个ItemModel对象返回。



查询所有商品

请求后端ItemController.list接口，跟上面类似，查询所有商品。基于ORM定义一个listitem方法获取所有的itemDO，使用itemDO的id找到itemStockDO，然后封装成itemModel的list。然后去除掉itemModel里面的敏感信息转换为itemVO的一个List。



#### 交易业务

下单模块

请求后端OrderController.createOrder接口，传入商品IdItemId和下单数量amount。接着在Session中获取用户登录信息，如果用户没有登录，直接抛异常。

在将订单存入库之前，先要调用OrderServiceImpl.createOrder方法，对商品信息、用户信息、下单数量进行校验。此外，还需要校验库存能不能减够。



订单ID的设计

订单ID不能是简单的自增长，而是要符合一定的规则，比如前8位，是年月日；中间6位为自增序列；最后2位为分库分表信息。

前8位比较好实现，使用LocalDateTime。

中间6位自增序列，需要新建一个sequence_info表，里面包含name、current_value、step三个字段。这个表及其对应的DO专门用来产生自增序列，step表示每次值增加的大小。



generatorOrderNo方法需要将序列的更新信息写入到sequence_info表，而且该方法封装在OrderServiceImpl.createOrder方法中。如果createOrder执行失败，会进行回滚，默认情况下，generatorOrderNo也会回滚。而我们希望生成ID的事务不受影响，就算订单创建失败，ID还是继续生成，保证每个ID只会被使用一次，所以generatorOrderNo方法使用了Spring的REQUIRES_NEW事务传播特性。



#### 秒杀业务

1.秒杀DO/Model和VO

PromoModel对象使用status字段，通过从数据库的start_time和end_time字段，与当前系统时间做比较，设置秒杀的状态（开始，结束，进行中）。

对于ItemModel，需要将PromoModel属性添加进去，这样就完成了商品和活动信息的关联。

在ItemServiceImpl.getItemById中，除了要查询商品信息ItemDO、库存信息ItemStockDO外，还需要查询出PromoModel。

对于ItemVO，也是一样的，我们需要把活动的信息（活动进行信息、活动价格等）显示给前端，所以需要在ItemVO里面添加promoStatus、promoPrice，startDate和endDate等属性。



2.普通商品升级为秒杀商品

请求ItemController.list接口，获取所有商品信息。然后通过点击的商品Id，请求ItemController.get接口，查询商品详细信息。

首先根据Id，调用ItemServiceImpl.getItemById查询出商品信息、库存信息、秒杀活动信息，一并封装到ItemModel中。然后再调用上面的convertVOFromModel，将这个ItemModel对象转换成ItemVO对象，包含了秒杀活动的信息，最后返回给前端以供显示。



3.活动商品以什么样价格下单

秒杀活动商品的下单，需要单独处理，以“秒杀价格”入下单库。所以OrderDO也需要添加promoId属性。活动商品的下单会附带itemId、amount，promoID请求OrderController.createOrder接口，进行参数校验。最后，如果promoId不为空，那么订单的价格就以活动价格为准。



#### 使用三级缓存降低数据库压力

**1.多级缓存的使用原理**

内存的速度是磁盘的成百上千倍，高并发下，从磁盘I/O十分影响性能。通过缓存将磁盘中的热点数据暂时存到内存里面，以后查询直接从内存中读取，减少磁盘I/O，提高速度。所谓多级，就是在多个层次设置缓存，一个层次没有就去下一个层次查询。



**2.三级缓存优化查询过程**

ItemController.getItem接口的问题：

每有一个Id，就调用ItemService去数据库查询一次。ItemService会查三张表，分别是商品信息表item表、商品库存stock表和活动信息promo表，三次数据库IO对性能影响较大。

优化：

三级缓存：JVM缓存+Redis缓存+MySQL缓存

JVM本地缓存方案使用Google的Guava Cache方案。

Guava Cache除了线程安全外，还可以控制超时时间，提供淘汰机制。

引用google.guava包后，在service包下新建一个CacheService类用于设置。

在ItemController里面，首先从本地缓存中获取，如果本地缓存没有，就去Redis里面获取，如果Redis也没有，就去数据库查询并存放到Redis里面。如果Redis里面有，将其获取后存到本地缓存里面。



JVM缓存缺点

更新麻烦，容易产生脏缓存。

受到JVM容量的限制。



**3.用Json优化存在Redis里面的Key和value的数据**

存到Redis里面的VALUE是类似/x05/x32的二进制格式，为了方便阅读将格式转换为Json，需要自定义RedisTemplate的序列化格式。

使用方法：

@Bean

public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory){

​    RedisTemplate redisTemplate=new RedisTemplate();

​    redisTemplate.setConnectionFactory(redisConnectionFactory);

​    

​    //首先解决key的序列化格式

​    StringRedisSerializer stringRedisSerializer=new StringRedisSerializer();

​    redisTemplate.setKeySerializer(stringRedisSerializer);

​    

​    //解决value的序列化格式

​    Jackson2JsonRedisSerializer jackson2JsonRedisSerializer=new Jackson2JsonRedisSerializer(Object.class);

​    redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);

​    

​    return redisTemplate;

}



##### 4.用Redis存放Session

基于Cookie传输SessionId

就是把Tomcat生成的SessionId转存到Redis服务器上，从而实现分布式会话。

在之前的项目引入两个jar包，分别是spring-boot-starter-data-redis和spring-session-data-redis，某些情况下，可能还需要引入spring-security-web。

config包下新建一个RedisConfig的类，添加@Component和@EnableRedisHttpSession(maxInactiveIntervalInSeconds=3600)注解让Spring识别并自动配置过期时间。

接着在application.properties里面添加Redis相关连接配置。

spring.redis.host=RedisServerIp

spring.redis.port=6379

spring.redis.database=0

spring.redis.password=0

由于UserModel对象会被存到Redis里面，需要被序列化，所以要对UserModel类实现Serializable接口。

之前的代码就会自动将Session保存到Redis服务器上。

this.httpServletRequest.getSession().setAttribute("IS_LOGIN",true);

this.httpServletRequest.getSession().setAttribute("LOGIN_USER",userModel);

sessionID找到Session Session里面存userModel和对应的登录状态



#### 访问数据库的优化

##### createOrder交易接口存在的问题

在OrderService.createOrder方法里面，首先要去数据库查询商品信息，而在查询商品信息的过程中，又要去查询秒杀活动信息，还要查询用户信息，最后还要对stock库存表进行-1update操作，对order_info订单信息表进行添加insert操作，对item商品信息表进行销量+1update操作。仅仅一个下单，就有6次数据库I/O操作，此外，减库存操作还存在行锁阻塞，所以下单接口并发性能很低。



##### 索引优化

扣减库存的SQL语句：

update stock set stock = stock -#{amount} where item_id = #{itemId} and stock >= #{amount}如果where条件的item_id字段没有索引，那么会锁表，性能很低。

使用alter table stock add UNIQUE INDEX item_id_index(item_id)，为item_id字段添加一个唯一索引，这样在修改的时候，只会锁行。



##### RocketMQ的事务型消息队列优化访问

##### 库存扣减优化 先扣缓存 再通过消息队列扣数据库

采用直接操作数据库的方式，一旦秒杀活动开始，大量的流量涌入扣减库存接口，数据库压力很大。那么可以先在缓存中下单。如果要在缓存中扣减库存，需要解决两个问题，第一个是活动开始前，将数据库的库存信息，同步到缓存中。第二个是下单之后，要将缓存中的库存信息同步到数据库中。这就需要用到异步消息队列。

数据同步到缓存中

PromoService新建一个publishPromo的方法，把数据库的缓存存到Redis里面去。

promo的id和item的id是一致的，通过一致的id进行缓存的存放

这里需要注意的是，当把库存存到Redis的时候，商品可能被下单，这样数据库的库存和Redis的库存就不一致了。解决方法就是活动未开始的时候，商品是下架状态，不能被下单。

Item在进行减库存操作时，先在redis缓存里面减。



事务型消息

事务型消息指先将消息发送到消息队列里面，这条消息处于prepared状态，broker会接受到这条消息，但是不会立刻把这条消息给消费者消费。

处于prepared状态的消息，会执行TransactionListener的executeLocalTransaction方法，根据执行结果，改变事务型消息的状态，让消费端消费或是不消费。

在mq.MqProducer类里面新注入一个TransactionMQProducer类，与DefaultMQProducer类似，也需要设置服务器地址、命名空间等。

新建一个transactionAsyncReduceStock的方法，该方法使用事务型消息进行异步扣减库存。

根据方法执行的情况决定消息的状态。实现在事务型消息中去执行下单操作，下单失败，则消息回滚，不会去数据库扣减库存。下单成功，则消息被消费，扣减数据库库存。



更新下单流程

在OrderController里面直接调用MqProducer.transactionAsyncReduceStock方法，发送一个事务型消息，然后在事务型消息中调用OrderService.createOrder方法，进行下单。实现根据下单是否成功了决定消息是否发送。通过使用事务型消息，把下单操作包装在异步扣减消息里面，让下单操作跟扣减消息同生共死。



处理createorder挂掉的情况

当执行orderService.createOrder后，突然又宕机了，根本没有返回，此时进入prepard状态的消息无法进行后续的处理，这个时候事务型消息就会进入UNKNOWN状态，我们需要处理这个状态。

在匿名类TransactionListener里面，还需要覆写checkLocalTransaction方法，这个方法就是用来处理UNKNOWN状态的。这就需要引入库存流水。记录每一次的交易信息。

使用initStockLog方法记录订单的交易记录 物品ID 买的物品数量 流水ID 这笔订单的交易状态（1表示初始状态，2表示下单扣减库存成功，3表示下单回滚）

用户请求后端OrderController.createOrder接口，我们先使用initLog初始化库存流水的状态，再调用事务型消息去用createOrder方法下单。

createOrder执行完没有宕机，要么执行成功，要么抛出异常。执行成功，那么就说明下单成功了，订单入库了，Redis里的库存扣了，销量增加了，等待着异步扣减库存，所以将事务型消息的状态，从UNKNOWN变为COMMIT，这样消费端就会消费这条消息，异步扣减库存。抛出异常，那么订单入库、Redis库存、销量增加，就会被数据库回滚，此时去异步扣减的消息，就应该“丢弃”，所以发回ROLLBACK，进行回滚。

createOrder执行完宕机了，那么这条消息会是UNKNOWN状态，这个时候就需要在checkLocalTransaction进行处理。如果createOrder执行完毕，此时stockLog.status==2，就说明下单成功，需要去异步扣减库存，所以返回COMMIT。如果status==1，说明下单还未完成，还需要继续执行下单操作，所以返回UNKNOWN。如果status==3，说明下单失败，需要回滚，不需要异步扣减库存，所以返回ROLLBACK。



#### 流量削峰

##### 秒杀令牌

利用秒杀令牌，使校验逻辑和下单逻辑分离。

PromoService添加一个generateSecondKillToken方法，将活动、商品、用户信息校验逻辑封装在里面。OrderController新开一个generateToken接口，以便前端请求，返回令牌。

前端在点击“下单”后，首先会请求generateToken接口，返回秒杀令牌。然后将秒杀令牌promoToken作为参数，只有拿到秒杀令牌后才能去请求后端createOrder接口。为了防止生成过多的令牌影响性能，通过PromoService.publishPromo将库存发布到了Redis上，将令牌总量也发布到Redis上，然后设定令牌总量是库存的5倍。



##### 线程池作为阻塞队列来限流

使用队列泄洪就是让多余的请求排队等待。排队有时候比多线程并发效率更高，多线程毕竟有锁的竞争、上下文的切换，很消耗性能。而排队是无锁的，单线程的，某些情况下效率更高。

在OrderController里面，之前拿到秒杀令牌后，就要开始执行下单的业务了。现在，我们把下单业务封装到一个固定大小的线程池中，设置线程池的最大线程数设置为20，一次只处理固定大小的请求。

在拿到秒杀令牌后，使用线程池来处理下单请求。在出现瞬间涌入较多流量的情况下，得到处理的也就20个，其它全部等待。



#### 限流方案

**令牌桶**

客户端请求接口，必须先从令牌桶中获取令牌，令牌是由一个“定时器”定期填充的。在一个时间内，令牌的数量是有限的。令牌桶存放令牌的数量决定了吞吐量。

使用RateLimiter实现令牌桶

orderCreateRateLimiter = RateLimiter.create(300);	//在桶里面放300个令牌

请求createOrder接口之前，会调用RateLimiter.tryAcquire方法，看当前令牌是否足够，不够直接抛出异常。

CreateOrder方法入口处尝试获取令牌 拿不到令牌就抛异常

if (!orderCreateRateLimiter.tryAcquire())

​     throw new BizException(EmBusinessError.RATELIMIT);



##### TomCat的配置优化

Spring Boot内嵌Tomcat默认的线程设置比较低——默认最大等待队列为100，默认最大可连接数为10000，默认最大工作线程数为200，默认最小工作线程数为10。当请求超过200+100后，会拒绝处理；当连接超过10000后，会拒绝连接。对于最大连接数，一般默认的10000就行了，而其它三个配置，则需要根据需求进行优化。

在application.properties里面进行修改：

server.tomcat.accept-count=1000	//阻塞线程队列的大小

server.tomcat.max-threads=800

server.tomcat.min-spare-threads=100

server.tomcat.max-connections=10000（默认）

这里最大等待队列设为1000，最大工作线程数设为800，最小工作线程数设为100。

等待队列不是越大越好，一是受到内存的限制，二是大量的出队入队操作耗费CPU性能。

最大线程数不是越大越好，因为线程越多，CPU上下文切换的开销越大，存在一个“阈值”，对于一个4核8G的服务器，经验值是800。

而最小线程数设为100，则是为了应付一些突发情况。

这样，当正常运行时，Tomcat维护了大概100个线程左右，而当压测时，线程数量猛增到800多个。



优化为长连接

内嵌的Tomcat默认使用HTTP1.0的短连接，在Spring-boot的配置文件中无法转换为长连接。

当然Spring Boot并没有把内嵌Tomcat的所有配置都导出。一些配置需要通过 WebServerFactoryCustomizer<ConfigurableWebServerFactory>接口来实现自定义，写一个配置文件。

这里需要自定义KeepAlive长连接的配置，减少客户端和服务器的连接请求次数，避免重复建立连接，提高性能。



##### Nginx负载均衡配置

使用Nginx反向代理两台服务器和静态资源的磁盘。前端访问时，静态资源请求和Ajax请求分开访问不同的服务器。

1.静态资源的路径绑定，在nginx.conf中修改

location /resources/ {

​	alias /usr/local/openresty/nginx/html/resources/;  //alias一般起替换作用

​	index index.html index.html;

}

这样，用户就能通过http://miaoshaserver/resources/访问到静态页面。

2.nginx开启对两台服务器的反响代理

Ajax请求通过Nginx反向代理到两台应用服务器，实现负载均衡。在nginx.conf里面添加以下字段：

upstream backend_server{

​    server miaoshaApp1_ip（这个地方写被代理服务器的IP地址） weight=1; 	//weight设置的是权重，权重越大，被访问的机率越大

​    server miaoshaApp2_ip   weight=1;

​    keepalive 30;	//长连接维护时间30s

}

...

server{

​    location / {

​        proxy_pass http://backend_server;

​        proxy_set_header Host $http_host:$proxy_port;

​        proxy_set_header X-Real-IP $remote_addr;

​        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;

 proxy_http_version 1.1;	//开启HTTP1.1模式

 proxy_set_header Connection "";

​    }

}

这样，用http://miaoshaserver访问Nginx服务器，请求会被均衡地代理到下面的两个代理服务器上。



##### 性能测试

##### 优化前：

<img src="https://github.com/InnovateLin/SeckillPromoService/blob/master/picture/before.png">

##### 优化后
<img src="https://github.com/InnovateLin/SeckillPromoService/blob/master/picture/after.png">



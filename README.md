# Spider工具
用于快速构建spider
## 使用说明
测试示例代码如下：
```
/**
 * 测试爬虫以及爬虫使用示例
 *
 * @throws Exception 异常
 */
@Test
public void testSpider() throws Exception {
    Spider spider = new Spider(10);
    spider.addTask("http://127.0.0.1:12345/api/getUsers", System.out::println);
}
```
以上代码会请求一次http://127.0.0.1:12345/api/getUsers，然后将请求结果（字符串）传给回调函数，如果需要请求多次，那么只需要多次调用addTask方法即可，创建Spider的构造器接受一个long类型的数字，表示对同一域名两次抓取的时间间隔，单位为毫秒，用于流控，防止请求过快。

# DB工具

### 说明
**使用以上能力时mybatis的xml配置文件将会失效**

## 使用@ResultMapDefine注解定义ResultMap的能力
使用该工具可以使用注解@ResultMapDefine定义ResultMap，使用该注解定义的ResultMap可以用在Xml中。

## 使用@Mapper注解定义Mapper的能力
使用该工具可以使用注解@Mapper定义Mapper（使用注解写sql而不是xml），定义的Mapper集成spring后可以注入。

### 使用@TypeAlias注解定义TypeAlias的能力
使用该工具可以使用注解@TypeAlias定义TypeAlias（等同于xml配置文件中的typeAlias）

### 使用注解编写sql注意事项：
- 使用注解的方式映射结果集的时候必须有对应的构造器（默认结果集使用全字段的构造器的方式注入，如果不想使用全字段的构造器的方式注入可以使用@ConstructorArgs注解，该注解和结果集类实际的构造器对应，构造器中不包含的结果集字段将使用setter注入）

- 如果使用定义在类内的内部类作为结果集的映射时该类必须使用static修饰，例如static class User

- 八大基本类型的字段如果使用构造器注入那么必须与@ConstructorArgs注解中的@Arg注解中的javaType保持一致

- 如果结果集中的字段与java中的class字段名不一致，可以使用@Results映射
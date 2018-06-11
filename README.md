# Spider工具
用于快速构建spider
## 使用说明
测试示例代码如下：
```java
import org.junit.Test;

/**
 * @author joe
 * @version 2018.03.08 17:34
 */
public class SpiderTest {
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
}
```
以上代码会请求一次`http://127.0.0.1:12345/api/getUsers`，然后将请求结果（字符串）传给回调函数，如果需要请求多次，那么只需要多次调用addTask方法即可，创建Spider的构造器接受一个long类型的数字，表示对同一域名两次抓取的时间间隔，单位为毫秒，用于流控，防止请求过快。

# DB工具

### 说明
**使用以上能力时mybatis的xml配置文件将会失效**

## 使用@ResultMapDefine注解定义ResultMap的能力
使用该工具可以使用注解@ResultMapDefine定义ResultMap，使用该注解定义的ResultMap可以用在Xml中。

### 为什么使用该能力
我们先看下传统xml定义ResultMap的方案：
```xml
<resultMap id="History" type="com.joe.spider.util.db.History">
  <id column="id" property="id"/>
  <result column="contain" property="contain"/>
  <result column="ip" property="ip"/>
  <result column="time" property="time"/>
  <result column="keyboard" property="key"/>
</resultMap>
```
然后看下使用@ResultMapDefine注解如何定义一个ResultMap：
```java
package com.joe.spider.util.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 定义在其他类中必须是static的
 *
 * @author joe
 * @version 2018.06.08 10:31
 */
@ResultMapDefine
@Data
@AllArgsConstructor
@NoArgsConstructor
public class History {
    private String id;
    private boolean contain;
    private String ip;
    private String time;
    /**
     * 数据库中该字段名为keyboard，映射到pojo中为key
     */
    @Property("keyboard")
    private String key;
}
```
这样一个和上边xml效果一样的resultMap就定义出来了。

可能看到这里有的人会问，这不就是把ResultMap定义从xml转到java类里边的吗？有什么不同吗？注意：在xml中定义resultMap时History类也
是需要存在的，只是少一个@ResultMapDefine和@Property注解，而正是该注解代替了xml中7行resultMap定义，这样区别看出来了吗？从此定
义ResultMap只需创建完pojo类后添加注解即可搞定，而不用再去xml中写一大堆定义。试想一下，如果你有50个这样的pojo，每个pojo给你节
省7行代码，那50个就是350行代码，而且相信实际项目中大多数表字段都不止7个，这会省去你相当多的时间，而且如果使用xml的方式，当你
的pojo名字更改或者字段更改，那还后续还需要在xml中维护更改，也是相当麻烦，而是用注解则就没有这种烦恼了。

## 使用@Mapper注解定义Mapper的能力
使用该工具可以使用注解@Mapper定义Mapper（使用注解写sql而不是xml），定义的Mapper集成spring后可以注入。

### 为什么使用该能力

该能力主要是在使用注解形式编写SQL的mapper时用到的，首先看Mapper定义：
```java
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author joe
 * @version 2018.06.08 11:51
 */
@Mapper
public interface Dao {
    /**
     * 查找最多10条history（ResultMap使用注解定义的ResultMap，如果需要也可以使用xml中定义的ResultMap）
     *
     * @return 最多10条history
     */
    @ResultMap("default.History")
    @Select("select * from history limit 0 , 10")
    List<History> selectAllHistory();
}
```
使用这种Mapper时需要在mybatis的配置文件中加入以下内容：
```xml
<mappers>
  <mapper class="Dao"/>
</mappers>
```

在Mapper少的时候这样并不是太麻烦，但是如果Mapper多的话就很麻烦了，而且还容易忘，如果换成注解，则只需要添加包扫描，然后在
自己的Mapper类上加入@Mapper注解，然后系统启动的时候会自动将这些Mapper添加到mybatis中而不用编写配置文件。

### 使用注解编写sql注意事项：
- 使用注解的方式映射结果集的时候必须有对应的构造器（默认结果集使用全字段的构造器的方式注入，如果不想使用全字段的构造器的
方式注入可以使用@ConstructorArgs注解，该注解和结果集类实际的构造器对应，构造器中不包含的结果集字段将使用setter注入）。

- 如果使用定义在类内的内部类作为结果集的映射时该类必须使用static修饰，例如static class User。

- 八大基本类型的字段如果使用构造器注入那么必须与@ConstructorArgs注解中的@Arg注解中的javaType保持一致。

- 如果结果集中的字段与java中的class字段名不一致，可以使用@Results映射（或者直接使用resultMap）。

## DB工具使用示例说明：独立使用mybatis说明

首先是DAO定义代码：
```java
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author joe
 * @version 2018.06.08 11:51
 */
@Mapper
public interface Dao {
    /**
     * 查找最多10条history（ResultMap使用注解定义的ResultMap，如果需要也可以使用xml中定义的ResultMap）
     *
     * @return 最多10条history
     */
    @ResultMap("default.History")
    @Select("select * from history limit 0 , 10")
    List<History> selectAllHistory();
}
```

ResultMap定义代码：
```java
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

/**
 * 定义在其他类中必须是static的
 *
 * @author joe
 * @version 2018.06.08 10:31
 */
@ResultMapDefine
@Alias("History")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class History {
    private String id;
    private boolean contain;
    private String ip;
    private String time;
    /**
     * 数据库中该字段名为keyboard，映射到pojo中为key
     */
    @Property("keyboard")
    private String key;
}
```

测试代码（Junit的测试代码可以换成main方法，数据库连接可以换成自己的数据库连接，需要该连接指向的数据库中有一个叫history的表，同时该表中至少有一条数据，该表的定义参照History类或者下方生成代码）：
```java
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * 数据库工具测试
 * <p>
 * 说明：需要给定的url中存在一个表叫history，有{@link History History}中的字段
 *
 * @author joe
 * @version 2018.06.07 18:30
 */
public class DBUtilTest {
    /**
     * 数据库连接URL，需要替换为自己的
     */
    static String url = "jdbc:mysql://dbserver:9999/movie?characterEncoding=utf-8&allowMultiQueries=true";
    /**
     * 数据库用户名，替换为自己的
     */
    static String username = "root";
    /**
     * 数据库密码，替换为自己的
     */
    static String password = "Qwer1@34";

    @Test
    public void dbTest() throws Exception {
        //构建SqlSessionFactory
        SqlSessionFactory sqlSessionFactory = DBUtil.build(url, username, password, String.valueOf(System
                .currentTimeMillis()), "History和Dao所在的包或者父级包，例如com.joe");
        //获取Dao
        Dao dao = sqlSessionFactory.openSession().getMapper(Dao.class);
        
        //查询所有历史记录
        List<History> histories = dao.selectAllHistory();
        //验证查询出来的历史记录不为空（需要表中有数据）
        Assert.assertNotEquals(0, histories.size());
    }
}
```

数据库表结构生成SQL：
```sql
CREATE TABLE "history"(
  id VARCHAR(50),
  contain TINYINT(1),
  ip VARCHAR(15),
  time VARCHAR(20),
  keyboard VARCHAR(200),
  PRIMARY KEY (id)
)
```

## DB工具使用示例说明：spring集成mybatis使用说明
集成spring的时候保留上边的ResultMap定义和Dao定义，然后添加如下文件：

首先是mybatis的映射文件
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
<mapper namespace="com.joe.spider.util.db.HistoryMapper">
    <!-- 使用注解定义的resultMap -->
    <select id="getHistoryByResultMap" resultMap="default.History">
        select * from history limit 0 , 10
    </select>

    <!--使用注解定义的resultType-->
    <select id="getHistoryByResultType" resultType="History">
        select * from history limit 0 , 10
    </select>
</mapper>
```

Mapper定义：
```java
import java.util.List;

/**
 * @author joe
 * @version 2018.06.08 10:28
 */
public interface HistoryMapper {
    /**
     * 查找最多10条history
     *
     * @return 最多10条history
     */
    List<History> getHistoryByResultMap();

    /**
     * 查找最多10条history
     *
     * @return 最多10条history
     */
    List<History> getHistoryByResultType();
}
```

测试类：
```java
import org.junit.Before;
import org.junit.Test;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

/**
 * SpringDBUtil测试
 *
 * @author joe
 * @version 2018.06.08 10:21
 */
@Configuration
public class SpringDBUtilTest {
    private HistoryMapper mapper;
    private ApplicationContext context;
    private Dao dao;

    /**
     * 测试xml和注解混合使用
     */
    @Test
    public void doMixTest() {
        System.out.println(mapper.getHistoryByResultType());
        System.out.println(mapper.getHistoryByResultMap());
        System.out.println(dao.selectAllHistory());
    }

    /**
    * 初始化 
    */
    @Before
    public void init() {
        context = new AnnotationConfigApplicationContext("替换为该类所在的包");
        mapper = context.getBean(HistoryMapper.class);
        dao = context.getBean(Dao.class);
    }

    @Bean
    public SqlSessionFactoryBean sqlSessionFactory(ResourceLoader loader) {
        // 需要数据库的url、username、password，同时需要一个ID（可以随机生成），需要一个ResourceLoader用来加载资源（可
        // 以直接按照这个方法定义，spring会自动将这个参数注入进来），还需要一个包名，所有的ResultMap和Mapper（注解定义
        // 的）需要包含在该包名下，不然会扫描不到。
        return SpringDBUtil.buildSqlSessionFactoryBean(DBUtilTest.url, DBUtilTest.username, DBUtilTest.password,
                "123", loader, "ResultMap和Mapper所在的包或者父级包，例如com.joe");
    }

    @Bean
    public MapperScannerConfigurer mapperScannerConfigurer() {
        // 两个字符串参数，第一个是SqlSessionFactoryBean的名字，第二个是定义Mapper扫描配置，该路径要尽可能精确同时除
        // 了mapper接口外尽量不要放置其他接口，不然都会当做mapper来处理生成bean，占用多余内存.
        return SpringDBUtil.buildMapperScannerConfigurer("sqlSessionFactory", "替换为mapper所在包或者父级包");
    }
}
```

运行后可以看到getHistoryByResultType方法返回的结果中key值都是null，这是因为key值在数据库中映射名为keyboard，而该方法使用的是resultType映射而不是resultMap（详情参照xml定义和mybatis文档）。

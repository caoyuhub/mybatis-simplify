# mybatis-simplify
##说明
**没有mybatis的原有功能做任何改动，升级可以直接替换掉原有的mybatis-spring-boot-starter**
##安装
###引入jar包
**springboot项目引入以下jar包（如果已经引入mybatis-spring-boot-starter则替换掉）**
```
<dependency>
  <groupId>com.github.caoyuhub</groupId>
  <artifactId>mybatis-simplify-spring-boot-starter</artifactId>
  <version>2.1.3</version>
</dependency>
```
**环境搭建过程和普通的mybatis-spring-boot-starter一模一样，这里就忽略掉。**
### 实体类的改进
**实体类中必须要有几个注释，其他并无差异**

1. @Table(name = "table_name")
2. @Column(name = "databaseColumnName")
3. @Id

**table是注解在实体类上用来标记实体类对应的表名**

**Column用来标注字段对应数据库中字段名（只能写在字段的get方法上），如果字段上没有这个注解则不会认为它是属于数据库的字段。**

**Id用来标记表中的id（只能写在字段的get方法上）**

**例如:**
```
@Table(name = "user_info")
public class User {
    private String id;
    private String name;



    @Id
    @Column(name = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "user_name" ,custom = "#key ~ #value")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

```

**其中Column 里面有一个参数是custom 是自定义查询，这里是用于模糊查询（postgresql语法）**

**解析为sql后会变为 user_name ~ 'value'**

###mapper的改进
**在原有的mapper只上继承BaseMapper即可，如果没有继承BaseMapper按照mybatis原有功能运行不受影响。**

**例如：**
```
public interface UserMapper extends BaseMapper<User,String > {

}
```
**泛型中第一个是当前mapper对应的实体类类型，第二个是实体类id的类型。**

**在BaseMapper中有基本的增删改查，可以直接使用。**

### @Select的改进
**对这个注解进行了方法名自动识别的改进，可以通过方法名自动生成对应的sql**

**可以用的适配方法如下：**

1. findFirstBy  -   Object
2. findAll    -   List
3. findBy       -   List

####例如：
######例1：findFirstBy

```
    //SELECT id,user_name FROM user WHERE user_name = ? LIMIT 1
    @Select
    User findFirstByName(@Param("name")String name)
```
**@Select的括号中什么都不用写（原有的是写sql，也可以用）@Param中的参数就是实体类重的字段名**

######例2：findAll
```
    //SELECT id,user_name FROM user WHERE user_name = ? AND id = ? ORDER BY id DESC
    @Select
    List<User> findAllOrderByIdDesc(User user)
```
**findAll是查询整个实体类(不为空的对象)，所以需要传入整个实体类对象**

######例3：findBy
```
    //SELECT id,user_name FROM user WHERE user_name = ?
    @Select
    User findByName(@Param("name")String name)


    //SELECT id,user_name FROM user WHERE user_name = ? AND id = ?
    @Select
    User findByNameAndId(@Param("name")String name,@Param("id")String id)


    //SELECT id,user_name FROM user WHERE user_name = ? OR id = ?
    @Select
    User findByNameOrId(@Param("name")String name,@Param("id")String id)
```

**findBy可以在后面灵活的添加  and 或者 or 这里or不好控制所以不建议使用**

##复杂查询
1. ORDER BY
2. IN
3. NOT IN
4. custom

**这些都是可以拼接到方法名后面的**

####例如：
######例1：ORDER BY
```
    //SELECT id,user_name FROM user WHERE user_name = ? AND id = ? ORDER BY id DESC
    @Select
    List<User> findAllOrderByIdDesc(User user)

    //SELECT id,user_name FROM user WHERE user_name = ? ORDER BY id DESC
    @Select
    List<User> findByNameOrderByIdDesc(@Param("name")String name)
```
**这里的Desc可以换成Asc**

######例2：IN
```
        //SELECT id,user_name FROM user WHERE id IN (?,?,?)
        @Select
        List<User> findByInId(@Param("id")List<String> id)
```
######例3：NOT IN
```
        //SELECT id,user_name FROM user WHERE id NOT IN (?,?,?)
        @Select
        List<User> findByNotInId(@Param("id")List<String> id)
```

######例4：custom
```
    *Entity
    @Column(name = "user_name" ,custom = "#key ~ #value")
    public String getName() {
        return name;
    }
    ---------------------------------------------------------------------

    *Mapper
    //SELECT id,user_name FROM user WHERE user_name ~ ?
    @Select
    @Custom
    List<User> findByName(@Param("name")String name)
```
**如果不实用@Custom 条件会变成 WHERE name = ?**

**由于实体类中name的注释中有custom = "#key ~ #value，所以在加入方法中加入注解@Custom会吧sql编译为name ~ ?**

**这样可以实现postgresql的模糊查询，如果是mysql的模糊查询需要在实体类中吧custom改为 #key like CONCAT('%', #value, '%')**

**在findAll和findAllxxx中会默认使用这个自定义语句的**


######例5：复杂查询
```
    //SELECT id,user_name FROM user WHERE user_name = ? AND id IN(?,?,?) ORDER BY id DESC
    @Select
    List<User> findByNameAndInIdOrderByIdDesc(@Param("name")String name,@Param("id")List<STring> ids);
```

##删除
* deleteBy
**删除和查询的写法基本一样，复杂查询那一块的语法都能拿过来用**
####例如
```
    int deleteByName(@Param("name")String name);

    int deleteByNameAndInId(@Param("name")String name,@Param("id")List<STring> ids);
```
**删除就不做过多介绍**

**分页可以结合分页插件来使用,我这里是用的PageHelper**

**修改和删除可以用BaseMapper重的insert和update**

**简单查询等可以用BaseMapper中的基本方法**

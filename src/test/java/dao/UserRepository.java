package dao;

import entity.ProductAndUser;
import org.malred.annotations.*;
import entity.TbUser;
import org.malred.repository.BaseCRUDRepository;

import java.util.List;

// 还有一种,注册带泛型的repo接口,反射获取泛型类,
// 泛型类@table注解指定表名,如果没有就通过simplename作为表名
// 然后sqlbuilder拼接sql
// 根据泛型类的字段分别创建crudbyxxx
@Repository("tb_user")
public interface UserRepository extends BaseCRUDRepository<TbUser> {
    @Select("select * from tb_user where username=?")
    public TbUser selectOneByUsername(String username);

    @Select("select * from tb_user where password!=?")
    public List<TbUser> selectOneByNEPassword(String password);


    // 提供复杂sql的执行
    @Select("select " +
            "u.id as uid,username,addr,password,gender," +
            "p.id as pid,product_name,product_time " +
            "from tb_user u " +
            "inner join tb_product p " +
            "on u.id=p.id " +
            "where u.id=?")
    public List<ProductAndUser> findUserAndProductJoin(int id);

    @Update("update tb_user set username=? where id=?")
    public int uptUser(String uname, int id);

    @Delete("delete from tb_user where id=? and password=?")
    public int delUser(int id, String password);

    @Insert("insert into tb_user(username,password) values (?,?),(?,?)")
    public int addUser(String uname1, String pass1, String uname2, String pass2);
}

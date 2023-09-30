package dao;

import org.malred.repository.BaseCRUDRepository;
import entity.TbUser;

import java.util.List;

public interface TbUserGenRepository extends BaseCRUDRepository<TbUser> {

    public List<TbUser> find_by_password_gen(String password);

    public int update_by_password_gen(
            String gender,
            String addr,
            String username,
            String password
    );

    public int delete_by_password_gen(String password);

    public List<TbUser> find_by_gender_gen(String gender);

    public int update_by_gender_gen(
            String password,
            String addr,
            String username,
            String gender
    );

    public int delete_by_gender_gen(String gender);

    public List<TbUser> find_by_addr_gen(String addr);

    public int update_by_addr_gen(
            String password,
            String gender,
            String username,
            String addr
    );

    public int delete_by_addr_gen(String addr);

    public List<TbUser> find_by_username_gen(String username);

    public int update_by_username_gen(
            String password,
            String gender,
            String addr,
            String username
    );

    public int delete_by_username_gen(String username);
}
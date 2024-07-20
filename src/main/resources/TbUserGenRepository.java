import org.malred.repository.BaseCRUDRepository;
import entity.TbUser;
import java.util.List;

public interface TbUserGenRepository extends BaseCRUDRepository<TbUser> {

    public List<TbUser> find_by_username_gen (String username);
public int update_by_username_gen (
                        String password,
                        String gender,
                        String addr,
        String username
);
public int delete_by_username_gen (String username);
    public List<TbUser> find_by_password_gen (String password);
public int update_by_password_gen (
                        String username,
                        String gender,
                        String addr,
        String password
);
public int delete_by_password_gen (String password);
    public List<TbUser> find_by_gender_gen (String gender);
public int update_by_gender_gen (
                        String username,
                        String password,
                        String addr,
        String gender
);
public int delete_by_gender_gen (String gender);
    public List<TbUser> find_by_addr_gen (String addr);
public int update_by_addr_gen (
                        String username,
                        String password,
                        String gender,
        String addr
);
public int delete_by_addr_gen (String addr);
}
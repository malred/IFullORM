import org.malred.repository.BaseCRUDRepository;
import entity.TbTest;
import java.util.List;

public interface TbTestGenRepository extends BaseCRUDRepository<TbTest> {

    public List<TbTest> find_by_username_gen (String username);
public int update_by_username_gen (
                        String email,
                        Date birthdate,
                        boolean is_active,
        String username
);
public int delete_by_username_gen (String username);
    public List<TbTest> find_by_email_gen (String email);
public int update_by_email_gen (
                        String username,
                        Date birthdate,
                        boolean is_active,
        String email
);
public int delete_by_email_gen (String email);
    public List<TbTest> find_by_birthdate_gen (Date birthdate);
public int update_by_birthdate_gen (
                        String username,
                        String email,
                        boolean is_active,
        Date birthdate
);
public int delete_by_birthdate_gen (Date birthdate);
    public List<TbTest> find_by_is_active_gen (boolean is_active);
public int update_by_is_active_gen (
                        String username,
                        String email,
                        Date birthdate,
        boolean is_active
);
public int delete_by_is_active_gen (boolean is_active);
}
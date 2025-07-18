package smart.ai.admin.v2.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import smart.ai.admin.v2.common.domain.Menu;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    
    List<Menu> findByParentIsNullOrderBySortOrder();
    
    List<Menu> findByParentIdOrderBySortOrder(Long parentId);
    
    List<Menu> findByEnabled(boolean enabled);
    
    List<Menu> findByType(Menu.MenuType type);
    
    @Query("SELECT m FROM Menu m JOIN m.roles r WHERE r.id = :roleId ORDER BY m.sortOrder")
    List<Menu> findByRoleId(@Param("roleId") Long roleId);
    
    @Query("SELECT m FROM Menu m JOIN m.roles r WHERE r.name = :roleName ORDER BY m.sortOrder")
    List<Menu> findByRoleName(@Param("roleName") String roleName);
    
    @Query("SELECT m FROM Menu m JOIN m.roles r WHERE r.id IN :roleIds ORDER BY m.sortOrder")
    List<Menu> findByRoleIds(@Param("roleIds") List<Long> roleIds);
    
    // 양방향 관계 제거로 인해 주석 처리
    // @Query("SELECT DISTINCT m FROM Menu m JOIN m.roles r JOIN r.users u WHERE u.id = :userId ORDER BY m.order")
    // List<Menu> findByUserId(@Param("userId") Long userId);
    
    // @Query("SELECT DISTINCT m FROM Menu m JOIN m.roles r JOIN r.users u WHERE u.username = :username ORDER BY m.order")
    // List<Menu> findByUsername(@Param("username") String username);
} 
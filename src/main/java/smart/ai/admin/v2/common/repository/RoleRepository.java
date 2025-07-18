package smart.ai.admin.v2.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import smart.ai.admin.v2.common.domain.Role;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    Optional<Role> findByName(String name);
    
    boolean existsByName(String name);
    
    List<Role> findByEnabled(boolean enabled);
    
    // 양방향 관계 제거로 인해 주석 처리
    // @Query("SELECT r FROM Role r JOIN r.users u WHERE u.id = :userId")
    // List<Role> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT r FROM Role r JOIN r.menus m WHERE m.id = :menuId")
    List<Role> findByMenuId(@Param("menuId") Long menuId);
} 
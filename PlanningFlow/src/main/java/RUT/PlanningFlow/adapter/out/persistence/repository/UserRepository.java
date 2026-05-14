package RUT.PlanningFlow.adapter.out.persistence.repository;

import RUT.PlanningFlow.adapter.out.persistence.entity.UserEntity;
import RUT.PlanningFlow.adapter.out.persistence.entity.UserSkillEntity;
import RUT.PlanningFlow.domain.enums.UserRoles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends BaseRepository<UserEntity, Integer> {
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmailIgnoreCase(String email);
    Page<UserEntity> findAllByOrderByUsernameAscIdAsc(Pageable pageable);

    @Query("""
            SELECT u
            FROM UserEntity u
            WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
               OR LOWER(COALESCE(u.fullName, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            ORDER BY u.username ASC, u.id ASC
            """)
    Page<UserEntity> findByUsernameContainingIgnoreCaseOrderByUsernameAscIdAsc(
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );

    @Query("""
            SELECT DISTINCT u
            FROM UserEntity u
            JOIN u.roles r
            WHERE r.name IN :roles
            ORDER BY u.username ASC, u.id ASC
            """)
    Page<UserEntity> findHavingRoles(@Param("roles") List<UserRoles> roles, Pageable pageable);

    @Query("""
            SELECT DISTINCT u
            FROM UserEntity u
            JOIN u.roles r
            WHERE r.name IN :roles
            AND (
                LOWER(u.username) LIKE LOWER(CONCAT('%', :term, '%'))
                OR LOWER(COALESCE(u.fullName, '')) LIKE LOWER(CONCAT('%', :term, '%'))
            )
            ORDER BY u.username ASC, u.id ASC
            """)
    Page<UserEntity> findHavingRolesAndUsernameContaining(
            @Param("roles") List<UserRoles> roles,
            @Param("term") String term,
            Pageable pageable
    );

    @Query("""
            SELECT DISTINCT u
            FROM UserEntity u
            JOIN u.roles r
            WHERE r.name = :role
            ORDER BY u.username ASC, u.id ASC
            """)
    Page<UserEntity> findUsersHavingRole(@Param("role") UserRoles role, Pageable pageable);

    @Query("""
            SELECT DISTINCT u
            FROM UserEntity u
            JOIN u.roles r
            WHERE r.name = :role
            AND (
                LOWER(u.username) LIKE LOWER(CONCAT('%', :term, '%'))
                OR LOWER(COALESCE(u.fullName, '')) LIKE LOWER(CONCAT('%', :term, '%'))
            )
            ORDER BY u.username ASC, u.id ASC
            """)
    Page<UserEntity> findUsersHavingRoleAndUsernameContaining(
            @Param("role") UserRoles role,
            @Param("term") String term,
            Pageable pageable
    );

    @Query("""
            SELECT DISTINCT u
            FROM UserEntity u
            JOIN u.roles rf
            JOIN u.roles rd
            WHERE rf.name = :filterRole AND rd.name IN :directoryRoles
            ORDER BY u.username ASC, u.id ASC
            """)
    Page<UserEntity> findHavingDirectoryRoleAndFilterRole(
            @Param("directoryRoles") List<UserRoles> directoryRoles,
            @Param("filterRole") UserRoles filterRole,
            Pageable pageable
    );

    @Query("""
            SELECT DISTINCT u
            FROM UserEntity u
            JOIN u.roles rf
            JOIN u.roles rd
            WHERE rf.name = :filterRole AND rd.name IN :directoryRoles
            AND (
                LOWER(u.username) LIKE LOWER(CONCAT('%', :term, '%'))
                OR LOWER(COALESCE(u.fullName, '')) LIKE LOWER(CONCAT('%', :term, '%'))
            )
            ORDER BY u.username ASC, u.id ASC
            """)
    Page<UserEntity> findHavingDirectoryRoleAndFilterRoleAndUsernameContaining(
            @Param("directoryRoles") List<UserRoles> directoryRoles,
            @Param("filterRole") UserRoles filterRole,
            @Param("term") String term,
            Pageable pageable
    );

    @Query("""
            SELECT DISTINCT u
            FROM UserEntity u
            JOIN u.roles r
            WHERE r.name = :role
            ORDER BY u.username ASC, u.id ASC
            """)
    List<UserEntity> findActiveParticipant(@Param("role") UserRoles role);

    @Query("""
            SELECT us
            FROM UserSkillEntity us
            WHERE us.user.id = :userId
            """)
    List<UserSkillEntity> findSkillsForUser(@Param("userId") Integer userId);

    @Query("SELECT COUNT(u) FROM UserEntity u")
    long countAllUsers();
}